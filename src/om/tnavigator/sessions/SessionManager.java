package om.tnavigator.sessions;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.Log;
import om.OmException;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.NavigatorServlet;
import om.tnavigator.auth.Authentication;
import om.tnavigator.auth.UncheckedUserDetails;
import om.tnavigator.sessions.ClaimedUserDetails.Status;
import util.misc.GeneralUtils;
import util.misc.StopException;
import util.misc.Strings;


public class SessionManager
{
	/** Map of cookie value (String) -> UserSession */
	public Map<String, UserSession> sessions = new HashMap<String, UserSession>();

	/** Map of OUCU-testID (String) -> UserSession */
	public Map<String, UserSession> usernames = new HashMap<String, UserSession>();

	/** Map of OUCU-testID (String) -> Long (date that prohibition expires) */
	public Map<String, Long> tempForbid = new HashMap<String, Long>();

	/** Session expiry thread */
	public SessionExpirer sessionExpirer;

	/** Tracks when last error occurred while sending forbids to each other nav */
	public long[] lastSessionKillerError;

	public Object sessionKillerErrorSynch = new Object();

	/**
	 * List of NewSession objects that are stored to check we don't start a new
	 * session twice in a row to same address (= cookies off)
	 */
	public LinkedList<NewSession> cookiesOffCheck = new LinkedList<NewSession>();

	/** Config file contents */
	protected NavigatorConfig nc;

	/** Log */
	protected Log l;

	public SessionManager(NavigatorConfig nc, Log l)
	{
		this.nc = nc;
		this.l = l;
		lastSessionKillerError = new long[nc.getOtherNavigators().length];
		sessionExpirer = new SessionExpirer(this);
	}

	/**
	 * Dispose of this class.
	 */
	public void close() {
		// Kill expiry thread
		sessionExpirer.close();
	}

	/**
	 * Kill sessions with a particular ID on other servers.
	 * @param sKillOtherSessions the session id to kill.
	 */
	public void killOtherSessions(String sKillOtherSessions)
	{
		new RemoteSessionKiller(this, sKillOtherSessions, nc.getOtherNavigators());
	}

	/**
	 * Get the name of the per-test session cookie for a given test.
	 * @param testId the test id, that is the key bit of the deploy file name/URL.
	 * @return the cookie name.
	 */
	public String getTestCookieName(String testId) {
		return "tnavigator_session_" + testId;
	}

	/**
	 * @param authentication the authentication plugin.
	 * @param request the reuqest being handled.
	 * @param response the response we will send.
	 * @param requestStartTime the time when we started handling this request.
	 * @param sTestID the name of the test being requested.
	 * @return information about whether the user is logged in.
	 * @throws OmException
	 * @throws StopException
	 * @throws IOException
	 */
	public ClaimedUserDetails tryToFindUserSession(Authentication authentication,
			HttpServletRequest request, HttpServletResponse response,
			long requestStartTime, String sTestID) throws OmException,
			StopException, IOException
	{
		UncheckedUserDetails uud = authentication.getUncheckedUserDetails(request);
		ClaimedUserDetails claimedDetails = new ClaimedUserDetails();

		// Get OUCU (null if no SAMS cookie), used to synch sessions for
		// single user
		claimedDetails.sOUCU = uud.getUsername();

		// See if they've got a fake OUCU (null if none)
		claimedDetails.sFakeOUCU = GeneralUtils.getCookie(request, NavigatorServlet.FAKEOUCUCOOKIENAME);

		// Auth hash
		claimedDetails.iAuthHash = (uud.getCookie() + "/" + claimedDetails.sFakeOUCU).hashCode();

		// If they haven't got a cookie or it's unknown, assign them one and
		// redirect.
		boolean bTempForbid = false;
		String sKillOtherSessions = null;
		synchronized (sessions) {
			// Remove entries from cookies-off list after 1 second
			while (!cookiesOffCheck.isEmpty()
					&& requestStartTime - (cookiesOffCheck.getFirst()).lTime > 1000) {
				cookiesOffCheck.removeFirst();
			}

			// See if they've got a cookie for this test
			String sCookie = GeneralUtils.getCookie(request, getTestCookieName(sTestID));

			// Check whether they already have a session or not
			if (sCookie != null) {
				claimedDetails.us = sessions.get(sCookie);
			}

			if (claimedDetails.us != null) {
				// Check cookies in case they changed
				if (claimedDetails.us.iAuthHash != 0 && claimedDetails.us.iAuthHash != claimedDetails.iAuthHash) {
					// If credentials change, they need a new session
					sessions.remove(claimedDetails.us.sCookie);
					claimedDetails.us = null;
				}
			}

			// New sessions!
			if (claimedDetails.us == null) {
				String sAddr = request.getRemoteAddr();

				// Check if we've already been redirected
				for (NewSession ns : cookiesOffCheck) {
					if (ns.sAddr.equals(sAddr)) {
						claimedDetails.status = Status.CANNOT_CREATE_COOKIE;
						return claimedDetails;
					}
				}

				// Record this redirect so that we notice if it happens
				// twice
				NewSession ns = new NewSession();
				ns.lTime = requestStartTime;
				ns.sAddr = sAddr;
				cookiesOffCheck.addLast(ns);

				do {
					// Make 7-letter random cookie
					sCookie = Strings.randomAlNumString(7);
				} while (sessions.containsKey(sCookie));
				// And what are the chances of that?

				claimedDetails.us = new UserSession(sCookie);
				l.logDebug("Created new UserSession.");
				sessions.put(claimedDetails.us.sCookie, claimedDetails.us);
				// We do the actual redirect later on outside this synch.

				// At same time as creating new session, if they're logged
				// in supposedly, check it's for real. If their cookie doesn't
				// authenticated, this will cause the cookie to be removed
				// and avoid multiple redirects.
				if (claimedDetails.sOUCU != null) {
					if (!authentication.getUserDetails(request, response, false).isLoggedIn()) {
						// And we need to set this to zero to reflect that
						// we just wiped
						// their cookie.
						claimedDetails.iAuthHash = 0;
					}
				}
			}

			// If this is the first time we've had an OUCU for this session,
			// check
			// it to make sure we don't need to ditch any other sessions
			if (claimedDetails.us.sCheckedOUCUKey == null && claimedDetails.sOUCU != null) {
				claimedDetails.us.sCheckedOUCUKey = claimedDetails.sOUCU + "-" + sTestID;

				// Check the temp-forbid list
				Long lTimeout = tempForbid.get(claimedDetails.us.sCheckedOUCUKey);
				if (lTimeout != null
						&& lTimeout.longValue() > System
								.currentTimeMillis()) {
					// Kill session from main list & mark it to send error
					// message later
					sessions.remove(claimedDetails.us.sCookie);
					bTempForbid = true;
				} else {
					// If it was a timed-out forbid, get rid of it
					if (lTimeout != null)
						tempForbid.remove(claimedDetails.us.sCheckedOUCUKey);

					// Put this in the OUCU->session map
					UserSession usOld = usernames.put(claimedDetails.us.sCheckedOUCUKey,
							claimedDetails.us);
					// If there was one already there, get rid of it
					if (usOld != null) {
						sessions.remove(usOld.sCookie);
					}
					sKillOtherSessions = claimedDetails.us.sCheckedOUCUKey;
				}
			}
		}
		// If they started a session, tell other servers to kill that
		// session (in thread)
		if (sKillOtherSessions != null) {
			killOtherSessions(sKillOtherSessions);
		}

		// Error if forbidden
		if (bTempForbid) {
			claimedDetails.status = Status.TEMP_FORBID;
			return claimedDetails;
		}

		return claimedDetails;
	}
}
