package om;

import util.misc.UtilityException;

public class OpenmarkException extends UtilityException {

	private static final long serialVersionUID = -3257564817232924668L;

	public OpenmarkException() {
		super();
	}

	public OpenmarkException(String s) {
		super(s);
	}

	public OpenmarkException(Exception x) {
		super(x);
	}

	public OpenmarkException(String s, Exception x) {
		super(s, x);
	}

}
