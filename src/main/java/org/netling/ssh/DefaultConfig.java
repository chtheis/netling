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

package org.netling.ssh;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.netling.ssh.common.Factory;
import org.netling.ssh.common.SecurityUtils;
import org.netling.ssh.signature.SignatureDSA;
import org.netling.ssh.signature.SignatureRSA;
import org.netling.ssh.transport.cipher.AES128CBC;
import org.netling.ssh.transport.cipher.AES128CTR;
import org.netling.ssh.transport.cipher.AES192CBC;
import org.netling.ssh.transport.cipher.AES192CTR;
import org.netling.ssh.transport.cipher.AES256CBC;
import org.netling.ssh.transport.cipher.AES256CTR;
import org.netling.ssh.transport.cipher.BlowfishCBC;
import org.netling.ssh.transport.cipher.Cipher;
import org.netling.ssh.transport.cipher.TripleDESCBC;
import org.netling.ssh.transport.compression.NoneCompression;
import org.netling.ssh.transport.kex.DHG1;
import org.netling.ssh.transport.kex.DHG14;
import org.netling.ssh.transport.mac.HMACMD5;
import org.netling.ssh.transport.mac.HMACMD596;
import org.netling.ssh.transport.mac.HMACSHA1;
import org.netling.ssh.transport.mac.HMACSHA196;
import org.netling.ssh.transport.random.BouncyCastleRandom;
import org.netling.ssh.transport.random.JCERandom;
import org.netling.ssh.transport.random.SingletonRandomFactory;
import org.netling.ssh.userauth.keyprovider.OpenSSHKeyFile;
import org.netling.ssh.userauth.keyprovider.PKCS8KeyFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Config} that is initialized as follows. Items marked with an asterisk are added to the config only if
 * BouncyCastle is in the classpath.
 * <p/>
 * <ul> <li>{@link ConfigImpl#setKeyExchangeFactories Key exchange}: {@link DHG14}*, {@link DHG1}</li> <li>{@link
 * ConfigImpl#setCipherFactories Ciphers} [1]: {@link AES128CTR}, {@link AES192CTR}, {@link AES256CTR}, {@link
 * AES128CBC}, {@link AES192CBC}, {@link AES256CBC}, {@link AES192CBC}, {@link TripleDESCBC}, {@link BlowfishCBC}</li>
 * <li>{@link ConfigImpl#setMACFactories MAC}: {@link HMACSHA1}, {@link HMACSHA196}, {@link HMACMD5}, {@link
 * HMACMD596}</li> <li>{@link ConfigImpl#setCompressionFactories Compression}: {@link NoneCompression}</li> <li>{@link
 * ConfigImpl#setSignatureFactories Signature}: {@link SignatureRSA}, {@link SignatureDSA}</li> <li>{@link
 * ConfigImpl#setRandomFactory PRNG}: {@link BouncyCastleRandom}* or {@link JCERandom}</li> <li>{@link
 * ConfigImpl#setFileKeyProviderFactories Key file support}: {@link PKCS8KeyFile}*, {@link OpenSSHKeyFile}*</li>
 * <li>{@link ConfigImpl#setVersion Client version}: {@code "NET_3_0"}</li> </ul>
 * <p/>
 * [1] It is worth noting that Sun's JRE does not have the unlimited cryptography extension enabled by default. This
 * prevents using ciphers with strength greater than 128.
 */
public class DefaultConfig
        extends ConfigImpl {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String VERSION = "NETLING_1_0";

    public DefaultConfig() {
        setVersion(VERSION);
        final boolean bouncyCastleRegistered = SecurityUtils.isBouncyCastleRegistered();
        initKeyExchangeFactories(bouncyCastleRegistered);
        initRandomFactory(bouncyCastleRegistered);
        initFileKeyProviderFactories(bouncyCastleRegistered);
        initCipherFactories();
        initCompressionFactories();
        initMACFactories();
        initSignatureFactories();
    }

    protected void initKeyExchangeFactories(boolean bouncyCastleRegistered) {
        if (bouncyCastleRegistered)
            setKeyExchangeFactories(new DHG14.Factory(), new DHG1.Factory());
        else
            setKeyExchangeFactories(new DHG1.Factory());
    }

    protected void initRandomFactory(boolean bouncyCastleRegistered) {
        setRandomFactory(new SingletonRandomFactory(bouncyCastleRegistered ? new BouncyCastleRandom.Factory() : new JCERandom.Factory()));
    }

    protected void initFileKeyProviderFactories(boolean bouncyCastleRegistered) {
        if (bouncyCastleRegistered) {
            setFileKeyProviderFactories(new PKCS8KeyFile.Factory(), new OpenSSHKeyFile.Factory());
        }
    }


    protected void initCipherFactories() {
        List<Factory.Named<Cipher>> avail = new LinkedList<Factory.Named<Cipher>>(Arrays.<Factory.Named<Cipher>>asList(
                new AES128CTR.Factory(),
                new AES192CTR.Factory(),
                new AES256CTR.Factory(),
                new AES128CBC.Factory(),
                new AES192CBC.Factory(),
                new AES256CBC.Factory(),
                new TripleDESCBC.Factory(),
                new BlowfishCBC.Factory()));

        boolean warn = false;
        // Ref. https://issues.apache.org/jira/browse/SSHD-24
        // "AES256 and AES192 requires unlimited cryptography extension"
        for (Iterator<Factory.Named<Cipher>> i = avail.iterator(); i.hasNext();) {
            final Factory.Named<Cipher> f = i.next();
            try {
                final Cipher c = f.create();
                final byte[] key = new byte[c.getBlockSize()];
                final byte[] iv = new byte[c.getIVSize()];
                c.init(Cipher.Mode.Encrypt, key, iv);
            } catch (Exception e) {
                warn = true;
                i.remove();
            }
        }
        if (warn)
            log.warn("Disabling high-strength ciphers: cipher strengths apparently limited by JCE policy");

        setCipherFactories(avail);
    }

    protected void initSignatureFactories() {
        setSignatureFactories(new SignatureRSA.Factory(), new SignatureDSA.Factory());
    }

    protected void initMACFactories() {
        setMACFactories(new HMACSHA1.Factory(), new HMACSHA196.Factory(), new HMACMD5.Factory(),
                        new HMACMD596.Factory());
    }

    protected void initCompressionFactories() {
        setCompressionFactories(new NoneCompression.Factory());
    }


}
