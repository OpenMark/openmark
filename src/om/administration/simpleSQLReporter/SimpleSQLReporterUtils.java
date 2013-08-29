package om.administration.simpleSQLReporter;

import om.tnavigator.NavigatorConfig;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.OmQueries;

public class SimpleSQLReporterUtils {

    public static String NAVTESTS1="NAVTESTS1";
    public static String NAVTESTS2="NAVTESTS2";
    public static String NAVTESTS3="NAVTESTS3";

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
}
