package om.tnavigator.auth;

import om.Log;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.sessions.TemplateLoader;

public class AuthenticationInitialisation {

	private DatabaseAccess databaseAccess;

	private NavigatorConfig navigatorConfig;

	private TemplateLoader templatesLoader;

	private Log log;

	public DatabaseAccess getDatabaseAccess() {
		return databaseAccess;
	}

	public NavigatorConfig getNavigatorConfig() {
		return navigatorConfig;
	}

	public TemplateLoader getTemplateLoader() {
		return templatesLoader;
	}

	public Log getLog() {
		return log;
	}

	public AuthenticationInitialisation(DatabaseAccess da, NavigatorConfig nc,
			TemplateLoader tl, Log globalLog) {
		databaseAccess = da;
		navigatorConfig = nc;
		templatesLoader = tl;
		log = globalLog;
	}

}
