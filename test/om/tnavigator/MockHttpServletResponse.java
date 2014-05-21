/* OpenMark online assessment system
 * Copyright (C) 2007 The Open University
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package om.tnavigator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class MockHttpServletResponse implements HttpServletResponse
{
	private String redirectUrl = null;
	private Map<String, Cookie> cookies = new HashMap<String, Cookie>();

	public String getRedirectUrl()
	{
		return redirectUrl;
	}

	public String getCookieValue(String name)
	{
		return cookies.get(name).getValue();
	}

	public int getNumCookies() {
		return cookies.size();
	}

	@Override
	public void flushBuffer() throws IOException
	{
	}

	@Override
	public int getBufferSize()
	{
		return 0;
	}

	@Override
	public String getCharacterEncoding()
	{
		return null;
	}

	@Override
	public String getContentType()
	{
		return null;
	}

	@Override
	public Locale getLocale()
	{
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException
	{
		return null;
	}

	@Override
	public PrintWriter getWriter() throws IOException
	{
		return null;
	}

	@Override
	public boolean isCommitted()
	{
		return false;
	}

	@Override
	public void reset()
	{
	}

	@Override
	public void resetBuffer()
	{
	}

	@Override
	public void setBufferSize(int arg0)
	{
	}

	@Override
	public void setCharacterEncoding(String arg0)
	{
	}

	@Override
	public void setContentLength(int arg0)
	{
	}

	@Override
	public void setContentType(String arg0)
	{
	}

	@Override
	public void setLocale(Locale arg0)
	{
	}

	@Override
	public void addCookie(Cookie cookie)
	{
		cookies.put(cookie.getName(), cookie);
	}

	@Override
	public void addDateHeader(String arg0, long arg1)
	{
	}

	@Override
	public void addHeader(String arg0, String arg1)
	{
	}

	@Override
	public void addIntHeader(String arg0, int arg1)
	{
	}

	@Override
	public boolean containsHeader(String arg0)
	{
		return false;
	}

	@Override
	public String encodeRedirectURL(String arg0)
	{
		return null;
	}

	@Override
	public String encodeRedirectUrl(String arg0)
	{
		return null;
	}

	@Override
	public String encodeURL(String arg0)
	{
		return null;
	}

	@Override
	public String encodeUrl(String arg0)
	{
		return null;
	}

	@Override
	public void sendError(int arg0) throws IOException
	{
	}

	@Override
	public void sendError(int arg0, String arg1) throws IOException
	{
	}

	@Override
	public void sendRedirect(String url) throws IOException
	{
		redirectUrl = url;
	}

	@Override
	public void setDateHeader(String arg0, long arg1)
	{
	}

	@Override
	public void setHeader(String arg0, String arg1)
	{
	}

	@Override
	public void setIntHeader(String arg0, int arg1)
	{
	}

	@Override
	public void setStatus(int arg0)
	{
	}

	@Override
	public void setStatus(int arg0, String arg1)
	{
	}
}
