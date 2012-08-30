package om.tnavigator.request.authorship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import om.RenderedOutput;
import om.tnavigator.UserSession;
import om.tnavigator.db.JUnitTestCaseOmQueries;
import om.tnavigator.db.OmQueries;

import org.junit.After;
import org.junit.Test;

public class TestAuthorshipConfirmation extends AbstractAuthorshipTestCase {

	@After
	public void tearDown() throws Exception {
		dummyTransaction = null;
	}

	@Override
	protected OmQueries getOmQueries(String s) throws Exception {
		return new JUnitTestCaseOmQueries(s);
	}

	@Test public void testFormatQueryRetrieve() throws Exception {
		AuthorshipConfirmationChecking check = createAuthorshipConfirmationChecking(1);
		UserSession us = getUserSession(2325);
		String s = check.formatQuery(us, getOmQueries("nav_")
			.retrieveAuthorshipConfirmationQuery());
		assertNotNull(s);
		assertEquals("SELECT authorshipConfirmation from nav_tests WHERE ti = '2325'", s);
	}

	@Test public void testFormatQueryUpdate() throws Exception {
		AuthorshipConfirmationChecking check = createAuthorshipConfirmationChecking(1);
		UserSession us = getUserSession(2325);
		String s = check.formatQuery(us, getOmQueries("nav_")
			.updateAuthorshipConfirmationQuery());
		assertNotNull(s);
		assertEquals("UPDATE nav_tests SET authorshipConfirmation=1 WHERE ti='2325'", s);
	}

	@Test public void testHasSuccessfullyConfirmed() throws Exception {
		RenderedOutput ro = createAuthorshipConfirmationChecking(1)
			.hasSuccessfullyConfirmed(getUserSession(2));
		assertNotNull(ro);
		assertTrue(ro.isSuccessful());
	}

	@Test public void testFailedHasSuccessfullyConfirmed() throws Exception {
		try {
			createAuthorshipConfirmationChecking(1)
				.hasSuccessfullyConfirmed(null);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	@Test public void testMakeConfirmation() throws Exception {
		RenderedOutput ro = createAuthorshipConfirmationChecking(1)
			.makeConfirmation(getUserSession(2));
		assertNotNull(ro);
		assertTrue(ro.isSuccessful());
	}

	@Test public void testFailedMakeConfirmationOnUpdateResponse() throws Exception {
		RenderedOutput ro = createAuthorshipConfirmationChecking(-72)
			.makeConfirmation(getUserSession(2));
		assertNotNull(ro);
		assertFalse(ro.isSuccessful());
	}

	@Test public void testFailedMakeConfirmation() throws Exception {
		try {
			createAuthorshipConfirmationChecking(1)
				.makeConfirmation(null);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
		}
	}

}
