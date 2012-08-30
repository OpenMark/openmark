package om.tnavigator.request.authorship;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import om.tnavigator.db.JUnitTestCaseOmQueries;
import om.tnavigator.db.OmQueries;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.xml.XML;

public class XMLAuthorshipConfirmationBuilderTestCases extends AbstractAuthorshipTestCase {

	@Test public void testDisplay() throws Exception {
		Document doc = template();
		assertNotNull(doc);
		String output = XML.saveString(doc);
		assertNotNull(output);
		StandardAuthorshipConfirmationRequestHandling rh = new StandardAuthorshipConfirmationRequestHandling();
		output = rh.stripForDisplay(output);
		assertFalse(output.contains("<div class=\"authority-confirmation\">"));
		assertFalse(output.endsWith("</div>"));
	}

	@Test public void testPickUpTemplate() throws Exception {
		Document doc = template();
		assertNotNull(doc);
	}

	@Test public void testUpdatePostLocation() throws Exception {
		XMLAuthorshipConfirmationBuilder builder = new XMLAuthorshipConfirmationBuilder();
		Document doc = template();
		assertNotNull(doc);
		Node n = doc.getFirstChild();
		assertNotNull(n);
		builder.updatePostLocation(doc, getDummyRequestAssociates());
		Element e = XML.getChild(doc.getFirstChild(), "form");
		assertNotNull(e);
		String s = XML.saveString(doc);
		assertTrue(s.contains("/om-tn/mu120.module5/"));
	}

	@Test public void testInvalidDocument() throws Exception {
		XMLAuthorshipConfirmationBuilder builder = new XMLAuthorshipConfirmationBuilder();
		assertFalse(builder.checkDocumentForRequiredNodes(null));
	}

	@Test public void testValidDocument() throws Exception {
		XMLAuthorshipConfirmationBuilder builder = new XMLAuthorshipConfirmationBuilder();
		Document doc = template();
		assertNotNull(doc);
		Node n = doc.getFirstChild();
		assertNotNull(n);
		assertTrue(Node.ELEMENT_NODE == n.getNodeType());
		assertTrue(builder.checkDocumentForRequiredNodes((Element) n));
	}

	@Test public void testDetermineElementForRenderingWithoutErrorMessage()
		throws Exception {
		XMLAuthorshipConfirmationBuilder builder = new XMLAuthorshipConfirmationBuilder();
		Document originalTemplate = template();
		Document doc = builder.renderForDisplay(originalTemplate, false,
			getDummyRequestAssociates());
		assertNotNull(doc);
		assertTrue(XML.hasChild(doc, XMLAuthorshipConfirmationBuilder.DIV));
		assertTrue(XML.hasChild(doc.getFirstChild(),
			XMLAuthorshipConfirmationBuilder.FORM));
		assertFalse(XML.hasChild(doc.getFirstChild(),
			XMLAuthorshipConfirmationBuilder.ERROR_MESSAGE));
		assertNotSame(doc, originalTemplate);
	}

	@Test public void testDetermineElementForRenderingWITHErrorMessage()
		throws Exception {
		XMLAuthorshipConfirmationBuilder builder = new XMLAuthorshipConfirmationBuilder();
		Document originalTemplate = template();
		Document doc = builder.renderForDisplay(originalTemplate, true,
			getDummyRequestAssociates());
		assertNotNull(doc);
		String s = XML.saveString(doc);
		assertTrue(XML.hasChild(doc, XMLAuthorshipConfirmationBuilder.DIV));
		assertTrue(XML.hasChild(doc.getFirstChild(),
			XMLAuthorshipConfirmationBuilder.FORM));
		assertThat(s, containsString("<div class=\"error-message\""));
		assertNotSame(doc, originalTemplate);
	}

	@Override
	protected OmQueries getOmQueries(String s) throws Exception {
		return new JUnitTestCaseOmQueries(s);
	}

}
