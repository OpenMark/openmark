package om.devservlet.deployment;

import java.io.Serializable;

import om.devservlet.RequestResponse;

/**
 * A simplified POJO that holds details to report to the user as to the progress
 *  of the deployment of the Questions to the configured locations.
 * @author Trevor Hinson
 */

public class RenderedOutput implements Serializable, RequestResponse {

	private static final long serialVersionUID = -8788725158743881209L;

	private StringBuffer renderedOutput;

	private StringBuffer getOutput() {
		if (null == renderedOutput) {
			renderedOutput = new StringBuffer();
		}
		return renderedOutput;
	}

	public StringBuffer append(String s) {
		return getOutput().append(s);
	}

	public String toString() {
		return getOutput().toString();
	}

}
