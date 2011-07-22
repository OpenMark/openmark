package om;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import util.xml.XML;

/**
 * From the provided requestHandlers.xml configuration File this class
 *  interprets it and then holds reference to the settings within the composite
 *  configurationSettings collection so that these can be examined at runtime. 
 * @author Trevor Hinson
 */
public class RequestHandlingConfiguration {

	private static String REQUEST_HANDLERS = "requestHandlers";

	private static String REQUEST_HANDLER = "requestHandler";

	private static String FULL_CLASS_NAME = "fullClassName";

	private static String INVOCATION_PATH = "invocationPath";

	private Map<String, RequestHandlerSettings> configurationSettings
		= new HashMap<String, RequestHandlerSettings>();

	/**
	 * Constructs the object based on the configuration file provided.
	 * @param f
	 * @throws IOException
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	public RequestHandlingConfiguration(File f)
		throws IOException, RequestHandlingException {
		if (null != f) {
			Document doc = XML.parse(f);
			if (null != doc) {
				List<RequestHandlerSettings> settings = null;
				NodeList nl = doc.getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					if (null != n ? Node.ELEMENT_NODE == n.getNodeType() : false) {
						Element e = (Element) n;
						if (REQUEST_HANDLERS.equals(e.getNodeName())) {
							settings = unpackRequestHandlers(e);
						}
					}
				}
				applyRequestHandlerSettings(settings);
			} else {
				throw new RequestHandlingException("Unable to setup as the "
					+ getClass().getName() + " as the Document object was null.");
			}
		}
	}

	/**
	 * Provides immutable access to the composite RequestHandlerSettings
	 *  collection for interogation.
	 * @return
	 * @author Trevor Hinson
	 */
	public Map<String, RequestHandlerSettings> getSettings() {
		return Collections.unmodifiableMap(configurationSettings);
	}

	/**
	 * Provides access to the relevant RequestHandlerSettings objects for the
	 *  provided invocationPath key argument.
	 * @param key
	 * @return
	 * @author Trevor Hinson
	 */
	public RequestHandlerSettings getRequestHandlerSettings(String key) {
		return configurationSettings.get(key);
	}

	/**
	 * Applies the RequestHandlerSettings List to the composite
	 *  configurationSettings collection.
	 * @param rhs
	 * @author Trevor Hinson
	 */
	protected void applyRequestHandlerSettings(List<RequestHandlerSettings> rhs)
		throws RequestHandlingException {
		if (null != rhs ? rhs.size() > 0 : false) {
			for (RequestHandlerSettings settings : rhs) {
				if (null != settings ? settings.valid() : false) {
					configurationSettings.put(
						settings.getInvocationPath(), settings);
				}
			}
		}
	}

	/**
	 * Iterates over each of the REQUEST_HANDLERS from the configuration and 
	 *  delegates to the parseRequestHandler(Element e) method for processing.
	 * @param e
	 * @return
	 * @exception
	 * @author Trevor Hinson
	 */
	protected List<RequestHandlerSettings> unpackRequestHandlers(Element e)
		throws RequestHandlingException {
		List<RequestHandlerSettings> settings = new ArrayList<RequestHandlerSettings>();
		if (null != e) {
			NodeList nl = e.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (null != n ? Node.ELEMENT_NODE == n.getNodeType() : false) {
					RequestHandlerSettings rhs = parseRequestHandler((Element) n);
					if (null != rhs) {
						settings.add(rhs);
					}
				}
			}
		}
		return settings;
	}

	/**
	 * Caters for the processing of each REQUEST_HANDLER from the configuration
	 *  file and returns a RequestHandlerSettings object representation.
	 * @param ele
	 * @return
	 * @exception 
	 * @author Trevor Hinson
	 */
	protected RequestHandlerSettings parseRequestHandler(Element ele)
		throws RequestHandlingException {
		RequestHandlerSettings settings = null;
		if (null != ele ? REQUEST_HANDLER.equals(ele.getNodeName()) : false) {
			NodeList rhNodeList = ele.getChildNodes();
			if (null != rhNodeList ? rhNodeList.getLength() > 0 : false) {
				String invocationPath = null;
				String fullClassName = null;
				Map<String, String> config = new HashMap<String, String>();
				for (int t = 0 ; t < rhNodeList.getLength() ; t++) {
					Node tn = rhNodeList.item(t);
					if (null != tn ? Node.ELEMENT_NODE == tn.getNodeType() : false) {
						Element el = (Element) tn;
						if (INVOCATION_PATH.equals(el.getNodeName())) {
							invocationPath = el.getTextContent();
						} else if (FULL_CLASS_NAME.equals(el.getNodeName())) {
							fullClassName = el.getTextContent();
						}
						config.put(el.getNodeName(), el.getTextContent());
					}
				}
				if (StringUtils.isNotEmpty(fullClassName)
					&& StringUtils.isNotEmpty(invocationPath)) {
					Class<RequestHandler> rh = retrieveRequestHandlerClass(fullClassName);
					settings = new RequestHandlerSettings(invocationPath, rh, config);
				}
			}
		}
		return settings;
	}

	/**
	 * Dynamically loads the class based on the full className argument.
	 * @param className Must contain the full name i.e.: "om.RequestHandlerImpl"
	 * @return
	 * @throws ServletException
	 * @author Trevor Hinson
	 */
	protected Class<RequestHandler> retrieveRequestHandlerClass(String className)
		throws RequestHandlingException {
		Class<RequestHandler> rh = null;
		if (StringUtils.isNotEmpty(className)) {
			try {
				Class<?> cla = getClass().getClassLoader().loadClass(className);
				if (RequestHandler.class.isAssignableFrom(cla)) {
					rh = getRequestHandlerClass(cla);
				} else {
					throw new RequestHandlingException("The configured class name : "
						+ className + " is not assignable from : "
						+ RequestHandler.class.getName());
				}
			} catch (ClassNotFoundException x) {
				throw new RequestHandlingException(x);
			}
		} else {
			throw new RequestHandlingException("Unable to retrieve the" +
				" RequestHandler as the name was null.");
		}
		return rh;
	}

	/**
	 * Ensures that the Class that we have loaded is essentially a RequestHandler
	 * @param <T>
	 * @param cla
	 * @return
	 * @throws ClassNotFoundException
	 * @author Trevor Hinson
	 */
	@SuppressWarnings("unchecked")
	protected <T> Class<T> getRequestHandlerClass(Class<?> cla)
		throws ClassNotFoundException {
		Class<T> rh = null;
		if (null != cla) {
			rh = (Class<T>) cla.asSubclass(RequestHandler.class);
		}
		return rh;
	}
}
