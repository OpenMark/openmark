package om.administration.databaseCleaner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.DisplayUtils;
import om.abstractservlet.AbstractRequestHandler;
import om.abstractservlet.RenderedOutput;
import om.abstractservlet.RequestAssociates;
import om.abstractservlet.RequestHandlerEnums;
import om.abstractservlet.RequestHandlingException;
import om.abstractservlet.RequestParameterNames;
import om.abstractservlet.RequestResponse;
import om.administration.questionbank.CleaningException;
import om.administration.questionbank.QuestionAndTestBankLocations;
import om.tnavigator.NavigatorConfig;
import util.misc.Strings;


/**
 * Provides http(s) request level access to a configured (or default)
 * CleanQuestionBanks implementation to handle the request and respond back.
 * 
 * @author Trevor Hinson
 */

public class DatabaseCleaningRequestHandler extends AbstractRequestHandler {

	private static final long serialVersionUID = 3432094601409857406L;

	private String filteredUrl;

	private String postToUrl;

	private String extractionFileNamePrefix;

	private List<String> testInstanceTables;

	private List<String> questionInstanceTables;

	private QueryQuestionBanks questionBanks;
	
	private static String HEADING1 = "<h1>Report generation for pre database clearence</h1><br /><br />";

	private static String FINISHED = "Finished now rendering back to the user : ";

	private static String EXTRACT_ERROR ="<h1>An Error Occured Running the clearence report </h1><br /><br />" ;
	
	private static String RUNNING="Running the clearence report ...";
	
	//private static String QUERYER = "CleanQuestionBanksImplementation";
	private static String QUERYER = "DatabaseCleaningImplementation";

	private static String QUESTION_BANKS = "questionBanks";

	private static String TEST_BANKS = "testBanks";

	private static String COMMA = ",";


	@Override
	public RequestResponse handle(HttpServletRequest request,
		HttpServletResponse response, RequestAssociates associates)
		throws RequestHandlingException {
		RequestResponse rr = new RenderedOutput();
		if (null != request && null != response && null != associates) {
			initialise(associates);
			String uri = request.getPathInfo();
			if (Strings.isNotEmpty(filteredUrl)
				? filteredUrl.equals(uri) : false) {
				getLog().logDebug(RUNNING);
				StringBuilder output = new StringBuilder(DisplayUtils.header());
				try {
					ExtractionResponse er = process(request,response, associates);
					if (null != er ? er.isExtracted() : false) {
						output.append(HEADING1)
							.append(er.getResponseMessage());
					}
				} catch (ExtractorException x) {
					output.append(EXTRACT_ERROR);
					output.append(handleExtractorException(x.getMessage()));
				}
				output.append(DisplayUtils.footer());
				rr.append(output.toString());
				getLog().logDebug( FINISHED + output);
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
	 * checks if date provide and
	 *  delegates to the relevant request handler accordingly.
	 * @param request
	 * @param associates
	 * @return
	 * @throws ExtractorException
	 * @author Trevor Hinson
	 */
	private ExtractionResponse process(HttpServletRequest request,HttpServletResponse response,
		RequestAssociates associates) throws ExtractorException {
		String reportFromDate = request.getParameter(ExtractorEnums.reportFromDate.toString());
		return Strings.isNotEmpty(reportFromDate)
			? generateDataExtraction(request, response,associates, reportFromDate)
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
	private ExtractionResponse generateDataExtraction(HttpServletRequest request,HttpServletResponse response, 
		RequestAssociates associates, String studentTi) throws ExtractorException {
		ExtractionResponse er = null;		
		try
		{	
			RequestResponse rr = super.handle(request, response, associates);
			identifyQuestions(associates, request, rr);
		}
		catch(RequestHandlingException e)
		{
			throw new ExtractorException(e);

		}
		
		return er;
	}
	/*
	 * determiones questions from test files
	 * 
	 * @param associates
	 * @param request
	 * @param rr
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	protected void identifyQuestions(RequestAssociates associates,
			HttpServletRequest request, RequestResponse rr)
			throws RequestHandlingException {
			try {
				QueryQuestionBanks cqb = getQuestionBanks(associates);
				QBTBQueryResponse cr = cqb.identify(
					identifyLocations(associates), associates);
				//RenderedCleaningResult rcr = clearanceResponseRenderer
					//	.renderIdentifiedQuestions(cr, associates);
				//if (null != rcr) {
				//	rr.append(rcr.toString());
				//}
				//request.getSession().setAttribute(CLEARANCE_RESPONSE, cr);
			} catch (CleaningException x) {
				throw new RequestHandlingException(x);
			}
		}
	
	/**
	 * Picks up the locations configured for the actual question banks and then
	 * returns these.
	 * 
	 * @param associates
	 * @return
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	protected QuestionAndTestBankLocations identifyLocations(
		RequestAssociates associates) throws RequestHandlingException {
		QuestionAndTestBankLocations locations = null;
		if (null != associates) {
			List<String> questionBanks = retrieveXBank(associates,
				QUESTION_BANKS);
			List<String> testBanks = retrieveXBank(associates, TEST_BANKS);
			if (validList(questionBanks) && validList(testBanks)) {
				locations = new QuestionAndTestBankLocations();
				locations.setQuestionBanks(questionBanks);
				locations.setTestBanks(testBanks);
			}
		}
		return locations;
	}
	

	/**
	 * Checks to see if the List<String> argument is not null and contains
	 * elements.
	 * 
	 * @param lst
	 * @return
	 * @author Trevor Hinson
	 */
	protected boolean validList(List<String> lst) {
		return null != lst ? lst.size() > 0 : false;
	}
	
	/**
	 * Retrieves a given set of question or test banks from the configuration
	 * based on the key argument.
	 * 
	 * @param associates
	 * @param key
	 * @return
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	protected List<String> retrieveXBank(RequestAssociates associates,
		String key) throws RequestHandlingException {
		List<String> banks = null;
		if (null != associates ? Strings.isNotEmpty(key) : false) {
			String value = associates.getConfig(key);
			if (Strings.isNotEmpty(value)) {
				String[] pieces = value.split(COMMA);
				for (int i = 0; i < pieces.length; i++) {
					String bit = pieces[i];
					if (Strings.isNotEmpty(bit)) {
						if (null == banks) {
							banks = new ArrayList<String>();
						}
						banks.add(bit);
					}
				}
			}
		}
		return banks;
	}
	/**
	 * Returns the composite cleanQuestionBanks implementation. The
	 * implementation itself can be configured. If there is no configuration, ie 
	 * getConfig returns NULL
	 * then the default implementation is used. NOTE - this implementation
	 * is based on a "per request" - is is not geared to be multi-threaded.
	 * 
	 * @param associates
	 * @return
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	protected QueryQuestionBanks getQuestionBanks(
			RequestAssociates associates) throws RequestHandlingException {
		if (null == questionBanks) {
			if (null != associates) {
				String className = associates.getConfig(QUERYER);
				getLog().logDebug(
					"Trying to class load implementaiton : " + className);
				questionBanks = retrieveConfiguredCleaner(className);
			}
		}
		return questionBanks;
	}
	
	/**
	 * Tries to dynamically classload the className argument and IF this turns
	 * out to be a valid assignable class then it is returned. Otherwise the
	 * default implementation is returned.
	 * 
	 * @param className
	 * @return
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	protected QueryQuestionBanks retrieveConfiguredCleaner(String className)
		throws RequestHandlingException {
		QueryQuestionBanks cqb = new QuestionBankQueryer();
		if (Strings.isNotEmpty(className)) {
			try {
				Class<?> cla = Class.forName(className);
				if (null != cla) {
					try {
						Object obj = cla.newInstance();
						if (null != obj ? QueryQuestionBanks.class
								.isAssignableFrom(obj.getClass()) : false) {
							cqb = (QueryQuestionBanks) obj;
						}
					} catch (InstantiationException x) {
						throw new RequestHandlingException(x);
					} catch (IllegalAccessException x) {
						throw new RequestHandlingException(x);
					}
				}
			} catch (ClassNotFoundException x) {
				throw new RequestHandlingException(x);
			}
		}
		return cqb;
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
			//TODO SLW2

			//String studentTi = request.getParameter(
			//ExtractorEnums.studentTi.toString());
			String studentTi = "";
			metaData.put(ExtractorEnums.postToUrl.toString(), postToUrl);
			metaData.put(ExtractorEnums.navigatorConfigKey.toString(),
				pickupNavigatorConfig(associates));
			if (Strings.isNotEmpty(studentTi)
				&& Strings.isNotEmpty(extractionFileNamePrefix)) {
				metaData.put(ExtractorEnums.extractionFileNamePrefix.toString(),
					extractionFileNamePrefix);
				metaData.put(RequestParameterNames.logPath.toString(), getLogPath());
				//TODO SLW2
				//metaData.put(ExtractorEnums.studentTi.toString(), studentTi);
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
		if (Strings.isNotEmpty(s)) {
			String[] bits = s.split(",");
			if (null != bits) {
				for (int i = 0; i < bits.length; i++) {
					String bit = bits[i];
					if (Strings.isNotEmpty(bit)) {
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
		/*
	protected void identifyTestQuestions(RequestAssociates associates,
			HttpServletRequest request, RequestResponse rr)
			throws RequestHandlingException {
			try {
				QuestionBanks cqb = getQuestionBanks(associates);
				ClearanceResponse cr = cqb.identify(
					identifyLocations(associates), associates);
				RenderedCleaningResult rcr = clearanceResponseRenderer
						.renderIdentifiedQuestions(cr, associates);
				if (null != rcr) {
					rr.append(rcr.toString());
				}
				request.getSession().setAttribute(CLEARANCE_RESPONSE, cr);
			} catch (CleaningException x) {
				throw new RequestHandlingException(x);
			}
		}
	

	  protected QuestionBanks getQuestionBanks(
			RequestAssociates associates) throws RequestHandlingException {
		if (null == QuestionBanks) {
			if (null != associates) {
				String className = associates.getConfig(QUERYER);
				getLog().logDebug(
					"Trying to class load implementaiton : " + className);
				QuestionBanks = retrieveConfiguredCleaner(className);
			}
		}
		return QuestionBanks;
	}
	
	protected QuestionAndTestBankLocations identifyLocations(
			RequestAssociates associates) throws RequestHandlingException {
			QuestionAndTestBankLocations locations = null;
			if (null != associates) {
				List<String> questionBanks = retrieveXBank(associates,
					QUESTION_BANKS);
				List<String> testBanks = retrieveXBank(associates, TEST_BANKS);
				if (validList(questionBanks) && validList(testBanks)) {
					locations = new QuestionAndTestBankLocations();
					locations.setQuestionBanks(questionBanks);
					locations.setTestBanks(testBanks);
				}
			}
			return locations;
		}
	
	*/
}

