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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import om.OmUnexpectedException;
import om.tnavigator.NavigatorServlet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.xml.*;

/**
 * A report writer for writing reports as HTML.
 */
public class HtmlReportWriter extends TabularReportWriter
{
	/** To make the HTML report render the "frog" column as a hyperlink, add a 
	 * "frog_link" key to the data hash. */
	public static final String LINK_SUFFIX = "_link";
	private int row = 0;
	private Document template;
	private Element tableBody;

	/**
	 * @param pw the print writer we will be writing to.
	 * @param columns a list of column definitions.
	 * @param ns the navigator servlet
	 */
	public HtmlReportWriter(PrintWriter pw,List<TabularReportBase.ColumnDefinition>columns, NavigatorServlet ns)
	{
		super(pw,columns, ns);
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.TabularReportWriter#sendHead()
	 */
	@Override
	public void printHead(String batchid, String title, TabularReportBase report)
	{
		Element mainElement;
		try {
			template = ns.getTemplate(false, false, true);
			Map<String,Object> replacements = new HashMap<String,Object>();
			replacements.put("TITLEBAR", title);
			replacements.put("ACCESS", "");
			replacements.put("TESTTITLE",title);
			replacements.put("TITLE","");
			replacements.put("AUXTITLE", batchid != null ? "batch number: " + batchid : ".");
			XML.find(template,"title","%%TOOLTIP%%").removeAttribute("title");
			XML.replaceTokens(template,replacements);

			XML.remove(XML.find(template,"id","progressinfo"));
			XML.removeChildren(XML.find(template,"id","buttons"));
			mainElement = XML.find(template,"id","main");
			XML.removeChildren(mainElement);
			XML.getChild(template.getDocumentElement(),"body").setAttribute("class","progressleft");
		} catch (XMLException e) {
			throw new OmUnexpectedException("Could not load the template.", e);
		}

		report.extraHtmlContent(mainElement);

		Element table = XML.createChild(mainElement, "table");
		mainElement.setAttribute("class", "basicpage");
		table.setAttribute("class", "topheaders");
		Element tableHead = XML.createChild(table, "thead");
		tableBody = XML.createChild(table, "tbody");
		for (TabularReportBase.ColumnDefinition column : columns)
		{
			Element header = XML.createChild(tableHead, "th");
			header.setAttribute("scope", "col");
			header.setAttribute("class" , column.id);
			XML.setText(header, column.name);
		}
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
		Element tableRow = XML.createChild(tableBody, "tr"); 
		row++;
		if ("yes".equals(data.get("error"))) {
			tableRow.setAttribute("class", "error");
		} else {
			tableRow.setAttribute("class", row%2 == 0 ? "even" : "odd");
		}	
		for (TabularReportBase.ColumnDefinition column : columns)
		{
			Element cell = XML.createChild(tableRow, "td");
			cell.setAttribute("class" , column.id);
			if (data.containsKey(column.id + LINK_SUFFIX)) {
				Element link = XML.createChild(cell, "a");
				link.setAttribute("href", data.get(column.id + LINK_SUFFIX));
				XML.setText(link, data.get(column.id));
			} else {
				XML.setText(cell, data.get(column.id));
			}
		}
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.TabularReportWriter#sendTail()
	 */
	@Override
	public void printTail()
	{
		try {
			XHTML.saveFullDocument(template, pw, false, "en");
		} catch (IOException e) {
			throw new OmUnexpectedException("Could not output the report.", e);
		}
	}
}
