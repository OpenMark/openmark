package om.abstractservlet;

import util.misc.FinalizedResponse;

public class StandardFinalizedResponse implements FinalizedResponse {

	private boolean success;

	public StandardFinalizedResponse(boolean b) {
		success = b;
	}

	@Override
	public boolean isSuccess() {
		return success;
	}

}
