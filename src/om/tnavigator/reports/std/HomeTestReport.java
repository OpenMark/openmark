/**
 *
 */
package om.tnavigator.reports.std;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.axis.qengine.Score;
import om.tnavigator.*;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.reports.OmTestReport;
import om.tnavigator.sessions.UserSession;
import om.tnavigator.teststructure.CachingQuestionMetadataSource;
import om.tnavigator.teststructure.QuestionMetadataSource;
import om.tnavigator.teststructure.TestDeployment;
import util.misc.QuestionName;

/**
 * The main OM report. Lists summaries of various things with links to more detailed reports.
 */
public class HomeTestReport implements OmTestReport {
	private static class DbInfoPerson
	{
		String sOUCU,sPI,sDate,sDateFinished,sTime,sTimeFinished;
	}

	private static class DbInfoQuestion
	{
		String sQuestion,sAverage,sCount;
		int iMajor;
		int iNumber;
	}

	private NavigatorServlet ns;

	/**
	 * Create an instance of this report.
	 * @param ns the navigator servlet we belong to.
	 */
	public HomeTestReport(NavigatorServlet ns)
	{
		this.ns = ns;
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#getUrlTestReportName()
	 */
	public String getUrlTestReportName() {
		return "";
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#getReadableReportName()
	 */
	public String getReadableReportName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#isApplicable(om.tnavigator.TestDeployment)
	 */
	public boolean isApplicable(TestDeployment td) {
		return true;
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#handleTestReport(om.tnavigator.NavigatorServlet, om.tnavigator.UserSession, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void handleTestReport(UserSession us,
			String sSuffix, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		List<DbInfoPerson> lFinished=new LinkedList<DbInfoPerson>();
		List<DbInfoPerson> lUnfinished=new LinkedList<DbInfoPerson>();
		List<DbInfoPerson> lAdmin=new LinkedList<DbInfoPerson>();
		List<DbInfoQuestion> lQuestions=new LinkedList<DbInfoQuestion>();
		QuestionMetadataSource metaDataSource = new CachingQuestionMetadataSource(ns);

		SimpleDateFormat
			sdfD=new SimpleDateFormat("dd MMMM yyyy"),
			sdfT=new SimpleDateFormat("HH:mm:ss");

		// Query from database for PIs and questions
		DatabaseAccess.Transaction dat=ns.getDatabaseAccess().newTransaction();
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
			Set<String> sPIsDone=new HashSet<String>();
			ResultSet rs=ns.getOmQueries().queryTestAttempters(dat,us.getTestId());
			while(rs.next())
			{
				DbInfoPerson dip=new DbInfoPerson();
				dip.sOUCU=rs.getString(1);
				dip.sPI=rs.getString(2);
				if(sPIsDone.contains(dip.sPI))
					continue;
				Timestamp tsStart=rs.getTimestamp(3);
				dip.sTime=sdfT.format(tsStart);
				dip.sDate=sdfD.format(tsStart);
				Timestamp tsFinished=rs.getTimestamp(6);
				if(tsFinished==null)
				{
					dip.sTimeFinished="";
					dip.sDateFinished="";
				}
				else
				{
					dip.sTimeFinished=sdfT.format(tsFinished);
					dip.sDateFinished=sdfD.format(tsFinished);
				}
				int iFinished=rs.getInt(4),iAdmin=rs.getInt(5);
				if(iAdmin==1)
					lAdmin.add(dip);
				else if(iFinished==1)
					lFinished.add(dip);
				else
					lUnfinished.add(dip);
				sPIsDone.add(dip.sPI);
			}

			// Get list of questions in test
			rs=ns.getOmQueries().queryQuestionList(dat,us.getTestId());
			while(rs.next())
			{
				DbInfoQuestion diq=new DbInfoQuestion();
				diq.sQuestion=rs.getString(1);
				if(rs.getInt(3)==0)
				{
					diq.sAverage="-";
				}
				else
				{
					diq.sAverage=Math.round(rs.getInt(2)*100.0 / rs.getInt(3))+"";
					while(diq.sAverage.length()<3) diq.sAverage="0"+diq.sAverage;
					diq.sAverage=diq.sAverage.substring(0,diq.sAverage.length()-2)+"."+
						diq.sAverage.substring(diq.sAverage.length()-2);
				}
				diq.iMajor=rs.getInt(4);
				diq.iNumber=rs.getInt(5);
				diq.sCount=rs.getInt(6)+"";
				lQuestions.add(diq);
			}
		}
		finally
		{
			dat.finish();
		}

		// Build result
		StringBuffer sb=new StringBuffer("<div class='basicpage'>");

		sb.append("<h3>Finished tests ("+lFinished.size()+")</h3>");
		sb.append("<p>Finished tests are those where the user clicked the " +
			"'End test' button to submit their answers.</p>");
		sb.append("<table class='topheaders'><tr><th>PI</th><th>OUCU</th><th>Start date</th><th>Time</th><th>Finish date</th><th>Time</th></tr>");
		for(DbInfoPerson dip : lFinished)
		{
			sb.append("<tr><td><a href='reports!user!"+dip.sPI+"'>"+dip.sPI+"</a></td>" +
				"<td>"+dip.sOUCU+"</td><td>"+dip.sDate+"</td><td>"+dip.sTime+"</td>" +
				"<td>"+dip.sDateFinished+"</td><td>"+dip.sTimeFinished+"</td></tr>");
		}
		sb.append("</table>");

		sb.append("<h3>Unfinished tests ("+lUnfinished.size()+")</h3>");
		sb.append("<table class='topheaders'><tr><th>PI</th><th>OUCU</th><th>Start date</th><th>Time</th></tr>");
		for(DbInfoPerson dip : lUnfinished)
		{
			sb.append("<tr><td><a href='reports!user!"+dip.sPI+"'>"+dip.sPI+"</a></td>" +
				"<td>"+dip.sOUCU+"</td><td>"+dip.sDate+"</td><td>"+dip.sTime+"</td></tr>");
		}
		sb.append("</table>");

		sb.append("<h3>Admin user tests</h3>");
		sb.append("<p>Tests taken by admin users (finished or not) will not be included in any official results.</p>");
		sb.append("<table class='topheaders'><tr><th>PI</th><th>OUCU</th><th>Start date</th><th>Time</th><th>Finish date</th><th>Time</th></tr>");
		for(DbInfoPerson dip : lAdmin)
		{
			sb.append("<tr><td><a href='reports!user!"+dip.sPI+"'>"+dip.sPI+"</a></td>" +
				"<td>"+dip.sOUCU+"</td><td>"+dip.sDate+"</td><td>"+dip.sTime+"</td>" +
				"<td>"+dip.sDateFinished+"</td><td>"+dip.sTimeFinished+"</td></tr>");
		}
		sb.append("</table>");

		sb.append("<h3>Questions in test</h3>");
		sb.append("<p>The following counts include all who attempted a question, " +
			"whether or not they finished the test.</p>");
		sb.append("<table class='topheaders'><tr><th>#</th><th>ID</th><th>Taken by</th><th>Average score</th><th>Out of</th></tr>");
		for(DbInfoQuestion diq : lQuestions)
		{
			Score[] as = metaDataSource.getMaximumScores(null, new QuestionName(diq.sQuestion,
					metaDataSource.getLatestVersion(diq.sQuestion, diq.iMajor)));
			String sMax = "??";
			for(int iScore=0;iScore<as.length;iScore++)
			{
				if(as[iScore].getAxis()==null)
					sMax=""+as[iScore].getMarks();
			}
			sb.append("<tr><td>"+diq.iNumber+"</td><td><a href='reports!question!"+diq.sQuestion+"'>"+diq.sQuestion+"</a>" +
				"</td><td>"+diq.sCount+"</td><td>"+diq.sAverage+"</td><td>"+sMax+"</td></tr>");
		}
		sb.append("</table>");

		SortedMap<String, String> reportsToList = new TreeMap<String, String>();
		for (OmTestReport report : ns.getReports().getTestReports())
		{
			String reportName = report.getReadableReportName();
			if (reportName != null && report.isApplicable(us.getTestDeployment()))
			{
				reportsToList.put(reportName, report.getUrlTestReportName());
			}
		}
		if(!reportsToList.isEmpty())
		{
			sb.append("<h3>Other reports on this test</h3>");
			for (Map.Entry<String, String> entry : reportsToList.entrySet()) {
				sb.append("<p><a href='reports!" + entry.getValue() + "'>" + entry.getKey() + "</a></p>");
			}
		}

		sb.append("<p style='margin-top:2em'><a href='./'>Return to test</a></p>");

		sb.append("</div>");

		ns.serveTestContent(us,"Reports","",null,null,sb.toString(),false, request, response, true);
	}
}
