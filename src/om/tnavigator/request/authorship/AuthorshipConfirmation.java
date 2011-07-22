package om.tnavigator.request.authorship;

import om.RenderedOutput;
import om.tnavigator.UserSession;

public interface AuthorshipConfirmation {

	/**
	 * 
	 * @param us
	 * @return
	 * @throws AuthorshipConfirmationException
	 * @author Trevor Hinson
	 */
	RenderedOutput makeConfirmation(UserSession us)
		throws AuthorshipConfirmationException;

	/**
	 * .
	 * @param us
	 * @return
	 * @throws AuthorshipConfirmationException
	 * @author Trevor Hinson
	 */
	RenderedOutput hasSuccessfullyConfirmed(UserSession us)
		throws AuthorshipConfirmationException;

}
