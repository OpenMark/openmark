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
package om.tnavigator.sessions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import om.OmException;
import om.OmVersion;
import om.axis.qengine.Resource;
import om.tnavigator.NavigatorServlet;
import om.tnavigator.OmServiceBalancer;
import om.tnavigator.auth.UserDetails;
import om.tnavigator.teststructure.TestDefinition;
import om.tnavigator.teststructure.TestDeployment;
import om.tnavigator.teststructure.TestGroup;
import om.tnavigator.teststructure.TestLeaf;
import om.tnavigator.teststructure.TestRealisation;

/** Data stored about particular user */
public class UserSession
{
	private NavigatorServlet ns;

	/** Cookie (key of map too, but duplicated here) */
	public String sCookie;

	/** Question session ID for question engine */
	public OmServiceBalancer.OmServiceSession oss = null;

	/** Sequence used to check you don't do things out of order */
	public String sSequence;

	/** Time of session start */
	public long lSessionStart = System.currentTimeMillis();

	/** Time of last action in session */
	private long lastActionTime = System.currentTimeMillis();

	/** Current test deployment. */
	protected TestDeployment tdDeployment = null;

	/** Current test definition (null for single question) */
	protected TestDefinition testDefinition = null;

	/**
	 * The test realisation, that is, exactly what sections
	 * and questions make up the test for this user, given
	 * the random choices.
	 */
	protected TestRealisation testRealisation = null;

	/** Index within test items */
	private int testPosition;

	/** User login details */
	public UserDetails ud = null;

	/** OUCU (real or fake) */
	public String sOUCU;

	/**
	 * Hash of cookies that determine ud and without which the session
	 * should be dumped
	 */
	public int iAuthHash=0;

	/** Whether they have admin access */
	public boolean bAdmin = false;

	/** Whether they can also view reports */
	public boolean bAllowReports = false;

	/** Whether the final PCDC code has been generated */
	public boolean bHasGeneratedFinalPCDC = false;

	/** Map of String (filename) -> Resource */
	public Map<String,Resource> mResources = new HashMap<String,Resource>();

	/** CSS */
	public String sCSS = "";

	/** Progress info */
	public String sProgressInfo = "";

	/** Database ID for test, question, sequence */
	private int dbTi;
	public int iDBqi;
	public int iDBseq;

	/** True if they have finished the test */
	private boolean bFinished;

	/** The version of the test navigator software that started this attempt. */
	public String navigatorVersion;

	/**
	 * Set true only for specific requests that are permitted after the forbid
	 * date (up to the forbid-extension date)
	 */
	public boolean bAllowAfterForbid;

	/**
	 * True if their browser has been checked and found OK, or they have
	 * decided to ignore the warning.
	 */
	public boolean bCheckedBrowser;

	/**
	 * Once we've seen their OUCU and checked that we only hold one session
	 * for them, this is set to OUCU-testID.
	 */
	public String sCheckedOUCUKey;

	/** Index increments whenever there is a new CSS version */
	public int iCSSIndex=0;

	/**
	 * Very hacky way to store the situation when confirm emails are sent
	 * or not. 1=sent, -1=error
	 */
	public int iEmailSent=0;

	/** A place where any extra information can be stored in the session. */
	private Map<String,Object> extraSessionInfo = new HashMap<String, Object>();

	public NavigatorServlet getNs() {
		return ns;
	}

	public int getICSSIndex() {
		return new Integer(iCSSIndex).intValue();
	}

	/**
	 * @param owner
	 */
	public UserSession(NavigatorServlet owner, String cookie) {
		this.ns = owner;
		this.lSessionStart = System.currentTimeMillis();
		this.sCookie = cookie;
		ns.getLog().logDebug("Created new UserSession.");
	}

	/**
	 * @return the sTestID
	 */
	public String getTestId() {
		if (testRealisation != null)
			return testRealisation.getTestId();
		else
			return null;
	}

	/**
	 * @return is this a session on a real test, or in single question mode.
	 */
	public boolean isSingle()
	{
		return getTestDeployment().isSingleQuestion();
	}

	/**
	 * Load a test deployment file into this session.
	 * @param testId thid id of the test deployment to load.
	 * @throws OmException
	 */
	public void loadTestDeployment(String testId) throws OmException {
		// Load test deploy, if necessary.
		if(tdDeployment==null)
		{
			File deployFile = ns.pathForTestDeployment(testId);
			tdDeployment = new TestDeployment(deployFile);
		}
	}

	/**
	 * @return the tdDeployment
	 * @throws OmException if the deployment has not already been
	 * loaded, and there is an error doing so.
	 */
	public TestDeployment getTestDeployment() {
		return tdDeployment;
	}

	/**
	 * @return the tdDeployment
	 * @throws OmException if the deployment has not already been
	 * loaded, and there is an error doing so.
	 */
	public TestRealisation getTestRealisation() {
		return testRealisation;
	}

	/**
	 * @return the lRandomSeed
	 */
	public long getRandomSeed() {
		return testRealisation.getRandomSeed();
	}

	/**
	 * @param fixedVariant the iFixedVariant to set.
	 */
	public void setFixedVariant(int fixedVariant) {
		testRealisation.setFixedVariant(fixedVariant);
	}

	/**
	 * @return the iFixedVariant
	 */
	public int getFixedVariant() {
		return testRealisation.getFixedVariant();
	}

	/**
	 * @return the tg
	 */
	public TestGroup getRootTestGroup() {
		return testRealisation.getRootTestGroup();
	}

	/**
	 * @return the atl
	 */
	public TestLeaf[] getTestLeavesInOrder() {
		return testRealisation.getTestLeavesInOrder();
	}

	/**
	 * @param iIndex the iIndex to set
	 */
	public void setTestPosition(int iIndex) {
		this.testPosition = iIndex;
	}

	/**
	 * @param bFinished the bFinished to set
	 */
	public void setFinished(boolean bFinished) {
		this.bFinished = bFinished;
	}

	/**
	 * @return the bFinished
	 */
	public boolean isFinished() {
		return bFinished;
	}

	/**
	 * @return the sOUCU
	 */
	public String getOUCU() {
		return sOUCU;
	}
	/**
	 * @param bFinished the bFinished to set
	 */
	public void setHasGeneratedFinalPCDC(boolean bFinished) {
		this.bHasGeneratedFinalPCDC = bFinished;
	}

	/**
	 * @return the bFinished
	 */
	public boolean isHasGeneratedFinalPCDC() {
		return bHasGeneratedFinalPCDC;
	}

	/**
	 * @return the iIndex
	 */
	public int getTestPosition() {
		return testPosition;
	}

	/**
	 * @param dbTi the dbTi to set.
	 */
	public void setDbTi(int dbTi) {
		this.dbTi = dbTi;
		if (testRealisation != null) testRealisation.setDbTi(dbTi);
	}

	/**
	 * @return the iDBti
	 */
	public int getDbTi() {
		return dbTi;
	}

	/**
	 * @return the testDefinition
	 */
	public TestDefinition getTestDefinition() {
		return testDefinition;
	}

	/**
	 * Update the last action time, to record the fact this session has just been used again.
	 */
	public void touch() {
		this.lastActionTime = System.currentTimeMillis();
	}

	/**
	 * Set the last action time to a long way in the past, so this session
	 * is expired the next time the session expirer runs.
	 */
	public void markForDiscard() {
		this.lastActionTime = 0;
	}

	/**
	 * @return the last time this session was used.
	 */
	long getLastActionTime() {
		return lastActionTime;
	}

	/**
	 * Store a miscellaneous piece of information in the user session.
	 * @param key the key identifying this piece of information.
	 * @param value the value to store.
	 */
	public void store(String key, Object value)
	{
		extraSessionInfo.put(key,value);
	}

	/**
	 * Retrieve a miscellaneous piece of information from the user session.
	 * @param <T> the type of thing we expect to retrieve.
	 * @param key the key identifying this piece of information
	 * @param dataType the class of the expected type. Used to determine T.
	 * @return the requested value, assuring that it is of type dataType.
	 * @throws OmException
	 */
	public <T> T retrive(String key, Class<T> dataType) throws OmException
	{
		Object value =  extraSessionInfo.get(key);
		if (dataType.isAssignableFrom(value.getClass())) {
			return dataType.cast(value);
		}
		throw new OmException("Could not retrieve '" + key + "' from the session");
	}

	/**
	 * Remove a miscellaneous piece of information from the user session.
	 * @param key the key identifying this piece of information
	 */
	public void remove(String key)
	{
		extraSessionInfo.remove(key);
	}

	/**
	 * @param testID the deploy file id.
	 * @param finished whether the student had finished their attempt.
	 * @param randomSeed the random seed to use for this attempt.
	 * @param fixedVariant the fixed variant to use, or -1 for none.
	 * @throws OmException
	 */
	public void realiseTest(String testID, boolean finished, long randomSeed, int fixedVariant) throws OmException {
		if(isSingle())
		{
			testDefinition = null;
			testRealisation = TestRealisation.realiseSingleQuestion(tdDeployment.getQuestion(), randomSeed, fixedVariant, testID, dbTi);
		}
		else
		{
			testDefinition = tdDeployment.getTestDefinition();
			testRealisation = TestRealisation.realiseTest(testDefinition, randomSeed, fixedVariant, testID, dbTi);
		}
		testPosition = 0;
		bFinished = finished;
		navigatorVersion = OmVersion.getVersion();
	}
}
