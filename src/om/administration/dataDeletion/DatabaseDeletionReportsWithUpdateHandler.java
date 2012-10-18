package om.administration.dataDeletion;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.RenderedOutput;
import om.RequestAssociates;
import om.RequestHandlingException;
import om.RequestResponse;

public class DatabaseDeletionReportsWithUpdateHandler extends DatabaseDeletionReports {

	private static boolean DOUPDATE=true;
	private static final long serialVersionUID = 3432094601409857406L;


@Override
public RequestResponse handle(HttpServletRequest request,
	HttpServletResponse response, RequestAssociates associates)
	throws RequestHandlingException {		
	
	RequestResponse rr = new RenderedOutput();

	rr=handleAll(request,response,associates,DOUPDATE);

	return rr;
	}

}