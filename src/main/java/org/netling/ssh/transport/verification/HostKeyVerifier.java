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
package org.netling.ssh.transport.verification;

import java.security.PublicKey;

/** Host key verification interface. */
public interface HostKeyVerifier {

    /**
     * This callback is invoked when the server's host key needs to be verified. The return value indicates to the
     * caller whether the SSH connection should proceed.
     * <p/>
     * <strong>Note</strong>: host key verification is the basis for security in SSH, therefore exercise due caution in
     * implementing!
     *
     * @param hostname remote hostname
     * @param port     remote port
     * @param key      host key of server
     *
     * @return {@code true} if key is acceptable, {@code false} otherwise
     */
    boolean verify(String hostname, int port, PublicKey key);

}
