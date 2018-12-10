package om.tnavigator;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.Log;
import om.tnavigator.db.DatabaseAccess;

/**
 * Delete the test instance/intances
 */
public class DeleteUserTestAttempt {

	private NavigatorServlet ns;

	public DeleteUserTestAttempt(NavigatorServlet ns) {
		this.ns = ns;
	}

	/**
	 * Delete the test attempt
	 * @param request
	 * @param response
	 * @param dat
	 * @param tis comma-separated list of tis of test instances to delete
	 * @param log instance.
	 */
	public void deleteTestAttempt(HttpServletRequest request, HttpServletResponse response,  DatabaseAccess.Transaction dat, String tis, Log log) throws IOException {
		if ( tis!=null) {
			String message = "SUCCESS";
			String[] testInstances = tis.split(",");
			for (String testInstance : testInstances) {
				try
				{
					ns.getOmQueries().deleteEntireTestAttempts(dat, testInstance);
				} catch(SQLException ex)
				{
					log.logError("deleteUserTestAttempt", "Error deleting test attempt ti=" + testInstance);
					message = String.format("FAILURE");
				} finally
				{
					dat.finish();
				}
			}
			PrintWriter out = response.getWriter();
			out.print(message);
		}
	}
}
