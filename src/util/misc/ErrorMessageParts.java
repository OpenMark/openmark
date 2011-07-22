package util.misc;

public class ErrorMessageParts {

	private String title;
	
	private String message;
	
	private boolean resetTestPosition;
	
	private Throwable throwable;

	private String errorTemplateReference;

	public String getErrorTemplateReference() {
		return errorTemplateReference;
	}

	public void setErrorTemplateReference(String s) {
		errorTemplateReference = s;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String s) {
		title = s;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String s) {
		message = s;
	}

	public boolean isResetTestPosition() {
		return resetTestPosition;
	}

	public void setResetTestPosition(boolean b) {
		resetTestPosition = b;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable t) {
		throwable = t;
	}

	public ErrorMessageParts(String sTitle, String sMessage,
		boolean bResetTestPosition, Throwable tException, String templateName) {
		title = sTitle;
		message = sMessage;
		resetTestPosition = bResetTestPosition;
		throwable = tException;
		errorTemplateReference = templateName;
	}

}
