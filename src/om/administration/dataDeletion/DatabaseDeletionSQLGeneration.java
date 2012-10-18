package om.administration.dataDeletion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.AbstractRequestHandler;
import om.DisplayUtils;
import om.Log;
import om.OmException;
import om.OmUnexpectedException;
import om.RenderedOutput;
import om.RequestAssociates;
import om.RequestHandlerEnums;
import om.RequestHandlingException;
import om.RequestParameterNames;
import om.RequestResponse;
import om.administration.databaseCleaner.ExtractorException;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.reports.std.DeployedTestsReport.Test;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import util.misc.GeneralUtils;

public class DatabaseDeletionSQLGeneration extends AbstractRequestHandler {

	private String filteredUrl;

	private String postToUrl;
	private String displayName;
	
	public static String DATEFORMATFORDB="yyyyMMdd HH:mm:ss";
	

	private String TestBanks;
    private static int BATCHAMOUNT=10;
	private Integer batchNumber=BATCHAMOUNT;
	private Boolean doDebug=false;
	private Boolean tableByTable=false;
	private static Integer  MAXBATCH=100;
	private String[] tiTables;
	private String[] qiTables;

	
    private Calendar dateDeleteBeforeIsAssessed=Calendar.getInstance();
    private Calendar dateDeleteBeforeNotAssessed=Calendar.getInstance();
	private String sDateDeleteBeforeIsAssessed;
	private String sDateDeleteBeforeNotAssessed;
	private String sqlFileName="SQLDATA";
	private static String HTML_PREFIX=".htm";
	
    private NavigatorConfig nc;	
	private String SQLIDString="";
	
	private static int ISASSESSEDYEARS=-6;
	private static int NOTASSESSEDYEARS=-2;
	private final static Pattern filenamePattern = Pattern.compile("^(.*)\\.deploy\\.xml$");
	private final static DateFormat IDdateFormat = new SimpleDateFormat("HHmmss");
	private final static boolean 	HTML_IT=true;

	private final static String TESTBANK="C:\\Program Files\\apache-tomcat-6.0.33\\webapps\\om-tn\\testbank";
	private static String WHERESTRING1="t.deploy=\'%s\' and t.finishedclock < '%s'";
	private static String WHERESTRING2="ti=\'%s\'";
	private static String WHERESTRING3="qi=\'%s\'";




	private static String TESTSTABLE="tests";
    public static String SQL1="sql1";
    public static String SQL2="sql2";
    public static String SQL3="sql3";
    public static String SQL4="sql4";
    public static String SQL5="sql5";

    
	/* create a stamp for the og file so sql statmenets can easily be extracted as a group */


	public RequestResponse handleAll(HttpServletRequest request,
			HttpServletResponse response, RequestAssociates associates,boolean findQuestions)
			throws RequestHandlingException {
		
		
		RequestResponse rr = new RenderedOutput();
		if (null != request && null != response && null != associates) {
			
			//initialise();
			
			initialise(associates);

			
			String uri = request.getPathInfo();
			if (StringUtils.isNotEmpty(filteredUrl)
				? filteredUrl.equals(uri) : false) {
				getLog().logDebug("Running the Sql Generation ...");
				StringBuilder output = new StringBuilder(DisplayUtils.header());
				
				output.append("<h1>Student Data deletion SQL Generation ( "+SQLIDString+" )</h1>");	
				if (doDebug)
				{
					output.append("<h1>ruunning in DEBUG mode</h1>");	
				}

				output.append("<h1>Deleting assessed tests before "+sDateDeleteBeforeIsAssessed+
						" and non-assessed tests before "+sDateDeleteBeforeNotAssessed+"</h1>");	
				
				try {
					String er = process(request, associates,findQuestions);
					if (null != er ) {
						output.append(er);			
					}

				} catch (Exception x) {
					output.append("<h1>An Error Occured with the SQL Generation </h1><br /><br />");
					output.append(handleException(x.getMessage()));
				}
				output.append(DisplayUtils.footer());
				rr.append(output.toString());
				/*write it to a file*/

				//writeToFile(sqlFileName + HTML_PREFIX, output);

				getLog().logDebug("Finished now rendering back to the user : ");
			}
		}
		return rr;
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

	private String doHeadings(boolean findQuestions)
	{
		StringBuilder output = new StringBuilder();
		if(!displayName.isEmpty())
		{
				output.append("<h1>"+displayName+"</h1>");
		}		
		output.append("<h2>");
		if(findQuestions)
		{
			output.append("Finding all relevant question instances");
		}
		else
		{
			
			output.append(" Legal minimum only");
		}
		output.append("</h2>");

		output.append("<h2>Processing in batches of "+batchNumber+" </h2>");
		return output.toString();
	}
	
	/**
	 * Checks for a existence of the "studentTi" within the request and
	 *  delegates to the relevant request handler accordingly.
	 * @param request
	 * @param associates
	 * @return
	 * @throws ExtractorException
	 * @author sarah wood
	 */
	private String process(HttpServletRequest request,
		RequestAssociates associates,boolean findQuestions) throws DataDeletionException {
		StringBuilder output = new StringBuilder();
		Map<String, Object> metaData = new HashMap<String, Object>();
		try
		{
				metaData=generateMetaData(request, associates);
				this.nc=pickupNavigatorConfig(associates);
				/* note this doesnt actually do much yet */
				//Map<String, Object> metaData = new HashMap<String, Object>();
				//metaData=generateMetaData(request, associates);
				output.append(doHeadings(findQuestions));
					/* read all the questions */
				List<Test> deployData=getQuestionsFromTestBank(nc);
				
				/* if the list is empty and we havent errored, then we've found no new data */
				if (deployData.isEmpty())
				{
					output.append("<p>No new data found</p> ");
				}
				else
				{
					/* if we are finding the questions and generating all the deletion sql, then do one thing
					 * if e are just generating the legal data munging then do another */
					/* first we generate a list fo where conditions based on the deploy file data */
					if (findQuestions)
					{
						//SQLString sql3=new SQLString(NAVTESTS3,nc.getDBName(),batchNumber,deployData,
						//		sDateDeleteBeforeIsAssessed,sDateDeleteBeforeNotAssessed);		
						/* now this one we actually execute, and its the result of what we get that we use to egenrate a whole bunch of stuff 
						 * it returns a load of tis and qis that we use to build the ill use */
						/* if we batch in more than max allowed it can cause false data */
						Log l=getLog();
						if (batchNumber > MAXBATCH)
						{
							output.append ("Batch number greater than maximum advised ("+MAXBATCH.toString()+"). Run with less and check results consistent ");
						}
						try
						{						
							if (tableByTable)
							{
								/* as we blow the java heap space if we do all the tests at once, we have to do it one test at a time sometimes
								 * specified in the requesthandling file */
								for (Test thisTest:deployData)
								{
									
									String[] args={thisTest.getTestName(),deleteDateAsString(thisTest)};
									LinkedList<String> ws=new LinkedList<String>();
									ws.add(getWhereStr(args,WHERESTRING1));
									TIQIDataFromDB tiqiList=new TIQIDataFromDB(SQL3,batchNumber,
											sDateDeleteBeforeIsAssessed,sDateDeleteBeforeNotAssessed,nc,ws,l,doDebug);
									output.append(getTIQIsqls(tiqiList,getLog(),thisTest.getTestName()));						
								}
							}
							else
							{
								LinkedList<String> whereConditions=setWhereConditionsTest(deployData,WHERESTRING1);
								TIQIDataFromDB tiqiList=new TIQIDataFromDB(SQL3,batchNumber,
										sDateDeleteBeforeIsAssessed,sDateDeleteBeforeNotAssessed,nc,whereConditions,l,doDebug);
								output.append(getTIQIsqls(tiqiList,getLog(),"ALL TESTS"));
							}
						}
						catch (Exception x)
						{
							throw new DataDeletionException(x);
						}
								
					}
					else
					{
						/* create a new sqlstring object - this will process the tests to generate a where string in batches
						 * then top and tail each where string with the start and finish part eg select or update or whatever
						 * it will also set up the correct header which can be output if needed
						 */
						LinkedList<String> whereConditions=setWhereConditionsTest(deployData,WHERESTRING1);

						SQLString sql1=new SQLString(nc.getDBPrefix()+TESTSTABLE,SQL1,nc.getDBName(),batchNumber,
								sDateDeleteBeforeIsAssessed,sDateDeleteBeforeNotAssessed,whereConditions);						
						output.append(sql1.getHeader());
						output.append(sql1.getFullSQLasHTML());
						
						SQLString sql2=new SQLString(nc.getDBPrefix()+TESTSTABLE,SQL2,nc.getDBName(),batchNumber,
								sDateDeleteBeforeIsAssessed,sDateDeleteBeforeNotAssessed,whereConditions);
						output.append(sql2.getHeader());
						output.append(sql2.getFullSQLasHTML());
					}

				}
				getLog().logDebug("Finished now rendering back to the user : ");			
			/* output it */
		}
		catch (Exception x)
		{
			throw new DataDeletionException(x);
		}	
		return output.toString();
	}

	private String getTIQIsqls(TIQIDataFromDB tiqiList,Log l,String testName) throws DataDeletionException 
	{
	/* now we generate the sql to use, based on the tis nd qis we have extracted */
	/* first build a where list, then apply it to each table in tern */
		StringBuilder output=new StringBuilder();
		
		if(tiqiList.isEmptyTIQILists())
		{
			output.append("<h1>No data found for "+testName+"</h1>");
		}
		else
		{
			output.append("<h1>"+testName+"</h1>");		
			try
			{
				LinkedList<String> TIwhereConditions=setWhereConditionsString(tiqiList.getTIs(),WHERESTRING2);
				LinkedList<String> QIwhereConditions=setWhereConditionsString(tiqiList.getQIs(),WHERESTRING3);
				
				/* first generate the select */	
				output.append("<h2>Generating SELECT statements</h2>");
				output.append(getSql(SQL4,TIwhereConditions,QIwhereConditions,l));
		
				/* now we do it again for the delete strings */
				/* first we do the TI tables */
				output.append("<h2>Generating DELETE statements</h2>");
	
				output.append(getSql(SQL5,TIwhereConditions,QIwhereConditions,l));
				
				/* now we just output a list of tis and qis  to the screen */
				l.logWithTag("<h1>TI List</h1>",SQLIDString);
				l.logWithTag(logInstancesAsString(TIwhereConditions,SQLIDString,l),SQLIDString);
				//output.append(getInstancesAsString(TIwhereConditions,l));
				//output.append("<h1>QI List</h1>");
				l.logWithTag("<h1>QI List</h1>",SQLIDString);
				l.logWithTag(logInstancesAsString(QIwhereConditions,SQLIDString,l),SQLIDString);
				//output.append(getInstancesAsString(QIwhereConditions));
			}
			catch ( Exception x)
			{
				throw new DataDeletionException(x);
			}
		}
			
		return output.toString();
	}
	
	private String getSql(String whatToDO,LinkedList<String> TIwhereConditions,LinkedList<String> QIwhereConditions,Log l)
	throws DataDeletionException 
	{
		StringBuilder output=new StringBuilder();
		try
		{
			for (String tableName: tiTables)
			{
				SQLString sqlT=new SQLString(nc.getDBPrefix()+tableName,whatToDO,nc.getDBName(),batchNumber,
						sDateDeleteBeforeIsAssessed,sDateDeleteBeforeNotAssessed,TIwhereConditions);						
				output.append(sqlT.getHeader()+nc.getDBPrefix()+tableName+" select on TI<br/>");
				//output.append(sqlT.getFullSQLasHTML());
				sqlT.logFullSQLasPlainText(SQLIDString+"_"+nc.getDBPrefix()+tableName,l);
			}
			/* then we do the QITables */
			for (String tableName: qiTables)
			{
				SQLString sqlT=new SQLString(nc.getDBPrefix()+tableName,whatToDO,nc.getDBName(),batchNumber,
						sDateDeleteBeforeIsAssessed,sDateDeleteBeforeNotAssessed,QIwhereConditions);						
				output.append(sqlT.getHeader()+nc.getDBPrefix()+tableName +" select on QI<br/>");
				//output.append(sqlT.getFullSQLasHTML());
				sqlT.logFullSQLasPlainText(SQLIDString+"_"+nc.getDBPrefix()+tableName,l);
			}
		}
		catch ( Exception x)
		{
			throw new DataDeletionException(x);
		}
		return output.toString();
	}
	
	/* look at the deploy data generated and build up alist of where conditions. We'll put those togethger in the 
	 * SQLString
	 * This one is specifice to tests
	 */
	private LinkedList<String> setWhereConditionsTest(List<Test> deployData,String whatWhere) throws DataDeletionException
	{
		LinkedList<String> ws=new LinkedList<String>();
		for (Test thisTest:deployData)
		{
			try
			{
				String[] args={thisTest.getTestName(),deleteDateAsString(thisTest)};
				ws.add(getWhereStr(args,whatWhere));
			}
			catch (Exception x)
			{
				throw new DataDeletionException(x);
			}		
		}
		return ws;
	}
	/* look at the deploy data generated and build up alist of where conditions. We'll put those togethger in the 
	 * SQLString
	/* this one is ggnereic to strings wrap them up into batches */
	private LinkedList<String> setWhereConditionsString(ArrayList<String> WhereData,String whatWhere) throws DataDeletionException
	{
		LinkedList<String> ws=new LinkedList<String>();
		for (String thisWhere:WhereData)
		{
			try
			{
				String[] args={thisWhere};
				ws.add(getWhereStr(args,whatWhere));
			}
			catch (Exception x)
			{
				throw new DataDeletionException(x);
			}		
		}
		return ws;
	}
	
	/* look at the deploy data generated and build up alist of where conditions. We'll put those togethger in the 
	 * SQLString
	/* this one is ggnereic to strings wrap them up into batches */
	private String InstancesAsString(LinkedList<String> WhereData,boolean doLog,String tag,Log l) throws DataDeletionException
	{
		StringBuilder outputStr=new StringBuilder();
		for (String thisWhere:WhereData)
		{
			try
			{
				if (doLog)
				{
					l.logWithTag(thisWhere,tag);
				}
				else
				{
					outputStr.append(thisWhere+"</br>");
					
				}
			}
			catch (Exception x)
			{
				throw new DataDeletionException(x);
			}		
		}
		return outputStr.toString();
	}
	
	private String getInstancesAsString(LinkedList<String> WhereData,Log l) throws DataDeletionException
	{

		return InstancesAsString(WhereData,false,"",l);
	}
	
	private String logInstancesAsString(LinkedList<String> WhereData,String tag,Log l) throws DataDeletionException
	{

		return InstancesAsString(WhereData,true,tag,l);
	}

	/* get the deleteion date  as a string for the sql */
	private String deleteDateAsString(Test thistest)throws DataDeletionException
	{
			
		try
		{
			if (thistest.isAssessed())				
			{
				//getLog().logNormal("ASSESSED_STATUS",thistest.getTestName()+" is assessed");
				return sDateDeleteBeforeIsAssessed;
			}
			else
			{
				//getLog().logNormal("ASSESSED_STATUS",thistest.getTestName()+" is not assessed");
				return sDateDeleteBeforeNotAssessed;
			}		

		}
		catch (Exception x)
		{
			throw new DataDeletionException(x);
		}

	}
	/* buuild up the where part of the sql string for the test names*/
	private String getWhereStr(String[] testConditions,String whatWhere)  throws DataDeletionException
	{

		StringBuilder whereStr=new StringBuilder();
		try
		{	
			String thisOne=String.format(whatWhere, testConditions);
			whereStr.append(thisOne);

		}
		catch (Exception x)
		{
			throw new DataDeletionException(x);
		}
		return whereStr.toString();
	}
	



	/* if sw set a test bank in the requesthandling file use it otherwise use the default one */
	public File getTestbankFolder() 
	{
		if (TestBanks == null)
		{
			return new File(TESTBANK);
		}
		else 
		{
			return new File(TestBanks);
		}
	}
	
/* read through the test bank and get a list of all deploy files */
	
	private List<Test> getQuestionsFromTestBank(NavigatorConfig nc) throws DataDeletionException 
	{
		List<Test> deployData=new LinkedList<Test>();
		try
		{

			/* read the testbank for deploy files */
			
			File testBank = getTestbankFolder();
			
			File[] deployFiles = testBank.listFiles(
			new FilenameFilter() 
			{
				@Override
				public boolean accept(File dir, String name) 
				{
					return filenamePattern.matcher(name).matches();
				}
			});
			
			/* process each file */
			for (int i = 0; i < deployFiles.length; i++) {
				File deployFile = deployFiles[i];
				Matcher m = filenamePattern.matcher(deployFile.getName());
				m.matches();
				String deployId = m.group(1);
				try 
				{
					/*read and store the test data */
					Test thistest=new Test(deployId, deployFile, testBank);
					deployData.add(thistest);
				} 
				catch (OmException e) 
				{
					throw new OmUnexpectedException("Error parsing test " + deployId, e);
				}
			}
			
			
		}
		catch (Exception x)
		{
			throw new DataDeletionException(x);
		}
		return deployData;
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
			if (null != TestBanks) {
				metaData.put(DatabaseDeletionEnums.testBanks.toString(),
						TestBanks);
			}

			metaData.put(DatabaseDeletionEnums.batchNumber.toString(),batchNumber);
			metaData.put(DatabaseDeletionEnums.displayName.toString(),displayName);
	

		} catch (IOException x) {
			throw new DataDeletionException(x);
		}
		return metaData;
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
		if (StringUtils.isNotEmpty(s)) {
			String[] bits = s.split(",");
			if (null != bits) {
				for (int i = 0; i < bits.length; i++) {
					String bit = bits[i];
					if (StringUtils.isNotEmpty(bit)) {
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
	

	
	public void initialise(RequestAssociates associates)
	throws RequestHandlingException {
	
		try
		{
			/* set up the delete dates */
		    
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			dateDeleteBeforeIsAssessed.add(Calendar.YEAR, ISASSESSEDYEARS);
			sDateDeleteBeforeIsAssessed=sdf.format(dateDeleteBeforeIsAssessed.getTime());
			
			dateDeleteBeforeNotAssessed.add(Calendar.YEAR, NOTASSESSEDYEARS);
			sDateDeleteBeforeNotAssessed=sdf.format(dateDeleteBeforeNotAssessed.getTime());
			
			Date now = new Date();
		    SQLIDString = "SQL"+IDdateFormat.format(now);	
		    super.initialise(associates);
			Object o = associates.getConfiguration().get(
				RequestHandlerEnums.invocationPath.toString());
			if (null != o ? o instanceof String : false) {
				filteredUrl = (String) o;
			}
			/* read config from the requesthandlers.xml file obkject set up by the admin interface handler*/
			Map<String, Object> tt1 = associates.getConfiguration();
			String tb=DatabaseDeletionEnums.testBanks.toString();
			TestBanks=tt1.get(tb).toString();
			/* get the number of items to process at a time from the requesthandling xml file */
			String bn=DatabaseDeletionEnums.batchNumber.toString();
			Object o1 = tt1.get(bn);
			if (null != o1 ? o1 instanceof String : false) 
			{
				batchNumber=Integer.parseInt(o1.toString());
			}
			
			String dd=DatabaseDeletionEnums.debug.toString();
			Object o4 = tt1.get(dd);
			if (null != o4 ? o4 instanceof String : false) 
			{
				doDebug=Boolean.parseBoolean(o4.toString());
			}
			/* get the display name */
			String dispNam=DatabaseDeletionEnums.displayName.toString();
			if (null != o1 ? o1 instanceof String : false) 
			{
				displayName=tt1.get(dispNam).toString();
			}
			/* get the name of the tables that we search on ti, and the tables where we search on qi - if they ecxist
			 * as only used for on of the admin options */
			String tiTabs=DatabaseDeletionEnums.tiTables.toString();
			Object o2 = tt1.get(tiTabs);
			String qiTabs=DatabaseDeletionEnums.qiTables.toString();
			Object o3 = tt1.get(qiTabs);
			if (null != o2 ? o2 instanceof String : false) 
			{
				tiTables=o2.toString().split(",");
				/* now split it up as it will be a csv string
				 * and store it in the list */
			}
			if (null != o3 ? o3 instanceof String : false) 
			{
				qiTables=o3.toString().split(",");
				/* now split it up as it will be a csv string */

			}
			/* get the prefi8x of the name to use for the sql statments to be written to */
			String sqlFileNamePrefix=DatabaseDeletionEnums.sqlFileNamePrefix.toString();
			Object o5 = tt1.get(sqlFileNamePrefix);
			if (null != o5 ? o5 instanceof String : false) 
			{
				sqlFileName=o5.toString()+"_"+SQLIDString;
			}
			/* generate sql for all tables, or split it up table by table because sometimes we blow th ejava heap space*/
			String tbt=DatabaseDeletionEnums.tableByTable.toString();
			Object o6 = tt1.get(tbt);
			if (null != o6 ? o6 instanceof String : false) 
			{
				tableByTable=Boolean.parseBoolean(o6.toString());	
			}
		}
		catch (Exception e)
		{
			throw new RequestHandlingException(e);
		}

	}
	

}
