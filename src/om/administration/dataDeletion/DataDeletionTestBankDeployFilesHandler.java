	package om.administration.dataDeletion;
	import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.abstractservlet.RenderedOutput;
import om.abstractservlet.RequestAssociates;
import om.abstractservlet.RequestResponse;
import util.misc.UtilityException;

public class DataDeletionTestBankDeployFilesHandler extends DataDeletionTestBank {
	private static final long serialVersionUID = 8250850902628464468L;

	@Override
	public RequestResponse handle(HttpServletRequest request,
		HttpServletResponse response, RequestAssociates associates)
		throws UtilityException {
		
		RequestResponse rr = new RenderedOutput();
		
		rr=handleAll(request,response,associates);

		return rr;
	}

}
