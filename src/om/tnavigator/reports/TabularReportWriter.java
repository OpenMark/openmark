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
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import om.tnavigator.reports.TabularReportBase.ColumnDefinition;

/**
 * Class to write out a TabularReport in a certain format.
 */
public abstract class TabularReportWriter {
	protected final PrintWriter pw;
	protected final List<TabularReportBase.ColumnDefinition>columns;
	/**
	 * Create an instance of this writer for writing the given report to the given HTTP Servlet
	 * @param pw the print writer we will be writing to.
	 * @param columns a list of column definitions.
	 */
	public TabularReportWriter(PrintWriter pw,List<ColumnDefinition>columns)
	{
		this.pw = pw;
		this.columns = columns;
	}

	/**
	 * Send the HTTP headers for this report format.
	 * @param response the response to set the headers on.
	 * @param batchid The Id of this batch.
	 */
	public abstract void sendHeaders(HttpServletResponse response, String batchid);

	/**
	 * Write out any content that goes before the body of the report.
	 * @param batchid The Id of this batch.
	 * @param title TODO
	 * @param report The report that will be output.
	 */
	public abstract void printHead(String batchid, String title, TabularReportBase report);

	/**
	 * Write out a row of the report.
	 * @param data a map of columnid -> value, where the column ids need to match the keys of the
	 *  report.getColumns() map.
	 */
	public abstract void printRow(Map<String, String> data);

	/**
	 * Write out any content that goes after the body of the report.
	 */
	public abstract void printTail();
}
