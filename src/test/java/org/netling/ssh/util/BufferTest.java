/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netling.ssh.util;

import org.netling.ssh.common.Buffer;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/** Tests {@link Buffer} functionality */
public class BufferTest {
    private Buffer.PlainBuffer posBuf;
    private Buffer.PlainBuffer handyBuf;

    @Before
    public void setUp()
            throws UnsupportedEncodingException, GeneralSecurityException {
        // for position test
        byte[] data = "Hello".getBytes("UTF-8");
        posBuf = new Buffer.PlainBuffer(data);
        handyBuf = new Buffer.PlainBuffer();
    }

    @Test
    public void testDataTypes() {
        // bool
        assertEquals(handyBuf.putBoolean(true).readBoolean(), true);

        // byte
        assertEquals(handyBuf.putByte((byte) 10).readByte(), (byte) 10);

        // byte array
        assertArrayEquals(handyBuf.putBytes("some string".getBytes()).readBytes(), "some string".getBytes());

        // mpint
        BigInteger bi = new BigInteger("1111111111111111111111111111111");
        assertEquals(handyBuf.putMPInt(bi).readMPInt(), bi);

        // string
        assertEquals(handyBuf.putString("some string").readString(), "some string");

        // uint32
        assertEquals(handyBuf.putInt(0xffffffffL).readLong(), 0xffffffffL);
    }

    @Test
    public void testPassword() {
        char[] pass = "lolcatz".toCharArray();
        // test if put correctly as a string
        assertEquals(new Buffer.PlainBuffer().putSensitiveString(pass).readString(), "lolcatz");
        // test that char[] was blanked out
        assertArrayEquals(pass, "       ".toCharArray());
    }

    @Test
    public void testPosition()
            throws UnsupportedEncodingException {
        assertEquals(5, posBuf.wpos());
        assertEquals(0, posBuf.rpos());
        assertEquals(5, posBuf.available());
        // read some bytes
        byte b = posBuf.readByte();
        assertEquals(b, (byte) 'H');
        assertEquals(1, posBuf.rpos());
        assertEquals(4, posBuf.available());
    }

    @Test
    public void testPublickey() {
        // TODO stub
    }

    @Test
    public void testSignature() {
        // TODO stub
    }

    @Test(expected = Buffer.BufferException.class)
    public void testUnderflow() {
        // exhaust the buffer
        for (int i = 0; i < 5; ++i)
            posBuf.readByte();
        // underflow
        posBuf.readByte();
    }

}
