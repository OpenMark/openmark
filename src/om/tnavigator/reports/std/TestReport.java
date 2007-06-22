/**
 * 
 */
package om.tnavigator.reports.std;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import om.tnavigator.NavigatorServlet;
import om.tnavigator.reports.TabularReportBase;
import om.tnavigator.reports.TabularReportWriter;

/**
 * @author tjh238
 *
 */
public class TestReport extends TabularReportBase {
	/**
	 * Create an instance of this report.
	 * @param ns the navigator servlet we belong to.
	 */
	public TestReport(NavigatorServlet ns) {
		super(ns);
	}
	
	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmReport#getUrlReportName()
	 */
	public String getUrlReportName() {
		return "fortesting";
	}
	
	/* (non-Javadoc)
	 * @see om.tnavigator.reports.TabularReportBase#init(om.tnavigator.NavigatorServlet, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public List<ColumnDefinition> init(HttpServletRequest request) {
		batchid = "1234";
		List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
		columns.add(new ColumnDefinition("student", "Student"));
		columns.add(new ColumnDefinition("assignment", "Assignment"));
		columns.add(new ColumnDefinition("score", "Score"));
		return columns;
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.TabularReportBase#generateReport(om.tnavigator.reports.TabularReportWriter)
	 */
	@Override
	public void generateReport(TabularReportWriter reportWriter) {
		Map<String, String> data = new HashMap<String, String>();
		for (int i = 0; i < 10; ++i) {
			data.put("student", "X000000" + i);
			data.put("assignment", "CMA41");
			data.put("score", ""+(100-i));
			reportWriter.printRow(data);
		}
	}
}
