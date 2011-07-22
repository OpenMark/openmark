package om.tnavigator.db;

import om.tnavigator.db.DatabaseAccess.ConnectionInfo;

public class JUnitTestCaseTransaction extends DatabaseAccess.Transaction {

	protected JUnitTestCaseTransaction(JUnitTestCaseDatabaseAccess databaseAccess,
		ConnectionInfo ci) {
		databaseAccess.super(ci);
	}

	@Override
	public long finish() {
		return System.currentTimeMillis();
	}

}
