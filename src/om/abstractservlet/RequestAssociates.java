package om.abstractservlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import om.Log;
import om.tnavigator.NavigatorConfig;
import util.misc.GeneralUtils;
import util.misc.UtilityException;

/**
 * Provides a visitor based pattern of things that may be needed by the request.
 *  There should only ever be one of these per request and destroyed at the
 *  end of the request.
 * @author Trevor Hinson
 */

public class RequestAssociates {

	private boolean post;

	private String path;

	private ServletContext servletContext;

	private Map<String, Object> configuration;

	private Map<String, Object> principleObjects = new HashMap<String, Object>();

	private Map<String, String> requestParameters = new HashMap<String, String>();

	public Map<String, String> getRequestParameters() {
		return requestParameters;
	}

	public void setRequestParameters(Map<String, String> rp) {
		if (null != rp ? rp.size() > 0 : false) {
			requestParameters.putAll(rp);
		}
	}

	public String getConfig(String key) throws UtilityException {
		Object obj = getConfiguration().get(key);
		return get(String.class, obj, key);
	}

	public String getConfig(Enum<?> enu) throws UtilityException {
		return getConfig(null != enu ? enu.toString() : "");
	}

	public <T> T get(Class<T> cla, String key) throws UtilityException {
		Object obj = getPrincipleObjects().get(key);
		return get(cla, obj, key);
	}

	protected <T> T get(Class<T> cla, Object obj, String key)
		throws UtilityException {
		T o = null;
		if (null != obj) {
			if (cla.isAssignableFrom(obj.getClass())) {
				o = cla.cast(obj);
			}
			if (null == o) {
				throw new UtilityException("There is no "
					+ " object stored in the RequestAssociates for the key : "
					+ key + " and the class : " + cla);
			}
		}
		return o;
	}

	public <T> T get(Class<T> cla, RequestParameterNames rpm)
		throws UtilityException {
		return null != rpm ? get(cla, rpm.toString()) : null;
	}

	public NavigatorConfig getNavigatorConfig()
		throws UtilityException {
		return get(NavigatorConfig.class,
			RequestParameterNames.NavigatorConfig.toString());
	}

	public Map<String, Object> getPrincipleObjects() {
		return principleObjects;
	}

	public void putPrincipleObject(String key, Object value) {
		getPrincipleObjects().put(key, value);
	}

	public RequestAssociates(ServletContext context,
			String sPath, boolean fromAPost, Map<String, Object> config) {
		post = fromAPost;
		path = sPath;
		configuration = config;
		servletContext = context;
	}

	public Log getLog() {
		Log log = null;
		if (null != principleObjects) {
			Object obj = principleObjects.get(
				RequestParameterNames.Log.toString());
			if (null != obj ? obj instanceof Log : false) {
				log = (Log) obj;
			}
		}
		return log;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public String getPath() {
		return path;
	}

	public boolean getPost() {
		return post;
	}

	public Map<String, Object> getConfiguration() {
		if (null == configuration) {
			configuration = new HashMap<String, Object>();
		}
		return configuration;
	}

	public void addConfiguration(String key, String value) {
		if (null != key ? key.length() > 0 : false) {
			getConfiguration().put(key, value);
		}
	}

	public boolean valid() {
		boolean valid = false;
		if (null != getPath() ? getPath().length() > 0 : false) {
			valid = null != getServletContext();
		}
		return valid;
	}

	public String toString() {
		String s = GeneralUtils.toString(this);
		return null != s ? s : super.toString();
	}

}
