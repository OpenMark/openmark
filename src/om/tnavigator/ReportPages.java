/* OpenMark online assessment system
   Copyright (C) 2007 The Open University

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package om.tnavigator;

import java.lang.reflect.Method;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.*;

import om.axis.qengine.Score;
import om.tnavigator.db.DatabaseAccess;
import util.xml.*;

class ReportPages
{
	private NavigatorServlet ns;
	private Object cmaReport=null;
	private Method handleReportsCMA=null,handleReportsCMAData=null,tidyCMAData=null;
	
	ReportPages(NavigatorServlet ns)
	{
		this.ns=ns;
		try
		{
			// OU-specific CMA system
			Class cmaReportClass=Class.forName("om.cma.CMAReport");
			cmaReport=cmaReportClass.
				getConstructor(new Class[] {NavigatorServlet.class}).
				newInstance(new Object[] {ns});
			handleReportsCMA=cmaReportClass.getMethod("handleReportsCMA",
				new Class[] {UserSession.class,HttpServletRequest.class,HttpServletResponse.class});
			handleReportsCMAData=cmaReportClass.getMethod("handleReportsCMAData",
				new Class[] {UserSession.class,String.class,HttpServletRequest.class,HttpServletResponse.class});
			tidyCMAData=cmaReportClass.getMethod("tidyCMAData",new Class[] {});
		}
		catch(Exception e)
		{
			cmaReport=null;
		}
	}

	void handle(UserSession us,String sSuffix,HttpServletRequest request,HttpServletResponse response)
		throws Exception
	{
		// Require admin privileges AND within university (just for paranoia's sake)
		// AND view report thing
		if(!us.bAdmin || !us.bAllowReports)
		{
			ns.sendError(us,request,response,
				HttpServletResponse.SC_FORBIDDEN,false,null, "Forbidden", "You do not have permission to view reports.", null);
		}
		if(!ns.checkLocalIP(request))
		{
			ns.sendError(us,request,response,HttpServletResponse.SC_FORBIDDEN,
				false,null,"Forbidden", "Reports may only be accessed within the local network.", null);
		}
		
		if(sSuffix.equals("")) 
			handleReportsHome(us,request,response);
		else if(sSuffix.startsWith("user!"))
			handleReportsUser(us,sSuffix.substring("user!".length()),request,response);
		else if(sSuffix.startsWith("question!"))
			handleReportsQuestion(us,sSuffix.substring("question!".length()),request,response);
		else if(cmaReport!=null && sSuffix.equals("cma"))
		{
			handleReportsCMA.invoke(cmaReport,new Object[] {us,request,response});
		}
		else if(cmaReport!=null && sSuffix.startsWith("cmadata!"))
		{
			handleReportsCMAData.invoke(cmaReport,new Object[] {us,
				sSuffix.substring("cmadata!".length()),request,response});
		}
		else
		{
			ns.sendError(null,request,response,HttpServletResponse.SC_NOT_FOUND,
				false,null,"Not found", "No such report.", null);
		}
	}
	
	private static class DbInfoPerson
	{
		String sOUCU,sPI,sDate,sDateFinished,sTime,sTimeFinished;		
	}
	private static class DbInfoQuestion
	{
		String sQuestion,sAverage,sCount;
		int iMajor;
		int iNumber;
	}
	
	private void handleReportsHome(UserSession us,
		HttpServletRequest request,HttpServletResponse response) throws Exception
	{
		if(cmaReport!=null)
		{
			tidyCMAData.invoke(cmaReport,new Object[] {});
		}
			
		List lFinished=new LinkedList(),lUnfinished=new LinkedList(),
			lAdmin=new LinkedList(),lQuestions=new LinkedList();
	
		SimpleDateFormat 
			sdfD=new SimpleDateFormat("dd MMMM yyyy"),
			sdfT=new SimpleDateFormat("HH:mm:ss");
		
		// Query from database for PIs and questions
		DatabaseAccess.Transaction dat=ns.getDatabaseAccess().newTransaction();		
		try
		{
			// Get list of people who did the test. 
			// Show:
			// * Each person only once
			// * Only the most recent finished attempt, or most recent unfinished attempt
			//   if there aren't any finished
			// * Within categories (finished/unfinished), sorting by PI
			// I achieve this by setting the sort order and dropping all but the first
			// result for each PI.
			Set sPIsDone=new HashSet();
			ResultSet rs=ns.getOmQueries().queryTestAttempters(dat,us.sTestID);
			while(rs.next())
			{				
				DbInfoPerson dip=new DbInfoPerson();
				dip.sOUCU=rs.getString(1);
				dip.sPI=rs.getString(2);
				if(sPIsDone.contains(dip.sPI))
					continue;
				Timestamp tsStart=rs.getTimestamp(3);
				dip.sTime=sdfT.format(tsStart);
				dip.sDate=sdfD.format(tsStart);
				Timestamp tsFinished=rs.getTimestamp(6);
				if(tsFinished==null) 
				{
					dip.sTimeFinished="";
					dip.sDateFinished="";
				}
				else 
				{
					dip.sTimeFinished=sdfT.format(tsFinished);
					dip.sDateFinished=sdfD.format(tsFinished);
				}
				int iFinished=rs.getInt(4),iAdmin=rs.getInt(5);
				if(iAdmin==1)
					lAdmin.add(dip);
				else if(iFinished==1)
					lFinished.add(dip);
				else
					lUnfinished.add(dip);
				sPIsDone.add(dip.sPI);
			}
			
			// Get list of questions in test
			rs=ns.getOmQueries().queryQuestionList(dat,us.sTestID);				
			while(rs.next())
			{
				DbInfoQuestion diq=new DbInfoQuestion();
				diq.sQuestion=rs.getString(1);
				if(rs.getInt(3)==0)
				{
					diq.sAverage="-";
					diq.sCount="?";
				}
				else
				{
					diq.sAverage=Math.round(rs.getInt(2)*100.0 / rs.getInt(3))+"";
					while(diq.sAverage.length()<3) diq.sAverage="0"+diq.sAverage;
					diq.sAverage=diq.sAverage.substring(0,diq.sAverage.length()-2)+"."+
						diq.sAverage.substring(diq.sAverage.length()-2);
					diq.sCount=rs.getInt(3)+"";
				}
				diq.iMajor=rs.getInt(4);
				diq.iNumber=rs.getInt(5);
				lQuestions.add(diq);
			}
		}
		finally
		{
			dat.finish();
		}
		
		// Build result
		StringBuffer sb=new StringBuffer("<div class='basicpage'>");
		
		sb.append("<h3>Finished tests ("+lFinished.size()+")</h3>");
		sb.append("<p>Finished tests are those where the user clicked the " +
			"'End test' button to submit their answers.</p>");
		sb.append("<table class='topheaders'><tr><th>PI</th><th>OUCU</th><th>Start date</th><th>Time</th><th>Finish date</th><th>Time</th></tr>");
		for(Iterator i=lFinished.iterator();i.hasNext();)
		{
			DbInfoPerson dip=(DbInfoPerson)i.next();
			sb.append("<tr><td><a href='reports!user!"+dip.sPI+"'>"+dip.sPI+"</a></td>" +
				"<td>"+dip.sOUCU+"</td><td>"+dip.sDate+"</td><td>"+dip.sTime+"</td>" +
				"<td>"+dip.sDateFinished+"</td><td>"+dip.sTimeFinished+"</td></tr>");
		}
		sb.append("</table>");
		
		sb.append("<h3>Unfinished tests ("+lUnfinished.size()+")</h3>");
		sb.append("<table class='topheaders'><tr><th>PI</th><th>OUCU</th><th>Start date</th><th>Time</th></tr>");
		for(Iterator i=lUnfinished.iterator();i.hasNext();)
		{
			DbInfoPerson dip=(DbInfoPerson)i.next();
			sb.append("<tr><td><a href='reports!user!"+dip.sPI+"'>"+dip.sPI+"</a></td>" +
				"<td>"+dip.sOUCU+"</td><td>"+dip.sDate+"</td><td>"+dip.sTime+"</td></tr>");
		}
		sb.append("</table>");
		
		sb.append("<h3>Admin user tests</h3>");
		sb.append("<p>Tests taken by admin users (finished or not) will not be included in any official results.</p>");
		sb.append("<table class='topheaders'><tr><th>PI</th><th>OUCU</th><th>Start date</th><th>Time</th><th>Finish date</th><th>Time</th></tr>");
		for(Iterator i=lAdmin.iterator();i.hasNext();)
		{
			DbInfoPerson dip=(DbInfoPerson)i.next();
			sb.append("<tr><td><a href='reports!user!"+dip.sPI+"'>"+dip.sPI+"</a></td>" +
				"<td>"+dip.sOUCU+"</td><td>"+dip.sDate+"</td><td>"+dip.sTime+"</td>" +
				"<td>"+dip.sDateFinished+"</td><td>"+dip.sTimeFinished+"</td></tr>");
		}
		sb.append("</table>");
		
		sb.append("<h3>Questions in test</h3>");
		sb.append("<p>The following counts include all who attempted a question, " +
			"whether or not they finished the test.</p>");
		sb.append("<table class='topheaders'><tr><th>#</th><th>ID</th><th>Taken by</th><th>Average score</th><th>Out of</th></tr>");
		for(Iterator i=lQuestions.iterator();i.hasNext();)
		{
			DbInfoQuestion diq=(DbInfoQuestion)i.next();
			Score[] as=ns.getMaximumScores(null,diq.sQuestion,
				ns.getLatestVersion(diq.sQuestion,diq.iMajor).toString(),request);
			String sMax="??";
			for(int iScore=0;iScore<as.length;iScore++)
			{
				if(as[iScore].getAxis()==null)
					sMax=""+as[iScore].getMarks();
			}
			sb.append("<tr><td>"+diq.iNumber+"</td><td><a href='reports!question!"+diq.sQuestion+"'>"+diq.sQuestion+"</a>" +
				"</td><td>"+diq.sCount+"</td><td>"+diq.sAverage+"</td><td>"+sMax+"</td></tr>");			
		}
		sb.append("</table>");
		
		if(us.tdDeployment.hasCMAData())
		{
			sb.append("<h3>CMA tools</h3>"+
					"<p><a href='reports!cma'>Extract CMA data</a></p>");
		}
		
		sb.append("<p style='margin-top:2em'><a href='./'>Return to test</a></p>");
		
		sb.append("</div>");
		
		ns.serveTestContent(us,"Reports","",null,null,sb.toString(),false, request, response, true);				
	}
	
	private void handleReportsUser(UserSession us,String sUser,
		HttpServletRequest request,HttpServletResponse response) throws Exception
	{
		// Build result
		StringBuffer sb=new StringBuffer("<div class='basicpage userreport report'>");
		
		SimpleDateFormat sdf=new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");
		
		// Query from database for PIs and questions
		DatabaseAccess.Transaction dat=ns.getDatabaseAccess().newTransaction();		
		try
		{
			// Get session info
			Map mSessionInfo=new HashMap();
			ResultSet rs=ns.getOmQueries().queryUserReportSessions(dat,us.sTestID,sUser);
			while(rs.next()) 
			{
				int iTAttempt=rs.getInt(1);
				Timestamp tsDate=rs.getTimestamp(2);
				String sIP=rs.getString(3);
				String sUserAgent=rs.getString(4);
				
				Integer iKey=new Integer(iTAttempt);
				String sOutput=(String)mSessionInfo.get(iKey);
				if(sOutput==null) sOutput=
					"<div class='sessions'>" +
					"<table class='topheaders'>"+
					"<tr><th class='time'>Time</th><th class='ip'>IP address</th><th class='useragent'>User agent</th></tr>"+
					"</table></div>";
				sOutput=sOutput.substring(0,sOutput.length()-"</table></div>".length());
				sOutput+="<tr><td class='time'>"+sdf.format(tsDate)+
					"</td><td class='ip'>"+sIP+"</td><td class='useragent'>"+
					XHTML.escape(sUserAgent, XHTML.ESCAPE_TEXT)+"</td></tr></table></div>";
				mSessionInfo.put(iKey,sOutput);
			}
			
			rs=ns.getOmQueries().queryUserReportTest(dat,us.sTestID,sUser);
			
			int iCurrentTAttempt=0;
			String sCurrentQuestion=null;
			int iCurrentQAttempt=0;
			
			boolean bInTest=false,bInQuestion=false;
			while(rs.next())
			{
				int iTAttempt=rs.getInt(1);
				int iFinished=rs.getInt(2);
				String sQuestion=rs.getString(3);
				int iQAttempt=rs.getInt(4);
				Timestamp tsDate=rs.getTimestamp(5);
				String sQuestionLine=rs.getString(6);
				String sAnswerLine=rs.getString(7);
				String sActionSummary=rs.getString(8);
	  		int iAttempts=rs.getInt(9);
		  	String sAxis=rs.getString(10);
	  		String sAxisScore=rs.getString(11);
				int iQNumber=rs.getInt(12);
				Timestamp tsFinished=rs.getTimestamp(13);
				Timestamp tsMinAction=rs.getTimestamp(14);
				Timestamp tsMaxAction=rs.getTimestamp(15);
				
				if(iTAttempt!=iCurrentTAttempt)
				{
					iCurrentTAttempt=iTAttempt;
					if(bInQuestion)
					{
						sb.append("</div></div>");
						bInQuestion=false;
					}
					if(bInTest) sb.append("</div>");
					
					sb.append("<div class='tattempt'><h3>Test attempt "+iCurrentTAttempt+
						" ("+(iFinished==0?"Unfinished":"Finished on "+(tsFinished==null ? "[date not available] " : sdf.format(tsFinished))
								)+")</h3>");
					bInTest=true;
					
					String sSessionInfo=(String)mSessionInfo.get(new Integer(iTAttempt));
					if(sSessionInfo!=null) sb.append(sSessionInfo);
				}
				
				if(!sQuestion.equals(sCurrentQuestion) || iQAttempt!=iCurrentQAttempt)
				{
					iCurrentQAttempt=iQAttempt;
					if(bInQuestion) sb.append("</div></div>");
					
					sb.append("<div class='qattempt'><h4>#"+iQNumber+" ("+sQuestion+") attempt "+
						iCurrentQAttempt+"</h4>"+
		  		  "<div class='started'>Access time: <em>"+sdf.format(tsDate)+"</em></div>"+
		  		  "<div class='started'>Action times: <em>"+sdf.format(tsMinAction)+"</em> - <em>"+sdf.format(tsMaxAction)+"</em></div>"+
		  		  "<div class='question'>Question: <em>"+XML.escape(sQuestionLine)+"</em></div>"+
		  		  "<div class='answer'>User's answer: <em>"+XML.escape(sAnswerLine)+"</em></div>"+
		  		  "<pre>"+XML.escape(sActionSummary)+"</pre>"+
		  		  "<div class='attempts'>Result: <em>");
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
					bInQuestion=true;
				}
				
				if(sAxis!=null)
		  	{
					sb.append("<div><span class='axis'>"+
		  		  (sAxis.equals("") ? "Default" : sAxis)+
		  		  ": </span><span class='val'>"+sAxisScore+"</span></div>");
		  	}				
			}
			if(bInQuestion) sb.append("</div></div>");
			if(bInTest) sb.append("</div>");
		}
		finally
		{
			dat.finish();
		}
	
		sb.append("<p><a href='reports!'>Back to reports home</a></p>");
		sb.append("</div>");
		ns.serveTestContent(us,"Reports: "+sUser,"",null,null,sb.toString(),false, request, response, true);				
	}
	
	private void handleReportsQuestion(UserSession us,String sQuestion,
		HttpServletRequest request,HttpServletResponse response) throws Exception
	{
		// Build result
		StringBuffer sb=new StringBuffer("<div class='basicpage questionreport report'>");
		
		SimpleDateFormat sdf=new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");
		
		// Query from database for PIs and questions
		DatabaseAccess.Transaction dat=ns.getDatabaseAccess().newTransaction();		
		try
		{
		  ResultSet rs=ns.getOmQueries().queryQuestionReport(dat,us.sTestID,sQuestion);		  
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
