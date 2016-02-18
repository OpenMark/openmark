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

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#getUrlTestReportName()
	 */
	public String getUrlTestReportName() {
		return "moodle";
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#getReadableReportName()
	 */
	public String getReadableReportName() {
		return "Results for import into the Moodle gradebook";
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#isApplicable(om.tnavigator.TestDeployment)
	 */
	public boolean isApplicable(TestDeployment td) {
		return true;
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmReport#getUrlReportName()
	 */
	public String getUrlReportName() {
		return "moodle";
	}

	private class MoodleTabularReport extends TabularReportBase {
		private String testId;
		private TestDefinition def;

		MoodleTabularReport(String testId, TestDeployment deploy) throws OmException {
			this.testId = testId;
			this.def = deploy.getTestDefinition();
			this.ns = MoodleFormatReport.this.ns;
			batchid = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			title = testId + " results for export to Moodle";
		}

		private void outputScoresForPI(AttemptForPI bestAttempt,TabularReportWriter reportWriter)
		{
			String assignmentid=bestAttempt.getAssignmentid();
			CombinedScore score=bestAttempt.getScore();
			String pi=bestAttempt.getPI();
			try
			{
				// Output a row of the report for each axis.
				for (String axis : score.getAxesOrdered()) {
					Map<String, String> row = new HashMap<String, String>();
					row.put("student", pi);
					row.put("assignment", axis != null ? assignmentid + "." + axis : assignmentid);
					row.put("score", score.getScore(axis) + "");
					reportWriter.printRow(row);
				}
			} catch (Exception e) {
				throw new OmUnexpectedException("Error outputting report.", e);
			}
		}

		/* (non-Javadoc)
		 * @see om.tnavigator.reports.TabularReportBase#generateReport(om.tnavigator.reports.TabularReportWriter)
		 */
		@Override
		public void generateReport(TabularReportWriter reportWriter) {
			QuestionMetadataSource metadataSource = new CachingQuestionMetadataSource(ns);
			DatabaseAccess.Transaction dat = null;
			try
			{
				dat = ns.getDatabaseAccess().newTransaction();

				// Get list of people who did the test.
				// Show:
				// * Each person only once
				// * Only the most recent finished attempt, or most recent unfinished attempt
				//   if there aren't any finished
				// * Within categories (finished/unfinished), sorting by PI
				// I achieve this by setting the sort order and dropping all but the first
				// result for each PI.
				ResultSet rs = ns.getOmQueries().queryTestAttemptersByPIandFinishedASC(dat, testId);

				String lastpi = "";
				AttemptForPI bestAttempt = new AttemptForPI("", 0, null, "", false);
				while(rs.next())
				{
					String pi = rs.getString(2);

					// First we deal with the previous student and output it if necessary.
					if (!"".equals(lastpi) && !"".equals(pi) && !pi.equals(lastpi))
					{
						// We need to output the last student if they finished.
						if (bestAttempt.hasFinished())
						{
							outputScoresForPI(bestAttempt, reportWriter);
						}

						bestAttempt = new AttemptForPI("", 0, null, "", false);
					}

					// Now we get on with checking this student.
					int ti = rs.getInt(9);
					int isFinished = rs.getInt(4);
					long randomSeed = rs.getLong(7);
					int fixedVariant = rs.getInt(8);

					// Create TestRealisation
					TestRealisation testRealisation = TestRealisation.realiseTest(
							def, randomSeed, fixedVariant, testId, ti);

					// Use it to get the score.
					String assignmentid = testRealisation.getTestId();
					CombinedScore score = testRealisation.getScore(new NavigatorServlet.RequestTimings(),
							metadataSource, ns.getDatabaseAccess(), ns.getOmQueries());

					// Get the name of the first axis, which is the default.
					String defaultAxis=null;
					for (String axis : score.getAxesOrdered()) {
						defaultAxis = axis;
						break;
					}

					double thisscore = score.getScore(defaultAxis);

					// Set values for the current attempt then compare with th best attempt so far.
					AttemptForPI ThisAttempt = new AttemptForPI(pi, thisscore, score, assignmentid, isFinished == 1);
					bestAttempt.SetIfGreater(ThisAttempt);
					lastpi = pi;
				}

				if (bestAttempt.hasFinished())
				{
					outputScoresForPI(bestAttempt,reportWriter);
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

		/* (non-Javadoc)
		 * @see om.tnavigator.reports.TabularReportBase#init(javax.servlet.http.HttpServletRequest)
		 */
		@Override
		public List<ColumnDefinition> init(HttpServletRequest request) {
			List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
			columns.add(new ColumnDefinition("student", "Student"));
			columns.add(new ColumnDefinition("assignment", "Assignment"));
			columns.add(new ColumnDefinition("score", "Score"));
			return columns;
		}
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#handleTestReport(om.tnavigator.UserSession, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void handleTestReport(UserSession us, String suffix,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		MoodleTabularReport report = new MoodleTabularReport(us.getTestId(), us.getTestDeployment());
		report.handleReport(request, response);
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmReport#handleReport(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void handleReport(String suffix, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String testId = request.getParameter("test");
		TestDeployment deploy = new TestDeployment(ns.pathForTestDeployment(testId));

		MoodleTabularReport report = new MoodleTabularReport(testId, deploy);
		report.handleReport(request, response);
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmReport#isSecurityRestricted()
	 */
	@Override
	public boolean isSecurityRestricted() {
		return true;
	}
}
