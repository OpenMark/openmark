package om.tnavigator.auth;

/**
 * Implementation of UserDetails that returns "" or false for everything. Used when we ask
 * for details of a user who is not logged in.
 */
public class NullUserDetails implements UserDetails
{
	@Override
	public String getAuthIDsAsString()
	{
		return "";
	}

	@Override
	public String getCookie()
	{
		return "";
	}

	@Override
	public String getPersonId()
	{
		return "";
	}

	@Override
	public String getUsername()
	{
		return "";
	}

	@Override
	public boolean hasAuthID(String sAuthId)
	{
		return false;
	}

	@Override
	public boolean isLoggedIn()
	{
		return false;
	}

	@Override
	public boolean shouldReceiveTestMail()
	{
		return false;
	}
}
