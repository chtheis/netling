/*
* Copyright 2010 netling project <http://netling.org>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.netling.ftp;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.junit.Test;

/**
 * Test the socket connect timeout functionality
 * @author Rory <rwinston@apache.org>
 *
 */
public class TestConnectTimeout  {

	@Test
    public void testConnectTimeout() throws SocketException, IOException {
        FTPClient client = new FTPClient();
        client.setConnectTimeout(1000);
        
        try {
            // Connect to a valid host on a bogus port
            client.connect("ftp.microsoft.com", 1234);
            assertTrue("Expecting SocketTimeoutException", false);
        } 
        catch (SocketTimeoutException se) {
            assertTrue(true);
        }
        catch (UnknownHostException ue) {
            // Not much we can do about this, we may be firewalled
            assertTrue(true);
        }
        
    }
}
