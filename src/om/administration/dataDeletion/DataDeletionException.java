package om.administration.dataDeletion;

import om.abstractservlet.RequestHandlingException;

public class DataDeletionException extends RequestHandlingException {

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
