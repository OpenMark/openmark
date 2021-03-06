package om.tnavigator.request.authorship;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.ServletContext;

import om.Log;
import om.OmException;
import om.abstractservlet.RequestAssociates;
import om.abstractservlet.RequestParameterNames;
import om.tnavigator.AbstractNavigatorTestCase;
import om.tnavigator.JUnitTestCaseUserSession;
import om.tnavigator.NavigatorServlet;
import om.tnavigator.TestCaseServletContext;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.DatabaseAccess.ConnectionInfo;
import om.tnavigator.db.JUnitTestCaseDatabaseAccess;
import om.tnavigator.db.JUnitTestCaseResultSet;
import om.tnavigator.db.JUnitTestCaseTransaction;
import om.tnavigator.db.OmQueries;
import om.tnavigator.teststructure.JUnitTestCaseTestDefinition;
import om.tnavigator.teststructure.JUnitTestCaseTestRealisation;
import om.tnavigator.teststructure.TestDeployment;

import org.w3c.dom.Document;

public abstract class AbstractAuthorshipTestCase extends AbstractNavigatorTestCase {

	protected TestAuthorshipConfirmationTransaction dummyTransaction;

	private static String AUTHORSHIP_CONFIRMATION_TEMPLATE_NAME = "testcase-authorship-confirmation.xml";

	private static String PARENT_TEMPLATE = "template.xhtml";

	protected RequestAssociates getDummyRequestAssociates() throws Exception {
		RequestAssociates ra = new RequestAssociates(newTestCaseNavigationServlet()
			.getServletContext(), "/", false, new HashMap<String, Object>());
		ra.putPrincipleObject(RequestParameterNames.DatabaseAccess.toString(),
			new JUnitTestCaseDatabaseAccess());
		ra.putPrincipleObject(RequestParameterNames.Log.toString(), log);
		ra.putPrincipleObject(RequestParameterNames.OmQueries.toString(), getOmQueries(null));
		ra.putPrincipleObject(RequestParameterNames.AuthorshipQueryBean.toString(),
			getAuthorshipQueryBean());
		ra.putPrincipleObject(RequestParameterNames.UserSession.toString(), getUserSession(1));
		ra.putPrincipleObject(RequestParameterNames.AuthorshipAction.toString(),
			AuthorshipAction.confirmingAuthorship);
		ra.getPrincipleObjects().put(RequestParameterNames.AccessCSSAppend.toString(),"tester");
		ra.putPrincipleObject(RequestParameterNames.AuthorshipXMLDocument.toString(), template());
		ra.putPrincipleObject(RequestParameterNames.PostLocation.toString(), "/om-tn/mu120.module5/");
		ra.putPrincipleObject(RequestParameterNames.AccessibilityCookie.toString(), "falseCookie");
		ra.putPrincipleObject(RequestParameterNames.ParentTemplate.toString(), getParentTemplate());
		return ra;
	}

	protected abstract OmQueries getOmQueries(String s) throws Exception;

	protected Document getParentTemplate() throws Exception {
		return getPickUpDocument(PARENT_TEMPLATE, AbstractNavigatorTestCase.class);
	}

	protected Document template() throws Exception {
		return getPickUpDocument(AUTHORSHIP_CONFIRMATION_TEMPLATE_NAME, AbstractNavigatorTestCase.class);
	}

	protected TestCaseNavigationServlet newTestCaseNavigationServlet()
		throws Exception {
		return new TestCaseNavigationServlet(log);
	}

	protected JUnitTestCaseUserSession getDummyUserSession() throws Exception {
		return new JUnitTestCaseUserSession("123");
	}

	protected AuthorshipQueryBean getAuthorshipQueryBean() throws Exception {
		return new AuthorshipQueryBean(getOmQueries(null));
	}

	protected JUnitTestCaseUserSession getUserSession(int testID) throws Exception {
		JUnitTestCaseTestDefinition td = pickUpTestDefinition(BASIC_TEST_DEFINITION);
		TestDeployment deploy = pickUpTestDeployment(BASIC_TEST_DEPLOYMENT);
		JUnitTestCaseTestRealisation rel = JUnitTestCaseTestRealisation
			.getTestRealisationInstance(new Integer(testID), td, dummyScore());
		JUnitTestCaseUserSession sess = new JUnitTestCaseUserSession("123", td, deploy, rel);
		sess.setTestCaseDbTi(testID);
		return sess;
	}

	protected AuthorshipConfirmationChecking createAuthorshipConfirmationChecking(
		int updateResponse, TestAuthorshipConfirmationResultSet rs) throws Exception {
		TestCaseAuthorshipConfirmationDatabaseAccess databaseAccess = new TestCaseAuthorshipConfirmationDatabaseAccess();
		dummyTransaction = new TestAuthorshipConfirmationTransaction(databaseAccess, null);
		dummyTransaction.dummyQueryResultSet = rs;
		dummyTransaction.dummyUpdateResponse = updateResponse;
		return new AuthorshipConfirmationChecking(databaseAccess, log,
			getAuthorshipQueryBean());
	}

	protected AuthorshipConfirmationChecking createAuthorshipConfirmationChecking(
		int updateResponse) throws Exception {
		return new AuthorshipConfirmationChecking(getAuthorshipTestCaseDatabaseAccess(
			updateResponse), log, getAuthorshipQueryBean());
	}

	protected DatabaseAccess getAuthorshipTestCaseDatabaseAccess(int updateResponse) throws OmException {
		TestCaseAuthorshipConfirmationDatabaseAccess databaseAccess = new TestCaseAuthorshipConfirmationDatabaseAccess();
		dummyTransaction = new TestAuthorshipConfirmationTransaction(databaseAccess, null);
		dummyTransaction.dummyQueryResultSet = new TestAuthorshipConfirmationResultSet(1, 1);
		dummyTransaction.dummyUpdateResponse = updateResponse;
		return databaseAccess;
	}

	protected class TestCaseNavigationServlet extends NavigatorServlet {

		private static final long serialVersionUID = 2648307557006921126L;

		public TestCaseNavigationServlet(Log log) {
			l = log;
		}
		
		public ServletContext getServletContext() {
			return new TestCaseServletContext();
		}

	}

	protected class TestCaseAuthorshipConfirmationDatabaseAccess extends JUnitTestCaseDatabaseAccess {
		public TestCaseAuthorshipConfirmationDatabaseAccess() throws OmException
		{
			super();
		}

		public Transaction newTransaction() throws SQLException {
			return dummyTransaction;
		}
		
	}

	protected class TestAuthorshipConfirmationTransaction extends JUnitTestCaseTransaction {
		
		JUnitTestCaseResultSet dummyQueryResultSet;
		
		int dummyUpdateResponse;
		
		protected TestAuthorshipConfirmationTransaction(
			JUnitTestCaseDatabaseAccess databaseAccess, ConnectionInfo ci) {
			super(databaseAccess, ci);
		}
		
		public ResultSet query(String sSQL) throws SQLException {
			return dummyQueryResultSet;
		}
		
		public int update(String sSQL) throws SQLException {
			return dummyUpdateResponse;
		}
	}

	protected class TestAuthorshipConfirmationResultSet extends JUnitTestCaseResultSet {
		
		private int amountOfDummyData = 1;
		
		private int returnValue;

		private int counter = 0;

		public TestAuthorshipConfirmationResultSet(int n, int amount) {
			returnValue = n;
			amountOfDummyData = amount;
		}
		
		public int getInt(String columnLabel) throws SQLException {
			int n = -1;
			if (AuthorshipConfirmationChecking
				.AUTHORSHIP_CONFIRMATION.equals(columnLabel)) {
				n = returnValue;
			}
			return n;
		}
		
		@Override
		public boolean next() throws SQLException {
			boolean continu = false;
			if (counter < amountOfDummyData) {
				continu = true;
				counter++;
			}
			return continu;
		}
	}
}
