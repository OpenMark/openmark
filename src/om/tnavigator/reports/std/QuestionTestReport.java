/**
 * 
 */
package om.tnavigator.reports.std;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.tnavigator.*;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.reports.OmTestReport;
import util.xml.XML;

/**
 * @author tjh238
 *
 */
public class QuestionTestReport implements OmTestReport {
	NavigatorServlet ns;
	
	/**
	 * Create an instance of this report.
	 * @param ns the navigator servlet we belong to.
	 */
	public QuestionTestReport(NavigatorServlet ns) {
		this.ns = ns;
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#getUrlTestReportName()
	 */
	public String getUrlTestReportName() {
		return "question";
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#getReadableReportName()
	 */
	public String getReadableReportName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#isApplicable(om.tnavigator.TestDeployment)
	 */
	public boolean isApplicable(TestDeployment td) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#handleTestReport(om.tnavigator.UserSession, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void handleTestReport(UserSession us, String sQuestion,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// Build result
		StringBuffer sb=new StringBuffer("<div class='basicpage questionreport report'>");
		
		SimpleDateFormat sdf=new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");
		
		// Query from database for PIs and questions
		DatabaseAccess.Transaction dat=ns.getDatabaseAccess().newTransaction();		
		try
		{
		  ResultSet rs=ns.getOmQueries().queryQuestionReport(dat,us.getTestId(),sQuestion);		  
		  String sCurrentPI=null;
		  int iCurrentAttempt=0;
		  boolean bInPI=false,bInAttempt=false;
		  while(rs.next())
		  {
		  	String sPI=rs.getString(1);
				if(!sPI.equals(sCurrentPI))
		  	{
		  		if(bInAttempt) 
		  		{		  			
		  			sb.append("</div></div>");
		  			bInAttempt=false;
		  		}
		  		if(bInPI) sb.append("</div>");
		  		sCurrentPI=sPI;
		  		sb.append("<div class='pi'><h3>"+sCurrentPI+" ("+rs.getString(2)+")</h3>");
		  		iCurrentAttempt=0;
		  		bInPI=true;  
		  	}
		  	if(rs.getInt(3)!=iCurrentAttempt)
		  	{
		  		if(bInAttempt) sb.append("</div></div>");
		  		sb.append(
		  			"<div class='attempt'>"+
		  		  "<div class='started'>Started: <em>"+sdf.format(rs.getTimestamp(4))+"</em></div>"+
		  		  "<div class='question'>Question: <em>"+XML.escape(rs.getString(5))+"</em></div>"+
		  		  "<div class='answer'>Answer: <em>"+XML.escape(rs.getString(6))+"</em></div>"+
		  		  "<pre>"+XML.escape(rs.getString(7))+"</pre>"+
		  		  "<div class='attempts'>Result: <em>");
		  		int iAttempts=rs.getInt(8);
					switch(iAttempts)
		  		{
		  		case -1: sb.append("Wrong"); break;
		  		case 0 : sb.append("Pass"); break;
		  		case 1 : sb.append("1<sup>st</sup>"); break;
		  		case 2 : sb.append("2<sup>nd</sup>"); break;
		  		case 3 : sb.append("3<sup>rd</sup>"); break;
		  		default: sb.append(iAttempts+"<sup>th</sup>"); break;
		  		}
		  		sb.append("</em></div><div class='scores'><span class='t'>Score:</span>");
		  		bInAttempt=true;
		  	}
		  	
		  	String sAxis=rs.getString(9);
				if(sAxis!=null)
		  	{
		  		sb.append("<div><span class='axis'>"+
		  		  (sAxis.equals("") ? "Default" : sAxis)+
		  		  ": </span><span class='val'>"+rs.getString(10)+"</span></div>");
		  	}
		  }
			if(bInAttempt) sb.append("</div></div>");
			if(bInPI) sb.append("</div>");
		}
		finally
		{
			dat.finish();
		}
		
		sb.append("<p><a href='reports!'>Back to reports home</a></p>");
		sb.append("</div>");
		
		ns.serveTestContent(us,"Reports: "+sQuestion,"",null,null,sb.toString(),false, request, response, true);				
	}

}
