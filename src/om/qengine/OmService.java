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
package om.qengine;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;

import javax.servlet.ServletContext;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import om.OmException;
import om.question.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.misc.*;
import util.xml.XML;

/**
 * Question engine Web service. This contains all public methods of the
 * question engine. Actual work is deferred to the {@link om.question} package.
 */
public class OmService implements ServiceLifecycle
{
	/** Servlet context */
	private ServletContext sc;

	/** Cache for Java question objects and jar files */
	private QuestionCache qc;

	/** All current question sessions (maps String -> QuestionSession) */
	private Map<String, QuestionSession> mQuestionSessions=new HashMap<String, QuestionSession>();

	/** ID to use for next session */
	private int iNextSessionID=1;

	/** Synch object used for thread */
	private Object oThreadSynch=new Object();

	/** How often to check for timed-out sessions */
	private final static long SESSIONTIMEOUTCHECK=60L*60L*1000L;

	/** How long sessions are allowed to lie idle */
	private final static long SESSIONTIMEOUT=24L*60L*60L*1000L;

	/** Single instance (needed only for check method) */
	private static OmService osSingleton=null;

	/** Used so random code can find the current question engine using a static method. */
    private static ThreadLocal<OmService> servletForThread = new ThreadLocal<OmService>();

    /** Used to store information read from the configuration file, and also things added
     * later by setConfiguration. */
    private Map<String, Object> configuration = new HashMap<String, Object>();

    /** Information about a single question session */
	private static class QuestionSession
	{
		String sSession;
		Question q;
		long lLastUsedTime;
		boolean bInUse;
	}

	/**
	 * Creates a new question session for the given question.
	 * <p>
	 * API METHOD: This method signature must not be changed in future (after
	 * initial release) unless careful attention is paid to simultaneous changes
	 * of Test Navigator. In general, if additional parameters or return values
	 * are added, a new method should be defined.
	 * <h3>Obtaining question file</h3>
	 * If the question engine already has this question in its cache, it will
	 * use that (questions of the same version are guaranteed not to change).
	 * Otherwise it retrieves questions using the following URL:
	 * <p>
	 * <i>questionBaseURL</i>/<i>questionID</i>.<i>questionversion</i>
	 * <p>
	 * For security reasons, the URL may be https: and the test navigator
	 * (which is likely to be the thing serving the URL) will operate an IP
	 * whitelist, only responding to requests from question engines it has already
	 * initiated connections to.
	 * <h3>Initial parameters</h3>
	 * initialParamNames and initialParamValues (which must be of equal length)
	 * include initial configuration parameters to be passed to the question.
	 * <table border="1">
	 * <tr><th>Name</th><th>Value</th></tr>
	 * <tr><td>randomseed</td><td>Random number seed given to question, which
	 *   should be based on the current user, the test, and the number of times
	 *   they've started this question before. Parsed as decimal integer (up to
	 *   64 bit)</td></tr>
	 * </table>
	 * @param questionID Unique ID of question
	 * @param questionVersion Version identifier of question (should include
	 *   only filename-space characters, probably just digits and full stops).
	 *   May be null, to indicate that the question may not be cached (for
	 *   preview usage only)
	 * @param questionBaseURL Base URL for questions (see above)
	 * @param initialParamNames Names of initial parameters
	 * @param initialParamValues Values of initial parameters
	 * @param cachedResources List of resources that the Test Navigator has cached
	 * @return Various data in order to provide the initial page of the question
	 * @throws OmException Whenever something goes wrong
	 */
	public StartReturn start(
		String questionID,String questionVersion,String questionBaseURL,
		String[] initialParamNames,String[] initialParamValues,
		String[] cachedResources)
	  throws OmException
	{
		setServletForThread();
		if(initialParamNames==null) initialParamNames=new String[0];
		if(initialParamValues==null) initialParamValues=new String[0];
		if(cachedResources==null) cachedResources=new String[0];

		try
		{
			// Slurp values from parameters
			if(initialParamNames.length!=initialParamValues.length)
				throw new OmException("Parameter name and value arrays of different length");
			long lRandomSeed=0;
			boolean bGotRandomSeed=false;
			String sFixedFG=null,sFixedBG=null;
			boolean bPlain=false;
			double dZoom=1.0;
			int iFixedVariant=-1;

			for(int i=0;i<initialParamNames.length;i++)
			{
				String
					sName=initialParamNames[i],
					sValue=initialParamValues[i];
				if(sName.equals("randomseed"))
				{
					lRandomSeed=Long.parseLong(sValue);
					bGotRandomSeed=true;
				}
				else if(sName.equals("fixedfg"))
				{
					sFixedFG=sValue;
				}
				else if(sName.equals("fixedbg"))
				{
					sFixedBG=sValue;
				}
				else if(sName.equals("plain"))
				{
					bPlain=sValue.equals("yes");
				}
				else if(sName.equals("zoom"))
				{
					dZoom=Double.parseDouble(sValue);
				}
				else if(sName.equals("fixedvariant"))
				{
					iFixedVariant=Integer.parseInt(sValue);
				}
			}
			if(!bGotRandomSeed) throw new OmException("Required parameter missing: randomseed");

			// Get question into cache
			QuestionCache.QuestionKey qk=new QuestionCache.QuestionKey(
				questionID,questionVersion);
			obtainQuestion(questionBaseURL,qk);

			// Create and initialise question
			QuestionCache.QuestionInstance qi=qc.newQuestion(qk);
			Question q=qi.q;
			InitParams ip=new InitParams(lRandomSeed,sFixedFG,sFixedBG,dZoom,bPlain,qi.ccl,iFixedVariant);
			Rendering r=q.init(qc.getMetadata(qk),ip);

			// Generate session details and store in map
			QuestionSession qs=new QuestionSession();
			qs.q=q;
			qs.lLastUsedTime=System.currentTimeMillis();
			String sSession;
			synchronized(this)
			{
				sSession=""+(iNextSessionID++);
				qs.sSession=sSession;
				mQuestionSessions.put(sSession,qs);
			}

			return new StartReturn(sSession,r);
		}
		catch(Throwable t)
		{
			throw handleException("start",t);
		}
		finally
		{
			unsetServletForThread();
		}
	}

	/**
	 * Obtains XML metadata about a question. Metadata currently is of the form:
	 * <pre>
	 * &lt;questionmetadata>
	 * &lt;plainmode>yes&lt;/plainmode> (or 'no')
	 * &lt;title>My Fun Question&lt;/title> (optional if question has no title)
	 * &lt;/questionmetadata>
	 * </pre>
	 * <p>
	 * Order of the content is not significant and callers must accept changes
	 * in order as well as additional tags.
	 * <p>
	 * This mechanism may be changed at some point because we might start storing
	 * metadata independently of the question engine, bu for now...
	 * <p>
	 * API METHOD: This method signature must not be changed in future (after
	 * initial release) unless careful attention is paid to simultaneous changes
	 * of Test Navigator. In general, if additional parameters or return values
	 * are added, a new method should be defined.
	 * @param questionID Unique ID of question
	 * @param questionVersion Version identifier of question (should include
	 *   only filename-space characters, probably just digits and full stops).
	 *   May be null, to indicate that the question may not be cached (for
	 *   preview usage only)
	 * @param questionBaseURL Base URL for questions (see above)
	 * @return True if it's OK in plain mode, false if it isn't
	 * @throws OmException Whenever something goes wrong
	 */
	public String getQuestionMetadata(
		String questionID,String questionVersion,String questionBaseURL)
		throws OmException
	{
		setServletForThread();
		try
		{
			// Initial part
			String sResponse="<questionmetadata>";

			// Get question into cache
			QuestionCache.QuestionKey qk=new QuestionCache.QuestionKey(
				questionID,questionVersion);
			obtainQuestion(questionBaseURL,qk);

			// Get question.xml from question cache
			Document dMetadata=qc.getMetadata(qk);

			// Title
			if(XML.hasChild(dMetadata.getDocumentElement(),"title"))
				sResponse+=XML.saveString(XML.getChild(dMetadata.getDocumentElement(),"title"));

			// Scoring
			if(XML.hasChild(dMetadata.getDocumentElement(),"scoring"))
				sResponse+=XML.saveString(XML.getChild(dMetadata.getDocumentElement(),"scoring"));

			// Plain mode support
			if("no".equals(dMetadata.getDocumentElement().getAttribute("plainmode")))
				sResponse+="<plainmode>no</plainmode>";
			else
				sResponse+="<plainmode>yes</plainmode>";

			// Finish response
			sResponse+="</questionmetadata>";
			return sResponse;
		}
		catch(Throwable t)
		{
			throw handleException("getQuestionMetadata",t);
		}
		finally
		{
			unsetServletForThread();
		}
	}


	private void obtainQuestion(String questionBaseURL,QuestionCache.QuestionKey qk) throws OmException
	{
		if(!qc.containsQuestion(qk))
		{
			String sContentURL=questionBaseURL+"/"+qk.getURLPart();
			try
			{
				// Get it from the URL
				byte[] abJar;
				if(sContentURL.equals("/!test.0.0"))
				{
					// Question used during automated check
					abJar=IO.loadBytes(getClass().getResourceAsStream("testquestion.jar"));
				}
				else
				{
					URL u=new URL(sContentURL);
					HttpURLConnection huc=(HttpURLConnection)u.openConnection();
					HTTPS.considerCertificatesValid(huc);
					HTTPS.allowDifferentServerNames(huc);
					abJar=IO.loadBytes(huc.getInputStream());
				}

				// Put it in cache
				qc.saveQuestion(qk,abJar);

			}
			catch(MalformedURLException e)
			{
				throw new OmException("Invalid question content URL: "+
					sContentURL,e);
			}
			catch(IOException e)
			{
				throw new OmException("Error accessing content URL: "+
					sContentURL,e);
			}
		}
	}

	/**
	 * Stops a given question session, freeing up resources.
	 * <p>
	 * If a question session is not stopped after a given timeout (possibly 24
	 * hours) since last {@link #start(String,String,String,String[],String[],String[])} or
	 * {@link #process(String, String[], String[])}, the question
	 * engine should automatically time-out the session.
	 * <p>
	 * API METHOD: This method signature must not be changed in future (after
	 * initial release) unless careful attention is paid to simultaneous changes
	 * of Test Navigator. In general, if additional parameters or return values
	 * are added, a new method should be defined.
	 * @param questionSession
	 * @throws OmException
	 */
	public void stop(String questionSession) throws OmException
	{
		setServletForThread();
		try
		{
			synchronized(this)
			{
				QuestionSession qs=mQuestionSessions.get(questionSession);
				if(qs==null) throw new OmException("Unknown question session");
				if(qs.bInUse) throw new OmException("Question cannot be stopped mid-call");

				closeSession(qs);
			}
		}
		catch(Throwable t)
		{
			throw handleException("stop",t);
		}
		finally
		{
			unsetServletForThread();
		}
	}

	/**
	 * Removes a given session from the internal memory
	 * @throws OmException If something goes wrong
	 */
	private void closeSession(QuestionSession qs) throws OmException
	{
		qs.q.close();
		qc.returnQuestion(qs.q);

		mQuestionSessions.remove(qs.sSession);
	}

	/**
	 * Processes a user's action in a question session.
	 * <p>
	 * A user's action consists of a number of name-value pairs. These are generally
	 * the form parameters from the user's submission, but other information may
	 * be included here if needed.
	 * <p>
	 * If the action occurred in plain mode then the following
	 * name/value pair - "plain","yes" - <b>must</b> be included in the parameters.
	 * This is because questions can accept plain-mode actions even when the question
	 * itself was not started in plain mode (e.g. when playing through steps to
	 * restart a question that was initially attempted in plain mode).
	 * <p>
	 * API METHOD: This method signature must not be changed in future (after
	 * initial release) unless careful attention is paid to simultaneous changes
	 * of Test Navigator. In general, if additional parameters or return values
	 * are added, a new method should be defined.
	 * @param questionSession Question session ID
	 * @param names Form names in user's answer
	 * @param values Form parameters (must be an array of equal size to
	 *   names)
	 * @return New XHTML etc.
	 * @throws OmException
	 */
	public ProcessReturn process(String questionSession,String[] names,String[] values)
    		throws OmException
	{
		setServletForThread();
		if(names==null) names=new String[0];
		if(values==null) values=new String[0];

		try
		{
			// Get question session
			QuestionSession qs;
			synchronized(this)
			{
				qs=mQuestionSessions.get(questionSession);
				if(qs==null) throw new OmException(
					"Unknown question session");
				qs.lLastUsedTime=System.currentTimeMillis(); // In sync just because it's a long
				qs.bInUse=true;
			}

			try
			{
				// Build up parameter object
				ActionParams ap=new ActionParams();
				if(names.length!=values.length) throw new OmException(
					"names and values must be of same length");
				for(int i=0;i<names.length;i++)
				{
					ap.setParameter(names[i],values[i]);
				}

				// Actually call question
				ActionRendering ar=qs.q.action(ap);

				// Close question session if needed
				if(ar.isSessionEnd())
				{
					closeSession(qs);
				}

				return new ProcessReturn(ar);
			}
			finally
			{
				synchronized(this)
				{
					qs.bInUse=false;
				}
			}
		}
		catch(Throwable t)
		{
			throw handleException("process",t);
		}
		finally
		{
			unsetServletForThread();
		}
	}

	private static OmException handleException(String sMethod,Throwable t)
	{
		return new OmException("[[BEGINEXCEPTION]]"+
			Exceptions.getString(t,new String[] {"om"})+"[[ENDEXCEPTION]]");
	}

	public void init(Object oContext) throws ServiceException
	{
		System.gc();

		// Create the singleton instance that the check servlet expects.
		if(osSingleton!=null) throw new ServiceException("Static OmService already exists");
		osSingleton=this;

		// Get the ServletContext.
		try
		{
			ServletEndpointContext sec=(ServletEndpointContext)oContext;
			sc=sec.getServletContext();
			qc=new QuestionCache(new File(sc.getRealPath("questioncache")));
		}
		catch(Throwable t)
		{
			throw new ServiceException(t);
		}

		// Ensure we are running in headless mode.
		try
		{
			System.setProperty("java.awt.headless", "true");
		}
		catch(Throwable t)
		{
		}
		if(!GraphicsEnvironment.isHeadless())
		{
			throw new ServiceException("Your application server must be set to run in " +
				"headless mode. Add the following option to the Java command line that " +
				"launches it: -Djava.awt.headless=true");
		}

		// Load the configuration from the configuration file.
		File f = new File(sc.getRealPath("qengine.xml"));
		if (f.exists()) {
			try {
				Document configXML = XML.parse(f);
				Element[] elements = XML.getChildren(configXML.getDocumentElement());
				for (int i = 0; i < elements.length; i++) {
					setConfiguration(elements[i].getTagName(), elements[i]);
				}
			} catch (IOException e) {
				new ServiceException("Failed to load and parse configuration file.");
			}
		}

		// Start the check thread.
		(new Thread(new Runnable()
		{
			public void run()
			{
				checkThread();
			}
		})).start();
	}

	/** Thread that periodically expires unused sessions */
	private void checkThread()
	{
		while(true)
		{
			synchronized(oThreadSynch)
			{
				// Timeout runs each hour
				try
				{
					oThreadSynch.wait(SESSIONTIMEOUTCHECK);
				}
				catch(InterruptedException e)
				{
				}

				// Bail on exit
				if(sc==null)
				{
					oThreadSynch.notifyAll();
					return;
				}
			}

			// OK, check for any timed-out sessions
			long lTimeout=System.currentTimeMillis() - SESSIONTIMEOUT;
			synchronized(this)
			{
				// Copy list because we'll make changes to map while going through
				LinkedList<Map.Entry<String, QuestionSession> > ll =
						new LinkedList<Map.Entry<String, QuestionSession> >(mQuestionSessions.entrySet());
				for (Map.Entry<String, QuestionSession> me : ll)
				{
					QuestionSession qs=me.getValue();
					if(qs.lLastUsedTime < lTimeout)
					{
						try
						{
							closeSession(qs);
						}
						catch(OmException oe)
						{
							// Ignore exception
						}
					}
				}
			}

		}
	}

	/** @return Static instance of the class; null if none's been created yet */
	static OmService getStatic() { return osSingleton; }

	public void destroy()
	{
		if(osSingleton==this) osSingleton=null;

		sc=null;
		mQuestionSessions=null;
		qc.shutdown();
		qc=null;

		// Close thread
		synchronized(oThreadSynch)
		{
			oThreadSynch.notifyAll();
			try
			{
				oThreadSynch.wait();
			}
			catch(InterruptedException e)
			{
			}
		}
		oThreadSynch=null;

		System.gc();
	}

	/**
	 * @return An XML string with information about the question engine status.
	 */
	public String getEngineInfo()
	{
		String sMemoryUsed=Strings.formatBytes(
			Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());

		return
			"<engineinfo>\n" +
			"<name>Om question engine</name>\n"+
			"<usedmemory>"+sMemoryUsed+"</usedmemory>\n"+
			"<activesessions>"+mQuestionSessions.size()+"</activesessions>\n"+
			"</engineinfo>";
	}

	/**
	 * This method must be called in a thread before getServletForThread will work.
	 */
	private void setServletForThread() {
		servletForThread.set(this);
	}

	/**
	 * @return the instace of this class that is currently handling the current thread.
	 */
	public static OmService getServletForThread() {
		return servletForThread.get();
	}

	/**
	 * 
	 */
	private void unsetServletForThread() {
		servletForThread.set(null);
	}

	/**
	 * @param key key to identify the bit of information requested.
	 * @return the corresponding object.
	 */
	synchronized public Object getConfiguration(String key) {
		return configuration.get(key);
	}
	
	/**
	 * Store some configuration information.
	 * @param key key to identify the bit of information requested.
	 * @param value the corresponding object.
	 */
	synchronized public void setConfiguration(String key, Object value) {
		configuration.put(key, value);
	}
}
