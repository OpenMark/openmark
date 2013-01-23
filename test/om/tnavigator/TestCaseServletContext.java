package om.tnavigator;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class TestCaseServletContext implements ServletContext {

	private Map<String, Object> attributes = new HashMap<String, Object>();

	@Override
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return null;
	}

	@Override
	public ServletContext getContext(String arg0) {
		return null;
	}

	@Override
	public String getContextPath() {
		return null;
	}

	@Override
	public String getInitParameter(String arg0) {
		return null;
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return null;
	}

	@Override
	public int getMajorVersion() {
		return 0;
	}

	@Override
	public String getMimeType(String arg0) {
		return null;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		return null;
	}

	@Override
	public String getRealPath(String arg0) {
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		return null;
	}

	@Override
	public Set<String> getResourcePaths(String arg0) {
		return null;
	}

	@Override
	public String getServerInfo() {
		return null;
	}

	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		return null;
	}

	@Override
	public String getServletContextName() {
		return null;
	}

	@Override
	public Enumeration<String> getServletNames() {
		return null;
	}

	@Override
	public Enumeration<Servlet> getServlets() {
		return null;
	}

	@Override
	public void log(String arg0) {

	}

	@Override
	public void log(Exception arg0, String arg1) {

	}

	@Override
	public void log(String arg0, Throwable arg1) {

	}

	@Override
	public void removeAttribute(String arg0) {

	}

	@Override
	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}

}
