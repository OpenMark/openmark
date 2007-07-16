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

import util.xml.XHTML;

/**
 * A report writer for writing reports as HTML.
 */
public class HtmlReportWriter extends TabularReportWriter
{
	private int row = 0;
	
	/**
	 * @param pw the print writer we will be writing to.
	 * @param columns a list of column definitions.
	 */
	public HtmlReportWriter(PrintWriter pw,List<TabularReportBase.ColumnDefinition>columns)
	{
		super(pw,columns);
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.TabularReportWriter#sendHead()
	 */
	@Override
	public void printHead(String batchid, String title, TabularReportBase report)
	{
		pw.println("<html>");
		pw.println("\t<head>");
		pw.println("\t\t<title>" + title + " batch number: " + batchid + "</title>");
		pw.println("\t</head>");
		pw.println("\t<body>");
		pw.println("\t\t<h1>" + title + " batch number: " + batchid + "</h1>");
		report.extraHtmlContent(pw);
		pw.println("\t\t<table>");
		pw.println("\t\t\t<thead>");
		pw.println("\t\t\t\t<tr>");
		for (TabularReportBase.ColumnDefinition column : columns)
		{
			pw.println("\t\t\t\t\t<th scope=\"col\" class=\"" + XHTML.escape(column.id, XHTML.ESCAPE_ATTRDQ) + "\">" +
					XHTML.escape(column.name, XHTML.ESCAPE_TEXT) + "</th>");
		}
		pw.println("\t\t\t\t</tr>");
		pw.println("\t\t\t</thead>");
		pw.println("\t\t\t<tbody>");
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.TabularReportWriter#sendHeaders(javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void sendHeaders(HttpServletResponse response, String batchid)
	{
		response.setContentType("text/html;charset=UTF-8");
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.TabularReportWriter#sendRow(java.util.Map)
	 */
	@Override
	public void printRow(Map<String, String> data)
	{
		row++;
		String tag = "th scope=\"row\"";
		pw.println("\t\t\t\t<tr class=\"" + (row%2 == 0 ? "even" : "odd") + "\">");
		for (TabularReportBase.ColumnDefinition column : columns)
		{
			pw.println("\t\t\t\t\t<" + tag + " scope=\"col\" class=\"" + XHTML.escape(column.id, XHTML.ESCAPE_ATTRDQ) + "\">" +
					XHTML.escape(data.get(column.id), XHTML.ESCAPE_TEXT) + "</" + tag + ">");
			tag = "td";
		}
		pw.println("\t\t\t\t</tr>");
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.TabularReportWriter#sendTail()
	 */
	@Override
	public void printTail()
	{
		pw.println("\t\t\t</tbody>");
		pw.println("\t\t</table>");
		pw.println("\t</body>");
		pw.println("</html>");
	}
}
