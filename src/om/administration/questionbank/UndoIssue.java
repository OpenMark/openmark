package om.administration.questionbank;

public class UndoIssue extends ClearanceIssue {

	public UndoIssue(String msg) {
		super(msg);
	}

	public UndoIssue(String msg, Exception x) {
		super(msg, x);
	}
}
