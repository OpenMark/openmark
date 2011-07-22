package om.administration.questionbank;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import om.RequestAssociates;
import om.RequestHandlingException;

import org.apache.commons.lang.StringUtils;

import util.misc.IO;
import util.xml.XML;

/**
 * Responsible for rendering the output from the ClearanceResponses.  This
 *  implementation uses a configured xhtml to then place details of the response
 *  into.  OM does not use technologies such as JSP/JSF etc at this time and
 *  this keeps in touch with that rather than bring in additional technology.
 * @author Trevor Hinson
 */

public class ClearanceResponseRenderer implements Serializable {

	private static final long serialVersionUID = 4848269637617006644L;

	public static String QUESTION_FORM_NAME_PREFIX = "superfluous_";

	private static String INPUT_FIELD = "<input type=\"{0}\" name=\"{1}\" value=\"{2}\" />";

	private static String FORM = "<form method=\"post\" action=\"{0}\" id=\"clearance\">";

	private static String CLEARANCE_RESPONSE_MESSAGE = "ClearanceResponseMessage";

	private static String DEFAULT_FORM_ACTION = "/om-admin/question-clearance";

	private static Integer DEFAULT_NUMBER_PER_PAGE = 10;

	private static String SUBMIT = "submit";

	private static String CHECK_BOX = "checkbox";

	private static String RESET = "reset";

	private static String TEMPLATE = "template";

	private static String UL = "<ul>";

	private static String UL_CLOSE = "</ul>";

	private static String LI = "<li>";

	private static String LI_CLOSE = "</li>";

	private static String B = "<b>";

	private static String B_CLOSE = "</b>";

	private static String BR = "<br />";

	private static String H3 = "<h3>";

	private static String H3_CLOSE = "</h3>";

	private static String CLOSE_FORM = "</form>";

	/**
	 * Given the ClearanceResponse argument here such things are the
	 *  ClearanceResponse.getOutOfSyncTests(),
	 *  ClearanceResponse.getSuperfluousQuestions(), 
	 *  ClearanceResponse.getInvalidTests()
	 *  are combined with the configured template and returned for the user to
	 *  choose what to do next.
	 * 
	 * @param cr
	 * @param associates
	 * @return
	 * @throws CleaningException
	 * @author Trevor Hinson
	 */
	RenderedCleaningResult renderIdentifiedQuestions(ClearanceResponse cr,
		RequestAssociates associates) throws CleaningException {
		RenderedCleaningResult rcr = new RenderedCleaningResult();
		if (null != cr) {
			try {
				rcr = new RenderedCleaningResult();
				StringBuffer sb = new StringBuffer()
					.append(renderFormStart(associates));
				sb.append(renderSuperfluousQuestions(associates,
					cr.getSuperfluousQuestions()));
				sb.append(BR).append(BR).append(B).append("Analyse Fresh : ")
					.append(B_CLOSE).append(renderResetCheckBox()).append(BR)
					.append(BR);
				sb.append(renderOutOfSyncTests(cr.getOutOfSyncTests()));
				sb.append(renderBrokenTests(cr.getBrokenTests()))
					.append(renderSubmitButton()).append(CLOSE_FORM);
				rcr.append(mergeTemplate(sb, associates));
			} catch (RequestHandlingException x) {
				throw new CleaningException(x);
			}
		}
		return rcr;
	}

	/**
	 * Returns a rendered version of an XHTML submit button.
	 * 
	 * @return
	 * @author Trevor Hinson
	 */
	protected StringBuffer renderSubmitButton() {
		InputFieldElements ife = new InputFieldElements();
		ife.setEachAs(SUBMIT);
		return renderInputField(ife);
	}

	protected StringBuffer renderSubmitButton(String value) {
		InputFieldElements ife = new InputFieldElements();
		ife.setEachAs(SUBMIT);
		ife.value = value;
		return renderInputField(ife);
	}

	protected StringBuffer renderResetCheckBox() {
		InputFieldElements ife = new InputFieldElements();
		ife.name = "start";
		ife.value = "new";
		ife.type = CHECK_BOX;
		return renderInputField(ife);
	}

	/**
	 * Iterates over the invalid tests and renders each so that they can be
	 *  highlighted to the user.
	 * 
	 * @param brokenTests
	 * @return
	 * @author Trevor Hinson
	 */
	protected StringBuffer renderBrokenTests(
		List<BrokenTestQuestionReferences> brokenTests) {
		StringBuffer sb = new StringBuffer();
		if (null != brokenTests ? brokenTests.size() > 0 : false) {
			sb.append(H3).append("Tests that are \"broken\" because they reference")
				.append(" Questions that do not exist in ALL of the question banks")
				.append(H3_CLOSE).append(UL);
			for (BrokenTestQuestionReferences ref : brokenTests) {
				sb.append(LI).append(B).append(ref.getTestName()).append(B_CLOSE)
					.append(BR);
				for (String ques : ref.getQuestionsFoundIn().keySet()) {
					Set<String> locations = ref.getQuestionsFoundIn().get(ques);
					if (null != locations ? locations.size() > 0 : false) {
						sb.append(UL).append(BR).append(LI)
							.append(B).append(ques).append(B_CLOSE)
							.append(" is the latest version for the question referenced in : ")
							.append(BR).append(renderLocations(ref.getFullTestLocationPaths()))
							.append(BR)
							.append("<b>However</b> the question itself is only found in : ")
							.append(BR);
						for (String loc : locations) {
							sb.append(loc).append(BR);
						}
						sb.append(LI_CLOSE).append(UL_CLOSE).append(BR).append(BR);
					} else {
						sb.append("Holds reference to questions that can not be found.");
					}
				}
				sb.append(LI_CLOSE);
			}
			sb.append(UL_CLOSE);
		}
		return sb;
	}

	/**
	 * Simple data holder for the details needed in an input field.
	 * 
	 * @author Trevor Hinson
	 */
	class InputFieldElements {
		
		String type;
		
		String name;
		
		String value;
		
		void setEachAs(String s) {
			type = s;
			name = s;
			value = s;
		}

	}

	/**
	 * Provides a simple wrapping around a MessageFormat of the composite static
	 *  INPUT_FIELDS which is substituted with the values from the
	 *  InputFieldElements argument.
	 * 
	 * @param ife
	 * @return
	 * @author Trevor Hinson
	 */
	protected StringBuffer renderInputField(InputFieldElements ife) {
		StringBuffer sb = new StringBuffer();
		if (null != ife) {
			Object[] args = {ife.type, ife.name, ife.value};
			String replaced = MessageFormat.format(INPUT_FIELD, args);
			sb.append(replaced);
		}
		return sb;
	}

	/**
	 * Renders out the contents of the locations argument with a new line for
	 *  each item also.
	 * 
	 * @param locations
	 * @return
	 * @author Trevor Hinson
	 */
	protected StringBuffer renderLocations(List<String> locations) {
		StringBuffer sb = new StringBuffer();
		if (null != locations ? locations.size() > 0 : false) {
			for (Iterator<String> i = locations.iterator(); i.hasNext();) {
				String loc = i.next();
				if (StringUtils.isNotEmpty(loc)) {
					sb.append(" - ").append(loc);
					if (i.hasNext()) {
						sb.append(BR);
					}
				}
			}			
		}
		return sb;
	}

	/**
	 * Merges the rendered response with the configured template.
	 * 
	 * @param sb
	 * @param associates
	 * @return
	 * @throws CleaningException
	 * @author Trevor Hinson
	 */
	protected String mergeTemplate(StringBuffer sb,
		RequestAssociates associates) throws CleaningException {
		Map<String, String> replacements = getReplacements(sb);
		String template = retrieveTemplateLocation(associates);
		return applyToTemplate(replacements, template);
	}

	/**
	 * Iterates over each of the TestSynchronizationCheck and renders them into
	 *  an xhtml consumable output.
	 * 
	 * @param test
	 * @return
	 * @author Trevor Hinson
	 */
	protected StringBuffer renderOutOfSyncTests(List<TestSynchronizationCheck> tests) {
		StringBuffer sb = new StringBuffer();
		if (null != tests ? tests.size() > 0 : false) {
			sb.append(H3).append("Out of Sync Tests.").append(H3_CLOSE).append(UL);
			for (TestSynchronizationCheck check : tests) {
				if (null != check) {
					sb.append(LI).append(renderIndividualTest(check))
						.append(LI_CLOSE);
				}
			}
			sb.append(UL_CLOSE);
		}
		return sb;
	}

	/**
	 * Renders a TestSynchronizationCheck into xhtml format along with the
	 *  questions that Test itself references.
	 * 
	 * @param check
	 * @return
	 * @author Trevor Hinson
	 */
	protected StringBuffer renderIndividualTest(TestSynchronizationCheck check) {
		StringBuffer sb = new StringBuffer();
		if (null != check) {
			sb.append(B).append(check.getName()).append(B_CLOSE).append(BR);
			sb.append(" Was only found in : ").append(BR);
			sb.append(UL);
			for (String qn : check.getLocationsTestIsFoundIn()) {
				sb.append(LI).append(qn).append(LI_CLOSE);
			}
			sb.append(UL_CLOSE);
		}
		return sb;
	}

	/**
	 * Iterates over the Superfluous questions and renders them into xhtml
	 *  format for display back to the user.
	 * 
	 * @param qs
	 * @param associates
	 * @return
	 * @author Trevor Hinson
	 */
	protected StringBuffer renderSuperfluousQuestions(RequestAssociates associates,
		Map<String, IdentifiedSuperfluousQuestion> qs) throws RequestHandlingException {
		StringBuffer sb = new StringBuffer();
		if (null != qs ? qs.size() > 0 : false) {
			sb.append(H3).append("Superfluous Questions").append(H3_CLOSE)
				.append(UL);
			Integer pageNumber = QuestionBankCleaningRequestHandler
				.identifyPagingNumber(associates);
			Integer numberPerPage = getNumberPerPage(associates);
			sb.append("Number of results = ").append(B).append(qs.size())
				.append(B_CLOSE).append(BR).append(BR);
			if (null != pageNumber ? pageNumber > -1 : false) {
				PagingDetails pd = new PagingDetails();
				pd.numberPerPage = numberPerPage;
				pd.pageNumber = pageNumber;
				pageResults(sb, pd, qs);
			} else {
				sb.append("No further results.");
			}
			sb.append(UL_CLOSE);
		}
		return sb;
	}

	public class PagingDetails {
		
		Integer pageNumber = 1;
		
		Integer numberPerPage = 10;
		
	}

	protected void pageResults(StringBuffer sb, PagingDetails pd,
		Map<String, IdentifiedSuperfluousQuestion> qs)
		throws RequestHandlingException {
		if (null != pd.pageNumber ? pd.pageNumber > 0 : false) {
			Integer numberOfPages = qs.size() / pd.numberPerPage;
			if ((numberOfPages % pd.numberPerPage) > 0) {
				numberOfPages = numberOfPages + 1;
			}
			if (pd.pageNumber -1 <= numberOfPages) {
				int count = 0;
				int num = (pd.pageNumber -1) * pd.numberPerPage;
				for (int i = num; count < pd.numberPerPage
					&& num + count <= qs.size() -1; i++, count++) {
					sb.append(renderIdentifiedQuestion(
						(IdentifiedSuperfluousQuestion) qs.values().toArray()[i]));
				}
			} else {
				// ...
			}
			renderPreviousNext(sb, pd, qs.size());
		}
	}

	protected void renderPreviousNext(StringBuffer sb, PagingDetails pd,
		Integer numberOfResults) {
		if (null != sb) {
			sb.append(BR).append(BR);
			if (pd.pageNumber > 1) {
				sb.append("<a href=\"?page=").append(pd.pageNumber -1)
					.append("\">[ Previous ]</a> (got to ")
					.append(pd.pageNumber - 1).append(")");
			}
			int n = numberOfResults / pd.numberPerPage;
			if (pd.pageNumber < n + 1) {
				sb.append(" - <a href=\"?page=").append(pd.pageNumber + 1)
					.append("\">[ Next ]</a> (go to page ")
					.append(pd.pageNumber + 1).append(")");
			}
		}
	}

	protected void renderSuperfluousQuestions(StringBuffer sb,
		Map<String, IdentifiedSuperfluousQuestion> qs) {
		for (IdentifiedSuperfluousQuestion q : qs.values()) {
			if (null != q) {
				sb.append(renderIdentifiedQuestion(q));					
			}
		}
	}

	/**
	 * Picks up the configured value for the number per page setting.  If not
	 *  set then we return the DEFAULT_NUMBER_PER_PAGE
	 * 
	 * @param ra
	 * @return
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	Integer getNumberPerPage(RequestAssociates ra)
		throws RequestHandlingException {
		Integer number = DEFAULT_NUMBER_PER_PAGE;
		if (null != ra) {
			String s = ra.getConfig(ClearanceEnums.resultsPerPage.toString());
			try {
				Integer n = new Integer(s);
				if (null != n ? n > 0 : false) {
					number = n;
				}
			} catch (NumberFormatException x) {
				throw new RequestHandlingException(x);
			}
		}
		return number;
	}

	/**
	 * Renders to a String the initial <form element ...
	 * 
	 * @param associates
	 * @return
	 * @exception 
	 * @author Trevor Hinson
	 */
	protected String renderFormStart(RequestAssociates associates)
		throws RequestHandlingException {
		String action = DEFAULT_FORM_ACTION;
		if (null != associates) {
			String value = associates.getConfig(ClearanceEnums.postToUrl.toString());
			if (StringUtils.isNotEmpty(value)) {
				action = value;
			}
		}
		return MessageFormat.format(FORM, new Object[]{action});
	}

	/**
	 * Based on the information held within the ClearanceResponse argument this
	 *  method identifies any problems and merges those with the configured
	 *  template to inform the user of what happened.
	 * 
	 * @param cr
	 * @param associates
	 * @param selected
	 * @return
	 * @throws CleaningException
	 * @author Trevor Hinson
	 */
	RenderedCleaningResult renderRemovalResponse(ClearanceResponse cr,
		RequestAssociates associates, Map<String, String> params)
		throws CleaningException {
		return renderResponse(cr, associates, params,
			null != cr ? cr.getProblemRemoving() : null, ClearanceEnums.clean);
	}

	/**
	 * Applies the merged template to a new RenderedCleaningResult and returns
	 *  it. 
	 * 
	 * @param cr
	 * @param associates
	 * @param params
	 * @param issues
	 * @param enu
	 * @return
	 * @throws CleaningException
	 * @author Trevor Hinson
	 */
	RenderedCleaningResult renderResponse(ClearanceResponse cr,
		RequestAssociates associates, Map<String, String> params,
		Map<IdentifiedSuperfluousQuestion, ?> issues, ClearanceEnums enu)
		throws CleaningException {
		RenderedCleaningResult rcr = null;
		if (null != cr) {
			rcr = new RenderedCleaningResult();
			StringBuffer sb = renderIssueDetails(params, issues, enu, associates);
			rcr.append(mergeTemplate(sb, associates));
		}
		return rcr;
	}

	/**
	 * Adds the argument to a new Map<String, String> with the key of
	 *  CLEARANCE_RESPONSE_MESSAGE so that this can be merged with the template.
	 * 
	 * @param sb
	 * @return
	 * @author Trevor Hinson
	 */
	protected Map<String, String> getReplacements(StringBuffer sb) {
		Map<String, String> replacements = new HashMap<String, String>();
		replacements.put(CLEARANCE_RESPONSE_MESSAGE, null != sb ? sb.toString()
			: "Unknown.  Please check the logs.");
		return replacements;
	}

	/**
	 * Iterates over the issues (if there are any) and adds them in a rendered
	 *  format to the StringBuffer response.
	 * 
	 * @param params
	 * @param problems
	 * @return
	 * @throws CleaningException
	 * @author Trevor Hinson
	 */
	protected StringBuffer renderIssueDetails(Map<String, String> params,
		Map<IdentifiedSuperfluousQuestion, ?> issues, ClearanceEnums enu,
		RequestAssociates ra) throws CleaningException {
		StringBuffer sb = new StringBuffer();
		if (null != issues ? issues.size() > 0 : false) {
			sb.append(B).append("Issues ...").append(B_CLOSE).append(BR)
				.append(UL);
			for (IdentifiedSuperfluousQuestion q : issues.keySet()) {
				Object value = issues.get(q);
				if (null != value ? value instanceof RemovalIssueDetails : false) {
					RemovalIssueDetails rid = (RemovalIssueDetails) value;
					sb.append(renderIdentifiedQuestion(q));
					sb.append("Issue reported : ").append(rid.getSummary());
				}
			}
			sb.append(UL_CLOSE);
		}
		return appendSelected(params, sb, issues.keySet(), ra, enu);
	}

	/**
	 * Renders the given IdentifiedSuperfluousQuestion into a xhtml consumable
	 *  format.
	 * 
	 * @param q
	 * @return
	 * @author Trevor Hinson
	 */
	protected StringBuffer renderIdentifiedQuestion(
		IdentifiedSuperfluousQuestion q) {
		StringBuffer sb = new StringBuffer();
		if (null != q) {
			sb.append(LI).append(B).append(q.getName()).append(B_CLOSE)
				.append(BR).append("Located in : ").append(BR);
			Iterator<String> locs = q.getLocations();
			while (locs.hasNext()) {
				String s = locs.next();
				InputFieldElements ife = new InputFieldElements();
				ife.type = CHECK_BOX;
				ife.value = s + consistentPathSeperatorForDisplay(s) + q.getName();
				ife.name = QUESTION_FORM_NAME_PREFIX + q.getName();
				sb.append(" - [").append(s).append("] ")
					.append(renderInputField(ife)).append(BR);
			}
			sb.append(LI_CLOSE);
		}
		return sb;
	}

	/**
	 * As the original question-bank and test-bank locations are configured here
	 *  we try to remain consistent with that has been applied in there.
	 * 
	 * @param s
	 * @return
	 * @author Trevor Hinson
	 */
	public static String consistentPathSeperatorForDisplay(String s) {
		return StringUtils.isNotEmpty(s) ? s.contains("/") ? "/" : "\\" : File.separator;
	}

	/**
	 * Iterates over the selected Questions and if they are not within the
	 *  Set argument then they are reported to have been removed successfully.
	 * 
	 * @param params
	 * @param sb
	 * @param isq
	 * @return
	 * @throws CleaningException
	 * @author Trevor Hinson
	 */
	protected StringBuffer appendSelected(Map<String, String> params, StringBuffer sb,
		Set<IdentifiedSuperfluousQuestion> isq, RequestAssociates ra,
		ClearanceEnums enu) throws CleaningException {
		if (null != params && null != sb && null != isq) {
			sb.append(BR).append(BR).append(B)
				.append("The following questions were successfully removed :")
				.append(B_CLOSE);
			if (params.size() > 0) {
				sb.append(BR).append(UL);
				for (String sel : params.values()) {
					if (StringUtils.isNotEmpty(sel) ? !isIn(isq, sel) : false) {
						if (!SUBMIT.equalsIgnoreCase(sel) && !RESET.equalsIgnoreCase(sel)) {
							sb.append(LI);
							List<String> qu = QuestionBankCleaner.getQuestions(sel);
							if (null != qu ? qu.size() > 0 : false) {
								for (String s : qu) {
									if (StringUtils.isNotEmpty(s)) {
										sb.append(s).append(BR);
									}
								}
							} else {
								sb.append(sel).append(BR);
							}
							sb.append(LI_CLOSE).append(BR).append(BR);
						}
					}
				}
				sb.append(UL_CLOSE);
			}
		}
		return sb;
	}

	/**
	 * Checks to see if the name argument is the name of one of the
	 *  {@link IdentifiedSuperfluousQuestion} and returns true is it is the same
	 * 
	 * @param col
	 * @param name
	 * @return
	 * @author Trevor Hinson
	 */
	protected boolean isIn(Set<IdentifiedSuperfluousQuestion> col, String name) {
		boolean is = false;
		if (null != col && StringUtils.isNotEmpty(name)) {
			for (IdentifiedSuperfluousQuestion q : col) {
				if (null != q ? StringUtils.isNotEmpty(q.getName()) : false) {
					if (name.equals(q.getName())) {
						is = true;
					}
				}
			}
		}
		return is;
	}

	/**
	 * Takes the replacements argument and then looks to replace the tokens
	 *  within the template and returns it as a String.
	 * 
	 * @param replacements
	 * @return
	 * @author Trevor Hinson
	 */
	protected String applyToTemplate(Map<String, String> replacements,
		String template) throws CleaningException {
		String output = "??";
		try {
			output = IO.loadString(new FileInputStream(template));
		} catch (IOException x) {
			throw new CleaningException(x);
		}
		return XML.replaceTokens(output, "%%", replacements);
	}

	/**
	 * Returns the template configuration value so that it may be used to pick
	 *  up the actual xhtml template for rendering.
	 * 
	 * @param ra
	 * @return
	 * @exception
	 * @author Trevor Hinson
	 */
	protected String retrieveTemplateLocation(RequestAssociates ra)
		throws CleaningException {
		try {
			String s = ra.getConfig(TEMPLATE);
			String name = ra.getServletContext().getRealPath(s);
			return null != ra ? name : null;
		} catch (RequestHandlingException x) {
			throw new CleaningException(x);
		}
	}

}
