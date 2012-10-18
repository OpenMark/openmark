package om.administration.simpleSQLReporter;

import java.text.MessageFormat;

import om.administration.dataDeletion.DataDeletionException;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.OmQueries;

public class SimpleSQLReporterUtils {

    public static String NAVTESTS1="NAVTESTS1";
    public static String NAVTESTS2="NAVTESTS2";
    public static String NAVTESTS3="NAVTESTS3";

  	private static String SELECTSTRING1="select {2} from [{0}].[dbo].[{1}] where ";
	private static String UPDATESTRING1="update [{0}].[dbo].[{1}] set oucu={2}, pi={2} where ti in ";
	private static String WHERESTRING1="( deploy={0} and finishedclock &lt; {1} )";
	private static String DELSTRING="DELETED";

	/* atable names */
    private static String NAVTESTS="nav_tests";

    
	public static DatabaseAccess getDatabaseConnection(NavigatorConfig nc) throws SimpleSQLReporterException 
	{
		String dbClass = nc.getDBClass();
		String dbPrefix = nc.getDBPrefix();
		DatabaseAccess da=null;
		OmQueries oq;

		try 
		{
			oq = (OmQueries) Class.forName(dbClass).getConstructor(
					new Class[] { String.class }).newInstance(
					new Object[] { dbPrefix });
			da = new DatabaseAccess(nc.getDatabaseURL(oq),  null);
		} catch (Exception e) 
		{
			throw new SimpleSQLReporterException(
					"Error creating database class or JDBC driver (make sure DB plugin and JDBC driver are both installed): "
					+ e.getMessage());
		}
		return da;
	}
	
	public static String paragraphIt(StringBuilder what)
	{
		StringBuilder para=new StringBuilder();
		para.append("<p>");
		para.append(what);
		para.append("</p>");
		return para.toString();
		
	}
	
	public static String breakIt(StringBuilder what)
	{
		StringBuilder para=new StringBuilder();
		para.append(what);
		para.append("<br/>");
		return para.toString();
		
	}

	/* we are processing in batches, so we need to know if its time to process */
	private boolean timeToProcess(int cntr,int batch) throws SimpleSQLReporterException
	{
		try
		{
			/* divide by batch number, and if it divides evenly, its time to process */
			Integer t1=(cntr/batch);
			double t2=cntr/(double)batch;
			double t3=(double) t1;
			return(t3==t2);

		}
		catch (Exception x)
		{
			throw new SimpleSQLReporterException(x);
		}
	}
	
}
