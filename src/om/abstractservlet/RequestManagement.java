package om.abstractservlet;

import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.misc.UtilityException;


public interface RequestManagement extends GracefulFinalization, Serializable {

	/**
	 * The implementation should pick up the appropriate, configured,
	 *  RequestHandler implementation and then delegate to it providing the
	 *  response back to that which invoked this.
	 * @param request
	 * @throws UtilityException
	 * @author Trevor Hinson
	 */
	void handleRequest(HttpServletRequest request,
		HttpServletResponse response, RequestAssociates associates)
		throws UtilityException;

	/**
	 * Provides runtime details of the configuration settings.  Should return
	 *  an immutable Map
	 * @return
	 * @author Trevor Hinson
	 */
	Map<String, RequestHandlerSettings> getSettings();

}
