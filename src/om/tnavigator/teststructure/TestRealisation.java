/* OpenMark online assessment system
 * Copyright (C) 2007 The Open University
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package om.tnavigator.teststructure;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import om.OmException;
import om.OmFormatException;
import om.tnavigator.NavigatorServlet.RequestTimings;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.OmQueries;
import om.tnavigator.scores.CombinedScore;

import org.w3c.dom.Element;

import util.misc.QuestionName;
import util.misc.QuestionVersion;
import util.misc.VersionUtil;
import util.xml.XML;

/**
 * This class stores the structure of a test, as realised for a particular
 * user. That means that, in section which pick questions at random,
 * the particular questions used have been chosen.
 */
public class TestRealisation {
	// Random seed for session.
	private long randomSeed;

	// If not -1, this indicates a fixed variant has been selected.
	private int fixedVariant=-1;

	// ID of in-progress test deployment.
	private String testId = null;

	// Database id for the test. tests.ti.
	private int dbTi;

	// The top level test group of the test.
	private TestGroup rootTestGroup;

	// Test items, linearised.
	protected TestLeaf[] testLeavesInOrder;

	protected TestRealisation(TestGroup rootTestGroup, TestLeaf[] testLeavesInOrder,
			long randomSeed, int fixedVariant, String testId, int dbTi) {
		this.rootTestGroup = rootTestGroup;
		this.testLeavesInOrder = testLeavesInOrder;
		this.randomSeed = randomSeed;
		this.fixedVariant = fixedVariant;
		this.testId = testId;
		this.dbTi = dbTi;
	}

	/**
	 * @param def
	 * @param lRandomSeed
	 * @param iFixedVariant
	 * @param testId
	 * @param dbTi
	 * @return a realisation of the sepecified test, given the random seed and variant.
	 * @throws OmFormatException
	 */
	public static TestRealisation realiseTest(TestDefinition def, long lRandomSeed, int iFixedVariant,
			String testId, int dbTi) throws OmFormatException {
		TestGroup testGroup = def.getResolvedContent(lRandomSeed);
		return new TestRealisation(testGroup, testGroup.getLeafItems(),
				lRandomSeed, iFixedVariant, testId, dbTi);
	}

	/**
	 * @param question
	 * @param lRandomSeed
	 * @param iFixedVariant
	 * @param testId
	 * @param dbTi
	 * @return a realisation of a single question mode.
	 * @throws OmFormatException
	 */
	public static TestRealisation realiseSingleQuestion(String question, long lRandomSeed,
			int iFixedVariant, String testId, int dbTi) throws OmFormatException {
		TestLeaf[] leaves = new TestLeaf[1];
		Element e=XML.createDocument().createElement("question");
		e.setAttribute("id",question);
		leaves[0] = new TestQuestion(null,e);

		return new TestRealisation(null, leaves, lRandomSeed, iFixedVariant, testId, dbTi);
	}

	/**
	 * @param tg the tg to set
	 */
	void setRootTestGroup(TestGroup tg) {
		this.rootTestGroup = tg;
	}

	/**
	 * @return the tg
	 */
	public TestGroup getRootTestGroup() {
		return rootTestGroup;
	}

	/**
	 * @param atl the atl to set
	 */
	void setTestLeavesInOrder(TestLeaf[] atl) {
		this.testLeavesInOrder = atl;
	}

	/**
	 * @return the atl
	 */
	public TestLeaf[] getTestLeavesInOrder() {
		return testLeavesInOrder;
	}

	/**
	 * @return the sTestID
	 */
	public String getTestId() {
		return testId;
	}

	/**
	 * @return the lRandomSeed
	 */
	public long getRandomSeed() {
		return randomSeed;
	}

	/**
	 * @param fixedVariant the iFixedVariant to set.
	 */
	public void setFixedVariant(int fixedVariant) {
		this.fixedVariant = fixedVariant;
	}

	/**
	 * @return the iFixedVariant
	 */
	public int getFixedVariant() {
		return fixedVariant;
	}

	/**
	 * @param dbTi the database id for the test. tests.ti.
	 */
	public void setDbTi(int dbTi) {
		this.dbTi = dbTi;
	}

	/**
	 * @return the database id for the test. tests.ti.
	 */
	int getDbTi() {
		return dbTi;
	}

	public CombinedScore getScore(RequestTimings rt, QuestionMetadataSource metadataSource,
			DatabaseAccess da, OmQueries oq) throws Exception {

		DatabaseAccess.Transaction dat = da.newTransaction();
		try
		{
			// Query for all questions in test alongside each completed attempt
			// at that question, ordered so that the most recent completed attempt
			// occurs *first* [we then ignore following attempts for each question
			// in code].
			ResultSet rs = oq.queryScores(dat, dbTi);

			rs.next();
			return getScoreFromResultSet(rt, rs, metadataSource);
		}
		finally
		{
			rt.setDatabaseElapsedTime(rt.getDatabaseElapsedTime() + dat.finish());
		}
	}

	/**
	 * Compute and return the score for this test attempt, pulling the data from
	 * the given result set.
	 *
	 * The first 8 columns of the result set, must match what is returned by
	 * {@link OmQueries.queryScores}, and the cursor must be at the first row
	 * for this ti. Once the method is complete, the cursor will be at the first
	 * row with a different ti, or the end of the recordset.
	 *
	 * The process of computing the score fills in the score for each question
	 * and section, so sometimes this method is called to pre-compute all that,
	 * and the result is discarded.
	 *
	 * Becuase Java ResultSets are stupid (you cannot rely on isAfterLast) we
	 * adopt the convention that the it is closed as soon at rs.next() returns false.
	 *
	 * @param rt Used for storing performance information.
	 * @param metadataSource The servlet we are computing the answer for.
	 * @param da Database access.
	 * @param oq For querying the database.
	 * @return the score for this testRealisation by fetching the scores from the database.
	 * @throws Exception
	 */
	public CombinedScore getScoreFromResultSet(RequestTimings rt, ResultSet rs, QuestionMetadataSource metadataSource) throws Exception
	{
		// These two maps should always have the same set of keys.
		Map<String, Map<String, Double> > questionScores =
				new HashMap<String, Map<String, Double>>();
		Map<String, QuestionVersion> questionVersions =
				new HashMap<String, QuestionVersion>();

		// Build into map of question ID -> axis string/"" -> score (Integer)
		String sCurQuestion="";
		int iCurAttempt=-1;
		while (!rs.isClosed())
		{
			int ti = rs.getInt(8);

			// Detect if we have multiple tis in this result set, and have
			// consumed all the rows for this ti.
			if (dbTi != ti) {
				break;
			}

			String sQuestion=rs.getString(1);
			int iMajor=rs.getInt(2),iMinor=rs.getInt(3); // Will be 0 if null
			boolean bGotVersion=!rs.wasNull() && iMajor!=0;
			int iAttempt=rs.getInt(4);
			String sAxis=rs.getString(5);
			int iScore=rs.getInt(6);
			int iRequiredMajor=rs.getInt(7);
			if (!rs.next()) {
				rs.close();
			}

			// Ignore all attempts on a question other than the first encountered
			// (this is a pain to do in SQL so I'm doing it in code, relying on
			// the sort order that makes the latest finished attempt appear first)
			if(sCurQuestion.equals(sQuestion) && iAttempt!=iCurAttempt)
				continue;
			sCurQuestion = sQuestion;

			// Find question. If it's not there create it - all questions get
			// an entry in the hash map even if they have no results
			Map<String, Double> scores = questionScores.get(sQuestion);
			if (scores == null)
			{
				QuestionVersion qv;
				if (bGotVersion)
				{
					// If they actually took the question, we use the version they took
					// (this version info is used for getting max score)
					qv = new QuestionVersion(iMajor, iMinor);
				}
				else
				{
					// If they didn't take the question then either use the latest version
					// overall or the latest specified major version
					qv = metadataSource.getLatestVersion(sQuestion,
							iRequiredMajor==0 ? VersionUtil.VERSION_UNSPECIFIED : iRequiredMajor);
				}
				questionVersions.put(sQuestion, qv);

				scores = new HashMap<String, Double>();
				questionScores.put(sQuestion, scores);
			}

			// In this case null axis => SQL null => no results for this question.
			// Default axis is ""
			if(sAxis!=null)
			{
				if(sAxis.equals(""))
					scores.put(null,(double)iScore);
				else
					scores.put(sAxis,(double)iScore);
			}
		}

		applyScores(questionVersions, questionScores, metadataSource, rt);

		// Sanity check: make sure all the questions have a score
		sanityCheckScores();

		// Now calculate the total score
		return rootTestGroup.getFinalScore();
	}

	/**
	 * Sanity check: make sure all the questions have a score
	 * @throws OmException
	 */
	protected void sanityCheckScores() throws OmException {
		for(int iQuestion=0;iQuestion<testLeavesInOrder.length;iQuestion++) {
			if(!(testLeavesInOrder[iQuestion] instanceof TestQuestion)) continue;
			TestQuestion tq=(TestQuestion)testLeavesInOrder[iQuestion];
			if(!tq.hasActualScore())
				throw new OmException("Couldn't find score for question: "+tq.getID());
		}
	}

	protected void applyScores(Map<String, QuestionVersion> questionVersions,
		Map<String, Map<String, Double>> questionScores, QuestionMetadataSource metadataSource, RequestTimings rt)
		throws Exception {
		// Loop around all questions, setting up the score in each one.
		for (Map.Entry<String, QuestionVersion> me : questionVersions.entrySet()) {
			// Get create the score
			String sQuestion = me.getKey();
			QuestionVersion qv = me.getValue();
			CombinedScore score = CombinedScore.fromArrays(
				questionScores.get(sQuestion),
				metadataSource.getMaximumScores(rt, new QuestionName(sQuestion, qv)));
			attatchToAppropriateTestQuestion(score, sQuestion);
		}
	}

	/**
	 * Attaches the score to the TestQuestion itself.
	 * @param score
	 * @param sQuestion
	 */
	protected void attatchToAppropriateTestQuestion(CombinedScore score,
		String sQuestion) {
		for (int iQuestion=0;iQuestion<testLeavesInOrder.length;iQuestion++) {
			if (!(testLeavesInOrder[iQuestion] instanceof TestQuestion)) continue;
			TestQuestion tq = (TestQuestion)testLeavesInOrder[iQuestion];
			if (tq.getID().equals(sQuestion)) {
				tq.setActualScore(score);
				break;
			}
		}
	}
}
