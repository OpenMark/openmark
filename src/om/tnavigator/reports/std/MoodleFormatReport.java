/**
 *
 */
package om.tnavigator.reports.std;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import om.OmException;
import om.OmUnexpectedException;
import om.tnavigator.NavigatorServlet;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.reports.OmReport;
import om.tnavigator.reports.OmTestReport;
import om.tnavigator.reports.TabularReportBase;
import om.tnavigator.reports.TabularReportWriter;
import om.tnavigator.scores.CombinedScore;
import om.tnavigator.sessions.UserSession;
import om.tnavigator.teststructure.CachingQuestionMetadataSource;
import om.tnavigator.teststructure.QuestionMetadataSource;
import om.tnavigator.teststructure.TestDefinition;
import om.tnavigator.teststructure.TestDeployment;
import om.tnavigator.teststructure.TestRealisation;
import util.xml.XML;
import util.xml.XMLException;

/**
 * This report exports test scores in the format expected by the Moodle &gt;=1.9 gradebook.
 */
public class MoodleFormatReport implements OmTestReport, OmReport {
	private NavigatorServlet ns;

	/**
	 * Create an instance of this report.
	 * @param ns the navigator servlet we belong to.
	 */
	public MoodleFormatReport(NavigatorServlet ns) {
		this.ns = ns;
	}

	@Override
	public String getUrlTestReportName() {
		return "moodle";
	}

	@Override
	public String getReadableReportName() {
		return "Results for import into the Moodle gradebook";
	}

	@Override
	public boolean isApplicable(TestDeployment td) {
		return true;
	}

	@Override
	public String getUrlReportName() {
		return "moodle";
	}

	private class MoodleTabularReport extends TabularReportBase {
		private String testId;
		private TestDefinition def;
		private boolean includeAnonymous;

		MoodleTabularReport(String testId, TestDeployment deploy, boolean includeAnonymous) throws OmException {
			this.testId = testId;
			this.def = deploy.getTestDefinition();
			this.ns = MoodleFormatReport.this.ns;
			this.includeAnonymous = includeAnonymous;
			batchid = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			title = testId + " results for export to Moodle";
		}

		@Override
		public void extraHtmlContent(Element mainElement) {
			super.extraHtmlContent(mainElement);
			try {
				Document d = mainElement.getOwnerDocument();
				Element form = XML.getChild(XML.getChild(mainElement, "form"), "p");

				Element label = d.createElement("label");
				label.setAttribute("for", "anonymous-input");
				XML.setText(label, "Include anonymous users ");

				Element input = d.createElement("input");
				input.setAttribute("type", "checkbox");
				input.setAttribute("value", "1");
				input.setAttribute("name", "anonymous");
				input.setAttribute("id", "anonymous-input");
				if (includeAnonymous)
				{
					input.setAttribute("checked", "checked");
				}

				Element hidden = d.createElement("input");
				hidden.setAttribute("type", "hidden");
				hidden.setAttribute("name", "test");
				hidden.setAttribute("value", testId);

				Node oldFirstChild = form.getFirstChild();
				form.insertBefore(label, oldFirstChild);
				form.insertBefore(input, oldFirstChild);
				form.insertBefore(hidden, oldFirstChild);
				form.insertBefore(d.createTextNode(" "), oldFirstChild);
			} catch (XMLException e) {
				throw new OmUnexpectedException("Cannot find form element.", e);
			}
		}

		/**
		 * Output all the scores for a given user.
		 * @param pi the user id
		 * @param score the scores
		 * @param reportWriter where to send the output.
		 */
		protected void outputScoresForPI(String pi, CombinedScore score, TabularReportWriter reportWriter)
		{
			try
			{
				// Output a row of the report for each axis.
				for (String axis : score.getAxesOrdered()) {
					Map<String, String> row = new HashMap<String, String>();
					row.put("student", pi);
					row.put("assignment", axis != null ? testId + "." + axis : testId);
					row.put("score", score.getScore(axis) + "");
					reportWriter.printRow(row);
				}
			} catch (Exception e) {
				throw new OmUnexpectedException("Error outputting report.", e);
			}
		}

		@Override
		public void generateReport(TabularReportWriter reportWriter) {
			QuestionMetadataSource metadataSource = new CachingQuestionMetadataSource(ns);
			DatabaseAccess.Transaction dat = null;
			try
			{
				dat = ns.getDatabaseAccess().newTransaction();

				// Get all the data for all the attempts by each user. The results
				// are sorted by user, and then attempt number.
				// We will loop through the attempts for each user, and keep the
				// one with the highest score. (So, if there are two with equal
				// score, we take the first.)
				ResultSet rs = ns.getOmQueries().queryScoresForAllAttemptsAtTest(dat, testId, includeAnonymous);

				// Because Java ResultSets are stupid (you cannot rely on isAfterLast) we
				// adopt the convention that the it is closed as soon at rs.next() returns false.
				if (!rs.next()) {
					rs.close();
				}

				String currentPi = "";
				double bestScore = -1;
				CombinedScore allScoresForBest = null;
				while (!rs.isClosed())
				{
					String pi = rs.getString(9);

					// If PI has changed, then .
					if (!"".equals(currentPi) && !pi.equals(currentPi))
					{
						// We need to output the last student if they finished.
						outputScoresForPI(currentPi, allScoresForBest, reportWriter);
						bestScore = -1;
						allScoresForBest = null;
					}
					currentPi = pi;

					// Now we get on with checking this student.
					int ti = rs.getInt(8);
					long randomSeed = rs.getLong(10);
					int fixedVariant = rs.getInt(11);

					// Create TestRealisation
					TestRealisation testRealisation = TestRealisation.realiseTest(
							def, randomSeed, fixedVariant, testId, ti);

					// Use it to get the score.
					CombinedScore score = testRealisation.getScoreFromResultSet(
							new NavigatorServlet.RequestTimings(), rs, metadataSource);

					// If this attempt is better, remember it.
					double thisScore = score.getScoreForFirstAxis();
					if (thisScore > bestScore) {
						bestScore = thisScore;
						allScoresForBest = score;
					}
				}

				if (allScoresForBest != null) {
					outputScoresForPI(currentPi, allScoresForBest, reportWriter);
				}

			} catch (Exception e) {
				throw new OmUnexpectedException("Error generating report.", e);
			}
			finally
			{
				if (dat != null)
				{
					dat.finish();
				}
			}
		}

		@Override
		public List<ColumnDefinition> init(HttpServletRequest request) {
			List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
			columns.add(new ColumnDefinition("student", "Student"));
			columns.add(new ColumnDefinition("assignment", "Assignment"));
			columns.add(new ColumnDefinition("score", "Score"));
			return columns;
		}
	}

	@Override
	public void handleTestReport(UserSession us, String suffix,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean includeAnonymous = "1".equals(request.getParameter("anonymous"));
		MoodleTabularReport report = new MoodleTabularReport(us.getTestId(), us.getTestDeployment(), includeAnonymous);
		report.handleReport(request, response);
	}

	@Override
	public void handleReport(String suffix, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String testId = request.getParameter("test");
		TestDeployment deploy = new TestDeployment(ns.pathForTestDeployment(testId));
		boolean includeAnonymous = "1".equals(request.getParameter("anonymous"));

		MoodleTabularReport report = new MoodleTabularReport(testId, deploy, includeAnonymous);
		report.handleReport(request, response);
	}

	@Override
	public boolean isSecurityRestricted() {
		return true;
	}
}
