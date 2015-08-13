package om.tnavigator.overduenotifier;

import java.io.Serializable;

/**
 * A simple data holder for test attempts that need to have a reminder sent.
 */
public class ReminderDetails implements Serializable
{
	private static final long serialVersionUID = -8007771713199598483L;

	private String pi;

	private String deploy;

	private String oucu;

	private int ti;

	public ReminderDetails(String oucu, String pi, int ti, String deploy)
	{
		this.pi = pi;
		this.oucu = oucu;
		this.ti = ti;
		this.deploy = deploy;
	}

	public int getTi()
	{
		return ti;
	}

	public String getOucu()
	{
		return oucu;
	}

	public String getDeploy()
	{
		return deploy;
	}

	public String getPi()
	{
		return pi;
	}

	public String toString()
	{
		return "[" + oucu + "|" + pi + "|" + deploy + "|" + ti + "]";
	}
}
