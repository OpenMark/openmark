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

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.Log;
import om.OmUnexpectedException;
import om.OmVersion;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.util.IPAddressCheckUtil;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import util.misc.IO;
import util.misc.RequestHelpers;
import util.misc.Strings;
import util.xml.XHTML;
import util.xml.XML;

class StatusPages {

	private static String PRE_PROCESSOR = "PRE_PROCESSOR";

	private NavigatorServlet ns;
	StatusPages(NavigatorServlet ns)
	{
		this.ns=ns;
	}

	void handle(String sSuffix,HttpServletRequest request,HttpServletResponse response)
		throws Exception
	{
		if (!IPAddressCheckUtil.checkTrustedIP(request, ns.getLog(), ns.getNavigatorConfig())) {
			ns.sendError(null,request,response,HttpServletResponse.SC_FORBIDDEN,
				false,false,null, "Forbidden", "This page may only be accessed within the internal network.", null);
		}

	  if(sSuffix.equals(""))
	  		handleStatusHome(request,response);
	  else if(sSuffix.startsWith("stats-"))
	  		handleStatusStats(sSuffix.substring("stats-".length()),request,response);
	  else if(sSuffix.startsWith("log-"))
	  		handleStatusLog(sSuffix.substring("log-".length()),request,response);
	  else if(sSuffix.equals("check"))
	  		handleStatusCheck(request,response);
	  else
		  	ns.sendError(null,request,response,HttpServletResponse.SC_NOT_FOUND,
		  		false,false,null, "Not found", "The requested URL does not correspond to a status page.", null);
	}

	private final static int MB=1024*1024;
	private final static int FREEMEMORYREQ=3*MB;

	/**
	 * Produces automated system status check page, usually requested once a
	 * minute or so to check it's working. Returns a status code 200 and string
	 * beginning 'OK.' if it's OK, and 500 with other text if it's not.
	 * <p>
	 * Defined error codes:
	 * <dl>
	 * <dd>TNFAIL1</dd><dt>Low on memory, probable memory leak. Check and
	 *   restart Tomcat if necessary</dt>
	 * <dd>TNFAIL2</dd><dt>Failure to contact database. Check database is
	 *   working (by accessing it in Query Analyzer or something) and accessible
	 *   to the server (check firewall permissions).
	 *   Restart of webapp is also worth trying, but unlikely to help.</dt>
	 * <dd>TNFAIL3</dd><dt>Failure to contact question engines. Check at least
	 *   one question engine is working and accessible.
	 *   Restart of webapp is also worth trying, but unlikely to help.</dt>
	 * </dl>
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void handleStatusCheck(HttpServletRequest request,HttpServletResponse response)
		throws Exception
	{
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		PrintWriter pw=response.getWriter();

		try
		{
			String sInfo="";

			// 1. Check free memory
			Runtime r=Runtime.getRuntime();
			if(r.freeMemory() < FREEMEMORYREQ)
			{
				System.gc();
				if(r.freeMemory() < FREEMEMORYREQ)
				{
					System.gc();
					if(r.freeMemory() < FREEMEMORYREQ)
					{
						throw new Exception("TNFAIL1. Memory low ("+
							((r.totalMemory()-r.freeMemory())/MB)+"MB used; "+(r.freeMemory()/MB)+"MB free)");
					}
				}
			}
			sInfo+="Memory used "+((r.totalMemory()-r.freeMemory())/MB)+"MB; free "+(r.freeMemory()/MB)+"MB. ";

			// 2. Check database connection
			long lStart=System.currentTimeMillis();
			DatabaseAccess.Transaction dat=ns.getDatabaseAccess().newTransaction();
			try
			{
				ns.getOmQueries().checkDatabaseConnection(dat);
			}
			catch(Throwable t)
			{
				throw new Exception("TNFAIL2. Database error ("+t.getMessage()+")");
			}
			finally
			{
				dat.finish();
			}
			long lTime=System.currentTimeMillis()-lStart;
			sInfo+="DB time "+lTime+"ms. ";

			// 3. Check question engines
			try
			{
				sInfo+="QE time: "+ns.checkOmServiceAvailable()+"ms. ";
			}
			catch(Throwable t)
			{
				throw new Exception("TNFAIL3. Question engine error ("+t.getMessage()+")");
			}

			// Add version
			sInfo+=OmVersion.getVersion()+" ("+OmVersion.getBuildDate()+").";

			// OK then, set status to OK (default, but anyway)
			response.setStatus(HttpServletResponse.SC_OK);
			pw.println("OK. "+sInfo);
		}
		catch(Throwable t)
		{
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			pw.print(t.getMessage()==null ? t.getClass().toString() : t.getMessage());
		}
		finally
		{
			pw.close();
		}
	}

	private void handleStatusLog(String sDate,HttpServletRequest request,HttpServletResponse response)
		throws Exception
	{
		File fLog=new File(ns.getLog().getLogFolder(),"navigator."+sDate+".log");
		if(fLog.length() > 1024*1024)
		{
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			OutputStream os=response.getOutputStream();
			os.write("[Log too large to format: displaying as plaintext]\n\n".getBytes("UTF-8"));
			IO.copy(new FileInputStream(fLog),os,true);
		}
		else
		{
			String sStart=IO.loadString(	new FileInputStream(
				  ns.getServletContext().getRealPath("WEB-INF/templates/statuslog.xhtml.start")));
			Map<String,String> mReplace=new HashMap<String,String>();
			mReplace.put("DATE",sDate);
			mReplace.put("ACCESS", RequestHelpers.getAccessCSSAppend(request));
			URL uThis=ns.getNavigatorConfig().getThisTN();
			mReplace.put("MACHINE",uThis.getHost().replaceAll(".open.ac.uk","")+uThis.getPath());
			sStart=XML.replaceTokens(sStart,"%%",mReplace);

			new LogBuilder(fLog,sStart,request,response);
		}
	}

	static class LogBuilder implements LogProcessorHandler
	{
		private PrintWriter pw=null;
		private Element eParent=null;

		private String sStart;

		LogBuilder(File f,String sStart,HttpServletRequest request,HttpServletResponse response) throws IOException
		{
			XHTML.setContentType(request,response);
			pw=response.getWriter();
			this.sStart=sStart; // Don't write this now, in case there's an error
			new LogProcessor(f,this);
		}

		LogBuilder(Document d,Element eParent)
		{
			this.eParent=eParent;
			Element[] ae=XML.getChildren(d.getDocumentElement());
			for (int i = 0; i < ae.length; i++)
			{
				try
				{
					entry(ae[i]);
				}
				catch(SAXException se)
				{
					throw new OmUnexpectedException(se);
				}
			}
		}

		public void start(Element e) throws SAXException
		{
			pw.print(sStart);
		}

		public void finish() throws SAXException
		{
			pw.print("</body></html>");
			pw.close();
		}

		public void entry(Element e) throws SAXException
		{
			try
			{
				String sLine="<div class='l "+e.getAttribute("severity").charAt(0)+"'>"+
					"<div class='f'><div class='t'>"+e.getAttribute("time")+"</div>"+
					"<div>"+e.getAttribute("category")+"</div></div>"+
					"<div class='m'>"+XHTML.escape(XML.getText(e),XHTML.ESCAPE_TEXT)+"</div>"+
					(XML.hasChild(e,"exception") ?
						"<pre>"+XHTML.escape(XML.getText(XML.getChild(e,"exception")),XHTML.ESCAPE_TEXT)+"</pre>" : "")+
					"</div>";

				if(pw!=null)
				{
					pw.println(sLine);
				}
				else
				{
					eParent.appendChild(
						eParent.getOwnerDocument().importNode(
							XML.parse(sLine).getDocumentElement(),true));
				}
			}
			catch(IOException e1)
			{
				throw new SAXException(e1); // This can't happen but whatever
			}
		}
	}

	private void handleStatusStats(String sDate,HttpServletRequest request,HttpServletResponse response)
		throws Exception
	{
		File fLog=new File(ns.getLog().getLogFolder(),"navigator."+sDate+".log");
		Document d=XML.parse(	new FileInputStream(
		  ns.getServletContext().getRealPath("WEB-INF/templates/statusstats.xhtml")));

		Map<String,String> mReplace=new HashMap<String,String>();
		mReplace.put("DATE",sDate);
		mReplace.put("ACCESS", RequestHelpers.getAccessCSSAppend(request));
		URL uThis=ns.getNavigatorConfig().getThisTN();
		mReplace.put("MACHINE",uThis.getHost().replaceAll(".open.ac.uk","")+uThis.getPath());

		StatsBuilder sb=new StatsBuilder(fLog,ns.getLog());
		sb.fillMap(mReplace);

		XML.replaceTokens(d,mReplace);

		XHTML.output(d,request,response,"en");
	}

	/**
	 * main method for testing.
	 * @param args
	 */
	public static void main(String[] args)
	{
//		System.out.println(StatsBuilder.REQUEST.matcher(
//				"rc4489,[total=62,qe=0,db=0],[ind=0,seq=0],/s151.pa/").matches());
		StatTracker st=new StatTracker();
		st.add(3);
		st.add(4);
		st.add(5);
		st.finish();
		System.out.println(st.mean()+","+st.median()+","+st.percentile(95));
	}

	static class StatTracker
	{
		private final static int BUFFERSIZE=16384; // 64KB
		private int[] aiBuffer=new int[BUFFERSIZE];
		private int iSize=0;
		private boolean bFinished=false;

		void add(int iStat)
		{
			assert !bFinished;
			if(iSize>=aiBuffer.length)
			{
				int[] aiNew=new int[aiBuffer.length+BUFFERSIZE];
				System.arraycopy(aiBuffer,0,aiNew,0,iSize);
				aiBuffer=aiNew;
			}
			aiBuffer[iSize++]=iStat;
		}

		void finish()
		{
			int[] aiNew=new int[iSize];
			System.arraycopy(aiBuffer,0,aiNew,0,iSize);
			aiBuffer=aiNew;

			Arrays.sort(aiBuffer);
			bFinished=true;
		}

		int mean()
		{
			assert bFinished;
			int iTotal=0;
			for(int i=0;i<aiBuffer.length;i++)
			{
				iTotal+=aiBuffer[i];
			}
			return iTotal / aiBuffer.length;
		}

		int median()
		{
			return percentile(50);
		}

		int percentile(int iPercent)
		{
			assert bFinished;
			return aiBuffer[ (iPercent * aiBuffer.length) / 100 ];
		}

	}

	static class StatsBuilder implements LogProcessorHandler
	{
		private Set<String> sOUCUs=new TreeSet<String>();
		private int iRequests=0,iTestRequests=0;
		private String sCurrentMinute="",sMaxMinuteR="",sMaxMinuteTR="";
		private int iCMRequests,iCMTestRequests;
		private int iMaxMRequests=0,iMaxMTestRequests=0;

		private StatTracker stTotal=new StatTracker(),stQE=new StatTracker(),stDB=new StatTracker();

		//private Log l; //temp
		StatsBuilder(File f,Log l) throws IOException
		{
			//this.l=l;
			new LogProcessor(f,this);
		}

		private static Pattern REQUEST=Pattern.compile(
				"^(.*?),\\[total=([0-9]+),qe=([0-9]+),db=([0-9]+)\\],\\[(?:ind=([0-9]+),seq=([0-9]+)|\\?)\\],(.*)$");
		private static Pattern REQUEST_TESTPATH=Pattern.compile(
				"^/[^/]+/$");

		public void entry(Element e) throws SAXException
		{
			if("Request".equals(e.getAttribute("category")))
			{
				Matcher m=REQUEST.matcher(XML.getText(e));
				if(m.matches())
				{
					// Check minute
					String sMinute=e.getAttribute("time").substring(0,5);
					if(!sMinute.equals(sCurrentMinute))
					{
						if(iCMRequests > iMaxMRequests)
						{
							iMaxMRequests=iCMRequests;
							sMaxMinuteR=sCurrentMinute;
						}
						if(iCMTestRequests > iMaxMTestRequests)
						{
							iMaxMTestRequests=iCMTestRequests;
							sMaxMinuteTR=sCurrentMinute;
						}
						iCMRequests=0;
						iCMTestRequests=0;
						sCurrentMinute=sMinute;
					}

					iRequests++;
					iCMRequests++;

					String sOUCU=m.group(1);
					int
						iTotalTime=Integer.parseInt(m.group(2)),
						iQETime=Integer.parseInt(m.group(3)),
						iDBTime=Integer.parseInt(m.group(4));
					String
						sPath=m.group(7);

					if(!sOUCU.matches("\\?+"))
					{
						// Logged-in request
						sOUCUs.add(sOUCU);

						if(REQUEST_TESTPATH.matcher(sPath).matches())
						{
							iTestRequests++;
							iCMTestRequests++;
							stTotal.add(iTotalTime);
							stQE.add(iQETime);
							stDB.add(iDBTime);
							//l.logDebug("STATS",sOUCU+" "+iTotalTime);
						}
					}

				}
			}
		}

		public void start(Element e) throws SAXException
		{
		}

		public void finish() throws SAXException
		{
			stTotal.finish();
			stDB.finish();
			stQE.finish();
		}

		void fillMap(Map<String,String> mReplace)
		{
			mReplace.put("USERS",""+sOUCUs.size());
			mReplace.put("REQUESTS",""+iRequests);
			mReplace.put("TESTREQUESTS",""+iTestRequests);
			StringBuffer sbOUCUs=new StringBuffer();
			boolean bFirst=true;
			for(String sOUCU : sOUCUs)
			{
				if(bFirst)
					bFirst=false;
				else
					sbOUCUs.append(' ');
				sbOUCUs.append(sOUCU);
			}
			mReplace.put("OUCUS",sbOUCUs.toString());

			mReplace.put("PERFTOTALMEAN",""+stTotal.mean());
			mReplace.put("PERFTOTALMEDIAN",""+stTotal.median());
			mReplace.put("PERFTOTAL95",""+stTotal.percentile(95));

			mReplace.put("PERFQEMEAN",""+stQE.mean());
			mReplace.put("PERFQEMEDIAN",""+stQE.median());
			mReplace.put("PERFQE95",""+stQE.percentile(95));

			mReplace.put("PERFDBMEAN",""+stDB.mean());
			mReplace.put("PERFDBMEDIAN",""+stDB.median());
			mReplace.put("PERFDB95",""+stDB.percentile(95));

			mReplace.put("LOADMAXMINUTER",sMaxMinuteR);
			mReplace.put("LOADMAXMINUTETR",sMaxMinuteTR);
			mReplace.put("LOADMAXRPM",""+iMaxMRequests);
			mReplace.put("LOADMAXTRPM",""+iMaxMTestRequests);
		}
	}

	private void replacePreProcessingNode(Document d) {
		Class<?> cla = ns.getNavigatorConfig().retrievePreProcessingRequestHandler();
		Map<String,Object> m = new HashMap<String,Object>();
		if (null == cla) {
			m.put(PRE_PROCESSOR,
				"No PreProcessor specified in the navigator.xml");
		} else {
			m.put(PRE_PROCESSOR, cla.toString());
		}
		XML.replaceTokens(d, m);
	}

	private void handleStatusHome(HttpServletRequest request,HttpServletResponse response)
	throws Exception
	{
		Document d=XML.parse(new FileInputStream(
		  ns.getServletContext().getRealPath("WEB-INF/templates/status.xhtml")));

		// Basic status

		Map<String,Object> mReplace=new HashMap<String,Object>();
		mReplace.put("ACCESS", RequestHelpers.getAccessCSSAppend(request));
		ns.obtainPerformanceInfo(mReplace);
		XML.replaceTokens(d,mReplace);

		// PreProcessor Setup
		replacePreProcessingNode(d);
		
		// QE performance
		XML.find(d,"id","qeperformance").appendChild(
			d.importNode((Node)mReplace.get("_qeperformance"),true));

		// Logs
		Log l=ns.getLog();
		Element eLogs=XML.find(d,"id","logs");
		File[] afLogs=IO.listFiles(l.getLogFolder());
		SortedSet<String> ss=new TreeSet<String>(new Comparator<String>()
		{
			public int compare(String o1,String o2)
			{
				// Reverse compare
				return o2.compareTo(o1);
			}
		});
		Pattern p=Pattern.compile("^navigator\\.([0-9]{4}-[0-9]{2}-[0-9]{2})\\.log$");
		for(int iLog=0;iLog<afLogs.length;iLog++)
		{
			Matcher m=p.matcher(afLogs[iLog].getName());
			if(m.matches())
			{
				ss.add(m.group(1));
			}
		}
		for(String sDate : ss)
		{
			Element eLI=XML.createChild(eLogs,"li");
			Element eA=XML.createChild(eLI,"a");
			eA.setAttribute("href","log-"+sDate);
			XML.createText(eA,sDate);
			File f=new File(l.getLogFolder(),"navigator."+sDate+".log");
			XML.createText(eLI," "+Strings.formatBytes(f.length())+" (");
			eA=XML.createChild(eLI,"a");
			eA.setAttribute("href","stats-"+sDate);
			XML.createText(eA,"Stats");
			XML.createText(eLI,")");
		}

		// Recent entries and problems
		new LogBuilder(l.getRecentEntries(),XML.find(d,"id","recententries"));
		new LogBuilder(l.getRecentProblems(),XML.find(d,"id","recentproblems"));

		XHTML.output(d,request,response,"en");
	}


}
