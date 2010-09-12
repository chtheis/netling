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
package org.netling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.netling.util.SubnetUtils;
import org.netling.util.SubnetUtils.SubnetInfo;

public class SubnetUtilsTest {

	@Test
	public void testParseSimpleNetmask() {
		final String address = "192.168.0.1";
		final String masks[] = new String[] { "255.0.0.0", "255.255.0.0", "255.255.255.0", "255.255.255.248"};
		final String bcastAddresses[] = new String[] { "192.255.255.255", "192.168.255.255", "192.168.0.255", "192.168.0.7"};
		final String lowAddresses[] = new String[] { "192.0.0.1", "192.168.0.1", "192.168.0.1", "192.168.0.1" };
		final String highAddresses[] = new String[] { "192.255.255.254", "192.168.255.254", "192.168.0.254", "192.168.0.6" };
		final String networkAddresses[] = new String[] { "192.0.0.0", "192.168.0.0", "192.168.0.0", "192.168.0.0" };
		final String cidrSignatures[] = new String[] { "192.168.0.1/8", "192.168.0.1/16", "192.168.0.1/24", "192.168.0.1/29" };
		final int usableAddresses[] = new int[] { 16777214, 65534, 254, 6 };

		for (int i = 0; i < masks.length; ++i) {
			SubnetUtils utils = new SubnetUtils(address, masks[i]);
			SubnetInfo info = utils.getInfo();
			assertEquals(bcastAddresses[i], info.getBroadcastAddress());
			assertEquals(cidrSignatures[i], info.getCidrSignature());
			assertEquals(lowAddresses[i], info.getLowAddress());
			assertEquals(highAddresses[i], info.getHighAddress());
			assertEquals(networkAddresses[i], info.getNetworkAddress());
			assertEquals(usableAddresses[i], info.getAddressCount());
		}
	}

	// TODO Lower address test
	@Test
	public void testAddresses() {
		SubnetUtils utils = new SubnetUtils("192.168.0.1/29");
		SubnetInfo info = utils.getInfo();
		assertTrue(info.isInRange("192.168.0.1"));
		// We don't count the broadcast address as usable
		assertFalse(info.isInRange("192.168.0.7"));
		assertFalse(info.isInRange("192.168.0.8"));
		assertFalse(info.isInRange("10.10.2.1"));
		assertFalse(info.isInRange("192.168.1.1"));
		assertFalse(info.isInRange("192.168.0.255"));
	}

	@Test
	public void testZeroNetmaskBits() {
		try {
			@SuppressWarnings("unused")
			SubnetUtils utils = new SubnetUtils("192.168.0.1/0");
			fail("Mask /0 should have generated an IllegalArgumentException");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	/**
	 * Test using the inclusiveHostCount flag, which includes
	 * the network and broadcast addresses in host counts
	 */
	@Test
	public void testCidrAddresses() {
		SubnetUtils utils = new SubnetUtils("192.168.0.1/8");
		utils.setInclusiveHostCount(true);
		SubnetInfo info = utils.getInfo();
		assertEquals("255.0.0.0",info.getNetmask());
		assertEquals(16777216, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/9");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.128.0.0",info.getNetmask());
		assertEquals(8388608, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/10");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.192.0.0",info.getNetmask());
		assertEquals(4194304, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/11");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.224.0.0",info.getNetmask());
		assertEquals(2097152, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/12");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.240.0.0",info.getNetmask());
		assertEquals(1048576, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/13");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.248.0.0",info.getNetmask());
		assertEquals(524288, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/14");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.252.0.0",info.getNetmask());
		assertEquals(262144, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/15");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.254.0.0",info.getNetmask());
		assertEquals(131072, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/16");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.0.0",info.getNetmask());
		assertEquals(65536, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/17");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.128.0",info.getNetmask());
		assertEquals(32768, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/18");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.192.0",info.getNetmask());
		assertEquals(16384, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/19");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.224.0",info.getNetmask());
		assertEquals(8192, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/20");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.240.0",info.getNetmask());
		assertEquals(4096, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/21");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.248.0",info.getNetmask());
		assertEquals(2048, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/22");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.252.0",info.getNetmask());
		assertEquals(1024, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/23");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.254.0",info.getNetmask());
		assertEquals(512, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/24");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.255.0",info.getNetmask());
		assertEquals(256, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/25");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.255.128",info.getNetmask());
		assertEquals(128, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/26");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.255.192",info.getNetmask());
		assertEquals(64, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/27");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.255.224",info.getNetmask());
		assertEquals(32, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/28");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.255.240",info.getNetmask());
		assertEquals(16, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/29");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.255.248",info.getNetmask());
		assertEquals(8, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/30");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.255.252",info.getNetmask());
		assertEquals(4, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/31");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.255.254",info.getNetmask());
		assertEquals(2, info.getAddressCount());

		utils = new SubnetUtils("192.168.0.1/32");
		utils.setInclusiveHostCount(true);
		info = utils.getInfo();
		assertEquals("255.255.255.255",info.getNetmask());
		assertEquals(1, info.getAddressCount());
	}
}

