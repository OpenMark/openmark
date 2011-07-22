package om.administration.questionbank;

public class ClearanceIssue {

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

	public ClearanceIssue(String msg) {
		summary = msg;
	}

	public ClearanceIssue(String msg, Exception x) {
		summary = msg;
		exception = x;
	}

}
