package om.tnavigator.teststructure;

import om.tnavigator.NavigatorServlet.RequestTimings;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.OmQueries;
import om.tnavigator.scores.CombinedScore;

public class JUnitTestCaseTestRealisation extends TestRealisation {

	private static CombinedScore combinedScore;

	protected JUnitTestCaseTestRealisation(TestGroup rootTestGroup,
		TestLeaf[] testLeavesInOrder, long randomSeed, int fixedVariant,
		String testId, int dbTi) {
		super(rootTestGroup, testLeavesInOrder, randomSeed, fixedVariant, testId, dbTi);
	}

	public static JUnitTestCaseTestRealisation getTestRealisationInstance(int dbTi,
		JUnitTestCaseTestDefinition td, CombinedScore cs) throws Exception {
		long randomSeed = System.currentTimeMillis();
		JUnitTestCaseTestGroup testGroup = (JUnitTestCaseTestGroup) td.getResolvedContent(randomSeed);
		TestLeaf[] testLeavesInOrder = testGroup.getLeafItems();
		JUnitTestCaseTestRealisation tctr = new JUnitTestCaseTestRealisation(testGroup,
			testLeavesInOrder, randomSeed, 1, null, dbTi);
		combinedScore = cs;
		return tctr;
	}

	@Override
	public CombinedScore getScore(RequestTimings rt, QuestionMetadataSource metadataSource,
			DatabaseAccess da, OmQueries oq) throws Exception {
		applyDummyScores();
		return combinedScore;
	}

	public void applyDummyScores() {
		for (int iQuestion=0; iQuestion < testLeavesInOrder.length; iQuestion++) {
			if (!(testLeavesInOrder[iQuestion] instanceof TestQuestion)) continue;
			TestQuestion tq = (TestQuestion) testLeavesInOrder[iQuestion];
			attatchToAppropriateTestQuestion(combinedScore, tq.getID());
		}
	}

}
