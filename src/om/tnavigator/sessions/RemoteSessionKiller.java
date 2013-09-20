package om.tnavigator.sessions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import util.misc.HTTPS;
import util.misc.IO;

/** Thread used to 'kill' a session on other servers by sending forbid calls */
class RemoteSessionKiller extends Thread {
	/**
	 * 
	 */
	private final SessionManager sessionManager;
	private String sOUCUTest;
	String[] asURL;

	RemoteSessionKiller(SessionManager sessionManager, String sOUCUTest, String[] asURL) {
		this.sessionManager = sessionManager;
		this.sOUCUTest = sOUCUTest;
		start();
	}

	@Override
	public void run() {
		for (int i = 0; i < asURL.length; i++) {
			try {
				URL u = new URL(asURL[i] + "!forbid/" + sOUCUTest);
				HttpURLConnection huc = (HttpURLConnection) u
						.openConnection();
				HTTPS.allowDifferentServerNames(huc);
				HTTPS.considerCertificatesValid(huc);

				huc.connect();
				if (huc.getResponseCode() != HttpServletResponse.SC_OK)
					throw new IOException("Error with navigator "
							+ huc.getResponseCode());
				IO.eat(huc.getInputStream());
				if (this.sessionManager.lastSessionKillerError[i] != 0) {
					synchronized (this.sessionManager.sessionKillerErrorSynch) {
						this.sessionManager.lastSessionKillerError[i] = 0;
					}
					// Because we only display errors once per hour, better
					// display the
					// 'OK' state too if it was marked as error before
					this.sessionManager.l.logNormal("Forbids", asURL[i]
							+ ": Forbid call OK now");
				}
			} catch (IOException ioe) {
				// Display an error once per hour if it's still erroring
				long lNow = System.currentTimeMillis();
				synchronized (this.sessionManager.sessionKillerErrorSynch) {
					if (this.sessionManager.lastSessionKillerError[i] == 0
							|| (lNow - this.sessionManager.lastSessionKillerError[i]) > 60 * 60 * 1000) {
						this.sessionManager.l.logWarning("Forbids", asURL[i]
								+ ": Forbid call failed", ioe);
						this.sessionManager.lastSessionKillerError[i] = lNow;
					}
				}
			}
		}
	}
}