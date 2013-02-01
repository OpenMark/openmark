package om.administration.questionbank;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import om.AbstractTestCase;

import org.junit.Test;

public class TestQuestionBankCleanerParse extends AbstractTestCase {

	private static String ICMA = "sdk125-10j.icma46.test.xml";

	@Test public void testRetrieveCompositeQuestions() throws Exception {
		File f = pickUpFile(BASIC_TEST_DEFINITION);
		assertNotNull(f);
		QuestionBankCleaner qbc = new QuestionBankCleaner();
		List<String> questions = qbc.retrieveCompositeQuestions(f);
		assertNotNull(questions);
		assertTrue(questions.size() == 6);
	}

	@Test public void testRetrieveCompositeQuestionsAlternative() throws Exception {
		File f = pickUpFile(ICMA, TestQuestionBankCleanerParse.class);
		assertNotNull(f);
		QuestionBankCleaner qbc = new QuestionBankCleaner();
		List<String> questions = qbc.retrieveCompositeQuestions(f);
		assertNotNull(questions);
		assertTrue(questions.size() == 8);
	}

}
