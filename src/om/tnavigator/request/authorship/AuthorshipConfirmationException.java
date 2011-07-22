package om.tnavigator.request.authorship;

import util.misc.UtilityException;

public class AuthorshipConfirmationException extends UtilityException {

	private static final long serialVersionUID = 7954420330058001342L;

	public AuthorshipConfirmationException() {
		super();
	}

	public AuthorshipConfirmationException(String s) {
		super(s);
	}

	public AuthorshipConfirmationException(Exception x) {
		super(x);
	}

	public AuthorshipConfirmationException(String s, Exception x) {
		super(s, x);
	}

}
