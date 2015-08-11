package om.administration.dataDeletion;

import util.misc.UtilityException;

public class DataDeletionException extends UtilityException {
	private static final long serialVersionUID = 4782354216166419540L;

	public DataDeletionException() {
			super();
		}

		public DataDeletionException(String s) {
			super(s);
		}

		public DataDeletionException(Exception x) {
			super(x);
		}

		public DataDeletionException(String s, Exception x) {
			super(s, x);
		}
	
}
