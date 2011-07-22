package om.administration.questionbank;

import om.RequestHandlingException;
import junit.framework.TestCase;

public class TestQuestionBankCleaningRequestHandler extends TestCase {

	private static String VALID_CLEANER = "om.administration.questionbank.QuestionBankCleaner";

	public void testRetrieveConfiguredCleanerInvalid() throws Exception {
		QuestionBankCleaningRequestHandler handler = new QuestionBankCleaningRequestHandler();
		try {
			handler.retrieveConfiguredCleaner("tester");
			assertTrue(false);
		} catch (Exception x) {
			assertTrue(x instanceof RequestHandlingException);
			System.out.println(x.getMessage());
			assertTrue(x.getMessage().contains("java.lang.ClassNotFoundException"));
		}
	}

	public void testRetrieveConfiguredCleanerWithNull() throws Exception {
		QuestionBankCleaningRequestHandler handler = new QuestionBankCleaningRequestHandler();
		CleanQuestionBanks cqb = handler.retrieveConfiguredCleaner(null);
		assertNotNull(cqb);
		assertTrue(cqb instanceof QuestionBankCleaner);
	}

	public void testRetrieveConfiguredCleanerWithValidString() throws Exception {
		QuestionBankCleaningRequestHandler handler = new QuestionBankCleaningRequestHandler();
		CleanQuestionBanks cqb = handler.retrieveConfiguredCleaner(VALID_CLEANER);
		assertNotNull(cqb);
		assertTrue(cqb instanceof QuestionBankCleaner);
	}
	
}
