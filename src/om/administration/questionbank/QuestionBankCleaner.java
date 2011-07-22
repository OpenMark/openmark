package om.administration.questionbank;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import om.RequestAssociates;
import om.RequestHandlingException;
import om.RequestParameterNames;
import om.tnavigator.db.DatabaseAccess;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.misc.GeneralUtils;
import util.misc.QuestionName;
import util.misc.StandardFileFilter;
import util.misc.VersionUtil;
import util.xml.XML;

/**
 * This implementation of the CleanQuestionBanks is particuarly memory hungry
 * and there are a few places with higher performance questions. This is due to
 * what has to happen and a reluctance on heavy IO otherwise. <br />
 * <br />
 * Note that this is implemented with a single thread approach.
 * 
 * @author Trevor Hinson
 */

public class QuestionBankCleaner implements CleanQuestionBanks {

	private static String DIRECTORY_DATE_FORMAT = "yyyyMMdd";

	private static String TEST = "test";

	private static String CONTENT = "content";

	private static String QUESTION = "question";

	private static String SECTION = "section";

	private static String ID = "id";

	private static String DOT_TEST_DOT_XML = ".test.xml";

	private FileFilter fileFilter = new StandardFileFilter(DOT_TEST_DOT_XML);

	private FileFilter questionFilter = new StandardFileFilter(VersionUtil.DOT_JAR);

	public FileFilter getQuestionFilter() {
		return questionFilter;
	}

	public void setQuestionFilter(FileFilter qff) {
		questionFilter = qff;
	}

	public void setFileFilter(FileFilter f) {
		fileFilter = f;
	}

	public FileFilter getFileFilter() {
		return fileFilter;
	}

	@Override
	public ClearanceResponse identify(QuestionAndTestBankLocations qbl,
		RequestAssociates ra) throws CleaningException {
		ClearanceResponse cr = new ClearanceResponse();
		if (null != qbl ? null != qbl.getQuestionBanks() && null != ra
			&& null != qbl.getTestBanks() : false) {
			Banks banks = new Banks();
			banks.numberOfQuestionBanks = qbl.getQuestionBanks().size();
			banks.numberOfTestBanks = qbl.getTestBanks().size();
			if (banks.numberOfQuestionBanks > 0 && banks.numberOfTestBanks > 0) {
				try {
					Map<String, TestSynchronizationCheck> tests = identifyAllTests(
							qbl.getTestBanks(), cr);
					AllQuestionsPool allQuestionsPool = identifyAllQuestions(qbl
							.getQuestionBanks());
					if (null != tests ? tests.size() > 0 : false) {
						TestQuestionsReferencedPool pool = new TestQuestionsReferencedPool();
						for (TestSynchronizationCheck testCheck : tests
								.values()) {
							addTestReferencedQuestions(testCheck, pool);
							if (!identifySyncStatus(testCheck, banks)) {
								cr.addOutOfSyncTest(testCheck);
							}
							determineBrokenTests(testCheck, allQuestionsPool,
									cr, banks);
						}
						identifySuperflousQuestions(pool, allQuestionsPool, cr,
								ra);
					}
				} catch (IOException x) {
					throw new CleaningException(x);
				}
			}
		}
		return cr;
	}

	/**
	 * A broken test is one that references a Question and that the latest
	 * version of that question is not present within one or many of the
	 * relevant question-banks. This would cause a test to not work correctly.
	 * 
	 * @param testCheck
	 * @param questionPool
	 * @param cr
	 * @param banks
	 * @author Trevor Hinson
	 */
	protected void determineBrokenTests(TestSynchronizationCheck testCheck,
		AllQuestionsPool questionPool, ClearanceResponse cr, Banks banks) {
		if (null != testCheck && null != questionPool && null != cr
			&& null != banks) {
			Map<String, BrokenTestQuestionReferences> btqr = new HashMap<String, BrokenTestQuestionReferences>();
			if (null != testCheck.getTestDetails()) {
				for (String location : testCheck.getTestDetails().keySet()) {
					TestDetails td = testCheck.getTestDetails().get(location);
					if (null != td ? null != td.getTestDefinition() : false) {
						TestQuestionsReferenced tqr = td
								.getQuestionsReferenced();
						if (null != tqr ? null != tqr.getNamesOfQuestions()
								: false) {
							for (String questionName : tqr
									.getNamesOfQuestions()) {
								maintainBrokenTest(questionName, td,
										questionPool, banks, testCheck, btqr);
							}
						}
					}
				}
			}
			if (btqr.size() > 0) {
				Set<BrokenTestQuestionReferences> broRef = new HashSet<BrokenTestQuestionReferences>();
				broRef.addAll(btqr.values());
				cr.addBrokenTest(broRef);
			}
		}
	}

	/**
	 * Looks to see if the questionName is found within the questionPool and if
	 * found then tries to create a BrokenTestQuestionReference and place it
	 * within the btqr Collection.
	 * 
	 * @param questionName
	 * @param td
	 * @param questionPool
	 * @param banks
	 * @param testCheck
	 * @param btqr
	 * @author Trevor Hinson
	 */
	protected void maintainBrokenTest(String questionName, TestDetails td,
		AllQuestionsPool questionPool, Banks banks,
		TestSynchronizationCheck testCheck,
		Map<String, BrokenTestQuestionReferences> btqr) {
		if (StringUtils.isNotEmpty(questionName)) {
			QuestionPoolDetails qpd = questionPool.getDetails(questionName);
			if (null != qpd) {
				Map<String, Set<String>> verNum = qpd
					.getQuestionsWithVersionNumbering();
				String latestVersion = qpd.identifyLatestVersion();
				if (null != verNum && StringUtils.isNotEmpty(latestVersion)) {
					Set<String> set = verNum.get(latestVersion);
					if (null != set) {
						if (set.size() != banks.numberOfQuestionBanks) {
							BrokenTestQuestionReferences ref = btqr
									.get(questionName);
							if (null == ref) {
								ref = new BrokenTestQuestionReferences(
										testCheck);
								ref.setQuestionName(questionName);
								btqr.put(questionName, ref);
							}
							ref.addFullTestLocationPaths(td.getTestDefinition()
									.getAbsolutePath());
							ref.add(latestVersion, set);
						}
					}
				}
			}
		}
	}

	/**
	 * Simplified means of keeping reference to the number of Question and Test
	 * banks that are to be analysed.
	 * 
	 * @author Trevor Hinson
	 */
	protected class Banks {

		int numberOfQuestionBanks = 0;

		int numberOfTestBanks = 0;

	}

	/**
	 * Here we add into a pool the questions that are referenced from the Test
	 * so we can easily identify them later.
	 * 
	 * @param test
	 * @param pool
	 * @author Trevor Hinson
	 */
	private void addTestReferencedQuestions(TestSynchronizationCheck test,
		TestQuestionsReferencedPool pool) {
		if (null != test && null != pool) {
			pool.add(test.getNamesOfTestQuestionsReferenced());
		}
	}

	/**
	 * Looks through all of the questions to identify if each question is
	 * references by a test. If not at all then we take that to mean the
	 * question is superflous to the system now (probably an old version).
	 * Therefore it is subject to being discarded.
	 * 
	 * @param pool
	 * @param allQuestionsFound
	 *            All questions found within the questionbanks.
	 * @param cr
	 * @param ra
	 * @exception
	 * @author Trevor Hinson
	 */
	protected void identifySuperflousQuestions(TestQuestionsReferencedPool trq,
		AllQuestionsPool qp, ClearanceResponse cr, RequestAssociates ra)
		throws CleaningException {
		if ((null != trq ? null != trq.getReferencedNames() : false)
				&& null != qp && null != cr) {
			Iterator<String> referencedQuestionNames = trq.getReferencedNames();
			while (referencedQuestionNames.hasNext()) {
				String referencedQuestionName = referencedQuestionNames.next();
				if (StringUtils.isNotEmpty(referencedQuestionName)) {
					identifySuperflousQuestionsFromTest(qp,
							referencedQuestionName, cr, trq, ra);
				}
			}
			applyRemainingNonReferencedQuestions(qp, cr, ra);
		}
	}

	/**
	 * Based on the referencedQuestionName argument this method picks up the
	 * associated QuestionPoolDetails and then identifies all those questions of
	 * which are within the QuestionPoolDetails that are NOT the latest version
	 * of the Question itself and then classifies them as superflous and
	 * therefore a candidate for removal.
	 * 
	 * @param qp
	 * @param referencedQuestionName
	 * @param cr
	 * @exception
	 * @author Trevor Hinson
	 */
	protected void identifySuperflousQuestionsFromTest(AllQuestionsPool qp,
		String testReferencedQuestionName, ClearanceResponse cr,
		TestQuestionsReferencedPool trq, RequestAssociates ra)
		throws CleaningException {
		if (null != qp && StringUtils.isNotEmpty(testReferencedQuestionName)
			&& null != cr && null != trq) {
			QuestionPoolDetails details = qp
				.getDetails(testReferencedQuestionName);
			if (null != details) {
				String latest = details.identifyLatestVersion();
				if (null != details.getQuestionsWithVersionNumbering()
					&& StringUtils.isNotEmpty(latest)) {
					DatabaseAccess da = getDatabaseAccess(ra);
					for (String fullName : details
							.getQuestionsWithVersionNumbering().keySet()) {
						if (StringUtils.isNotEmpty(fullName)) {
							if (!fullName.equals(latest)) {
								if (!isOpenForAnyStudent(generateQuery(ra,
										fullName), da)) {
									Set<String> locations = details
										.getQuestionsWithVersionNumbering()
										.get(fullName);
									cr.addSuperfluousQuestion(new IdentifiedSuperfluousQuestion(
										fullName, locations));
								}
							}
						}
					}
				}
			}
			qp.removeDetails(testReferencedQuestionName);
		}
	}

	/**
	 * Picks up the DatabaseAccess object from the RequestAssociates argument
	 *  and returns this.
	 * 
	 * @param ra
	 * @return
	 * @author Trevor Hinson
	 */
	protected DatabaseAccess getDatabaseAccess(RequestAssociates ra) {
		DatabaseAccess da = null;
		if (null != ra) {
			Object obj = ra.getPrincipleObjects().get(
				RequestParameterNames.DatabaseAccess.toString());
			if (null != obj ? obj instanceof DatabaseAccess : false) {
				da = (DatabaseAccess) obj;
			}
		}
		return da;
	}

	/**
	 * Checks the underlying persistence mechanism to ensure that there is not a
	 * version of the given question still open within the database. IF there is
	 * a version open then the method returns true and the question itself is
	 * therefore still required for that student and therefore can not be
	 * classified as superfluous as a result.
	 * 
	 * Defaults to true in order to be safe as those that are set to open will
	 * not appear within the superfluous listing.
	 * 
	 * @param fullName
	 * @param da
	 * @return
	 * @exception
	 * @author Trevor Hinson
	 */
	protected boolean isOpenForAnyStudent(String query, DatabaseAccess da)
		throws CleaningException {
		boolean isOpen = true;
		if (StringUtils.isNotEmpty(query) && null != da) {
			DatabaseAccess.Transaction dat = null;
			try {
				dat = da.newTransaction();
				ResultSet rs = dat.query(query);
				if (null != rs) {
					if (rs.next()) {
						isOpen = null == rs.getString("question") ? false : true;
					} else {
						isOpen = false;
					}
				}
			} catch (Exception x) {
				throw new CleaningException(
						"Error running the database query : " + x.getMessage(),
						x);
			} finally {
				if (dat != null) {
					dat.finish();
				}
			}
		}
		return isOpen;
	}

	/**
	 * After the identification of the non referenced questions then the rest of
	 * the questions within the AllQuestionsPool become superfluous by
	 * definition.
	 * 
	 * @param qp
	 * @param cr
	 * @param ra
	 * @exception
	 * @author Trevor Hinson
	 */
	protected void applyRemainingNonReferencedQuestions(AllQuestionsPool qp,
		ClearanceResponse cr, RequestAssociates ra)
		throws CleaningException {
		if (null != qp && null != cr) {
			for (String key : qp.getQuestionDetails().keySet()) {
				QuestionPoolDetails qpd = qp.getQuestionDetails().get(key);
				if (null != qpd ? null != qpd
						.getQuestionsWithVersionNumbering() : false) {
					DatabaseAccess da = getDatabaseAccess(ra);
					for (String fullName : qpd
							.getQuestionsWithVersionNumbering().keySet()) {
						if (!isOpenForAnyStudent(generateQuery(ra, fullName),
								da)) {
							Set<String> locations = qpd
								.getQuestionsWithVersionNumbering().get(fullName);
							cr.addSuperfluousQuestion(new IdentifiedSuperfluousQuestion(
								fullName, locations));
						}
					}
				}
			}
		}
	}

	/**
	 * Based on the configured query this is substituted with the
	 * questionFullName argument and returned.
	 * 
	 * @param ra
	 * @param questionFullName
	 * @exception 
	 * @return
	 * @author Trevor Hinson
	 */
	protected String generateQuery(RequestAssociates ra, String questionFullName)
		throws CleaningException {
		String query = null;
		if (StringUtils.isNotEmpty(questionFullName) && null != ra) {
			try {
				String q = ra.getConfig(ClearanceEnums.query);
				if (StringUtils.isNotEmpty(q)) {
					QuestionName qn = VersionUtil.represented(questionFullName);
					if (null != qn ? qn.isValid() : false) {
						Object[] args = {"'" + qn.getPrefix() + "'",
							qn.getQuestionVersion().iMajor,
							qn.getQuestionVersion().iMinor};
						query = MessageFormat.format(q, args);
					}
				}
			} catch (RequestHandlingException x) {
				throw new CleaningException(x);
			}
		}
		return query;
	}

	/**
	 * Checks each of the TestDetails to make sure that they are composed of the
	 * same questions.
	 * 
	 * @param locations
	 * @params numberOfQuestionBanks
	 * @return
	 * @author Trevor Hinson
	 */
	protected boolean identifySyncStatus(TestSynchronizationCheck testCheck,
			Banks ba) {
		boolean in = false;
		if (null != testCheck) {
			if (testCheck.isFoundInAllTestBanks()
					&& testCheck.getNumberOfTestDetailsHeld() == ba.numberOfTestBanks) {
				in = testCheck.areAllReferencedQuestionsInSync();
			}
		}
		return in;
	}

	/**
	 * Identifies all the composite questions within the test xml and returns
	 * their names for checking against.
	 * 
	 * @param f
	 * @return
	 * @throws IOException
	 * @author Trevor Hinson
	 */
	protected List<String> retrieveCompositeQuestions(File f)
		throws IOException {
		List<String> composedQuestions = new ArrayList<String>();
		if (null != f ? f.canRead() : false) {
			Document dTest = XML.parse(f);
			if (null != dTest) {
				if (XML.hasChild(dTest, TEST)) {
					Node test = XML.getChild(dTest, TEST);
					if (XML.hasChild(test, CONTENT)) {
						Node content = XML.getChild(test, CONTENT);
						if (null != content) {
							Element[] es = XML.getChildren(content);
							if (null != es ? es.length > 0 : false) {
								for (int i = 0; i < es.length; i++) {
									handleQuestionsAndSections(es[i],
										composedQuestions);
								}
							}
						}
					}
				}
			}
		}
		return composedQuestions;
	}

	/**
	 * Either a QUESTION or a SECTION (which contains QUESTIONS) are examined
	 * and then the ID is extracted to the composedQuestions collection.
	 * 
	 * @param e
	 * @param composedQuestions
	 * @author Trevor Hinson
	 */
	protected void handleQuestionsAndSections(Element e,
		List<String> composedQuestions) {
		if (null != e && null != composedQuestions) {
			if (QUESTION == e.getNodeName()) {
				retrieveElementID(e, composedQuestions);
			} else if (SECTION == e.getNodeName()) {
				Element[] questions = XML.getChildren(e, QUESTION);
				if (null != questions ? questions.length > 0 : false) {
					for (int j = 0; j < questions.length; j++) {
						retrieveElementID(questions[j], composedQuestions);
					}
				}
			}
		}
	}

	/**
	 * Tries to extract the value of the ID attribute from the Element argument
	 * and places it within the composedQuestions collection.
	 * 
	 * @param e
	 * @param composedQuestions
	 * @author Trevor Hinson
	 */
	protected void retrieveElementID(Element e, List<String> composedQuestions) {
		if (null != e && null != composedQuestions) {
			String id = e.getAttribute(ID);
			if (StringUtils.isNotEmpty(id)) {
				composedQuestions.add(id);
			}
		}
	}

	/**
	 * Identifies all the questions from within the configured questionbanks so
	 * that they can be analysed against the tests.
	 * 
	 * @param qbl
	 * @return
	 * @throws CleaningException
	 * @author Trevor Hinson
	 */
	protected AllQuestionsPool identifyAllQuestions(List<String> questionBanks)
		throws CleaningException {
		AllQuestionsPool allQuestions = new AllQuestionsPool();
		if (null != questionBanks ? questionBanks.size() > 0 : false) {
			for (String location : questionBanks) {
				if (StringUtils.isNotEmpty(location)) {
					File dir = new File(location);
					if (dir.exists() ? dir.isDirectory() && dir.canRead()
							: false) {
						File[] files = dir.listFiles(getQuestionFilter());
						if (null != files ? files.length > 0 : false) {
							for (File f : files) {
								if (null != f) {
									String prefix = GeneralUtils
											.questionNamePrefix(f.getName());
									QuestionPoolDetails qpd = allQuestions
											.getDetails(prefix);
									if (null != qpd) {
										qpd.addTo(f.getName(), location);
									} else {
										qpd = new QuestionPoolDetails(prefix);
										qpd.addTo(f.getName(), location);
										allQuestions.addDetails(prefix, qpd);
									}
								}
							}
						}
					}
				}
			}
		}
		return allQuestions;
	}

	/**
	 * Iterates over each of the provided locations looking for the tests held.
	 * On finding a test we create a new TestSynchronizationCheck. If a test by
	 * the same name exists within the Map that is being built then we
	 * essentially tick the location setting for that TestSynchronizationCheck
	 * 
	 * @param locations
	 * @param cr
	 * @return
	 * @author Trevor Hinson
	 */
	protected Map<String, TestSynchronizationCheck> identifyAllTests(
		List<String> locations, ClearanceResponse cr) throws IOException {
		Map<String, TestSynchronizationCheck> found = new HashMap<String, TestSynchronizationCheck>();
		if (null != cr && (null != locations ? locations.size() > 0 : false)) {
			for (String testLocation : locations) {
				if (StringUtils.isNotEmpty(testLocation)) {
					File dir = new File(testLocation);
					if (null != dir ? dir.isDirectory() && dir.canRead() : false) {
						File[] testFiles = dir.listFiles(getFileFilter());
						for (int i = 0; i < testFiles.length; i++) {
							File test = testFiles[i];
							if (null != test ? test.canRead() : false) {
								TestSynchronizationCheck checker = found
									.get(test.getName());
								if (null != checker) {
									List<String> questions = retrieveCompositeQuestions(test);
									checker.foundAt(testLocation, test,
										new TestQuestionsReferenced(questions));
								} else {
									List<String> questions = retrieveCompositeQuestions(test);
									checker = new TestSynchronizationCheck(locations);
									checker.setName(test.getName());
									checker.foundAt(testLocation, test,
										new TestQuestionsReferenced(questions));
									found.put(test.getName(), checker);
								}
							}
						}
					}
				}
			}
		}
		return found;
	}

	@Override
	public ClearanceResponse clearSelected(ClearanceResponse cr,
		Map<String, String> params) throws CleaningException {
		return handleCleanOrUndo(cr, ClearanceEnums.clean, params);
	}

	/**
	 * Iterates over the request parameters.  Then based on the ClearanceEnums
	 *  argument will delegate to the appropriate business logic to carry out
	 *  the task.
	 * 
	 * @param cr
	 * @param ce
	 * @param params
	 * @return
	 * @throws CleaningException
	 * @author Trevor Hinson
	 */
	protected ClearanceResponse handleCleanOrUndo(ClearanceResponse cr,
		ClearanceEnums ce, Map<String, String> params) throws CleaningException {
		if ((null != params ? params.size() > 0 : false) && null != cr ? null != cr
				.getSuperfluousQuestions() : false) {
			for (String key : params.keySet()) {
				String value = params.get(key);
				if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
					if (key.startsWith(ClearanceResponseRenderer.QUESTION_FORM_NAME_PREFIX)
						? key.length() > ClearanceResponseRenderer.QUESTION_FORM_NAME_PREFIX
						.length() : false) {
						String questionName = key.substring(
							ClearanceResponseRenderer.QUESTION_FORM_NAME_PREFIX
								.length(), key.length());
						clear(cr, questionName, ce, value);
					}
				}
			}
		}
		return cr;
	}

	/**
	 * Wrapper around the selection of how to process the users request based
	 *  on the ClearanceEnums argument.
	 * 
	 * @param cr 
	 * @param questionName 
	 * @param ce 
	 * @param value 
	 * @throws CleaningException 
	 * @author Trevor Hinson 
	 */
	protected void clear(ClearanceResponse cr, String questionName,
		ClearanceEnums ce, String value) throws CleaningException {
		if (null != cr && null != ce && StringUtils.isNotEmpty(questionName)) {
			IdentifiedSuperfluousQuestion isq = cr
				.getSuperfluousQuestions().get(questionName);
			List<String> questions = getQuestions(value);
			if (null != isq
				&& (null != questions ? questions.size() > 0 : false)) {
				if (ClearanceEnums.clean.equals(ce)) {
					archiveSelectedQuestions(questions, isq, cr, ce);
				}
			}
		}
	}

	/**
	 * Places the questions that have been selected by the user into an
	 *  appropriate archive for so that they may be retrieved in the future if
	 *  need be.
	 * 
	 * @param questions
	 * @param cr
	 * @author Trevor Hinson
	 */
	protected void archiveSelectedQuestions(List<String> questions,
		IdentifiedSuperfluousQuestion isq, ClearanceResponse cr,
		ClearanceEnums ce) {
		if ((null != questions ? questions.size() > 0 : false) && null != isq
			&& null != cr) {
			for (String s : questions) {
				if (StringUtils.isNotEmpty(s)) {
					File f = new File(s);
					if (f.exists()) {
						File dir = f.getParentFile();
						if (null != dir ? dir.isDirectory() && dir.canWrite()
								: false) {
							File archive = new File(dir.getAbsolutePath()
								+ File.separator + renderDate());
							if (continueWithArchiving(archive, cr, isq)) {
								archiveSelectedQuestions(f, archive, cr, isq);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Checks to see if the archiving of a particular question can continue or
	 *  not.  It assumes a positive outcome of true unless it is found
	 *  otherwise.
	 * 
	 * @param archive
	 * @param cr
	 * @param isq
	 * @return
	 * @author Trevor Hinson
	 */
	protected boolean continueWithArchiving(File archive, ClearanceResponse cr,
		IdentifiedSuperfluousQuestion isq) {
		boolean continu = true;
		if (!archive.exists()) {
			if (!archive.mkdir()) {
				continu = false;
				RemovalIssueDetails rid = new RemovalIssueDetails(
					"Unable to create the directory to "
					+ "archive the Question in : " + archive);
				cr.addProblemRemoving(isq, rid);
			}
		}
		return continu;
	}

	/**
	 * Carries out the archiving of the Question itself into the archive
	 *  File argument location.
	 * 
	 * @param f
	 * @param archive
	 * @param cr
	 * @param isq
	 * @author Trevor Hinson
	 */
	protected void archiveSelectedQuestions(File f, File archive,
		ClearanceResponse cr, IdentifiedSuperfluousQuestion isq) {
		if (archive.exists() ? archive.isDirectory() : false) {
			File archivedFile = new File(archive.getAbsolutePath()
				+ File.separator + f.getName());
			try {
				GeneralUtils.copyFile(f, archivedFile);
				if (!f.delete()) {
					RemovalIssueDetails rid = new RemovalIssueDetails(
						"Unable to remove the Question at this time.");
					cr.addProblemRemoving(isq, rid);
				}
			} catch (IOException x) {
				RemovalIssueDetails rid = new RemovalIssueDetails(
					"Unable to make a copy of the Question for the archive : "
					+ x.getMessage());
				rid.setException(x);
				cr.addProblemRemoving(isq, rid);
			}
		}
	}

	/**
	 * Simply renders todays date to a String in the DIRECTORY_DATE_FORMAT
	 * 
	 * @return
	 * @author Trevor Hinson
	 */
	protected String renderDate() {
		return new SimpleDateFormat(DIRECTORY_DATE_FORMAT).format(new Date());
	}

	/**
	 * Retrieves the actual selected question details from that provided in the
	 * request from the user.
	 * 
	 * @param responseParameterValue
	 * @return
	 * @author Trevor Hinson
	 */
	public static List<String> getQuestions(String responseParameterValue) {
		List<String> names = new ArrayList<String>();
		if (StringUtils.isNotEmpty(responseParameterValue)) {
			String seperator = ClearanceResponseRenderer
				.consistentPathSeperatorForDisplay(responseParameterValue);
			String checkFor = VersionUtil.DOT_JAR + seperator;
			if (responseParameterValue.contains(checkFor)) {
				String[] bits = responseParameterValue.split(checkFor);
				for (int i = 0; i < bits.length; i++) {
					String bit = bits[i];
					if (!bit.endsWith(VersionUtil.DOT_JAR)) {
						bit = bit + VersionUtil.DOT_JAR;
					}
					if (!bit.startsWith(seperator)) {
						bit = seperator + bit;
					}
					names.add(bit);
				}
			} else if (responseParameterValue.contains(VersionUtil.DOT_JAR)) {
				names.add(responseParameterValue);
			}
		}
		return names;
	}

	@Override
	public ClearanceResponse undo(ClearanceResponse cr,
		Map<String, String> params) throws CleaningException {
		return handleCleanOrUndo(cr, ClearanceEnums.undo, params);
	}

}
