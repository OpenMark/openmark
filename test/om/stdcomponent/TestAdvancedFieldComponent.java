package om.stdcomponent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import om.question.InitParams;
import om.question.Rendering;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.xml.XML;

public class TestAdvancedFieldComponent extends AbstractComponentTesting {

	private static String QUESTION_WITH_SUPER_SCRIPT = "s383.icma55.q04angular.1.7.jar"; 

	public void setUp() {}

	public void testQuestionHolder() throws Exception {
		File fJar = pickUpFile(QUESTION_WITH_SUPER_SCRIPT);
		QuestionHolder qh = getQuestionHolder(fJar, newInitParams());
		assertNotNull(qh);
	}

	public void testProductVisibleOutput() throws Exception {
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
		System.out.println("-----");
		System.out.println(s);

//		Resource[] resources = r.getResources();
//		assertNotNull(resources);
//		System.out.println(resources);
//		assertTrue(resources.length > 0);
//		for (int i = 0; i < resources.length; i++) {
//			Resource res = resources[i];
//			assertNotNull(res);
//			String name = res.getFilename();
//			assertNotNull(name);
//			if ("script.js".equals(name)) {
//				System.out.println(new StringBuffer().append(
//					new String(res.getContent())));
//			}
//		}

	}

	

}