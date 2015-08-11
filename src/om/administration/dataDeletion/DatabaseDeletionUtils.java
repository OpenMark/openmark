package om.administration.dataDeletion;

import java.util.Date;

import om.tnavigator.NavigatorConfig;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.OmQueries;

public class DatabaseDeletionUtils {

	private static String EN = "en";


    
	public static DatabaseAccess getDatabaseConnection(NavigatorConfig nc) throws DataDeletionException 
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
			da = new DatabaseAccess(null);
		} catch (Exception e) 
		{
			throw new DataDeletionException(
					"Error creating database class or JDBC driver (make sure DB plugin and JDBC driver are both installed): "
					+ e.getMessage());
		}
		return da;
	}
	
	public static String paragraphIt(String what)
	{
		StringBuilder para=new StringBuilder();
		para.append("<p>");
		para.append(what);
		para.append("</p>");
		return para.toString();
		
	}
	

	
	/* return which ever date is earlier */
	public static Date earliestDate(Date d1,Date d2) throws DataDeletionException
	{
		if (d1==null && d2==null)
		{
			return null;
		}			
		if (d1 == null) return d2;
		if (d2 == null) return d1;

		if (d1.before(d2))
		{
			return(d1);
		}
		else
		{
			return(d2);
		}
		
	}
	/* return whicher date is later */
	public static Date latestDate(Date d1, Date d2) throws DataDeletionException
	{
		if (d1==null && d2==null)
		{
			return null;
		}			
		if (d1 == null) return d2;
		if (d2 == null) return d1;
		
		if (d1.after(d2))
		{
			return(d1);
		}
		else
		{
			return(d2);
		}
		
	}
	
	/* we are processing in batches, so we need to know if its time to process */
	public static boolean timeToProcess(int cntr,int batchNumber) throws DataDeletionException
	{
		try
		{
			/* divide by batch number, and if it divides evenly, its time to process */
			Integer t1=(cntr/batchNumber);
			double t2=cntr/(double)batchNumber;
			double t3=(double) t1;
			return(t3==t2);

		}
		catch (Exception x)
		{
			throw new DataDeletionException(x);
		}
	}

	
}
