package om.tnavigator.request.authorship;

import om.tnavigator.db.OmQueries;

public class AuthorshipQueryBean {

	private OmQueries omQueries;

	public OmQueries getOmQueries() throws AuthorshipConfirmationException {
		if (null == omQueries) {
			throw new AuthorshipConfirmationException("Unable to continue as"
				+ " the composite OmQueries implementation is null.");
		}
		return omQueries;
	}

	public AuthorshipQueryBean(OmQueries q) {
		omQueries = q;
	}

}
