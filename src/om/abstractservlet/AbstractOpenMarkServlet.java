package om.abstractservlet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.Log;
import om.OmException;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.util.OMVisitor;
import util.misc.ErrorMessageParts;
import util.misc.Strings;
import util.misc.UtilityException;

/**
 * This abstractions provides for setup of using request delegation through
 *  to independent RequestHandlers based on the implementing supplied
 *  configuration.  As we currently (at time of typing) hold no other 
 *  methods (as per those within the industry - Spring etc) for catering for
 *  things we provide the approach here.  The implementing child Servlet class
 *  will still be able to choose the use of the RequestManagement or not.
 * <br /><br />
 * NOTE:</br />
 *  The composite RequestManagement implementation is optionally set either as
 *  a single instance within the Servlet (which is by default) OR alternatively
 *  if set within the NavigatorConfig as follows then this will be dynamically
 *  instantiated for each request.  The reason for this is so that the
 *  requestHandling.xml may be changed then at runtime if required - for example
 *  within an AdministrationServlet implementation or for when testing without
 *  a restart.  To configure the RequestManagement on a per request basis simply
 *  place the following node into the navigator.xml
 *  <br /><br />
 *  <RequestManagement>requestbased</RequestManagement>
 * TODO:
 * Currently the NavigatorConfig is placed within this class.  This is for
 *  the handling of IP restrictions which currently reside within this object.
 *  Ideally these should be extracted from the NavigatorConfig and placed into
 *  a seperate concern which this class is composed of instead.
 * 
 * @author Trevor Hinson
 */

public abstract class AbstractOpenMarkServlet extends HttpServlet {

	private static final long serialVersionUID = -6026057637254633705L;

	private static String PATH = "WEB-INF/templates";

	private static String NAVIGATOR_XML = "navigator.xml";

	private static String REQUEST_HANDLING_XML = "requestHandling.xml";

	private static String DEFAULT_ERROR_TITLE = "Request Handling Issue.";

	private RequestManagement requestManagement;

	private NavigatorConfig navigatorConfig;

	private Log log;

	private ErrorManagement errorManagement;

	public File getTemplatesFolder() {
		return new File(getServletContext().getRealPath(PATH));
	}

	protected String getErrorMessageFor(String s) {
		return "Unable to continue as the " + s + " implementation was null.";
	}

	protected Log getLog() throws ServletException {
		if (null == log) {
			throw new ServletException(getErrorMessageFor("Log"));
		}
		return log;
	}

	protected NavigatorConfig getNavigatorConfig() throws ServletException {
		if (null == navigatorConfig) {
			throw new ServletException(getErrorMessageFor("NavigatorConfig"));
		}
		return navigatorConfig;
	}

	protected ErrorManagement getErrorManagement() throws ServletException {
		if (null == errorManagement) {
			throw new ServletException(getErrorMessageFor("ErrorManagement"));
		}
		return errorManagement;
	}

	/**
	 * Setup what is necessary for every request of that is passed through the
	 *  child implementation of this HttpServlet.  For example: if you are using
	 *  the Database within your requests then you would configure a reference
	 *  to the Database objects within the RequestAssociates object so that the
	 *  object Database object itself can be passed to each RequestHandler
	 *  implementation without the need for the implementation to hold a member
	 *  variable composite reference.
	 * @param post
	 * @param request
	 * @param response
	 * @return
	 * @author Trevor Hinson
	 */
	protected RequestAssociates retrieveRequestAssociates(boolean post,
		HttpServletRequest request, HttpServletResponse response)
		throws ServletException {
		RequestAssociates ra = new RequestAssociates(getServletContext(),
			request.getPathInfo(), post, new HashMap<String, Object>());
		ra.setRequestParameters(getParameters(request));
		ra.getPrincipleObjects().put(RequestParameterNames.Log.toString(), log);
		ra.getPrincipleObjects().put(RequestParameterNames.NavigatorConfig.toString(),
			navigatorConfig);
		ra.getPrincipleObjects().put(RequestParameterNames.ErrorManagement.toString(),
			errorManagement);
		String rm = RequestParameterNames.RequestManagement.toString();
		ra.putPrincipleObject(rm, request.getAttribute(rm));
		return ra;
	}	

	/**
	 * Here we delegate straight away to the RequestManagement implementation
	 *  providing the constructed RequestAssociates object.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @author Trevor Hinson
	 */
	protected void handle(boolean posted, HttpServletRequest request,
		HttpServletResponse response) throws ServletException, IOException {
		RequestAssociates ra = null;
		try {
			RequestManagement rm = getRequestManagement();
			request.setAttribute(RequestParameterNames.RequestManagement.toString(), rm);
			ra = retrieveRequestAssociates(posted, request, response);
			rm.handleRequest(request, response, ra);
		} catch (Exception x) {
			dealWithErrors(request, response, x, ra);
		}
	}

	/**
	 * Delegates to the ErrorManagement for sending the appropriate error
	 *  details back to the user.
	 * 
	 * @param request
	 * @param response
	 * @param t
	 * @param ra
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void dealWithErrors(HttpServletRequest request,
		HttpServletResponse response, Throwable t, RequestAssociates ra)
		throws ServletException, IOException {
		ErrorMessageParts emp = getErrorMessageParts(request, response, t, ra);
		Map<String, String> replacements = getReplacements(request, response, ra, emp);
		try {
			getErrorManagement().sendErrorResponse(request, response, getOMVisitor(ra),
				emp, replacements);
		} catch (OmException x) {
			getLog().logError(
				"Problem when trying to handle the original error.", x);
			throw new ServletException(x);
		}
	}

	/**
	 * This default implementation instantiates a new OMVisitor with the 
	 *  ServletContext.  This should be overridden where needed.
	 * 
	 * @param ra
	 * @return
	 * @author Trevor Hinson
	 */
	protected OMVisitor getOMVisitor(RequestAssociates ra) {
		return new OMVisitor(getServletContext());
	}

	/**
	 * Standard implementation just returns an empty map.  The concrete child
	 *  instance of this class should override this as needed.
	 * 
	 * @param request
	 * @param response
	 * @param ra
	 * @return
	 * @author Trevor Hinson
	 */
	protected Map<String, String> getReplacements(HttpServletRequest request,
		HttpServletResponse response, RequestAssociates ra, ErrorMessageParts emp) {
		Map<String, String> replacements = new HashMap<String, String>();
		replacements.put("TITLE", emp.getTitle());
		replacements.put("MESSAGE", Strings.isNotEmpty(emp.getMessage())
			? emp.getMessage() : "No message specified.");
		if (null != emp.getThrowable()) {
			Throwable t = emp.getThrowable();
			replacements.put("EXCEPTION", Log.getOmExceptionString(t));
		}
		replacements.put("TIME", Log.DATETIMEFORMAT.format(new Date()));
		return replacements;
	}

	/**
	 * Builds a new ErrorMessageParts from the provided arguments.
	 *  
	 * @param t
	 * @param ra
	 * @return
	 * @author Trevor Hinson
	 */
	protected ErrorMessageParts getErrorMessageParts(HttpServletRequest request,
		HttpServletResponse response, Throwable t, RequestAssociates ra)
		throws ServletException {
		getLog().logError("Error on processing the request : ", t);
		return new ErrorMessageParts(getErrorTitle(t, ra), getErrorMessage(t, ra),
			false, t, getErrorTemplateName());
	}

	/**
	 * Invoked from getErrorMessageParts(... should be overridden where needed.
	 * 
	 * @return
	 * @author Trevor Hinson
	 */
	protected String getErrorTitle(Throwable t, RequestAssociates ra) {
		return DEFAULT_ERROR_TITLE;
	}

	protected String getErrorMessage(Throwable t, RequestAssociates ra) {
		return null != t ? t.getMessage() : "Unknown Error.  Please check the logs.";
	}

	protected abstract String getErrorTemplateName();

	/**
	 * For retrieving all the request parameters so that we do not need to pass
	 *  the HttpServletRequest around.
	 * 
	 * @param request
	 * @return
	 * @author Trevor Hinson
	 */
	public static Map<String, String> getParameters(HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();
		if (null != request) {
			Map<?,?> requestMap = request.getParameterMap();
			if (null != requestMap ? requestMap.size() > 0 : false) {
				for (Iterator<?> i = requestMap.keySet().iterator(); i.hasNext();) {
					Object key = i.next();
					Object val = requestMap.get(key);
					if ((null != key ?
						key instanceof String ? Strings.isNotEmpty((String) key)
							: false : false) ) {
						String value = null;
						if (null != val) {
							value = transferToStringFromStringArray(val);
						}
						params.put((String) key, value);
					}
				}
			}
		}
		return params;
	}

	public static String transferToStringFromStringArray(Object array) {
		StringBuffer sb = new StringBuffer();
		if (null != array) {
			if (array instanceof String[]) {
				int length = ((String[]) array).length;
				if (length > 0) {
					for (int i = 0; i < length; i++) {
						String str = ((String[]) array)[i];
						sb.append(str);
						if (i < length -1) {
							sb.append(",");
						}
					}
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Override within the child class but call back to here (super.init()) to 
	 *  ensure that the RequestManagement has been instantiated along with the
	 *  other features.
	 * 
	 * @author Trevor Hinson
	 */
	public void init() throws ServletException {
		initialiseLogging();
		initialiseNavigatorConfig();
		determineRequestManagementInitialisation();
		initialiseErrorManagement();
	}

	/**
	 * By default we initialise the RequestManagement implementation only once
	 *  at the init() of the Servlet itself.  However, it may be that from an
	 *  Administration perspective this is handled on a per request basis
	 *  (because that way the requestHandling.xml can be altered at runtime
	 *  without any server restarts).  As server restarts may be problematic 
	 *  within different environments.  Here we make a simple check first and
	 *  then initialise
	 * 
	 * @throws ServletException
	 * @author Trevor Hinson
	 */
	private void determineRequestManagementInitialisation()
		throws ServletException {
		if (!isRequestManagementRequestBased()) {
			requestManagement = initialiseRequestManagement();
		}
	}

	/**
	 * Reads the REQUEST_HANDLING_XML in order to establish a
	 *  RequestHandlingConfiguration then applies that to a new
	 *  RequestManagement implementation and assigns it to the composite
	 *  reference for use later.
	 * 
	 * @return
	 * @throws ServletException
	 * @author Trevor Hinson
	 */
	private RequestManagement initialiseRequestManagement()
		throws ServletException {
		RequestManagement rm = null;
		try {
			RequestHandlingConfiguration rhc = new RequestHandlingConfiguration(
				new File(getServletContext().getRealPath(REQUEST_HANDLING_XML)));
			rm = new RequestDelegation(rhc);
		} catch (IOException x) {
			throw new ServletException(x);
		} catch (RequestHandlingException x) {
			throw new ServletException(x);
		}
		return rm;
	}

	private boolean isRequestManagementRequestBased() throws ServletException {
		return getNavigatorConfig().isRequestManagementRequestBased();
	}

	/**
	 * Establishes the ErrorManagement implementation for use within the
	 *  RequestHandlers
	 * 
	 * @throws ServletException
	 * @author Trevor Hinson
	 */
	private void initialiseErrorManagement() throws ServletException {
		try {
			errorManagement = new ErrorManagement(navigatorConfig, log);
		} catch (UtilityException x) {
			throw new ServletException(x);
		}
	}

	/**
	 * Sets up the Log which may be used with each implementation.
	 * 
	 * @throws ServletException
	 * @author Trevor Hinson
	 */
	private void initialiseLogging() throws ServletException {
		try {
			log = new Log(new File(getServletContext().getRealPath("logs")),
				getClass().getName(), true);
		} catch (IOException x) {
			throw new ServletException("Error creating log", x);
		}
	}

	/**
	 * Sets up the NavigatorConfig for use within the RequestAssociates through
	 *  to the RequestHandler implementations.
	 * @throws ServletException
	 * @author Trevor Hinson
	 */
	private void initialiseNavigatorConfig() throws ServletException {
		try {
			navigatorConfig = new NavigatorConfig(
				new File(getServletContext().getRealPath(NAVIGATOR_XML)));
		} catch (MalformedURLException x) {
			throw new ServletException("Unexpected error parsing service URL",
				x);
		} catch (IOException x) {
			throw new ServletException("Error loading NavigatorConfig file", x);
		}
	}

	/**
	 * Returns access to the composite RequestManagement so as to be able to
	 *  pick up the neccessary RequestHandler at runtime.  Note that the
	 *  RequestManagement implementation does not have to be established at
	 *  init() of the Servlet implementation (default implementation). 
	 *  So if the <RequestManagement>requestbased</RequestManagement> has been
	 *  set within the NavigatorConfig then we return a new implementation each
	 *  time from this method.  This is so that certain environments do not
	 *  have to be restarted after a change to the requestHandling.xml
	 * @return
	 * @throws ServletException
	 * @author Trevor Hinson
	 */
	protected RequestManagement getRequestManagement() throws ServletException {
		RequestManagement rm = requestManagement;
		if (null == rm) {
			if (isRequestManagementRequestBased()) {
				rm = initialiseRequestManagement();
			}
			if (null == rm) {
				throw new ServletException("Unable to continue as the"
					+ " RequestManagement composite class implementation was null."
					+ " Please check that your configuration is correct and that"
					+ " your call super.init() from your child class.");
			}
		}
		return rm;
	}

	/**
	 * This method wraps the getServletConfig().getInitParameterNames() so that
	 *  it can be overridden easily within test cases for better testing of 
	 *  that which makes use of it.
	 * @return
	 * @author Trevor Hinson
	 */
	protected Enumeration<?> retrieveInitialParameters() {
		return getServletConfig().getInitParameterNames();
	}

	/**
	 * This method wraps the getServletConfig().getInitParameterNames() so that
	 *  it can be overridden easily within test cases for better testing of 
	 *  that which makes use of it.
	 * @return
	 * @author Trevor Hinson
	 */
	protected String getInitialParameter(String key) {
		return getServletConfig().getInitParameter(key);
	}

	@Override
	protected void doGet(HttpServletRequest request,
		HttpServletResponse response) throws ServletException, IOException {
		handle(false, request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
		HttpServletResponse response) throws ServletException, IOException {
		handle(true, request, response);
	}

	@Override
	public void destroy() {
		try {
			if (null != getLog()) {
				getLog().close();
			}
		} catch (ServletException x) {
			x.printStackTrace();
		}
	}
}
