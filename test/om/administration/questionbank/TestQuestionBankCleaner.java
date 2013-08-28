package om.administration.questionbank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import om.AbstractTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.misc.GeneralUtils;
import util.misc.StandardFileFilter;
import util.misc.Strings;

/**
 * Testing the cleaning of QuestionBanks.
 * 
 * @author Trevor Hinson
 */

public class TestQuestionBankCleaner extends AbstractTestCase {

	private static String FILE_NAME = "testcase-samples.mu120.module5.question";

	private static String QUESTION_BANK_ONE = "questionbankOne";

	private static String QUESTION_BANK_TWO = "questionbankTwo";

	private static String TEST_BANK_ONE = "testbankOne";

	private static String TEST_BANK_TWO = "testbankTwo";

	private File question;

	private File testDeployment;

	private File test;

	private File currentLocation;

	private File tempDirectory;

	private File tempQuestionBankDirectoryOne;

	private File tempQuestionBankDirectoryTwo;

	private File tempTestBankDirectoryOne;

	private File tempTestBankDirectoryTwo;

	@After
	public void tearDown() throws Exception {
		createReferenceIfNull();
		deleteDirectoryContents(tempQuestionBankDirectoryOne);
		assertTrue(tempQuestionBankDirectoryOne.delete());
		deleteDirectoryContents(tempQuestionBankDirectoryTwo);
		assertTrue(tempQuestionBankDirectoryTwo.delete());
		deleteDirectoryContents(tempTestBankDirectoryOne);
		assertTrue(tempTestBankDirectoryOne.delete());
		deleteDirectoryContents(tempTestBankDirectoryTwo);
		assertTrue(tempTestBankDirectoryTwo.delete());
		assertTrue(tempDirectory.delete());
	}

	/**
	 * Here we just enable things to reference correctly.  The need for this is
	 *  in the event of any test failing then they tearDown() still needs to
	 *  run and we need to tidy things up correctly so that the tests can run
	 *  properly next time.  AS we are adding to the File system we just need to
	 *  ensure that we tidy up any mess we leave in the result of something
	 *  breaking.
	 * @author Trevor Hinson
	 */
	private void createReferenceIfNull() throws Exception {
		question = retrieveFile(FILE_NAME + "01.jar");
		assertNotNull(question);
		getUnitTestLocation();
		if (null == tempDirectory) {
			tempDirectory = new File(currentLocation.getAbsolutePath()
				+ File.separator + "temp");
		}
		assertNotNull(tempDirectory);
		tempQuestionBankDirectoryOne = createReferenceIfNeeded(
			tempQuestionBankDirectoryOne, QUESTION_BANK_ONE);
		tempQuestionBankDirectoryTwo = createReferenceIfNeeded(
			tempQuestionBankDirectoryTwo, QUESTION_BANK_TWO);
		tempTestBankDirectoryOne = createReferenceIfNeeded(
			tempTestBankDirectoryOne, TEST_BANK_ONE);
		tempTestBankDirectoryTwo = createReferenceIfNeeded(
			tempTestBankDirectoryTwo, TEST_BANK_TWO);
	}

	private File createReferenceIfNeeded(File f, String name) {
		assertNotNull(tempDirectory);
		if (null == f) {
			assertNotNull(name);
			f = new File(tempDirectory.getAbsolutePath() + File.separator + name);
		}
		return f;
	}

	private void deleteDirectoryContents(File dir) {
		assertNotNull(dir);
		if (dir.exists()) {
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				assertTrue(f.delete());
			}
		}
	}

	private void getUnitTestLocation() {
		int end = question.getAbsolutePath().lastIndexOf(File.separator);
		assertTrue(end > 0);
		String location = question.getAbsolutePath().substring(0, end);
		currentLocation = new File(location);
	}

	@Before
	public void setUp() throws Exception {
		question = retrieveFile(FILE_NAME + "01.jar");
		assertNotNull(question);
		getUnitTestLocation();
		assertNotNull(currentLocation);
		assertTrue(currentLocation.canRead());
		assertTrue(currentLocation.canWrite());
		testDeployment = pickUpFile(BASIC_TEST_DEPLOYMENT);
		assertNotNull(testDeployment);
		test = pickUpFile(BASIC_TEST_DEFINITION);
		assertNotNull(test);
		createTempDirectory();
		
		tempQuestionBankDirectoryOne = createWithinTempDirectory(QUESTION_BANK_ONE);
		tempQuestionBankDirectoryTwo = createWithinTempDirectory(QUESTION_BANK_TWO);
		tempTestBankDirectoryOne = createWithinTempDirectory(TEST_BANK_ONE);
		tempTestBankDirectoryTwo = createWithinTempDirectory(TEST_BANK_TWO);
			
		createCopy(test, tempTestBankDirectoryOne.getAbsolutePath());
		createCopy(testDeployment, tempTestBankDirectoryOne.getAbsolutePath());
		
		generateDummyTestDetails();
		
		generateDummyQuestions();
		GeneralUtils.copyDirectory(tempTestBankDirectoryOne, tempTestBankDirectoryTwo);
		GeneralUtils.copyDirectory(tempQuestionBankDirectoryOne, tempQuestionBankDirectoryTwo);

	}

	protected void generateDummyTestDetails() throws Exception {
		for (int i = 1; i < 5; i++) {
			makeCopyTestsFiles(test, BASIC_TEST_DEFINITION, i);
			makeCopyTestsFiles(testDeployment, BASIC_TEST_DEPLOYMENT, i);
		}
	}

	protected void makeCopyTestsFiles(File f, String name, int counter) throws Exception {
		int n = name.lastIndexOf(".");
		String sr = name.substring(0, n);
		int k = sr.lastIndexOf(".");
		String prefix = sr.substring(0, k - 1);
		String suffix = sr.substring(k + 1, sr.length());
		String deployName = prefix + counter + "-";
		File temp = File.createTempFile(deployName, "." + suffix + ".xml",
			tempTestBankDirectoryOne); 
		GeneralUtils.copyFile(f, temp);
	}

	protected void generateDummyQuestions() throws Exception {
		assertNotNull(tempQuestionBankDirectoryOne);
		assertTrue(tempQuestionBankDirectoryOne.exists());
		assertNotNull(question);
		assertTrue(question.exists());
		for (int i = 1; i < 6; i++) {
			String name = FILE_NAME + "0" + i + "-";
			File f = File.createTempFile(name, ".jar",
					tempQuestionBankDirectoryOne);
			GeneralUtils.copyFile(question, f);
		}
	}

	/**
	 * Generates copies of the file given within the location provided so that
	 * these temp files can be utilised within the test case.
	 * 
	 * @author Trevor Hinson
	 */
	private void createCopy(File fileToCopy, String toLocation)
		throws Exception {
		assertNotNull(fileToCopy);
		assertTrue(Strings.isNotEmpty(toLocation));
		File newFile = new File(toLocation + File.separator
				+ fileToCopy.getName());
		GeneralUtils.copyFile(fileToCopy, newFile);
	}

	private File createWithinTempDirectory(String name) {
		assertNotNull(tempDirectory);
		File dir = new File(tempDirectory.getAbsolutePath() + File.separator
				+ name);
		assertNotNull(dir);
		if (!dir.exists()) {
			assertTrue(dir.mkdir());
		}
		return dir;
	}

	private void createTempDirectory() throws Exception {
		assertNotNull(currentLocation);
		File newDirectory = new File(currentLocation.getAbsolutePath()
				+ File.separator + "temp");
		if (!newDirectory.exists()) {
			assertTrue(newDirectory.mkdir());
		}
		tempDirectory = newDirectory;
		
	}

	private File retrieveFile(String name) throws Exception {
		URL url = TestQuestionBankCleaner.class.getResource(name);
		assertNotNull(url);
		String replacedPath = url.getPath().replace("%20", " ");
		return new File(replacedPath);
	}

	private List<String> pickUpTestLocations() {
		List<String> dirs = new ArrayList<String>();
		dirs.add(tempTestBankDirectoryOne.getAbsolutePath());
		dirs.add(tempTestBankDirectoryTwo.getAbsolutePath());
		return dirs;
	}

	private List<String> pickUpQuestionLocations() {
		List<String> dirs = new ArrayList<String>();
		dirs.add(tempQuestionBankDirectoryOne.getAbsolutePath());
		dirs.add(tempQuestionBankDirectoryTwo.getAbsolutePath());
		return dirs;
	}

	@Test public void testIdentifyAllTests() throws Exception {
		ClearanceResponse cr = new ClearanceResponse();
		QuestionBankCleaner cqb = new QuestionBankCleaner();
		Map<String, TestSynchronizationCheck> tests = cqb.identifyAllTests(
			pickUpTestLocations(), cr, new StandardFileFilter("*.xml"));
		assertNotNull(tests);
		assertEquals(tests.size(), 5);
	}

	@Test public void testRetrieveCompositeQuestions() throws Exception {
		QuestionBankCleaner cqb = new QuestionBankCleaner();
		QuestionAndTestBankLocations locs = new QuestionAndTestBankLocations();
		locs.setTestBanks(pickUpTestLocations());
		locs.setQuestionBanks(pickUpQuestionLocations());
		List<String> questions = cqb.retrieveCompositeQuestions(test);
		assertNotNull(questions);
		assertTrue(questions.size() == 6);
	}

	@Test public void testGetQuestionsEmpty() throws Exception {
		List<String> results = QuestionBankCleaner.getQuestions("");
		assertNotNull(results);
		assertTrue(results.size() == 0);
	}

	@Test public void testGetQuestionsNulled() throws Exception {
		List<String> results = QuestionBankCleaner.getQuestions(null);
		assertNotNull(results);
		assertTrue(results.size() == 0);
	}

	@Test public void testGetQuestionsIncorrect() throws Exception {
		List<String> results = QuestionBankCleaner.getQuestions("/tester1/sdfasdf");
		assertNotNull(results);
		assertTrue(results.size() == 0);
	}

	@Test public void testGetQuestionsSingled() throws Exception {
		String lookFor = "/questionBank1/sdk125b6.question06f.1.3.jar";
		List<String> results = QuestionBankCleaner
			.getQuestions(lookFor);
		assertNotNull(results);
		assertTrue(results.size() == 1);
		String s = results.get(0);
		assertNotNull(s);
		assertEquals(s, lookFor);
	}

	@Test public void testGetQuestionsMultiples() throws Exception {
		List<String> results = QuestionBankCleaner
			.getQuestions("/tester1/sdk125b6.question06f.1.3.jar, /tester2/sdk125b6.question06f.1.3.jar");
		assertNotNull(results);
		assertTrue(results.size() == 2);
		for (String s : results) {
			assertTrue(!s.startsWith(","));
			assertTrue(!s.startsWith(" "));
		}
	}

	@Test public void testIdentifySyncOfTest() throws Exception {
		ClearanceResponse cr = new ClearanceResponse();
		QuestionBankCleaner cqb = new QuestionBankCleaner();
		Map<String, TestSynchronizationCheck> checks = cqb.identifyAllTests(
			pickUpTestLocations(), cr, new StandardFileFilter("*.xml"));
		assertNotNull(checks);
		QuestionBankCleaner.Banks b = cqb.new Banks();
		b.numberOfTestBanks = 2;
		for (TestSynchronizationCheck test : checks.values()) {
			assertTrue(cqb.identifySyncStatus(test, b));
		}
	}

	@Test public void testArchive() throws Exception {
		ClearanceResponse cr = new ClearanceResponse();
		QuestionBankCleaner cqb = new QuestionBankCleaner();
		assertNotNull(tempQuestionBankDirectoryOne);
		File[] files = tempQuestionBankDirectoryOne.listFiles();
		assertNotNull(files);
		assertTrue(files.length > 0);
		File fileToArchive = files[0];
		assertNotNull(fileToArchive);
		String archiveName = tempQuestionBankDirectoryOne.getAbsolutePath()
			+ File.separator + cqb.renderDate();
		File archive = new File(archiveName);
		assertTrue(archive.mkdir());
		cqb.archieveIndividualQuestion(fileToArchive, archive, cr, null);
		assertFalse(fileToArchive.exists());
		File[] archivedFiles = archive.listFiles();
		assertTrue(archivedFiles.length == 1);
		for (int i = 0; i < archivedFiles.length; i++) {
			File f = archivedFiles[i];
			f.delete();
		}
		assertTrue(archive.delete());
	}
	
}
