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
package org.netling.ssh.transport.kex;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import org.netling.ssh.common.Buffer;
import org.netling.ssh.common.ByteArrayUtils;
import org.netling.ssh.common.DisconnectReason;
import org.netling.ssh.common.Factory;
import org.netling.ssh.common.KeyType;
import org.netling.ssh.common.Message;
import org.netling.ssh.common.SSHPacket;
import org.netling.ssh.signature.Signature;
import org.netling.ssh.transport.Transport;
import org.netling.ssh.transport.TransportException;
import org.netling.ssh.transport.digest.Digest;
import org.netling.ssh.transport.digest.SHA1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for DHG key exchange algorithms. Implementations will only have to configure the required data on the
 * {@link DH} class in the
 */
public abstract class AbstractDHG
        implements KeyExchange {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Transport trans;

    private final Digest sha = new SHA1();
    private final DH dh = new DH();

    private byte[] V_S;
    private byte[] V_C;
    private byte[] I_S;
    private byte[] I_C;

    private byte[] e;
    private byte[] K;
    private byte[] H;
    private PublicKey hostKey;

    @Override
    public byte[] getH() {
        return ByteArrayUtils.copyOf(H);
    }

    @Override
    public byte[] getK() {
        return ByteArrayUtils.copyOf(K);
    }

    @Override
    public Digest getHash() {
        return sha;
    }

    @Override
    public PublicKey getHostKey() {
        return hostKey;
    }

    @Override
    public void init(Transport trans, byte[] V_S, byte[] V_C, byte[] I_S, byte[] I_C)
            throws GeneralSecurityException, TransportException {
        this.trans = trans;
        this.V_S = ByteArrayUtils.copyOf(V_S);
        this.V_C = ByteArrayUtils.copyOf(V_C);
        this.I_S = ByteArrayUtils.copyOf(I_S);
        this.I_C = ByteArrayUtils.copyOf(I_C);
        sha.init();
        initDH(dh);
        e = dh.getE();

        log.info("Sending SSH_MSG_KEXDH_INIT");
        trans.write(new SSHPacket(Message.KEXDH_INIT).putMPInt(e));
    }

    @Override
    public boolean next(Message msg, SSHPacket packet)
            throws GeneralSecurityException, TransportException {
        if (msg != Message.KEXDH_31)
            throw new TransportException(DisconnectReason.KEY_EXCHANGE_FAILED, "Unexpected packet: " + msg);

        log.info("Received SSH_MSG_KEXDH_REPLY");
        final byte[] K_S = packet.readBytes();
        final byte[] f = packet.readMPIntAsBytes();
        final byte[] sig = packet.readBytes(); // signature sent by server
        dh.setF(new BigInteger(f));
        K = dh.getK();

        hostKey = new Buffer.PlainBuffer(K_S).readPublicKey();

        final Buffer.PlainBuffer buf = new Buffer.PlainBuffer()
                .putString(V_C)
                .putString(V_S)
                .putString(I_C)
                .putString(I_S)
                .putString(K_S)
                .putMPInt(e)
                .putMPInt(f)
                .putMPInt(K);
        sha.update(buf.array(), 0, buf.available());
        H = sha.digest();

        Signature signature = Factory.Named.Util.create(trans.getConfig().getSignatureFactories(),
                                                        KeyType.fromKey(hostKey).toString());
        signature.init(hostKey, null);
        signature.update(H, 0, H.length);
        if (!signature.verify(sig))
            throw new TransportException(DisconnectReason.KEY_EXCHANGE_FAILED,
                                         "KeyExchange signature verification failed");
        return true;
    }

    protected abstract void initDH(DH dh);

}
