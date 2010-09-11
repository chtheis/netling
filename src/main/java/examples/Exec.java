/*
 * Copyright 2010 netling project
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

import org.netling.ssh.SSHClient;
import org.netling.ssh.connection.channel.direct.Session;
import org.netling.ssh.connection.channel.direct.Session.Command;

import java.io.IOException;

/** This examples demonstrates how a remote command can be executed. */
public class Exec {

    public static void main(String... args)
            throws IOException {
        final SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts();

        ssh.connect("localhost");
        try {
            ssh.authPublickey(System.getProperty("user.name"));
            final Session session = ssh.startSession();
            try {
                final Command cmd = session.exec("ping -c 1 google.com");
                System.out.print(cmd.getOutputAsString());
                System.out.println("\n** exit status: " + cmd.getExitStatus());
            } finally {
                session.close();
            }

        } finally {
            ssh.disconnect();
        }
    }

}
