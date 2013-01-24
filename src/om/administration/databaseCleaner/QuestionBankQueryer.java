package om.administration.databaseCleaner;

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

import om.abstractservlet.RequestAssociates;
import om.abstractservlet.RequestHandlingException;
import om.abstractservlet.RequestParameterNames;
import om.administration.questionbank.AllQuestionsPool;
import om.administration.questionbank.CleaningException;
import om.administration.questionbank.ClearanceEnums;
import om.administration.questionbank.IdentifiedSuperfluousQuestion;
import om.administration.questionbank.QuestionAndTestBankLocations;
import om.administration.questionbank.QuestionPoolDetails;
import om.administration.questionbank.TestDetails;
import om.administration.questionbank.TestQuestionsReferenced;
import om.administration.questionbank.TestQuestionsReferencedPool;
import om.tnavigator.db.DatabaseAccess;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.misc.GeneralUtils;
import util.misc.QuestionName;
import util.misc.StandardFileFilter;
import util.misc.Strings;
import util.misc.VersionUtil;
import util.xml.XML;

/**
 * This implementation of the QueryQuestionBanks is particuarly memory hungry
 * and there are a few places with higher performance questions. This is due to
 * what has to happen and a reluctance on heavy IO otherwise. <br />
 * <br />
 * Note that this is implemented with a single thread approach.
 * 
 * @author Trevor Hinson
 */

public class QuestionBankQueryer implements QueryQuestionBanks {

	private static String DIRECTORY_DATE_FORMAT = "yyyyMMdd";
	
	private static String DIRECTORY_TIME_FORMAT = "HHmm";
	
	private static String FILENAME_SEPARATOR = "_";

	private static String TEST = "test";
	
	private static String DEPLOY = "deploy";

	private static String ASSESSED = "assessed";

	private static String CONTENT = "content";

	private static String QUESTION = "question";

	private static String SECTION = "section";

	private static String ID = "id";

	private static String COMMA = ",";

	private static String DOT_TEST_DOT_XML = ".test.xml";
	
	private static String DOT_DEPLOY_DOT_XML = ".deploy.xml";


	private FileFilter fileFilter = new StandardFileFilter(DOT_TEST_DOT_XML);

	private FileFilter deployFileFilter = new StandardFileFilter(DOT_DEPLOY_DOT_XML);

	private FileFilter questionFilter = new StandardFileFilter(VersionUtil.DOT_JAR);
	
	private String archiveFilename = renderDate()+ FILENAME_SEPARATOR + renderTime();

	private static Set<String> splitOn = new HashSet<String>();

	static {
		splitOn.add(VersionUtil.DOT_JAR);
		splitOn.add(".txt");
		splitOn.add(".xml");
	}

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
	

	public FileFilter getDeployFileFilter() {
		return deployFileFilter;
	}

	@Override
	public QBTBQueryResponse identify(QuestionAndTestBankLocations qbl,
		RequestAssociates ra) throws CleaningException {
		QBTBQueryResponse cr = new QBTBQueryResponse();
		if (null != qbl ? null != qbl.getQuestionBanks() && null != ra
			&& null != qbl.getTestBanks() : false) {
			Banks banks = new Banks();
			banks.numberOfQuestionBanks = qbl.getQuestionBanks().size();
			banks.numberOfTestBanks = qbl.getTestBanks().size();
			if (banks.numberOfQuestionBanks > 0 && banks.numberOfTestBanks > 0) {
				try {
					/* all the test details from the test files including all the questions */
					Map<String, TestBankData> tests = identifyAllTests(
							qbl.getTestBanks(), cr);
					/* all the quesytion fileshe wbank */
					//AllQuestionsPool allQuestionsPool = identifyAllQuestions(qbl
						//	.getQuestionBanks());
					/* tests is the details for every test file */
					if (null != tests ? tests.size() > 0 : false) {
				//		TestQuestionsReferencedPool pool
					//		= new TestQuestionsReferencedPool();
						
						/* iterate over the testsand get the data from the database */
						for (TestBankData testCheck : tests
								.values()) 
						{
							String tName=testCheck.getName();
							
					//		addTestReferencedQuestions(testCheck, pool);
					//		if (!identifySyncStatus(testCheck, banks)) {
					//			cr.addOutOfSyncTest(testCheck);
							}
							//determineTests( tests,cr, banks);
					//	}
					//	identifySuperflousQuestions(pool, allQuestionsPool, cr,
						//		ra);
					}
				} catch (IOException x) {
					throw new CleaningException(x);
				}
			}
		}
		return cr;
	}

	/**
	 * get al the tests and the assoicated questions
	 * check if they are assessed or not
	 * 
	 * @param testCheck
	 * @param questionPool
	 * @param cr
	 * @param banks
	 * @author sarah wood
	 */
	protected void determineTests( TestBankData testCheck,QBTBQueryResponse cr, Banks banks) {
		
		if ( null != cr && null != banks) 
		{			
			//Map<String, TestQuestionReferences> tqr
			//	= new HashMap<String, TestQuestionReferences>();
			/* look at each test and get its details */

			if (null != testCheck && null != testCheck.getTestDetails()) 
			{
				for (String location : testCheck.getTestDetails().keySet()) {
					TestDetails td = testCheck.getTestDetails().get(location);
					if (null != td ? null != td.getTestDefinition() : false) {
						TestQuestionsReferenced tqr = td
								.getQuestionsReferenced();
						if (null != tqr) 
						{
							for (String questionName : tqr
									.getNamesOfQuestions()) {
		//						maintainBrokenTest(questionName, td,
		//								questionPool, banks, testCheck, btqr);
							}
						}
					}
				}
			}
			
		//	if (tqr.size() > 0) {
		//		Set<TestQuestionReferences> testRef
		//			= new HashSet<TestQuestionReferences>();
		//		testRef.addAll(tqr.values());
		//		cr.addTest(testRef);
		//	}
		}
	}

	/**
	 *  tries to create a TestQuestionReference and place
	 *  it within the tqr Collection.
	 * 
	 * @param questionName
	 * @param td
	 * @param questionPool
	 * @param banks
	 * @param testCheck
	 * @param tqr
	 * @author Trevor Hinson
	 */
	protected void maintainTest(String questionName, TestDetails td,
		AllQuestionsPool questionPool, Banks banks,
		TestBankData testCheck,
		
		Map<String, TestQuestionReferences> tqr) {
		/* if there is a question name */
		if (Strings.isNotEmpty(questionName)) {
			/* get the detailas */
			QuestionPoolDetails qpd = questionPool.getDetails(questionName);
			if (null != qpd) {
				/* get its version */
				Map<String, Set<String>> verNum = qpd
					.getQuestionsWithVersionNumbering();
				/* find the latest version */
				String latestVersion = qpd.identifyLatestVersion();
				if (null != verNum && Strings.isNotEmpty(latestVersion)) {
					Set<String> set = verNum.get(latestVersion);
					if (null != set) {
						if (set.size() != banks.numberOfQuestionBanks) {
							
							TestQuestionReferences ref = tqr
									.get(questionName);
							if (null == ref) {
								ref = new TestQuestionReferences(
										testCheck);
								ref.setQuestionName(questionName);
								tqr.put(questionName, ref);
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
		AllQuestionsPool qp, QBTBQueryResponse cr, RequestAssociates ra)
		throws CleaningException {
		if ((null != trq ? null != trq.getReferencedNames() : false)
				&& null != qp && null != cr) {
			Iterator<String> referencedQuestionNames = trq.getReferencedNames();
			while (referencedQuestionNames.hasNext()) {
				String referencedQuestionName = referencedQuestionNames.next();
				if (Strings.isNotEmpty(referencedQuestionName)) {
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
		String testReferencedQuestionName, QBTBQueryResponse cr,
		TestQuestionsReferencedPool trq, RequestAssociates ra)
		throws CleaningException {
		if (null != qp && Strings.isNotEmpty(testReferencedQuestionName)
			&& null != cr && null != trq) {
			QuestionPoolDetails details = qp
				.getDetails(testReferencedQuestionName);
			if (null != details) {
				String latest = details.identifyLatestVersion();
				if (null != details.getQuestionsWithVersionNumbering()
					&& Strings.isNotEmpty(latest)) {
					DatabaseAccess da = getDatabaseAccess(ra);
					for (String fullName : details
							.getQuestionsWithVersionNumbering().keySet()) {
						if (Strings.isNotEmpty(fullName)) {
							if (!fullName.equals(latest)) {
								if (!isOpenForAnyStudent(generateQuery(ra,
										fullName), da)) {
									Set<String> locations = details
										.getQuestionsWithVersionNumbering()
										.get(fullName);
	//								cr.addSuperfluousQuestion(
		//								new IdentifiedSuperfluousQuestion(
	//										fullName, locations));
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
		if (Strings.isNotEmpty(query) && null != da) {
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
			QBTBQueryResponse cr, RequestAssociates ra)
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
			//				cr.addSuperfluousQuestion(
			//					new IdentifiedSuperfluousQuestion(
			//						fullName, locations));
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
		if (Strings.isNotEmpty(questionFullName) && null != ra) {
			try {
				String q = ra.getConfig(ClearanceEnums.query);
				if (Strings.isNotEmpty(q)) {
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
	
	/* identifies if a test is assed rom the XML file 
	 * first find the deploy file that corresponds, error if we cant find it
	 * @param f
	 * @return
	 * @throws IOException
	 * @author Sarah Wood
	 */
	
	protected boolean retrieveIsAssessed(String fname,File[] deployFiles) 
	throws IOException
	{
		boolean isAssessed=false;
		
		boolean foundit=false;
		String testName = fname.split(DOT_TEST_DOT_XML)[0];

		for (int i = 0; i < deployFiles.length && !foundit; i++) 
		{
			File f = deployFiles[i];
			String thisFileName=f.getName();
			/* get the test part of the deploy xml file */
			String deployName = thisFileName.split(DOT_DEPLOY_DOT_XML)[0];
			if (deployName.equals(testName))
			{
				foundit=true;		
				if (null != f ? f.canRead() : false) {
					Document dDeploy = XML.parse(f);
					if (null != dDeploy) {
						if (XML.hasChild(dDeploy, DEPLOY)) {
							Node deploy = XML.getChild(dDeploy, DEPLOY);
							if (XML.hasChild(deploy, CONTENT)) {
								Node content = XML.getChild(deploy, ASSESSED);
								if (null != content) {
									/* we have an assed tag so isAssessed is true */
									isAssessed=true;
								}
							}
						}
					}
				}
			}
		}
		return isAssessed;
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
			if (Strings.isNotEmpty(id)) {
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
				if (Strings.isNotEmpty(location)) {
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
	 * On finding a test we create a new TestBankData. If a test by
	 * the same name exists within the Map that is being built then we
	 * essentially tick the location setting for that TestBankData
	 * 
	 * @param locations
	 * @param cr
	 * @return
	 * @author Trevor Hinson
	 */
	protected Map<String, TestBankData> identifyAllTests(
		List<String> locations, QBTBQueryResponse cr) throws IOException {
		
		Map<String, TestBankData> found
			= new HashMap<String, TestBankData>();
		
		
		if (null != cr && (null != locations ? locations.size() > 0 : false)) {
			/* for all the test banks */
			for (String testLocation : locations) {
				if (Strings.isNotEmpty(testLocation)) {
					File dir = new File(testLocation);
					/* read the files in the test bank */
					if (null != dir ? dir.isDirectory() && dir.canRead() : false) {
						File[] testFiles = dir.listFiles(getFileFilter());
						File[] deployFiles = dir.listFiles(getDeployFileFilter());

						/* for each test file */
						for (int i = 0; i < testFiles.length; i++) 
						{
							File test = testFiles[i];
							if (null != test ? test.canRead() : false) {
								TestBankData checker = found
									.get(test.getName());
								try {	
									/* get a list of the questions from the XML file */
									/* if we alread foundnd and processed it? NULL if we havent */
									if (null != checker) {
										List<String> questions = retrieveCompositeQuestions(test);
										checker.foundAt(testLocation, test,
											new TestQuestionsReferenced(questions));
									} else {
										/* read the xml file for all the questions */
										List<String> questions = retrieveCompositeQuestions(test);
										checker = new TestBankData(locations);
										/* read the file name */
										checker.setName(test.getName());
										
										checker.foundAt(testLocation, test,
											new TestQuestionsReferenced(questions));	
										/* set the assessed tag for the test */
										TestDetails thisTest=checker.getThisTestDetails(test.getName());
										boolean ia=retrieveIsAssessed(test.getName(),deployFiles);
										thisTest.setIsAssessed(ia);

										found.put(test.getName(), checker);
									}
								} catch (IOException x) {
									cr.addTestXML(
										new TestXML(test.getAbsolutePath()));
								}
							}
						}
					}
				}
			}
		}
		return found;
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
		IdentifiedSuperfluousQuestion isq, QBTBQueryResponse cr,
		ClearanceEnums ce) {
		if ((null != questions ? questions.size() > 0 : false) && null != isq
			&& null != cr) {
			for (String s : questions) {
				if (Strings.isNotEmpty(s)) {
					File f = new File(s);
					if (f.exists()) {
						File dir = f.getParentFile();
						if (null != dir ? dir.isDirectory() && dir.canWrite()
								: false) {
							String archiveDirName=dir.getAbsolutePath()+File.separator+this.archiveFilename;
							/* if this is a new archive folder add it to the list */
							cr.addUniqArchiveDir(archiveDirName);
							File archive = new File(archiveDirName);
							//if (continueWithArchiving(archive, cr, isq)) {
								//archieveIndividualQuestion(f, archive, cr, isq);
							//}
						}
					}
				}
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
	 * Simply renders todays time to a String in the DIRECTORY_DATE_FORMAT
	 * 
	 * @return
	 * @author sarah wood
	 */
	protected String renderTime() {
		return new SimpleDateFormat(DIRECTORY_TIME_FORMAT).format(new Date());
	}
	/**
	 * Retrieves the actual selected question details from that provided in the
	 * request from the user.
	 * 
	 * @param responseParameterValue
	 * @return
	 * @author Trevor Hinson
	 */
	public static List<String> getQuestions(String requestParameterValue) {
		List<String> names = new ArrayList<String>();
		if (Strings.isNotEmpty(requestParameterValue)) {
			String checkFor = splitter(requestParameterValue);
			if (Strings.isNotEmpty(checkFor)) {
				String[] bits = requestParameterValue.split(checkFor);
				for (int i = 0; i < bits.length; i++) {
					String bit = bits[i];
					if (!bit.endsWith(checkFor)) {
						bit = bit + checkFor;
					}
					if (bit.startsWith(COMMA)) {
						bit = bit.substring(1, bit.length());
					}
					names.add(bit.trim());
				}
			}
		}
		return names;
	}

	/**
	 * Determines a valid splitting suffix from within the String argument and
	 *  then returns it.
	 * 
	 * @param s
	 * @return
	 * @author Trevor Hinson
	 */
	protected static String splitter(String s) {
		String splitter = null;
		if (Strings.isNotEmpty(s)) {
			x : for (String val : splitOn) {
				if (Strings.isNotEmpty(val) ? s.contains(val) : false) {
					splitter = val;
					break x;
				}
			}
		}
		return splitter;
	}
	


	protected QBTBQueryResponse handleCleanOrUndo(QBTBQueryResponse cr,
		ClearanceEnums ce, Map<String, String> params) throws CleaningException {
		return cr;
	}
	@Override 
	public QBTBQueryResponse undo(QBTBQueryResponse cr,
		Map<String, String> params) throws CleaningException {
		return handleCleanOrUndo(cr, ClearanceEnums.undo, params);
	}
	
	public QBTBQueryResponse clean(QBTBQueryResponse cr,
			Map<String, String> params) throws CleaningException {
			return handleCleanOrUndo(cr, ClearanceEnums.undo, params);
		}

}
