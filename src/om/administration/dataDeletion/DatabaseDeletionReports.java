package om.administration.dataDeletion;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.DisplayUtils;
import om.Log;
import om.abstractservlet.AbstractRequestHandler;
import om.abstractservlet.RenderedOutput;
import om.abstractservlet.RequestAssociates;
import om.abstractservlet.RequestHandlerEnums;
import om.abstractservlet.RequestHandlingException;
import om.abstractservlet.RequestParameterNames;
import om.abstractservlet.RequestResponse;
import om.administration.databaseCleaner.ExtractorException;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.db.DatabaseAccess;
import util.misc.Strings;


public class DatabaseDeletionReports extends AbstractRequestHandler {

	private static final long serialVersionUID = 3432094601409857406L;

	private String filteredUrl;

	private String postToUrl;
	
	private boolean doDBUpdate=false;
	private static boolean DONTDOAXIS=true;
		
	//private static String QUESTIONLISTSQL="SELECT x.qi,question,clock,finished,x.score" +
	//		" FROM nav_questions y, nav_scores x" +
	//		"   where finished > 0 and y.qi=x.qi" +
	//		"  order by question";
	
	//private static String QUESTIONLISTSQL="SELECT 	t2.question,t2.max_time,t2.total,t2.timesused,t1.totalscore,t1.timesused,t1.timedatagleaned	" +
	//		"FROM 	(select * from nav_questionstats) as  t1 	" +
	//		"right join 	" +
	//		"(SELECT  question, max(clock) as max_time, cast(sum(x.score) as decimal(10,2))as total,count(*) as timesused" +
	//		"	FROM nav_questions y, nav_scores x	 where finished > 0 and y.qi=x.qi group by question	 ) as t2" +
	//		" on t1.question=t2.question";
	
	
	private static String QUESTIONLISTSQL=
"select "+
		"a_question,"+ 
 		"s_axis, "+
 		"total_score, "+
 		"times_used, "+
 		"start_time, "+
 		"finish_time,  "+
 		"timefirstused ,"+
 		"timesused ,"+
 		"totalscore , "+
 		"timedatagleaned "+
 "from nav_questionstats "+
 "right join "+
 "("+
		"select "+
				"a_question, "+
 				"s_axis, "+
 				"sum(s_score) as total_score, "+
 				"min(s_starttime) as start_time, "+
 				"max(a_finishtime) as finish_time,"+
 				"count(*) as times_used "+
		 "from nav_questionstats "+
	 	"right join "+
	 	"( "+
	 	"	select "+ 
	 	"		qname as a_question, " +
	 	"		scores.axis as s_axis,"+ 		
		"		scores.score as s_score, "+
		"		axisdata.starttime as s_starttime, "+
		"		axisdata.finishtime as a_finishtime	"+
	 	"	 from nav_scores as scores "+ 
		"			join "+
		"		( "+
		"			select max(x.question) as qname, x.qi as qi,min(x.clock) as starttime,max(y.clock) as finishtime   "+
		"			from  nav_questions x, nav_actions  y "+
		"			where x.qi=y.qi and x.finished > 0 " +
		"			group by x.qi "+
		"		)  "+
		"		as axisdata "+ 
		"		on axisdata.qi=scores.qi " +
		") "+
		"as newstats "+
		"on a_question=question and s_axis=axis  "+
		"where timedatagleaned is null or a_finishtime >timedatagleaned  "+
		"group by a_question,s_axis  "+
") "+
"as stats "+
"on a_question=question and s_axis=axis " +
"order by a_question " ;
	
	
	public static String DATEFORMATFORDB="yyyyMMdd HH:mm:ss";
	
	/*dummy date until we read it from the database*/
	private static Date DATELASTRUN;
	
	private boolean dataGenerated=false;
	
	private Log l;
	
	
	public RequestResponse handleAll(HttpServletRequest request,
		HttpServletResponse response, RequestAssociates associates,boolean doUpdate)
		throws RequestHandlingException {
		RequestResponse rr = new RenderedOutput();
		
		setDoDBUpdate(doUpdate);
		if (null != request && null != response && null != associates) {
			initialise(associates);
			String uri = request.getPathInfo();
			if (Strings.isNotEmpty(filteredUrl)
				? filteredUrl.equals(uri) : false) {
				getLog().logDebug("Running the deletion Reports ...");
				

				StringBuilder output = new StringBuilder(DisplayUtils.header());
				try {
					StringBuilder er = process(request, associates);
					if (null != er ) {
						output.append("<h1>Student Data deletion Reports - new data</h1><br />");
						output.append(withOrWithoutUpdate(this.doDBUpdate));
												
						output.append(er);

					}

				} catch (Exception x) {
					output.append("<h1>An Error Occured with the deletion Reports </h1><br /><br />");
					output.append(handleException(x.getMessage()));
				}
				output.append(DisplayUtils.footer());
				rr.append(output.toString());
				getLog().logDebug("Finished now rendering back to the user : ");
			}
		}
		return rr;
	}
	
	private String withOrWithoutUpdate(boolean doUpd)
	{
		StringBuilder output = new StringBuilder();
		output.append("<h2>Running with");
		if (!doUpd)
		{
			output.append("out");
		}
		output.append(" Database update</h2>");
		return output.toString();
	}


	/**
	 * Simply wraps the exception thrown from the DataDeletionException so 
	 *  to present it back to the user.
	 * @param s
	 * @return
	 * @author Trevor Hinson
	 */
	private String handleException(String s) {
		return new StringBuilder(DisplayUtils.header())
			.append("<br /><br />").append("Error : ").append(s)
			.append(DisplayUtils.footer()).toString();
	}

	/**
	 * get all the questions in the database, then read the scores and total up average, inally generate a report
	 * @param request
	 * @param associates
	 * @return
	 * @throws DataDeletionException
	 * @author Sarah Wood
	 */
	private StringBuilder process(HttpServletRequest request,
		RequestAssociates associates) throws DataDeletionException {
		StringBuilder output = new StringBuilder();
		try
		{
//			if(! dataGenerated)
//			{
				NavigatorConfig nc=pickupNavigatorConfig(associates);
				/* note this doesnt actually do much yet */
				Map<String, Object> metaData = new HashMap<String, Object>();
				metaData=generateMetaData(request, associates);
				output.append("<h1>Question Scores Report</h1>");
	
				Date FromDate=getReportFromDate();
				/* read all the questions */
				List<QuestionAverageData> QuestionScores=getQuestionsFromDB(FromDate,nc);
				
				/* if the list is empty and we havent errored, then we've found no new data */
				if (QuestionScores.isEmpty())
				{
					output.append("<p>No new data found</p> ");
				}
				else
				{
					/* gebnerate the score data */
					//Map<QuestionAverageData,String> QuestionScores=generateQuestionScoreData(QuestionsFromDB);
					/* update the date report generated */
					/* create the output */
					dataGenerated=true;				
					/* update the database with the new totals */
					if(updateTheDB())
					{
						updateDBWithTotals(QuestionScores,nc);
					}
					output.append(generateQuestionScoreDataReportAsText(QuestionScores));
				}

		//	}
			/* output it */
		}
		catch (Exception x)
		{
			throw new DataDeletionException(x);
		}	
		return output;
			
	}
	
	/* get the fdate the report was last run. This will be stored in the datasbe but for now we'll use the epoch */
	
	private Date getReportFromDate()
	{
		return DATELASTRUN;
	}
	
	/* get the sql strings. For now static vars, but may make it more customisable later */
	
	private String getSQLString(String whichOne)throws DataDeletionException
	{
		String sqlstr="";
		if (whichOne.equals("question list"))sqlstr=QUESTIONLISTSQL;
	//	if (whichOne.equals("new total"))sqlstr=NEWTOTALSCORESSQL;
		//if (whichOne.equals("update total"))sqlstr=UPDATETOTALSCORESSQL;

		/* put the database prefix into the sql */
		if (sqlstr.isEmpty())
		{
			throw new  DataDeletionException("Unable to determine sqlstring");
		}

		return sqlstr;
	}

	
	
	/* generate the list of questions needed to look at from the database */
	private List<QuestionAverageData> getQuestionsFromDB(Date fromdate,NavigatorConfig nc) throws DataDeletionException 
	{
		List<QuestionAverageData> qdbdList=new LinkedList<QuestionAverageData>();
		DatabaseAccess.Transaction datS = null;
		DatabaseAccess da=null;

		try
		{
				String sqlstr = getSQLString("question list");
				da=DatabaseDeletionUtils.getDatabaseConnection(nc);
				datS = da.newTransaction();
				ResultSet rs=datS.query(sqlstr);
				/* iterate over the questions found and store in the list */
				while (rs.next()) 
				{
		 		
			 		/* this is fresh question data for wquestions finished since the lkast data report (glean) */
					String name=rs.getString(1);
					String axisName=rs.getString(2);
					double totscore=rs.getDouble(3);		
					int timesused=rs.getInt(4);
					Date tfu=rs.getDate(5);
					Date TimeLastUsed=rs.getDate(6);
					/* this is the data on question intsances we ahave already gleaned and stored */
					Date DBTimeFirstUsed=convertToDate(rs.getTimestamp(7));
					int DBTotalTimesUsed=rs.getInt(8);
					double DBTotalScore=rs.getDouble(9);	
				
					Date DBTimeDataGleaned =convertToDate(rs.getTimestamp(10));
					QuestionAverageData qdbd=new QuestionAverageData(
							name,TimeLastUsed,totscore,timesused,tfu,axisName,
							DBTotalTimesUsed,DBTotalScore,DBTimeFirstUsed,DBTimeDataGleaned);

						/* put an entry in the log file for this object */
						qdbd.logIt(l);
						qdbdList.add(qdbd);
 				}						
		}
		catch (Exception x)
		{
			throw new DataDeletionException(x);
		}
		finally 
		{
			if (null != datS) {
				datS.finish();
			}
			da.close();
		}
		return qdbdList;
	}
	
	private Date convertToDate(Timestamp ts)
	{
		Date thisDate =new Date();
		if (ts != null)
		{
			thisDate=new Date (ts.getTime());
		}
		else 
		{
			thisDate=null;
		}
		return thisDate;
		
	}
	
	
	private StringBuilder  generateQuestionScoreDataReportAsText(List<QuestionAverageData> questionScores) throws DataDeletionException
	{
		StringBuilder report=new StringBuilder();
		boolean noAxis=DONTDOAXIS;
		report.append("<table border='0' align='left' valign='top'>");
		report.append(QuestionAverageData.rptHeadingsAsHTML(noAxis));
		try
		{
			/* we output it by question not axis, so ad them all up 
			 * and output them */
			boolean first=true;
			QuestionAverageData QADbyQuestion= new QuestionAverageData();
			  for(Iterator itr = questionScores.iterator(); itr.hasNext();) 
			  {
				  //QuestionAverageData qdata = new QuestionAverageData();
				 QuestionAverageData qdata = (QuestionAverageData) itr.next();
				 /* if we want it by axis, then just output it, otherwise we only put out the blank axis*/
				 if (!noAxis || (noAxis && qdata.isBlankAxis()))
				 {
					  report.append(qdata.rptAsHTML(noAxis));
				 }

			  }
		}
		catch (Exception x)
		{
			throw new DataDeletionException(x);
		}
		report.append("</table>");

		return report;
	}
	
	/* run through the data generated and update the database
	 * add the totals, and insert new entriews for those that have not been updated before */
	private void  updateDBWithTotals(List<QuestionAverageData> questionScores,NavigatorConfig nc) throws DataDeletionException
	{
		DatabaseAccess.Transaction datU = null;
		DatabaseAccess da=null;
        DateFormat df = new SimpleDateFormat(DATEFORMATFORDB);
        Date now = new Date();
        String TimeLastUsedAsString = df.format(now);
		boolean noAxis=DONTDOAXIS;

		try
		{
			da=DatabaseDeletionUtils.getDatabaseConnection(nc);
			datU = da.newTransaction();

			  for(Iterator itr = questionScores.iterator(); itr.hasNext();) 
			  {
				  //QuestionAverageData qdata = new QuestionAverageData();
				  QuestionAverageData qdata = (QuestionAverageData) itr.next();
					/* update the database with the new totals */
					 if (!noAxis || (noAxis && qdata.isBlankAxis()))
					{
						 qdata.updateDBWithTotalsData(datU,nc,TimeLastUsedAsString);
					}
			  }

		}
		catch (Exception x)
		{
		throw new DataDeletionException(x);
		}
		finally 
		{
		if (null != datU) {
			datU.finish();
		}
		da.close();
		}

	}
	


	/**
	 * Picks up the NavigatorConfig that is required in order to run the
	 *  extraction process.
	 * @param ra
	 * @return
	 * @throws IOException
	 * @author Trevor Hinson
	 */
	private NavigatorConfig pickupNavigatorConfig(RequestAssociates ra)
		throws IOException {
		NavigatorConfig navigatorConfig = null;
		if (null != ra) {
			Object o = ra.getPrincipleObjects().get(
				RequestParameterNames.NavigatorConfig.toString());
			if (null != o ? o instanceof NavigatorConfig : false) {
				navigatorConfig = (NavigatorConfig) o;
			}
		}
		return navigatorConfig;
	}

	public void initialise(RequestAssociates associates)
	throws RequestHandlingException {
	super.initialise(associates);
	Object o = associates.getConfiguration().get(
		RequestHandlerEnums.invocationPath.toString());
	if (null != o ? o instanceof String : false) {
		filteredUrl = (String) o;
	}
	l=getLog();
	
}

	/**
	 * Returns a generated List<String> based on what is configured within the
	 *  web.xml for this filter implementation.
	 * @param s
	 * @return
	 * @author Trevor Hinson
	 */
	List<String> convert(String s) {
		List<String> lst = null;
		if (Strings.isNotEmpty(s)) {
			String[] bits = s.split(",");
			if (null != bits) {
				for (int i = 0; i < bits.length; i++) {
					String bit = bits[i];
					if (Strings.isNotEmpty(bit)) {
						if (null == lst) {
							lst = new ArrayList<String>();
						}
						lst.add(bit.trim());
					}
				}
			}
		}
		return lst;
	}
	
	/**
	 * Builds the metaData map from the various parameters needed to handle
	 *  the request.
	 * @param request
	 * @return
	 * @throws ExtractorException
	 * @author Trevor Hinson
	 */
	private Map<String, Object> generateMetaData(HttpServletRequest request,
		RequestAssociates associates) throws DataDeletionException 
		{
		/* doesnt actually do much for now, just a load of nulls, will fill it in later */
		Map<String, Object> metaData = new HashMap<String, Object>();
		try {
			metaData.put(DatabaseDeletionEnums.DatabaseDeletionUrl.toString(), filteredUrl);
			metaData.put(DatabaseDeletionEnums.postToUrl.toString(), postToUrl);
			metaData.put(DatabaseDeletionEnums.navigatorConfigKey.toString(),
				pickupNavigatorConfig(associates));
			metaData.put(RequestParameterNames.logPath.toString(), getLogPath());

		} catch (IOException x) {
			throw new DataDeletionException(x);
		}
		return metaData;
	}
	
	public void setDoDBUpdate(boolean d)
	{
		doDBUpdate=d;
	}
	
	public boolean updateTheDB()
	{
		return doDBUpdate ;
	}
	
	
	public void  destroy()
	{
		//da.close();

	}
}
