/**
 * 
 */
package om;

/**
 * Exceptions from the qestion that are transient in nature, 
 */
public class OmTransientQuestionException extends OmUnexpectedException {

	/** Required by the Serializable interface. */
	private static final long serialVersionUID = -1481815351361289477L;

	/**
	 * @param cause Cause
	 */
	public OmTransientQuestionException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param text Description of error (do not end in full stop)
	 */
	public OmTransientQuestionException(String text) {
		super(text);
	}

	/**
	 * @param text Description of error (do not end in full stop)
	 * @param cause Cause
	 */
	public OmTransientQuestionException(String text, Throwable cause) {
		super(text, cause);
	}
}
