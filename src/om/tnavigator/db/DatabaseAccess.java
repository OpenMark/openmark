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
package om.tnavigator.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import om.Log;
import om.OmException;

/** Thread-safe database access code */
public class DatabaseAccess
{
	/** JDBC URL for database */
	private DataSource dataSource;

	/** Log for debug logging */
	private Log l;

	/** Information about one connection */
	public static class ConnectionInfo
	{
		/** Actual connection */
		Connection c;

		/** Statement */
		Statement s;

		/** Last-used time */
		long lLastUsed;
	}

	/**
	 * A single SQL transaction that can contain multiple queries and updates.
	 * These are <i>not</i> themselves thread-safe, a single transaction must be
	 * used in a single thread only.
	 */
	public class Transaction
	{
		/** Connection this transaction is using */
		private ConnectionInfo ci;

		/** True if an error has occurred and we should rollback instead of committing */
		private boolean bError = false;

		/** Current result-set (if any) */
		private ResultSet rsCurrent = null;

		private long lTime = 0L;

		/**
		 * Constructs internally.
		 * @param ci Connection
		 */
		protected Transaction(ConnectionInfo ci) { this.ci=ci; }

		/**
		 * Runs an SQL query and returns a result set.
		 * <p>
		 * You do not need to close the ResultSet. It is automatically closed
		 * when finish() or any other Transaction call takes place.
		 * @param sSQL SQL query to run
		 * @return Results
		 * @throws SQLException
		 */
		public ResultSet query(String sSQL) throws SQLException
		{
			if(ci==null) throw new SQLException("Already commited");
			if(rsCurrent!=null)
			{
				rsCurrent.close();
				rsCurrent=null;
			}

			try
			{
				logDebug("[Q] "+sSQL);
				long lStart=System.currentTimeMillis();
				rsCurrent=ci.s.executeQuery(sSQL);
				long lElapsed=System.currentTimeMillis()-lStart;
				lTime+=lElapsed;
				logDebug("OK ("+lElapsed+"ms)");
				return rsCurrent;
			}
			catch(SQLException se)
			{
				bError=true;
				throw se;
			}
		}

		/**
		 * Runs an SQL update.
		 * @param sSQL SQL update command
		 * @return Number of rows
		 * @throws SQLException
		 */
		public int update(String sSQL) throws SQLException
		{
			try
			{
				if(ci==null) throw new SQLException("Already commited");
				if(rsCurrent!=null)
				{
					rsCurrent.close();
					rsCurrent=null;
				}
				logDebug("[U] "+sSQL);
				long lStart=System.currentTimeMillis();
				int iRows=ci.s.executeUpdate(sSQL);
				long lElapsed=System.currentTimeMillis()-lStart;
				lTime+=lElapsed;
				logDebug("OK ("+lElapsed+"ms)");
				return iRows;
			}
			catch(SQLException se)
			{
				bError=true;
				throw se;
			}
		}

		/**
		 * Must be called in try-finally, even if exceptions are thrown from other
		 * methods. Can safely call it more than once.
		 * @return Total time spent waiting inside all database calls
		 */
		public long finish()
		{
			if(ci!=null)
			{
				try
				{
					if(rsCurrent!=null)
					{
						rsCurrent.close();
						rsCurrent=null;
					}
					long lStart=System.currentTimeMillis();
					if(!bError)
						ci.c.commit();
					else
					  ci.c.rollback();
					long lElapsed=System.currentTimeMillis()-lStart;
					lTime+=lElapsed;
				}
				catch(SQLException se)
				{
					logError("Failure to finish() [error="+bError+"]",se);
				}
				releaseConnection(ci,bError);
				ci=null;
			}
			long lReturn=lTime;
			lTime=0L;
			return lReturn;
		}

		/**
		 * Call optionally before calling finish(), if some other error (not an
		 * exception from a Transaction method) occurred and you want to rollback.
		 * <p>
		 * finish() should <i>always</i> be called - you just call this first.
		 */
		public void rollback()
		{
			bError=true;
		}
	}

	/**
	 * Logs string, with exception.
	 * @param s Message
	 * @param t Exception
	 */
	private void logError(String s,Throwable t)
	{
		if(l!=null) l.logError("DatabaseAccess",s,t);
	}
	/**
	 * Logs string to debug log.
	 * @param s Message
	 */
	private void logDebug(String s)
	{
		if(l!=null) l.logDebug("DatabaseAccess",s,null);
	}

	/**
	 * Constructs access pool. Note that this starts a cleanup thread so you must
	 * take care to always call close().
	 * @param sURL JDBC URL of database including password etc.
	 * @param l Log to use for debug and error logging (null if none required)
	 * @throws OmException 
	 */
	public DatabaseAccess(Log l) throws OmException
	{
		this.l = l;
	}

	/**
	 * Closes access pool along with all database connections.
	 */
	public synchronized void close()
	{
	}

	/**
	 * Creates a new DatabaseAccess.Transaction. You <i>must</i> call finish()
	 * on this - immediately after this call returns, a try/finally block should
	 * begin that calls finish() on the Transaction.
	 * <p>
	 * This call may block if no connection is available.
	 * @return Transaction object to do stuff with
	 * @throws SQLException If there's a problem creating a required database
	 *   connection
	 */
	public Transaction newTransaction() throws SQLException
	{
		return new Transaction(getConnection());
	}

	/**
	 * Obtains a new connection separate from the general pool. You <i>must</i>
	 * close this connection when done with it (use try/finally).
	 * @return New connection object
	 * @throws SQLException If there's a problem creating the connection
	 */
	public Connection newUnpooledConnection() throws SQLException
	{
		Connection c = getDataSource().getConnection();
		c.setAutoCommit(false);
		return c;
	}

	private DataSource getDataSource() throws SQLException {
		if (dataSource != null) {
			return dataSource;
		}

		try
		{
			InitialContext context = new InitialContext();
			dataSource = (DataSource)context.lookup("java:/comp/env/jdbc/openmark");
			if(dataSource == null)
			{
				throw new SQLException("Could not find data source jdbc/openmark");
			}
		}
		catch(NamingException e)
		{
			throw new SQLException("Error loading data source jdbc/openmark", e);
		}
		return dataSource;
	}

	/**
	 * Obtain a database connection and lock it (must call releaseConnection).
	 * @return Connection info
	 * @throws SQLException Any problem getting connection
	 */
	private synchronized ConnectionInfo getConnection() throws SQLException
	{
		ConnectionInfo ci=new ConnectionInfo();
		ci.c = getDataSource().getConnection();
		ci.c.setAutoCommit(false);
		ci.s = ci.c.createStatement();
		return ci;
	}

	/**
	 * Release a database connection obtained using getConnection().
	 * @param ci Connection info
	 * @param bDiscard If true, discards the connection rather than returning it
	 *   to the pool
	 */
	private synchronized void releaseConnection(ConnectionInfo ci,boolean bDiscard)
	{
		if (ci.s != null)
		{
			try
			{
				ci.s.close();
			}
			catch(SQLException e)
			{
				l.logError("DatabaseAccess", "Failed to close an SQL statement.", e);
			}
			ci.s = null;
		}

		if (ci.c != null)
		{
			try
			{
				ci.c.close();
			}
			catch(SQLException e)
			{
				l.logError("DatabaseAccess", "Failed to close an database connection.", e);
			}
			ci.c = null;
		}
	}

	/**
	 * Return information for the status page about how the database connection is working.
	 *
	 * All keys in the Map returned should start DB.
	 *
	 * @return various statistics about the performance of database connections.
	 */
	public Map<String, String> getConnectionStats()
	{
		Map<String, String> stats = new HashMap<String, String>();
		Method method;

		try
		{
			try
			{
				method = dataSource.getClass().getMethod("getActive");
				stats.put("DBACTIVE", "" + method.invoke(dataSource));
			}
			catch (NoSuchMethodException e)
			{
				// Ignore.
			}

			try
			{
				method = dataSource.getClass().getMethod("getIdle");
				stats.put("DBIDLE", "" + method.invoke(dataSource));
			}
			catch (NoSuchMethodException e)
			{
				// Ignore.
			}

			try
			{
				method = dataSource.getClass().getMethod("getPoolSize");
				stats.put("DBTOTAL", "" + method.invoke(dataSource));
			}
			catch (NoSuchMethodException e)
			{
				// Ignore.
			}
		}
		catch (SecurityException e)
		{
			// Ignore.
		}
		catch(IllegalAccessException e)
		{
			// Ignore.
		}
		catch(IllegalArgumentException e)
		{
			// Ignore.
		}
		catch(InvocationTargetException e)
		{
			// Ignore.
		}

		return stats;
	}
}
