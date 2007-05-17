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

import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Stub;
import org.w3c.dom.Element;

import om.axis.qengine.*;
import om.tnavigator.NavigatorServlet.RequestTimings;
import util.misc.Strings;
import util.xml.XML;

/** 
 * Handles load balancing (and error recovery when possible) for Om service
 * access.
 */
class OmServiceBalancer
{
	/** Timeout (ms) for SOAP service calls; currently 30s */
	private static final int SOAPTIMEOUT=30000;
	
	/** Array of available service URLs */
	private URL[] au;
	/** Array of available services */
	private OmService[] aos;
	
	/** Track average performance of all services */
	private MovingAverage[] ama;
	
	/** Number of measurements to track in the averaging process */
	private final static int PERFORMANCEBUFFERSIZE=20;
	
	/** Log */
	private Log l;
	
	/** True if we should log additional debug info */
	private boolean bExtraDebug;
	
	/**
	 * @param aos Array of available services
	 */
	OmServiceBalancer(URL[] au,Log l,boolean bExtraDebug) throws ServiceException
	{
		this.au=au;
		this.l=l;
		this.bExtraDebug=bExtraDebug;
		
		aos=new OmService[au.length];
		OmServiceServiceLocator ossl=new OmServiceServiceLocator();
		for(int i=0;i<aos.length;i++)
		{
			aos[i]=ossl.getOm(au[i]);
			((Stub)aos[i]).setTimeout(SOAPTIMEOUT);
		}
		
		ama=new MovingAverage[aos.length];
		for(int i=0;i<aos.length;i++) ama[i]=new MovingAverage(PERFORMANCEBUFFERSIZE);

		lLastZero=System.currentTimeMillis();
	}
	
	
	/** 
	 * How often we add a zero to the averages (1 min, meaning a 'bad' number stays
	 * for 20 mins) 
	 */
	private final static int AVERAGEUPDATEDELAY=1*60*1000;
	
	/** Time of last pick */
	private long lLastZero;	
		
	/** Picks a random service (must be called from within synch block) */
	private int pickServer()
	{
		// Begin by adding zeros to the load balancer, corresponding to the time 
		// since last pick. This 'keeps it moving', meaning that 'bad' events
		// are forgotten after 20 mins, otherwise it'd be very unlikely that a 
		// server would ever be chosen again...
		long lNow=System.currentTimeMillis();		
		while(lLastZero+AVERAGEUPDATEDELAY < lNow)
		{
			for(int i=0;i<ama.length;i++) ama[i].add(0);
			lLastZero+=AVERAGEUPDATEDELAY;
		}
		
		// Pick service with good recent performance according to random number
		// and the algorithm:
			
		// p(choosing server A) = (sum(avgsq) - avgsq[A]) / (SERVERS-1)*(sum(avgsq))
		
		double[] adAvg=new double[aos.length];
		double[] adAvgSq=new double[aos.length];
		double dSumAvgSq=0;
		for(int i=0;i<aos.length;i++)
		{
			adAvg[i]=ama[i].get()+1.0; // The+1.0 is just to fudge it, otherwise when they're all 0 it's not so great
			adAvgSq[i]=adAvg[i]*adAvg[i];
			dSumAvgSq+=adAvgSq[i];
		}
		double[] adProb=new double[aos.length];
		double dDenominator=(aos.length-1)*dSumAvgSq;
		StringBuffer sb=null;
		if(bExtraDebug) sb=new StringBuffer("QE performance: ");
		for(int i=0;i<aos.length;i++)
		{
			if(dDenominator<0.0001)
				adProb[i]=1.0;
			else
				adProb[i]=(dSumAvgSq-adAvgSq[i]) / dDenominator;

			if(bExtraDebug) sb.append("["+(i+1)+"] "+
				Strings.formatOneDecimal(adAvg[i])+"ms, "+Strings.formatOneDecimal(adProb[i]*100.0)+"% ");
		}
		if(bExtraDebug) l.logDebug("OmServiceBalancer",sb.toString());
		
		double dPick=Math.random();
		for(int i=0;i<aos.length;i++)
		{
			dPick-=adProb[i];
			if(dPick<0)
			{
				return i;
			}
		}
		return aos.length-1;
	}
		
	/** 
	 * Represents a question session within a specific service. Once the session
	 * has started, all calls are to the same service (and not really 'balanced'
	 * any more).
	 */ 
	class OmServiceSession
	{
		/** Start return (retained only temporarily) */
		private om.axis.qengine.StartReturn srTemp;
		
		/** Question session */
		private String sQuestionSession;
		
		/** Service index */
		private int iService;
		
		private OmServiceSession(int iService,om.axis.qengine.StartReturn sr)
		{
			this.iService=iService;
			srTemp=sr;
			sQuestionSession=sr.getQuestionSession();
		}
		
		/**
		 * Returns the StartReturn then forgets about it (so you can call this only once).
		 * @return StartReturn that came from om.start()
		 */
		om.axis.qengine.StartReturn eatStartReturn()
		{
			om.axis.qengine.StartReturn sr=srTemp;
			srTemp=null;			
			return sr;
		}
		
		/**
		 * Stops the question session (only necessary if a session is aborted before
		 * ProcessReturn indicates that it ended anyhow).
		 * @param rt Timings
		 * @throws RemoteException If the service gives an error
		 */
		void stop(RequestTimings rt) throws RemoteException
		{
			long lStart=System.currentTimeMillis();
			aos[iService].stop(sQuestionSession);
			long lElapsed=System.currentTimeMillis()-lStart;
			ama[iService].add(lElapsed);
			rt.lQEngineElapsed+=lElapsed;
		}
		
		/**
		 * Processes another step of the question session.
		 * @param rt Timings
		 * @param names Input parameters (names)
		 * @param values Matching values
		 * @return Information about new state of session
		 * @throws RemoteException If the service gives an error
		 */
	  om.axis.qengine.ProcessReturn process(RequestTimings rt,String[] names,String[] values) throws RemoteException
	  {
			long lStart=System.currentTimeMillis();
	  	om.axis.qengine.ProcessReturn pr=aos[iService].process(sQuestionSession,names,values);
			long lElapsed=System.currentTimeMillis()-lStart;
			ama[iService].add(lElapsed);
			rt.lQEngineElapsed+=lElapsed;
	  	return pr;
	  }
		
	  /** @return URL of question engine being used */
	  public URL getEngineURL()
	  {
	  	return au[iService];
	  }
	}
	
	/**
	 * Calls an OmService start method on an appropriate question engine (picked
	 * from the load-balanced set based on performance). If the first question
	 * engine fails, others will be tried. If all fail, the last exception will
	 * be thrown.
	 * @param rt Timings for this request
	 * @param questionID ID of question
	 * @param questionVersion Version string for question
	 * @param questionBaseURL Base URL to obtain question if needed
	 * @param initialParamNames Parameters (names)
	 * @param initialParamValues Parameters (values)
	 * @param cachedResources List of cached resources (probably ignored)
	 * @return Session that can be used to carry out further requests
	 * @throws RemoteException If all question engines fail
	 */
  OmServiceSession start(
  	RequestTimings rt,
  	final String questionID, final String questionVersion, final String questionBaseURL, 
  	final String[] initialParamNames, final String[] initialParamValues, final String[] cachedResources) 
  	throws RemoteException
	{
  	final OmServiceSession[] out=new OmServiceSession[1]; 
  	
  	balanceThing(rt,new Balanceable()
  	{
			public void run(int iService) throws RemoteException
			{
	  		out[0]=new OmServiceSession(iService,aos[iService].start(
	    		questionID, questionVersion, questionBaseURL, initialParamNames, initialParamValues, cachedResources));
			}
  	});
  	
  	return out[0];
	}

  /**
   * Calls the OmService.getQuestionMetadata method on a load-balanced service.
   * @param rt Timings
	 * @param questionID ID of question
	 * @param questionVersion Version string for question
	 * @param questionBaseURL Base URL to obtain question if needed
   * @return True if question works in plain mode, false otherwise
   * @throws RemoteException If all services fail
   */
  public String getQuestionMetadata(RequestTimings rt,
  	final String questionID, final String questionVersion, final String questionBaseURL) 
  	throws RemoteException
  {
  	final String[] out=new String[1];
  	
  	balanceThing(rt,new Balanceable()
  	{
			public void run(int iService) throws RemoteException
			{
	  		out[0]=aos[iService].getQuestionMetadata(questionID, questionVersion, questionBaseURL);
			}
  	});
  	
  	return out[0];
  }

  /** Interface for things that can be balanced using balanceThing() */
  private static interface Balanceable
  {
  	/**
  	 * @param iService
  	 * @throws RemoteException
  	 */
  	void run(int iService) throws RemoteException;  	
  }
  
  /**
   * Load-balances (retrying across different services) a task.
   * @param rt Receives timing information (may be null)
   * @param b Task to balance
   * @throws RemoteException If the task fails on all services
   */
  private void balanceThing(RequestTimings rt,Balanceable b) throws RemoteException
	{
  	int iPick=pickServer();
  	int iService=iPick;
  	while(true)
  	{
  		try
  		{
	  		long lStart=System.currentTimeMillis();
	  		b.run(iService);
	  		long lElapsed=System.currentTimeMillis()-lStart;
	  		if(rt!=null) rt.lQEngineElapsed+=lElapsed;
	  		ama[iService].add(lElapsed);
	  		return;
  		}
  		catch(RemoteException re)
  		{
  			l.logError("OmServiceBalancer","Service "+au[iService]+"failed at balanced task",re);
  			// Add a 60 second penalty for not working
  			ama[iService].add(60000);
  			
	  		// Try next service - if there are any left!
	  		iService++;
	  		iService%=aos.length;
	  		if(iService==iPick) throw re;
  		}
  	}  	
	}
  
  /** 
   * Checks whether a server is available, throws an exception if all servers
   * fail. 
   * @return Total time taken (ms)
   * @throws RemoteException 
   */
  public int checkAvailable() throws RemoteException
  {
  	long lStart=System.currentTimeMillis();
  	balanceThing(null,new Balanceable()
		{
			public void run(int iService) throws RemoteException
			{
				aos[iService].getEngineInfo();
			}
		});
  	return (int)(System.currentTimeMillis()-lStart);
  }
  
  /**
   * Obtains information about question engine performance.
   * @return XHTML element that can be added to server status page.
   */
  Element getPerformanceInfo()
  {
  	Element eTable=XML.createDocument().createElement("table");
  	eTable.setAttribute("class","topheaders");
  	
  	Element eTR=XML.createChild(eTable,"tr");
    XML.createText(eTR,"th","Engine");
    XML.createText(eTR,"th","Performance");
    
    for(int i=0;i<au.length;i++)
    {
    	eTR=XML.createChild(eTable,"tr");
    	XML.createText(eTR,"td",au[i].getHost().replaceAll(".open.ac.uk","")+au[i].getPath());
    	XML.createText(eTR,"td",""+(int)ama[i].get());    	
    }  	
  	
  	return eTable;
  }

}
