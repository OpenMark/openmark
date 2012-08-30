package om.administration.questionbank;

import junit.framework.TestCase;
import om.RequestHandlingException;

import org.junit.Test;

public class TestQuestionBankCleaningRequestHandler extends TestCase {

	private static String VALID_CLEANER = "om.administration.questionbank.QuestionBankCleaner";

	@Test public void testRetrieveConfiguredCleanerInvalid() throws Exception {
		QuestionBankCleaningRequestHandler handler = new QuestionBankCleaningRequestHandler();
		try {
			handler.retrieveConfiguredCleaner("tester");
			assertTrue(false);
		} catch (Exception x) {
			assertTrue(x instanceof RequestHandlingException);
			assertTrue(x.getMessage().contains("java.lang.ClassNotFoundException"));
		}
	}

	@Test public void testRetrieveConfiguredCleanerWithNull() throws Exception {
		QuestionBankCleaningRequestHandler handler = new QuestionBankCleaningRequestHandler();
		CleanQuestionBanks cqb = handler.retrieveConfiguredCleaner(null);
		assertNotNull(cqb);
		assertTrue(cqb instanceof QuestionBankCleaner);
	}

	@Test public void testRetrieveConfiguredCleanerWithValidString() throws Exception {
		QuestionBankCleaningRequestHandler handler = new QuestionBankCleaningRequestHandler();
		CleanQuestionBanks cqb = handler.retrieveConfiguredCleaner(VALID_CLEANER);
		assertNotNull(cqb);
		assertTrue(cqb instanceof QuestionBankCleaner);
	}
	
}
