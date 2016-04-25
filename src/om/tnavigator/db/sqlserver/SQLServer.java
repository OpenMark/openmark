/* OpenMark online assessment system
   Copyright (C) 2007 The Open University

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package om.tnavigator.db.sqlserver;

import java.sql.ResultSet;
import java.sql.SQLException;

import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.OmQueries;
import om.tnavigator.db.DatabaseAccess.Transaction;
import util.misc.Strings;

/**
 * Specialisation of OmQueries for Microsoft SQL Server.
 */
public class SQLServer extends OmQueries
{
	/**
	 * @param prefix the database prefix to use.
	 * @throws ClassNotFoundException
	 */
	public SQLServer(String prefix) throws ClassNotFoundException {
		super(prefix);
		Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
	}

	@Override
	public int getInsertedSequenceID(Transaction dat,String table,String column)
		throws SQLException
	{
		ResultSet rs=dat.query("SELECT @@IDENTITY");
		rs.next();
		return rs.getInt(1);
	}

	@Override
	public ResultSet queryUnfinishedSessions(DatabaseAccess.Transaction dat,String oucu,String deploy) throws SQLException
	{
		return dat.query(
			"SELECT TOP 1 ti,rseed,finished,variant,testposition,navigatorversion,pi " +
			"FROM " + getPrefix() + "tests " +
			"WHERE oucu="+Strings.sqlQuote(oucu)+" AND deploy="+Strings.sqlQuote(deploy)+" " +
			"ORDER BY attempt DESC;");
	}

	@Override
	public String queryGuessPiForOucu(Transaction dat, String oucu) throws SQLException
	{
		ResultSet rs = dat.query(
				"SELECT TOP 1 pi " +
				"FROM " + getPrefix() + "tests " +
				"WHERE oucu = " + Strings.sqlQuote(oucu) + " AND pi <> oucu " +
				"ORDER BY clock DESC");
		try
		{
			if (rs.next())
			{
				return rs.getString(1);
			}
			else
			{
				return null;
			}
		}
		finally
		{
			rs.close();
		}
	}

	@Override
	protected String alterStringColumnWidthSQL(String table, String column, int newWidth) {
		return "ALTER TABLE " + getPrefix() + table +" ALTER COLUMN " + column + " NVARCHAR(" + newWidth + ")";
	}

	@Override
	protected String currentDateFunction()
	{
		return "GETDATE()";
	}

	@Override
	protected String dateTimeFieldType()
	{
		return "DATETIME";
	}

	@Override
	public void checkDatabaseConnection(DatabaseAccess.Transaction dat)
		throws SQLException
	{
		dat.query("SELECT @@version");
	}

	@Override
	protected String unicode(String quotedString)
	{
		return "N"+quotedString;
	}

	@Override
	protected void upgradeDatabaseTo131(DatabaseAccess.Transaction dat) throws SQLException
	{
		dat.update("ALTER TABLE " + getPrefix() + "tests ADD navigatorversion CHAR(16)");
		dat.update("UPDATE " + getPrefix() + "tests SET navigatorversion = '1.3.0'");
		dat.update("ALTER TABLE " + getPrefix() + "tests ALTER COLUMN navigatorversion CHAR(16) NOT NULL");
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.db.OmQueries#extractMonthFromTimestamp(java.lang.String)
	 */
	@Override
	protected String extractMonthFromTimestamp(String value) {
		return "MONTH(" + value + ")";
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.db.OmQueries#extractYearFromTimestamp(java.lang.String)
	 */
	@Override
	protected String extractYearFromTimestamp(String value) {
		return "YEAR(" + value + ")";
	}
}
