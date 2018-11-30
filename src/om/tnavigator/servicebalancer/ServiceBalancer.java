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
package om.tnavigator.servicebalancer;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import om.Log;
import om.OmUnexpectedException;
import om.tnavigator.NavigatorServlet.RequestTimings;

import org.w3c.dom.Element;

import util.misc.Strings;
import util.xml.XML;

/**
 * Handles load balancing (and error recovery when possible) for Om service
 * access.
 * @param <ServiceType> The type of service to balance.
 */
public abstract class ServiceBalancer<ServiceType extends java.rmi.Remote>
{
	/** Timeout (ms) for SOAP service calls; currently 30s */
	protected static final int SOAPTIMEOUT=30*1000;

	/** Penalty (ms) for when a service call fails (throws an exception); currently 60s */
	static final int FAILUREPENALTY=60*1000;

	/** Number of measurements to track in the averaging process */
	private final static int PERFORMANCEBUFFERSIZE = 20;

	/** How often we add a zero to the averages (1 min, meaning a 'bad' number stays
	 * for 20 mins) */
	private final static int AVERAGEUPDATEDELAY=1*60*1000;

	/** Array of available service URLs */
	URL[] serviceUrls;

	/** Array of available services */
	List<ServiceType> services;

	/** Track average performance of all services */
	MovingAverage[] movingAverages;

	/** Log */
	Log l;

	/** True if we should log additional debug info */
	private boolean logExtraDebug;

	/** Time of last pick */
	private long lLastZero;

	/**
	 * @param serviceUrls Array of available services
	 */
	protected ServiceBalancer(URL[] serviceUrls, Log l, boolean logExtraDebug)
			throws ServiceException {
		this.serviceUrls = serviceUrls;
		this.l = l;
		this.logExtraDebug = logExtraDebug;
		if (l == null) {
			logExtraDebug = false;
		}

		services = new ArrayList<ServiceType>(serviceUrls.length);

		for (int i = 0; i < serviceUrls.length; i++) {
			services.add(createService(serviceUrls[i]));
		}

		movingAverages=new MovingAverage[serviceUrls.length];
		for (int i = 0; i < serviceUrls.length; i++) {
			movingAverages[i] = new MovingAverage(PERFORMANCEBUFFERSIZE);
		}

		lLastZero = System.currentTimeMillis();
	}

	protected abstract ServiceType createService(URL url) throws ServiceException;

	/** Picks a random service (must be called from within synch block) */
	private int pickServer() {
		// Begin by adding zeros to the load balancer, corresponding to the time
		// since last pick. This 'keeps it moving', meaning that 'bad' events
		// are forgotten after 20 mins, otherwise it'd be very unlikely that a
		// server would ever be chosen again...
		long timeNow = System.currentTimeMillis();
		while (lLastZero + AVERAGEUPDATEDELAY < timeNow) {
			for (int i=0; i < movingAverages.length; i++) {
				movingAverages[i].add(0);
			}
			lLastZero += AVERAGEUPDATEDELAY;
		}

		// Pick service with good recent performance according to random number
		// and the algorithm:

		// p(choosing server A) = (sum(avgsq) - avgsq[A]) / (SERVERS-1)*(sum(avgsq))

		double[] adAvg = new double[serviceUrls.length];
		double[] adAvgSq = new double[serviceUrls.length];
		double dSumAvgSq = 0;
		for (int i = 0; i < serviceUrls.length; i++) {
			adAvg[i] = movingAverages[i].get() + 1.0; // The+1.0 is just to fudge it, otherwise when they're all 0 it's not so great
			adAvgSq[i] = adAvg[i]*adAvg[i];
			dSumAvgSq += adAvgSq[i];
		}
		double[] adProb = new double[serviceUrls.length];
		double dDenominator = (serviceUrls.length - 1)*dSumAvgSq;
		StringBuffer sb = null;
		if (logExtraDebug) {
			sb = new StringBuffer("QE performance: ");
		}
		for (int i = 0; i < serviceUrls.length; i++) {
			if(dDenominator < 0.0001) {
				adProb[i] = 1.0;
			} else {
				adProb[i] = (dSumAvgSq - adAvgSq[i]) / dDenominator;
			}

			if(logExtraDebug) {
				sb.append("[" + (i + 1) + "] " + Strings.formatOneDecimal(adAvg[i]) +
						"ms, " + Strings.formatOneDecimal(adProb[i]*100.0) + "% ");
			}
		}
		if (logExtraDebug) {
			l.logDebug("OmServiceBalancer", sb.toString());
		}

		double dPick = Math.random();
		for(int i = 0;i < serviceUrls.length; i++) {
			dPick -= adProb[i];
			if (dPick < 0) {
				return i;
			}
		}
		return serviceUrls.length - 1;
	}

	/** Interface for things that can be balanced using balanceThing() */
	protected abstract class ServiceTask<T extends Object>
	{
		/**
		 * @param service The service to use in performing the service call.
		 * @throws RemoteException from the service call.
		 */
		protected abstract T run(ServiceType service) throws RemoteException;
	}

	/**
	 * Load-balances (retrying across different services) a task.
	 * @param rt Receives timing information (may be null)
	 * @param b Task to balance
	 * @throws RemoteException If the task fails on all services
	 */
	protected <T extends Object> T balanceServiceTask(RequestTimings rt, ServiceTask<T> b) throws RemoteException {
		int initialPick=pickServer();
		int iService=initialPick;
		while (true) {
			try {
				long startTime = System.currentTimeMillis();

				T result = b.run(services.get(iService));

				long elapsedTime = System.currentTimeMillis() - startTime;
				movingAverages[iService].add(elapsedTime);
				if (rt != null) {
					rt.recordServiceTime(elapsedTime);
				}
				return result;
			} catch (RemoteException re) {
				if (l != null) {
					l.logError("OmServiceBalancer" , "Service " + serviceUrls[iService] +
							"failed at balanced task", re);
				}
				// Add a penalty for not working
				movingAverages[iService].add(FAILUREPENALTY);

				// Try next service - if there are any left!
				iService = (iService + 1) % serviceUrls.length;
				if (iService==initialPick) {
					throw re;
				}
			}
		}
	}

	/**
	 * @return A relatively cheap service task that can be called to check that the remote
	 * service is still working. Obviously, this should not have any permanent effect.
	 */
	protected abstract ServiceTask<? extends Object> getCheckTask();

	/**
	 * A session with a specific remote service. Once the session has started,
	 * all calls are to the same service (and not balanced any more).
	 */
	public class StickySession {
		/** Service index */
		private final ServiceType service;
		private final MovingAverage movingAverage;
		private final URL serviceUrl;
		/**
		 * @param service
		 * @param movingAverage
		 */
		protected StickySession(ServiceType service) {
			this.service = service;
			for (int i = 0; i < serviceUrls.length; i++) {
				ServiceType tempService = services.get(i);
				if (service == tempService) {
					serviceUrl = serviceUrls[i];
					movingAverage = movingAverages[i];
					return;

				}
			}
			throw new OmUnexpectedException("Attemtpt to start a StickySession with an unrecognise service.");
		}

		protected <T> T doServiceTask(RequestTimings rt, ServiceTask<T> b) throws RemoteException {
			try {
				long startTime = System.currentTimeMillis();

				T result = b.run(service);

				long elapsedTime = System.currentTimeMillis() - startTime;
				movingAverage.add(elapsedTime);
				if (rt != null) {
					rt.recordServiceTime(elapsedTime);
				}
				return result;
			} catch (RemoteException re) {
				if (l != null) {
					l.logError("OmServiceBalancer.StickySession" , "Service " + serviceUrl +
							"failed at task", re);
				}
				// Add a penalty for not working
				movingAverage.add(ServiceBalancer.FAILUREPENALTY);
				throw re;
			}
		}

		/** @return URL of question engine being used */
		public URL getEngineURL()
		{
			return serviceUrl;
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
		long startTime = System.currentTimeMillis();
		balanceServiceTask(null, getCheckTask());
		return (int) (System.currentTimeMillis() - startTime);
	}

	/**
	 * Obtains information about question engine performance.
	 * @return XHTML element that can be added to server status page.
	 */
	public Element getPerformanceInfo()
	{
		Element eTable = XML.createDocument().createElement("table");
		eTable.setAttribute("class", "topheaders");

		Element eTR = XML.createChild(eTable,"tr");
		XML.createText(eTR, "th", "Engine");
		XML.createText(eTR, "th", "Performance");

		for(int i = 0; i < serviceUrls.length; i++)
		{
			eTR = XML.createChild(eTable, "tr");
			XML.createText(eTR, "td", serviceUrls[i].getHost().replaceAll(".open.ac.uk", "") +
					serviceUrls[i].getPath());
			XML.createText(eTR, "td", "" + (int) movingAverages[i].get());
		}

		return eTable;
	}
}
