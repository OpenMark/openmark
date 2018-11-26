package om.tnavigator.scheduler;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import om.Log;
import om.OmException;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.DatabaseAccess.Transaction;
import om.tnavigator.db.OmQueries;
import om.tnavigator.teststructure.TestDeployment;
import util.misc.PeriodicThread;

public class oldTestDataRemover extends PeriodicThread
{

	/** Frequency at which we delete student test data */
	private final static int DELETE_TEST_DATA_DELAY = 1 * 60 * 1000;

	private final static Pattern filenamePattern = Pattern.compile("^(.*)\\.deploy\\.xml$");

	private Log log;

	private NavigatorConfig nc;

	DatabaseAccess da;

	private OmQueries oq;

	/**
	 * Constuctor.
	 * @param nc reference to the NavigatorConfig to use.
	 * @param log reference to the Log to use.
	 * @param da reference to the DatabaseAccess to use.
	 * @param oq reference to the OmQueries to use.
	 * @param delay reference to config time delay.
	 */
	public oldTestDataRemover(NavigatorConfig nc, Log log, DatabaseAccess da, OmQueries oq, String Delay) {
		super(setDataDelay(Delay, log));
		this.nc = nc;
		this.log = log;
		this.da = da;
		this.oq = oq;
	}

	/**
	 * Change the job Delay from config
	 * @param delay
	 * @param log
	 */
	private static int setDataDelay(String Delay, Log log) {
		if (!Delay.equals("0")) {
			return Integer.parseInt(Delay)* 60 * 60 * 1000;
		} else {
			return DELETE_TEST_DATA_DELAY;
		}
	}

	@Override
	protected void tick()
	{
		File maintenanceFile = new File(nc.getMaintenanceModeFilePath());
		if (maintenanceFile.exists()) {
			// Don't run job.
			return;
		}
		File stopFile = new File(nc.getDeleteOldDataFilePath());
		if (stopFile.exists()) {
			log.logWarning("Scheduler Jobs", "Delete old test data job not running because stop file is present.");
			return;
		}
		try{
			boolean isRecordExists = true;
			long startTime = System.currentTimeMillis();
			long maxDurationInMilliseconds = 10 * 60 * 1000;
			while (System.currentTimeMillis() < startTime + maxDurationInMilliseconds && isRecordExists) {
				Transaction dat = da.newTransaction();
				try
				{
					log.logNormal("oldTestDataRemover", "Delete old test data job starting.");
					oq.createTempDeployTable(dat, this.log);
					loadClnAfterDays(dat);
					isRecordExists = removeOldTestInstance(dat);
					if (!isRecordExists) {
						log.logDebug("oldTestDataRemover", "No records to delete.");
					}
				} catch (Exception x)
				{
					log.logError("oldTestDataRemover", "Error deleting old test data job. Run abandoned", x);
				} finally
				{
					oq.dropTempDeployTable(dat , this.log);
					dat.finish();
				}
			}
		}catch (Exception x) {
			log.logError("oldTestDataRemover", "Error deleting old test data job. Run abandoned", x);
		}
		log.logNormal("oldTestDataRemover", "Delete old test data job completed normally.");
	}

	/**
	 * Get the deploy file names and load the clean after day data.
	 * @param dat
	 */
	private void loadClnAfterDays(Transaction dat) {
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
				insertClnAfterDays(dat, deployFile, deploy);
			}
			catch (Exception x) {
				log.logError("oldTestDataRemover", "Error inserting deploy file " + deploy, x);
			}
		}
	}

	/**
	 * Remove Test Data Delay time from config
	 * @param dat
	 */
	private boolean removeOldTestInstance(Transaction dat)
			throws SQLException
	{
		boolean isrecordexist = false;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try
		{
			 Map<String, List<String>> testData  = oq.queryOldAttemptsNeedingCleaning(dat);
			if (!testData.isEmpty()) {
				oq.deleteEntireTestAttempts(testData, dat, this.log);
				isrecordexist = true;
			}
		} catch(SQLException ex)
		{
			log.logError("oldTestDataRemover", "Error deleting old test data job. Run abandoned", ex);
		}
		return isrecordexist;
	}

	/**
	 * Insert the deploy file data to temporary table
	 * @param dat
	 * @param deployFile
	 * @param deploy
	 */
	private void insertClnAfterDays(Transaction dat, File deployFile, String deploy)
			throws OmException, SQLException
	{
		String cleanDays = "1278";
		TestDeployment td = new TestDeployment(deployFile);
		if (td.getCleanDataAfterDays() != null) {
			cleanDays = td.getCleanDataAfterDays();
		}
		oq.insertClnAfterDays(dat, cleanDays, deploy, this.log);
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
