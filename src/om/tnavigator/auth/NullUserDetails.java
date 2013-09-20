package om.tnavigator.auth;

/**
 * Implementation of UserDetails that returns "" or false for everything. Used when we ask
 * for details of a user who is not logged in.
 */
public class NullUserDetails implements UserDetails
{
	public String getAuthIDsAsString()
	{
		return "";
	}

	public String getCookie()
	{
		return "";
	}

	public String getPersonID()
	{
		return "";
	}

	public String getUsername()
	{
		return "";
	}

	public boolean hasAuthID(String sAuthId)
	{
		return false;
	}

	public boolean isLoggedIn()
	{
		return false;
	}

	public boolean shouldReceiveTestMail()
	{
		return false;
	}

	public boolean isSysTest()
	{
		return false;
	}
}
