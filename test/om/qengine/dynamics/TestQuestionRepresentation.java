package om.qengine.dynamics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;

import om.AbstractTestCase;

import org.junit.Test;

public class TestQuestionRepresentation extends AbstractTestCase {

	private static String TEST_FOR = "The composite representation of a"
			+ " Question Java class is not valid :";

	@Test public void testIncorrectQuestionRepresentation() throws Exception {
		QuestionRepresentation qr = new QuestionRepresentation("", "");
		try {
			qr.getRepresentation();
			assertTrue(false);
		} catch (Exception x) {
			assertTrue(x.getMessage().contains(TEST_FOR));
		}
	}

	@Test public void testNullQuestionRepresentation() throws Exception {
		QuestionRepresentation qr = new QuestionRepresentation(null, null);
		try {
			qr.getRepresentation();
			assertTrue(false);
		} catch (Exception x) {
			assertTrue(x.getMessage().contains(TEST_FOR));
		}
	}

	private static String WRONG_TEMPLATED_JAVA = "package om.dynamic.questions.tester;"
		+ LINE_SEPERATOR + "import om.question.*;"
		+ LINE_SEPERATOR + "import om.helper.*;"
		+ LINE_SEPERATOR + "public class Tester {"
		+ LINE_SEPERATOR + LINE_SEPERATOR + "public Rendering init(Document d,InitParams ip) throws OmException {"
		+ LINE_SEPERATOR + "boolean b = false;"
		+ LINE_SEPERATOR + "}"
		+ LINE_SEPERATOR + LINE_SEPERATOR + "protected abstract boolean isRight(int attempt) throws OmDeveloperException {"
		+ LINE_SEPERATOR + "return true;"
		+ LINE_SEPERATOR + "}"
		+ LINE_SEPERATOR + "}";

	private static String TEMPLATED_JAVA = "package om.dynamic.questions.tester;"
		+ LINE_SEPERATOR + "import om.question.*;"
		+ LINE_SEPERATOR + "import om.helper.*;"
		+ LINE_SEPERATOR + "public class Tester {0} ["
		+ LINE_SEPERATOR + LINE_SEPERATOR + "public Rendering init(Document d,InitParams ip) throws OmException ["
		+ LINE_SEPERATOR + "boolean b = false;"
		+ LINE_SEPERATOR + "]"
		+ LINE_SEPERATOR + LINE_SEPERATOR + "protected abstract boolean isRight(int attempt) throws OmDeveloperException ["
		+ LINE_SEPERATOR + "return true;"
		+ LINE_SEPERATOR + "]"
		+ LINE_SEPERATOR + "]";

	private static String CLASS_NAME = "om.dynamic.questions.tester.Tester";

	@Test public void testWrongQuestionRepresentation() throws Exception {
		QuestionRepresentation qr = new QuestionRepresentation(WRONG_TEMPLATED_JAVA, CLASS_NAME);
		try {
			qr.getRepresentation();
			assertTrue(false);
		} catch (Exception x) {
			assertTrue(x.getMessage().contains(TEST_FOR));
		}
	}

	@Test public void testQuestionRepresentation() throws Exception {
		Object[] arguments = {"implements Question"};
		String check = MessageFormat.format(TEMPLATED_JAVA, arguments);
		QuestionRepresentation qr = new QuestionRepresentation(check, CLASS_NAME);
		try {
			String representation = qr.getRepresentation();
			assertNotNull(representation);
		} catch (Exception x) {
			assertFalse(x.getMessage().contains(TEST_FOR));
		}
	}

	@Test public void testStandardQuestionRepresentation() throws Exception {
		Object[] arguments = {"extends StandardQuestion"};
		String check = MessageFormat.format(TEMPLATED_JAVA, arguments);
		check = check.replace("[", "{");
		check = check.replace("]", "}");
		QuestionRepresentation qr = new QuestionRepresentation(check, CLASS_NAME);
		try {
			String representation = qr.getRepresentation();
			assertNotNull(representation);
		} catch (Exception x) {
			assertFalse(x.getMessage().contains(TEST_FOR));
		}
	}

	@Test public void testSimpleQuestion1QuestionRepresentation() throws Exception {
		Object[] arguments = {"extends SimpleQuestion1"};
		String check = MessageFormat.format(TEMPLATED_JAVA, arguments);
		check = check.replace("[", "{");
		check = check.replace("]", "}");
		QuestionRepresentation qr = new QuestionRepresentation(check, CLASS_NAME);
		try {
			String representation = qr.getRepresentation();
			assertNotNull(representation);
		} catch (Exception x) {
			assertFalse(x.getMessage().contains(TEST_FOR));
		}
	}

	@Test public void testSimpleQuestionRepresentation() throws Exception {
		Object[] arguments = {"extends SimpleQuestion1ForIAT"};
		String check = MessageFormat.format(TEMPLATED_JAVA, arguments);
		check = check.replace("[", "{");
		check = check.replace("]", "}");
		QuestionRepresentation qr = new QuestionRepresentation(check, CLASS_NAME);
		try {
			String representation = qr.getRepresentation();
			assertNotNull(representation);
		} catch (Exception x) {
			assertFalse(x.getMessage().contains(TEST_FOR));
		}
	}

	@Test public void testDeferredFeedbackQuestion1Representation() throws Exception {
		Object[] arguments = {"extends DeferredFeedbackQuestion1"};
		String check = MessageFormat.format(TEMPLATED_JAVA, arguments);
		check = check.replace("[", "{");
		check = check.replace("]", "}");
		QuestionRepresentation qr = new QuestionRepresentation(check, CLASS_NAME);
		try {
			String representation = qr.getRepresentation();
			assertNotNull(representation);
		} catch (Exception x) {
			assertFalse(x.getMessage().contains(TEST_FOR));
		}
	}

	@Test public void testSimpleQuestion20ForIATRepresentation() throws Exception {
		Object[] arguments = {"extends SimpleQuestion20ForIAT"};
		String check = MessageFormat.format(TEMPLATED_JAVA, arguments);
		check = check.replace("[", "{");
		check = check.replace("]", "}");
		QuestionRepresentation qr = new QuestionRepresentation(check, CLASS_NAME);
		try {
			String representation = qr.getRepresentation();
			assertNotNull(representation);
		} catch (Exception x) {
			assertFalse(x.getMessage().contains(TEST_FOR));
		}
	}

	@Test public void testSimpleQuestion3Representation() throws Exception {
		Object[] arguments = {"extends SimpleQuestion3"};
		String check = MessageFormat.format(TEMPLATED_JAVA, arguments);
		check = check.replace("[", "{");
		check = check.replace("]", "}");
		QuestionRepresentation qr = new QuestionRepresentation(check, CLASS_NAME);
		try {
			String representation = qr.getRepresentation();
			assertNotNull(representation);
		} catch (Exception x) {
			assertFalse(x.getMessage().contains(TEST_FOR));
		}
	}

}
