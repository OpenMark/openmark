package om.tnavigator;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import om.AbstractTestCase;
import om.Log;
import om.axis.qengine.Score;
import om.tnavigator.scores.CombinedScore;
import om.tnavigator.teststructure.JUnitTestCaseTestDefinition;
import om.tnavigator.teststructure.TestDeployment;
import util.misc.QuestionName;

public class AbstractNavigatorTestCase extends AbstractTestCase {

	protected CombinedScore dummyScore() {
		List<Score> scores = new ArrayList<Score>();
		Map<String, Double> rawScores = new HashMap<String, Double>();
		for (int i = 1; i < 6; i++) {
			scores.add(new Score("Module" + i, i * 10));
			rawScores.put("Module" + i, new Double(i));
		}
		return CombinedScore.fromArrays(rawScores, scores.toArray(new Score[5]));
	}

	protected TestDeployment pickUpTestDeployment(String name) throws Exception {
		return new TestDeployment(getPickUpDocument(name, AbstractTestCase.class), null, name);
	}

	protected JUnitTestCaseTestDefinition pickUpTestDefinition(String name) throws Exception {
		return new JUnitTestCaseTestDefinition(getPickUpDocument(name, AbstractTestCase.class));
	}

	protected class TestCaseNavigationServlet extends NavigatorServlet {

		private static final long serialVersionUID = 2648307557006921126L;

		public TestCaseNavigationServlet(Log log) {
			l = log;
		}

		@Override
		public Score[] getMaximumScores(RequestTimings rt, QuestionName question) throws IOException, RemoteException {
			List<Score> scores = new ArrayList<Score>();
			for (int i = 1; i < 6; i++) {
				scores.add(new Score("axis-" + i, i * 10));
			}
			return scores.toArray(new Score[5]);
		}

	}

}
