package om.abstractservlet;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.misc.GracefulFinalization;

/**
 * For each request a <b>new</b> implementation of this contract is created.
 *  This allows for objects to be held within the RequestHandler without the
 *  risk of session contamination. It also means that the servlet can be used
 *  more for control rather than business logic. This means that the Model from
 *  the MVC pattern is delegated to from the implementation of this interface.
 * @author Trevor Hinson
 */

public interface RequestHandler extends Serializable, GracefulFinalization {

	/**
	 * Delegated to in order to handle HTTP requests.  Different implementations
	 *  of this interface can be decided upon which to use from within
	 *  the controller (Servlet) based on the request itself.  We could abstract
	 *  that decision further from the Servlet also but that has been left at
	 *  this time.
	 * @param request
	 * @param response
	 * @param associates
	 * @throws RequestHandlingException
	 * @return
	 * @author Trevor Hinson
	 */
	RequestResponse handle(HttpServletRequest request,
		HttpServletResponse response, RequestAssociates associates)
		throws RequestHandlingException;

}
