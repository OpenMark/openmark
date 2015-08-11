package om.tnavigator.teststructure;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import om.tnavigator.NavigatorServlet;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.OmQueries;
import om.tnavigator.sessions.UserSession;
import util.misc.UtilityException;

/**
 * Class to generate a code to be used by the Are You ready for tests.
 */
public class PreCourseDiagCode {
	private static String SEPARATOR = "/";
	private static String BADCREATE = "Unable to crate AYRFcode due to non-specified ti ";
	private static String DATEFORMAT = "yyyyMMdd";
	private static String CANT_DETERMINE_PCDC = "Unable to determine pre-course diag code ";
	private static String BADDBACCESS = "Error accessing database for pcdc: ";

	private String PreCourseDiagCode = "";
	private int ti = 0;
	private String datetime = "";
	private String trafficlights = "";

	DateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
	Date date = new Date();

	/** Create a blank one. */
	public PreCourseDiagCode()
	{
		generateCode();
		datetime = dateFormat.format(date);
	}

	/**
	 * Base code on the ti Actually generate the code
	 */
	public PreCourseDiagCode(int ti)
	{
		// generate and store it in DB.
		String testInst=Integer.toString(ti);
		generateCode(testInst);
		this.ti = ti;
		datetime = dateFormat.format(date);
	}

	/** Just set the values. */
	public PreCourseDiagCode(String pcdc)
	{
		PreCourseDiagCode = pcdc;
		datetime = dateFormat.format(date);
	}

	/** Just set the values. */
	public PreCourseDiagCode(int ti,String oucu)
	{
		// generate and store it in DB.
		String testInst=Integer.toString(ti);
		generateCode(oucu,testInst);
		this.ti=ti;
		this.datetime=dateFormat.format(date);
	}

	public PreCourseDiagCode(int ti,String pcdc,String dt, String oucu,String tl)
	{
		if (pcdc != "")
		{
			PreCourseDiagCode=pcdc;
		}
		else
		{
			generateCode(oucu,tl);
		}
		this.ti=ti;
		datetime = (dt != "" ? dt: dateFormat.format(date));
		trafficlights = (tl != "" ? tl : "");
	}

	/**
	 * Generate the object by reading it from the database for this ti.
	 */
	public PreCourseDiagCode(DatabaseAccess.Transaction dat, NavigatorServlet ns, int ti)
			throws Exception
	{
		if (ti == 0)
		{
			throw new Error(BADDBACCESS);
		}
		this.ti = ti;
		this.getPreCourseDiagCodeFromDB(dat, ns, ti);
	}

	public void setTi(int ti)
	{
		this.ti=ti;
	}

	public void setCode(String pcdc)
	{
		PreCourseDiagCode = pcdc;
	}

	public void setTrafficlights(String tl)
	{
		trafficlights = tl;
	}

	/**
	 * generate the code from the date
	 */
	private void generateCode()
	{
		DateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
		Date date = new Date();
		PreCourseDiagCode = dateFormat.format(date);
	}

	/**
	 * Generate the code from a string.
	 */
	private void generateCode(String s)
	{
		PreCourseDiagCode = s;
	}

	/**
	 * Generate the code from the 2 strings.
	 */
	private void generateCode(String s1,String s2)
	{
		PreCourseDiagCode = CANT_DETERMINE_PCDC;
		if (s1 != "")
		{
			// Generate the code and store it in db for this ti.
			if (s2 != "")
			{
				PreCourseDiagCode=s2+SEPARATOR+s1;
			}
			else
			{
				//pcdc=s1+SEPARATOR+dateFormat.format(date);
				PreCourseDiagCode=s1;
			}
		}
		else throw new Error(BADCREATE);
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

	/** Save the code in the database against a ti. */
	public void updateDBwithCode(DatabaseAccess.Transaction dat,OmQueries oq)
			throws Exception
	{
		oq.updateTestPreCourseDiagCode(dat,this);
	}

	public void insertTestPCDC(DatabaseAccess.Transaction dat,OmQueries oq)
			throws Exception
	{
		oq.insertTestPreCourseDiagCode(dat,this);
	}

	/**
	 * Here we check that the test meets the criteria for generating the code
	 * only generate if option set and the test is not finished
	 * @return
	 * @throws UtilityException
	 */
	public static boolean shouldDoCode(UserSession us)
			throws Exception
	{
		return null != us && null != us.getTestDeployment() &&
				us.getTestDeployment().getbPcdc() && !us.isHasGeneratedFinalPCDC();
	}

	public static boolean shouldGenerateCode(UserSession us)
			throws Exception
	{
		return shouldDoCode(us);
	}

	public static boolean shouldGenerateNewCode(UserSession us)
			throws Exception  {
		// Reset the boolean in the usersession.
		us.setHasGeneratedFinalPCDC(false);
		return shouldDoCode(us);
	}

	public static boolean shouldReadCode(UserSession us)
			throws Exception
	{
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
	 * @throws UtilityException
	 * @author sarah wood
	 */
	public static boolean shouldDisplayCode(UserSession us)
			throws Exception
	{
		boolean should = false;
		// Start with a simple, run it if its an open to the world test
		// may extend to having a specific tag.
		if (null != us ? null != us.getTestDeployment() : false) {
			if (us.getTestDeployment().getbDisplayPcdc())
			{
				should = true;
			}
		}
		return should;
	}

	public  void  getPreCourseDiagCodeFromDB(DatabaseAccess.Transaction dat, NavigatorServlet ns, int iTi)
			throws Exception
	{
		ResultSet rs = ns.getOmQueries().queryPreCourseDiagCode(dat,iTi);
		while(rs.next())
		{
			// We should only have one entry per ti.
			this.ti = rs.getInt(1);
			this.PreCourseDiagCode = rs.getString(2);
			this.datetime = rs.getString(3);
			this.trafficlights=rs.getString(4);
		}
	}

	public boolean TrafficlightsIsEmpty()
	{
		return this.trafficlights.isEmpty();
	}
}
