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
 *
 * This file may incorporate work covered by the following copyright and
 * permission notice:
 *
 *     Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing,
 *      software distributed under the License is distributed on an
 *      "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *      KIND, either express or implied.  See the License for the
 *      specific language governing permissions and limitations
 *      under the License.
 */
package org.netling.ssh.signature;

import org.netling.ssh.common.KeyType;
import org.netling.ssh.common.SSHRuntimeException;

import java.security.SignatureException;

/** RSA {@link Signature} */
public class SignatureRSA
        extends AbstractSignature {

    /** A named factory for RSA {@link Signature} */
    public static class Factory
            implements org.netling.ssh.common.Factory.Named<Signature> {

        @Override
        public Signature create() {
            return new SignatureRSA();
        }

        @Override
        public String getName() {
            return KeyType.RSA.toString();
        }

    }

    public SignatureRSA() {
        super("SHA1withRSA");
    }

    @Override
    public byte[] sign() {
        try {
            return signature.sign();
        } catch (SignatureException e) {
            throw new SSHRuntimeException(e);
        }
    }

    @Override
    public boolean verify(byte[] sig) {
        sig = extractSig(sig);
        try {
            return signature.verify(sig);
        } catch (SignatureException e) {
            throw new SSHRuntimeException(e);
        }
    }

}
