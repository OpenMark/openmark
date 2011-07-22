package om.administration.questionbank;

public class RemovalIssueDetails extends UndoIssue {

	public RemovalIssueDetails(String msg) {
		super(msg);
	}

	public RemovalIssueDetails(String msg, Exception x) {
		super(msg, x);
	}
}
