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
package om.tnavigator.reports;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.tnavigator.UserSession;
import om.tnavigator.teststructure.TestDeployment;

/**
 * Interface for Om report plugins. A new instance of the report class is generated each time the
 * report is run.
 */
public interface OmTestReport {
	/**
	 * Generate the report, sending it to <code>response</code>.
	 *
	 * @param us The user's session on this test.
	 * @param sSuffix The remainder of the URL.
	 * @param request The request being responded to. It is recommended that you only use
	 * 	the values obtainable from getParameterMap. The URL will have been consumed in getting this
	 *  far. Autentication and cookies should be checked using methods of NavigatorServlet.
	 * @param response The response we are sending.
	 * @throws Exception
	 */
	public void handleTestReport(UserSession us,String sSuffix,
			HttpServletRequest request,HttpServletResponse response) throws Exception;

	/**
	 * @return The part of the URL that means the servlet should run this report.
	 */
	public String getUrlTestReportName();

	/**
	 * @return a human-readable name for this test report. Return null if you don't want
	 * this report listed at the bottom of the HomeTestReport.
	 */
	public String getReadableReportName();

	/**
	 * @param td The test deployment.
	 * @return whether this report is applicable to this test.
	 *
	 */
	public boolean isApplicable(TestDeployment td);
}
