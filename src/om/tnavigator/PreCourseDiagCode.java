package om.tnavigator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;

import javax.servlet.ServletException;

import om.OmUnexpectedException;
import om.RequestAssociates;
import om.RequestHandlingException;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.OmQueries;
import util.misc.Strings;

/* generate a code to be used by the Are You ready for tests
 *  
 */
public class PreCourseDiagCode {
	
	private static int OK=0;
	private static String SEPARATOR="/";
	private static String BADCREATE="Unable to crate AYRFcode due to non-specified ti ";
	private static String BADSTORE1="Unable to store code in db for code and ti instance sql error ";
	private static String BADSTORE2="Unable to store code in db for code and ti instance, null pcdc ";
	private static String BADSTORE3="Unable to update code in db for code and ti instance ";
	private static String DATEFORMAT="yyyyMMdd";
	private static String CANT_DETERMINE_PCDC="Unable to determine pre-course diag code ";
	private static String BADRETRIEVE="Unable to retrieve pre course diagnostic code from DB ";
	private static String BADSTRINGTOINT="Unable to convert ti from string to integer ";
	private static String DEFAULTCODE="DEFCODE";
	private static String BADDBACCESS="Error accessing database for pcdc: ";
	private static String ERRORGENERATE="Error generating pcdc code ";
	//private int RED=1;
	//private int AMBER=2;
	//private int green=3;
	//private int NOTL=0;
	
	private String PreCourseDiagCode="";
	private int ti=0;
	private String datetime="";
	private String trafficlights="";
	
	DateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
	Date date = new Date();	

	/* create a blank one */
	public PreCourseDiagCode()
	{
		generateCode();
		datetime=dateFormat.format(date);
	}
		/* base code on the ti Actually generate the code
	 * 
	 */
	public PreCourseDiagCode(int ti)
	{
		/* generate and store it in DB */
			String testInst=Integer.toString(ti);
			generateCode(testInst);
			this.ti=ti;
			datetime=dateFormat.format(date);
			
	}
	
	/* just set the values */
	public PreCourseDiagCode(String pcdc)
	{
		PreCourseDiagCode=pcdc;
		datetime=dateFormat.format(date);

	}
	/* just set the values */
	
	public PreCourseDiagCode(int ti,String oucu)
	{
		/* generate and store it in DB */
			String testInst=Integer.toString(ti);
			generateCode(oucu,testInst);
			this.ti=ti;
			this.datetime=dateFormat.format(date);

			
	}
	public PreCourseDiagCode(int ti,String pcdc,String dt, String oucu,String tl)
	{

		if (pcdc != "")
			{PreCourseDiagCode=pcdc;}
		else
			{generateCode(oucu,tl);}
		this.ti=ti;
		datetime=( dt != "" ? dt: dateFormat.format(date));
		trafficlights= (tl!=""?tl:"");

	}
	/* generate the object by reading it from the database for this ti */
	public PreCourseDiagCode(DatabaseAccess.Transaction dat, NavigatorServlet ns, int ti)
	 throws Exception
	{
		if (ti == 0){
			throw new Error(BADDBACCESS);}
		this.ti=ti;
		this.getPreCourseDiagCodeFromDB(dat, ns, ti); 


	}

	
	public void setTi(int ti)
	{
		this.ti=ti;			
	}
	
	public void setCode(String pcdc)
	{
		PreCourseDiagCode=pcdc;
	}
	
	
	public void setTrafficlights(String tl)
	{
		trafficlights=tl;
	}

	/*
	 * generate the code from the date
	 */
	private void generateCode()
	{
		DateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
		Date date = new Date();	
		PreCourseDiagCode=dateFormat.format(date);

	}
	
	/*
	 * generate the code from a string
	 */
	private void generateCode(String s)
	{
	
		PreCourseDiagCode=s;

	}
	
	/* generate the code from the 2 strings
	 * 
	 */
	private void generateCode(String s1,String s2)
	{
		//DateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
		//Date date = new Date();	
		PreCourseDiagCode=CANT_DETERMINE_PCDC;
		if ( s1 != "") {	
			/*generate the code and store it in db for this ti
			/*
			 */
				try{
					if (s2 != "")
					{
						PreCourseDiagCode=s2+SEPARATOR+s1;
					}
					else
					{
						//pcdc=s1+SEPARATOR+dateFormat.format(date);
						PreCourseDiagCode=s1;
					}
				}catch (Exception e) {
					throw new Error(ERRORGENERATE, e);
				}
			}else throw new Error(BADCREATE);
}
	
	
	public String getPreCourseDiagCode()
	{
		return PreCourseDiagCode;
	}

	public int getti()
	{
		return ti;
	}
	
	public String getTrafficlights()
	{
		return trafficlights;
	}
	
	public String getDatetime()
	{
		return datetime;
	}
	
	public String gettiAsString()
	{
		return Integer.toString(ti);
	}
	/* save the code in the database against a ti
	 * 
	 * 
	 */

	public void updateDBwithCode(DatabaseAccess.Transaction dat,OmQueries oq) 
	 throws Exception  {
						
		if (null !=this) {
				try{
					oq.updateTestPreCourseDiagCode(dat,this);
					}
					catch(SQLException e)
					{
						throw new Error(BADSTORE3+" "+this.gettiAsString());
					}
			}  
			else
			{
				throw new Error(BADSTORE2);
			}			
	}
	
	public void insertTestPCDC(DatabaseAccess.Transaction dat,OmQueries oq) 
	 throws Exception  {
		if (null !=this) {
			try{
				oq.insertTestPreCourseDiagCode(dat,this);
			}
			catch(SQLException e)
				{
				throw new Error(BADSTORE2+" "+this.gettiAsString());
				}
		}  
		else
		{
			throw new Error(BADSTORE2);
		}
		
}
	
	/**
	 * Here we check that the test meets the criteria for generating the code
	 * only generate if option set and the test is not finished
	 * @return
	 * @throws RequestHandlingException
	 * @author sarah wood
	 */
	static boolean shouldDoCode(UserSession us)
	 throws Exception  {
		boolean should = false;
		// start with a simple, run it if its an open to the world test
		// may extend to having a specific tag
		if (null != us ? null != us.getTestDeployment() : false) {
			if (us.getTestDeployment().getbPcdc() && !us.isHasGeneratedFinalPCDC())
			 {
				should = true;
			}
		}
		return should;
	}
	
	static boolean shouldGenerateCode(UserSession us)
	 throws Exception  {
		return shouldDoCode(us);
	}
	
	static boolean shouldGenerateNewCode(UserSession us)
	 throws Exception  {
		//reset the boolean in the usersession
		us.setHasGeneratedFinalPCDC(false);
		return shouldDoCode(us);

	}
	
	static boolean shouldReadCode(UserSession us)
	 throws Exception  {
		boolean should = false;
		// start with a simple, run it if its an open to the world test
		// may extend to having a specific tag
		if (null != us ? null != us.getTestDeployment() : false) {
			if (us.getTestDeployment().getbPcdc() )
			 {
				should = true;
			}
		}
		return should;
	}
	
	/**
	 * Here we check that the test shpould displayt he codeto the student
	 * @return
	 * @throws RequestHandlingException
	 * @author sarah wood
	 */
	static boolean shouldDisplayCode(UserSession us)
	 throws Exception  {
		boolean should = false;
		// start with a simple, run it if its an open to the world test
		// may extend to having a specific tag
		if (null != us ? null != us.getTestDeployment() : false) {
			if (us.getTestDeployment().getbDisplayPcdc() )
			 {
				should = true;
			}
		}
		return should;
	}

	public  void  getPreCourseDiagCodeFromDB(
			DatabaseAccess.Transaction dat, NavigatorServlet ns, int iTi) 
		 throws Exception  {
			
		try{
				ResultSet rs = ns.getOmQueries().queryPreCourseDiagCode(dat,iTi);
					try
						{
						while(rs.next())
							{
							/* we should only have one entry per ti
							 * 
							 */
							this.ti = rs.getInt(1);
							this.PreCourseDiagCode = rs.getString(2);
							this.datetime = rs.getString(3);
							this.trafficlights=rs.getString(4);
							}
						} catch (Exception e) {
							throw new OmUnexpectedException(BADRETRIEVE, e);
						}	
			} catch (Exception e) {
			throw new ServletException(BADDBACCESS+ e.getMessage(), e);
			} 			
	
	}
}





