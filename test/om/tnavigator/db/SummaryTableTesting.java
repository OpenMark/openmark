package om.tnavigator.db;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import om.AbstractTestCase;
import om.Log;
import om.axis.qengine.Score;
import om.tnavigator.JUnitTestCaseTestDefinition;
import om.tnavigator.JUnitTestCaseTestDefinitionOptions;
import om.tnavigator.JUnitTestCaseTestRealisation;
import om.tnavigator.JUnitTestCaseUserSession;
import om.tnavigator.NavigatorServlet;
import om.tnavigator.SummaryDetails;
import om.tnavigator.SummaryDetailsGeneration;
import om.tnavigator.SummaryTableBuilder;
import om.tnavigator.TestDeployment;
import om.tnavigator.scores.CombinedScore;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.misc.GeneralUtils;
import util.xml.XML;

/**
 * Here we test case the handling of the SummaryTable for the different types of
 *  student tests and presentation scenarios.
 * @author Trevor Hinson
 */

public class SummaryTableTesting extends AbstractTestCase {

	private JUnitTestCaseDatabaseAccess testDatabaseAccess;

	private JUnitTestCaseOmQueries queries;

	private Document d;

	private Node nParent;

	private List<List<Object>> getNormalData() {
		List<List<Object>> data = new ArrayList<List<Object>>();
		for (int i = 1; i < 6; i++) {
			List<Object> res = newRow(i);
			System.out.println(res);
			data.add(res);
		}
		return data;
	}

	private List<Object> newRow(int number) {
		List<Object> res = new ArrayList<Object>();
		res.add(number);
		res.add(number);
		res.add("sQ [" + number + "]");
		res.add("sA [" + number + "]");
		
//		change this ... to be the same as the sID !!!
		//res.add("last question : [" + number + "]" );
		res.add("samples.mu120.module5.question0" + number);
		res.add(number);
		res.add("Chapter " + number);
		return res;
	}

	public void tearDown() {
//		log.close();
//		File f = new File(getLogPath());
//		assertTrue(f.isDirectory());
//		System.out.println("Removing temp log files (if there are any) ... ");
//		File[] files = f.listFiles();
//		for (int i = 0; i < files.length; i++) {
//			File fil = files[i];
//			if (null != fil) {
//				if (fil.getName().contains(".log")) {
//					assertTrue(fil.delete());
//				}
//			}
//		}
	}

	public void setUp() throws Exception {
		boolean ok = true;
		super.setUp();
		try {
			testDatabaseAccess = new JUnitTestCaseDatabaseAccess();
			createParentNode();
		} catch (Exception e) {
			e.printStackTrace();
			ok = false;
		}
		assertTrue(ok);
	}

	private void createParentNode() throws Exception {
		d = XML.parse(pickUpFile(TEST_CASE_PROGRESS_SUMMARY));
		Map<String,String> mReplace=new HashMap<String,String>();
		// mReplace.put("EXTRA",endOfURL(request)); used to determine plain mode or not !
		mReplace.put("EXTRA", "");
		XML.replaceTokens(d, mReplace);
		Element eDiv = XML.find(d, "id", "summarytable");
		assertNotNull(eDiv);
		nParent = eDiv;
	}

	private SummaryDetails createSummaryDetails(JUnitTestCaseUserSession usession,
		boolean bPlain, boolean bIncludeQuestions, boolean bIncludeAttempts,
		boolean bIncludeScore) throws Exception {
		return SummaryDetailsGeneration.generateSummaryDetails(usession,
			nParent, bPlain, bIncludeQuestions, bIncludeAttempts, bIncludeScore);
	}

	private JUnitTestCaseUserSession createSession(String testID, boolean finished,
		long randomSeed, int fixedVariant, CombinedScore cs,
		JUnitTestCaseTestDefinitionOptions options) throws Exception {
		TestCaseNavigationServlet ns = new TestCaseNavigationServlet(log);
		JUnitTestCaseTestDefinition td = pickUpTestDefinition(BASIC_TEST_DEFINITION);
		td.optionsOverride(options);
		TestDeployment deploy = pickUpTestDeployment(BASIC_TEST_DEPLOYMENT);
		JUnitTestCaseTestRealisation rel = JUnitTestCaseTestRealisation
			.getTestRealisationInstance(new Integer(testID), td, cs);
		JUnitTestCaseUserSession usession = new JUnitTestCaseUserSession(ns, "123", td, deploy, rel);
		//usession.realiseTest(testID, finished, randomSeed, fixedVariant);
		//FIX THIS !
		if (true) { // test only if we are dealing with scores !
			usession.getTestRealisation().getScore(null, ns, null, null);
		}
		return usession;
	}

	/**
	 * Testing with :
	 *  plain = false
	 *  includeQuestions = false
	 *  includeAttempts = false
	 *  includeScore = false
	 */
	public void testSummaryTableBuildingNormalDataWithoutScores() throws Exception {
		SummaryTableTestCaseResultSet res = new SummaryTableTestCaseResultSet(getNormalData());
		queries = new SummaryTableTestCaseOmQueries(res);
		SummaryTableBuilder stb = new SummaryTableBuilder(testDatabaseAccess,
			queries);
		assertNotNull(log);
		CombinedScore cs = dummyScore();
		JUnitTestCaseUserSession usession = createSession("2", false, System.currentTimeMillis(), 1, cs, null);
		assertNotNull(usession);
		SummaryDetails sd = createSummaryDetails(usession, false, false, false, false);
		assertNotNull(sd);
		stb.addSummaryTable(sd);
		String results = XML.saveString(d);
		System.out.println(results);
		assertTrue(results.contains("<div id=\"summarytable\">"));
		for (int i = 1; i < 6; i++) {
			assertTrue(results.contains("Chapter " + i));
			assertTrue(results.contains("<tr class=\"answered\"><td>" + i + "</td></tr>"));
		}
	}

	public void testSummaryTableBuildingNormalDataNoScore() throws Exception {
		SummaryTableTestCaseResultSet res = new SummaryTableTestCaseResultSet(getNormalData());
		queries = new SummaryTableTestCaseOmQueries(res);
		SummaryTableBuilder stb = new SummaryTableBuilder(testDatabaseAccess,
			queries);
		assertNotNull(log);
		CombinedScore cs = dummyScore();
		JUnitTestCaseUserSession usession = createSession("2", false, System.currentTimeMillis(), 1, cs, null);
		assertNotNull(usession);
		SummaryDetails sd = createSummaryDetails(usession, true, true, true, false);
		assertNotNull(sd);
		stb.addSummaryTable(sd);
		String results = XML.saveString(d);
		System.out.println(results);
		assertTrue(results.contains("<div id=\"summarytable\">"));
		for (int i = 1; i < 6; i++) {
			assertTrue(results.contains("Chapter " + i));
			assertTrue(results.contains("Question " + i + ". Question: sQ [" + i + "]. Your answer: sA [" + i + "]. Result: Correct at " + i));
		}
	}

	public void testSummaryTableBuildingNormalDataPlainWithAttempts() throws Exception {
		SummaryTableTestCaseResultSet res = new SummaryTableTestCaseResultSet(getNormalData());
		queries = new SummaryTableTestCaseOmQueries(res);
		SummaryTableBuilder stb = new SummaryTableBuilder(testDatabaseAccess,
			queries);
		assertNotNull(log);
		CombinedScore cs = dummyScore();
		JUnitTestCaseUserSession usession = createSession("2", false, System.currentTimeMillis(), 1, cs, null);
		assertNotNull(usession);
		SummaryDetails sd = createSummaryDetails(usession, true, false, true, false);
		assertNotNull(sd);
		stb.addSummaryTable(sd);
		String results = XML.saveString(d);
		System.out.println(results);
		assertTrue(results.contains("<div id=\"summarytable\">"));
		for (int i = 1; i < 6; i++) {
			assertTrue(results.contains("Chapter " + i));
			assertTrue(results.contains("<h4>Chapter " + i + "</h4><div>Question " + i + ". Result: Correct at "));
		}
	}

	public void testSummaryTableBuildingNormalDataAllChecked() throws Exception {
		SummaryTableTestCaseResultSet res = new SummaryTableTestCaseResultSet(getNormalData());
		queries = new SummaryTableTestCaseOmQueries(res);
		SummaryTableBuilder stb = new SummaryTableBuilder(testDatabaseAccess,
			queries);
		assertNotNull(log);
		CombinedScore cs = dummyScore();
		JUnitTestCaseUserSession usession = createSession("2", false, System.currentTimeMillis(), 1, cs, null);
		assertNotNull(usession);
		SummaryDetails sd = createSummaryDetails(usession, true, true, true, true);
		assertNotNull(sd);
		stb.addSummaryTable(sd);
		String results = XML.saveString(d);
		System.out.println(results);
		assertTrue(results.contains("<div id=\"summarytable\">"));
		for (int i = 1; i < 6; i++) {
			assertTrue(results.contains("Chapter " + i));
			assertTrue(results.contains("Totals: Marks (Module5): 1.8. Out of: 18. Marks (Overall): 1.8. Out of: 18."));
		}
	}

	public void testOptionsOverriding() throws Exception {
		JUnitTestCaseTestDefinition td = pickUpTestDefinition(BASIC_TEST_DEFINITION);
		assertNotNull(td);
		JUnitTestCaseTestDefinitionOptions options = new JUnitTestCaseTestDefinitionOptions();
		options.setbEndSummary(false);
		td.optionsOverride(options);
		assertFalse(td.getEndSummaryValue());
	}

	public void testSummaryTableBuildingNormalDataPlainWithoutScores() throws Exception {
		SummaryTableTestCaseResultSet res = new SummaryTableTestCaseResultSet(getNormalData());
		queries = new SummaryTableTestCaseOmQueries(res);
		SummaryTableBuilder stb = new SummaryTableBuilder(testDatabaseAccess,
			queries);
		assertNotNull(log);
		CombinedScore cs = dummyScore();
		JUnitTestCaseUserSession usession = createSession("2", false, System.currentTimeMillis(), 1, cs, null);
		assertNotNull(usession);
		SummaryDetails sd = createSummaryDetails(usession, true, false, false, false);
		assertNotNull(sd);
		stb.addSummaryTable(sd);
		String results = XML.saveString(d);
		System.out.println(results);
		assertTrue(results.contains("<div id=\"summarytable\">"));
		for (int i = 1; i < 6; i++) {
			assertTrue(results.contains("Chapter " + i));
			assertTrue(results.contains("<tr class=\"answered\"><td>" + i + "</td></tr>"));
		}
	}

	public void testSummaryTableBuildingNormalDataIncludeQuestionsWithoutScores() throws Exception {
		SummaryTableTestCaseResultSet res = new SummaryTableTestCaseResultSet(getNormalData());
		queries = new SummaryTableTestCaseOmQueries(res);
		SummaryTableBuilder stb = new SummaryTableBuilder(testDatabaseAccess,
			queries);
		assertNotNull(log);
		CombinedScore cs = dummyScore();
		JUnitTestCaseUserSession usession = createSession("2", false, System.currentTimeMillis(), 1, cs, null);
		assertNotNull(usession);
		SummaryDetails sd = createSummaryDetails(usession, false, true, false, false);
		assertNotNull(sd);
		stb.addSummaryTable(sd);
		String results = XML.saveString(d);
		System.out.println(results);
		assertTrue(results.contains("<div id=\"summarytable\">"));
		for (int i = 1; i < 6; i++) {
			assertTrue(results.contains("<td colspan=\"3\">Chapter " + i));
			assertTrue(results.contains("<tr class=\"answered\"><td>" + i + "</td><td>" + i + "</td><td>sA [" + i + "]</td></tr>"));
		}
	}

	public void testSummaryTableBuildingNormalDataIncludeAttemptsWithoutScores() throws Exception {
		SummaryTableTestCaseResultSet res = new SummaryTableTestCaseResultSet(getNormalData());
		queries = new SummaryTableTestCaseOmQueries(res);
		SummaryTableBuilder stb = new SummaryTableBuilder(testDatabaseAccess,
			queries);
		assertNotNull(log);
		CombinedScore cs = dummyScore();
		JUnitTestCaseUserSession usession = createSession("2", false, System.currentTimeMillis(), 1, cs, null);
		assertNotNull(usession);
		SummaryDetails sd = createSummaryDetails(usession, false, false, true, false);
		assertNotNull(sd);
		stb.addSummaryTable(sd);
		String results = XML.saveString(d);
		System.out.println(results);
		assertTrue(results.contains("<div id=\"summarytable\">"));
		for (int i = 1; i < 6; i++) {
			assertTrue(results.contains("<td colspan=\"2\">Chapter " + i));
			assertTrue(results.contains("<tr class=\"answered\"><td>" + i + "</td><td>Correct at"));
		}
	}

	public void testSummaryTableBuildingNormalDataWithScores() throws Exception {
		SummaryTableTestCaseResultSet res = new SummaryTableTestCaseResultSet(getNormalData());
		queries = new SummaryTableTestCaseOmQueries(res);
		SummaryTableBuilder stb = new SummaryTableBuilder(testDatabaseAccess,
			queries);
		assertNotNull(log);
		CombinedScore cs = dummyScore();
		JUnitTestCaseUserSession usession = createSession("2", false, System.currentTimeMillis(), 1, cs, null);
		assertNotNull(usession);
		SummaryDetails sd = createSummaryDetails(usession, false, false, false, true);
		assertNotNull(sd);
		stb.addSummaryTable(sd);
		String results = XML.saveString(d);
		System.out.println(results);
		assertTrue(results.contains("<div id=\"summarytable\">"));
		for (int i = 1; i < 6; i++) {
			assertTrue(results.contains("<tr class=\"totals\"><td colspan=\"1\">Total</td><td>1.8</td><td>18</td><td>1.8</td><td>18</td></tr>"));
		}
	}

	/**
	 * Note that there are some large assumtions with this method ... if the
	 *  assumption if wrong then it is ok to fail (as that is part of the test).
	 * @param methods
	 * @param valueState
	 * @exception 
	 * @author Trevor Hinson
	 */
	private void invoke(Object obj, Method method, Object valueState)
		throws Exception {
		Object[] args = {valueState};
		try {
			method.invoke(obj, args);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private List<Method> retrieveInterestingMethods(Object def, String prefix,
		List<String> ignore) {
		List<Method> methods = new ArrayList<Method>();
		Method[] me = def.getClass().getMethods();
		for (int i = 0; i < me.length; i++) {
			Method m = me[i];
			if (m.getName().startsWith(prefix)) {
				if (!ignore.contains(m.getName())) {
					methods.add(m);
				}
			}
		}
		return methods;
	}

	private List<String> getMethodNameIgnoreList() {
		List<String> ignore = new ArrayList<String>();
		ignore.add("setsLabelSet");
		ignore.add("setsQuestionNumberHeader");
		return ignore;
	}

	/**
	 * Here we automatically test the many variations of the rendering for the
	 *  summary table.
	 * Iterate over each of the option cases until we identify where the issue
	 *  is make a number of combinations also.
	 */
	public void testStandardOptionVariations() throws Exception {
		recurseOptions(true, null, null, false, true, true, true);
	}

	public void testStandardOptionVariationsBuildingUp() throws Exception {
		recurseOptions(false, null, null, false, true, true, true);
	}

	public void testWithQuestionNumberHeader() throws Exception {
		recurseOptions(true, "questionNumberHeader", null, false, true, true, true);
	}

	public void testWithQuestionNumberHeaderBuildUp() throws Exception {
		recurseOptions(false, "questionNumberHeader", null, false, true, true, true);
	}

	public void testWithQuestionNumberHeaderEmpty() throws Exception {
		recurseOptions(true, "", null, false, true, true, true);
	}

	public void testWithQuestionNumberHeaderEmptyBuildUp() throws Exception {
		recurseOptions(false, "", null, false, true, true, true);
	}

	public void testBuildUpWithPlainNote() throws Exception {
		recurseOptions(false, "Tester", null, true, true, true, true);
	}

	public void testWithPlainNote() throws Exception {
		recurseOptions(true, "Tester", null, true, true, true, true);
	}

	public void testWithPlainNoteNoScore() throws Exception {
		recurseOptions(true, "Tester", null, true, true, true, false);
	}

	public void testWithPlainNoteNoScoreWithLabel() throws Exception {
		recurseOptions(true, "Tester", "label here", true, true, true, false);
	}

	public void testWithFalse() throws Exception {
		recurseOptions(false, null, null, false, false, false, false);
	}

	public void testWithTrue() throws Exception {
		recurseOptions(true, null, null, true, true, true, true);
	}

	private void recurseOptions(boolean reset, String questionNumberHeader, String labelBit,
		boolean bPlain, boolean bIncludeQuestions, boolean bIncludeAttempts, boolean bIncludeScores)
		throws Exception {
		JUnitTestCaseTestDefinitionOptions options = new JUnitTestCaseTestDefinitionOptions();
		List<Method> methods = retrieveInterestingMethods(options, "set", getMethodNameIgnoreList());
		assertNotNull(methods);
		assertTrue(methods.size() > 0);
		if (null != questionNumberHeader) {
			options.setsQuestionNumberHeader(questionNumberHeader);
		}
		if (null != labelBit) {
			options.setsLabelSet(labelBit);
		}
		for (int i = 0 ; i < methods.size() ; i++) {
			Method m = methods.get(i);
			assertNotNull(m);
			invoke(options, m, new Boolean(true));
			SummaryTableTestCaseResultSet res = new SummaryTableTestCaseResultSet(getNormalData());
			queries = new SummaryTableTestCaseOmQueries(res);
			SummaryTableBuilder stb = new SummaryTableBuilder(testDatabaseAccess,
				queries);
			assertNotNull(log);
			CombinedScore cs = dummyScore();
			JUnitTestCaseUserSession usession = createSession("2", false,
				System.currentTimeMillis(), 1, cs, options);
			SummaryDetails sd = createSummaryDetails(usession, bPlain, bIncludeQuestions, bIncludeAttempts, bIncludeScores);
			assertNotNull(sd);
			stb.addSummaryTable(sd);
			String results = XML.saveString(d);
			System.out.println(results);
			assertTrue(results.contains("<div id=\"summarytable\">"));
			for (int k = 1; k < 6; k++) {
				assertTrue(results.contains("<tr class=\"answered\"><td>" + k + "</td>"));
			}
			if (reset) {
				invoke(options, m, null);
			}
		}
	}

	public void testSummaryTableBuildingNormalDataEverythingNotPlain() throws Exception {
		SummaryTableTestCaseResultSet res = new SummaryTableTestCaseResultSet(getNormalData());
		queries = new SummaryTableTestCaseOmQueries(res);
		SummaryTableBuilder stb = new SummaryTableBuilder(testDatabaseAccess,
			queries);
		assertNotNull(log);
		CombinedScore cs = dummyScore();
		JUnitTestCaseTestDefinitionOptions options = new JUnitTestCaseTestDefinitionOptions();
		// if this is true then we fail with the numbering !!!
		options.allBooleans(true);
		
		options.setbNumberBySection(true);
		
		JUnitTestCaseUserSession usession = createSession("2", false,
			System.currentTimeMillis(), 1, cs, null);
		
		JUnitTestCaseTestDefinition tds = usession.getJUnitTestDefinition();
		String header = tds.getsQuestionNumberHeader();
		System.out.println("header = [" + header + "]");
		
		String section = "";
		
		tds.setField("sQuestionNumberHeader", section);

		String header2 = tds.getsQuestionNumberHeader();
		System.out.println("header2 = [" + header2 + "]");
		assertEquals(header2, section);
		SummaryDetails sd = createSummaryDetails(usession, false, true, true, true);
		assertNotNull(sd);
		stb.addSummaryTable(sd);
		String results = XML.saveString(d);
		System.out.println(results);
		assertTrue(results.contains("<div id=\"summarytable\">"));
		for (int i = 1; i < 6; i++) {
			assertTrue(results.contains("<tr class=\"answered\"><td>" + i + "</td>"));
		}
	}

	public void testSummaryTableBuildingNormalDataEverythingFalseNotPlain() throws Exception {
		SummaryTableTestCaseResultSet res = new SummaryTableTestCaseResultSet(getNormalData());
		queries = new SummaryTableTestCaseOmQueries(res);
		SummaryTableBuilder stb = new SummaryTableBuilder(testDatabaseAccess,
			queries);
		assertNotNull(log);
		CombinedScore cs = dummyScore();
		JUnitTestCaseTestDefinitionOptions options = new JUnitTestCaseTestDefinitionOptions();
		
		options.allBooleans(false);
		
		JUnitTestCaseUserSession usession = createSession("2", false,
			System.currentTimeMillis(), 1, cs, options);
		assertNotNull(usession);
		SummaryDetails sd = createSummaryDetails(usession, false, true, true, true);
		assertNotNull(sd);
		stb.addSummaryTable(sd);
		String results = XML.saveString(d);
		System.out.println(results);
		assertTrue(results.contains("<div id=\"summarytable\">"));
		for (int i = 1; i < 6; i++) {
			assertTrue(results.contains("<tr class=\"answered\"><td>" + i + "</td>"));
		}
	}

	public void testNumberbysectionANDquestionnumberheaderBlank() throws Exception {
		SummaryTableTestCaseResultSet res = new SummaryTableTestCaseResultSet(getNormalData());
		queries = new SummaryTableTestCaseOmQueries(res);
		SummaryTableBuilder stb = new SummaryTableBuilder(testDatabaseAccess,
			queries);
		assertNotNull(log);
		CombinedScore cs = dummyScore();
		JUnitTestCaseTestDefinitionOptions options = new JUnitTestCaseTestDefinitionOptions();
		options.allBooleans(false);
		JUnitTestCaseUserSession usession = createSession("2", false,
			System.currentTimeMillis(), 1, cs, options);
		assertNotNull(usession);
		SummaryDetails sd = createSummaryDetails(usession, false, true, true, true);
		assertNotNull(sd);
		stb.addSummaryTable(sd);
		String results = XML.saveString(d);
		System.out.println(results);
		assertTrue(results.contains("<div id=\"summarytable\">"));
		for (int i = 1; i < 6; i++) {
			assertTrue(results.contains("<tr class=\"answered\"><td>" + i + "</td>"));
		}
	}

	public void testRetrieveSummaryConfirmation() throws Exception {
		JUnitTestCaseTestDefinition def = pickUpTestDefinition(BASIC_TEST_DEFINITION);
		assertNotNull(def);
		String s = def.retrieveSummaryConfirmation();
		
		assertNotNull(s);
		System.out.println(s);
		assertEquals(s, "Completed");
	}

	public void testRetrieveSummaryConfirmationWithDeletedDocument() throws Exception {
		JUnitTestCaseTestDefinition def = pickUpTestDefinition(BASIC_TEST_DEFINITION);
		def.deleteDocumentForTesting();
		String s = def.retrieveSummaryConfirmation();
		assertNull(s);
	}

	private class SummaryTableTestCaseResultSet extends JUnitTestCaseResultSet {
		
		private List<List<Object>> results;
		
		private int currentPosition = -1;
		
		public SummaryTableTestCaseResultSet(List<List<Object>> play) {
			results = play;
		}
		
		@Override
		// Here we move through the composite results ...
		public boolean next() throws SQLException {
			boolean b = false;
			if (null != results ? currentPosition < results.size() - 1 : false) {
				currentPosition++;
				System.out.println("currentPosition = " + currentPosition);
				b = true;
			}
			return b;
		}
		
		@Override
		public String getString(int columnIndex) throws SQLException {
			String s = null;
			Object obj = retrieve(columnIndex - 1);
			if (null != obj ? obj instanceof String : false) {
				s = (String) obj;
			}
			return s;
		}
		
		@Override
		public int getInt(int columnIndex) throws SQLException {
			int n = -1;
			Object obj = retrieve(columnIndex -1);
			if (null != obj ? obj instanceof Integer : false) {
				n = (Integer) obj;
			}
			return n;
		}
		
		private Object retrieve(int columnIndex) {
			Object obj = null;
			if (null != results) {
				List<Object> res = results.get(currentPosition);
				if (null != res ? columnIndex < res.size() : false) {
					obj = res.get(columnIndex);
				}
			}
			return obj;
		}
		
	}

	private class SummaryTableTestCaseOmQueries extends JUnitTestCaseOmQueries {

		public SummaryTableTestCaseOmQueries(SummaryTableTestCaseResultSet res) {
			super(res);
		}

		public ResultSet querySummary(DatabaseAccess.Transaction dat,int ti)
			throws SQLException {
			return dummyResults;
		}
		
	}

}
