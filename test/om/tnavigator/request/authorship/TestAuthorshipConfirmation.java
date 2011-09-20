package om.tnavigator.request.authorship;

import om.RenderedOutput;
import om.tnavigator.UserSession;
import om.tnavigator.db.JUnitTestCaseOmQueries;
import om.tnavigator.db.OmQueries;

public class TestAuthorshipConfirmation extends AbstractAuthorshipTestCase {

	public void tearDown() throws Exception {
		dummyTransaction = null;
	}

	@Override
	protected OmQueries getOmQueries(String s) throws Exception {
		return new JUnitTestCaseOmQueries(s);
	}

	public void testFormatQueryRetrieve() throws Exception {
		AuthorshipConfirmationChecking check = createAuthorshipConfirmationChecking(1);
		UserSession us = getUserSession(2325);
		System.out.println(">>> " + us.getDbTi());
		String s = check.formatQuery(us, getOmQueries("[oms-dev].[dbo].[nav_tests]")
			.retrieveAuthorshipConfirmationQuery());
		assertNotNull(s);
		System.out.println(s);
		assertEquals("SELECT authorshipConfirmation from [oms-dev].[dbo].[nav_tests] where ti = '2325'", s);
	}

	public void testFormatQueryUpdate() throws Exception {
		AuthorshipConfirmationChecking check = createAuthorshipConfirmationChecking(1);
		UserSession us = getUserSession(2325);
		System.out.println(">>> " + us.getDbTi());
		String s = check.formatQuery(us, getOmQueries("[oms-dev].[dbo].[nav_tests]")
			.updateAuthorshipConfirmationQuery());
		assertNotNull(s);
		System.out.println(s);
		assertEquals("UPDATE [oms-dev].[dbo].[nav_tests] SET authorshipConfirmation=1 WHERE ti='2325'", s);
	}

	public void testHasSuccessfullyConfirmed() throws Exception {
		RenderedOutput ro = createAuthorshipConfirmationChecking(1)
			.hasSuccessfullyConfirmed(getUserSession(2));
		assertNotNull(ro);
		assertTrue(ro.isSuccessful());
	}

	public void testFailedHasSuccessfullyConfirmed() throws Exception {
		try {
			createAuthorshipConfirmationChecking(1)
				.hasSuccessfullyConfirmed(null);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	public void testMakeConfirmation() throws Exception {
		RenderedOutput ro = createAuthorshipConfirmationChecking(1)
			.makeConfirmation(getUserSession(2));
		assertNotNull(ro);
		assertTrue(ro.isSuccessful());
	}

	public void testFailedMakeConfirmationOnUpdateResponse() throws Exception {
		RenderedOutput ro = createAuthorshipConfirmationChecking(-72)
			.makeConfirmation(getUserSession(2));
		assertNotNull(ro);
		assertFalse(ro.isSuccessful());
	}

	public void testFailedMakeConfirmation() throws Exception {
		try {
			createAuthorshipConfirmationChecking(1)
				.makeConfirmation(null);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
		}
	}

}
