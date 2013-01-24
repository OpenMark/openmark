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

import om.Log;
import om.axis.qengine.*;
import om.tnavigator.NavigatorServlet.RequestTimings;
import om.tnavigator.servicebalancer.ServiceBalancer;

import org.apache.axis.client.Stub;


/**
 * Handles load balancing (and error recovery when possible) for Om service
 * access.
 */
class OmServiceBalancer extends ServiceBalancer<OmService> {
	/**
	 * @param serviceUrls Array of available services
	 */
	OmServiceBalancer(URL[] serviceUrls, Log l, boolean logExtraDebug) throws ServiceException {
		super(serviceUrls, l, logExtraDebug);
	}

	@Override
	protected OmService createService(URL url) throws ServiceException {
		OmServiceServiceLocator ossl = new OmServiceServiceLocator();
		OmService service = ossl.getOm(url);
		((Stub)service).setTimeout(SOAPTIMEOUT);
		return service;
	}

	/**
	 * Represents a question session within a specific service. Once the session
	 * has started, all calls are to the same service (and not really 'balanced'
	 * any more).
	 */
	public class OmServiceSession extends StickySession {
		/** Start return (retained only temporarily) */
		private StartReturn srTemp;

		/** Question session */
		private String sQuestionSession;
		private String questionID;
		private String questionVersion;

		private OmServiceSession(OmService service, String questionID, String questionVersion, om.axis.qengine.StartReturn sr) {
			super(service);
			srTemp = sr;
			sQuestionSession = sr.getQuestionSession();
			this.questionID = questionID;
			this.questionVersion = questionVersion;
		}

		/**
		 * Returns the StartReturn then forgets about it (so you can call this only once).
		 * @return StartReturn that came from om.start()
		 */
		StartReturn eatStartReturn() {
			StartReturn sr = srTemp;
			srTemp = null;
			return sr;
		}

		/**
		 * Stops the question session (only necessary if a session is aborted before
		 * ProcessReturn indicates that it ended anyhow).
		 * @param rt Timings
		 * @throws RemoteException If the service gives an error
		 */
		void stop(RequestTimings rt) throws RemoteException {
			doServiceTask(rt, new ServiceTask<Object>() {
				@Override
				protected Object run(OmService service) throws RemoteException {
					service.stop(sQuestionSession);
					return null;
				}
			});
		}

		/**
		 * Processes another step of the question session.
		 * @param rt Timings
		 * @param names Input parameters (names)
		 * @param values Matching values
		 * @return Information about new state of session
		 * @throws RemoteException If the service gives an error
		 */
		ProcessReturn process(RequestTimings rt, final String[] names, final String[] values)
				throws RemoteException {
			return doServiceTask(rt, new ServiceTask<ProcessReturn>() {
				@Override
				protected ProcessReturn run(OmService service) throws RemoteException {
					return service.process(sQuestionSession,names,values);
				}
			});
		}

		/**
		 * @return the questionID of the question that this session is for.
		 */
		public String getQuestionID() {
			return questionID;
		}

		/**
		 * @return the questionVersion of the question that this session is for.
		 */
		public String getQuestionVersion() {
			return questionVersion;
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
			final String[] initialParamNames, final String[] initialParamValues,
			final String[] cachedResources) throws RemoteException {
		return balanceServiceTask(rt, new ServiceTask<OmServiceSession>() {
			@Override
			public OmServiceSession run(OmService service) throws RemoteException {
				return new OmServiceSession(service, questionID, questionVersion, 
						service.start(questionID, questionVersion, questionBaseURL,
								initialParamNames, initialParamValues, cachedResources));
			}
		});
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
			throws RemoteException {
		return balanceServiceTask(rt,new ServiceTask<String>()
		{
			@Override
			public String run(OmService service) throws RemoteException {
				return service.getQuestionMetadata(questionID, questionVersion, questionBaseURL);
			}
		});
	}

	@Override
	protected ServiceTask<String> getCheckTask() {
		return new ServiceTask<String>() {
			@Override
			public String run(OmService service) throws RemoteException {
				return service.getEngineInfo();
			}
		};
	}
}
