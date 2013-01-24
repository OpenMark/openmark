package om.qengine.dynamics.util;

public class DynamicCompilationReport {
	
	private String source;
	
	private String message;
	
	private String code;
	
	private long lineNumber;

	public String getSource() {
		return source;
	}

	public void setSource(String s) {
		source = s;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String s) {
		message = s;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String s) {
		code = s;
	}

	public long getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(long l) {
		lineNumber = l;
	}

}
