package om.tnavigator;

import java.net.InetAddress;
import java.net.UnknownHostException;

import util.misc.UtilityException;

public class IPAddressCheckUtil {

	public static boolean checkIPAddress(String[] addresses, NavigatorConfig nc,
		Log log) throws UtilityException {
		boolean passed = false;
		try {
			if (null != addresses ? addresses.length > 0 : false) {
				passed = IPAddressCheckUtil.isIPInList(getINetAddress(),
					addresses, log);
			}
		} catch (UnknownHostException x) {
			throw new UtilityException(x);
		}
		return passed;
	}
	
	public static InetAddress getINetAddress() throws UnknownHostException {
		return InetAddress.getLocalHost();
	}

	public static boolean isIPInList(InetAddress ia, String[] addresses, Log l) {
		l.logDebug("IPCHECK", "isIPInList");
		byte[] ab = ia.getAddress();
		if (ab.length == 16) {
			// Check that IPv6 addresses are actually representations of IPv4 -
			// these have lots of zeros then either 0000 or FFFF, then the
			// number
			for (int i = 0; i < 10; i++)
				if (ab[i] != 0)
					return false;
			if (!((ab[10] == 0xff && ab[11] == 0xff) || (ab[10] == 0 && ab[11] == 0)))
				return false;
			byte[] abNew = new byte[4];
			System.arraycopy(ab, 12, abNew, 0, 4);
		} else if (ab.length != 4)
			throw new Error("InetAddress that wasn't 4 or 16 bytes long?!");

		for (int i = 0; i < addresses.length; i++) {
			String[] bytes = addresses[i].split("\\.");
			boolean ok = true;
			for (int pos = 0; pos < 4; pos++) {
				// * allows anything
				if (bytes[pos].equals("*"))
					continue;

				int actual = getUnsigned(ab[pos]);

				// Plain number, not a range
				if (bytes[pos].indexOf('-') == -1) {
					if (actual != Integer.parseInt(bytes[pos])) {
						ok = false;
						break;
					}
				} else // Range
				{
					String[] range = bytes[pos].split("-");
					if (actual < Integer.parseInt(range[0])
							|| actual > Integer.parseInt(range[1])) {
						ok = false;
						break;
					}
				}
			}
			if (ok) {
				l.logDebug("IPCHECK", "isIPInList- true");
				return true;
			}
		}
		l.logDebug("IPCHECK", "isIPInList - false");
		return false;
	}

	/**
	 * @param b
	 *            Byte value
	 * @return The unsigned version of that byte
	 */
	public static int getUnsigned(byte b) {
		int i = b;
		if (i >= 0)
			return i;
		return 256 + i;
	}

}
