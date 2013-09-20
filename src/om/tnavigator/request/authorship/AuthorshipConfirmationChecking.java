package om.tnavigator.request.authorship;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

import om.Log;
import om.abstractservlet.RenderedOutput;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.DatabaseAccess.Transaction;
import om.tnavigator.sessions.UserSession;
import util.misc.Strings;

/**
 * Used to determine if a user of a particular Test Instance has confirmed to 
 *  their own Authorship.
 * @author Trevor Hinson
 */

public class AuthorshipConfirmationChecking {

	public static String AUTHORSHIP_CONFIRMATION = "authorshipConfirmation";

	private DatabaseAccess databaseAccess;

	private Log log;

	private AuthorshipQueryBean authorshipQueryBean;

	private String getRetrieveQuery(UserSession us)
		throws AuthorshipConfirmationException {
		checkAuthorshipConfirmationBean();
		return formatQuery(us, authorshipQueryBean.getOmQueries()
			.retrieveAuthorshipConfirmationQuery());
	}

	private String getUpdateQuery(UserSession us)
		throws AuthorshipConfirmationException {
		checkAuthorshipConfirmationBean();
		return formatQuery(us, authorshipQueryBean.getOmQueries()
			.updateAuthorshipConfirmationQuery());
	}

	private void checkAuthorshipConfirmationBean()
		throws AuthorshipConfirmationException {
		if (null == authorshipQueryBean) {
			throw new AuthorshipConfirmationException("Unable to continue as"
				+ " the composite AuthorshipQueryBean was null.");
		}
	}

	/**
	 * Formats the query argument to accomodate for the database test identifier
	 *  held within the UserSession.
	 * @param us
	 * @param query
	 * @return
	 * @throws AuthorshipConfirmationException
	 * @author Trevor Hinson
	 */
	protected String formatQuery(UserSession us, String query)
		throws AuthorshipConfirmationException {
		if (null == us || null == query) {
			throw new AuthorshipConfirmationException("Unable to continue as"
				+ " either the UserSession or the query was null: "
				+ " UserSession = " + us + " query = query");
		}
		int ti = us.getDbTi();
		Object[] arguments = {"'" + new Integer(ti).toString() + "'"};
		String formattedQuery = MessageFormat.format(query, arguments);
		if (Strings.isEmpty(formattedQuery)) {
			throw new AuthorshipConfirmationException("Unable to continue as"
				+ " the formatting of the query returned empty.");
		}
		return formattedQuery;
	}

	private DatabaseAccess getDatabaseAccess()
		throws AuthorshipConfirmationException {
		if (null == databaseAccess) {
			throw new AuthorshipConfirmationException("Unable to continue"
				+ " as the DatabaseAccess object was null.");
		}
		return databaseAccess;
	}

	public AuthorshipConfirmationChecking(DatabaseAccess da, Log l,
		AuthorshipQueryBean bean) {
		databaseAccess = da;
		log = l;
		authorshipQueryBean = bean;
	}

	/**
	 * Check to see if this user has confirmed their authorship for the
	 *  test requested within their UserSession.  We return false within the
	 *  RenderedOutput if the User has not confirmed yet.
	 * @param us
	 * @return
	 * @throws AuthorshipConfirmationException
	 * @author Trevor Hinson
	 */
	public RenderedOutput hasSuccessfullyConfirmed(UserSession us)
		throws AuthorshipConfirmationException {
		RenderedOutput ro = new RenderedOutput();
		ro.setSuccessful(false);
		if (null != us) {
			try {
				Transaction t = getDatabaseAccess().newTransaction();
				ResultSet rs = t.query(getRetrieveQuery(us));
				x : while (rs.next()) {
					int auth = rs.getInt(AUTHORSHIP_CONFIRMATION);
					if (auth == 1) {
						ro.setSuccessful(true);
						break x;
					}
				}
				t.finish();
			} catch (SQLException x) {
				throw new AuthorshipConfirmationException(x);
			}
		} else {
			handleNullUserSession();
		}
		return ro;
	}

	/**
	 * Takes the UserSession and updates the database stating that this
	 *  UserSession has confirmed authorship of their Test.  If everything
	 *  completes successfully then respond with true within the RenderedOutput.
	 * @param us
	 * @return
	 * @throws AuthorshipConfirmationException
	 * @author Trevor Hinson
	 */
	public RenderedOutput makeConfirmation(UserSession us)
		throws AuthorshipConfirmationException {
		RenderedOutput ro = new RenderedOutput();
		ro.setSuccessful(false);
		if (null != us) {
			try {
				String query = getUpdateQuery(us);
				log.logDebug("Running update query of : " + query);
				Transaction t = getDatabaseAccess().newTransaction();
				int n = t.update(query);
				t.finish();
				ro.setSuccessful((n > 0));
			} catch (SQLException x) {
				throw new AuthorshipConfirmationException(x);
			}
		} else {
			handleNullUserSession();
		}
		return ro;
	}

	private void handleNullUserSession() throws AuthorshipConfirmationException {
		throw new AuthorshipConfirmationException("Unable to determine if"
			+ " the authorshipConfirmation has been made as the UserSession was null.");
	}
}
