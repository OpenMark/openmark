/**
 * 
 */
package om.tnavigator.reports.std;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.OmException;
import om.OmUnexpectedException;
import om.tnavigator.NavigatorServlet;
import om.tnavigator.reports.HtmlReportWriter;
import om.tnavigator.reports.OmReport;
import om.tnavigator.reports.TabularReportBase;
import om.tnavigator.reports.TabularReportWriter;
import om.tnavigator.teststructure.TestDeployment;
import om.tnavigator.util.IPAddressCheckUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.misc.UtilityException;
import util.xml.XML;
import util.xml.XMLException;

/**
 * This report lists the questions that have been deployed on this server, with the
 * available versions in reverse date order.
 */
public class DeployedTestsReport implements OmReport {
	private final static Pattern filenamePattern = Pattern.compile("^(.*)\\.deploy\\.xml$");
	private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private final static DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final static  String FILE_MISSING="File missing";

	private NavigatorServlet ns;

	/**
	 * Create an instance of this report.
	 * @param ns the navigator servlet we belong to.
	 */
	public DeployedTestsReport(NavigatorServlet ns) {
		this.ns = ns;
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmReport#getUrlReportName()
	 */
	@Override
	public String getUrlReportName() {
		return "alltests";
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmReport#handleReport(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handleReport(String suffix, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String prefix = request.getParameter("prefix");

		DeployedTestsTabularReport report = new DeployedTestsTabularReport(prefix);
		report.handleReport(request, response);
	}

	public static class Test {
		String deploy;
		String deployFileName;
		String deployModified;
		String test;
		String testModified;
		boolean open;
		boolean world;
		String openDate;
		String closeDate;
		String forbidDate;
		String feedbackDate;
		String supportcontacts;
		boolean isAssessed=false;
		boolean isArchiveable=false;
		String archiveDate="";

		public Test()
		{
			test = "";
			deployFileName="";
			testModified = "";
			open = false;
			world = false;
			openDate = "";
			closeDate = "";
			forbidDate = "";
			feedbackDate = "";
			supportcontacts = "[deploy file is invalid XML]";
			isArchiveable=false;
			archiveDate="";
			return;
		}
		
		public Test(String deploy, File deployFile, File testBank) throws OmException 
		{
			this.deploy = deploy;
			deployModified = dateTimeFormat.format(deployFile.lastModified());
			TestDeployment def;
			try {
				def = new TestDeployment(deployFile);
				initialise( def,deployFile,testBank);
				isArchiveable=isArchiveable(getYearsAsCal(-6),getYearsAsCal(-2), def);

			} catch (OmException e) {
				test = "";
				deployFileName="";
				testModified = "";
				open = false;
				world = false;
				openDate = "";
				closeDate = "";
				forbidDate = "";
				feedbackDate = "";
				supportcontacts = "[deploy file is invalid XML]";
				return;
			}
		}
		
		
		public Test(String deploy, File deployFile, File testBank,Calendar archiveDateIsAssessed, Calendar archivedateNotassessed) throws OmException 
		{
			
			this.deploy = deploy;
			deployModified = dateTimeFormat.format(deployFile.lastModified());
			TestDeployment def;
			try {
				def = new TestDeployment(deployFile);
				initialise( def,deployFile,testBank);
				isArchiveable=isArchiveable(archiveDateIsAssessed,archivedateNotassessed, def);
			} catch (OmException e) {
				test = "";
				deployFileName="";
				testModified = "";
				open = false;
				world = false;
				openDate = "";
				closeDate = "";
				forbidDate = "";
				feedbackDate = "";
				supportcontacts = "[deploy file is invalid XML]";
				return;
			}
		}

		/* get x years from now as calendar */
		private Calendar getYearsAsCal(int years)
		{
			Calendar cDate=Calendar.getInstance();
			cDate.add(Calendar.YEAR, years);
			return cDate;
		}
		
		private boolean isArchiveable(Calendar assessedDate, Calendar notAssessedDate,TestDeployment def)
		{
			
			try
			{
				Calendar cArchiveDate=notAssessedDate;
				if (def.isAssessed())
				{
					cArchiveDate=assessedDate;
				}

				archiveDate=dateFormat.format(cArchiveDate.getTime());
				boolean ia= def.isDateAfterForbid(cArchiveDate.getTimeInMillis());
				return ia;
			}
			catch (OmException e) {
				return false;
			}
		}
		
		
		public void initialise(TestDeployment def, File deployFile,File testBank) throws OmException 
		{

			if (def.isSingleQuestion()) {
				test = "[single question]";
				testModified = "";
			} else {
				test = def.getDefinition();
				File testFile = new File(testBank, test + ".test.xml");
				if (testFile.exists()) {
					testModified = dateTimeFormat.format(testFile.lastModified());
				} else {
					testModified = FILE_MISSING;
				}
			}
			deployFileName=deployFile.getAbsolutePath();
			open = def.isAfterOpen() && !def.isAfterForbid();
			world = def.isWorldAccess();
			openDate = def.displayOpenDate();
			closeDate = def.displayCloseDate();
			forbidDate = def.displayForbidDate();
			feedbackDate = def.displayFeedbackDate();
			supportcontacts = def.getSupportContacts();
			isAssessed=def.isAssessed();

		}

		private Map<String, String> toRow(boolean linkToDownloads) {
			Map<String, String> row = new HashMap<String, String>();
			row.put("deploy", deploy);
			row.put("deploy" + HtmlReportWriter.LINK_SUFFIX, "../" + deploy + "/");
			row.put("deploymodified", deployModified);
			row.put("deploymodified" + HtmlReportWriter.NOWRAP_SUFFIX, "");
			row.put("test", test);
			row.put("testmodified", testModified);
			row.put("testmodified" + HtmlReportWriter.NOWRAP_SUFFIX, "");
			if (linkToDownloads) {
				row.put("deploymodified" + HtmlReportWriter.LINK_SUFFIX, "../!deploy/" + deploy);
				if (!"".equals(testModified)&& !FILE_MISSING.equalsIgnoreCase(testModified)) {
					row.put("testmodified" + HtmlReportWriter.LINK_SUFFIX, "../!test/" + test);
				}
			}
			row.put("open", open ? "Yes" : "No");
			row.put("world", world ? "Yes" : "No");
			row.put("opendate", openDate);
			row.put("closedate", closeDate);
			row.put("forbiddate", forbidDate);
			row.put("feedbackdate", feedbackDate);
			row.put("supportcontacts", supportcontacts);
			row.put("isassessed", isAssessed ? "Yes" : "No");
			return row;
		}
		
		public String getTestName()
		{
			return deploy;
		}
		
		
		public String getTest()
		{
			return test;
		}
		
		public boolean isAssessed()
		{
			return isAssessed;
		}
		
	
		public String getDeployFileName()
		{
			return deployFileName;
		}
		
		
		public String getArchiveDate()
		{
			return archiveDate;
		}

		
		public String getDateClosed()
		{
			if(!forbidDate.isEmpty())
			{
				return forbidDate;
			}
			else if (!closeDate.isEmpty())
			{
				return closeDate;
			} else {
				return "";
			}
		}
	
		
		public boolean isArchiveable()
		{
			return isArchiveable;
		}

		public static Comparator<Test> testNameComparator 
	    = new Comparator<Test>() 
		{
		
			public int compare(Test rhs1, Test rhs2) 
			{
			String rhs1Name = rhs1.getTestName().toUpperCase();
			String rhs2Name = rhs2.getTestName().toUpperCase();
			//ascending order
			 int ret=rhs1Name.compareTo(rhs2Name);
			 return ret;
			}

		};
	}


	private class DeployedTestsTabularReport extends TabularReportBase {
		private final String prefix;
		private boolean linkToDownloads = false;

		/**
		 * Constructor.
		 * @param prefix optional prefix. If specified, only return questions whose id starts with this prefix.
		 */
		public DeployedTestsTabularReport(String prefix) {
			this.prefix = prefix;
			batchid = null;
			if (prefix == null ) {
				title = "All deployed tests";
			} else {
				title = "Deployed test with id " + prefix + "*";
			}
			this.ns = DeployedTestsReport.this.ns;
		}

		@Override
		public String getReportTagName() {
			return "tests";
		}
		@Override
		public String getRowTagName() {
			return "test";
		}

		/* (non-Javadoc)
		 * @see om.tnavigator.reports.TabularReportBase#init(javax.servlet.http.HttpServletRequest)
		 */
		@Override
		public List<ColumnDefinition> init(HttpServletRequest request) {
			try {
				linkToDownloads = IPAddressCheckUtil.checkSecureIP(
					request, ns.getLog(), ns.getNavigatorConfig());
			} catch (UtilityException e) {
				// Ingore this, we just don't show the links in this case.
			}

			List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
			columns.add(new ColumnDefinition("deploy", "Test id"));
			columns.add(new ColumnDefinition("deploymodified", "Deploy file last modified"));
			columns.add(new ColumnDefinition("test", "Test file"));
			columns.add(new ColumnDefinition("testmodified", "Test file last modified"));
			columns.add(new ColumnDefinition("open", "Open now?"));
			columns.add(new ColumnDefinition("world", "World accessible?"));
			columns.add(new ColumnDefinition("opendate", "Open date"));
			columns.add(new ColumnDefinition("closedate", "Close date"));
			columns.add(new ColumnDefinition("forbiddate", "Forbid date"));
			columns.add(new ColumnDefinition("feedbackdate", "Feedback date"));
			columns.add(new ColumnDefinition("supportcontacts", "Support contacts"));
			columns.add(new ColumnDefinition("isassessed", "Assessed?"));

			return columns;
		}

		/* (non-Javadoc)
		 * @see om.tnavigator.reports.TabularReportBase#generateReport(om.tnavigator.reports.TabularReportWriter)
		 */
		@Override
		public void generateReport(TabularReportWriter reportWriter) {
			File testBank = ns.getTestbankFolder();
			File[] deployFiles = testBank.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return filenamePattern.matcher(name).matches() && (prefix == null || name.startsWith(prefix));
				}
			});

			SortedSet<Test> tests = new TreeSet<Test>(new Comparator<Test>() {
				@Override
				public int compare(Test test1, Test test2) {
					if (test1.open && !test2.open) {
						return -1;
					} else if (!test1.open && test2.open) {
						return 1;
					} else {
						return test1.deploy.compareTo(test2.deploy);
					}
				}
			});
			for (int i = 0; i < deployFiles.length; i++) {
				File deployFile = deployFiles[i];
				Matcher m = filenamePattern.matcher(deployFile.getName());
				m.matches();
				String deployId = m.group(1);
				try {
					tests.add(new Test(deployId, deployFile, testBank));
				} catch (OmException e) {
					throw new OmUnexpectedException("Error parsing test " + deployId, e);
				}
			}

			for (Test test : tests) {
				reportWriter.printRow(test.toRow(linkToDownloads));
			}
			return;
		}

		/* (non-Javadoc)
		 * @see om.tnavigator.reports.TabularReportBase#extraHtmlContent(org.w3c.dom.Element)
		 */
		@Override
		public void extraHtmlContent(Element mainElement) {
			super.extraHtmlContent(mainElement);
			try {
				Document d = mainElement.getOwnerDocument();
				Element form = XML.getChild(XML.getChild(mainElement, "form"), "p");

				Element label = d.createElement("label");
				label.setAttribute("for", "prefix-input");
				XML.setText(label, "Prefix ");

				Element input = d.createElement("input");
				input.setAttribute("type", "text");
				input.setAttribute("size", "10");
				input.setAttribute("name", "prefix");
				input.setAttribute("id", "prefix-input");
				if (prefix != null) {
					input.setAttribute("value", prefix);
				}

				Node oldFirstChild = form.getFirstChild();
				form.insertBefore(label, oldFirstChild);
				form.insertBefore(input, oldFirstChild);
				form.insertBefore(d.createTextNode(" "), oldFirstChild);
			} catch (XMLException e) {
				throw new OmUnexpectedException("Cannot find form element.", e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmReport#isSecurityRestricted()
	 */
	@Override
	public boolean isSecurityRestricted() {
		return false;
	}
	

	
}
