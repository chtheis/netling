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
package org.netling.ssh.userauth.keyprovider;

import org.netling.ssh.common.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class KeyProviderUtil {

    /**
     * Attempts to detect how a key file is encoded.
     * <p/>
     * Return values are consistent with the {@code NamedFactory} implementations in the {@code keyprovider} package.
     *
     * @param location
     *
     * @return name of the key file format
     *
     * @throws java.io.IOException
     */
    public static FileKeyProvider.Format detectKeyFileFormat(File location)
            throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(location));
        String firstLine = br.readLine();
        IOUtils.closeQuietly(br);
        if (firstLine == null)
            throw new IOException("Empty file");
        if (firstLine.startsWith("-----BEGIN") && firstLine.endsWith("PRIVATE KEY-----"))
            if (new File(location + ".pub").exists())
                // Can delay asking for password since have unencrypted pubkey
                return FileKeyProvider.Format.OpenSSH;
            else
                // More general
                return FileKeyProvider.Format.PKCS8;
        /*
         * TODO: Tectia, PuTTY (.ppk) ...
         */
        return FileKeyProvider.Format.Unknown;
    }

}
