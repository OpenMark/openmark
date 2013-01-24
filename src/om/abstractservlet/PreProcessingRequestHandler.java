package om.abstractservlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.tnavigator.UserSession;

/**
 * Template contract for things that need to be handled before the rest of the
 *  processing takes place.
 * @author Trevor Hinson
 */

public interface PreProcessingRequestHandler extends RequestHandler {

	/**
	 * This is used to take anything that is needed from the argument objects
	 *  in order to form the RequestAssociates visitor that is passed to the
	 *  implementing RequestHandler at run time.  For example, if the
	 *  implementation of this interface needs certain things to work then they
	 *  can be extracted from the main objects here.
	 * @param servlet
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	RequestAssociates generateRequiredRequestAssociates(HttpServlet servlet,
		HttpServletRequest request, HttpServletResponse response,
		UserSession session)
		throws RequestHandlingException;

}
