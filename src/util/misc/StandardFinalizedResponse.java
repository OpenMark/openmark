package util.misc;

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
