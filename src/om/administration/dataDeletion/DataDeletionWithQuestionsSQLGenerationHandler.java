	package om.administration.dataDeletion;
	import javax.servlet.http.HttpServletRequest;
	import javax.servlet.http.HttpServletResponse;

	import om.RenderedOutput;
	import om.RequestAssociates;
	import om.RequestHandlingException;
	import om.RequestResponse;
	
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


