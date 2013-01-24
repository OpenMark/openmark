package om.tnavigator.request.authorship;

import om.abstractservlet.RequestAssociates;

import org.w3c.dom.Document;

public interface AuthorshipRenderedResponseBuilder {

	/**
	 * Used to create the necessary output for the environment for the
	 *  Authorship Confirmation question required at the start of Assessed tests
	 * @param template
	 * @param applyErrorMessage
	 * @param ra
	 * @return
	 * @throws AuthorshipConfirmationException
	 */
	Document renderForDisplay(Document template, boolean applyErrorMessage,
		RequestAssociates ra) throws AuthorshipConfirmationException;
	
}
