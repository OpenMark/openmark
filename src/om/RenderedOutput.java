package om;

import java.io.Serializable;


/**
 * A simplified POJO that holds details to report to the user as to the progress
 *  of the deployment of the Questions to the configured locations.
 * @author Trevor Hinson
 */

public class RenderedOutput implements Serializable, RequestResponse {

	private static final long serialVersionUID = -8788725158743881209L;

	// by default we remain positive ... This should be mutated otherwise !
	private boolean successful = true;

	private StringBuffer renderedOutput;

	private Object response;

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object obj) {
		response = obj;
	}

	public void setSuccessful(boolean b) {
		successful = b;
	}

	private StringBuffer getOutput() {
		if (null == renderedOutput) {
			renderedOutput = new StringBuffer();
		}
		return renderedOutput;
	}

	public StringBuffer append(String s) {
		return null != s ? getOutput().append(s) : null;
	}

	public String toString() {
		return getOutput().toString();
	}

	@Override
	public boolean isSuccessful() {
		return successful;
	}

}
