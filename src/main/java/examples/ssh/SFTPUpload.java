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
package examples.ssh;

import java.io.File;
import java.io.IOException;

import org.netling.sftp.SFTPClient;
import org.netling.ssh.SSHClient;

/** This example demonstrates uploading of a file over SFTP to the SSH server. */
public class SFTPUpload {

    public static void main(String[] args)
            throws IOException {
        final SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts();
        ssh.connect("localhost");
        try {
            ssh.authPublickey(System.getProperty("user.name"));
            final String src = System.getProperty("user.home") + File.separator + "test_file";
            final String target = "/tmp/";
            final SFTPClient sftp = new SFTPClient(ssh);
            try {
                sftp.put(src, target);
            } finally {
                sftp.close();
            }
        } finally {
            ssh.disconnect();
        }
    }

}
