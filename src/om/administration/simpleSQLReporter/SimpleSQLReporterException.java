package om.administration.simpleSQLReporter;

import om.abstractservlet.RequestHandlingException;

public class SimpleSQLReporterException extends RequestHandlingException {
	private static final long serialVersionUID = -7644496394483117921L;

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
