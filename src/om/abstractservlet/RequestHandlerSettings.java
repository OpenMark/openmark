package om.abstractservlet;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import util.misc.GeneralUtils;
import util.misc.UtilityException;

/**
 * Holds reference to the configuration specified in the requestHandlers.xml
 *  Each <requestHandler> Element is parsed and turned into an instance of
 *  this class for easier interogation at runtime.
 * @author Trevor Hinson
 */

public class RequestHandlerSettings {

	private String invocationPath;
	
	private String servletName="";

	private Class<RequestHandler> requestHandlerClassReference;

	private Map<String, String> parameters = new HashMap<String, String>();
	
	private static String SERVLETNAME = "openmark-admin";


	public RequestHandlerSettings(String path, Class<RequestHandler> requestHandler,
		Map<String, String> params,String sn) {
		requestHandlerClassReference = requestHandler;
		invocationPath = path;
		servletName=sn;
		if (null != params) {
			parameters.putAll(params);
		}
	}

	public String get(RequestHandlerEnums rhe) {
		String param="";
		if(getParameters().get(rhe.toString())!= null)
		{
			param=getParameters().get(rhe.toString());
		}
		return param;
	}

	protected boolean is(RequestHandlerEnums rhe) {
		return "true".equalsIgnoreCase(getParameters()
			.get(rhe.toString())) ? true : false;
	}

	public Class<RequestHandler> getRequestHandlerClass() {
		return requestHandlerClassReference;
	}

	public String getInvocationPath() {
		return invocationPath;
	}

	public Map<String, String> getParameters() {
		if (null == parameters) {
			parameters = new HashMap<String, String>();
		}
		return Collections.unmodifiableMap(parameters);
	}

	/**
	 * Checks for the requires settings in order to ensure that the
	 *  configuration provided will work correctly.
	 * @return
	 * @throws UtilityException
	 * @author Trevor Hinson
	 */
	public boolean valid() throws UtilityException {
		boolean valid = false;
		if (null != invocationPath ? invocationPath.length() > 0 : false) {
			if (null != requestHandlerClassReference ? hasLogPath() : false) {
				valid = true;
			}
		}
		if (!valid) {
			throw new UtilityException("Unable to continue as these"
				+ " RequestHandlerSettings are invalid : " + toString());
		}
		return valid;
	}

	/**
	 * Checks that a logPath has been set within the composite parameters Map
	 * @return
	 * @author Trevor Hinson
	 */
	protected boolean hasLogPath() {
		boolean has = false;
		if (null != parameters) {
			String logPath = parameters.get(RequestParameterNames.logPath.toString());
			if (null != logPath ? logPath.length() > 0 : false) {
				has = true;
			}
		}
		return has;
	}

	public String toString() {
		String s = GeneralUtils.toString(this);
		return null != s ? s : super.toString();
	}

	public String getServletName() {
		// TODO Auto-generated method stub
		if (servletName==null)
		{
			servletName=SERVLETNAME;

		}
		return servletName;
	}
	
	public static Comparator<RequestHandlerSettings> rhsDisplayNameComparator 
    = new Comparator<RequestHandlerSettings>() 
	{
	
		public int compare(RequestHandlerSettings rhs1, RequestHandlerSettings rhs2) 
		{
		String rhs1Name = rhs1.get(RequestHandlerEnums.displayName).toUpperCase();
		String rhs2Name = rhs2.get(RequestHandlerEnums.displayName).toUpperCase();
		//ascending order
		 int ret=rhs1Name.compareTo(rhs2Name);
		 return ret;
		}

	};
}
