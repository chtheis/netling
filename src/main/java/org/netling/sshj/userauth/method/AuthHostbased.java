/*
 * Copyright 2010 Shikhar Bhushan
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
package org.netling.sshj.userauth.method;

import org.netling.sshj.common.SSHPacket;
import org.netling.sshj.userauth.UserAuthException;
import org.netling.sshj.userauth.keyprovider.KeyProvider;

/** Implements the {@code hostbased} SSH authentication method. */
public class AuthHostbased
        extends KeyedAuthMethod {

    protected final String hostname;
    protected final String hostuser;

    public AuthHostbased(KeyProvider kProv, String hostname, String hostuser) {
        super("hostbased", kProv);
        this.hostname = hostname;
        this.hostuser = hostuser;
    }

    @Override
    protected SSHPacket buildReq()
            throws UserAuthException {
        final SSHPacket req = putPubKey(super.buildReq());
        req.putString(hostname).putString(hostuser);
        return putSig(req);
    }

}
