	package om.administration.dataDeletion;
	import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.abstractservlet.RenderedOutput;
import om.abstractservlet.RequestAssociates;
import om.abstractservlet.RequestHandlingException;
import om.abstractservlet.RequestResponse;
	
	public class DataDeletionWithQuestionsSQLGenerationHandler extends DatabaseDeletionSQLGeneration {
		
		private static boolean findQuestions=true;
		@Override
		public RequestResponse handle(HttpServletRequest request,
			HttpServletResponse response, RequestAssociates associates)
			throws RequestHandlingException {		
			
			RequestResponse rr = new RenderedOutput();
			
			rr=handleAll(request,response,associates,findQuestions);

			return rr;
			}

}


