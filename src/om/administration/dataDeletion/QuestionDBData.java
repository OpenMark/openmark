package om.administration.dataDeletion;

import java.util.Date;

/* holds the details of a question read from the database */

public class QuestionDBData {

	private String QuestionName="";

	private Date TimeUsed=null;

	private String qi="";
	
	private Boolean isFinished=false;
	
	private double score=0;

	public QuestionDBData(String qn, Date dt,String qi, boolean isfin,double score) throws DataDeletionException
	{
		if (qn.isEmpty() || qi.isEmpty() || dt== null )
			throw new DataDeletionException("Unabble to initialise QuestionAverageData"); 
		QuestionName=qn;
		TimeUsed=dt;
		isFinished=isfin;
		this.qi=qi;
		this.score=score;
	}
	
	public void setQuestionName(String qn) throws DataDeletionException
	{
		if (qn.isEmpty() )
			throw new DataDeletionException("Unable to set QuestionAverageData QuestionName "+qn); 
		QuestionName=qn;
	}

	
	public void setQI(String qi) throws DataDeletionException
	{
		if (qi.isEmpty() )
			throw new DataDeletionException("Unabble to set QuestionAverageData qi "+qi); 
		this.qi=qi;
	}
	
	public void setTimeUsed(Date dt) throws DataDeletionException
	{
		if (dt==null )
			throw new DataDeletionException("Unabble to set QuestionAverageData TimeUsed "+dt.toString()); 
		TimeUsed=dt;
	}
	
	public void setisFinished(boolean isfin)
	{
		isFinished=isfin;
	}
	
	public String getQuestionName()
	{
		return QuestionName;
	}
	
	public String getQI()
	{
		return qi;
	}
	public boolean getIsFinished()
	{
		return isFinished;
	}
	
	public Date getTimeUsed()
	{
		return TimeUsed;
	}
	
	
	public double getScore()
	{
		return score;
	}
	
}

