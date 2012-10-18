package om.administration.simpleSQLReporter;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.AbstractRequestHandler;
import om.DisplayUtils;
import om.RenderedOutput;
import om.RequestAssociates;
import om.RequestHandlerEnums;
import om.RequestHandlingException;
import om.RequestParameterNames;
import om.RequestResponse;
import om.administration.dataDeletion.DataDeletionException;
import om.administration.dataDeletion.DatabaseDeletionUtils;
import om.administration.databaseCleaner.ExtractorException;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.db.DatabaseAccess;

import org.apache.commons.lang.StringUtils;

public class SimpleSQLReporterHandler extends AbstractRequestHandler {

	private String filteredUrl;

	private String postToUrl;
	
	
	public static SimpleDateFormat DATEFORMAT=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

	//  private static int BATCHAMOUNT=10;
	//private Integer batchNumber=BATCHAMOUNT;

	private String SQLString="";
	private String separator=",";



	/* create a stamp for the og file so sql statmenets can easily be extracted as a group */


	@Override
	public RequestResponse handle(HttpServletRequest request,
		HttpServletResponse response, RequestAssociates associates)
		throws RequestHandlingException {
		RequestResponse rr = new RenderedOutput();
		if (null != request && null != response && null != associates) {
			
			//initialise();
			
			initialise(associates);
			String uri = request.getPathInfo();
			if (StringUtils.isNotEmpty(filteredUrl)
				? filteredUrl.equals(uri) : false) {
			    Date now = new Date();
				getLog().logDebug("Running the Sql Report  ...");
				StringBuilder output = new StringBuilder(DisplayUtils.header());
				try {
					String er = process(request, associates);
					if (null != er ) {
						output.append("<h1>Generating simple SQL report "+DATEFORMAT.format(now)+" </h1>");
						output.append("<p>"+SQLString+"</p><br />");						
						output.append(er);

					}

				} catch (Exception x) {
					output.append("<h1>An Error Occured with the SQL Generation </h1><br /><br />");
					output.append(handleException(x.getMessage()));
				}
				output.append(DisplayUtils.footer());
				rr.append(output.toString());
				getLog().logDebug("Finished now rendering back to the user : " + output);
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
		RequestAssociates associates) throws SimpleSQLReporterException {
		StringBuilder output = new StringBuilder();
		Map<String, Object> metaData = new HashMap<String, Object>();

		try
		{
			NavigatorConfig nc=pickupNavigatorConfig(associates);
			/* note this doesnt actually do much yet */
			metaData=generateMetaData(request, associates);
			/* read all the questions */
			String SQLReport=getDataFromDB(nc);
			
			/* if the list is empty and we havent errored, then we've found no new data */
			if (SQLReport.isEmpty())
			{
				output.append("<p>No new data found</p> ");
			}
			else
			{
				output.append(SQLReport.toString());
			}
			
			
			/* output it */
		}
		catch (Exception x)
		{
			throw new SimpleSQLReporterException(x);
		}	
		return output.toString();
	}
	
	/* generate the list of questions needed to look at from the database */
	private String getDataFromDB(NavigatorConfig nc) throws DataDeletionException 
	{

		StringBuilder dataString=new StringBuilder();
		DatabaseAccess.Transaction datS = null;
		DatabaseAccess da=null;

		try {
					da=DatabaseDeletionUtils.getDatabaseConnection(nc);
					datS = da.newTransaction();

					ResultSet rs=datS.query(SQLString);
				    ResultSetMetaData metaData = rs.getMetaData();
				    int columns = metaData.getColumnCount();
				    /* get the column names first of all */
					StringBuilder headings = new StringBuilder();
				    for (int i=1; i<columns+1; i++) 
				    {
				    	headings.append(metaData.getColumnName(i));
				    	headings.append(separator);
				    }
			        dataString.append(SimpleSQLReporterUtils.breakIt(headings));		

					/* iterate over the questions found and store in the list */
					while (rs.next()) 
					{
						StringBuilder record = new StringBuilder();
				        for (int i = 1; i <= columns; i++) {
				            String value = 	rs.getString(i);
				            record.append(value);
				            record.append(separator);
				        }
				        dataString.append(SimpleSQLReporterUtils.breakIt(record));		
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
		return dataString.toString();
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
		RequestAssociates associates) throws SimpleSQLReporterException 
		{
		/* doesnt actually do much for now, just a load of nulls, will fill it in later */
		Map<String, Object> metaData = new HashMap<String, Object>();
		try {
			metaData.put(SimpleSQLReporterEnums.SimpleSQLReporterUrl.toString(), filteredUrl);
			metaData.put(SimpleSQLReporterEnums.postToUrl.toString(), postToUrl);
			metaData.put(SimpleSQLReporterEnums.navigatorConfigKey.toString(),
				pickupNavigatorConfig(associates));
			metaData.put(RequestParameterNames.logPath.toString(), getLogPath());
			if (null != SQLString) {
				metaData.put(SimpleSQLReporterEnums.SQLString.toString(),
						SQLString);
			}
			if (null != separator) {
				metaData.put(SimpleSQLReporterEnums.separator.toString(),
						separator);
			}
			//metaData.put(SimpleSQLReporterEnums.batchNumber.toString(),
			//		batchNumber);
			

		} catch (IOException x) {
			throw new SimpleSQLReporterException(x);
		}
		return metaData;
	}

	
	public void initialise(RequestAssociates associates)
	throws RequestHandlingException {
	
		try
		{
			/* set up the delete dates */
		    super.initialise(associates);
			Object o = associates.getConfiguration().get(
				RequestHandlerEnums.invocationPath.toString());
			if (null != o ? o instanceof String : false) {
				filteredUrl = (String) o;
			}
			/* read config from the requesthandlers.xml file obkject set up by the admin interface handler*/
			Map<String, Object> tt1 = associates.getConfiguration();

			String ss=SimpleSQLReporterEnums.SQLString.toString();
			SQLString=tt1.get(ss).toString();
			
			/* if it exists get it, therwise we use the predefined separator*/
			String sep=SimpleSQLReporterEnums.separator.toString();
			if(tt1.containsValue(sep))
			{
				separator=tt1.get(sep).toString();
			}
			///* get the number of items to process at a time from the requesthandling xml file
		//	 * if 0 then do em all at once */
			//String bn=SimpleSQLReporterEnums.batchNumber.toString();
			//Object o1 = tt1.get(bn);
			//if (null != o1 ? o1 instanceof String : false) 
			//{
			//	batchNumber=Integer.parseInt(o1.toString());
			//}
		}
		catch (Exception e)
		{
			throw new RequestHandlingException(e);
		}

	}
	
	

}
