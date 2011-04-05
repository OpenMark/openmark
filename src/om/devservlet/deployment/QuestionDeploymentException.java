package om.devservlet.deployment;

import java.io.IOException;

public class QuestionDeploymentException extends IOException {

	private static final long serialVersionUID = 9034644821693312348L;

	public QuestionDeploymentException() {
		super();
	}

	public QuestionDeploymentException(String s) {
		super(s);
	}

	public QuestionDeploymentException(Exception x) {
		super(x);
	}

	public QuestionDeploymentException(String s, Exception x) {
		super(s, x);
	}
}
