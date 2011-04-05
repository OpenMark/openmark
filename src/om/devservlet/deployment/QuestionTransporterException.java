package om.devservlet.deployment;

/**
 * If anything goes wrong while transporting the Question to its destination.
 */

public class QuestionTransporterException
	extends QuestionDeploymentException {

	private static final long serialVersionUID = 8853083964934626308L;

	public QuestionTransporterException() {
		super();
	}

	public QuestionTransporterException(String s) {
		super(s);
	}

	public QuestionTransporterException(Exception x) {
		super(x);
	}

	public QuestionTransporterException(String s, Exception x) {
		super(s, x);
	}
}
