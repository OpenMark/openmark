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
package om.loadtest;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.misc.IO;
import util.xml.XML;

/** Load tester for test navigator */
public class TNTester
{
	/** Random thread delays */
	private final static int DELAYMIN=1000,DELAYMAX=5000;
//	private final static int DELAYMIN=0,DELAYMAX=0;

	/** Number of iterations per thread */
	private final static int ITERATIONS=15;

	/** Number of threads */
	private final static int THREADS=30;

	/** Which server to use (-1 = both, random pick) */
	private static int USESERVER=-1;
	private final static String[] HOSTS={"pclt166.open.ac.uk","sparrow.open.ac.uk"};
	private final static int[] PORTS={8080,80};

	/** If on, runs just a single iteration and displays more info */
	private final static boolean DEBUG=false;

	/**
	 * Run the test.
	 * @param args not used.
	 */
	public static void main(String[] args)
	{
		try
		{
			new TNTester();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}


	/** Map from String (oucu) -> List of StepStats */
	private Map<String,List<StepStats> > mStats=new HashMap<String,List<StepStats> >();

	/** Statistics for one 'step' of the test process */
	static class StepStats
	{
		/** Path of step */
		String sPath;
		/** Time taken for step (excluding media) */
		long lTime;

		/** List of MediaStats */
		List<MediaStats> lMedia=new LinkedList<MediaStats>();

		/** Total time taken for media for this step */
		long getMediaTime()
		{
			long lTotal=0;
			for(MediaStats ms : lMedia)
			{
				lTotal+=ms.lTime;
			}
			return lTotal;
		}
	}

	/** Statistics for one media item download attempt */
	static class MediaStats
	{
		/** Path of media */
		String sPath;
		/** Time taken to load item */
		long lTime;
	}

	private synchronized String getNextOUCU()
	{
		throw new UnsupportedOperationException(
				"This sort of test login is no longer supported by OpenMark.");
	}

	/** Test script */
	private HttpScript.Item[] ahsi;

	/** Track how many threads have finished */
	private int iFinished=0;

	private TNTester() throws Exception
	{
		if(USESERVER!=-1)
			System.out.println("Host: "+HOSTS[USESERVER]+":"+PORTS[USESERVER]);
		System.out.println("Iterations: "+ITERATIONS);
		System.out.println("Threads: "+THREADS);
		System.out.println("Step delay: "+DELAYMIN+"-"+DELAYMAX);
		System.out.println();

		HttpScript hs=new HttpScript(
			getClass().getResourceAsStream("tnscript.txt"));
		ahsi=hs.getItems();

		// Do test iteration of each question then clear statistics
		System.out.println("Initial preload iteration...");
		if(USESERVER==-1)
		{
			for(int i=0;i<HOSTS.length;i++) doSequence(null,i);
		}
		else
			doSequence(null,USESERVER);

		mStats=new HashMap<String,List<StepStats> >();

		if(DEBUG) return;

		// Start threads
		long lOverallStart=System.currentTimeMillis();
		System.out.println("\nMeasured test:");
		for(int i=0;i<THREADS;i++) 	new TestThread();
		while(iFinished<THREADS)
		{
			synchronized(this)
			{
				wait();
			}
		}


		System.out.println();
		System.out.println();
		System.out.println("Real time:\t"+(System.currentTimeMillis()-lOverallStart));
		System.out.println();
		System.out.println();

		// Get number of steps from first entry & create statistic counters
		int iSteps=mStats.values().iterator().next().size();
		TimeStatistics[]
	    atsXHTML=new TimeStatistics[iSteps],
	    atsMedia=new TimeStatistics[iSteps],
	    atsCombined=new TimeStatistics[iSteps];
		for(int i=0;i<iSteps;i++)
		{
			atsXHTML[i]=new TimeStatistics();
			atsMedia[i]=new TimeStatistics();
			atsCombined[i]=new TimeStatistics();
		}
		TimeStatistics
			tsTotalXHTML=new TimeStatistics(),
			tsTotalMedia=new TimeStatistics(),
			tsTotalCombined=new TimeStatistics();
		Map<String,TimeStatistics> mMediaTimes=new HashMap<String,TimeStatistics>();

		// Fill statistics
		for(Iterator<List<TNTester.StepStats> > iUser=mStats.values().iterator();iUser.hasNext();)
		{
			List<TNTester.StepStats> l=iUser.next();
			if(l.size()!=iSteps) throw new Exception(
				"Unexpected: list doesn't have right number of steps!");

			Iterator<TNTester.StepStats > iList=l.iterator();
			long lTotalXHTML=0,lTotalMedia=0,lTotalCombined=0;
			for(int iStep=0;iStep<iSteps;iStep++)
			{
				StepStats ss=(StepStats)iList.next();

				atsXHTML[iStep].add(ss.lTime);
				lTotalXHTML+=ss.lTime;
				atsMedia[iStep].add(ss.getMediaTime());
				lTotalMedia+=ss.getMediaTime();
				atsCombined[iStep].add(ss.lTime+ss.getMediaTime());
				lTotalCombined+=ss.lTime+ss.getMediaTime();

				for(Iterator<MediaStats> iMedia=ss.lMedia.iterator();iMedia.hasNext();)
				{
					MediaStats ms=iMedia.next();
					String sPath=ms.sPath.replaceFirst("^.*/om-tn/","");
					TimeStatistics tsThis=mMediaTimes.get(sPath);
					if(tsThis==null)
					{
						tsThis=new TimeStatistics();
						mMediaTimes.put(sPath,tsThis);
					}
					tsThis.add(ms.lTime);
				}
			}

			tsTotalXHTML.add(lTotalXHTML);
			tsTotalMedia.add(lTotalMedia);
			tsTotalCombined.add(lTotalCombined);
		}

		// Display results
		System.out.print("\tStep\t");
		for(int i=0;i<iSteps;i++) System.out.print(i+"\t");
		System.out.print("All steps\n");

		System.out.print("XHTML\tMean\t");
		for(int i=0;i<iSteps;i++)
		{
			System.out.print(atsXHTML[i].getMean()+"\t");
		}
		System.out.print(tsTotalXHTML.getMean()+"\n");

		System.out.print("\tMedian\t");
		for(int i=0;i<iSteps;i++)
		{
			System.out.print(atsXHTML[i].getMedians()[10]+"\t");
		}
		System.out.print(tsTotalXHTML.getMedians()[10]+"\n");

		System.out.print("\t95%le\t");
		for(int i=0;i<iSteps;i++)
		{
			System.out.print(atsXHTML[i].getMedians()[19]+"\t");
		}
		System.out.print(tsTotalXHTML.getMedians()[19]+"\n");

		System.out.print("\tMax\t");
		for(int i=0;i<iSteps;i++)
		{
			System.out.print(atsXHTML[i].getMedians()[20]+"\t");
		}
		System.out.print(tsTotalXHTML.getMedians()[20]+"\n");


		System.out.print("Media\tMean\t");
		for(int i=0;i<iSteps;i++)
		{
			System.out.print(atsMedia[i].getMean()+"\t");
		}
		System.out.print(tsTotalMedia.getMean()+"\n");

		System.out.print("\tMedian\t");
		for(int i=0;i<iSteps;i++)
		{
			System.out.print(atsMedia[i].getMedians()[10]+"\t");
		}
		System.out.print(tsTotalMedia.getMedians()[10]+"\n");

		System.out.print("\t95%le\t");
		for(int i=0;i<iSteps;i++)
		{
			System.out.print(atsMedia[i].getMedians()[19]+"\t");
		}
		System.out.print(tsTotalMedia.getMedians()[19]+"\n");

		System.out.print("\tMax\t");
		for(int i=0;i<iSteps;i++)
		{
			System.out.print(atsMedia[i].getMedians()[20]+"\t");
		}
		System.out.print(tsTotalMedia.getMedians()[20]+"\n");


		System.out.print("Combined\tMean\t");
		for(int i=0;i<iSteps;i++)
		{
			System.out.print(atsCombined[i].getMean()+"\t");
		}
		System.out.print(tsTotalCombined.getMean()+"\n");
		System.out.print("\tMedian\t");
		for(int i=0;i<iSteps;i++)
		{
			System.out.print(atsCombined[i].getMedians()[10]+"\t");
		}
		System.out.print(tsTotalCombined.getMedians()[10]+"\n");

		System.out.print("\t95%le\t");
		for(int i=0;i<iSteps;i++)
		{
			System.out.print(atsCombined[i].getMedians()[19]+"\t");
		}
		System.out.print(tsTotalCombined.getMedians()[19]+"\n");

		System.out.print("\tMax\t");
		for(int i=0;i<iSteps;i++)
		{
			System.out.print(atsCombined[i].getMedians()[20]+"\t");
		}
		System.out.print(tsTotalCombined.getMedians()[20]+"\n");

		System.out.println();

		// Display media results
		System.out.println("Media\tMean\tMedian\t95%le\tMax");
		for(Iterator<Map.Entry<String, TimeStatistics> > iMedia=mMediaTimes.entrySet().iterator();iMedia.hasNext();)
		{
			Map.Entry<String, TimeStatistics> me=iMedia.next();
			TimeStatistics ts=(TimeStatistics)me.getValue();
			System.out.println(me.getKey()+"\t"+ts.getMean()+"\t"+
				ts.getMedians()[10]+"\t"+ts.getMedians()[19]+"\t"+ts.getMedians()[20]);
		}

		System.out.println();
	}

	class TestThread extends Thread
	{
		TestThread()
		{
			start();
		}

		@Override
		public void run()
		{
			Random r=new Random(System.currentTimeMillis() ^ this.hashCode());

			for(int i=0;i<ITERATIONS;i++)
			{
				// Pick server
				int iServer;
				if(USESERVER==-1)
					iServer=r.nextInt(HOSTS.length);
				else
					iServer=USESERVER;

				// Do it
				doSequence(r,iServer);
			}
			synchronized(TNTester.this)
			{
				iFinished++;
				TNTester.this.notifyAll();
			}
		}
	}

	/** Map of OUCU -> host used (String) */
	private Map<String,String> mHosts=new HashMap<String,String>();

	/**
	 * Runs through the sequence with one user.
	 * @param ahsi Array of script items
	 */
	private void doSequence(Random r,int iServer)
	{
		String sOUCU=getNextOUCU();
		try
		{
			// Random delay before starting (so all threads don't begin in synch)
			if(r!=null) Thread.sleep(r.nextInt(DELAYMAX-DELAYMIN+1)+DELAYMIN);

			Map<String,String> mTokens=new HashMap<String,String>();
			mTokens.put("AUTHCOOKIE","1ed6b71fed0260d1d9aa1730b77325a5430cba4a"+sOUCU+"%2E%2E00000000%2E%2E");
			String sCookie=sendRequestGetCookie(iServer,mTokens,ahsi[0]);
			mTokens.put("SESSIONCOOKIE",sCookie);

			if(DEBUG) System.err.println("* Got cookie: "+sCookie);

			List<StepStats> lSteps;
			synchronized(mStats)
			{
				lSteps=mStats.get(sOUCU);
				if(lSteps==null)
				{
					lSteps=new LinkedList<StepStats>();
					mStats.put(sOUCU,lSteps);
				}
				else throw new Error("wtf?");
				mHosts.put(sOUCU,HOSTS[iServer]);
			}

			for(int iItem=1;iItem<ahsi.length;iItem++)
			{
				HttpScript.Item i=ahsi[iItem];
				if(i.getURL().matches("^.*/simple1/(\\?.*)?$"))
				{
					StepStats ss=new StepStats();
					lSteps.add(ss);

					if(DEBUG) System.err.println("* Step "+(lSteps.size()-1));
					sendRequestGetMedia(iServer,mTokens,i,ss);

					// Delay
					if(r!=null) Thread.sleep(r.nextInt(DELAYMAX-DELAYMIN+1)+DELAYMIN);
				}
			}

			StringBuffer sb=new StringBuffer(sOUCU+"\t"+HOSTS[iServer]+"\n");
			sb.append("\tXHTML");
			long lTotal=0;
			for(Iterator<StepStats> i=lSteps.iterator();i.hasNext();)
			{
				StepStats ss=i.next();
				sb.append("\t"+ss.lTime);
				lTotal+=ss.lTime;
			}
			sb.append("\t\t"+lTotal);
			sb.append("\n\tMedia");
			lTotal=0;
			for(Iterator<StepStats> i=lSteps.iterator();i.hasNext();)
			{
				StepStats ss=i.next();
				sb.append("\t"+ss.getMediaTime());
				lTotal+=ss.getMediaTime();
			}
			sb.append("\t\t"+lTotal);
			System.out.println(sb.toString());
		}
		catch(Throwable t)
		{
			System.err.println(sOUCU+"\tError\t"+t);
			synchronized(mStats)
			{
				mStats.remove(sOUCU);
			}
		}
	}

	private final static Pattern SETCOOKIE=Pattern.compile(
		"Set-Cookie: tnavigator_session_simple1=([^;]+); Path=/");

	private void sendRequestGetMedia(int iServer,Map<String, String> mTokens,HttpScript.Item i,StepStats ss) throws IOException
	{
		if(DEBUG) System.err.print("  "+i.getURL());
		ss.sPath=i.getURL();
		long lBefore=System.currentTimeMillis();

		Socket s=new Socket(HOSTS[iServer],PORTS[iServer]);
		// Again with the dodgy charset assumptions
		OutputStreamWriter osw=new OutputStreamWriter(s.getOutputStream());
		String sRequest=i.getRequest();
		sRequest=XML.replaceTokens(sRequest,"%%",mTokens).replaceAll("HTTP/1.1","HTTP/1.0");
		osw.write(sRequest+"\r\n");
		if(i.getData()!=null) osw.write(i.getData());
		osw.flush();

		// Get input
		InputStream is=s.getInputStream();
		String sLine="";
		int iContentLength=-1;
		boolean bFirst=true;
		while(true)
		{
			int iChar=is.read();
			if(iChar==-1) throw new IOException("Unexpected EOF in socket input");
			if(iChar==10)
			{
				if(sLine.equals("")) break; // End of headers
				Matcher m=HttpScript.CONTENTLENGTH.matcher(sLine);
				if(m.matches())
					iContentLength=Integer.parseInt(m.group(1));
				if(bFirst)
				{
					bFirst=false;
					Matcher mStatus=HttpScript.STATUSCODE.matcher(sLine);
					if(!mStatus.matches())
						throw new IOException("Unexpected HTTP status format: "+sLine);
					if(Integer.parseInt(mStatus.group(1))!=i.getExpectedResponse())
						throw new IOException("Unexpected response code: "+sLine+" (expecting "+i.getExpectedResponse()+")\n"+
							IO.loadString(is));
				}
//				System.err.println(sLine);
				sLine="";
			}
			else if(iChar!=13)
			{
				sLine+=(char)iChar;
			}
		}

		byte[] abData;
		if(iContentLength!=-1)
		{
			abData=new byte[iContentLength];
			for(int iRead=0;iRead<abData.length;)
			{
				int iThisTime=is.read(abData,iRead,abData.length-iRead);
				if(iThisTime==-1) throw new IOException("Didn't get all data");
				iRead+=iThisTime;
			}
		}
		else
		{
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			while(true)
			{
				int iRead=is.read();
				if(iRead==-1) break;
				baos.write(iRead);
			}
			abData=baos.toByteArray();
		}
		s.close();

		ss.lTime=System.currentTimeMillis()-lBefore;
		if(DEBUG)
		{
			System.err.print(" ["+abData.length+" bytes]");
			System.err.println(" ("+ss.lTime+" ms)");
		}

		// OK now go through looking for *.gif, jpg, .css, .png
		String sContent=new String(abData);
		Set<String> sDone=new HashSet<String>();
		Matcher m=MEDIALINK.matcher(sContent);
		while(m.find())
		{
			String sURL=m.group(1);
			if(sDone.contains(sURL)) continue;
			sDone.add(sURL);

			sURL="http://"+HOSTS[iServer]+
				(PORTS[iServer]!=80 ? (":"+PORTS[iServer]) : "")+
				"/om-tn/simple1/"+sURL;
			sURL=sURL.replaceAll("simple1/../","");

			loadMedia(mTokens,new URL(sURL),ss);
		}
	}

	private void loadMedia(Map<String, String> mTokens,URL u,StepStats ss) throws IOException
	{
		MediaStats ms=new MediaStats();
		ms.sPath=u.toString();
		ss.lMedia.add(ms);
		if(DEBUG) System.err.print("  "+ms.sPath);
		long lBefore=System.currentTimeMillis();
		HttpURLConnection huc=(HttpURLConnection)u.openConnection();
		huc.setRequestProperty("Cookie",
			"tnavigator_session_simple1="+mTokens.get("SESSIONCOOKIE")+
			"; SAMSsession="+mTokens.get("AUTHCOOKIE"));
		huc.connect();
		IO.loadBytes(huc.getInputStream());
		ms.lTime=System.currentTimeMillis()-lBefore;
		if(DEBUG) System.err.println(" ("+ms.lTime+" ms)");
	}

	private final static Pattern MEDIALINK=Pattern.compile(
		"\"([^\"]+\\.(png|gif|jpg|css))\"");


	private String sendRequestGetCookie(int iServer,Map<String, String> mTokens,HttpScript.Item i) throws IOException
	{
		Socket s=new Socket(HOSTS[iServer],PORTS[iServer]);
		// Again with the dodgy charset assumptions
		OutputStreamWriter osw=new OutputStreamWriter(s.getOutputStream());
		String sRequest=i.getRequest();
		sRequest=XML.replaceTokens(sRequest,"%%",mTokens);
		osw.write(sRequest+"\r\n");
		if(i.getData()!=null) osw.write(i.getData());
		osw.flush();

		// Get input
		InputStream is=s.getInputStream();
		String sLine="";
		int iContentLength=0;
		String sCookie=null;
		boolean bFirst=true;
		while(true)
		{
			int iChar=is.read();
			if(iChar==-1) throw new IOException("Unexpected EOF in socket input");
			if(iChar==10)
			{
				if(sLine.equals("")) break; // End of headers
				Matcher m=HttpScript.CONTENTLENGTH.matcher(sLine);
				if(m.matches())
					iContentLength=Integer.parseInt(m.group(1));
				m=SETCOOKIE.matcher(sLine);
				if(m.matches())
					sCookie=m.group(1);
				if(bFirst)
				{
					bFirst=false;
					Matcher mStatus=HttpScript.STATUSCODE.matcher(sLine);
					if(!mStatus.matches())
						throw new IOException("Unexpected HTTP status format: "+sLine);
					if(Integer.parseInt(mStatus.group(1))!=i.getExpectedResponse())
					{
						throw new IOException("Unexpected response code: "+sLine+" (expecting "+i.getExpectedResponse()+")\n"+
							IO.loadString(is));
					}
				}
//				System.err.println(sLine);
				sLine="";
			}
			else if(iChar!=13)
			{
				sLine+=(char)iChar;
			}
		}

		for(int iContent=0;iContent<iContentLength;iContent++)
		{
			is.read();
		}

		s.close();

		if(sCookie==null) throw new IOException("Didn't obtain cookie");
		return sCookie;
	}
}
