/* OpenMark online assessment system
   Copyright (C) 2007 The Open University

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package om.loadtest;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.*;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.descriptor.JspConfigDescriptor;

/**
 * Implementation of ServletContext used for performance testing.
 */
public class TestServletContext implements ServletContext
{
	private File fContextPath;
	/**
	 * Constructor.
	 * @param fContextPath
	 */
	public TestServletContext(File fContextPath)
	{
		this.fContextPath=fContextPath;
	}
	public ServletContext getContext(String arg0)
	{
		return null;
	}
	public String getContextPath() {
		return null;
	}
	public int getMajorVersion()
	{
		return 0;
	}
	public int getMinorVersion()
	{
		return 0;
	}
	public String getMimeType(String arg0)
	{
		return null;
	}
	public Set<String> getResourcePaths(String arg0)
	{
		return null;
	}
	public URL getResource(String arg0) throws MalformedURLException
	{
		return null;
	}
	public InputStream getResourceAsStream(String arg0)
	{
		return null;
	}
	public RequestDispatcher getRequestDispatcher(String arg0)
	{
		return null;
	}
	public RequestDispatcher getNamedDispatcher(String arg0)
	{
		return null;
	}
	public Servlet getServlet(String arg0) throws ServletException
	{
		return null;
	}
	public Enumeration<Servlet> getServlets()
	{
		return null;
	}
	public Enumeration<String> getServletNames()
	{
		return null;
	}
	public void log(String arg0)
	{
	}
	public void log(Exception arg0,String arg1)
	{
	}
	public void log(String arg0,Throwable arg1)
	{
	}
	public String getRealPath(String arg0)
	{
		return fContextPath.getPath()+"/"+arg0;
	}
	public String getServerInfo()
	{
		return null;
	}
	public String getInitParameter(String arg0)
	{
		return null;
	}
	public Enumeration<String> getInitParameterNames()
	{
		return null;
	}
	public Object getAttribute(String arg0)
	{
		return null;
	}
	public Enumeration<String> getAttributeNames()
	{
		return null;
	}
	public void setAttribute(String arg0,Object arg1)
	{
	}
	public void removeAttribute(String arg0)
	{
	}
	public String getServletContextName()
	{
		return null;
	}
	@Override
	public Dynamic addFilter(String arg0, String arg1)
	{
		return null;
	}
	@Override
	public Dynamic addFilter(String arg0, Filter arg1)
	{
		return null;
	}
	@Override
	public Dynamic addFilter(String arg0, Class<? extends Filter> arg1)
	{
		return null;
	}
	@Override
	public void addListener(String arg0)
	{
	}
	@Override
	public <T extends EventListener> void addListener(T arg0)
	{
	}
	@Override
	public void addListener(Class<? extends EventListener> arg0)
	{
	}
	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0, String arg1)
	{
		return null;
	}
	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0, Servlet arg1)
	{
		return null;
	}
	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			Class<? extends Servlet> arg1)
	{
		return null;
	}
	@Override
	public <T extends Filter> T createFilter(Class<T> arg0) throws ServletException
	{
		return null;
	}
	@Override
	public <T extends EventListener> T createListener(Class<T> arg0) throws ServletException
	{
		return null;
	}
	@Override
	public <T extends Servlet> T createServlet(Class<T> arg0) throws ServletException
	{
		return null;
	}
	@Override
	public void declareRoles(String... arg0)
	{
	}
	@Override
	public ClassLoader getClassLoader()
	{
		return null;
	}
	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
	{
		return null;
	}
	@Override
	public int getEffectiveMajorVersion()
	{
		return 0;
	}
	@Override
	public int getEffectiveMinorVersion()
	{
		return 0;
	}
	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
	{
		return null;
	}
	@Override
	public FilterRegistration getFilterRegistration(String arg0)
	{
		return null;
	}
	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations()
	{
		return null;
	}
	@Override
	public JspConfigDescriptor getJspConfigDescriptor()
	{
		return null;
	}
	@Override
	public ServletRegistration getServletRegistration(String arg0)
	{
		return null;
	}
	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations()
	{
		return null;
	}
	@Override
	public SessionCookieConfig getSessionCookieConfig()
	{
		return null;
	}
	@Override
	public boolean setInitParameter(String arg0, String arg1)
	{
		return false;
	}
	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> arg0)
			throws IllegalStateException, IllegalArgumentException
	{
	}
}
