package om.tnavigator.auth;

import java.io.File;

import om.Log;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.db.DatabaseAccess;

public class AuthenticationInitialisation {

	private DatabaseAccess databaseAccess;

	private NavigatorConfig navigatorConfig;

	private File templatesFolder;

	private Log log;

	public DatabaseAccess getDatabaseAccess() {
		return databaseAccess;
	}

	public NavigatorConfig getNavigatorConfig() {
		return navigatorConfig;
	}

	public File getTemplatesFolder() {
		return templatesFolder;
	}

	public Log getLog() {
		return log;
	}

	public AuthenticationInitialisation(DatabaseAccess da, NavigatorConfig nc,
		File folder, Log globalLog) {
		databaseAccess = da;
		navigatorConfig = nc;
		templatesFolder = folder;
		log = globalLog;
	}

}
