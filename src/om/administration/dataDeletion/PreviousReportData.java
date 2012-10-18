package om.administration.dataDeletion;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PreviousReportData {

	private Integer DBTotalTimesUsed=0;
	private double DBTotalScore=0;
	private Date DBDateQuestionFirstUsed=null;
	private Date DBTimeDataGleaned=null;
	
	
	public void PreviousReportData(Date DBqfu,int DBttu,double DBts,Date DBtdg)throws DataDeletionException
	{
		init(DBqfu,DBttu,DBts,DBtdg);
	}
	
	public void init(Date DBqfu,int DBttu,double DBts,Date DBtdg) 
	            
	{		
		DBTotalTimesUsed=DBttu;
		DBTotalScore=DBts;
		DBDateQuestionFirstUsed=DBqfu;
		DBTimeDataGleaned=DBtdg;
	}
	
	public String getDBTimeDataGleanedAsString()
	{
        DateFormat df = new SimpleDateFormat(DatabaseDeletionReports.DATEFORMATFORDB);
		return df.format(DBTimeDataGleaned);
	}
	
	public String getDBDateQuestionFirstUsedAsString()
	{
        DateFormat df = new SimpleDateFormat(DatabaseDeletionReports.DATEFORMATFORDB);
		return df.format(DBDateQuestionFirstUsed);
	}
	
	public int getDBTotalTimesUsed()
	{
			return DBTotalTimesUsed;
	}
	
	public double getDBTotalScore()
	{
		return DBTotalScore;
	}
	
	public Date getDBDateQuestionFirstUsed()
	{	
		return DBDateQuestionFirstUsed;
	}
	
	public String rptCSVString(boolean doHeadings)
	{
		StringBuilder report=new StringBuilder();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
 		if (doHeadings)	report.append("DBTimeDataGleaned=");
 		String SDBTimeDataGleaned = df.format(DBTimeDataGleaned);
		report.append(SDBTimeDataGleaned+",");
 		if (doHeadings)	report.append("DBDateQuestionFirstUsed=");
 		String SDBDateQuestionFirstUsed = df.format(DBDateQuestionFirstUsed);
		report.append(SDBDateQuestionFirstUsed+",");
		if (doHeadings)	report.append("DBTotalTimesUsed=");
		report.append(DBTotalTimesUsed+",");
		if (doHeadings)	report.append("DBTotalScore=");
		report.append(DBTotalScore+",");
		
		return report.toString();
	}

	
	/* does this objhect contain DB data? */
	public boolean isEmpty() throws DataDeletionException
	{
		boolean notInDB=false;
		notInDB= (DBTimeDataGleaned==null && DBTotalTimesUsed==0 && DBTotalScore==0);
		/* if we think is IT in the DB, check that the data is valid, and timesused is > 0 if the score is >0 */
		if (!notInDB && (DBTimeDataGleaned==null || (DBTotalTimesUsed==0 && DBTotalScore>0)))
		{
			throw new DataDeletionException("invalid data returned from database for question ");
		}
		return notInDB;
	}

	
	public void updateTotals (PreviousReportData prd)  throws DataDeletionException
	{
		try
		{
			this.DBTotalScore=this.DBTotalScore+prd.DBTotalScore;
			this.DBTotalTimesUsed=this.DBTotalTimesUsed+prd.getDBTotalTimesUsed();
			this.DBDateQuestionFirstUsed=DatabaseDeletionUtils.earliestDate(this.DBDateQuestionFirstUsed,prd.getDBDateQuestionFirstUsed());;
			this.DBTimeDataGleaned=prd.DBTimeDataGleaned;

		}
		catch (Exception e)
		{
			throw new DataDeletionException(e);
		}
		
	}
	
}
	
	
