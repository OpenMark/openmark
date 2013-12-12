package om.tnavigator.sessions;

/**
 * As part of establishing whether the user is logged in, we need to temporarily
 * track various related bits of information that comes from cookies, in order
 * to find the right UserSession, if any. This class groups the various things
 * into one object.
 */
public class ClaimedUserDetails
{
	public enum Status {
		OK,
		CANNOT_CREATE_COOKIE,
		TEMP_FORBID,
	}

	public Status status = Status.OK;

	/**
	 * The username from the authentication cookie. We have not yet validated that
	 * they are actually logged in as this user.
	 */
	public String sOUCU;

	/**
	 * Fake OUCU. This is a random string used when the test can be attempted by
	 * people who are not logged in.
	 */
	public String sFakeOUCU;

	/**
	 * Hash used to validate the fake OUCU.
	 */
	public int iAuthHash;

	/**
	 * Holds the user session, if we find one.
	 */
	public UserSession us;
}
