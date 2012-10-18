package om.administration.simpleSQLReporter;

import om.RequestHandlingException;

public class SimpleSQLReporterException extends RequestHandlingException {

	 public SimpleSQLReporterException() {
			super();
		}

		public SimpleSQLReporterException(String s) {
			super(s);
		}

		public SimpleSQLReporterException(Exception x) {
			super(x);
		}

		public SimpleSQLReporterException(String s, Exception x) {
			super(s, x);
		}
	
}
