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
package org.netling.sshj.sftp;

import org.netling.sshj.common.Buffer;

public class SFTPPacket<T extends SFTPPacket<T>>
        extends Buffer<T> {

    public SFTPPacket() {
        super();
    }

    public SFTPPacket(Buffer<T> buf) {
        super(buf);
    }

    public SFTPPacket(PacketType pt) {
        super();
        putByte(pt.toByte());
    }

    public FileAttributes readFileAttributes() {
        final FileAttributes.Builder builder = new FileAttributes.Builder();
        final int mask = readInt();
        if (FileAttributes.Flag.SIZE.isSet(mask))
            builder.withSize(readUINT64());
        if (FileAttributes.Flag.UIDGID.isSet(mask))
            builder.withUIDGID(readInt(), readInt());
        if (FileAttributes.Flag.MODE.isSet(mask))
            builder.withPermissions(readInt());
        if (FileAttributes.Flag.ACMODTIME.isSet(mask))
            builder.withAtimeMtime(readInt(), readInt());
        if (FileAttributes.Flag.EXTENDED.isSet(mask)) {
            final int extCount = readInt();
            for (int i = 0; i < extCount; i++)
                builder.withExtended(readString(), readString());
        }
        return builder.build();
    }

    public PacketType readType() {
        return PacketType.fromByte(readByte());
    }

    public T putFileAttributes(FileAttributes fa) {
        return putRawBytes(fa.toBytes());
    }

    public T putType(PacketType type) {
        return putByte(type.toByte());
    }

}
