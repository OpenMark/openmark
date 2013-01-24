package om;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.abstractservlet.RequestAssociates;
import om.abstractservlet.RequestHandler;
import om.abstractservlet.RequestHandlingException;
import om.abstractservlet.RequestResponse;
import util.misc.FinalizedResponse;
import util.misc.UtilityException;

public class DummyRequestHandler implements RequestHandler {

	private static final long serialVersionUID = 1064595816664072769L;

	@Override
	public RequestResponse handle(HttpServletRequest request,
		HttpServletResponse response, RequestAssociates associates)
		throws RequestHandlingException {
		return null;
	}

	@Override
	public FinalizedResponse close(Object o) throws UtilityException {
		return null;
	}

}
