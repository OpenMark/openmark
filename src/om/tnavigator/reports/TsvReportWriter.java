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

/**
 * A report writer for writing reports as a tab-separated text file.
 */
public class TsvReportWriter extends TextReportWriter {
	/**
	 * Create a report writer for writing this report out as a tab-separated text file.
	 * @param pw the PrintWriter to write to.
	 * @param columns a list of column definitions.
	 */
	public TsvReportWriter(PrintWriter pw,List<TabularReportBase.ColumnDefinition>columns)
	{
		super(pw,columns,"\r\n", "\t", "", "");
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.TabularReportWriter#sendHeaders(javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void sendHeaders(HttpServletResponse response,String batchid)
	{
		response.setContentType("text/plain; charset=UTF-8");
		response.setHeader("Content-Disposition","attachment; filename=report-"+
				batchid+".txt");
	}
}
