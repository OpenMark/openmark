package om.tnavigator;

import om.Log;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.OmQueries;
import util.misc.GeneralUtils;
import util.misc.UtilityException;

/**
 * An abstraction helper for persistence within OpenMark.  This class allows
 *  for communication with the OpenMark database implementation.
 * @author Trevor Hinson
 */

public abstract class AbstractPersistenceDelegator {

	private NavigatorConfig navigatorConfig;

	private DatabaseAccess da;

	private OmQueries oq;

	private String logPath;

	private boolean showDebug;

	private Log log;

	/**
	 * Sets up and returns the Log implementation for this class and child
	 *  classes.
	 * @return
	 * @throws UtilityException
	 * @author Trevor Hinson
	 */
	protected Log getLog() throws UtilityException {
		if (null == log) {
			log = GeneralUtils.getLog(getClass(), logPath, showDebug);
		}
		return log;
	}

	/**
	 * Used to setup the persistence delegation and should be invoked by the
	 *  child of this class BEFORE any other database operations are carried out
	 * @param nc
	 * @param l
	 * @throws PersistenceException
	 * @author Trevor Hinson
	 */
	protected void initialise(NavigatorConfig nc, String path, boolean debug)
			throws UtilityException {
		logPath = path;
		showDebug = debug;
		setNavigatorConfig(nc);
		databaseAccessSetup();
		getLog().logDebug("Finished the parent initialisation of"
			+ " the Persistence implementation ... " + getClass().getName());
	}

	/**
	 * Sets up the database access from that provided at runtime.  Specifically
	 *  the NotifierConfig.  Assumes a single thread model and as such if the
	 *  child implementation of this class is to run within a multi threaded
	 *  environment then the neccessary steps should be taken to ensure that
	 *  things are only initialised once.
	 * @throws PersistenceException
	 * @author Trevor Hinson
	 */
	protected void databaseAccessSetup() throws UtilityException {
		String dbClass = getNavigatorConfig().getDBClass();
		String dbPrefix = getNavigatorConfig().getDBPrefix();
		try {
			oq = (OmQueries) Class.forName(dbClass).getConstructor(
					new Class[] { String.class }).newInstance(
					new Object[] { dbPrefix });
			da = new DatabaseAccess(
					getNavigatorConfig().hasDebugFlag("log-sql") ? getLog() : null);
		} catch (Exception e) {
			throw new UtilityException(
				"Error creating database class or JDBC driver (make sure DB "
				+ "plugin and JDBC driver are both installed): "
				+ e.getMessage());
		}
		DatabaseAccess.Transaction dat = null;
		try {
			dat = da.newTransaction();
			oq.checkTables(dat, getLog(), getNavigatorConfig());
		} catch (Exception e) {
			throw new UtilityException("Error initialising database tables: "
					+ e.getMessage(), e);
		} finally {
			if (dat != null)
				dat.finish();
		}
	}

	/**
	 * Simple accessor method for the child classes to invoke in order to run
	 *  operations on the database.v
	 * @return
	 * @throws PersistenceException
	 */
	protected DatabaseAccess getDatabaseAccess() throws UtilityException {
		if (null == da) {
			throw new UtilityException("Unable to continue as the"
				+ " DatabaseAccess object was null.");
		}
		return da;
	}

	protected void setNavigatorConfig(NavigatorConfig nc) {
		navigatorConfig = nc;
	}

	protected NavigatorConfig getNavigatorConfig() throws UtilityException {
		if (null == navigatorConfig) {
			throw new UtilityException("Unable to continue as the"
					+ " NavigatorConfig was null.");
		}
		return navigatorConfig;
	}

}
