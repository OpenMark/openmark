package om.tnavigator.request.authorship;

import om.RequestAssociates;
import om.RequestParameterNames;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.xml.XML;
import util.xml.XMLException;

public class XMLAuthorshipConfirmationBuilder
	implements AuthorshipRenderedResponseBuilder {

	protected static String DIV = "div";

	protected static String FORM = "form";

	protected static String ERROR_MESSAGE = "error-message";

	protected static String CLASS = "class";

	protected static String ID = "id";

	protected static String BRBR = "<br /><br />";

	@Override
	public Document renderForDisplay(Document doc, boolean applyErrorMessage,
		RequestAssociates ra) throws AuthorshipConfirmationException {
		Document response = null;
		if (null != doc) {
			response = XML.clone(doc);
			updatePostLocation(response, ra);
			Element error_message = getElement(response.getFirstChild(),
				ERROR_MESSAGE);
			String error_text = error_message.getTextContent();
			if (applyErrorMessage) {
				Element div = getElement(response, DIV);
				Element err = response.createElement(DIV);
				err.setAttribute(CLASS, ERROR_MESSAGE);
				err.setAttribute(ID, ERROR_MESSAGE);
				err.setTextContent(error_text);
				div.appendChild(err);
				div.replaceChild(err, error_message);
			} else {
				response.getFirstChild().removeChild(error_message);
			}
		}
		return response;
	}

	/**
	 * Updates the Location which to post the users response to.  This updates
	 *  the Document itself that is rendered back to the user.
	 * @param response
	 * @param ra
	 * @throws AuthorshipConfirmationException
	 * @author Trevor Hinson
	 */
	protected void updatePostLocation(Document response, RequestAssociates ra)
		throws AuthorshipConfirmationException {
		try {
			Object obj = ra.getPrincipleObjects().get(
				RequestParameterNames.PostLocation.toString());
			if (null != obj ? obj instanceof String : false) {
				String location = (String) obj;
				Element e = XML.getChild(response.getFirstChild(), "form");
				e.setAttribute("action", location);
			} else {
				throw new AuthorshipConfirmationException("The "
					+ RequestParameterNames.PostLocation.toString()
					+ " was not valid : " + obj);
			}
		} catch (XMLException x) {
			throw new AuthorshipConfirmationException(x);
		}
	}

	/**
	 * Picks up a given Element from the specified Node argument.
	 * @param parent
	 * @param name
	 * @return
	 * @throws AuthorshipConfirmationException
	 * @author Trevor Hinson
	 */
	private Element getElement(Node parent, String name)
		throws AuthorshipConfirmationException {
		Element ele = null;
		try {
			ele = XML.getChild(parent, name);
		} catch (XMLException x) {
			throw new AuthorshipConfirmationException(x);
		}
		return ele;
	}

	/**
	 * Tests that the Element has the required nodes in order to continue.
	 * @param e
	 * @return
	 * @author Trevor Hinson
	 */
	protected boolean checkDocumentForRequiredNodes(Element e) {
		boolean fine = false;
		if (null != e) {
			if (XML.hasChild(e, FORM) && XML.hasChild(e, ERROR_MESSAGE)) {
				fine = true;
			}
		}
		return fine;
	}

}
