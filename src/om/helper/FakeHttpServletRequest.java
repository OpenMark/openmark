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
package om.helper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class FakeHttpServletRequest extends HttpServletRequestWrapper {
	private Map<String, String> params;

	public FakeHttpServletRequest(HttpServletRequest request, Map<String, String> params) {
		super(request);
		this.params = params;
	}

	@Override
	public String getParameter(String name) {
		return params.get(name);
	}

	@Override
	public java.util.Map<String, String[]> getParameterMap() {
		Map<String, String[]> result = new HashMap<String, String[]>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			result.put(entry.getKey(), new String[] {entry.getValue()});
		}
		return result;
	}

	@Override
	public java.util.Enumeration<String> getParameterNames() {
		return Collections.enumeration(params.keySet());
	}

	@Override
	public String[] getParameterValues(String name) {
		return new String[] {params.get(name)};
	}
}
