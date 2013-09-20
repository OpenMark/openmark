package om.tnavigator.sessions;

import java.util.Iterator;

import util.misc.PeriodicThread;

/** Thread that gets rid of unused sessions */
class SessionExpirer extends PeriodicThread {

	/** How long an unused session lurks around before expiring (8 hrs) */
	private final static int SESSIONEXPIRY = 8 * 60 * 60 * 1000;

	/** How often we check for expired sessions (15 mins) */
	private final static int SESSIONCHECKDELAY = 15 * 60 * 1000;

	/**
	 * 
	 */
	private final SessionManager sessionManager;

	SessionExpirer(SessionManager sessionManager) {
		super(SESSIONCHECKDELAY);
		this.sessionManager = sessionManager;
	}

	@Override
	protected void tick() {
		synchronized (this.sessionManager.sessions) {
			// See if any sessions need expiring
			long lNow = System.currentTimeMillis();
			long lExpiry = lNow - SESSIONEXPIRY;

			for (Iterator<UserSession> i = this.sessionManager.sessions.values().iterator(); i
					.hasNext();) {
				UserSession us = i.next();
				if (us.getLastActionTime() < lExpiry) {
					i.remove();
					this.sessionManager.usernames.remove(us.sCheckedOUCUKey);
				}
			}

			for (Iterator<Long> i = this.sessionManager.tempForbid.values().iterator(); i
					.hasNext();) {
				if (lNow > i.next()) {
					i.remove();
				}
			}
		}
	}
}