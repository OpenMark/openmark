package om.tnavigator.request.authorship;

import om.abstractservlet.RenderedOutput;
import om.tnavigator.sessions.UserSession;

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
