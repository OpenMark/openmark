package om.tnavigator.request.authorship;

import java.util.HashMap;
import java.util.Map;

import om.RenderedOutput;
import om.RequestAssociates;
import om.RequestParameterNames;
import om.RequestResponse;
import om.tnavigator.JUnitTestCaseHttpServletRequest;
import om.tnavigator.JUnitTestCaseUserSession;
import om.tnavigator.TestDeployment;
import om.tnavigator.db.JUnitTestCaseOmQueries;
import om.tnavigator.db.OmQueries;

public class AuthorshipRequestTesting extends AbstractAuthorshipTestCase {

	public void testAuthorshipAction() throws Exception {
		AuthorshipAction aa = AuthorshipAction.confirmingAuthorship;
		assertEquals(AuthorshipAction.confirmingAuthorship, aa);
		assertSame(AuthorshipAction.confirmingAuthorship, aa);
		assertNotSame(AuthorshipAction.hasConfirmed, aa);
		assertTrue(AuthorshipAction.confirmingAuthorship.equals(aa));
		assertFalse(AuthorshipAction.hasConfirmed.equals(aa));
	}

	@Override
	protected OmQueries getOmQueries(String s) throws Exception {
		return new JUnitTestCaseOmQueries(s);
	}

	public void testCreateAuthorshipConfirmationCheckingWithNullRequestAssociates()
		throws Exception {
		checkNulledRequestAssociateComposite("RequestAssociates", null);
	}

	public void testCreateAuthorshipConfirmationCheckingWithNullDatabaseAccess()
		throws Exception {
		checkNulledRequestAssociateComposite(RequestParameterNames.DatabaseAccess.toString(),
			getDummyRequestAssociates());
	}

	public void testCreateAuthorshipConfirmationCheckingWithNullLog()
		throws Exception {
		checkNulledRequestAssociateComposite(RequestParameterNames.Log.toString(),
			getDummyRequestAssociates());
	}

	public void testCreateAuthorshipConfirmationCheckingWithNullAuthorshipQueryBean()
		throws Exception {
		checkNulledRequestAssociateComposite(RequestParameterNames.AuthorshipQueryBean.toString(),
			getDummyRequestAssociates());
	}

	public void testCreateAuthorshipConfirmationCheckingWithNullUserSession()
		throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation = new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		ra.putPrincipleObject(RequestParameterNames.UserSession.toString(), null);
		try {
			confirmation.handleAuthorshipConfirmationChecking(
				new AuthorshipConfirmationHttpServletRequest(), ra,
				confirmation.createAuthorshipConfirmationChecking(ra));
		} catch (Exception x) {
			assertTrue(x.getMessage().contains("Unable to continue as the "
				+ RequestParameterNames.UserSession.toString()));
		}
	}

	public void testCreateAuthorshipConfirmationCheckingWithNoAuthorshipAction() throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation = new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		ra.putPrincipleObject(RequestParameterNames.AuthorshipAction.toString(), null);
		RequestResponse rr = confirmation.handleAuthorshipConfirmationChecking(
			new AuthorshipConfirmationHttpServletRequest(), ra,
			confirmation.createAuthorshipConfirmationChecking(ra));
		assertNotNull(rr);
		System.out.println(">> " + rr);
		assertFalse(rr.isSuccessful());
		assertNotNull(rr.toString());
		assertTrue(rr.toString().contains("Please respond to the question below then move to the next page and submit your answer."));
	}

	public void testCreateAuthorshipConfirmationCheckingConfirmed() throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation
			= new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		ra.putPrincipleObject(RequestParameterNames.DatabaseAccess.toString(),
				getAuthorshipTestCaseDatabaseAccess(1));
		RequestResponse rr = confirmation.handleAuthorshipConfirmationChecking(
			new AuthorshipConfirmationHttpServletRequest(), ra,
			confirmation.createAuthorshipConfirmationChecking(ra));
		assertNotNull(rr);
		assertTrue(rr.isSuccessful());
	}

	public void testCreateAuthorshipConfirmationCheckingWithAuthorshipActionConfirmed()
		throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation
			= new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		ra.putPrincipleObject(RequestParameterNames.DatabaseAccess.toString(),
			getAuthorshipTestCaseDatabaseAccess(1));
		ra.putPrincipleObject(RequestParameterNames.AuthorshipAction.toString(),
			AuthorshipAction.hasConfirmed);
		RequestResponse rr = confirmation.handleAuthorshipConfirmationChecking(
			new AuthorshipConfirmationHttpServletRequest(), ra,
			confirmation.createAuthorshipConfirmationChecking(ra));
		assertNotNull(rr);
		assertTrue(rr.isSuccessful());
	}

	public void testCreateAuthorshipConfirmationCheckingWithAuthorshipAction()
		throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation
			= new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		ra.putPrincipleObject(RequestParameterNames.DatabaseAccess.toString(),
			getAuthorshipTestCaseDatabaseAccess(1));
		ra.putPrincipleObject(RequestParameterNames.AuthorshipAction.toString(),
			AuthorshipAction.confirmingAuthorship);
		RequestResponse rr = confirmation.handleAuthorshipConfirmationChecking(
			new AuthorshipConfirmationHttpServletRequest(), ra,
			confirmation.createAuthorshipConfirmationChecking(ra));
		assertNotNull(rr);
		System.out.println(">>> " + rr);
		assertTrue(rr.isSuccessful());
	}

	public void testCreateAuthorshipConfirmationCheckingWithAuthorshipActionMakeConfirmation() throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation
			= new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		ra.putPrincipleObject(RequestParameterNames.DatabaseAccess.toString(),
			getAuthorshipTestCaseDatabaseAccess(1));
		RequestResponse rr = confirmation.handleAuthorshipConfirmationChecking(
			new AuthorshipConfirmationHttpServletRequest(), ra,
			confirmation.createAuthorshipConfirmationChecking(ra));
		assertNotNull(rr);
		assertTrue(rr.isSuccessful());
	}

	public void testUserNeedsToConfirmAuthorship() throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation
			= new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		ra.putPrincipleObject(RequestParameterNames.AuthorshipAction.toString(), null);
		RequestResponse rr = confirmation.handleAuthorshipConfirmationChecking(
			new AuthorshipConfirmationHttpServletRequest(), ra,
			confirmation.createAuthorshipConfirmationChecking(ra));
		assertNotNull(rr);
		assertFalse(rr.isSuccessful());
		assertTrue(rr instanceof RenderedOutput);
		String output = ((RenderedOutput) rr).toString();
		assertNotNull(output);
		assertTrue(output.length() > 0);
	}

	public void testConfirmedAuthorship() throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation
			= new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		ra.putPrincipleObject(RequestParameterNames.DatabaseAccess.toString(),
			getAuthorshipTestCaseDatabaseAccess(1));
		RequestResponse rr = confirmation.handleAuthorshipConfirmationChecking(
			new AuthorshipConfirmationHttpServletRequest(), ra,
			confirmation.createAuthorshipConfirmationChecking(ra));
		assertNotNull(rr);
		assertTrue(rr.isSuccessful());
		assertTrue(rr instanceof RenderedOutput);
		String output = ((RenderedOutput) rr).toString();
		assertNotNull(output);
		assertTrue(output.length() == 0);
	}

	public void testAddUsersResponseToRequestAssociates() throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation
			= new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		AuthorshipConfirmationHttpServletRequest dummyRequest = new AuthorshipConfirmationHttpServletRequest();
		dummyRequest.addDummyParameter(RequestParameterNames.UserAuthorshipConfirmationResponse.toString(), "tester");
		confirmation.addUsersResponseToRequestAssociates(dummyRequest, ra);
		Object o = ra.getPrincipleObjects().get(RequestParameterNames.UserAuthorshipConfirmationResponse.toString());
		assertNotNull(o);
		assertTrue(o instanceof String);
		assertEquals(o, "tester");
	}
	
	public void testAddUsersResponseToRequestAssociatesNulled() throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation
			= new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		AuthorshipConfirmationHttpServletRequest dummyRequest = new AuthorshipConfirmationHttpServletRequest();
		dummyRequest.addDummyParameter(RequestParameterNames.UserAuthorshipConfirmationResponse.toString(), null);
		confirmation.addUsersResponseToRequestAssociates(dummyRequest, ra);
		Object o = ra.getPrincipleObjects().get(RequestParameterNames.UserAuthorshipConfirmationResponse.toString());
		assertNull(o);
	}

	public void testAddUsersResponseToRequestAssociatesForCorrectAction() throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation
			= new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		ra.putPrincipleObject(RequestParameterNames.AuthorshipAction.toString(), null);
		AuthorshipConfirmationHttpServletRequest dummyRequest = new AuthorshipConfirmationHttpServletRequest();
		dummyRequest.addDummyParameter(RequestParameterNames.UserAuthorshipConfirmationResponse.toString(),
			RequestParameterNames.confirmed.toString());
		confirmation.addUsersResponseToRequestAssociates(dummyRequest, ra);
		Object o = ra.getPrincipleObjects().get(RequestParameterNames.AuthorshipAction.toString());
		assertNotNull(o);
		assertTrue(o instanceof AuthorshipAction);
		assertEquals(o, AuthorshipAction.confirmingAuthorship);
	}

	public void testAddUsersResponseToRequestAssociatesForNullAction() throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation
			= new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		ra.putPrincipleObject(RequestParameterNames.AuthorshipAction.toString(), null);
		AuthorshipConfirmationHttpServletRequest dummyRequest = new AuthorshipConfirmationHttpServletRequest();
		dummyRequest.addDummyParameter(RequestParameterNames.UserAuthorshipConfirmationResponse.toString(),
			"anything else is not valid");
		confirmation.addUsersResponseToRequestAssociates(dummyRequest, ra);
		Object o = ra.getPrincipleObjects().get(RequestParameterNames.AuthorshipAction.toString());
		assertNull(o);
	}

	public void testShouldRun() throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation
			= new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		JUnitTestCaseUserSession sess = getUserSession(2);
		TestDeployment td = sess.getTestDeployment();
		assertNotNull(td);
		td.setType(TestDeployment.TYPE_ASSESSED_REQUIRED);
		ra.putPrincipleObject(RequestParameterNames.UserSession.toString(), sess);
		assertTrue(confirmation.shouldRun(ra));
	}

	public void testShouldRunNOT() throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation
			= new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		ra.putPrincipleObject(RequestParameterNames.UserSession.toString(), null);
		assertFalse(confirmation.shouldRun(ra));
	}

	public void testErrorPageResponse() throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation
			= new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		ra.putPrincipleObject(RequestParameterNames.DatabaseAccess.toString(),
			getAuthorshipTestCaseDatabaseAccess(1));
		ra.putPrincipleObject(RequestParameterNames.AuthorshipAction.toString(), null);
		ra.putPrincipleObject(RequestParameterNames.UserAuthorshipConfirmationResponse.toString(),
			RequestParameterNames.Cancelled.toString());
		RequestResponse rr = confirmation.handleAuthorshipConfirmationChecking(
			new AuthorshipConfirmationHttpServletRequest(), ra,
			confirmation.createAuthorshipConfirmationChecking(ra));
		assertNotNull(rr);
		assertFalse(rr.isSuccessful());
		assertTrue(rr.toString().contains("This iCMA will remain unavailable to you."));
		System.out.println(rr);
	}

	public void testCheckForOnLoad() throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation
			= new StandardAuthorshipConfirmationRequestHandling();
		RequestAssociates ra = getDummyRequestAssociates();
		ra.putPrincipleObject(RequestParameterNames.DatabaseAccess.toString(),
			getAuthorshipTestCaseDatabaseAccess(1));
		ra.putPrincipleObject(RequestParameterNames.UserAuthorshipConfirmationResponse.toString(),
			"blah blah");
		ra.putPrincipleObject(RequestParameterNames.AuthorshipAction.toString(),null);
		RequestResponse rr = confirmation.handleAuthorshipConfirmationChecking(
			new AuthorshipConfirmationHttpServletRequest(), ra,
			confirmation.createAuthorshipConfirmationChecking(ra));
		assertNotNull(rr);
		assertTrue(rr.toString().contains("onLoad=\"establish();"));
	}

	private void checkNulledRequestAssociateComposite(String nulled,
		RequestAssociates ra) throws Exception {
		StandardAuthorshipConfirmationRequestHandling confirmation
			= new StandardAuthorshipConfirmationRequestHandling();
		if (null != ra) {
			ra.putPrincipleObject(nulled, null);
		}
		try {
			confirmation.createAuthorshipConfirmationChecking(ra);
			assertTrue(false);
		} catch (Exception x) {
			assertTrue(x.getMessage().contains("Unable to continue as the "
				+ nulled));
		}
	}

	private class AuthorshipConfirmationHttpServletRequest extends JUnitTestCaseHttpServletRequest {
		
		private Map<String, String> parameters = new HashMap<String, String>();
		
		void addDummyParameter(String key, String value) {
			parameters.put(key, value);
		}
		
		public String getParameter(String arg0) {
			return parameters.get(arg0);
		}
		
		@Override
		public String getHeader(String arg0) {
			return "MSIE";
		}
		
	}

}
