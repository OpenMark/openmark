/* OpenMark online assessment system
   Copyright (C) 2007 The Open University

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package om.tnavigator.teststructure;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import om.OmException;
import om.OmFormatException;
import om.OmUnexpectedException;
import om.tnavigator.auth.UserDetails;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.xml.XML;
import util.xml.XMLException;

/** Represents information from the .deploy.xml file */
public class TestDeployment
{
	// The folder the deploy file came from.
	private File testBank;

	private String sDefinition,sQuestion;
	private Document dDeploy;
	private Element eDates,eAccess;

	/** Batch number (null if test is not set up for CMA transfer) */
	private String sBatch=null;
	/** Course code (null if course is not set up for CMA) */
	private String sCourseCode=null;
	/** Array of assignment numbers (null if test not set up for CMA) */
	private String[] asAssignmentNum;
	/** Array of question counts in each assignment (null if test not set up for CMA) */
	private int[] aiAssignmentCount;

	/** Type of test */
	private int iType;
	/**
	 * Used to record whether the <assessed> tag has attributes. In the past it
	 * could. These days it should not, and we may want to report that.
	 */
	private boolean assessedTagHasLegacyAttributes = false;
	/** Test is not assessed (default) */
	public final static int TYPE_NOTASSESSED = 1;
	/** Test is assessed and students are in trouble if they don't submit */
	public final static int TYPE_ASSESSED = 2;

	/* PCDC message   required */
	public final static boolean PCDC_REQUIRED=true;
	public final static boolean PCDC_NOT_REQUIRED=false;
		/* PCDC message   displayed to student */
	public final static boolean PCDC_DISPLAY=true;
	public final static boolean PCDC_NODISPLAY=false;

	private boolean bPcdc=PCDC_NOT_REQUIRED;
	private boolean bDisplayPcdc=PCDC_DISPLAY;

	/** True if the test should send out confirm emails */
	private boolean bSubmitEmail;

	private static final int DEFAULT_NUMBER_OF_QUESTIONS = 1;

	private static final int DEFAULT_FORBID_EXTENSION_DAYS = 7;

	private int forbidExtensionDays = DEFAULT_FORBID_EXTENSION_DAYS;

	private static String NUMBER_OF_QUESTIONS = "numberofquestions";

	private static String FORBID_EXTENSION = "forbidextension";

	private int numberOfQuestions = DEFAULT_NUMBER_OF_QUESTIONS;

	private static String EMAIL_STUDENTS = "emailstudents";

	private static String MODULE = "module";

	private static String ICMA = "icma";

	private String module;

	private String icma;

	private boolean bhasEmailStudents=false;

	private String templateSet = null;

	private int cleanDataAfterDays = 0;

	/**
	 * Get the days after which attempts at this test should be automatically deleted.
	 * @return number of days, or 0 to mean use default.
	 */
	public int getCleanDataAfterDays() {
		return cleanDataAfterDays;
	}

	public void setType(int n) {
		if (TYPE_NOTASSESSED == n || TYPE_ASSESSED == n) {
			iType = n;
		}
	}

	public String getModule() {
		return module;
	}

	public String getIcma() {
		return icma;
	}

	public boolean getbPcdc() {
		return bPcdc;
	}

	public boolean getbDisplayPcdc() {
		return bDisplayPcdc;
	}

	public int getForbidExtensionDays() {
		return forbidExtensionDays;
	}

	/**
	 * @return the student must have finished this many questions before they
	 * get a notification about overdue quiz attempts.
	 */
	public int getNumberOfQuestionsForNotification() {
		return numberOfQuestions;
	}

	/**
	 * Checks to see if the test is using the emailstudents facility.
	 * @return
	 */
	public boolean isUsingEmailStudents() {
		return bhasEmailStudents;
	}

	/**
	 * Get which Template set to use, otherwise null.
	 */
	public String getTemplateSet()
	{
		return templateSet;
	}

	/**
	 * Constructs test definition and checks format.
	 * @param f File to use
	 * @throws OmException Failure loading file or parsing XML
	 * @throws OmFormatException Anything wrong with the specific format
	 */
	public TestDeployment(File f) throws OmException
	{
		this(parseFile(f), f.getParentFile(), f.getName());
	}

	private static Document parseFile(File f) throws OmException {
		try
		{
			return XML.parse(f);
		}
		catch (IOException ioe)
		{
			throw new OmException("Error loading/parsing " + f.getName(), ioe);
		}
	}

	/**
	 * Constructs test definition and checks format.
	 * @param f File to use
	 * @throws OmException Failure loading file or parsing XML
	 * @throws OmFormatException Anything wrong with the specific format
	 */
	public TestDeployment(Document d, File bank, String sErrorIdentifier) throws OmException
	{
		dDeploy = d;
		testBank = bank;

		try
		{
			// Get basic stuff from the XML so we don't need to throw exceptions
			// later if it's absent
			Element eRoot=dDeploy.getDocumentElement();
			if(XML.hasChild(eRoot,"question"))
			{
				sQuestion=XML.getText(eRoot,"question");
			}
			else
			{
				sDefinition=XML.getText(eRoot,"definition");
			}
			eDates=XML.getChild(eRoot,"dates");
			eAccess=XML.getChild(eRoot,"access");

			if(XML.hasChild(eRoot,"cleandataafterdays"))
			{
				cleanDataAfterDays=XML.getInt(eRoot,"cleandataafterdays");
			}

			if(XML.hasChild(eRoot, "assessed"))
			{
				iType = TYPE_ASSESSED;
				if (XML.getChild(eRoot, "assessed").hasAttributes()) {
					assessedTagHasLegacyAttributes = true;
				}
			}
			else
			{
				iType = TYPE_NOTASSESSED;
			}

			// Check is pre-course diagnostic code required, and check the
			// attribute display.
			if(XML.hasChild(eRoot,"pcdc"))
			{
				bPcdc=PCDC_REQUIRED;
				/** by default its displayed **/
				Element ePCDC=XML.getChild(eRoot,"pcdc");
				if("no".equalsIgnoreCase(ePCDC.getAttribute("display")))
					bDisplayPcdc=PCDC_NODISPLAY;
			}
			else
			{
				bPcdc=PCDC_NOT_REQUIRED;
			}

			handleEmailStudentsNode(eRoot);

			if(XML.hasChild(eRoot,"email"))
			{
				Element eEmail=XML.getChild(eRoot,"email");
				bSubmitEmail="yes".equals(eEmail.getAttribute("submit"));
			}

			if(XML.hasChild(eRoot,"cma"))
			{
				Element eCMA=XML.getChild(eRoot,"cma");
				sBatch=XML.getText(eCMA,"batch");
				if(!sBatch.matches("3[0-9]{3}"))
					throw new OmFormatException("Error processing "+sErrorIdentifier+
						"; <cma> <batch> number not in required format");
				sCourseCode=XML.getText(eCMA,"course");
				if(!sCourseCode.matches("[A-Z]+[0-9]+") || sCourseCode.length()>7)
					throw new OmFormatException("Error processing "+sErrorIdentifier+
						"; <cma> <course> number not in required format");
				Element[] aeAssignments=XML.getChildren(eCMA,"assignment");
				if(aeAssignments.length==0)
					throw new OmFormatException("Error processing "+sErrorIdentifier+
						"; <cma> <assignment> missing");
				asAssignmentNum=new String[aeAssignments.length];
				aiAssignmentCount=new int[aeAssignments.length];
				int iExpectedNext=1;
				for(int i=0;i<aeAssignments.length;i++)
				{
					int
						iFrom=Integer.parseInt(
							XML.getRequiredAttribute(aeAssignments[i],"from")),
						iTo=Integer.parseInt(
							XML.getRequiredAttribute(aeAssignments[i],"to"));
					asAssignmentNum[i]=XML.getText(aeAssignments[i]);
					if(!asAssignmentNum[i].matches("[0-9]{2}"))
						throw new OmFormatException("Error processing "+sErrorIdentifier+
							"; <cma> <assignment> number not in required format");
					if(iFrom!=iExpectedNext)
						throw new OmFormatException("Error processing "+sErrorIdentifier+
							"; <cma> <assignment> from= numbers must be consecutive");
					aiAssignmentCount[i]=iTo-iFrom+1;
					iExpectedNext+=aiAssignmentCount[i];
				}
			}

			if (XML.hasChild(eRoot, "templateset"))
			{
				templateSet = XML.getText(eRoot, "templateset");
			}
		}
		catch(NumberFormatException nfe)
		{
			throw new OmFormatException("Error processing "+sErrorIdentifier+
				"; invalid integer",nfe);
		}
		catch(XMLException xe)
		{
			throw new OmFormatException("Error processing "+sErrorIdentifier,xe);
		}
	}

	private void handleEmailStudentsNode(Element eRoot) throws XMLException {
		if (null != eRoot) {
			if (XML.hasChild(eRoot, EMAIL_STUDENTS)) {
				bhasEmailStudents=true;
				Element e = XML.getChild(eRoot, EMAIL_STUDENTS);
				if (null != e) {
					if (XML.hasChild(e, NUMBER_OF_QUESTIONS)) {
						numberOfQuestions = retrieveValue(e, NUMBER_OF_QUESTIONS);
					}
					if (XML.hasChild(e, FORBID_EXTENSION)) {
						Integer n = retrieveValue(e, FORBID_EXTENSION);
						if (null != n) {
							forbidExtensionDays = n;
						}
					}
					module = retrieveTextValue(e, MODULE);
					icma = retrieveTextValue(e, ICMA);
				}
			}
		}
	}

	private String retrieveTextValue(Element e, String name)
		throws XMLException {
		String value = null;
		if (XML.hasChild(e, name)) {
			value = XML.getText(XML.getChild(e, name));
		}
		return value;
	}

	private int retrieveValue(Element e, String name) throws XMLException {
		Integer number = null;
		String s = retrieveTextValue(e, name);
		if (null != s ? s.length() > 0 : false) {
			number = new Integer(s);
		}
		return number;
	}

	/** @return One of the TYPE_xx constants */
	public int getType()
	{
		return iType;
	}

	/** @return Whether this is an assessed test. */
	public boolean isAssessed()
	{
		// Allow any value > TYPE_NOTASSESSED, not just TYPE_ASSESSED.
		return iType > TYPE_NOTASSESSED;
	}

	/**
	 * @return get the name of the test definition file.
	 */
	public String getDefinition()
	{
		if(sDefinition==null)
			throw new OmUnexpectedException("Can't get definition, single only");
		return sDefinition;
	}

	public String getQuestion()
	{
		if(sQuestion==null)
			throw new OmUnexpectedException("Can't get question, test only");
		return sQuestion;
	}

	/** @return True if deployment is for use in single-question mode (only) */
	public boolean isSingleQuestion()
	{
		return sQuestion!=null;
	}

	public boolean authorshipRequired()
	{
		return iType == TYPE_ASSESSED;
	}

	public boolean assessedTagHasLegacyAttributes() {
		return assessedTagHasLegacyAttributes;
	}

	/**
	 * @return whether this test is world accessible.
	 * @throws OmFormatException
	 */
	public boolean isWorldAccess() throws OmFormatException
	{
		try
		{
			return ("yes".equals(XML.getChild(eAccess,"users").getAttribute("world")));
		}
		catch(XMLException e)
		{
			throw new OmFormatException("Missing <users> in deployment file");
		}
	}

	/**
	 * Checks access to test. (Does not check dates! And don't call this if
	 * isWorldAccess() returns true.)
	 * @param ud User details from SAMS
	 * @return True if a user has access to the test, false otherwise.
	 * @throws OmFormatException If there's something wrong with the file
	 */
	public boolean hasAccess(UserDetails ud) throws OmFormatException
	{
		try
		{
			// Check ordinary user access.
			Element eParent=XML.getChild(eAccess, "users");
			if (getAccessTag(ud, eParent, false) != null) return true;

			// Now check admin access, if there are any.
			if (XML.hasChild(eAccess,"admins"))
			{
				eParent=XML.getChild(eAccess,"admins");
				if (getAccessTag(ud, eParent, false) != null) return true;
			}

			// Neither user nor admin.
			return false;
		}
		catch(XMLException xe)
		{
			throw new OmFormatException("Error in test deployment file",xe);
		}
	}

	/**
	 * @param ud SAMS login details
	 * @return True if user has admin access to test
	 * @throws OmFormatException
	 */
	public boolean isAdmin(UserDetails ud) throws OmFormatException
	{
		if (!XML.hasChild(eAccess,"admins")) return false;
		try
		{
			return getAccessTag(ud, XML.getChild(eAccess, "admins"), false) != null;
		}
		catch(XMLException e)
		{
			// Can't happen as we just checked hasChild
			throw new OmUnexpectedException(e);
		}
	}

	/**
	 * @param ud SAMS login details
	 * @return True if user can access reports
	 * @throws OmFormatException
	 */
	public boolean allowReports(UserDetails ud) throws OmFormatException
	{
		if (!XML.hasChild(eAccess, "admins")) return false;
		try
		{
			return getAccessTag(ud, XML.getChild(eAccess, "admins"), true) != null;
		}
		catch(XMLException e)
		{
			// Can't happen as we just checked hasChild
			throw new OmUnexpectedException(e);
		}
	}

	/**
	 * Checks access statements under the specified tag to see if they match the
	 * user.
	 * @param ud Details of user
	 * @param eParent which list of users to check. This is normally either the
	 *      users or the admin tag from the deploy file.
	 * @param withReports should only be used when eParent is the admin tag. If
	 *      true, only return element with the report="yes" attribute.
	 * @return True if user has access via this tag, false otherwise
	 * @throws OmFormatException If there's something wrong with the file
	 */
	private Element getAccessTag(UserDetails ud, Element eParent, boolean withReports) throws OmFormatException
	{
		// Look for all access options within parent
		Element[] aeOptions = XML.getChildren(eParent);
		for (int iOption = 0; iOption < aeOptions.length; iOption++)
		{
			Element eOption = aeOptions[iOption];

			if (withReports && !"yes".equals(eOption.getAttribute("reports"))) {
				continue;
			}

			String sTag = eOption.getTagName();
			// OUCU is what the OU calls usernames, left in for backwards compatibility
			if (sTag.equals("oucu") || sTag.equals("username"))
			{
				if (XML.getText(eOption).equals(ud.getUsername())) return eOption;
			}
			else if(sTag.equals("authid"))
			{
				if (ud.hasAuthID(XML.getText(eOption))) return eOption;
			}
			else throw new OmFormatException(
				"Error in test deployment file; unknown access tag <"+sTag+">");
		}

		// No matches.
		return null;
	}

	/** @return True if it's after/on the open date for the test (always returns
	 *   true if the open date was set to 'yes')
	 * @throws OmFormatException */
	public boolean isAfterOpen() throws OmFormatException
	{
		return isAfterDate("open","00:00:00",true,"yes",-1);
	}

	/**
	 * @return True if it's after/on the close date for the test, i.e. results
	 *   don't count any more (always returns false if there is no close date)
	 * @throws OmFormatException
	 */
	public boolean isAfterClose() throws OmFormatException
	{
		return isAfterDate("close","23:59:59",false,null,-1) || isAfterForbid();
	}

	/**
	 * @return True if it's after/on the forbid date for the test, i.e. students
	 *   literally can't take the test any more
	 *   (always returns false if there is no forbid date)
	 * @throws OmFormatException
	 */
	public boolean isAfterForbid() throws OmFormatException
	{
		return isAfterDate("forbid","23:59:59",false,null,-1);
	}

	public boolean isDateAfterForbid(long when) throws OmFormatException
	{
		return isAfterDate("forbid","23:59:59",false,null,when);
	}


	/**
	 * After the forbid date there is a 4 hour extension during which you can
	 * submit the test but can't do anything else.
	 * @return True if it's after/on the forbid extension
	 *   (always returns false if there is no forbid date)
	 * @throws OmFormatException
	 */
	public boolean isAfterForbidExtension() throws OmFormatException {
		int reduceBy = 4;
		if (isUsingEmailStudents()) {
			int num = getForbidExtensionDays();
			reduceBy = num > 0 ? num * 24 : reduceBy;
		}
		return isAfterDate("forbid","23:59:59",false,null,
			System.currentTimeMillis()-reduceBy*60*60*1000);
	}

	/**
	 * @return True if feedback is permitted (always returns true if there is
	 *   no feedback date)
	 * @throws OmFormatException
	 */
	public boolean isAfterFeedback() throws OmFormatException
	{
		return isAfterDate("feedback","00:00:00",true,null,-1);
	}

	/** Initial date-time pattern match for validation */
	private static final Pattern DATETIME=Pattern.compile("^[0-9]{4}-[0-9]{2}-[0-9]{2}(?: ([0-9:]+))?$");
	/** Matches time including seconds */
	private static final Pattern FULLTIME=Pattern.compile("^[0-9]{2}:[0-9]{2}:[0-9]{2}$");
	/** Matches time without seconds */
	private static final Pattern PARTTIME=Pattern.compile("^[0-9]{2}:[0-9]{2}$");

	/**
	 * @param sTag Date tag to compare
	 * @param sDefaultTime Default time for that tag if only date is specified
	 * @param bDefault Default result if entire tag is absent
	 * @param sDefaultMarker If non-null, require this string in order to use
	 *   default result (otherwise, will give default result if no date was
	 *   specified)
	 * @param lNow Time in milliseconds, -1 to use current
	 * @return True if the date has been passed, false if it hasn't
	 * @throws OmFormatException
	 */
	private boolean isAfterDate(String sTag,String sDefaultTime,boolean bDefault,String sDefaultMarker,
			long lNow)
	  throws OmFormatException
	{
		if(lNow==-1) lNow=System.currentTimeMillis();
		// If it's not there, give default
		if(!XML.hasChild(eDates,sTag))
		{
			if(sDefaultMarker==null)
				return bDefault;
			else
				throw new OmFormatException("Error in test deployment file: <dates> <"+sTag+"> is required");
		}

		try
		{
			if(sDefaultMarker!=null && XML.getText(eDates,sTag).equals(sDefaultMarker))
				return bDefault;
		}
		catch(XMLException e)
		{
			throw new OmUnexpectedException(e);
		}

		// So is 'now' after that date?
		return (new Date(lNow)).after(getActualDate(sTag,sDefaultTime));
	}

	/**
	 * @param sTag Date tag to compare
	 * @param sDefaultTime Default time for that tag if only date is specified
	 * @return Java date object
	 * @throws OmFormatException
	 */
	private Date getActualDate(String sTag,String sDefaultTime)
	  throws OmFormatException
	{
		String sDate;
		try
		{
			// Get the date text
			sDate=XML.getText(eDates,sTag);
		}
		catch(XMLException xe)
		{
			// Shouldn't happen because we just checked hasChild
			throw new OmUnexpectedException(xe);
		}

		// Check it with regex
		Matcher m=DATETIME.matcher(sDate);
		if(!m.matches()) throw new OmFormatException("Invalid date: "+sDate);
		if(m.group(1)==null)
		{
			// No time specified, use default
			sDate+=" "+sDefaultTime;
		}
		else
		{
			// Check time
			String sTime=m.group(1);
			Matcher mPart=PARTTIME.matcher(sTime);
			if(mPart.matches())
			{
				sDate+=":00";
			}
			else if(!FULLTIME.matcher(sTime).matches())
			{
				throw new OmFormatException("Invalid time: "+sDate);
			}
		}

		try
		{
			// OK, we now have a full date and time string, let's parse it
			// (note that this SDF isn't static because apparently they aren't
			// supposed to be thread-safe)
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// We're using the local time zone (I hope)
			return sdf.parse(sDate);
		}
		catch(ParseException e)
		{
			// Shouldn't happen because we validated the date
			throw new OmUnexpectedException(e);
		}
	}

	/**
	 * @param tagName Date tag
	 * @return Java Date object. Returns null if tag doesn't exist.
	 * @throws OmFormatException
	 */
	public Date getActualDate(String tagName) throws OmFormatException {
		if (!XML.hasChild(eDates, tagName)) {
			return null;
		}
		return getActualDate(tagName, "00:00:00");
	}

	/**
	 * @param tagName Date tag
	 * @param sDefaultTime default time to use if none specified
	 * @return Java Date object. Returns null if tag doesn't exist.
	 * @throws OmFormatException
	 */
	public Date getActualDateWithDefaultTime(String tagName,String sDefaultTime) throws OmFormatException {
		if (!XML.hasChild(eDates, tagName)) {
			return null;
		}
		if (sDefaultTime != null)
		{
			return getActualDate(tagName,sDefaultTime );
		}
		else
		{
			throw new OmFormatException("Invalid default time specified");
		}
	}
	/**
	 * @return A friendly display version of the date in the format 13 September 2005.
	 * @throws OmFormatException
	 */
	public String displayFeedbackDate() throws OmFormatException
	{
		SimpleDateFormat sdf=new SimpleDateFormat("dd MMMM yyyy");
		if (XML.hasChild(eDates,"feedback")) {
			return sdf.format(getActualDate("feedback","00:00:00"));
		} else {
			return "";
		}
	}

	/** @return True if a close date was specified */
	public boolean hasCloseDate()
	{
		return XML.hasChild(eDates,"close") || XML.hasChild(eDates,"forbid");
	}

	/**
	 * @return A friendly display version of the date in the format 13 September 2005.
	 * @throws OmFormatException
	 */
	public String displayCloseDate() throws OmFormatException
	{
		SimpleDateFormat sdf=new SimpleDateFormat("dd MMMM yyyy");
		if(XML.hasChild(eDates,"close"))
		{
			return sdf.format(getActualDate("close","00:00:00"));
		}
		else if (XML.hasChild(eDates,"forbid"))
		{
			return sdf.format(getActualDate("forbid","00:00:00"));
		} else {
			return "";
		}
	}
	/**
	 * @return A friendly display version of the date in the format 13 September 2005.
	 * @throws OmFormatException
	 */
	public String displayForbidDate() throws OmFormatException
	{
		SimpleDateFormat sdf=new SimpleDateFormat("dd MMMM yyyy");
		if (XML.hasChild(eDates,"forbid")) {
			return sdf.format(getActualDate("forbid","00:00:00"));
		} else {
			return "";
		}
	}

	/**
	 * Used to display the date of the forbid extension for when a student is
	 * to be notified.
	 * @return
	 * @throws OmFormatException
	 */
	public String displayForbidExtensionDate()
		throws OmFormatException {

		int extensionValue = getForbidExtensionDays();
		if (extensionValue <= 0)
		{
			return null;
		}

		if (!XML.hasChild(eDates, "forbid"))
		{
			return null;
		}
		Date finishDate = getActualDate("forbid", "00:00:00");

		if (null == finishDate)
		{
			return null;
		}

		long extensionDate = finishDate.getTime() + extensionValue * 24 * 60 * 60 * 1000;
		Date eDate = new Date(extensionDate);
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
		return sdf.format(eDate);
	}

	/**
	 * @return A friendly display version of the date in the format 13 September 2005.
	 * @throws OmFormatException
	 */
	public String displayOpenDate() throws OmFormatException
	{
		SimpleDateFormat sdf=new SimpleDateFormat("dd MMMM yyyy");
		if (XML.hasChild(eDates,"open")) {
			try {
				if ("yes".equals(XML.getText(eDates, "open"))) {
					return "Always";
				} else {
					return sdf.format(getActualDate("open","00:00:00"));
				}
			} catch (XMLException e) {
				throw new OmUnexpectedException(e);
			}
		} else {
			return "";
		}
	}

	/** @return True if this deployment is set up for CMA conversion */
	public boolean hasCMAData()
	{
		return sBatch!=null;
	}

	/** @return True if a submit confirm email should be sent, false otherwise */
	public boolean requiresSubmitEmail()
	{
		return bSubmitEmail;
	}

	/**
	 * @return the definition of the test this deployment file points to.
	 * @throws OmException
	 */
	public TestDefinition getTestDefinition() throws OmException
	{
		File fDefinition=new File(testBank, getDefinition()+".test.xml");
		return new TestDefinition(fDefinition);
	}

	/**
	 * @return the information about who to contact if a problem is identified with this test.
	 */
	public String getSupportContacts() {
		if (XML.hasChild(dDeploy.getDocumentElement(), "supportcontacts")) {
			try {
				return XML.getText(dDeploy.getDocumentElement(), "supportcontacts");
			} catch (XMLException e) {
				// Cannot happen because of the hasChild test.
				throw new OmUnexpectedException(e);
			}
		} else {
			return "";
		}
	}

	/**
	 * @return List of usernames who can access the test
	 */
	public List<String> getAllowedUsernames() {
		ArrayList<String> usernames = new ArrayList<String>();
		if(!XML.hasChild(eAccess, "users")) return usernames;
		try {
			Element[] eUsers=XML.getChildren(XML.getChild(eAccess,"users"));
			for (Element eUser : eUsers) {
				if ("oucu".equals(eUser.getTagName()) || "username".equals(eUser.getTagName())) {
					usernames.add(XML.getText(eUser));
				}
			}
		} catch (XMLException e) {
			// Can't happen as we just checked hasChild
			throw new OmUnexpectedException(e);
		}
		return usernames;
	}

	/**
	 * @return List of allowed user authids
	 */
	public List<String> getAllowedUserAuthids() {
		ArrayList<String> userAuthids = new ArrayList<String>();
		if(!XML.hasChild(eAccess, "users")) return userAuthids;
		try {
			Element[] eUsers=XML.getChildren(XML.getChild(eAccess,"users"));
			for (Element eUser : eUsers) {
				if ("authid".equals(eUser.getTagName())) {
					userAuthids.add(XML.getText(eUser));
				}
			}
		} catch (XMLException e) {
			// Can't happen as we just checked hasChild
			throw new OmUnexpectedException(e);
		}
		return userAuthids;
	}

	/**
	 * @return List of all admin usernames
	 */
	public List<String> getAdminUsernames() {
		ArrayList<String> adminUsernames = new ArrayList<String>();
		if(!XML.hasChild(eAccess, "admins")) return adminUsernames;
		try {
			Element[] eUsers=XML.getChildren(XML.getChild(eAccess,"admins"));
			for (Element eUser : eUsers) {
				if ("oucu".equals(eUser.getTagName()) || "username".equals(eUser.getTagName())) {
					adminUsernames.add(XML.getText(eUser));
				}
			}
		} catch (XMLException e) {
			// Can't happen as we just checked hasChild
			throw new OmUnexpectedException(e);
		}
		return adminUsernames;
	}

	/**
	 * @return List of admin authids
	 */
	public List<String> getAdminAuthids() {
		ArrayList<String> adminAuthids = new ArrayList<String>();
		if(!XML.hasChild(eAccess, "admins")) return adminAuthids;
		try {
			Element[] eUsers=XML.getChildren(XML.getChild(eAccess,"admins"));
			for (Element eUser : eUsers) {
				if ("authid".equals(eUser.getTagName())) {
					adminAuthids.add(XML.getText(eUser));
				}
			}
		} catch (XMLException e) {
			// Can't happen as we just checked hasChild
			throw new OmUnexpectedException(e);
		}
		return adminAuthids;
	}

	// Information required to support legacy OU systems

	/** @return CMA batch code */
	public String getBatch() { return sBatch; }
	/** @return OU course code */
	public String getCourseCode() { return sCourseCode; }
	/** @return Array of 2-digit assignment numbers */
	public String[] getAssignmentNumbers() { return asAssignmentNum; }
	/** @return Array of counts of questions in each numbered assignment */
	public int[] getAssignmentCounts() { return aiAssignmentCount; }
}
