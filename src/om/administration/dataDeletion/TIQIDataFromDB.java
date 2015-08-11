package om.administration.dataDeletion;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import om.Log;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.db.DatabaseAccess;


public class TIQIDataFromDB {

	//private Map<String, ArrayList<String>> TIQIList = new HashMap<String, ArrayList<String>>();
	private SQLString sqlString;
	private ArrayList<String> TIList;
	private ArrayList<String> QIList;
	private Boolean doDebug=false;

	
	
	public TIQIDataFromDB(String name, Integer bn,String dbia,String dbna,NavigatorConfig nc, LinkedList<String> wheres,Log l,Boolean doDebug)throws DataDeletionException
	{
		this.doDebug=doDebug;
		try
		{
			/* batch it up */
			int cnt=0;
			Integer btchCnt=1;
			LinkedList<String> thisSetOfWhereStrings=new LinkedList<String>();
			boolean db=doDebug();
			for (Iterator<String> itr = wheres.iterator(); itr.hasNext(); ) 
			{
				/* generate the sql|String*/
				cnt++;	
				thisSetOfWhereStrings.add(itr.next());
				boolean ttp=DatabaseDeletionUtils.timeToProcess(cnt,bn);
				boolean hn=itr.hasNext();
				 if( ttp || !hn)
				 {
						/* output it */
					 	l.logDebug("Batch Number "+btchCnt.toString()+" ...");
						sqlString=new SQLString(name, bn,
								dbia, dbna, thisSetOfWhereStrings);	
						/* then run the extraction which generates the list TIQIList*/
						/*DEBUG*/
						
						if (!db)
						{
							runIt(nc,l);
						}
						thisSetOfWhereStrings=new LinkedList<String>();
						btchCnt++;

				 }
			}

		}
		catch (Exception e)
		{
			throw new DataDeletionException(e);
		}

	}
	/* if the list exists return it, if it doesnt, create it */
	public ArrayList<String> getTIList() {
		if (null == TIList) {
			synchronized (this) {
				if (null == TIList) {
					TIList = new ArrayList<String>();
				}
			}
		}
		return TIList;
	}
	
	/* if the list exists return it, if it doesnt, create it */
	public ArrayList<String> getQIList() {
		if (null == QIList) {
			synchronized (this) {
				if (null == QIList) {
					QIList = new ArrayList<String>();
				}
			}
		}
		return QIList;
	}
	
	/* generate the list of questions needed to look at from the database */
	private void runIt(NavigatorConfig nc,Log l) throws DataDeletionException 
	{
		DatabaseAccess.Transaction datS = null;
		DatabaseAccess da=null;
		try
		{
				da=DatabaseDeletionUtils.getDatabaseConnection(nc);
				datS = da.newTransaction();
				
				/* we iterate over the list fo sql statments generated */
				LinkedList<String> SQLStringsList=sqlString.getSQLFullStrings();
				boolean db=doDebug();
				for(Iterator<String> itr = SQLStringsList.iterator(); itr.hasNext();) 
				{
					String sqlString=itr.next();	
					ResultSet rs=datS.query(sqlString);
					/* iterate over the questions found and store in the list */
					Integer i=0;
				 	l.logDebug("resultset start... ");
				 	String lastTI=null;
					while (rs.next()) 
					{
						i++;
				 		/* this is fresh question data for wquestions finished since the lkast data report (glean) */
						/* addd the ti */

						String ti=rs.getString(1);
						String qi=rs.getString(2);

						if (!db)
						{
							if (!ti.equals(lastTI) || lastTI==null)
							{
								lastTI=ti;
								addInstance(ti,getTIList());
							}
							/* add the qi */
							addInstance(qi,getQIList());
						}

	 				}	
				 	l.logDebug("resultset finish... value of i "+i.toString());

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
	}

	
	/* if an entry ecists in the map, then add this one to the list, otherwise, add a new entry to the hashmap */
	public void addInstance(String inst,ArrayList<String> whichList) throws DataDeletionException 
	{
		if (null != inst )
		{

			/* if we dont have a list of qis for this ti, create a new entry for the ti */
			if (null == whichList) 
			{
				synchronized (this) 
				{
					if (null == whichList) 
					{
						whichList = new ArrayList<String>();
					}
				}
			}
		//	if (!whichList.contains(inst))
			//{
				whichList.add(inst.toString());
		//	}

		}
		else
		{
			throw new DataDeletionException("addInstance: invalid ti/qi data ");

		}
	}
	
	/* if there was nothing in the list then return empty */
	public boolean isEmptyTIQILists()
	{
		/* if one of them is empty, then they should both be */
		return (TIList == null || QIList == null || TIList.isEmpty() ||  QIList.isEmpty());
	}
	
	
		
	/* generate the list of sql statements needed to clear out the database for the ti/qi list retrieved */
	public void generateSQL(String[] tiTables, String[] qiTables)
	{
		
		/* first we iterate over the list of tables for ti, then we do the same for qi */
		
		
	}
	/* get uniqie list of tis */
	public ArrayList<String> getTIs()
	{
		return TIList;
	}
	
	/* get Unique list of QIS */
	public ArrayList<String> getQIs()
	{

		return QIList;
	}
	
	public Boolean doDebug()
	{
		return doDebug;
	}
	
	
}
