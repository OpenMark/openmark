package om.abstractservlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.tnavigator.auth.Authentication;
import om.tnavigator.auth.AuthenticationFactory;
import om.tnavigator.auth.AuthenticationInstantiationException;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.OmQueries;
import om.tnavigator.util.OMVisitor;

/**
 * Servlets (webapps) that require access to the OpenMark database should extend
 *  this implementation as it provides everything setup for the
 *  RequestAssociates and tidies itself on shutdown. 
 * @author Trevor Hinson
 */

public abstract class AbstractDatabaseSpecificOpenMarkServlet
	extends AbstractOpenMarkServlet {

	private static final long serialVersionUID = -7047308575322691989L;

	private DatabaseAccess databaseAccess;

	private OmQueries omQueries;

	private Authentication authentication;

	protected OmQueries getOmQueries() throws ServletException {
		if (null == omQueries) {
			throw new ServletException(getErrorMessageFor(
				RequestParameterNames.OmQueries.toString()));
		}
		return omQueries;
	}

	protected Authentication getAuthentication() throws ServletException {
		if (null == authentication) {
			throw new ServletException(getErrorMessageFor(
				RequestParameterNames.Authentication.toString()));
		}
		return authentication;
	}

	protected DatabaseAccess getDatabaseAccess() throws ServletException {
		if (null == databaseAccess) {
			throw new ServletException(getErrorMessageFor(
				RequestParameterNames.DatabaseAccess.toString()));
		}
		return databaseAccess;
	}

	@Override
	protected String getErrorTemplateName() {
		return ErrorManagement.ADMINISTRATION_ERROR_TEMPLATE;
	}

	/**
	 * Applies the DatabaseAccess and OmQueries to the RequestAssociates for
	 *  use within the RequestHandler implementations.
	 * @author Trevor Hinson
	 */
	protected RequestAssociates retrieveRequestAssociates(boolean post,
		HttpServletRequest request, HttpServletResponse response)
		throws ServletException {
		RequestAssociates ra = super.retrieveRequestAssociates(post, request, response);
		ra.getPrincipleObjects().put(
			RequestParameterNames.DatabaseAccess.toString(), getDatabaseAccess());
		ra.getPrincipleObjects().put(RequestParameterNames.OmQueries.toString(),
			getOmQueries());
		ra.getPrincipleObjects().put(RequestParameterNames.Authentication.toString(),
			getAuthentication());
		ra.getPrincipleObjects().put(RequestParameterNames.OMVisitor.toString(),
			getOMVisitor(ra));
		return ra;
	}

	public void init() throws ServletException {
		super.init();
		initialiseDatabase();
		initialiseAuthentication();
	}

	@Override
	public void destroy() {
		super.destroy();
		if (null != databaseAccess) {
			databaseAccess.close();
		}
	}

	protected OMVisitor getOMVisitor(RequestAssociates ra) {
		return new OMVisitor(databaseAccess, omQueries, authentication,
			getServletContext());
	}

	protected void initialiseAuthentication() throws ServletException {
		try {
			authentication = AuthenticationFactory.initialiseAuthentication(
				getNavigatorConfig(), getDatabaseAccess(), getTemplateLoader(),
				getLog());
		} catch (AuthenticationInstantiationException x) {
			throw new ServletException(x);
		}
	}

	/**
	 * Sets up the database communication for use by the RequestHandler
	 *  implementations.
	 * @throws ServletException
	 */
	private void initialiseDatabase() throws ServletException {
		String dbClass = getNavigatorConfig().getDBClass();
		String dbPrefix = getNavigatorConfig().getDBPrefix();
		try {
			omQueries = (OmQueries) Class.forName(dbClass).getConstructor(
				new Class[] { String.class }).newInstance(
				new Object[] { dbPrefix });
			databaseAccess = new DatabaseAccess(getNavigatorConfig().getDatabaseURL(
				omQueries), getNavigatorConfig().hasDebugFlag("log-sql") ? getLog() : null);
		} catch (Exception e) {
			throw new ServletException(
				"Error creating database class or JDBC driver"
				+ " (make sure DB plugin and JDBC driver are both installed): "
				+ e.getMessage());
		}
		DatabaseAccess.Transaction dat = null;
		try {
			dat = databaseAccess.newTransaction();
			omQueries.checkTables(dat, getLog(), getNavigatorConfig());
		} catch (Exception e) {
			throw new ServletException("Error initialising database tables: "
				+ e.getMessage(), e);
		} finally {
			if (dat != null) {
				dat.finish();
			}
		}
	}

}
