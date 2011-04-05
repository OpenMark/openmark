package util.misc;

public class UtilityException extends Exception {

	private static final long serialVersionUID = -6292644727069872877L;

	public UtilityException() {
		super();
	}

	public UtilityException(String s) {
		super(s);
	}

	public UtilityException(Exception x) {
		super(x);
	}

	public UtilityException(String s, Exception x) {
		super(s, x);
	}
}
