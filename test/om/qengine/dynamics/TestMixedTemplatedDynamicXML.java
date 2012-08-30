package om.qengine.dynamics;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import om.AbstractTestCase;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.xml.XML;

public class TestMixedTemplatedDynamicXML extends AbstractTestCase {

	private static String CODED_TEMPLATE = "<handler handlerType=\"Mixed\" packageSuffix=\"testing\" className=\"CodedTemplate\" extends=\"om.helper.SimpleQuestion3\">"
		+ "<additionalImports>"
		+ "import com.testing;"
		+ "</additionalImports>"
		+ "<variables>"
		+ "private boolean isSet;"
		+ "</variables>"
		+ "<init>"
		+ "isSet = true;"
		+ "</init>"
		+ "<isRight>"
		+ "return isSet;"
		+ "</isRight>"
		+ "</handler>";

	protected Element createElement() throws Exception {
		Document doc = XML.parse(CODED_TEMPLATE);
		return doc.getDocumentElement();
	}

	@Test public void testMixedTemplate() throws Exception {
		MixedBuilderType mht = new MixedBuilderType();
		Element e = createElement();
		QuestionRepresentation qr = mht.generateClassRepresentation(e);
		assertNotNull(qr);
		assertNotNull(qr.getFullClassName());
		String generatedJava = qr.getRepresentation();
		assertNotNull(generatedJava);
		assertTrue(generatedJava.contains("om.dynamic.questions.testing;"));
		assertTrue(generatedJava.contains("CodedTemplate"));
		assertTrue(generatedJava.contains("om.helper.SimpleQuestion3"));
		assertTrue(generatedJava.contains("import com.testing;"));
		assertTrue(generatedJava.contains("private boolean isSet"));
		assertTrue(generatedJava.contains("isSet = true"));
		assertTrue(generatedJava.contains("return isSet;"));
	}

}
