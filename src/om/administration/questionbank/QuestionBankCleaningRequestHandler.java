package om.administration.questionbank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.abstractservlet.AbstractRequestHandler;
import om.abstractservlet.RequestAssociates;
import om.abstractservlet.RequestResponse;
import util.misc.Strings;
import util.misc.UtilityException;


/**
 * Provides http(s) request level access to a configured (or default)
 * CleanQuestionBanks implementation to handle the request and respond back.
 * 
 * @author Trevor Hinson
 */

public class QuestionBankCleaningRequestHandler extends AbstractRequestHandler {

	private static final long serialVersionUID = -6541900913618683491L;

	private static String CLEANER = "CleanQuestionBanksImplementation";

	private static String CLEARANCE_RESPONSE = "ClearanceResponse";

	private static String QUESTION_BANKS = "questionBanks";

	private static String TEST_BANKS = "testBanks";

	private static String START = "start";

	private static String NEW = "new";

	private static String COMMA = ",";

	private CleanQuestionBanks cleanQuestionBanks;

	private ClearanceResponseRenderer clearanceResponseRenderer
		= new ClearanceResponseRenderer();

	/**
	 * Returns the composite cleanQuestionBanks implementation. The
	 * implementation itself can be configured. If there is no configuration, ie 
	 * getConfig returns NULL
	 * then the default implementation is used. NOTE - this implementation
	 * is based on a "per request" - is is not geared to be multi-threaded.
	 * 
	 * @param associates
	 * @return
	 * @throws UtilityException
	 * @author Trevor Hinson
	 */
	protected CleanQuestionBanks getCleanQuestionBanks(
			RequestAssociates associates) throws UtilityException {
		if (null == cleanQuestionBanks) {
			if (null != associates) {
				String className = associates.getConfig(CLEANER);
				getLog().logDebug(
					"Trying to class load implementaiton : " + className);
				cleanQuestionBanks = retrieveConfiguredCleaner(className);
			}
		}
		return cleanQuestionBanks;
	}

	/**
	 * Tries to dynamically classload the className argument and IF this turns
	 * out to be a valid assignable class then it is returned. Otherwise the
	 * default implementation is returned.
	 * 
	 * @param className
	 * @return
	 * @throws UtilityException
	 * @author Trevor Hinson
	 */
	protected CleanQuestionBanks retrieveConfiguredCleaner(String className)
		throws UtilityException {
		CleanQuestionBanks cqb = new QuestionBankCleaner();
		if (Strings.isNotEmpty(className)) {
			try {
				Class<?> cla = Class.forName(className);
				if (null != cla) {
					try {
						Object obj = cla.newInstance();
						if (null != obj ? CleanQuestionBanks.class
								.isAssignableFrom(obj.getClass()) : false) {
							cqb = (CleanQuestionBanks) obj;
						}
					} catch (InstantiationException x) {
						throw new UtilityException(x);
					} catch (IllegalAccessException x) {
						throw new UtilityException(x);
					}
				}
			} catch (ClassNotFoundException x) {
				throw new UtilityException(x);
			}
		}
		return cqb;
	}

	public RequestResponse handle(HttpServletRequest request,
		HttpServletResponse response, RequestAssociates associates)
		throws UtilityException {
		RequestResponse rr = super.handle(request, response, associates);
		checkForReset(associates, request);
		ClearanceResponse cr = pickUpClearanceResponse(request);
		boolean hasSelected = hasSelectedQuestionsForRemoval(associates);
		if (null != cr) {
			if (hasSelected) {
				handleClearing(rr, associates, request, cr);
			} else {
				try {
					RenderedCleaningResult rcr = clearanceResponseRenderer
						.renderIdentifiedQuestions(cr, associates);
					if (null != rcr) {
						rr.append(rcr.toString());
					}
				} catch (CleaningException x) {
					throw new UtilityException(x);
				}
			}
		} else {
			identifySuperflousQuestions(associates, request, rr);
		}
		return rr;
	}
	
	/**
	 * Checks to see if there is a parameter within the request that is
	 *  instructing that the QuestionBankCleaner reevaluate.
	 * 
	 * @param ra
	 * @param request
	 * @author Trevor Hinson
	 */
	protected void checkForReset(RequestAssociates ra,
		HttpServletRequest request) {
		if (null != ra ? null != ra.getRequestParameters() : false) {
			Map<String, String> params = ra.getRequestParameters();
			if (null != params ? params.size() > 0 : false) {
				x : for (String key : params.keySet()) {
					String value = params.get(key);
					if (START.equalsIgnoreCase(key) && NEW.equalsIgnoreCase(value)) {
						request.getSession().setAttribute(CLEARANCE_RESPONSE, null);
						break x;
					}
				}
			}
		}
	}

	static Integer identifyPagingNumber(RequestAssociates ra) {
		Integer number = 1;
		if (null != ra ? null != ra.getRequestParameters() : false) {
			String s = ra.getRequestParameters().get(ClearanceEnums.page.toString());
			if (Strings.isNotEmpty(s)) {
				try {
					number = new Integer(s);
				} catch (NumberFormatException x) {
					// log ...
				}
			}
		}
		return number;		
	}

	/**
	 * Delegates to clear the users selected questions and then tries to render
	 * any results from that process in order to report them back.
	 * 
	 * @param rr
	 * @param request
	 * @param cqb
	 * @param cr
	 * @throws UtilityException
	 * @author Trevor Hinson
	 */
	protected void handleClearing(RequestResponse rr,
		RequestAssociates associates, HttpServletRequest request,
		ClearanceResponse cr) throws UtilityException {
		try {
			CleanQuestionBanks cqb = getCleanQuestionBanks(associates);
			Map<String, String> params = associates.getRequestParameters();
			cr = cqb.clearSelected(cr, params);
			RenderedCleaningResult rcr = clearanceResponseRenderer
				.renderRemovalResponse(cr, associates, params);
			if (null != rcr) {
				rr.append(rcr.toString());
			}
			request.getSession().setAttribute(CLEARANCE_RESPONSE, null);
		} catch (CleaningException x) {
			throw new UtilityException(x);
		}
	}

	/**
	 * Checks to see if the user has selected any questions for removal.
	 * 
	 * @param ra
	 * @return
	 * @author Trevor Hinson
	 */
	protected boolean hasSelectedQuestionsForRemoval(RequestAssociates ra) {
		boolean has = false;
		if (null != ra ? null != ra.getRequestParameters() : false) {
			Map<String, String> params = ra.getRequestParameters();
			if (null != params ? params.size() > 0 : false) {
				x : for (String str : params.keySet()) {
					if (Strings.isNotEmpty(str)) {
						if (str.startsWith(
							ClearanceResponseRenderer.QUESTION_FORM_NAME_PREFIX)) {
							has = true;
							break x;
						}
					}
				}
			}
		}
		return has;
	}

	/**
	 * Delegates the request to the composite CleanQuestionBanks and then calls
	 * through to render the response from there so that the user may select
	 * which to remove.
	 * 
	 * @param associates
	 * @param request
	 * @param rr
	 * @throws UtilityException
	 * @author Trevor Hinson
	 */
	protected void identifySuperflousQuestions(RequestAssociates associates,
		HttpServletRequest request, RequestResponse rr)
		throws UtilityException {
		try {
			CleanQuestionBanks cqb = getCleanQuestionBanks(associates);
			ClearanceResponse cr = cqb.identify(
				identifyLocations(associates), associates);
			RenderedCleaningResult rcr = clearanceResponseRenderer
					.renderIdentifiedQuestions(cr, associates);
			if (null != rcr) {
				rr.append(rcr.toString());
			}
			request.getSession().setAttribute(CLEARANCE_RESPONSE, cr);
		} catch (CleaningException x) {
			throw new UtilityException(x);
		}
	}

	/**
	 * Picks up the locations configured for the actual question banks and then
	 * returns these.
	 * 
	 * @param associates
	 * @return
	 * @throws UtilityException
	 * @author Trevor Hinson
	 */
	protected QuestionAndTestBankLocations identifyLocations(
		RequestAssociates associates) throws UtilityException {
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
	 * @throws UtilityException
	 * @author Trevor Hinson
	 */
	protected List<String> retrieveXBank(RequestAssociates associates,
		String key) throws UtilityException {
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
	 * Tries to retrieve the ClearanceResponse from the users HttpSession. This
	 * should be available there IF there has already been a successful
	 * identification of superfluous questions.
	 * 
	 * @param request
	 * @return
	 * @author Trevor Hinson
	 */
	protected ClearanceResponse pickUpClearanceResponse(
		HttpServletRequest request) {
		ClearanceResponse cr = null;
		if (null != request) {
			Object obj = request.getSession().getAttribute(CLEARANCE_RESPONSE);
			if (null != obj ? obj instanceof ClearanceResponse : false) {
				cr = (ClearanceResponse) obj;
			}
		}
		return cr;
	}

}
