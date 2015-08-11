package om.administration.dataDeletion;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.abstractservlet.RenderedOutput;
import om.abstractservlet.RequestAssociates;
import om.abstractservlet.RequestResponse;
import util.misc.UtilityException;

public class DataMungingLegalMinimumHandler extends DatabaseDeletionSQLGeneration {
	private static final long serialVersionUID = 8199140125837639783L;

	private static boolean findQuestions=false;
	@Override
	public RequestResponse handle(HttpServletRequest request,
		HttpServletResponse response, RequestAssociates associates)
		throws UtilityException {
		
		RequestResponse rr = new RenderedOutput();
		
		rr=handleAll(request,response,associates,findQuestions);

		return rr;
	}
}
