package om.tnavigator.util;

import javax.servlet.ServletContext;

import om.OmException;
import om.abstractservlet.RequestParameterNames;
import om.tnavigator.auth.Authentication;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.OmQueries;

/**
 * Holds reference to the primary objects of an OpenMark running instance. The
 *  concept here is that an instance of this class should exist for the duration
 *  of a request.  The object is then passed to where it is needed in a
 *  "visitor" manner and called upon where necessary.  The approach is taken to
 *  refactor the controllers and to move the model into seperate testable units.
 * @author Trevor Hinson
 */

public class OMVisitor {

	private DatabaseAccess databaseAccess;

	private OmQueries omQueries;

	private Authentication authentication;

	private ServletContext servletContext;

	public ServletContext getServletContext() throws OmException {
		if (null == servletContext) {
			throwNullCompositeObject(RequestParameterNames.ServletContext);
		}
		return servletContext;
	}

	public DatabaseAccess getDatabaseAccess() throws OmException {
		if (null == databaseAccess) {
			throwNullCompositeObject(RequestParameterNames.DatabaseAccess);
		}
		return databaseAccess;
	}

	public OmQueries getOmQueries() throws OmException {
		if (null == omQueries) {
			throwNullCompositeObject(RequestParameterNames.OmQueries);
		}
		return omQueries;
	}

	public Authentication getAuthentication() throws OmException {
		if (null == authentication) {
			throwNullCompositeObject(RequestParameterNames.Authentication);
		}
		return authentication;
	}

	private void throwNullCompositeObject(RequestParameterNames rpn)
		throws OmException {
		throw new OmException("Unable to continue as the" +
			rpn + " implementation was null.");
	}

	public OMVisitor(DatabaseAccess da, OmQueries om, Authentication auth,
		ServletContext context) {
		databaseAccess = da;
		omQueries = om;
		authentication = auth;
		servletContext = context;
	}

	public OMVisitor(ServletContext context) {
		servletContext = context;
	}
}
