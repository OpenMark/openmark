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
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.OmException;
import om.OmUnexpectedException;
import om.tnavigator.NavigatorServlet;

/**
 * Base class for reports that are basically a table of data
 */
public abstract class TabularReportBase implements OmReport {
	NavigatorServlet ns;
	
	/**
	 * @param ns 
	 */
	public TabularReportBase(NavigatorServlet ns) {
		this.ns = ns;
	}
	
	private static enum Format {
		/** HTML output class */
		html(HtmlReportWriter.class),
		/** CSV output class */
		csv(CsvReportWriter.class),
		/** TSV output class */
		tsv(TsvReportWriter.class),
		/** Moodle XML output class */
		xml(XmlReportWriter.class);
		
		private final Class<? extends TabularReportWriter> writerClass;
		Format(Class<? extends TabularReportWriter> writerClass)
		{
			this.writerClass = writerClass;
		}
		
		/**
		 * @param pw a print writer
		 * @param columns a list of ColumnDefinitions.
		 * @return an instance of the specific type of TabularReportWriter, initialised with ph and columns. 
		 */
		public TabularReportWriter makeInstance(PrintWriter pw, List<ColumnDefinition>columns) {
			try
			{
				return writerClass.getConstructor(PrintWriter.class, List.class).
						newInstance(pw, columns);
			}
			catch (InstantiationException e)
			{
				throw new OmUnexpectedException(e);
			}
			catch (IllegalAccessException e)
			{
				throw new OmUnexpectedException(e);
			}
			catch (InvocationTargetException e)
			{
				throw new OmUnexpectedException(e);
			}
			catch (NoSuchMethodException e)
			{
				throw new OmUnexpectedException(e);
			}
		}
	};
	
	protected class ColumnDefinition {
		/** The internal name of this column, used, for example, as a tag name when writing to XML, or a CSS class name. */
		public final String id;
		/** The public name of this column, used, as the content for &lt;th> when writing to HTML. */
		public final String name;
		/**
		 * @param id
		 * @param name
		 */
		public ColumnDefinition(final String id, final String name) {
			this.id = id;
			this.name = name;
		}
	}
	
	protected String batchid;

	private TabularReportWriter setupWriter(HttpServletRequest request,
			HttpServletResponse response, List<ColumnDefinition> columns) throws OmException {
		String format = request.getParameter("format");
		if (format == null)
		{
			format = Format.html.toString();
		}
		try {
			return Format.valueOf(format).makeInstance(response.getWriter(), columns);
		}
		catch (IllegalArgumentException e)
		{
			throw new OmException("Unknown report format", e);
		}
		catch (IOException e) {
			throw new OmUnexpectedException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmReport#handle(om.tnavigator.NavigatorServlet, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void handleReport(String suffix, HttpServletRequest request, 
			HttpServletResponse response) throws OmException {
		List<ColumnDefinition> columns = init(request);
		TabularReportWriter reportWriter = setupWriter(request, response, columns);
		reportWriter.sendHeaders(response, batchid);
		reportWriter.printHead(batchid);
		generateReport(reportWriter);
		reportWriter.printTail();
	}

	/**
	 * Initialise this instance of this report. Including checking access, and initialising
	 * <code>columns</code>.
	 * @param tn The test navigator servlet that we got this request from.
	 * @param request The request being responded to. It is recommended that you only use
	 * 	the values obtainable from getParameterMap. The URL will have been consumed in getting this
	 *  far. Autentication and cookies should be checked using methods of NavigatorServlet. 
	 * @return A list of column definitions.
	 */
	public abstract List<ColumnDefinition> init(HttpServletRequest request);

	/**
	 * Generate the report by calling reportWriter.writeRow repeatedly.
	 * @param reportWriter the place to send output.
	 */
	public abstract void generateReport(TabularReportWriter reportWriter);
}