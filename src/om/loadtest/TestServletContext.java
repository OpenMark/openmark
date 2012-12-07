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
import java.util.Set;

import javax.servlet.*;

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
	public Set<?> getResourcePaths(String arg0)
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
	public Enumeration<?> getServlets()
	{
		return null;
	}
	public Enumeration<?> getServletNames()
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
	public Enumeration<?> getInitParameterNames()
	{
		return null;
	}
	public Object getAttribute(String arg0)
	{
		return null;
	}
	public Enumeration<?> getAttributeNames()
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
}