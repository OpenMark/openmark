package om.administration.dataDeletion;

import java.io.File;
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

import om.DisplayUtils;
import om.OmException;
import om.OmUnexpectedException;
import om.abstractservlet.AbstractRequestHandler;
import om.abstractservlet.RenderedOutput;
import om.abstractservlet.RequestAssociates;
import om.abstractservlet.RequestHandlerEnums;
import om.abstractservlet.RequestParameterNames;
import om.abstractservlet.RequestResponse;
import om.administration.extraction.ExtractorException;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.reports.std.DeployedTestsReport.Test;

import org.apache.commons.lang.StringUtils;

import util.misc.UtilityException;

public class DataDeletionTestBank extends AbstractRequestHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2382451311202597521L;

	private String filteredUrl;

	private String postToUrl;
	private String displayName;
	
	public static String DATEFORMATFORDB="yyyyMMdd HH:mm:ss";
	
	public static int ISASSESSEDYEARS=-6;
	public static int NOTASSESSEDYEARS=-2;

	private String TestBanks;
    private static int BATCHAMOUNT=10;
	private Integer batchNumber=BATCHAMOUNT;
	private Boolean doDebug=false;
	private Boolean tableByTable=true;	
    private Calendar dateDeleteBeforeIsAssessed=Calendar.getInstance();
    private Calendar dateDeleteBeforeNotAssessed=Calendar.getInstance();
	private String sDateDeleteBeforeIsAssessed;
	private String sDateDeleteBeforeNotAssessed;
	
    private NavigatorConfig nc;	
	private String SQLIDString="";

	private final static String NEWPARA="<p>";
	private final static String DELSTMT="rm ";
	private static String ENDPARA="</p>";

	private final static Pattern filenamePattern = Pattern.compile("^(.*)\\.deploy\\.xml$");
	private final static DateFormat IDdateFormat = new SimpleDateFormat("HHmmss");

	private final static String TESTBANK="C:\\Program Files\\apache-tomcat-6.0.33\\webapps\\om-tn\\testbank";



    
	/* create a stamp for the og file so sql statmenets can easily be extracted as a group */


	public RequestResponse handleAll(HttpServletRequest request,
			HttpServletResponse response, RequestAssociates associates)
			throws UtilityException {
		
		
		RequestResponse rr = new RenderedOutput();
		if (null != request && null != response && null != associates) {
			
			//initialise();
			
			initialise(associates);

			
			String uri = request.getPathInfo();
			if (StringUtils.isNotEmpty(filteredUrl)
				? filteredUrl.equals(uri) : false) {
				getLog().logDebug("Running the Sql Generation ...");
				StringBuilder output = new StringBuilder(DisplayUtils.header());
				
				output.append("<h1>TestBank data deletion</h1>");	
				if (doDebug)
				{
					output.append("<h1>running in DEBUG mode</h1>");	
				}

				output.append("<h1>Deleting assessed tests before "+sDateDeleteBeforeIsAssessed+
						" and non-assessed tests before "+sDateDeleteBeforeNotAssessed+"</h1>");	
				
				try {
					String er = process(request, associates);

					if (null != er ) {
						output.append(er);			
					}

				} catch (Exception x) {
					output.append("<h1>An Error Occured with the testbank clearing </h1><br /><br />");
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

	private String doHeadings()
	{
		StringBuilder output = new StringBuilder();
		if(!displayName.isEmpty())
		{
				output.append("<h1>"+displayName+"</h1>");
		}		

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
		RequestAssociates associates) throws DataDeletionException {
		StringBuilder output = new StringBuilder();

		Map<String, Object> metaData = new HashMap<String, Object>();
		try
		{
				metaData=generateMetaData(request, associates);
				this.nc=pickupNavigatorConfig(associates);

				output.append(doHeadings());
					/* read all the questions */
				List<Test> deployData=getQuestionsFromTestBank(nc);
				
				/* if the list is empty and we havent errored, then we've found no new data */
				if (deployData.isEmpty())
				{
					output.append("<p>No new data found</p> ");
				}
				else
				{
					try
					{
						/* First we generate a list of what we are looking at and what we are doing, then we generate a list of delete statments 
						 * for easy cut and paste
						 */
						for (Test thisTest:deployData)
						{
							output.append(reportCheckAndArchive(thisTest));
						}
						output.append("<h1>Delete statements for archiveable tests</h1> ");
						for (Test thisTest:deployData)
						{
							output.append(checkAndArchive(thisTest));
						}
					}
					catch (Exception x)
					{
						throw new DataDeletionException(x);
					}
								
				}
			
			/* output it */
		}
		catch (Exception x)
		{
			throw new DataDeletionException(x);
		}	
		return output.toString();
	}


	public Object reportCheckAndArchive(Test thisTest)
	{
		//* check when it was closed and if its assessed. If assessed and 6 years old, or non-assessed and 2 years old
		StringBuilder outputStr=new StringBuilder();
		outputStr.append(NEWPARA);
		outputStr.append(thisTest.getDeployFileName());
		outputStr.append(", Date Closed : ");
		outputStr.append(thisTest.getDateClosed());
		outputStr.append(", Assessed? : ");
		outputStr.append(thisTest.isAssessed() ? "Yes": "No");
		outputStr.append(", Archive date : ");
		outputStr.append(thisTest.getArchiveDate());
		outputStr.append(",");
		if (thisTest.isArchiveable())
		{
			outputStr.append(" ready to archive");
		}
		else
		{
			/* chekc for a close date, and if there is one, do manual archive and check */
			if (thisTest.getDateClosed().isEmpty())
			{
				outputStr.append(" manual archive");

			}
			else
			{
				outputStr.append(" Current");
			}
		}			
		outputStr.append(ENDPARA);	
	
		return outputStr.toString();
	}
	
	public Object checkAndArchive(Test thisTest)
	{
		//* check when it was closed and if its assessed. If assessed and 6 years old, or non-assessed and 2 years old
		StringBuilder outputStr=new StringBuilder();
		if (thisTest.isArchiveable())
		{
					outputStr.append(NEWPARA);
					outputStr.append(DELSTMT);
					outputStr.append(thisTest.getDeployFileName());
					outputStr.append(ENDPARA);	

		}
	
		return outputStr.toString();
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
					Test thistest=new Test(deployId, deployFile, testBank,dateDeleteBeforeIsAssessed, dateDeleteBeforeNotAssessed);
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
	throws UtilityException {
	
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
			displayName=tt1.get(dispNam).toString();

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
			throw new UtilityException(e);
		}

	}
	

}
