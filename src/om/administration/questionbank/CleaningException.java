package om.administration.questionbank;

import util.misc.UtilityException;

public class CleaningException extends UtilityException {

	private static final long serialVersionUID = -5707977070536591988L;

	public CleaningException() {
		super();
	}

	public CleaningException(String s) {
		super(s);
	}

	public CleaningException(Exception x) {
		super(x);
	}

	public CleaningException(String s, Exception x) {
		super(s, x);
	}
}
