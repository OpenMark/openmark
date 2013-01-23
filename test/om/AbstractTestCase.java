package om;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import om.axis.qengine.Score;
import om.tnavigator.JUnitTestCaseTestDefinition;
import om.tnavigator.NavigatorServlet;
import om.tnavigator.scores.CombinedScore;
import om.tnavigator.teststructure.TestDeployment;

import org.junit.Before;
import org.w3c.dom.Document;

import util.misc.GeneralUtils;
import util.xml.XML;

public class AbstractTestCase {

	protected static String LINE_SEPERATOR = System.getProperty("line.separator");

	protected static String BASIC_TEST_DEFINITION = "mu120.module5.test.xml";

	protected static String BASIC_TEST_DEPLOYMENT = "mu120.module5.deploy.xml";

	protected static String TEST_CASE_PROGRESS_SUMMARY = "testcase-progresssummary.xhtml";

	protected Log log;

	protected Document getPickUpDocument(String name) throws Exception {
		assertNotNull(name);
		File f = pickUpFile(name);
		assertNotNull(f);
		return XML.parse(f);
	}

	@Before
	public void setUp() throws Exception{
		String logPath = getLogPath();
		log = GeneralUtils.getLog(getClass(), logPath, true);
		log.logDebug("Running tests ...");
	}

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
		return new TestDeployment(pickUpFile(name));
	}

	protected JUnitTestCaseTestDefinition pickUpTestDefinition(String name) throws Exception {
		return new JUnitTestCaseTestDefinition(pickUpFile(name));
	}

	protected String getLogPath() {
		File fileLocation = pickUpFile(TEST_CASE_PROGRESS_SUMMARY);
		assertNotNull(fileLocation);
		return fileLocation.getAbsolutePath().substring(0,
			fileLocation.getAbsolutePath().lastIndexOf(File.separator) + 1);
	}

	protected File pickUpFile(String name) {
		URL url = ClassLoader.getSystemResource(name);
		File f = null;
		if (null != url) {
			String path = url.getPath();
			path = path.replace("%20", " ");
			f = new File(path);
		}
		return f;		
	}

	protected class TestCaseNavigationServlet extends NavigatorServlet {

		private static final long serialVersionUID = 2648307557006921126L;

		public TestCaseNavigationServlet(Log log) {
			l = log;
		}

		@Override
		public Score[] getMaximumScores(RequestTimings rt, String sID,
			String sVersion) throws IOException, RemoteException {
			List<Score> scores = new ArrayList<Score>();
			for (int i = 1; i < 6; i++) {
				scores.add(new Score("axis-" + i, i * 10));
			}
			return scores.toArray(new Score[5]);
		}

	}

}
