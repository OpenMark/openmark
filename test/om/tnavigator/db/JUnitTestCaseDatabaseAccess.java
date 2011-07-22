package om.tnavigator.db;

import java.sql.SQLException;

import om.Log;

public class JUnitTestCaseDatabaseAccess extends DatabaseAccess {

	public JUnitTestCaseDatabaseAccess() {
		super(null, null);
	}
	
	public JUnitTestCaseDatabaseAccess(String sURL, Log l) {
		super(sURL, l);
	}
	
	public Transaction newTransaction() throws SQLException {
		return new JUnitTestCaseTransaction(this, null);
	}

}
