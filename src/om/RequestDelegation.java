package om;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;

import om.tnavigator.NavigatorConfig;
import om.tnavigator.auth.Authentication;
import om.tnavigator.auth.UserDetails;

import util.misc.FinalizedResponse;
import util.misc.IPAddressCheckUtil;
import util.misc.UtilityException;
import util.xml.XHTML;
import util.xml.XML;
import util.xml.XMLException;

/**
 * A means of handling a users request through the configuration of different
 *  request handlers for the url path.  This class utilises the configured
 *  composite RequestHandlingConfiguration for delegating a users request
 *  appropriately.
 * 
 * @author Trevor Hinson
 */
public class RequestDelegation implements RequestManagement {

	private static final long serialVersionUID = -5410620397155010135L;

	private static String EN = "en";

	private RequestHandlingConfiguration requestHandlingConfiguration;

	public RequestDelegation(RequestHandlingConfiguration configuration) {
		requestHandlingConfiguration = configuration;
	}

	@Override
	public Map<String, RequestHandlerSettings> getSettings() {
		Map<String, RequestHandlerSettings> settings
			= new HashMap<String, RequestHandlerSettings>();
		if (null != requestHandlingConfiguration) {
			Map<String, RequestHandlerSettings> config
				= requestHandlingConfiguration.getSettings();
			if (null != config ? config.size() > 0 : false) {
				settings.putAll(config);
			}
		}
		return Collections.unmodifiableMap(settings);
	}

	@Override
	public void handleRequest(HttpServletRequest request,
		HttpServletResponse response, RequestAssociates associates)
		throws RequestHandlingException {
		if (null != request && null != response && null != associates) {
			RequestHandlerSettings rhs = requestHandlingConfiguration
				.getRequestHandlerSettings(request.getPathInfo());
			if (null != rhs ? rhs.valid() : false) {
				if (allowedIPAddress(rhs, associates, request)
					&& isUserAllowed(request, response, rhs, associates)) {
					RequestHandler rh = newRequestHandler(rhs.getRequestHandlerClass());
					try {
						if (null != rh) {
							delegateToRequestHandler(request, response, rh,
								associates, rhs);
						} else {
							throw new RequestHandlingException(
								"There is not a RequestHandler configured for this path : "
								+ request.getPathInfo());
						}
					} catch (Exception x) {
						if (null != rh) {
							try {
								rh.close(null);
							} catch (UtilityException e) {
								e.printStackTrace();
							}
						}
						throw new RequestHandlingException(x);
					}
				} else {
					throw new RequestHandlingException(
						"Access denied for the requested url : "
						+ request.getPathInfo());
				}
			} else {
				throw new RequestHandlingException("Unable to continue as the"
					+ " RequestHandlerSettings were null or invalid for the path : "
					+ request.getPathInfo());
			}
		} else {
			throw new RequestHandlingException("Invalid invocation.  "
				+ "One of the required arguments was null : "
				+ "\n HttpServletRequest = " + request
				+ "\n HttpServletResponse = " + response
				+ "\n RequestAssociates = " + associates);
		}
	}

	/**
	 * Delegates processing to the identified RequestHandler implementation and
	 *  handles the response from that.
	 * 
	 * @param request
	 * @param response
	 * @param rh
	 * @param ra
	 * @param rhs
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	protected void delegateToRequestHandler(HttpServletRequest request,
		HttpServletResponse response, RequestHandler rh, RequestAssociates ra,
		RequestHandlerSettings rhs) throws RequestHandlingException {
		try {
			addToRequestAssociates(rhs, ra);
			RequestResponse rr = rh.handle(request, response, ra);
			rh.close(null);
			if (null != rr ? rr.isSuccessful() : false) {
				if (rr.asXHTML()) {
					output(request, response, rr.toString());
				} else {
					try {
						response.setContentType("application/xhtml+xml");
						PrintWriter pw = response.getWriter();
						pw.write(rr.toString());
						pw.close();
					} catch (IOException x) {
						throw new RequestHandlingException(x);
					}
				}
			} else {
				throw new RequestHandlingException(
					"The request was uncessessful : " + rr);
			}
		} catch (UtilityException x) {
			throw new RequestHandlingException(x);
		}
	}

	/**
	 * Streams the output back to the user using the legacy implementation.
	 * 
	 * @param request
	 * @param response
	 * @param output
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	protected void output(HttpServletRequest request,
		HttpServletResponse response, String output) throws RequestHandlingException {
		try {
			Document d = XML.parse(output);
			XHTML.output(d, request, response, EN);
		} catch (XMLException x) {
			throw new RequestHandlingException(x);
		} catch (IOException x) {
			throw new RequestHandlingException(x);
		}
	}

	/**
	 * Checks the RequestHandlerSettings to see if we need to check for 
	 *  requiestSecureIP or requiresTrustedIP.  If either of these are set to
	 *  true then we carry out the test itself.
	 * 
	 * @param rhs
	 * @param ra
	 * @param request
	 * @return
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	protected boolean allowedIPAddress(RequestHandlerSettings rhs,
		RequestAssociates ra, HttpServletRequest request)
		throws RequestHandlingException {
		boolean allowed = false;
		if (null != rhs && null != ra) {
			try {
				boolean requiresTrustedIP = rhs.is(
					RequestHandlerEnums.requiresTrustedIP);
				boolean requiresSecureIP = rhs.is(
					RequestHandlerEnums.requiresSecureIP);
				if (!requiresTrustedIP && !requiresSecureIP) {
					allowed = true;
				} else {
					if (requiresSecureIP) {
						allowed = IPAddressCheckUtil.checkSecureIP(request,
							ra.getLog(), ra.getNavigatorConfig());
					}
					if (requiresTrustedIP) {
						NavigatorConfig nc = ra.getNavigatorConfig();
						Log l = ra.getLog();
						allowed = IPAddressCheckUtil.isIPInList(
							InetAddress.getByName(request.getRemoteAddr()),
							nc.getTrustedAddresses(), l);
					}
				}
			} catch (UnknownHostException x) {
				throw new RequestHandlingException(x);
			}
		}
		return allowed;
	}

	/**
	 * Checks the settings RequestHandlerEnums.requiresAuthentication of the
	 *  RequestHandlerSettings and the RequestHandlerEnums.adminUsersOnly and
	 *  if either are set to true then it delegates to the Authentication
	 *  implementation to get hold of the users details and check on them.
	 * 
	 * @param request
	 * @param response
	 * @param rhs
	 * @param ra
	 * @return
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	protected boolean isUserAllowed(HttpServletRequest request,
		HttpServletResponse response, RequestHandlerSettings rhs,
		RequestAssociates ra) throws RequestHandlingException {
		boolean allowed = false;
		if (null != rhs && null != request && null != ra) {
			boolean requiresAuthentication = rhs.is(
				RequestHandlerEnums.requiresAuthentication);
			boolean adminUsersOnly = rhs.is(RequestHandlerEnums.adminUsersOnly);
			if (requiresAuthentication || adminUsersOnly) {
				Authentication authentication = ra.get(Authentication.class,
					RequestParameterNames.Authentication.toString());
				try {
					UserDetails ud = authentication.getUserDetails(request,
						response, false);
					if (ud.isLoggedIn()) {
						if (adminUsersOnly) {
							if (Arrays.asList(
								ra.getNavigatorConfig().getStandardAdminUsernames())
									.contains(ud.getUsername())) {
								allowed = true;
							}
						} else {
							allowed = true;
						}
					}
				} catch (IOException x) {
					throw new RequestHandlingException(x);
				}
			} else {
				allowed = true;
			}
		}
		return allowed;
	}

	/**
	 * Adds the configuration from the RequestHandlerSettings to the RequestAssociates
	 *  composite configuration map so that the settings may be used by the
	 *  RequestHandler implementation.
	 * 
	 * @param rhs
	 * @param associates
	 * @author Trevor Hinson
	 */
	protected void addToRequestAssociates(RequestHandlerSettings rhs,
		RequestAssociates associates) {
		if (null != rhs && null != associates) {
			Map<String, String> params = rhs.getParameters();
			if (null != params ? params.size() > 0 : false) {
				for (String key : params.keySet()) {
					associates.addConfiguration(key, params.get(key));
				}
			}
		}
	}

	/**
	 * Instantiates a new RequestHander implementation from the provided Class
	 *  representation.
	 * 
	 * @param cla
	 * @return
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	protected RequestHandler newRequestHandler(Class<RequestHandler> cla)
		throws RequestHandlingException {
		RequestHandler rh = null;
		if (null != cla) {
			try {
				rh = cla.newInstance();
			} catch (InstantiationException x) {
				throw new RequestHandlingException(x);
			} catch (IllegalAccessException x) {
				throw new RequestHandlingException(x);
			}
		}
		return rh;
	}

	@Override
	public FinalizedResponse close(Object o) throws UtilityException {
		
		return null;
	}

}
