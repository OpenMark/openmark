package util.misc;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class RequestHelpers {

	public static final String ACCESSCOOKIENAME = "tnavigator_access";

	public final static Pattern COLOURSPATTERN = Pattern
		.compile(".*\\[colours=(.*?)\\].*");

	private static Pattern SERVLETPATH = Pattern.compile("/([^/]*).*");

	/**
	 * @param request HTTP request
	 * @return Servlet URL ending in / e.g. http://wherever/om-tn/
	 */
	public static String getServletURL(HttpServletRequest request) {
		// Works for http and https URLs on any server as long as the test
		// navigator root is http://somewhere/something/ (i.e. it must be
		// one level down).
		return request.getRequestURL().toString().replaceAll(
				"^(https?://[^/]+/[^/]+/).*$", "$1");
	}

	public static boolean inPlainMode(HttpServletRequest request) {
		String sAccess = RequestHelpers.getAccessibilityCookie(request);
		return (sAccess.indexOf("[plain]") != -1);
	}

	/** @return A stupid bit to add to the end of the test URL */
	public static String endOfURL(HttpServletRequest request) {
		return endOfURL(inPlainMode(request));
	}

	/** @return A stupid bit to add to the end of the test URL */
	private static String endOfURL(boolean bPlain) {
		// The form URL gets a random number after it in plain mode because
		// otherwise
		// JAWS doesn't reread the page properly, thinking it's the same one.
		// GAH!
		if (bPlain)
			return "_" + Math.random();
		else
			return "";
	}

	/**
	 * Displays servlet URLs in a form suitable for including in error messages
	 * etc. (The idea is not to show the full URL because then maybe users will
	 * click it.)
	 * 
	 * @param u
	 *            Actual URL
	 * @return String displaying the servlet context @ hostname
	 */
	public static String displayServletURL(URL u) {
		Matcher m = SERVLETPATH.matcher(u.getPath());
		m.matches();
		return m.group(1) + " @ "
				+ u.getHost().replaceAll("\\.open\\.ac\\.uk", "");
	}

	/**
	 * @param request
	 *            the reqiest we are responding to.
	 * @return a bit that goes inthe navigator(here).css name depending on
	 *         access cookie.
	 */
	public static String getAccessCSSAppend(HttpServletRequest request) {
		// Fix up accessibility details from cookie
		String sAccessibility = getAccessibilityCookie(request);
		// If in plain mode, no CSS, why is this being called anyway?
		boolean bPlain = sAccessibility.indexOf("[plain]") != -1;
		if (bPlain)
			return "";

		// Ignore zoom as it doesn't affect file (yet)
		// File has colour code in
		Matcher m = COLOURSPATTERN.matcher(sAccessibility);
		if (m.matches()) {
			return "." + m.group(1);
		}

		return "";
	}

	/**
	 * @param request
	 *            HTTP request
	 * @return The accessibility cookie, or an empty string if it isn't set
	 */
	public static String getAccessibilityCookie(HttpServletRequest request) {
		String sCookie = "";
		Cookie[] ac = request.getCookies();
		if (ac == null)
			ac = new Cookie[0];
		for (int i = 0; i < ac.length; i++) {
			if (ac[i].getName().equals(ACCESSCOOKIENAME)) {
				sCookie = ac[i].getValue();
			}
		}
		return sCookie;
	}
}
