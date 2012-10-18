package om.administration.databaseCleaner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Here we continue with the existing presentation model and detail with requests
 *  by rendering out a form that can be used to submit the "ti" (Test Instance)
 *  identifier of the Student whos details we wish to extract from the system.
 * NOTE that extraction does not mean removal.
 * @author Trevor Hinson
 */

public class ExtractionFormRenderer implements Extractor {

	private static String ENTER_DETAILS_TEXT = "Enter the from Date for question stats generation. (leave blank to run for all data)";
	private static String FORM_NAME = "databaseCleanerStats";
	private String DATEFORMAT = "dd-MM-yyyy";
	private String ALLDATA="all";


	@Override
	public ExtractionResponse extract(Map<String, Object> metaData)
		throws ExtractorException {
		ExtractionResponse er = new ExtractionResponse();
		String extractUrl = retrieveExtractUrl(metaData);
		if (StringUtils.isNotEmpty(extractUrl)) {
			er.setResponseMessage(generateStandardView(extractUrl,FORM_NAME));
			er.setExtracted(true);
		} else {
			throw new ExtractorException("Please ensure that the "
				+ ExtractorEnums.extractionUrl
				+ " has been configured for the Extractor servlet filter.");
		}
		return er;
	}

	private String generateStandardView(String url,String formName) {
		StringBuilder sb = new StringBuilder();
	     
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
		Long lEpocSeconds=now.getTime();
		
		sb.append(ENTER_DETAILS_TEXT).append(" in the form ").append(DATEFORMAT).append("<br /><br />");
		/* use todays date by default, store it as number of seconds since epoc in a hiddon field */
		sb.append("<form id=\"extractor\" method=\"post\" action=\"")
			.append(url);
			/* date field */
		sb.append("\"><input id=\"rptDate\" type=\"text\"  value=\"")
		//.append(sdf.format(now)).append("\" ")
		.append(ALLDATA).append("\" ")

		//.append("\" ")
		.append("name=\"")
		.append(ExtractorEnums.reportFromDate).append("\"/>");
		/* hidden date fields */
		//sb.append("<input id=\"rptDateSecs\" type=\"hidden\" value=\"")
		//.append(lEpocSeconds.toString()).append("\" ")
		//.append("name=\"")
		//.append(ExtractorEnums.reportFromDate).append("2")
		//.append("\" />");
		//sb.append("<input id=\"rptDateToday\" type=\"hidden\" value=\"")
		//.append(sdf.format(now)).append("\" ")
		//.append("name=\"")
		//.append(ExtractorEnums.reportFromDate).append("3")
		//.append("\" />");
		sb.append("<input type=\"submit\" value=\"submit\"")
		.append(" id=\""+formName+"\" name=\""+formName+"\" /></form>");
		return sb.toString();
	}

	private String retrieveExtractUrl(Map<String, Object> metaData) {
		String extractUrl = null;
		if (null != metaData) {
			Object obj = metaData.get(ExtractorEnums.postToUrl.toString());
			if (null != obj ? obj instanceof String : false) {
				extractUrl = (String) obj;
			}
		}
		return extractUrl;
	}

}
