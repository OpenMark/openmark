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
		POST_NO_SESSION,
		REDIRECTING,
	}

	public Status status = Status.OK;

	/**
	 * Holds the user session, if we find one.
	 */
	public UserSession us;
}
