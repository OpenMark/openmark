/**
 *
 */
package om.tnavigator.reports.std;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.OmException;
import om.OmUnexpectedException;
import om.tnavigator.*;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.reports.*;
import om.tnavigator.scores.CombinedScore;

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
			batchid = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			title = testId + " results for export to Moodle";
		}

		/* (non-Javadoc)
		 * @see om.tnavigator.reports.TabularReportBase#generateReport(om.tnavigator.reports.TabularReportWriter)
		 */
		@Override
		public void generateReport(TabularReportWriter reportWriter) {
			// Query from database for PIs and questions
			DatabaseAccess.Transaction dat;
			try {
				dat = ns.getDatabaseAccess().newTransaction();
			} catch (SQLException e1) {
				throw new OmUnexpectedException("Cannot connect to the database");
			}
			try
			{
				// Get list of people who did the test.
				// Show:
				// * Each person only once
				// * Only the most recent finished attempt, or most recent unfinished attempt
				//   if there aren't any finished
				// * Within categories (finished/unfinished), sorting by PI
				// I achieve this by setting the sort order and dropping all but the first
				// result for each PI.
				Set<String> pisDone = new HashSet<String>();
				ResultSet rs = ns.getOmQueries().queryTestAttempters(dat, testId);
				while(rs.next())
				{
					int isFinished=rs.getInt(4);
					int isAdmin=rs.getInt(5);

					if (isAdmin != 1 && isFinished == 1)
					{
						// Complete attempt, so get score and send it.
						String pi = rs.getString(2);
						long randomSeed = rs.getLong(7);
						int fixedVariant = rs.getInt(8);
						int ti = rs.getInt(9);
						if (pisDone.contains(pi))
						{
							// We have already processed a more recent attempt by this user.
							continue;
						}

						ns.getLog().logDebug("Adding results for " + pi +
								" to the moodle report. (ti = " + ti + ")");

						// Create TestRealisation
						TestRealisation testRealisation = TestRealisation.realiseTest(
								def, randomSeed, fixedVariant, testId, ti
						);

						// Use it to get the score.
						CombinedScore score = testRealisation.getScore(new NavigatorServlet.RequestTimings(), ns, ns.getDatabaseAccess(), ns.getOmQueries());

						// Output a row of the report for each axis.
						for (String axis : score.getAxes()) {
							Map<String, String> row = new HashMap<String, String>();
							String assignmentid = testRealisation.getTestId();
							if (axis != null)
							{
								assignmentid += "." + axis;
							}
							row.put("student", pi);
							row.put("assignment", assignmentid);
							row.put("score", score.getScore(axis) + "");
							reportWriter.printRow(row);
						}
						pisDone.add(pi);
					}
				}
			} catch (Exception e) {
				throw new OmUnexpectedException("Error generating report.", e);
			}
			finally
			{
				dat.finish();
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

}
