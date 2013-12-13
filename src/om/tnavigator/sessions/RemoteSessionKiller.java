package om.tnavigator.sessions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import util.misc.HTTPS;
import util.misc.IO;

/** Thread used to 'kill' a session on other servers by sending forbid calls */
class RemoteSessionKiller extends Thread
{
	/** We only log session killer errors with this frequency. */
	private final static int ERROR_LOGGING_PERIOD = 60 * 60 * 1000;

	/** The session manager we were launched by. */
	private final SessionManager sessionManager;
	private String username;
	String[] otherTestNavigatorUrls;

	/**
	 * Block the given user from getting a session on all the other servers.
	 * @param sessionManager the session manager that invoked us.
	 * @param username the user to block.
	 * @param otherTestNavigatorUrls the other test navigators to notify.
	 */
	RemoteSessionKiller(SessionManager sessionManager, String username, String[] otherTestNavigatorUrls)
	{
		this.sessionManager = sessionManager;
		this.username = username;
		this.otherTestNavigatorUrls = otherTestNavigatorUrls;
		start();
	}

	/**
	 * Send a forbid to a particular other server.
	 * @param otherTestNavigatorUrl the base URL of the other test navigator.
	 * @throws IOException
	 */
	private void forbidUserOnServer(String otherTestNavigatorUrl) throws IOException
	{
		URL forbidURL = new URL(otherTestNavigatorUrl + "!forbid/" + username);
		HttpURLConnection huc = (HttpURLConnection) forbidURL.openConnection();
		HTTPS.allowDifferentServerNames(huc);
		HTTPS.considerCertificatesValid(huc);

		huc.connect();
		if (huc.getResponseCode() != HttpServletResponse.SC_OK)
		{
			throw new IOException("Error with navigator "
					+ huc.getResponseCode());
		}
		IO.eat(huc.getInputStream());
	}

	@Override
	public void run()
	{
		for (int i = 0; i < otherTestNavigatorUrls.length; i++)
		{
			try
			{
				forbidUserOnServer(otherTestNavigatorUrls[i]);

				// It there was a previous error, log that it is OK now.
				if (sessionManager.lastSessionKillerError[i] != 0)
				{
					synchronized (sessionManager.sessionKillerErrorSynch)
					{
						sessionManager.lastSessionKillerError[i] = 0;
					}
					sessionManager.l.logNormal("Forbids",
							otherTestNavigatorUrls[i] + ": Forbid call OK now");
				}
			}
			catch (IOException ioe)
			{
				// Display an error once per hour if it's still erroring.
				long lNow = System.currentTimeMillis();
				synchronized (sessionManager.sessionKillerErrorSynch)
				{
					long lastError = sessionManager.lastSessionKillerError[i];
					if (lastError == 0 || lNow - lastError > ERROR_LOGGING_PERIOD)
					{
						sessionManager.l.logWarning("Forbids",
								otherTestNavigatorUrls[i] + ": Forbid call failed", ioe);
						sessionManager.lastSessionKillerError[i] = lNow;
					}
				}
			}
		}
	}
}
