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

/**
 * {@code none} auth. No authentication information is exchanged in the request packet save username and the next
 * service requested. This method generally fails and is typically used to find out the method allowed by an SSH server
 * (sent as part of the {@code SSH_MSG_USERAUTH_FAILURE} packet)
 */
public class AuthNone
        extends AbstractAuthMethod {

    AuthNone() {
        super("none");
    }

}
