package om.administration.extraction;

import util.misc.UtilityException;

public class ExtractorException extends UtilityException {

	private static final long serialVersionUID = 4942824297939017821L;

	public ExtractorException() {
		super();
	}

	public ExtractorException(String s) {
		super(s);
	}

	public ExtractorException(Exception x) {
		super(x);
	}

	public ExtractorException(String s, Exception x) {
		super(s, x);
	}
}
