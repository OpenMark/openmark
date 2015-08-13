package om.tnavigator.overduenotifier;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import om.Log;
import om.OmException;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.auth.Authentication;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.DatabaseAccess.Transaction;
import om.tnavigator.db.OmQueries;
import om.tnavigator.sessions.TemplateLoader;
import om.tnavigator.teststructure.TestDeployment;

import org.apache.commons.lang.StringUtils;

import util.misc.PeriodicThread;
import util.misc.UtilityException;

/**
 * This background tasks runs once per hour (OVERDUE_CHECK_DELAY) to send
 * overdue attempt notifications.
 *
 * Notifications are sent if:
 *  + The test has a close date.
 *  + The &lt;emailstudents> tag is set up in the deploy file.
 *  + A student has started an attempt and answered at least &lt;numberofquestions> questions
 *  + The attempt has not been finished, and the email has not already been sent for this test.
 */
public class OverdueTestNotifier extends PeriodicThread
{
	/** Frequency with which we check for overdue attempts (ms). */
	private final static int OVERDUE_CHECK_DELAY = 3600 * 1000;

	/**
	 * Regular expression for finding deploy files.
	 * (With capturing brackets for the part of the filename that goes in the URL.)
	 */
	private final static Pattern filenamePattern = Pattern.compile("^(.*)\\.deploy\\.xml$");

	private NavigatorConfig nc;

	private Log log;

	DatabaseAccess da;

	private OmQueries oq;

	private Authentication authentication;

	private TemplateLoader templateLoader;

	/**
	 * Constuctor.
	 * @param nc reference to the NavigatorConfig to use.
	 * @param log reference to the Log to use.
	 * @param da reference to the DatabaseAccess to use.
	 * @param oq reference to the OmQueries to use.
	 * @param auth reference to the Authentication to use.
	 * @param templateLoader reference to the TemplateLoader to use.
	 */
	public OverdueTestNotifier(NavigatorConfig nc, Log log, DatabaseAccess da, OmQueries oq, Authentication auth, TemplateLoader templateLoader)
	{
		super(OVERDUE_CHECK_DELAY);
		this.nc = nc;
		this.log = log;
		this.da = da;
		this.oq = oq;
		this.authentication = auth;
		this.templateLoader = templateLoader;
	}

	@Override
	protected void tick()
	{
		File maintenanceFile = new File(nc.getMaintenanceModeFilePath());
		if (maintenanceFile.exists()) {
			// Don't run notifier, but don't warn.
			return;
		}

		File stopFile = new File(nc.getStopNotifierFilePath());
		if (stopFile.exists()) {
			log.logWarning("OverdueTestNotifier", "Notifier not running becuase stop file is present.");
			return;
		}

		try {
			log.logNormal("OverdueTestNotifier", "Notifier run starting.");
			sendAllNotification();
			log.logNormal("OverdueTestNotifier", "Notifier run completed normally.");
		} catch (Exception x) {
			log.logError("OverdueTestNotifier", "Error sending overdue test notifications. Run abandoned", x);
		}
	}

	/**
	 * Do the work.
	 */
	public void sendAllNotification()
	{
		File testBank = new File(nc.getTestbankPath());
		File[] deployFiles = testBank.listFiles(new FilenameFilter()
				{
					@Override
					public boolean accept(File f, String name) {
						return filenamePattern.matcher(name).matches();
					}
				});

		for (int i = 0; i < deployFiles.length; i++)
		{
			File deployFile = deployFiles[i];
			String deploy = testNameFromDeployFile(deployFile);
			try
			{
				sendNotificationsForTest(deployFile, deploy);
			}
			catch (Exception x) {
				log.logError("OverdueTestNotifier", "Error sending overdue notifications for test " + deploy, x);
			}
		}
	}

	/**
	 * Get the test name (as it goes in the URL) from the name of a deploy file.
	 *
	 * @param fileName
	 * @return
	 */
	private String testNameFromDeployFile(File deployFile)
	{
		Matcher m = filenamePattern.matcher(deployFile.getName());
		m.matches();
		return m.group(1);
	}

	/**
	 * Send all the notifications for one deploy file.
	 *
	 * @param su
	 * @param files
	 * @throws OmException
	 * @throws SQLException
	 */
	private void sendNotificationsForTest(File deployFile, String deploy)
			throws OmException, SQLException
	{
		TestDeployment td = new TestDeployment(deployFile);

		if (!td.isUsingEmailStudents() || !td.isAfterForbid())
		{
			// Either this test is not using notifications, or we are not yet at
			// the final close date (forbid date).
			return;
		}

		if (td.isAfterForbidExtension())
		{
			// We are past the end of the overdue period. No point sending notifications.
			return;
		}

		// Sleep a second before each test we need to check, to avoid overloading
		// the server.
		try
		{
			Thread.sleep(1000);
		}
		catch(InterruptedException e)
		{
			// Ignore.
		}

		log.logDebug("OverdueTestNotifier", "Processing notifications for test " + deploy + ".");
		List<ReminderDetails> overdueAttempts = getOverdueTestAttempts(td, deploy);
		for (ReminderDetails overdueAttempt : overdueAttempts)
		{
			try
			{
				String msg = messageText(td, overdueAttempt);
				String response = authentication.sendMail(
						overdueAttempt.getOucu(), overdueAttempt.getPi(), msg,
						Authentication.SUBMISSION_REMINDER);
				log.logNormal("OverdueTestNotifier", "Notified " + overdueAttempt.getOucu() +
						" about overdue attempt at test " + deploy + "." +
						" Message despatch ID " + response + ".");

				Transaction dat = da.newTransaction();
				try
				{
					oq.updateTestOverdueNotificationSent(dat, overdueAttempt.getTi());
				}
				finally
				{
					dat.finish();
				}
			}
			catch (Exception x)
			{
				log.logError("OverdueTestNotifier", "Error notifying " + overdueAttempt.getOucu() +
						" about overdue attempt at test " + deploy + ".", x);
			}
		}
	}

	/**
	 * Get all the reminders that should be sent for a particular test.
	 * @param td
	 * @param deploy
	 * @return
	 * @throws SQLException
	 */
	public List<ReminderDetails> getOverdueTestAttempts(TestDeployment td, String deploy)
			throws SQLException
	{
		Transaction dat = da.newTransaction();
		try
		{
			ResultSet rs = oq.queryOverdueAttemptsForTest(dat, deploy, td.getNumberOfQuestionsForNotification());
			List<ReminderDetails> overdueAttempts = new ArrayList<ReminderDetails>();
			while (rs.next())
			{
				overdueAttempts.add(new ReminderDetails(rs.getString("oucu"),
						rs.getString("pi"), rs.getInt("ti"), deploy));
			}
			return overdueAttempts;
		}
		finally
		{
			dat.finish();
		}

	}

	/**
	 * @param td
	 * @param rd
	 * @return the message that will be used to send to the student.
	 */
	String messageText(TestDeployment td, ReminderDetails rd)
			throws OmException, UtilityException, IOException {

		String template = templateLoader.loadStringTemplate("reminder.email.txt");
		template = removeLineBreakPrefixes(template);

		String msg = MessageFormat.format(template, icmaName(td, rd),
				extensionEndDate(td), navigatorUrlBase(rd));

		return messageSubjectLine(td, rd) + "\n\n" + msg;
	}

	/**
	 * Determines the subject line to use for the message by checking what we
	 * have available to use for it. If the iCMA code, module and endDate are
	 * know then we use a meaningful subject line. Otherwise we use a simpel default.
	 * @param td
	 * @param rd
	 * @return The subject line.
	 */
	private String messageSubjectLine(TestDeployment td, ReminderDetails rd)
			throws OmException, UtilityException
	{

		String icma = icmaName(td, rd);
		String module = td.getModule();
		String endedOn = td.displayCloseDate();

		if (StringUtils.isNotEmpty(icma) && StringUtils.isNotEmpty(module)
				&& StringUtils.isNotEmpty(endedOn))
		{
			return MessageFormat.format("Summative {0} on module {1} closed on {2}.",
					icma, module, endedOn);
		}
		else
		{
			return MessageFormat.format("Your test closed on {0}", endedOn);
		}
	}

	/**
	 * Tidies up the string argument so that the String can be used correctly
	 * with the Authentication implementation of sendMail.
	 * @param msg the message as loaded from the template.
	 * @return the message with leading \r and \n stripped.
	 */
	private String removeLineBreakPrefixes(String msg)
	{
		while (msg.length() > 1 && msg.startsWith("\n") || msg.startsWith("\r"))
		{
			msg = msg.substring(1, msg.length());
		}
		return msg;
	}

	/**
	 * @param rd
	 * @return The URL of the test.
	 */
	private String navigatorUrlBase(ReminderDetails rd)
	{
		return nc.getPublicUrl() + rd.getDeploy() + "/";
	}

	/**
	 * Get the end date of the Test for placement within the message being
	 * sent to the student.
	 * @param td
	 * @return
	 */
	private String extensionEndDate(TestDeployment td)
			throws OmException, UtilityException
	{

		String extensionEndDate = td.displayForbidExtensionDate();

		if (StringUtils.isEmpty(extensionEndDate))
		{
			extensionEndDate = "warning - no test finish date specified";
		}

		return extensionEndDate;
	}

	/**
	 * Get the iCMA code for a test if there is one, otherwise just use the deploy file name.
	 * @param td
	 * @param rd
	 * @return the name to use to refer to this test in the email.
	 */
	private String icmaName(TestDeployment td, ReminderDetails rd)
			throws UtilityException
	{
		String icma = td.getIcma();

		if (StringUtils.isEmpty(icma))
		{
			icma = rd.getDeploy();
		}

		return icma;
	}
}
