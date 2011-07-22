package om;

public class RequestHandlingException extends Exception {

	private static final long serialVersionUID = 6328771350272695999L;

	public RequestHandlingException() {
		super();
	}

	public RequestHandlingException(String s) {
		super(s);
	}

	public RequestHandlingException(Exception x) {
		super(x);
	}

	public RequestHandlingException(String s, Exception x) {
		super(s, x);
	}
}
