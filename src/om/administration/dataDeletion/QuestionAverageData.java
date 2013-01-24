package om.administration.dataDeletion;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import om.Log;
import om.abstractservlet.AbstractRequestHandler;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.db.DatabaseAccess;


/* holds the details of a question read from the database */

public class QuestionAverageData extends AbstractRequestHandler {

	private static final long serialVersionUID = 3432094601409857406L;

	private String QuestionName="";
	private Date TimeFirstUsed=null;
	private Date TimeLastUsed=null;
	private double TotalScore=0.0;
	private Integer TimesUsed=0;

	private PreviousReportData DBData=new PreviousReportData();
	
	private String AxisName="";


	private static String NEWTOTALSCORESSQL="INSERT INTO {6}questionstats (question,totalscore,timesused,timedatagleaned,axis,timefirstused)" +
	"VALUES({0},{1},{2},{3},{4},{5})";
	private static String UPDATETOTALSCORESSQL="UPDATE {6}questionstats " +
	"SET totalscore={1}, timesused={2},timedatagleaned={3},axis={4},timefirstused={5} where question={0}";

	public QuestionAverageData() throws DataDeletionException
	{

	}
	
	public QuestionAverageData(QuestionAverageData qad) throws DataDeletionException
	{
		this.QuestionName=qad.QuestionName;
		this.TimeLastUsed=qad.TimeLastUsed;
		this.TotalScore=qad.TotalScore;
		this.TimesUsed=qad.TimesUsed;
		this.DBData=qad.DBData;
		this.AxisName=qad.AxisName;
		this.TimeFirstUsed=qad.TimeFirstUsed;
	}
	
	/* create new instance ready polulated */
	public QuestionAverageData(String qn, Date tlu, double ts, int tu,Date tfu,String an,
			int DBttu,double DBts, Date DBqfu,Date DBtdg) throws DataDeletionException
	{
		if (qn.isEmpty() || tlu== null || tfu==null )
			throw new DataDeletionException("Unable to initialise QuestionAverageData"); 
		try
		{
			QuestionName=qn;
			TimeLastUsed=tlu;
			TotalScore=ts;
			TimesUsed=tu;
			DBData.init(DBqfu,DBttu,DBts,DBtdg);
			AxisName=an;
			TimeFirstUsed=tfu;
		}
		catch (Exception x)
		{
			throw new DataDeletionException("Unable to initilise QuestionAverageData QuestionName: "+x); 

		}
		


	}
	
	public void setQuestionName(String qn) throws DataDeletionException
	{
		if (qn.isEmpty() )
			throw new DataDeletionException("Unable to set QuestionAverageData QuestionName "+qn); 
		QuestionName=qn;
	}

	
	public void setTimeLastUsed(Date tlu) throws DataDeletionException
	{
		TimeLastUsed=tlu;
	}
	
	public void setTotalScore(double ts) throws DataDeletionException
	{
		TotalScore=ts;
	}
	
	public void setTimesUsed(Integer tu)
	{
		TimesUsed=tu;
	}
	
	public String getQuestionName()
	{
		return QuestionName;
	}
	
	public Integer getTimesUsed()
	{
		return TimesUsed;
	}
	
	public Date getTimeLastUsed()
	{
		return TimeLastUsed;
	}
	
	
	public Date getTimeFirstUsed()
	{
		return TimeFirstUsed;
	}
	
	public String getDateAsString(Date dt)
	{
        DateFormat df = new SimpleDateFormat(DatabaseDeletionReports.DATEFORMATFORDB);
		return df.format(dt);
	}
	
	public Double getTotalScore()
	{
		return TotalScore;
	}
	
	public PreviousReportData getDBData()
	{
		return DBData;
	}
	
	
	/* return the average score over this question */
	public double getAverageScore() throws DataDeletionException
	{
		double avg=0;
		try
		{
			if (TimesUsed > 0)
			{
				avg=TotalScore/(double) TimesUsed;
			}
		}
		catch (Exception x)
		{
			throw new DataDeletionException(x);
		}
		return avg;
		
	}
	/* increment the timesused */
	public void inc()
	{
		TimesUsed++;
	}
	/* increment the timesused */
	public void inc(Integer x)
	{
		TimesUsed=TimesUsed+x;
	}
	
	/*Add score to the total already held */
	public void UpdateTotalScore(double score)
	{
		TotalScore=TotalScore+score;
	}
	
	/* if the specified time is later than the one held, update the time last used */
	public void UpdateTimeLastUsed(Date timeused)throws DataDeletionException
	{
		try
		{
			if (timeused.after(TimeLastUsed))
			{
				TimeLastUsed=timeused;
			}
			
		}
		catch(Exception x)
		{
			throw new DataDeletionException(x);
		}
	}
	
	public static String rptHeadingsAsHTML(boolean noAxis)
	{
		return generateHeadingsHTML(noAxis);
	}
	public static String rptHeadingsAsHTML()
	{
		/* if we dont specify whether to do axis data - we do it */
		return generateHeadingsHTML(false);

	}
	/* output the headings for the text report */
	public static String generateHeadingsHTML(boolean noAxis)
	{
		StringBuilder report=new StringBuilder();
		report.append("<tr align='left'><th align='left'>");
		report.append("Name"+"</th><th align='left'>");
		if (!noAxis)
		{
			report.append("Axis"+"</th><th align='left'>");
		}
		report.append("Average score "+"</th><th align='left'>");
		report.append("Times Used "+"</th><th align='left'>");
		report.append("Total Score "+"</th><th align='left'>");
		report.append("Date First Used "+"</th><th align='left'>");
		report.append("Date last Used"+"</th></tr>");

		return report.toString();
		
	}
	/* genherate the data as html */
	public String rptAsHTML(boolean noAxis) throws DataDeletionException
	{
		try
		{
			return GenerateHTML(noAxis);
		}
		catch (DataDeletionException e)
		{
			throw new DataDeletionException(e);
		}
	}
	
	public String rptAsHTML() throws DataDeletionException
	{
		try
		{
			/* if we dont specify whether to do axis data - we do it */
			return GenerateHTML(false);
		}
		catch (DataDeletionException e)
		{
			throw new DataDeletionException(e);
		}
	}

	public String GenerateHTML(boolean noAxis) throws DataDeletionException
	{
		StringBuilder report=new StringBuilder();
		try
		{
			report.append("<tr><td>");
			report.append(QuestionName+",</td><td>");
			if(!noAxis)
			{
				report.append(AxisName+",</td><td>");
			}
			/* limit it to 2 decimal places */
	        DecimalFormat dblFmt = new DecimalFormat("#.##");
			report.append(dblFmt.format(getAverageScore())+",</td><td>");
			report.append(TimesUsed+",</td><td>");
			report.append(TotalScore+",</td><td>");
	        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	        String TimeLastUsedAsString = df.format(TimeLastUsed);
			String TimeFirstUsedAsString=df.format(getEarliestTimeFirstUsed());
			report.append(TimeFirstUsedAsString+",</td><td>");

			report.append(TimeLastUsedAsString+"</td></tr>");
		}
		catch (Exception e)
		{
			throw new DataDeletionException(e);
		}
		return report.toString();
		
	}
/* returns true if the axis is blank */	
	public boolean isBlankAxis()
	{
		return this.AxisName.isEmpty();
	}
	
	/* generate a string, used by logging. If the headings is */
	public String rptCSV(boolean doHeadings)
	{
		if (doHeadings) 
		{
			return rptCSVString(true);
		}
		else
		{
			return rptCSVString(false);
		}
	}
	/* generate a string, used by logging. If the headings is */
	public String rptCSV()
	{
		return rptCSVString(false);
	}
	
	/* this is called by both the CSV ,methods */
	private String rptCSVString(boolean doHeadings)
	{
		StringBuilder report=new StringBuilder();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");		
		
		if (doHeadings)	report.append("QuestionName=");
		report.append(QuestionName+",");
		if (doHeadings)	report.append("AxisName=");
		report.append(AxisName+",");
		/* ouptut the data from the previous report*/
		report.append(DBData.rptCSVString(doHeadings));
		
		/* ne do the new data */
		if (doHeadings)	report.append("TotalScore=");
		report.append(TotalScore+",");
		if (doHeadings)	report.append("TimesUsed=");
		report.append(TimesUsed+",");

        String TimeFirstUsedAsString = df.format(TimeFirstUsed);
        if (doHeadings)	report.append("TimeFirstUsedAsString");
        report.append(TimeFirstUsedAsString+",");
        
        String TimeLastUsedAsString = df.format(TimeLastUsed);
        if (doHeadings)	report.append("TimeLastUsedAsString");
        report.append(TimeLastUsedAsString+",");
        
        
		return report.toString();
		
	}

	/* find out if the new first date is beforethe one recorded or after it, and return the earliest */
	public Date getEarliestTimeFirstUsed()throws DataDeletionException
	{
	
		if ((DBData.getDBDateQuestionFirstUsed() == null) && TimeFirstUsed==null)
		{
			throw new DataDeletionException("Unable to " +
					"determin earliest time as biooth dates null");
		}
		if (DBData.getDBDateQuestionFirstUsed() == null)
		{			
			return TimeFirstUsed ;
		}
		else			
		{
			if (DBData.getDBDateQuestionFirstUsed().before(TimeFirstUsed))
			{			
				return DBData.getDBDateQuestionFirstUsed();
			}
			else
			{
				return TimeFirstUsed ;
			}
		}
	}

	
	/* up[date the database with the details */
	public void updateDBWithTotalsData(DatabaseAccess.Transaction dat, NavigatorConfig nc,String sNow) throws DataDeletionException
	{
		try
		{
			/* if we had a value already in the database then it should be set in the object, so check it */
			/*  make up the sql string by checking whether its an insert or an uupdate */
			/* first we have to calculate the values */
			
			Double ts=TotalScore+DBData.getDBTotalScore();
			int tu=TimesUsed+DBData.getDBTotalTimesUsed();
			String tfu=getDateAsString(getEarliestTimeFirstUsed());
			
			Object[] arguments = {"'"+QuestionName+"'","'"+ts+"'","'"+tu+"'","'"+
					sNow+"'","'"+AxisName+"'","'"+tfu+"'",nc.getDBPrefix()};
			String sqlstr="";
			boolean doInsert=NotInDB();
			
			if (doInsert)
			{
				sqlstr=MessageFormat.format(NEWTOTALSCORESSQL, arguments);
			}
			else
			{
				sqlstr=MessageFormat.format(UPDATETOTALSCORESSQL, arguments);
			}
			doUpdate(sqlstr,dat);

		}
		catch(Exception e)
		{
			throw new DataDeletionException(e);
		}
	}
	
	/* does this objhect contain DB data? */
	private boolean NotInDB() throws DataDeletionException
	{
		
		return DBData.isEmpty();
	}
	
	/* applies update to the DB */
	private void doUpdate(String sqlstr,DatabaseAccess.Transaction dat) throws DataDeletionException
	{
		if (!sqlstr.isEmpty())
		{
			try
			{
				dat.update(sqlstr);		
			}
			catch (Exception e)
			{
				throw new DataDeletionException("Unable to perform DB update/insert for "+QuestionName+" "+sqlstr+" :"+e);
			}
		}
		else
		{
			throw new DataDeletionException("DB update/insert string empty for "+QuestionName);
		}
	}

	/* write the value to the log file */
	public void logIt(Log l) throws DataDeletionException
	{
	
		try
		{
			/* put an entry in the log file for this object */
			l.logNormal(rptCSV(true));
		}
		catch (Exception e)
		{
		}
	}
	
	public boolean sameQuestionName(QuestionAverageData qad)
	{
		return (this.QuestionName.equals(qad.getQuestionName()));
		
	}

	
	
	public void updateTotals (QuestionAverageData qad)  throws DataDeletionException
	{
		try
		{
			this.QuestionName=qad.QuestionName;
			this.TotalScore=this.TotalScore+qad.TotalScore;
			this.TimesUsed=this.TimesUsed+qad.getTimesUsed();			
			this.TimeFirstUsed=DatabaseDeletionUtils.earliestDate(this.TimeFirstUsed,qad.getTimeFirstUsed());
			this.TimeLastUsed=DatabaseDeletionUtils.latestDate(this.TimeLastUsed,qad.getTimeLastUsed());
			this.DBData.updateTotals(qad.getDBData());

		}
		catch (Exception e)
		{
			throw new DataDeletionException(e);
		}
		
	}
	
	
}

