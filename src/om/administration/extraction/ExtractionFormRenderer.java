package om.administration.extraction;

import java.util.Map;

import util.misc.Strings;


/**
 * Here we continue with the existing presentation model and detail with requests
 *  by rendering out a form that can be used to submit the "ti" (Test Instance)
 *  identifier of the Student whos details we wish to extract from the system.
 * NOTE that extraction does not mean removal.
 * @author Trevor Hinson
 */

public class ExtractionFormRenderer implements Extractor {

	private static String ENTER_DETAILS_TEXT = "Enter the Test Instance (ti) for the Student that you wish to extract.";

	@Override
	public ExtractionResponse extract(Map<String, Object> metaData)
		throws ExtractorException {
		ExtractionResponse er = new ExtractionResponse();
		String extractUrl = retrieveExtractUrl(metaData);
		if (Strings.isNotEmpty(extractUrl)) {
			er.setResponseMessage(generateStandardView(extractUrl));
			er.setExtracted(true);
		} else {
			throw new ExtractorException("Please ensure that the "
				+ ExtractorEnums.extractionUrl
				+ " has been configured for the Extractor servlet filter.");
		}
		return er;
	}

	private String generateStandardView(String url) {
		StringBuilder sb = new StringBuilder();
		sb.append(ENTER_DETAILS_TEXT).append("<br /><br />");
		sb.append("<form id=\"extractor\" method=\"post\" action=\"")
			.append(url).append("\"><input id=\"ti\" type=\"text\" name=\"")
			.append(ExtractorEnums.studentTi).append("\" /> <input type=\"submit\" value=\"submit\"")
			.append(" id=\"extractSubmission\" name=\"extractSubmission\" /></form>");
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
