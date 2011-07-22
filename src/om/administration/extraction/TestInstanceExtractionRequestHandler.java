package om.administration.extraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.AbstractRequestHandler;
import om.DisplayUtils;
import om.RenderedOutput;
import om.RequestAssociates;
import om.RequestHandlerEnums;
import om.RequestHandlingException;
import om.RequestParameterNames;
import om.RequestResponse;
import om.tnavigator.NavigatorConfig;

import org.apache.commons.lang.StringUtils;

import util.misc.UtilityException;

public class TestInstanceExtractionRequestHandler extends AbstractRequestHandler {

	private static final long serialVersionUID = 3432094601409857406L;

	private String filteredUrl;

	private String postToUrl;

	private String extractionFileNamePrefix;

	private List<String> testInstanceTables;

	private List<String> questionInstanceTables;

	@Override
	public RequestResponse handle(HttpServletRequest request,
		HttpServletResponse response, RequestAssociates associates)
		throws RequestHandlingException {
		RequestResponse rr = new RenderedOutput();
		if (null != request && null != response && null != associates) {
			initialise(associates);
			String uri = request.getPathInfo();
			if (StringUtils.isNotEmpty(filteredUrl)
				? filteredUrl.equals(uri) : false) {
				getLog().logDebug("Running the extractor ...");
				StringBuilder output = new StringBuilder(DisplayUtils.header());
				try {
					ExtractionResponse er = process(request, associates);
					if (null != er ? er.isExtracted() : false) {
						output.append("<h1>Student Details Extractor</h1><br /><br />")
							.append(er.getResponseMessage());
					}
				} catch (ExtractorException x) {
					output.append("<h1>An Error Occured with the Extractor </h1><br /><br />");
					output.append(handleExtractorException(x.getMessage()));
				}
				output.append(DisplayUtils.footer());
				rr.append(output.toString());
				getLog().logDebug("Finished now rendering back to the user : " + output);
			}
		}
		return rr;
	}

	/**
	 * Picks up the necessary elements from the RequestAssociates and holds
	 *  them as composite objects of this class for use later.
	 * NOTE that nothing Student or Request or Session specific should be
	 *  handled in this way.
	 * @param associates
	 * @exception
	 * @author Trevor Hinson
	 */
	public void initialise(RequestAssociates associates)
		throws RequestHandlingException {
		super.initialise(associates);
		Object o = associates.getConfiguration().get(
			RequestHandlerEnums.invocationPath.toString());
		if (null != o ? o instanceof String : false) {
			filteredUrl = (String) o;
		}
		
		Object po = associates.getConfiguration().get(
			ExtractorEnums.postToUrl.toString());
		if (null != po ? po instanceof String : false) {
			postToUrl = (String) po;
		}
		
		Object ef = associates.getConfiguration().get(
			ExtractorEnums.extractionFileNamePrefix.toString());
		if (null != ef ? ef instanceof String : false) {
			extractionFileNamePrefix = (String) ef; 
		}
		
		Object tt = associates.getConfiguration().get(
			ExtractorEnums.TestInstanceTables.toString());
		if (null != tt ? tt instanceof String : false) {
			testInstanceTables = convert((String) tt);
		}
		
		Object qt = associates.getConfiguration().get(
			ExtractorEnums.QuestionInstanceTables.toString());
		if (null != qt ? qt instanceof String : false) {
			questionInstanceTables = convert((String) qt);
		}
	}

	/**
	 * Simply wraps the exception thrown from the ExtractionRequestHandler so 
	 *  to present it back to the user.
	 * @param s
	 * @return
	 * @author Trevor Hinson
	 */
	private String handleExtractorException(String s) {
		return new StringBuilder(DisplayUtils.header())
			.append("<br /><br />").append("Error : ").append(s)
			.append(DisplayUtils.footer()).toString();
	}

	/**
	 * Checks for a existence of the "studentTi" within the request and
	 *  delegates to the relevant request handler accordingly.
	 * @param request
	 * @param associates
	 * @return
	 * @throws ExtractorException
	 * @author Trevor Hinson
	 */
	private ExtractionResponse process(HttpServletRequest request,
		RequestAssociates associates) throws ExtractorException {
		String studentTi = request.getParameter(ExtractorEnums.studentTi.toString());
		return StringUtils.isNotEmpty(studentTi)
			? delegateForExtraction(request, associates, studentTi)
				: renderRequestForm(request, associates);
	}

	/**
	 * Simply wraps a new ExtractionFormRenderer's extract method.
	 * @param request
	 * @return
	 * @throws ExtractorException
	 * @author Trevor Hinson
	 */
	private ExtractionResponse renderRequestForm(HttpServletRequest request,
		RequestAssociates associates) throws ExtractorException {
		return new ExtractionFormRenderer().extract(
			generateMetaData(request, associates));
	}

	/**
	 * Wraps around the actual extraction of the Test Instance data.
	 * @param request
	 * @param associates
	 * @return
	 * @throws ExtractorException
	 * @author Trevor Hinson
	 */
	private ExtractionResponse delegateForExtraction(HttpServletRequest request,
		RequestAssociates associates, String studentTi) throws ExtractorException {
		ExtractionResponse er = null;
		Extractor erh = new StudentDetailsExtractor();
		er = erh.extract(generateMetaData(request, associates));
		try {
			((StudentDetailsExtractor) erh).close(null);
		} catch (UtilityException x) {
			throw new ExtractorException(x);
		}
		return er;
	}

	/**
	 * Picks up the NavigatorConfig that is required in order to run the
	 *  extraction process.
	 * @param ra
	 * @return
	 * @throws IOException
	 * @author Trevor Hinson
	 */
	private NavigatorConfig pickupNavigatorConfig(RequestAssociates ra)
		throws IOException {
		NavigatorConfig navigatorConfig = null;
		if (null != ra) {
			Object o = ra.getPrincipleObjects().get(
				RequestParameterNames.NavigatorConfig.toString());
			if (null != o ? o instanceof NavigatorConfig : false) {
				navigatorConfig = (NavigatorConfig) o;
			}
		}
		return navigatorConfig;
	}

	/**
	 * Builds the metaData map from the various parameters needed to handle
	 *  the request.
	 * @param request
	 * @return
	 * @throws ExtractorException
	 * @author Trevor Hinson
	 */
	private Map<String, Object> generateMetaData(HttpServletRequest request,
		RequestAssociates associates) throws ExtractorException {
		Map<String, Object> metaData = new HashMap<String, Object>();
		try {
			metaData.put(ExtractorEnums.extractionUrl.toString(), filteredUrl);
			String studentTi = request.getParameter(
				ExtractorEnums.studentTi.toString());
			metaData.put(ExtractorEnums.postToUrl.toString(), postToUrl);
			metaData.put(ExtractorEnums.navigatorConfigKey.toString(),
				pickupNavigatorConfig(associates));
			if (StringUtils.isNotEmpty(studentTi)
				&& StringUtils.isNotEmpty(extractionFileNamePrefix)) {
				metaData.put(ExtractorEnums.extractionFileNamePrefix.toString(),
					extractionFileNamePrefix);
				metaData.put(RequestParameterNames.logPath.toString(), getLogPath());
				metaData.put(ExtractorEnums.studentTi.toString(), studentTi);
				if (null != testInstanceTables) {
					metaData.put(ExtractorEnums.TestInstanceTables.toString(),
						testInstanceTables);
				}
				if (null != questionInstanceTables) {
					metaData.put(ExtractorEnums.QuestionInstanceTables.toString(),
						questionInstanceTables);
				}
			}
		} catch (IOException x) {
			throw new ExtractorException(x);
		}
		return metaData;
	}

	/**
	 * Returns a generated List<String> based on what is configured within the
	 *  web.xml for this filter implementation.
	 * @param s
	 * @return
	 * @author Trevor Hinson
	 */
	List<String> convert(String s) {
		List<String> lst = null;
		if (StringUtils.isNotEmpty(s)) {
			String[] bits = s.split(",");
			if (null != bits) {
				for (int i = 0; i < bits.length; i++) {
					String bit = bits[i];
					if (StringUtils.isNotEmpty(bit)) {
						if (null == lst) {
							lst = new ArrayList<String>();
						}
						lst.add(bit.trim());
					}
				}
			}
		}
		return lst;
	}
}
