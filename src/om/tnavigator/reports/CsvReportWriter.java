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

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import om.tnavigator.NavigatorServlet;

/**
 * A report writer for writing reports as CSV.
 */
public class CsvReportWriter extends TextReportWriter {
	/**
	 * Create a report writer for writing this report out as CSV.
	 * @param pw the PrintWriter to write to.
	 * @param columns a list of column definitions.
	 * @param ns the navigator servlet
	 */
	public CsvReportWriter(PrintWriter pw,List<TabularReportBase.ColumnDefinition>columns, NavigatorServlet ns)
	{
		super(pw,columns, ",", ns);
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.TabularReportWriter#sendHeaders(javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void sendHeaders(HttpServletResponse response, String batchid)
	{
		response.setContentType("text/csv; charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=report-"+
				batchid+".csv");
	}
}
