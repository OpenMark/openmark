package om.tnavigator.sessions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


import om.Log;
import om.tnavigator.NavigatorConfig;


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
}
