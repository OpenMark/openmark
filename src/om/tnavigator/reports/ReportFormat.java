package om.tnavigator.reports;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import om.OmUnexpectedException;
import om.tnavigator.NavigatorServlet;
import om.tnavigator.reports.TabularReportBase.ColumnDefinition;

enum ReportFormat {
	/** HTML output class */
	html(HtmlReportWriter.class, "HTML"),
	/** CSV output class */
	csv(CsvReportWriter.class, "CSV"),
	/** TSV output class */
	tsv(TsvReportWriter.class, "Tab-separated text"),
	/** Moodle XML output class */
	xml(XmlReportWriter.class, "XML (in browser)"),
	/** Moodle XML output class */
	xmldownload(XmlForDownloadReportWriter.class, "XML (download)");

	private final Class<? extends TabularReportWriter> writerClass;
	private final String niceName;
	ReportFormat(Class<? extends TabularReportWriter> writerClass, String niceName) {
		this.writerClass = writerClass;
		this.niceName = niceName;
	}
	/**
	 * @return a human-readable name for this report.
	 */
	public String getNiceName() {
		return niceName;
	}

	/**
	 * @param pw a print writer
	 * @param columns a list of ColumnDefinitions.
	 * @param ns the navigator servlet.
	 * @return an instance of the specific type of TabularReportWriter, initialised with ph and columns.
	 */
	public TabularReportWriter makeInstance(PrintWriter pw, List<ColumnDefinition>columns, NavigatorServlet ns) {
		try
		{
			return writerClass.getConstructor(PrintWriter.class, List.class, NavigatorServlet.class).
					newInstance(pw, columns, ns);
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
}
