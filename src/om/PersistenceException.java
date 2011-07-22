package om;

public class PersistenceException extends OpenmarkException {

	private static final long serialVersionUID = -6377838743819183229L;

	public PersistenceException() {
		super();
	}

	public PersistenceException(String s) {
		super(s);
	}

	public PersistenceException(Exception x) {
		super(x);
	}

	public PersistenceException(String s, Exception x) {
		super(s, x);
	}
}
