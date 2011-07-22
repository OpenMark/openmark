package util.misc;

import om.OmException;

public class StopException extends OmException {

	private static final long serialVersionUID = 799889494142408696L;

	public StopException() {
		super("Stopped");
	}

}
