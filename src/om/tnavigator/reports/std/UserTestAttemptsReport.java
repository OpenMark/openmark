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
import org.w3c.dom.Node;

import om.OmUnexpectedException;
import om.tnavigator.NavigatorServlet;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.reports.HtmlReportWriter;
import om.tnavigator.reports.OmReport;
import om.tnavigator.reports.TabularReportBase;
import om.tnavigator.reports.TabularReportWriter;
import util.xml.XML;
import util.xml.XMLException;

/**
 * This report shows the attempted test for the oucu.
 */
public class UserTestAttemptsReport implements OmReport {

	private NavigatorServlet ns;

	public UserTestAttemptsReport(NavigatorServlet ns) {
		this.ns = ns;
	}

	/*
	 * Get the report url.
	 * @see om.tnavigator.reports.OmReport#getUrlReportName()
	 */
	@Override
	public String getUrlReportName() {
		return "userattempts";
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmReport#handleReport(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handleReport(String suffix, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String prefix = request.getParameter("prefix");
		UserTestTabularReport report = new UserTestTabularReport(prefix);
		report.handleReport(request, response);
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmReport#isSecurityRestricted()
	 */
	@Override
	public boolean isSecurityRestricted() {
		return false;
	}

	private class UserTestTabularReport extends TabularReportBase {
		private final String prefix;

		/**
		 * Constructor.
		 * @param prefix optional prefix. If specified, returns only test attempted by oucu.
		 */
		public UserTestTabularReport(String prefix) {
			this.prefix = prefix;
			batchid = null;
			if (prefix == null ) {
				title = "Test attempt reports";
			} else {
				title = "Test attempt by " + prefix;
			}
			this.ns = UserTestAttemptsReport.this.ns;
		}


		@Override
		public String getReportTagName() {
			return "userattempts";
		}

		/* (non-Javadoc)
		 * @see om.tnavigator.reports.TabularReportBase#init(javax.servlet.http.HttpServletRequest)
		 */
		@Override
		public List<ColumnDefinition> init(HttpServletRequest request) {
			List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
			columns.add(new ColumnDefinition("OUCU", "OUCU"));
			columns.add(new ColumnDefinition("Title", "Title"));
			columns.add(new ColumnDefinition("AttemptedQn", "Attempted questions"));
			columns.add(new ColumnDefinition("PCDC", "Pre-course diagnostic "));
			columns.add(new ColumnDefinition("Startdate", "Start date"));
			columns.add(new ColumnDefinition("FinishedDate", "Finished date"));
			columns.add(new ColumnDefinition("Select", ""));
			return columns;
		}

		/**
		 * Add HTML form with text fields to submit.
		 * @param mainElement the printWriter from the HttpServletResponse.
		 */
		@Override
		public void extraHtmlContent(Element mainElement) {
			super.extraHtmlContent(mainElement);
			try {

				Document d = mainElement.getOwnerDocument();
				Element form = XML.getChild(XML.getChild(mainElement, "form"), "p");
				loadJs(form);
				Element input = d.createElement("input");
				input.setAttribute("type", "text");
				input.setAttribute("size", "10");
				input.setAttribute("name", "prefix");
				input.setAttribute("id", "prefix-input");
				if (prefix != null && prefix.length() > 0) {
					input.setAttribute("value", prefix);
				} else {
					input.setAttribute("placeholder", "OUCU");
				}

				Element deleteTest = XML.createChild(form, "p");
				Element spanText = XML.createChild(deleteTest, "span");
				Element buttonDelete = XML.createChild(spanText, "input");
				buttonDelete.setAttribute("type", "button");
				buttonDelete.setAttribute("name", "delete_test");
				buttonDelete.setAttribute("value", "Delete selected attempts");

				Node oldFirstChild = form.getFirstChild();
				form.insertBefore(input, oldFirstChild);
				form.appendChild(deleteTest);
			} catch (XMLException e) {
				throw new OmUnexpectedException("Cannot find form element.", e);
			}
		}

		/**
		 * Load the required js for checkbox toggle and delete test instance.
		 * @param form which includes js file.
		 */
		private void loadJs(Element form) {
			Element scriptTag = XML.createChild(form, "script");
			scriptTag.setAttribute("src", "../!shared/js/user-test-attempts.js");
			scriptTag.setAttribute("type", "text/javascript");
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
					ResultSet rs = ns.getOmQueries().queryTestDetails(dat, prefix);
					Map<String, String> row = new HashMap<String, String>();
					while (rs.next()) {
						String oucu = rs.getString("oucu");
						String ti = rs.getString("ti");
						String finishedDate = rs.getString("finishedtime");
						finishedDate = (finishedDate != null ? finishedDate : "Not submitted");
						String pcdc = rs.getString("precoursediagcode");
						String title = rs.getString("title");
						String startTime = rs.getString("starttime");
						row.put("OUCU", oucu);
						row.put("OUCU" + HtmlReportWriter.LABEL_SUFFIX, ti);
						row.put("Title", title);
						row.put("Title"+ HtmlReportWriter.LABEL_SUFFIX, ti);
						row.put("PCDC" + HtmlReportWriter.LABEL_SUFFIX, (pcdc !=null ? pcdc: ""));
						row.put("AttemptedQn", "View");
						String params ="?ti=" + ti +"&deploy="+title+"&oucu="+prefix+"&startTime="+startTime+"&endTime="+finishedDate;
						row.put("AttemptedQn" + HtmlReportWriter.LINK_SUFFIX, "../!report/questionsinattempts"+params);
						row.put("Startdate", startTime);
						row.put("FinishedDate", finishedDate);
						row.put("Select"+ HtmlReportWriter.CHECKBOX_SUFFIX, ti);
						reportWriter.printRow(row);
					}
				} catch (SQLException e) {
				throw new OmUnexpectedException("Error generating student report.", e);
			} finally {
				dat.finish();
			}
		}
	}
}
