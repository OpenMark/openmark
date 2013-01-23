/**
 *
 */
package om.tnavigator.reports.std;

import java.sql.ResultSet;
import java.sql.SQLException;
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
import om.tnavigator.UserSession;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.reports.OmReport;
import om.tnavigator.reports.OmTestReport;
import om.tnavigator.reports.TabularReportBase;
import om.tnavigator.reports.TabularReportWriter;
import om.tnavigator.scores.CombinedScore;
import om.tnavigator.teststructure.TestDefinition;
import om.tnavigator.teststructure.TestDeployment;
import om.tnavigator.teststructure.TestRealisation;
import om.tnavigator.auth.SAMSOucuPi;
import util.misc.GeneralUtils;


/**
 * This report exports test scores in the format expected by the Moodle &gt;=1.9 gradebook.
 */
public class MoodleFormatReport implements OmTestReport, OmReport {
	private NavigatorServlet ns;
	private static boolean USE_DB_PI_LOOKUP = false;


	/**
	 * Create an instance of this report.
	 * @param ns the navigator servlet we belong to.
	 */
	public MoodleFormatReport(NavigatorServlet ns) {
		this.ns = ns;

	}
	
	/* read through the oucu pi table and create a hashmap */
	private  Map<String,String> generateOucuPiMap(NavigatorServlet ns) throws OmUnexpectedException
	{
		Map<String, String> mPIs = new HashMap<String, String>();
		DatabaseAccess.Transaction dat;
		try {
			dat = ns.getDatabaseAccess().newTransaction();
		} catch (SQLException e1) {
			throw new OmUnexpectedException("Cannot connect to the database");
		}
		try
		{
			ResultSet rs = ns.getOmQueries().queryPiFromOucu(dat);
			while(rs.next())
			{
				String sOUCU=rs.getString(1);
				String sPI=rs.getString(2);
				mPIs.put(sOUCU,sPI);
			}		
		} 
		catch (Exception e) 
		{
			throw new OmUnexpectedException("Error generating list of oucu pis.", e);
		}
		finally
		{
			dat.finish();
		}
		return mPIs;
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

		
		String assignmentid=bestAttempt.getassignmentid();
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
	

	 
	 private String getPiFromOucu(Map<String,String> mPIs,String oucu,String dpi)
	 {
		 String thisPi=dpi;
		 /* so if oucu is not null or empty , and its the same as the pi, we may have a problem so look it up */
		  if (GeneralUtils.isOUCUPIequalButNotTemp(oucu,dpi) )
		 {
			 if (USE_DB_PI_LOOKUP)
			 {
			 /* if we have a non empty oucu , and it matches the pi we have a promblem so look it up */
				 thisPi=mPIs.get(oucu);
			 }
			 else
			 {
				 /* use the webservice */
				 SAMSOucuPi op=new SAMSOucuPi(oucu,dpi,ns.getNavigatorConfig(),ns.getLog());
				 thisPi=op.getPi();
				 
			 }
		 }
		 /* if we found something then return it, otherwise just return what we had in the first place */
		 if(!(thisPi.isEmpty()) || thisPi==null)
		 {
			 return thisPi;
		 }
		 else
		 {
			 return dpi;
		 }
	 }
	 
		/* (non-Javadoc)
		 * @see om.tnavigator.reports.TabularReportBase#generateReport(om.tnavigator.reports.TabularReportWriter)
		 */
		@Override
		public void generateReport(TabularReportWriter reportWriter) {
			// Query from database for PIs and questions
			/* get a map of oucus and pis */
			// Store PIs. Map of String (oucu) -> String (pi)
			Map<String,String> mPIs=new HashMap<String,String>();
			/* IF we are using the databse then we need to read the oucus pis into a hash map */
			if (USE_DB_PI_LOOKUP)
			{
				try
				{		
					mPIs=generateOucuPiMap(ns);
	
				}
				catch(OmUnexpectedException e)
				{
					ns.getLog().logDebug("Problem generating list of oucu pis "+e);
	
				}
			}
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
				
				ResultSet rs = ns.getOmQueries().queryTestAttemptersByPIandFinishedASC(dat, testId);
				//create empty lists
				boolean startflag=true;
				String lastpi="";
				AttemptForPI BestAttempt = new AttemptForPI("",0,null,"",false);

				while(rs.next())
				{
					String pi = rs.getString(2);

					//ns.getLog().logDebug("*************** pi " + pi);
					//we dont do admins, and we dont do dummy students which start with a Q
					//comment out as we are now going to export dummy and admin
					//if (isAdmin != 1 && pi != "" && !isDummy) //need the test on pi just to be on safe side
					//{
						
						//first we deal with the previous student and output it if necesary
						if (!pi.equals(lastpi) && !(startflag) && !pi.equals(""))
						{
							// ok, we need to process and output last student if they finished
							// startflag is there so we dont process the first student yet
			
							if(BestAttempt.gethasFinished())
							{
									outputScoresForPI(BestAttempt,reportWriter);	
									ns.getLog().logDebug("Output score for  " +BestAttempt.getPI() +
											" score " + BestAttempt.gettestScore() );
							}
							//reset BestAttempt
							BestAttempt = new AttemptForPI("",0,null,"",false);

						}
						//we checked wether to output this one, so reset the startflag 
					    startflag=false;
						
						// now we get on with checking this student
						int ti = rs.getInt(9);
						int isFinished=rs.getInt(4);
						long randomSeed = rs.getLong(7);
						int fixedVariant = rs.getInt(8);
						
						//not an admin so go with it
						//is this pi the same as the last? if it isnt we need to process it
						//ns.getLog().logDebug("pi *" + pi +
						//		"* lastpi *" + lastpi + "* ti " + ti );

						// Create TestRealisation
						TestRealisation testRealisation = TestRealisation.realiseTest(
								def, randomSeed, fixedVariant, testId, ti);	
						// Use it to get the score.
						String assignmentid = testRealisation.getTestId();
						
						CombinedScore score = testRealisation.getScore(new NavigatorServlet.RequestTimings(), ns, ns.getDatabaseAccess(), ns.getOmQueries());
						//set up out test attempt ready for the list
						// despite the comments in the getscore declaration., using null causes a java exception
						// so we use the get axis ordered and pick of the first one.
						
						String defaultAxis=null;
						for (String axis : score.getAxesOrdered()) {
							//  get the score for the first one, which is the default
							ns.getLog().logDebug("axis " + axis );
							defaultAxis=axis;
							break;
							}

						double thisscore=score.getScore(defaultAxis);					
						ns.getLog().logDebug("this attempt " + pi +
								" score " + thisscore );	
						//set values for the current attempt then compare with th best attempt so far
						AttemptForPI ThisAttempt = new AttemptForPI(pi,thisscore,score,assignmentid,isFinished==1);
		
						BestAttempt.SetIfGreater(ThisAttempt);
						lastpi=pi;
					}
				//}
				// now output the last student as long as they aren't an admin
				if (BestAttempt.gethasFinished())
				{	
					outputScoresForPI(BestAttempt,reportWriter);
					ns.getLog().logDebug("Output score for  " +BestAttempt.getPI() +
							" score " + BestAttempt.gettestScore() );
				}

				//
				
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

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmReport#isSecurityRestricted()
	 */
	@Override
	public boolean isSecurityRestricted() {
		return true;
	}

}
