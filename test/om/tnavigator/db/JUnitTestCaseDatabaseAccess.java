package om.tnavigator.db;

import java.sql.SQLException;

import om.Log;
import om.OmException;

public class JUnitTestCaseDatabaseAccess extends DatabaseAccess {

	public JUnitTestCaseDatabaseAccess() throws OmException {
		super(null);
	}
	
	public JUnitTestCaseDatabaseAccess(Log l) throws OmException {
		super(l);
	}
	
	public Transaction newTransaction() throws SQLException {
		return new JUnitTestCaseTransaction(this, null);
	}

}
