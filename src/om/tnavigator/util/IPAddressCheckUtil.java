package om.tnavigator.util;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import om.Log;
import om.tnavigator.NavigatorConfig;
import util.misc.UtilityException;

public class IPAddressCheckUtil {

	/**
	 * Check whether the client's IP address is in the range of 'trusted'
	 * addresses defined in navigator.xml.
	 * @param request HTTP request
	 * @return whether the originating IP is a trusted address.
	 * @throws UnknownHostException
	 */
	public static boolean checkTrustedIP(HttpServletRequest request, Log log,
			NavigatorConfig nc) throws UtilityException
	{
		log.logDebug("IPCHECK", "Checking an IP address against the trusted list.");
		return checkIpAgainstList(request, log, nc.getTrustedAddresses());
	}

	/**
	 * Deprecated use checkTrustedIP() instead.
	 * Check whether the client's IP address is in the range of 'trusted'
	 * addresses defined in navigator.xml.
	 * @param request HTTP request
	 * @param log the logger.
	 * @param nc the Navigator config.
	 * @return whether the originating IP is a trusted address.
	 * @throws UnknownHostException
	 */
	@Deprecated
	public static boolean checkLocalIP(HttpServletRequest request, Log log,
			NavigatorConfig nc) throws UtilityException
	{
		return checkTrustedIP(request, log, nc);
	}

	/**
	 * Check whether the client's IP address is in the range of 'secure'
	 * addresses defined in navigator.xml.
	 * @param request HTTP request
	 * @param log the logger.
	 * @param nc the Navigator config.
	 * @return whether the originating IP is a trusted address.
	 * @throws UnknownHostException
	 */
	public static boolean checkSecureIP(HttpServletRequest request, Log log,
			NavigatorConfig nc) throws UtilityException
	{
		log.logDebug("IPCHECK", "Checking an IP address against the secure list.");
		return checkIpAgainstList(request, log, nc.getSecureAddresses());
	}

	/**
	 * Deprecated use checkTrustedIP() instead.
	 * Check whether the client's IP address is in a list of allowed IP address patterns.
	 * @param request HTTP request
	 * @param log the logger.
	 * @param allowedRanges list of IP address patterns that are allowed.
	 * @return whether the originating IP is a trusted address.
	 * @throws UnknownHostException
	 * @throws UtilityException
	 */
	public static boolean checkIpAgainstList(HttpServletRequest request, Log log,
			String[] allowedRanges) throws UtilityException
	{
		String clientIp = getIPAddress(request);

		if (clientIp == null)
		{
			log.logDebug("IPCHECK", "Failed to find the user's IP address. Check failed.");
			for (Enumeration<?> hdrs = request.getHeaderNames(); hdrs.hasMoreElements();)
			{
				String headerName = (String) hdrs.nextElement();
				log.logDebug("name = " + headerName + " value= "
						+ request.getHeader(headerName));
			}
			return false;
		}

		log.logDebug("IPCHECK", "Checking IP address " + clientIp);
		try
		{
			return isIPInList(InetAddress.getByName(clientIp), allowedRanges, log);
		}
		catch(UnknownHostException e)
		{
			throw new UtilityException("Invalid IP address " + clientIp);
		}
	}

	/**
	 * What this is doing is rather unclear. It seems to be cehcking whether locahost
	 * is a list of addresses.
	 * @param addresses
	 * @param nc
	 * @param log
	 * @return
	 * @throws UtilityException
	 */
	public static boolean checkIPAddress(String[] addresses, NavigatorConfig nc,
		Log log) throws UtilityException {
		boolean passed = false;
		try {
			if (null != addresses && addresses.length > 0) {
				passed = IPAddressCheckUtil.isIPInList(InetAddress.getLocalHost(),
					addresses, log);
			}
		} catch (UnknownHostException x) {
			throw new UtilityException(x);
		}
		return passed;
	}

	/**
	 * Deprecated. Do not use.
	 * Get the local host address.
	 * @return
	 * @throws UnknownHostException
	 */
	@Deprecated
	public static InetAddress getINetAddress() throws UnknownHostException {
		return InetAddress.getLocalHost();
	}

	/**
	 * Check whether a given IP address is in a list of allowed address patterns.
	 * @param address an IP address to check.
	 * @param addresses list of allowed address patterns.
	 * @param log the Log.
	 * @return whether the address matches one of the patterns.
	 * @throws UtilityException
	 */
	public static boolean isIPInList(InetAddress address, String[] addresses, Log log) throws UtilityException {
		// Handle IP6 addresses against IP6 patterns first.
		if (address instanceof Inet6Address)
		{
			for(String pattern : addresses)
			{
				Range range = parseIp6Pattern(pattern);
				if (range == null)
				{
					// Not an IP6 pattern.
					continue;
				}
				if (range.contains(address)) {
					log.logDebug("IPCHECK", "Permitted IP6 address.");
					return true;
				}
			}

			// Otherwise, is this an IP6 address that is equivalent to an IP4
			// address, so make the IP4 address and check it.
			if (((Inet6Address)address).isIPv4CompatibleAddress())
			{
				try
				{
					address = InetAddress.getByAddress(Arrays.copyOfRange(
							address.getAddress(), 12, 16));
				}
				catch(UnknownHostException e)
				{
					throw new UtilityException("Cannot convert from an IP6 to an IP4 address. " +
							"(This should never happen.)");
				}
			}
			else
			{
				return false;
			}
		}

		byte[] addressbytes = address.getAddress();
		if (addressbytes.length != 4)
		{
			throw new UtilityException("InetAddress that wasn't 4 or 16 bytes long?!");
		}

		// Check each pattern
		for (int i = 0; i < addresses.length; i++) {
			String[] bytepatterns = addresses[i].split("\\.");
			if (bytepatterns.length != 4) {
				// Not an valid IP4 pattern.
				continue;
			}

			boolean ok = true;
			for (int pos = 0; pos < 4; pos++) {
				// * allows anything
				if (bytepatterns[pos].equals("*"))
					continue;

				int addressbyte = getUnsigned(addressbytes[pos]);

				// Plain number, not a range
				if (bytepatterns[pos].indexOf('-') == -1)
				{
					if (addressbyte != Integer.parseInt(bytepatterns[pos]))
					{
						ok = false;
						break;
					}
				}
				else // Range
				{
					String[] range = bytepatterns[pos].split("-");
					if (addressbyte < Integer.parseInt(range[0])
							|| addressbyte > Integer.parseInt(range[1]))
					{
						ok = false;
						break;
					}
				}
			}

			if (ok)
			{
				log.logDebug("IPCHECK", "Permitted IP4 address.");
				return true;
			}
		}

		log.logDebug("IPCHECK", "Not a permitted address.");
		return false;
	}

	/**
	 * @param b Byte value
	 * @return The unsigned version of that byte
	 */
	public static int getUnsigned(byte b) {
		int i = b;
		if (i >= 0)
			return i;
		return 256 + i;
	}

	/**
	 * @param pattern an IP6 address range, e.g. "fe80:10cd::/64"
	 * @return the corresponding range
	 * @throws UtilityException
	 */
	protected static Range parseIp6Pattern(String pattern) throws UtilityException
	{
		String[] bits = pattern.split("/");
		if (bits.length != 2) {
			// Not a valid pattern.
			return null;
		}

		try
		{
			int maskBits = Integer.parseInt(bits[1]);
			InetAddress address = InetAddress.getByName(bits[0]);
			if (!(address instanceof Inet6Address)) {
				return null;
			}
			return new Range(address, maskBits);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	/**
	 * Represents a range of IP addresses.
	 */
	public static class Range {
		/** Low end of the address range, as an iteger (inclusive). */
		protected BigInteger low;
		/** High end of the address range, as an iteger (inclusive). */
		protected BigInteger high;

		/**
		 * Create a new address range from one address, and a number of significant bits.
		 * @param address one address in the range.
		 * @param maskBits number of significant bits that define the range.
		 */
		public Range(InetAddress address, int maskBits) {
			byte[] addressBytes = address.getAddress();
			BigInteger mask = BigInteger.ONE.negate().shiftLeft(addressBytes.length * 8 - maskBits);
			BigInteger adr = new BigInteger(addressBytes);
			low = adr.and(mask);
			high = low.add(mask.not());
		}

		/**
		 * Work out if a given address is inside the range.
		 * @param address an address.
		 * @return whether the adderss is in the range.
		 */
		public boolean contains(InetAddress address) {
			BigInteger adr = new BigInteger(address.getAddress());
			return low.compareTo(adr) <= 0 && adr.compareTo(high) <= 0;
		}
	}

	/**
	 * Try to get the remote users IP address from a HttpServletRequest, in a
	 * way that works even if the request has come through a load-balancer.
	 * @param request httpServletRequest
	 * @return The originating IP address of the session
	 */
	public static String getIPAddress(HttpServletRequest request)
	{
		// students.open.ac.uk actually provides the one with the underline,
		// but I think the more standard header would be as below.
		String clientIp = request.getHeader("client_ip");
		if (clientIp == null) {
			clientIp = request.getHeader("Client-IP");
		}
		if (clientIp == null) {
			clientIp = request.getHeader("X_FORWARDED_FOR");
		}
		if (clientIp == null) {
			clientIp = request.getRemoteAddr();
		}
		return clientIp;
	}

	/**
	 * Check an array of IP address patterns to ensure they are valid. If not,
	 * throw and exception.
	 * @param addressPatterns the patterns to validate.
	 * @param errorMessage message to throw, if the check fails. The problematic pattern is appended.
	 * @throws IOException if any of the patterns are invalid.
	 * @throws UtilityException
	 */
	public static void checkIpAddressPatterns(String[] addressPatterns, String errorMessage)
			throws UtilityException
	{
		for(int i = 0; i < addressPatterns.length; i++)
		{
			if (("." + addressPatterns[i]).matches("(.((\\d+(-\\d+)?)|\\*)){4}")) {
				// Valid IP4 pattern.
				continue;
			}

			Range range = parseIp6Pattern(addressPatterns[i]);
			if (range != null)
			{
				// Valid IP6 pattern.
				continue;
			}

			// Not a valid pattern.
			throw new UtilityException(errorMessage + addressPatterns[i]);
		}
	}
}
