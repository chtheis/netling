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
package org.netling.ssh.userauth.keyprovider;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.netling.ssh.common.KeyType;

/** A KeyProvider is a container for a public-private keypair. */
public interface KeyProvider {

    /**
     * @return the private key.
     *
     * @throws IOException if there is an I/O error retrieving the private key
     */
    PrivateKey getPrivate()
            throws IOException;

    /**
     * @return the public key.
     *
     * @throws IOException if there is an I/O error retrieving the public key
     */
    PublicKey getPublic()
            throws IOException;

    /**
     * @return the {@link KeyType}.
     *
     * @throws IOException if there is an I/O error retrieving the key type
     */
    KeyType getType()
            throws IOException;

}
