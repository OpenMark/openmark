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
package util.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides a method for analysing browser user agent strings.
 */
public class UserAgent
{
	private static final Pattern IEVERSION=Pattern.compile(
		"^.*MSIE ([0-9]+)\\.([0-9]+)[^0-9].*Windows.*$");
	private static final Pattern GECKOVERSION=Pattern.compile(
		"^.*rv:([0-9]+)\\.([0-9]+)[^0-9].*$");


	/**
	 * @param request the request to analyse the user-agent header of.
	 * @return A string identifying the browser that made the request,
	 *   of the form gecko-1-8 or ie-5[space]ie-5-5, or
	 *   empty string if unknown
	 */
	public static String getBrowserString(HttpServletRequest request)
	{
		String sAgent=request.getHeader("user-agent");
		// Filter troublemakers
		if(sAgent.indexOf("KHTML")!=-1)
			return "khtml";
		if(sAgent.indexOf("Opera")!=-1)
			return "opera";

		// Check version of our two supported browsers
		Matcher mGecko=GECKOVERSION.matcher(sAgent);
		if(mGecko.matches())
		{
			return "gecko-"+mGecko.group(1)+"-"+mGecko.group(2);
		}
		Matcher mIE=IEVERSION.matcher(sAgent);
		if(mIE.matches())
		{
			return "winie-"+mIE.group(1); // Major verison only
		}

		return "";
	}
}
