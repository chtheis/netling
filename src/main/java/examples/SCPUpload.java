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
package examples;

import org.netling.scp.SCPFileTransfer;
import org.netling.ssh.SSHClient;

import java.io.File;
import java.io.IOException;

/** This example demonstrates uploading of a file over SCP to the SSH server. */
public class SCPUpload {

    public static void main(String[] args)
            throws IOException, ClassNotFoundException {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts();
        ssh.connect("localhost");
        try {
            ssh.authPublickey(System.getProperty("user.name"));

            // Present here to demo algorithm renegotiation - could have just put this before connect()
            // Make sure JZlib is in classpath for this to work
            ssh.useCompression();

            final String src = System.getProperty("user.home") + File.separator + "test_file";
            final String target = "/tmp/";
            final SCPFileTransfer xfer = new SCPFileTransfer(ssh);
            xfer.upload(src, target);
        } finally {
            ssh.disconnect();
        }
    }
}
