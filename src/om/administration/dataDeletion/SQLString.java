package om.administration.dataDeletion;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.lang3.StringEscapeUtils;

import om.Log;
import util.xml.XML;


public class SQLString {

	private String SQLname;
	private String tableName;
	private String SQLStartString;
	private String SQLEndstring;
	private LinkedList<String> SQLFullStrings;
	private LinkedList<String> WhereStrings;
	//private LinkedList<String> whereConditions;
	private Integer batchNumber=0;
   // private String sDateDeleteBeforeIsAssessed;
   // private String sDateDeleteBeforeNotAssessed;
    private String SQLHeader;
	
    public static String SQL1="sql1";
    public static String SQL2="sql2";
    public static String SQL3="sql3";
    public static String SQL4="sql4";
    public static String SQL5="sql5";
    
	private  String SQL1HEADER="<h1>Select SQL - data munging only</h1>";
	private  String SQL2HEADER="<h1>Update SQL</h1>";
	private  String SQL3HEADER="<h1>Select SQL - TIs and QIs</h1>";
	//private  String SQL4HEADER="<p>Select SQL</p>";
	//private  String SQL5HEADER="<p>Delete SQL</p>";
	private  String SQL4HEADER="";
	private  String SQL5HEADER="";

  	private static String SELECTSTRING1STRT="select {2} from [{1}] t where ";
  	private static String SELECTSTRING1END="";
  	
	private static String UPDATESTRING1STRT="update [{1}] set oucu={2}, pi={2} where ti in ( select ti from from [{0}].[dbo].[{1}] where ";
	private static String UPDATESTRING1END=" ) ";
	
  //	private static String SELECTSTRING2STRT="select ti,qi from [{0}].[dbo].[nav_questions] where ti in " +
  //			"(select ti	from [{0}].[dbo].[nav_tests] where ";
  //  	private static String SELECTSTRING2END=") order by ti,qi";

	private static String SELECTSTRING2STRT="select distinct t.ti, q.qi from [nav_questions] q "+
	"join [{0}].[dbo].[nav_tests] t on q.ti = t.ti " +
	"where (";
	private static String SELECTSTRING2END=") order by t.ti, q.qi";
	
	private static String SELECTSTRING3STRT="select * from [{1}] where ";
	private static String SELECTSTRING3END="";
	
  	
	private static String DELETESTRING5STRT="delete from [{1}] where ";
	private static String DELETESTRING5END="";
  	
	private static String DELSTRING="DELETED";

    
    /* with the table name */
	public SQLString(String tname,String name, Integer bn, String dbia,String dbna,LinkedList<String> wc)	 throws DataDeletionException
	{
		try
		{
			initialise(tname,name,bn,dbia,dbna,wc);
		}
		catch(Exception x)
		{
			throw new DataDeletionException(x);
		}
	}
	/*without the tablename */
	public SQLString(String name, Integer bn, String dbia,String dbna,LinkedList<String> wc)	 throws DataDeletionException
	{
		try
		{
			initialise("",name,bn,dbia,dbna,wc);
		}
		catch(Exception x)
		{
			throw new DataDeletionException(x);
		}
	}
	
	private void initialise(String tname, String name, Integer bn, String dbia,String dbna,LinkedList<String> wc)	 throws DataDeletionException
	{
		try
		{
			this.SQLname=name;
			this.tableName=tname;
			//Collections.copy(deployTestData,dtd);
			batchNumber=bn;			
			/* now generate evertything from what we have */
			SQLHeader=getSQLHeaderText();
			SQLStartString=setStartString();
			SQLEndstring=setFinishString();
			WhereStrings=setWhereStrings(wc);
			/* generate the full String for this SQL */
			SQLFullStrings=this.setFullString();


		}
		catch (Exception e)
		{
			throw new DataDeletionException(e);
		}

	}

	//public void setDeploytestData(List<Test> dtd)
	//{
	//	Collections.copy(deployTestData,dtd);	
	//}
	
	public LinkedList<String> getSQLFullStrings()
	{
		return SQLFullStrings;
	}
	
	public String getHeader()
	{
		return SQLHeader;
	}

	
	/* get the select and update strings needed to munge the users personal data for old tests */
	
	private LinkedList<String> setFullString() throws DataDeletionException
	{
		//List<StringBuilder> SQLStrings=new LinkedList<StringBuilder>();
		
		LinkedList<String> SQLStrings=new LinkedList<String>();
		/* get the where strings based on date determined by readinbg the deploy file data*/
		
		try
		{
			/* iterate over the where strings and buil up the full sql strings adding the start and end parts
			 * */
			 
			for(String ws : WhereStrings) 
			{
				StringBuilder SQLString=new StringBuilder();
				SQLString.append(SQLStartString);
				SQLString.append(ws);
				SQLString.append(SQLEndstring);
				//SQLStrings.append(DatabaseDeletionUtils.paragraphIt(SQLString.toString()));
				SQLStrings.add(SQLString.toString());

			}
		}
		catch (Exception x)
		{
			throw new DataDeletionException(x);
		}

		return SQLStrings;
	}
	
	/* run thorught the list of sql statements and generate them as a string. Add <p> and html escape it if its going to output on a web page, 
	 * otherwise simply add a ; and a space */
	 
	public String getFullSQLasPlainText()
	{
		return this.getFullSQLasString(false,false,null,null);
	}
	
	public String getFullSQLasHTML()
	{
		return this.getFullSQLasString(true,false,null,null);
	}
	
	/* write it to the log file as html */
	public String logFullSQLasHTML(String tag,Log l)
	{
		return this.getFullSQLasString(true,true,tag,l);
	}
	
	/* write it to the log file as plain text */
	public String logFullSQLasPlainText(String tag,Log l)
	{
		return this.getFullSQLasString(false,true,tag,l);
	}
	
	public String getFullSQLasString(boolean HTMLit,boolean LogIt,String tag,Log l)
	{
		StringBuilder fullsqlstring=new StringBuilder();
		for(Iterator<String> itr = SQLFullStrings.iterator(); itr.hasNext();) 
		{
			StringBuilder SQLString=new StringBuilder();
			SQLString.append(itr.next());
			SQLString.append(";");
			if (HTMLit)
			{
				if(LogIt)
				{
					l.logWithTag(DatabaseDeletionUtils.paragraphIt(XML.escape(SQLString.toString())),tag);
				}
				else
				{
					fullsqlstring.append(DatabaseDeletionUtils.paragraphIt(XML.escape(SQLString.toString())));
				}
			}
			else
			{
				if(LogIt)
				{
					l.logWithTag(StringEscapeUtils.escapeJava(SQLString.append(" ").toString()),tag);
				}
				else
				{
					fullsqlstring.append(StringEscapeUtils.escapeJava(SQLString.append(" ").toString()));
				}
			}

		}
		return fullsqlstring.toString();
	}
		
	
	
	
	/* im sure there is a better way to do this , but for now */
	private  String setStartString()
	{
		String str="";
		if (SQLname.equals(SQL1))
		{
			/* select everything */
			Object[] arguments = {"",tableName,"*"};
			str=MessageFormat.format(SELECTSTRING1STRT, arguments);
		}
		if (SQLname.equals(SQL2))
		{
			Object[] arguments = {"",tableName,"'"+DELSTRING+"'"};
			str=MessageFormat.format(UPDATESTRING1STRT, arguments);			
		}		
		if (SQLname.equals(SQL3))
		{
			Object[] arguments = {""};
			str=MessageFormat.format(SELECTSTRING2STRT, arguments);	
		}
		if (SQLname.equals(SQL4))
		{
			Object[] arguments = {"",tableName};
			str=MessageFormat.format(SELECTSTRING3STRT, arguments);	
		}
		if (SQLname.equals(SQL5))
		{
			Object[] arguments = {"",tableName};
			str=MessageFormat.format(DELETESTRING5STRT, arguments);	
		}
		return str;
	}
	
	/* im sure there is a better way to do this , but for now */
	private  String setFinishString()
	{
		String str="";
		//String dbName=nc.getDBName();
		if (SQLname.equals(SQL1))
		{
			str=SELECTSTRING1END;
		}
		if (SQLname.equals(SQL2))
		{
			str=UPDATESTRING1END;;
		}
		if (SQLname.equals(SQL3))
		{
			/* select everything */
			str=SELECTSTRING2END;
		}
		if (SQLname.equals(SQL4))
		{
			/* select everything */
			str=SELECTSTRING3END;
		}
		if (SQLname.equals(SQL5))
		{
			/* select everything */
			str=DELETESTRING5END;
		}
		return str;
	}
	
	
	
	/* im sure there is a better way to do this , but for now */
	private String getSQLHeaderText()
	{
		String str="";
		if (SQLname.equals(SQL1))
		{
			str= SQL1HEADER;
		}
		if (SQLname.equals(SQL2))
		{
			str= SQL2HEADER;

		}
		if (SQLname.equals(SQL3))
		{
			str= SQL3HEADER;

		}
		if (SQLname.equals(SQL4))
		{
			str= SQL4HEADER;

		}
		if (SQLname.equals(SQL5))
		{
			str= SQL5HEADER;

		}
		return str;
	}
	/* build up the where clause for deploys and dates from the list of tests generated */

	private LinkedList<String>  setWhereStrings(LinkedList<String> wc) throws DataDeletionException
	{

		StringBuilder whereStr=new StringBuilder();
		LinkedList<String> whereStrings=new LinkedList<String>();
		int cntr=0;
		/* generate the first part of the select string. We'll do this properly eventually */
		try
		{		
			/* where conditions is set to null, just return */
			if (wc != null)
			{
				for (Iterator<String> itr = wc.iterator(); itr.hasNext(); ) 
				{
					/* process in batches  to limit SQL calls */
					String ws=itr.next();
					 cntr++;
					 whereStr.append("(");
					 whereStr.append(ws);
					 whereStr.append(")");
					 if(DatabaseDeletionUtils.timeToProcess(cntr,batchNumber) || !itr.hasNext())
					 {
						 whereStrings.add(whereStr.toString());
						 whereStr=new StringBuilder();
					 }
					 else
					 {
						 whereStr.append(" or ");
					 }
				}
			}

		}
		catch (Exception x)
		{
			throw new DataDeletionException(x);
		}

		return whereStrings;
	}
	



	
	/* if everything except the table name is the same, reset the full string using the new table name
	 */
	public void resetTableName(String tableName) throws DataDeletionException
	{
		try
		{
			this.tableName=tableName;
			/* generate the full String for this SQL */
			SQLFullStrings=this.setFullString();


		}
		catch (Exception e)
		{
			throw new DataDeletionException(e);
		}
	}


	
}
