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

import om.tnavigator.NavigatorServlet;

/**
 * @author tjh238
 *
 */
public abstract class TextReportWriter extends TabularReportWriter
{
	private final String rowSeparator;
	private final String cellSeparator;
	private final String cellWrapperOpen;
	private final String cellWrapperClose;
	private boolean rowStarted=false;

	/**
	 * @param pw the print writer we will be writing to.
	 * @param columns a list of column definitions.
	 * @param rowSeparator sting to output between rows.
	 * @param cellSeparator sting to output between data cells.
	 * @param cellWrapperOpen sting to output before each cell.
	 * @param cellWrapperClose sting to output after each cell.
	 * @param ns the navigator servlet
	 */
	public TextReportWriter(PrintWriter pw,List<TabularReportBase.ColumnDefinition>columns,
			final String rowSeparator,final String cellSeparator,
			final String cellWrapperOpen,final String cellWrapperClose, NavigatorServlet ns)
	{
		super(pw,columns, ns);
		this.rowSeparator=rowSeparator;
		this.cellSeparator=cellSeparator;
		this.cellWrapperOpen=cellWrapperOpen;
		this.cellWrapperClose=cellWrapperClose;
	}

	/**
	 * @param pw the print writer we will be writing to.
	 * @param columns a list of column definitions.
	 * @param cellSeparator sting to output between data cells.
	 * @param ns the navigator servlet
	 */
	public TextReportWriter(PrintWriter pw,List<TabularReportBase.ColumnDefinition>columns,
			final String cellSeparator, NavigatorServlet ns)
	{
		this(pw,columns,"\r\n",cellSeparator,"\"","\"", ns);
	}

	private void endLine()
	{
		pw.print(rowSeparator);
		rowStarted=false;
	}

	private void printCell(String value)
	{
		if (rowStarted)
		{
			pw.print(cellSeparator);
		}
		pw.print(cellWrapperOpen);
		pw.print(value);
		pw.print(cellWrapperClose);
		rowStarted=true;
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.TabularReportWriter#sendHead()
	 */
	@Override
	public void printHead(String batchid, String title, TabularReportBase report)
	{
		for (TabularReportBase.ColumnDefinition column : columns)
		{
			printCell(column.name);
		}
		endLine();
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.TabularReportWriter#sendRow(java.util.Map)
	 */
	@Override
	public void printRow(Map<String, String> data)
	{
		for (TabularReportBase.ColumnDefinition column : columns)
		{
			printCell(data.get(column.id));
		}
		endLine();
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.TabularReportWriter#sendTail()
	 */
	@Override
	public void printTail()
	{
	}
}
