/*
 * Copyright 2010 netling project <http://netling.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netling.ssh.userauth.method;

import org.netling.ssh.common.Message;
import org.netling.ssh.common.SSHPacket;
import org.netling.ssh.transport.TransportException;
import org.netling.ssh.userauth.UserAuthException;

/** Implements the {@code keyboard-interactive} authentication method. */
public class AuthKeyboardInteractive
        extends AbstractAuthMethod {

    private final ChallengeResponseProvider provider;

    public AuthKeyboardInteractive(ChallengeResponseProvider provider) {
        super("keyboard-interactive");
        this.provider = provider;
    }

    @Override
    public SSHPacket buildReq()
            throws UserAuthException {
        return super.buildReq() // the generic stuff
                .putString("") // lang-tag
                .putString(buildCommaSeparatedSubmethodList());
    }

    private String buildCommaSeparatedSubmethodList() {
        StringBuilder sb = new StringBuilder();
        for (String submethod : provider.getSubmethods()) {
            if (sb.length() > 0)
                sb.append(",");
            sb.append(submethod);
        }
        return sb.toString();
    }

    private static class CharArrWrap {
        private final char[] arr;

        private CharArrWrap(char[] arr) {
            this.arr = arr;
        }
    }

    @Override
    public void handle(Message cmd, SSHPacket buf)
            throws UserAuthException, TransportException {
        if (cmd != Message.USERAUTH_60) {
            super.handle(cmd, buf);
        } else {
            provider.init(makeAccountResource(), buf.readString(), buf.readString());
            buf.readString(); // lang-tag
            final int numPrompts = buf.readInt();
            final CharArrWrap[] userReplies = new CharArrWrap[numPrompts];
            for (int i = 0; i < numPrompts; i++) {
                final String prompt = buf.readString();
                final boolean echo = buf.readBoolean();
                log.info("Requesting response for challenge `{}`; echo={}", prompt, echo);
                userReplies[i] = new CharArrWrap(provider.getResponse(prompt, echo));
            }
            respond(userReplies);
        }
    }

    private void respond(CharArrWrap[] userReplies)
            throws TransportException {
        final SSHPacket pkt = new SSHPacket(Message.USERAUTH_INFO_RESPONSE).putInt(userReplies.length);
        for (final CharArrWrap response : userReplies)
            pkt.putSensitiveString(response.arr);
        params.getTransport().write(pkt);
    }

    @Override
    public boolean shouldRetry() {
        return provider.shouldRetry();
    }

}
