package om.tnavigator.sessions;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
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
	private Map<String, UserSession> sessions = new HashMap<String, UserSession>();

	/** Map of OUCU-testID (String) -> UserSession */
	private Map<String, UserSession> usernames = new HashMap<String, UserSession>();

	/** Map of OUCU-testID (String) -> Long (date that prohibition expires) */
	private Map<String, Long> tempForbid = new HashMap<String, Long>();

	/** Session expiry thread */
	private PeriodicThread sessionExpirer;

	/** Tracks when last error occurred while sending forbids to each other nav */
	long[] lastSessionKillerError;

	Object sessionKillerErrorSynch = new Object();

	/**
	 * List of NewSession objects that are stored to check we don't start a new
	 * session twice in a row to same address (= cookies off)
	 */
	public LinkedList<NewSession> cookiesOffCheck = new LinkedList<NewSession>();

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
				synchronized (sessions)
				{
					// See if any sessions need expiring
					long now = System.currentTimeMillis();
					long expiryTime = now - SESSIONEXPIRY;

					for (UserSession us : sessions.values())
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
	public String getTestCookieName(String testId)
	{
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
		synchronized (sessions)
		{
			// Remove entries from cookies-off list after 1 second
			while (!cookiesOffCheck.isEmpty()
					&& requestStartTime - (cookiesOffCheck.getFirst()).lTime > 1000)
			{
				cookiesOffCheck.removeFirst();
			}

			// See if they've got a cookie for this test
			String sCookie = GeneralUtils.getCookie(request, getTestCookieName(sTestID));

			// Check whether they already have a session or not
			if (sCookie != null)
			{
				claimedDetails.us = sessions.get(sCookie);
			}

			if (claimedDetails.us != null)
			{
				// Check cookies in case they changed
				if (claimedDetails.us.iAuthHash != 0 && claimedDetails.us.iAuthHash != claimedDetails.iAuthHash)
				{
					// If credentials change, they need a new session
					sessions.remove(claimedDetails.us.sCookie);
					claimedDetails.us = null;
				}
			}

			// New sessions!
			if (claimedDetails.us == null)
			{
				String sAddr = request.getRemoteAddr();

				// Check if we've already been redirected
				for (NewSession ns : cookiesOffCheck)
				{
					if (ns.sAddr.equals(sAddr))
					{
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

				do
				{
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
				if (claimedDetails.sOUCU != null)
				{
					if (!authentication.getUserDetails(request, response, false).isLoggedIn())
					{
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
			if (claimedDetails.us.sCheckedOUCUKey == null && claimedDetails.sOUCU != null)
			{
				claimedDetails.us.sCheckedOUCUKey = claimedDetails.sOUCU + "-" + sTestID;

				// Check the temp-forbid list
				Long lTimeout = tempForbid.get(claimedDetails.us.sCheckedOUCUKey);
				if (lTimeout != null && lTimeout.longValue() > System.currentTimeMillis())
				{
					// Kill session from main list & mark it to send error
					// message later
					sessions.remove(claimedDetails.us.sCookie);
					bTempForbid = true;
				}
				else
				{
					// If it was a timed-out forbid, get rid of it
					if (lTimeout != null)
						tempForbid.remove(claimedDetails.us.sCheckedOUCUKey);

					// Put this in the OUCU->session map
					UserSession usOld = usernames.put(claimedDetails.us.sCheckedOUCUKey,
							claimedDetails.us);
					// If there was one already there, get rid of it
					if (usOld != null)
					{
						sessions.remove(usOld.sCookie);
					}
					sKillOtherSessions = claimedDetails.us.sCheckedOUCUKey;
				}
			}
		}
		// If they started a session, tell other servers to kill that
		// session (in thread)
		if (sKillOtherSessions != null)
		{
			killOtherSessions(sKillOtherSessions);
		}

		// Error if forbidden
		if (bTempForbid)
		{
			claimedDetails.status = Status.TEMP_FORBID;
			return claimedDetails;
		}

		return claimedDetails;
	}

	/**
	 * @param authentication the authentication plugin.
	 * @param request the reuqest being handled.
	 * @param response the response we will send.
	 * @param claimedDetails the result of tryToFindUserSession.
	 * @param sTestID the name of the test being requested.
	 * @param deployFile the path to the deploy file, which we know exists.
	 * @return whether the login is valid.
	 * @throws IOException
	 * @throws StopException
	 * @throws OmException
	 * @throws OmFormatException
	 */
	public boolean verifyUserSession(Authentication authentication,
			HttpServletRequest request, HttpServletResponse response,
			ClaimedUserDetails claimedDetails, String sTestID, File deployFile) throws IOException,
			StopException, OmException, OmFormatException
	{
		UserSession us = claimedDetails.us;

		// Set last action time (so session doesn't time out)
		us.touch();

		// If they have an OUCU but also a temp-login then we need to
		// chuck away their session...
		if (us.ud != null && claimedDetails.sOUCU != null && !us.ud.isLoggedIn())
		{
			Cookie c = new Cookie(getTestCookieName(sTestID), "");
			c.setMaxAge(0);
			c.setPath("/");
			response.addCookie(c);
			response.sendRedirect(request.getRequestURI());
			return false;
		}

		// Get auth if needed
		if (us.ud != null)
		{
			return true;
		}

		us.loadTestDeployment(deployFile);

		us.ud = authentication.getUserDetails(request, response, !us
				.getTestDeployment().isWorldAccess());
		if (us.ud == null)
		{
			// They've been redirected to SAMS. Chuck their session
			// as soon as expirer next runs, they won't be needing
			// it as we didn't give them a cookie yet.
			us.markForDiscard();
			return false;
		}

		// We only give them a cookie after passing this stage. If
		// they were redirected to SAMS, they don't get a cookie
		// until the next request.

		if (!us.cookieCreated)
		{
			Cookie c = new Cookie(getTestCookieName(sTestID), us.sCookie);
			c.setPath("/");
			response.addCookie(c);
			us.cookieCreated = true;
		}

		if (us.ud.isLoggedIn())
		{
			us.sOUCU = us.ud.getUsername();
		}
		else if (claimedDetails.sFakeOUCU != null)
		{
			// Not logged in, but they already have a fake OUCU.
			us.sOUCU = claimedDetails.sFakeOUCU;
		}
		else
		{
			// If they're not logged in, and we need to create a new fake OUCU
			// for them, and then redirect.
			// Make 8-letter random OUCU. We don't bother
			// storing a list, but there are about 3,000 billion
			// possibilities so it should be OK.
			us.sOUCU = "_" + Strings.randomAlNumString(7);

			// Set it in cookie for future sessions
			Cookie c = new Cookie(NavigatorServlet.FAKEOUCUCOOKIENAME, us.sOUCU);
			c.setPath("/");
			// Expiry is 4 years
			c.setMaxAge((3 * 365 + 366) * 24 * 60 * 60);
			response.addCookie(c);
			response.sendRedirect(request.getRequestURI());
			return false;
		}

		// Remember auth hash so that we will know if they change cookie now.
		us.iAuthHash = claimedDetails.iAuthHash;

		return true;
	}

	public void blockUserTemporarily(String blockedUsername)
	{
		synchronized (sessions)
		{
			// Ditch existing session.
			UserSession us = usernames.remove(blockedUsername);
			if (us != null)
			{
				killSession(us);
			}

			// Forbid that user for 1 minute .This is intended to prevent the
			// possibility of timing issues allowing a user to get logged on to
			// both servers at once; should that happen, chances are they'll
			// instead be *dumped* from both servers at once (for 60 seconds).
			tempForbid.put(blockedUsername, System.currentTimeMillis() + FORBID_PERIOD);
		}
	}

	/**
	 * Remove as session from the list of active sessions.
	 * @param us the session to kill.
	 */
	public void killSession(UserSession us)
	{
		synchronized (sessions)
		{
			sessions.remove(us.sCookie);
			usernames.remove(us.sCheckedOUCUKey);
		}
	}

	/**
	 * Add useful entries to performanceInfo.
	 * @param performanceInfo Map to which to add information.
	 */
	public void obtainPerformanceInfo(Map<String, Object> performanceInfo)
	{
		synchronized (sessions)
		{
			performanceInfo.put("SESSIONS", sessions.size() + "");
			performanceInfo.put("TEMPFORBIDS", tempForbid.size() + "");
		}
	}
}
