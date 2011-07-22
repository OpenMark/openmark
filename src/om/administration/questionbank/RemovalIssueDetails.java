package om.administration.questionbank;

import java.io.Serializable;

public class RemovalIssueDetails implements Serializable {

	private static final long serialVersionUID = -78873260272770953L;

	private String summary;

	private Exception exception;

	public Exception getException() {
		return exception;
	}

	public void setException(Exception x) {
		exception = x;
	}

	public String getSummary() {
		return summary;
	}

	public RemovalIssueDetails(String msg) {
		summary = msg;
	}

}
