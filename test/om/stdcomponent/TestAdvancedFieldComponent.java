package om.stdcomponent;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import om.question.InitParams;
import om.question.Rendering;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.xml.XML;

public class TestAdvancedFieldComponent extends AbstractComponentTesting {

	private static String QUESTION_WITH_SUPER_SCRIPT = "s383.icma55.q04angular.1.7.jar"; 

	@Test public void testQuestionHolder() throws Exception {
		File fJar = pickUpFile(QUESTION_WITH_SUPER_SCRIPT);
		QuestionHolder qh = getQuestionHolder(fJar, newInitParams());
		assertNotNull(qh);
	}

	@Test public void testProductVisibleOutput() throws Exception {
		File fJar = pickUpFile(QUESTION_WITH_SUPER_SCRIPT);
		InitParams initParams = newInitParams();
		QuestionHolder qh = getQuestionHolder(fJar, initParams);	
		Rendering r = qh.question.init(qh.document, initParams);
		assertNotNull(r);
		Element xhtml = r.getXHTML();
		assertNotNull(xhtml);
		String output = XML.saveString(xhtml);
		assertNotNull(output);
		assertTrue(output.contains("%%IDPREFIX%%omval_eInput"));
		Element e = XML.getNestedChild(xhtml, "input");
		assertNotNull(e);
		Node n = e.getParentNode();
		assertNotNull(n);
		assertTrue(Node.ELEMENT_NODE == n.getNodeType());
		String s = XML.saveString((Element) n);
		assertNotNull(s);
	}
}
