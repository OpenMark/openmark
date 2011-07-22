package om.administration.questionbank;

import java.io.File;
import java.util.List;

import om.AbstractTestCase;

public class TestQuestionBankCleanerParse extends AbstractTestCase {

	private static String ICMA = "sdk125-10j.icma46.test.xml";

	public void testRetrieveCompositeQuestions() throws Exception {
		File f = pickUpFile(BASIC_TEST_DEFINITION);
		assertNotNull(f);
		QuestionBankCleaner qbc = new QuestionBankCleaner();
		List<String> questions = qbc.retrieveCompositeQuestions(f);
		assertNotNull(questions);
		assertTrue(questions.size() == 6);
	}

	public void testRetrieveCompositeQuestionsAlternative() throws Exception {
		File f = pickUpFile(ICMA);
		assertNotNull(f);
		QuestionBankCleaner qbc = new QuestionBankCleaner();
		List<String> questions = qbc.retrieveCompositeQuestions(f);
		assertNotNull(questions);
		System.out.println(questions.size());
		assertTrue(questions.size() == 8);
	}

}
