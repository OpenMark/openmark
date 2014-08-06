/* OpenMark online assessment system
 * Copyright (C) 2007 The Open University
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package om.tnavigator.sessions;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.Log;
import om.OmException;
import om.OmFormatException;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.NavigatorServlet;
import om.tnavigator.auth.Authentication;
import om.tnavigator.auth.UncheckedUserDetails;
import om.tnavigator.sessions.ClaimedUserDetails.Status;
import util.misc.GeneralUtils;
import util.misc.PeriodicThread;
import util.misc.StopException;
import util.misc.Strings;


/**
 * Tracks the user sessions, and is responsible for finding the right session for
 * the current request.
 */
public class SessionManager
{
	/** How long an unused session lurks around before expiring (8 hrs) */
	private final static int SESSIONEXPIRY = 8 * 60 * 60 * 1000;

	/** How often we check for expired sessions (15 mins) */
	private final static int SESSIONCHECKDELAY = 15 * 60 * 1000;

	/** Time that temp forbids last. */
	private final static long FORBID_PERIOD = 60 * 1000;

	/** Map of cookie value (String) -> UserSession */
	private Map<String, UserSession> sessionsByCookieValue = new HashMap<String, UserSession>();

	/** Map of OUCU-testID (String) -> UserSession */
	private Map<String, UserSession> sessionsByUsernameAndTestId = new HashMap<String, UserSession>();

	/** Map of OUCU-testID (String) -> Long (date that prohibition expires) */
	private Map<String, Long> tempForbid = new HashMap<String, Long>();

	/** Session expiry thread */
	private PeriodicThread sessionExpirer;

	/** Tracks when last error occurred while sending forbids to each other nav */
	long[] lastSessionKillerError;

	Object sessionKillerErrorSynch = new Object();

	/** Config file contents */
	protected NavigatorConfig nc;

	/** Log */
	protected Log l;

	/**
	 * Create a new session manager.
	 * @param nc navigator config
	 * @param l log.
	 */
	public SessionManager(NavigatorConfig nc, Log l)
	{
		this.nc = nc;
		this.l = l;
		lastSessionKillerError = new long[nc.getOtherNavigators().length];
		sessionExpirer = new PeriodicThread(SESSIONCHECKDELAY)
		{
			@Override
			protected void tick()
			{
				synchronized (sessionsByCookieValue)
				{
					// See if any sessions need expiring
					long now = System.currentTimeMillis();
					long expiryTime = now - SESSIONEXPIRY;

					for (UserSession us : sessionsByCookieValue.values())
					{
						if (us.getLastActionTime() < expiryTime)
						{
							killSession(us);
						}
					}

					for (Map.Entry<String, Long> entry : tempForbid.entrySet())
					{
						if (now > entry.getValue())
						{
							tempForbid.remove(entry.getKey());
						}
					}
				}
			}
		};
	}

	/**
	 * Dispose of this class.
	 */
	public void close()
	{
		sessionExpirer.close();
	}

	/**
	 * Kill sessions with a particular ID on other servers.
	 * @param usernameAndTestId the user/test combination to kill on other servers.
	 */
	public void killSessionsOnOtherNavigators(String usernameAndTestId)
	{
		new RemoteSessionKiller(this, usernameAndTestId, nc.getOtherNavigators());
	}

	/**
	 * Get the name of the per-test session cookie for a given test.
	 * @param testId the test id, that is the key bit of the deploy file name/URL.
	 * @return the cookie name.
	 */
	public String getTestCookieName(String testId)
	{
		return "tnavigator_session_" + testId;
	}

	/**
	 * @param sTestID the test the user is trying to access.
	 * @param sCookie the value of their cookie, if any.
	 * @param sOUCU the unchecked OUCU, if any.
	 * @param sFakeOUCU the fake OUCU from the cookie, if any.
	 */
	protected void debugLogState(String sTestID, String sCookie, String sOUCU, String sFakeOUCU) {
		StringBuffer debugOutput = new StringBuffer(1024);
		debugOutput.append("Session manager state:\n");
		debugOutput.append("sTestID = ").append(sTestID).append("\n");
		debugOutput.append("sCookie = ").append(sCookie).append("\n");
		debugOutput.append("claimedDetails.sOUCU = ").append(sOUCU).append("\n");
		debugOutput.append("claimedDetails.sFakeOUCU = ").append(sFakeOUCU).append("\n");
		debugOutput.append("sessionsByCookieValue:\n");
		for (Map.Entry<String, UserSession> e: sessionsByCookieValue.entrySet()) {
			debugOutput.append("    ").append(e.getKey()).append(" => ")
					.append(debugDisplayUserSession(e.getValue())).append("\n");
		}
		debugOutput.append("sessionsByUsernameAndTestId:\n");
		for (Map.Entry<String, UserSession> e: sessionsByUsernameAndTestId.entrySet()) {
			debugOutput.append("    ").append(e.getKey()).append(" => ")
					.append(debugDisplayUserSession(e.getValue())).append("\n");
		}
		l.logDebug("SessionManager", debugOutput.toString());
	}

	protected String debugDisplayUserSession(UserSession us) {
		StringBuffer sb = new StringBuffer(100);
		sb.append(us.sCookie).append(", ").append(us.sOUCU).append(", ").append(us.iAuthHash);
		if (us.tdDeployment != null) {
			sb.append(", ").append(us.tdDeployment.getDefinition());
		}
		return sb.toString();
	}

	/**
	 * @param authentication the authentication plugin.
	 * @param request the request being handled.
	 * @param response the response we will send.
	 * @param bPost if this was a POST request.
	 * @param sTestID the name of the test being requested.
	 * @param deployFile the path to the deploy file, which we know exists.
	 * @return information about whether the user is logged in.
	 * @throws OmException
	 * @throws StopException
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	public ClaimedUserDetails tryToFindUserSession(Authentication authentication,
			HttpServletRequest request, HttpServletResponse response,
			boolean bPost, String sTestID, File deployFile) throws OmException,
			StopException, IOException
	{
		UncheckedUserDetails uud = authentication.getUncheckedUserDetails(request);
		ClaimedUserDetails claimedDetails = new ClaimedUserDetails();

		// Get OUCU (null if no SAMS cookie), used to synch sessions for
		// single user
		String sOUCU = uud.getUsername();

		// See if they've got a fake OUCU (null if none)
		String sFakeOUCU = GeneralUtils.getCookie(request, NavigatorServlet.FAKEOUCUCOOKIENAME);

		// Auth hash
		int iAuthHash = (uud.getCookie() + "/" + sFakeOUCU).hashCode();

		// If they haven't got a cookie or it's unknown, assign them one and
		// redirect.
		synchronized (sessionsByCookieValue)
		{
			// See if they've got a cookie for this test
			String sCookie = GeneralUtils.getCookie(request, getTestCookieName(sTestID));

			if (false) {
				debugLogState(sTestID, sCookie, sOUCU, sFakeOUCU);
			}

			// Check whether they already have a session or not
			if (sCookie != null)
			{
				claimedDetails.us = sessionsByCookieValue.get(sCookie);
			}

			if (claimedDetails.us != null)
			{
				// Check cookies in case they changed.
				if (claimedDetails.us.iAuthHash != 0 && claimedDetails.us.iAuthHash != iAuthHash)
				{
					l.logDebug("SessionManager", "Discarding session because iAuthHash does not match. " +
							"Auth hash in session: " + claimedDetails.us.iAuthHash + ". " +
							"Unchecked cookie: " + uud.getCookie() + ". " +
							"Fake OUCU: " + sFakeOUCU + ". " +
							"Current Auth hash: " + iAuthHash + ".");
					// If credentials change, they need a new session.
					killSession(claimedDetails.us);
					claimedDetails.us = null;
				}
			}

			// New sessions!
			boolean newSession = claimedDetails.us == null;
			if (newSession)
			{
				// Check if we've already been redirected.
				String cookieSetParam = request.getParameter("setcookie");
				if (cookieSetParam != null)
				{
					try {
						long cookieSetTime = Long.parseLong(cookieSetParam);
						if (System.currentTimeMillis() < cookieSetTime + 5000L) {
							claimedDetails.status = Status.CANNOT_CREATE_COOKIE;
							return claimedDetails;
						}
					} catch (NumberFormatException e) {
						// Not a valid param. Same as if param not given.
					}
				}

				claimedDetails.us = new UserSession(getUnusedCookieValue());
				l.logDebug("SessionManager", "Created a new UserSession.");

				// Store the appropriate username in the session.
				if (sOUCU != null)
				{
					// Claimed to be logged in. verifyUserSession will check that.
					claimedDetails.us.sOUCU = sOUCU;
				}
				else if (sFakeOUCU != null)
				{
					// Not logged in, but they already have a fake OUCU.
					claimedDetails.us.sOUCU = sFakeOUCU;
				}
				else
				{
					// If they're not logged in, and we need to create a new fake OUCU
					// for them. In this case, verifyUserSession will create the cookie.
					claimedDetails.us.sOUCU = "_" + Strings.randomAlNumString(7);
					iAuthHash = (uud.getCookie() + "/" + claimedDetails.us.sOUCU).hashCode();
				}

				claimedDetails.us.usernameTestIdKey = claimedDetails.us.sOUCU + "-" + sTestID;

				if (isTemporarilyForbidden(claimedDetails.us.usernameTestIdKey))
				{
					// Kill session from main list & mark it to send error message later.
					l.logDebug("SessionManager", "New session was temporarily forbidden. Killing it.");

					killSession(claimedDetails.us);
					claimedDetails.status = Status.TEMP_FORBID;
					return claimedDetails;
				}
				else
				{
					// This session is good. So ensure there are not sessions on
					// the other servers in the cluster.
					killSessionsOnOtherNavigators(claimedDetails.us.usernameTestIdKey);
				}
			}

			if (newSession) {
				// Partially add the session now. We will also add it to
				// sessionsByUsernameAndTestId once we have checked the OUCU.
				sessionsByCookieValue.put(claimedDetails.us.sCookie, claimedDetails.us);
			}
		}

		if (bPost && claimedDetails.us.ud == null) {
			claimedDetails.status = Status.POST_NO_SESSION;
			return claimedDetails;
		}

		claimedDetails.us.claimExclusiveLock();
		if (!verifyUserSession(authentication,
				request, response, claimedDetails.us, sOUCU, sFakeOUCU, iAuthHash, sTestID, deployFile))
		{
			claimedDetails.status = Status.REDIRECTING;
			claimedDetails.us.releaseExclusiveLock();
		}

		return claimedDetails;
	}

	/**
	 * @param authentication the authentication plugin.
	 * @param request the request being handled.
	 * @param response the response we will send.
	 * @param us the user session to verify
	 * @param sOUCU the OUCU the user claimed, if any.
	 * @param sFakeOUCU the users fake OUCU if they have one
	 * @param iAuthHash the auth hash
	 * @param sTestID the name of the test being requested.
	 * @param deployFile the path to the deploy file, which we know exists.
	 * @return whether the login is valid.
	 * @throws IOException
	 * @throws StopException
	 * @throws OmException
	 * @throws OmFormatException
	 */
	protected boolean verifyUserSession(Authentication authentication,
			HttpServletRequest request, HttpServletResponse response,
			UserSession us, String sOUCU, String sFakeOUCU, int iAuthHash,
			String sTestID, File deployFile)
			throws IOException, StopException, OmException, OmFormatException
	{
		boolean createdCookie = false;

		// Set last action time (so session doesn't time out)
		us.touch();

		// If they now have a claimed real OUCU, but were previously using a fake
		// OUCU, kill their session cookie so they start a new session with thier OUCU.
		if (us.ud != null && sOUCU != null && !us.ud.isLoggedIn())
		{
			Cookie c = new Cookie(getTestCookieName(sTestID), "");
			c.setMaxAge(0);
			c.setPath("/");
			response.addCookie(c);
			response.sendRedirect(request.getRequestURI());
			return false;
		}

		// If they have already been authenticated previously in this session,
		// we don't need to do it again.
		if (us.ud != null)
		{
			return true;
		}

		us.loadTestDeployment(deployFile);

		us.ud = authentication.getUserDetails(request, response,
				!us.getTestDeployment().isWorldAccess());

		if (us.ud == null)
		{
			// They've been redirected to SAMS. Chuck their session
			// as soon as expirer next runs, they won't be needing
			// it as we didn't give them a cookie yet.
			us.markForDiscard();
			return false;
		}

		if (sOUCU != null && !us.ud.isLoggedIn()) {
			// If they claimed a real OUCU, and that is not right, and they have
			// not been redirected, this is a dodgy request, so throw an Exception.
			throw new OmException("There was a problem with your login. Please close your browser then try again.");
		}

		// We only give them a cookie after passing this stage. If
		// they were redirected to SAMS, they don't get a cookie
		// until the next request.
		if (!us.cookieCreated)
		{
			// Now we are sure we are using this session, we add it to the list properly.
			addSession(us);

			Cookie c = new Cookie(getTestCookieName(sTestID), us.sCookie);
			c.setPath("/");
			response.addCookie(c);
			us.cookieCreated = true;
			createdCookie = true;
		}

		if (sFakeOUCU == null && !us.ud.isLoggedIn())
		{
			// They also need a cookie to store their fake OUCU.
			Cookie c = new Cookie(NavigatorServlet.FAKEOUCUCOOKIENAME, us.sOUCU);
			c.setPath("/");
			// Expiry is 4 years
			c.setMaxAge((3 * 365 + 366) * 24 * 60 * 60);
			response.addCookie(c);
			createdCookie = true;
		}

		// Remember the auth hash so that we will know if they change cookie now.
		us.iAuthHash = iAuthHash;

		if (createdCookie) {
			response.sendRedirect(request.getRequestURI() + "?setcookie=" + System.currentTimeMillis());
			return false;
		}

		return true;
	}

	/**
	 * Get an unused session cookie value.
	 * @return
	 */
	private String getUnusedCookieValue()
	{
		String cookieValue;
		do
		{
			// Make 7-letter random cookie value.
			cookieValue = Strings.randomAlNumString(7);
		} while (sessionsByCookieValue.containsKey(cookieValue));
		// And what are the chances of that?
		return cookieValue;
	}

	/**
	 * @param oucuAndTestId
	 * @return is this combination of OUCU and test id currently forbidden?
	 */
	boolean isTemporarilyForbidden(String oucuAndTestId) {
		// Check the temp-forbid list.
		Long forbiddenUntil = tempForbid.get(oucuAndTestId);
		boolean forbidden = forbiddenUntil != null && forbiddenUntil.longValue() > System.currentTimeMillis();
		// If it was a timed-out forbid, get rid of it.
		if (forbiddenUntil != null && !forbidden)
		{
			tempForbid.remove(oucuAndTestId);
		}
		return forbidden;
	}

	public void blockUserTemporarily(String blockedUsername)
	{
		synchronized (sessionsByCookieValue)
		{
			// Ditch existing session.
			UserSession us = sessionsByUsernameAndTestId.remove(blockedUsername);
			if (us != null)
			{
				killSession(us);
			}

			// Forbid that user for 1 minute. This is intended to prevent the
			// possibility of timing issues allowing a user to get logged on to
			// both servers at once; should that happen, chances are they'll
			// instead be *dumped* from both servers at once (for 60 seconds).
			tempForbid.put(blockedUsername, System.currentTimeMillis() + FORBID_PERIOD);
		}
	}

	/**
	 * Add a new session.
	 * @param newSession the session to add.
	 */
	void addSession(UserSession newSession)
	{
		synchronized (sessionsByCookieValue)
		{
			sessionsByCookieValue.put(newSession.sCookie, newSession);
			UserSession oldSession = sessionsByUsernameAndTestId.put(newSession.usernameTestIdKey, newSession);
			// If there was one already there, get rid of it.
			if (oldSession != null)
			{
				sessionsByCookieValue.remove(oldSession.sCookie);
			}
		}
	}

	/**
	 * Remove as session from the list of active sessions.
	 * @param us the session to kill.
	 */
	public void killSession(UserSession us)
	{
		synchronized (sessionsByCookieValue)
		{
			sessionsByCookieValue.remove(us.sCookie);
			sessionsByUsernameAndTestId.remove(us.usernameTestIdKey);
		}
	}

	/**
	 * Add useful entries to performanceInfo.
	 * @param performanceInfo Map to which to add information.
	 */
	public void obtainPerformanceInfo(Map<String, Object> performanceInfo)
	{
		synchronized (sessionsByCookieValue)
		{
			performanceInfo.put("SESSIONS", sessionsByCookieValue.size() + "");
			performanceInfo.put("TEMPFORBIDS", tempForbid.size() + "");
		}
	}
}
