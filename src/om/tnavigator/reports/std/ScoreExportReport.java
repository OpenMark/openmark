/**
 *
 */
package om.tnavigator.reports.std;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.OmException;
import om.OmUnexpectedException;
import om.tnavigator.NavigatorServlet;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.reports.OmReport;
import om.tnavigator.reports.OmTestReport;
import om.tnavigator.reports.TabularReportBase;
import om.tnavigator.reports.TabularReportWriter;
import om.tnavigator.scores.CombinedScore;
import om.tnavigator.sessions.UserSession;
import om.tnavigator.teststructure.CachingQuestionMetadataSource;
import om.tnavigator.teststructure.QuestionMetadataSource;
import om.tnavigator.teststructure.TestDefinition;
import om.tnavigator.teststructure.TestDeployment;
import om.tnavigator.teststructure.TestRealisation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.xml.XML;
import util.xml.XMLException;

/**
 * This report exports test scores in the format expected by the Moodle &gt;=1.9 gradebook.
 */
public class ScoreExportReport implements OmTestReport, OmReport
{
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private static final DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private NavigatorServlet ns;

	/**
	 * Create an instance of this report.
	 * @param ns the navigator servlet we belong to.
	 */
	public ScoreExportReport(NavigatorServlet ns)
	{
		this.ns = ns;
	}

	@Override
	public String getUrlTestReportName()
	{
		return "scores";
	}

	@Override
	public String getReadableReportName()
	{
		return "Export scores";
	}

	@Override
	public boolean isApplicable(TestDeployment td)
	{
		return true;
	}

	@Override
	public String getUrlReportName()
	{
		return "scores";
	}

	private class ScoreExportTabularReport extends TabularReportBase
	{
		private String testId;
		private TestDefinition def;
		private Calendar reportStartDate;
		private Calendar reportEndDate;
		private LinkedHashMap<String, String> days;
		private LinkedHashMap<String, String> months;
		private LinkedHashMap<String, String> years;

		ScoreExportTabularReport(String testId, TestDeployment deploy,
				Calendar reportStartDate, Calendar reportEndDate) throws OmException
		{

			if (reportStartDate == null)
			{
				reportStartDate = Calendar.getInstance();
				reportStartDate.add(Calendar.DAY_OF_MONTH, -1);
			}
			reportStartDate.set(Calendar.HOUR_OF_DAY, 0);
			reportStartDate.set(Calendar.MINUTE, 0);
			reportStartDate.set(Calendar.SECOND, 0);
			reportStartDate.set(Calendar.MILLISECOND, 0);

			if (reportEndDate == null)
			{
				reportEndDate = Calendar.getInstance();
			}
			reportEndDate.set(Calendar.HOUR_OF_DAY, 23);
			reportEndDate.set(Calendar.MINUTE, 59);
			reportEndDate.set(Calendar.SECOND, 59);
			reportEndDate.set(Calendar.MILLISECOND, 999);

			this.testId = testId;
			this.def = deploy.getTestDefinition();
			this.ns = ScoreExportReport.this.ns;
			this.reportStartDate = reportStartDate;
			this.reportEndDate = reportEndDate;
			title = testId + " score export";
			batchid = testId + "-" + dateFormat.format(reportStartDate.getTime()) + "-" +
					dateFormat.format(reportEndDate.getTime());
		}

		protected void initialiseDateOptions()
		{
			days = new LinkedHashMap<String, String>(31);
			for (int i = 1; i <= 31; ++i)
			{
				days.put("" + i, "" + i);
			}

			String[] monthNames = {"January", "February", "March", "April", "May", "June",
					"July", "August", "September", "October", "November", "December"};
			months = new LinkedHashMap<String, String>(31);
			for (int i = 0; i < monthNames.length; i++)
			{
				months.put("" + (i + 1), monthNames[i]);
			}

			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			years = new LinkedHashMap<String, String>();
			for (int i = 2005; i <= currentYear; i++)
			{
				years.put("" + i, "" + i);
			}
		}

		@Override
		public void extraHtmlContent(Element mainElement)
		{
			initialiseDateOptions();
			super.extraHtmlContent(mainElement);
			try
			{
				Element form = XML.getChild(mainElement, "form");

				// New fields are added at the top of the form, so add them backwards.
				outputDateSelector(form, "end", " End ", reportEndDate);
				outputDateSelector(form, "start", " Start ", reportStartDate);

				Element hidden = XML.createChild(form, "input");
				hidden.setAttribute("type", "hidden");
				hidden.setAttribute("name", "test");
				hidden.setAttribute("value", testId);
			}
			catch (XMLException e)
			{
				throw new OmUnexpectedException("Cannot find form element.", e);
			}
		}

		protected void outputDateSelector(Element parent, String name, String label, Calendar currentValue)
		{
			Document d = parent.getOwnerDocument();

			Element p = d.createElement("p");
			parent.insertBefore(p, parent.getFirstChild());

			outputSelect(p, name + "d", label + " day", "" + currentValue.get(Calendar.DAY_OF_MONTH), days);
			outputSelect(p, name + "m", label + " month", "" + (currentValue.get(Calendar.MONTH) + 1), months);
			outputSelect(p, name + "y", label + " year", "" + currentValue.get(Calendar.YEAR), years);
		}

		protected void outputSelect(Element parent, String name, String labelText,
				String currentValue, LinkedHashMap<String, String> options)
		{
			Element label = XML.createChild(parent, "label");
			label.setAttribute("for", name + "-select");
			XML.setText(label, labelText);

			Element select = XML.createChild(parent, "select");
			select.setAttribute("name", name);
			select.setAttribute("id", name + "-input");

			for (Map.Entry<String, String> option : options.entrySet())
			{
				Element optionElement = XML.createChild(select, "option");
				optionElement.setAttribute("value", option.getKey());
				XML.createText(optionElement, option.getValue());
				if (option.getKey().equals(currentValue))
				{
					optionElement.setAttribute("selected", "selected");
				}
			}
		}

		@Override
		public List<ColumnDefinition> init(HttpServletRequest request)
		{
			List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
			columns.add(new ColumnDefinition("student", "Student"));
			columns.add(new ColumnDefinition("oucu", "OUCU"));
			columns.add(new ColumnDefinition("starttime", "Start time"));
			columns.add(new ColumnDefinition("finishtime", "Submit time"));
			columns.add(new ColumnDefinition("score", "Score"));
			columns.add(new ColumnDefinition("axis", "Axis"));
			return columns;
		}

		private void outputRowForPI(Map<String, String> baseRow, CombinedScore score, TabularReportWriter reportWriter)
		{
			try
			{
				// Output a row of the report for each axis.
				for (String axis : score.getAxesOrdered())
				{
					Map<String, String> row = new HashMap<String, String>(baseRow);
					row.put("score", score.getScore(axis) + "");
					row.put("axis", axis == null ? "" : axis);
					reportWriter.printRow(row);
				}
			}
			catch (Exception e)
			{
				throw new OmUnexpectedException("Error outputting report.", e);
			}
		}

		@Override
		public void generateReport(TabularReportWriter reportWriter)
		{
			QuestionMetadataSource metadataSource = new CachingQuestionMetadataSource(ns);
			DatabaseAccess.Transaction dat = null;
			try
			{
				dat = ns.getDatabaseAccess().newTransaction();

				// Get all the data for all the attempts by each user. The results
				// are sorted by user, and then attempt number.
				// We will loop through the attempts for each user, and keep the
				// one with the highest score. (So, if there are two with equal
				// score, we take the first.)
				ResultSet rs = ns.getOmQueries().queryScoresForAttemptsAtTestByDate(dat, testId, reportStartDate, reportEndDate);

				// Because Java ResultSets are stupid (you cannot rely on isAfterLast) we
				// adopt the convention that the it is closed as soon at rs.next() returns false.
				if (!rs.next())
				{
					rs.close();
				}

				String currentPi = "";
				double bestScore = -1;
				CombinedScore allScoresForBest = null;
				Map<String, String> baseRow = new HashMap<String, String>();
				while (!rs.isClosed())
				{
					String pi = rs.getString(9);

					// If PI has changed, then .
					if (!"".equals(currentPi) && !pi.equals(currentPi))
					{
						// We need to output the last student if they finished.
						outputRowForPI(baseRow, allScoresForBest, reportWriter);
						bestScore = -1;
						allScoresForBest = null;
						baseRow = new HashMap<String, String>();
					}
					currentPi = pi;

					// Now we get on with checking this student.
					int ti = rs.getInt(8);
					long randomSeed = rs.getLong(10);
					int fixedVariant = rs.getInt(11);
					String oucu = rs.getString(12);
					Timestamp startTime = rs.getTimestamp(13);
					Timestamp finishTime = rs.getTimestamp(14);

					// Create TestRealisation
					TestRealisation testRealisation = TestRealisation.realiseTest(
							def, randomSeed, fixedVariant, testId, ti);

					// Use it to get the score.
					CombinedScore score = testRealisation.getScoreFromResultSet(
							new NavigatorServlet.RequestTimings(), rs, metadataSource);

					// If this attempt is better, remember it.
					double thisScore = score.getScoreForFirstAxis();
					if (thisScore > bestScore)
					{
						bestScore = thisScore;
						allScoresForBest = score;
						baseRow.put("pi", currentPi);
						baseRow.put("oucu", oucu);
						baseRow.put("starttime", formatDate(startTime));
						baseRow.put("finishtime", formatDate(finishTime));
					}
				}

				if (allScoresForBest != null)
				{
					outputRowForPI(baseRow, allScoresForBest, reportWriter);
				}

			}
			catch (Exception e)
			{
				throw new OmUnexpectedException("Error generating report.", e);
			}
			finally
			{
				if (dat != null)
				{
					dat.finish();
				}
			}
		}

		/**
		 * Convert a Timestamp to a string for display.
		 * @param ts the timestamp from the dataabse (may be null).
		 * @return the formatted string.
		 */
		protected String formatDate(Timestamp ts)
		{
			if (ts == null)
			{
				return "";
			}
			else
			{
				return timeFormat.format(ts);
			}
		}
	}

	@Override
	public void handleTestReport(UserSession us, String suffix,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{

		Calendar reportStartDate = getDateParameter(request, "start");
		Calendar reportEndDate = getDateParameter(request, "end");

		ScoreExportTabularReport report = new ScoreExportTabularReport(us.getTestId(),
				us.getTestDeployment(),reportStartDate, reportEndDate);
		report.handleReport(request, response);
	}

	@Override
	public void handleReport(String suffix, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String testId = request.getParameter("test");
		TestDeployment deploy = new TestDeployment(ns.pathForTestDeployment(testId));

		Calendar reportStartDate = getDateParameter(request, "start");
		Calendar reportEndDate = getDateParameter(request, "end");

		ScoreExportTabularReport report = new ScoreExportTabularReport(testId,
				deploy, reportStartDate, reportEndDate);
		report.handleReport(request, response);
	}

	protected Calendar getDateParameter(HttpServletRequest request, String name)
	{
		String year = request.getParameter(name + "y");
		String month = request.getParameter(name + "m");
		String day = request.getParameter(name + "d");
		if (year == null || month == null || day == null)
		{
			return null;
		}

		try
		{
			Calendar value = Calendar.getInstance();
			value.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day));
			return value;
		}
		catch(NumberFormatException e)
		{
			return null;
		}
	}

	@Override
	public boolean isSecurityRestricted()
	{
		return true;
	}
}
