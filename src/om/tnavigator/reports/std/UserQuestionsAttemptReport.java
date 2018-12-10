package om.tnavigator.reports.std;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import om.OmUnexpectedException;
import om.tnavigator.NavigatorServlet;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.reports.OmReport;
import om.tnavigator.reports.TabularReportBase;
import om.tnavigator.reports.TabularReportWriter;
import util.xml.XML;
import util.xml.XMLException;

/**
 * This report shows the attempted questions for selected test instance
 */
public class UserQuestionsAttemptReport implements OmReport {

	private NavigatorServlet ns;

	public UserQuestionsAttemptReport(NavigatorServlet ns) {
		this.ns = ns;
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#getUrlTestReportName()
	 */
	@Override
	public String getUrlReportName() {
		return "questionsinattempts";
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmReport#handleReport(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handleReport(String suffix, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String ti = request.getParameter("ti");
		String oucu = request.getParameter("oucu");
		String deploy = request.getParameter("deploy");
		String startTime = request.getParameter("startTime");
		String endTime = request.getParameter("endTime");

		UserQuestionTabularReport report = new UserQuestionTabularReport(ti, deploy, oucu, startTime, endTime);
		report.handleReport(request, response);
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmReport#isSecurityRestricted()
	 */
	@Override
	public boolean isSecurityRestricted() {
		return false;
	}

	private class UserQuestionTabularReport extends TabularReportBase {
		private final String ti;
		private final String oucu;
		private final String startTime;
		private final String endTime;

		/**
		 * Constructor.
		 * @param testinstance
		 * @param deploy.
		 * @param oucu
		 * @param startTime of test
		 * @param endTime of test if submitted.
		 */
		public UserQuestionTabularReport(String ti, String deploy, String oucu, String startTime, String endTime) {
			this.ti = ti;
			this.oucu = oucu;
			this.startTime = startTime;
			this.endTime = endTime;
			if (deploy != null) {
				title = "Question for test "+deploy;
			}
			this.ns = UserQuestionsAttemptReport.this.ns;
		}

		/* (non-Javadoc)
		 * @see om.tnavigator.reports.TabularReportBase#init(javax.servlet.http.HttpServletRequest)
		 */
		@Override
		public List<ColumnDefinition> init(HttpServletRequest request) {
			List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
			columns.add(new ColumnDefinition("QnDescription", "Question description"));
			columns.add(new ColumnDefinition("QnLine", "Question line"));
			columns.add(new ColumnDefinition("Attempt", "Attempt"));
			columns.add(new ColumnDefinition("Score", "Score"));
			return columns;
		}

		/**
		 * Add html form with text fields to submit.
		 * @param mainElement the printWriter from the HttpServletResponse.
		 */
		@Override
		public void extraHtmlContent(Element mainElement) {
			super.extraHtmlContent(mainElement);
			try {
				Document d = mainElement.getOwnerDocument();
				Element form = XML.getChild(XML.getChild(mainElement, "form"), "p");

				Element div = d.createElement("div");
				div.setAttribute("class", "reportinfo");
				Element div1 = XML.createChild(div, "div");
				div1.setTextContent("Test attempted by "+ this.oucu);
				Element div2 = XML.createChild(div, "div");
				div2.setTextContent("Start date: "+ this.startTime);
				Element div3 = XML.createChild(div, "div");
				div3.setTextContent("Submited: "+ this.endTime);

				Element backLink = XML.createChild(form, "a");
				backLink.setAttribute("href", "../!report/userattempts?prefix="+oucu);
				backLink.setAttribute("value", "Back to test");
				backLink.setAttribute("style", "padding-left:10px");
				backLink.setTextContent("Back to test attempt");
				form.appendChild(backLink);

				form.appendChild(div);
			} catch (XMLException e) {
				throw new OmUnexpectedException("Cannot find form element.", e);
			}
		}

		/**
		 * Generate a report querying DB.
		 * @param reportwriter
		 */
		@Override
		public void generateReport(TabularReportWriter reportWriter) {
			DatabaseAccess.Transaction dat;
			try {
				dat = ns.getDatabaseAccess().newTransaction();
			} catch (SQLException e) {
				throw new OmUnexpectedException("Error connecting to the database.", e);
			}
			try {
				ResultSet rs = ns.getOmQueries().queryQuestionsDetails(dat, ti);
				Map<String, String> row = new HashMap<String, String>();
				while (rs.next()) {
					String qnLine = rs.getString("questionline");
					String attempt = rs.getString("attempt");
					String score = rs.getString("score");
					row.put("QnDescription", rs.getString("questions"));
					row.put("QnLine", (qnLine !=null ? qnLine :""));
					row.put("Attempt", (attempt != null ? attempt : ""));
					row.put("Score", (score != null ? score : ""));
					reportWriter.printRow(row);
				}
				} catch (SQLException e) {
				throw new OmUnexpectedException("Error generating report.", e);
			} finally {
				dat.finish();
			}
		}
	}
}
