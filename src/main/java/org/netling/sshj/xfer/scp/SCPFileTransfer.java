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
package org.netling.sshj.xfer.scp;

import org.netling.sshj.connection.channel.direct.SessionFactory;
import org.netling.sshj.xfer.AbstractFileTransfer;
import org.netling.sshj.xfer.FileTransfer;

import java.io.IOException;

public class SCPFileTransfer
        extends AbstractFileTransfer
        implements FileTransfer {

    private final SessionFactory sessionFactory;

    public SCPFileTransfer(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public SCPDownloadClient newSCPDownloadClient() {
        return new SCPDownloadClient(sessionFactory, getTransferListener(), getModeSetter());
    }

    public SCPUploadClient newSCPUploadClient() {
        return new SCPUploadClient(sessionFactory, getTransferListener(), getModeGetter());
    }

    @Override
    public void download(String remotePath, String localPath)
            throws IOException {
        newSCPDownloadClient().copy(remotePath, localPath);
    }

    @Override
    public void upload(String localPath, String remotePath)
            throws IOException {
        newSCPUploadClient().copy(localPath, remotePath);
    }

}
