package om.tnavigator.db;

import java.sql.SQLException;

import om.tnavigator.db.DatabaseAccess.Transaction;

public class JUnitTestCaseOmQueries extends OmQueries {

	protected JUnitTestCaseResultSet dummyResults;

	public JUnitTestCaseOmQueries(JUnitTestCaseResultSet res) {
		super(null);
		dummyResults = res;
	}

	public JUnitTestCaseOmQueries() {
		super(null);
	}

	protected JUnitTestCaseOmQueries(String prefix) {
		super(prefix);
	}

	@Override
	public void checkDatabaseConnection(Transaction dat) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	protected String extractMonthFromTimestamp(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String extractYearFromTimestamp(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getInsertedSequenceID(Transaction dat, String table,
			String column) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getURL(String server, String database, String username,
			String password) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

}
