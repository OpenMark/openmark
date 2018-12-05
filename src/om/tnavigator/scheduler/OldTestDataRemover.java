package om.tnavigator.scheduler;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import om.Log;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.DatabaseAccess.Transaction;
import om.tnavigator.db.OmQueries;
import om.tnavigator.teststructure.TestDeployment;
import util.misc.PeriodicThread;

public class OldTestDataRemover extends PeriodicThread
{

	private final static Pattern filenamePattern = Pattern.compile("^(.*)\\.deploy\\.xml$");

	private Log log;

	private NavigatorConfig nc;

	DatabaseAccess da;

	private OmQueries oq;

	/**
	 * Constructor.
	 * @param nc reference to the NavigatorConfig to use.
	 * @param log reference to the Log to use.
	 * @param da reference to the DatabaseAccess to use.
	 * @param oq reference to the OmQueries to use.
	 * @param delay time to wait between runs of this clean-up task in hours.
	 */
	public OldTestDataRemover(NavigatorConfig nc, Log log, DatabaseAccess da, OmQueries oq, int delay)
	{
		super(delay * 60 * 60 * 1000);
		this.nc = nc;
		this.log = log;
		this.da = da;
		this.oq = oq;
	}

	@Override
	protected void tick()
	{
		File maintenanceFile = new File(nc.getMaintenanceModeFilePath());
		if (maintenanceFile.exists())
		{
			// Don't run this job when the system is in maintenance mode.
			return;
		}
		File stopFile = new File(nc.getDeleteOldDataFilePath());
		if (stopFile.exists())
		{
			log.logWarning("Scheduler Jobs", "Delete old test data job not running because stop file is present.");
			return;
		}
		log.logNormal("OldTestDataRemover", "Delete old test data job starting.");
		try
		{
			boolean recordsDeleted = true;
			long startTime = System.currentTimeMillis();
			long maxDurationInMilliseconds = 10 * 60 * 1000; // 10 Minutes.
			while (System.currentTimeMillis() < startTime + maxDurationInMilliseconds && recordsDeleted)
			{
				log.logNormal("OldTestDataRemover", "Starting a new batch of up to 1000 test attempts.");
				Transaction dat = null;
				try
				{
					dat = da.newTransaction();
					// Do we want to create the temp table in the loop?
					oq.createTempDeployTable(dat, this.log);
					loadCleanAfterDays(dat);
					recordsDeleted = removeOldTestInstance(dat);
					if (!recordsDeleted)
					{
						log.logDebug("OldTestDataRemover", "No records to delete in this batch.");
					}
				}
				catch (Exception x)
				{
					log.logError("OldTestDataRemover", "Error deleting old test data job.", x);
				}
				finally
				{
					if (dat != null) {
						try
						{
							oq.dropTempDeployTable(dat, this.log);
						}
						catch (Exception x)
						{
							log.logError("OldTestDataRemover", "Error dropping temp table. Run abandoned", x);
						}
						dat.finish();
					}
				}
			}
			log.logNormal("OldTestDataRemover", "Delete old test data job completed normally.");
		}
		catch (Exception x)
		{
			log.logError("OldTestDataRemover", "Error deleting old test data job. Run abandoned", x);
		}
	}

	/**
	 * Get the deploy file names and load the clean after day data.
	 * @param dat the transaction in which to do the work.
	 */
	private void loadCleanAfterDays(Transaction dat)
	{
		File testBank = new File(nc.getTestbankPath());
		File[] deployFiles = testBank.listFiles(new FilenameFilter()
				{
					@Override
					public boolean accept(File f, String name)
					{
						return filenamePattern.matcher(name).matches();
					}
				});

		for (int i = 0; i < deployFiles.length; i++)
		{
			File deployFile = deployFiles[i];
			String deploy = testNameFromDeployFile(deployFile);
			try
			{
				TestDeployment td = new TestDeployment(deployFile);
				int cleanDays = td.getCleanDataAfterDays();
				if (cleanDays == 0)
				{
					cleanDays = 1278;
				}
				oq.insertClnAfterDays(dat, cleanDays, deploy);
			}
			catch (Exception x)
			{
				log.logError("OldTestDataRemover", "Error inserting deploy file " + deploy, x);
			}
		}
	}

	/**
	 * Remove one batch of up to 1000 test attempts.
	 * @param dat the transaction in which to do the work.
	 * @return true if any records were deleted (so there might be more).
	 */
	private boolean removeOldTestInstance(Transaction dat)
			throws SQLException
	{
		boolean recordsDeleted = false;
		ResultSet rs = null;
		rs = oq.queryOldAttemptsNeedingCleaning(dat);
		while (rs.next())
		{
			String ti = rs.getString("ti");
			String oucu = rs.getString("oucu");
			String deploy = rs.getString("deploy");
			Transaction deleteTransaction = null;
			try
			{
				deleteTransaction = da.newTransaction();
				oq.deleteEntireTestAttempts(deleteTransaction, ti);
				log.logNormal("OldTestDataRemover", "Old test attempt ti=" + ti + " at test " +
						deploy + " by user " + oucu + " deleted.");
				recordsDeleted = true;
			}
			catch(SQLException ex)
			{
				log.logError("OldTestDataRemover", "Error deleting test for test attempt ti=" +
						ti + " at test deploy " + deploy + " by user " + oucu, ex);
			}
			finally
			{
				deleteTransaction.finish();
			}
		}
		return recordsDeleted;
	}

	/**
	 * Get the test name (as it goes in the URL) from the name of a deploy file.
	 * @param fileName
	 * @return
	 */
	private String testNameFromDeployFile(File deployFile)
	{
		Matcher m = filenamePattern.matcher(deployFile.getName());
		m.matches();
		return m.group(1);
	}
}
