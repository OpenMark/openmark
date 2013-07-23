/* OpenMark online assessment system
   Copyright (C) 2007 The Open University

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package om.tnavigator.util;

import java.net.InetAddress;

import om.AbstractTestCase;

import org.junit.Assert;
import org.junit.Test;

import util.misc.UtilityException;

/**
 * JUnit test cases for util.xml.IPAddressCheckUtil.
 */
public class IPAddressCheckUtilTest extends AbstractTestCase
{
	@Test
	public void testIP4() throws Exception
	{
		String[] allowedAddresses = new String[] {"137.108.*.*", "194.66.128-159.*", "1.2.3.4"};

		Assert.assertTrue(IPAddressCheckUtil.isIPInList(
				InetAddress.getByName("137.108.176.69"),
				allowedAddresses, log));

		Assert.assertFalse(IPAddressCheckUtil.isIPInList(
				InetAddress.getByName("137.109.176.69"),
				allowedAddresses, log));

		Assert.assertFalse(IPAddressCheckUtil.isIPInList(
				InetAddress.getByName("194.66.127.5"),
				allowedAddresses, log));

		Assert.assertTrue(IPAddressCheckUtil.isIPInList(
				InetAddress.getByName("194.66.128.5"),
				allowedAddresses, log));

		Assert.assertTrue(IPAddressCheckUtil.isIPInList(
				InetAddress.getByName("194.66.159.5"),
				allowedAddresses, log));

		Assert.assertFalse(IPAddressCheckUtil.isIPInList(
				InetAddress.getByName("194.66.160.5"),
				allowedAddresses, log));

		Assert.assertTrue(IPAddressCheckUtil.isIPInList(
				InetAddress.getByName("1.2.3.4"),
				allowedAddresses, log));
	}

	@Test
	public void testRangeIP4() throws Exception {
		IPAddressCheckUtil.Range range = new IPAddressCheckUtil.Range(
				InetAddress.getByName("192.168.56.1"), 24);
		Assert.assertTrue(range.contains(InetAddress.getByName("192.168.56.1")));
		Assert.assertFalse(range.contains(InetAddress.getByName("192.168.55.255")));
		Assert.assertTrue(range.contains(InetAddress.getByName("192.168.56.0")));
		Assert.assertTrue(range.contains(InetAddress.getByName("192.168.56.255")));
		Assert.assertFalse(range.contains(InetAddress.getByName("192.168.57.0")));
	}

	@Test
	public void testRangeIP6() throws Exception {
		IPAddressCheckUtil.Range range = new IPAddressCheckUtil.Range(
				InetAddress.getByName("fe80::d1ae:e8:fdf0:1274"), 96);
		Assert.assertTrue(range.contains(InetAddress.getByName("fe80::d1ae:e8:fdf0:1274")));
		Assert.assertFalse(range.contains(InetAddress.getByName("fe80::d1ae:e7:ffff:ffff")));
		Assert.assertTrue(range.contains(InetAddress.getByName("fe80::d1ae:e8:0:0")));
		Assert.assertTrue(range.contains(InetAddress.getByName("fe80::d1ae:e8:ffff:ffff")));
		Assert.assertFalse(range.contains(InetAddress.getByName("fe80::d1ae:e9:0:0")));
	}

	@Test
	public void testIP6() throws Exception
	{
		String[] allowedAddresses = new String[] {"fe80::d1ae:e8:fdf0:1274/128", "fe80::10:20:0:0/96"};

		Assert.assertTrue(IPAddressCheckUtil.isIPInList(InetAddress.getByName("fe80::d1ae:e8:fdf0:1274"),
				allowedAddresses, log));

		Assert.assertFalse(IPAddressCheckUtil.isIPInList(InetAddress.getByName("fe80::10:19:ffff:ffff"),
				allowedAddresses, log));

		Assert.assertTrue(IPAddressCheckUtil.isIPInList(InetAddress.getByName("fe80::10:20:0:0"),
				allowedAddresses, log));

		Assert.assertTrue(IPAddressCheckUtil.isIPInList(InetAddress.getByName("fe80::10:20:ffff:ffff"),
				allowedAddresses, log));

		Assert.assertFalse(IPAddressCheckUtil.isIPInList(InetAddress.getByName("fe80::10:21:0:0"),
				allowedAddresses, log));
	}

	@Test
	public void testCheckIpAddressPatternsValid() throws Exception
	{
		IPAddressCheckUtil.checkIpAddressPatterns(new String[] {
				"137.108.*.*", "194.66.128-159.*", "1.2.3.4", "fe80::d1ae:e8:fdf0:1274/128", "fe80::10:20:0:0/96"},
				"Error ");
	}

	@Test(expected=UtilityException.class)
	public void testCheckIpAddressPatternsInvalidIp4() throws Exception
	{
		IPAddressCheckUtil.checkIpAddressPatterns(new String[] {
				"1.2.3.4/24"}, "Error ");
	}

	@Test(expected=UtilityException.class)
	public void testCheckIpAddressPatternsInvalidIp6() throws Exception
	{
		IPAddressCheckUtil.checkIpAddressPatterns(new String[] {
				"fe80::d1ae.e8:fdf0:1274/64"}, "Error ");
	}

	@Test(expected=UtilityException.class)
	public void testCheckIpAddressPatternsInvalidCrazy() throws Exception
	{
		IPAddressCheckUtil.checkIpAddressPatterns(new String[] {
				"frog"}, "Error ");
	}
}
