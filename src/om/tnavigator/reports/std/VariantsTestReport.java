/**
 *
 */
package om.tnavigator.reports.std;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.OmException;
import om.OmUnexpectedException;
import om.tnavigator.*;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.reports.*;

import org.w3c.dom.Element;

/**
 * This report analyses different variants of each question, and gives the number
 * of students who was shown each variant, and their average scores. This gives
 * some indication of whether the different variants are equally difficult. It relies
 * on question authors adding "Variant = 123" somewhere in the questionline. Actually, 
 * the regular expression searched for is "[Vv]ariant\s*=\s*(\d+)".
 */
public class VariantsTestReport implements OmTestReport {
	private final static Pattern variantRegexp = Pattern.compile("[Vv]ariant\\s*=\\s*(\\d+)");
	private final static NumberFormat fixedFormat = new DecimalFormat("00000000");
	private NavigatorServlet ns;

	/**
	 * Create an instance of this report.
	 * @param ns the navigator servlet we belong to.
	 */
	public VariantsTestReport(NavigatorServlet ns) {
		this.ns = ns;
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#getUrlTestReportName()
	 */
	public String getUrlTestReportName() {
		return "variants";
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#getReadableReportName()
	 */
	public String getReadableReportName() {
		return "Question variants report";
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#isApplicable(om.tnavigator.TestDeployment)
	 */
	public boolean isApplicable(TestDeployment td) {
		return true;
	}

	private class VariantsTabularReport extends TabularReportBase {
		private int overallMaxVariant = Integer.MIN_VALUE;
		private int overallMinVariant = Integer.MAX_VALUE;
		private boolean hasUnknown = false;
		private SortedMap<String, QuestionData> questionData = new TreeMap<String, QuestionData>();

		private class QuestionData {
			private final static String UNKNOWNVARIANT = "unknown";
			private int position;
			private String questionid;
			private int maxVariant = Integer.MIN_VALUE;
			private int minVariant = Integer.MAX_VALUE;
			private Map<String, Integer> numAttemptsPerVariant = new HashMap<String, Integer>();
			private Map<String, Double> totalScorePerVariant = new HashMap<String, Double>();
			private QuestionData(int position, String questionid) {
				this.position = position;
				this.questionid = questionid;
			}
			private void recordVariantScore(Integer variant, double score) {
				String sVariant;
				if (variant != null) {
					sVariant = variant + "";
					if (variant > maxVariant) {
						maxVariant = variant;
					}
					if (variant > overallMaxVariant) {
						overallMaxVariant = variant;
					}
					if (variant < minVariant) {
						minVariant = variant;
					}
					if (variant < overallMinVariant) {
						overallMinVariant = variant;
					}
				} else {
					sVariant = UNKNOWNVARIANT;
					hasUnknown = true;
				}
				if (numAttemptsPerVariant.containsKey(sVariant)) {
					numAttemptsPerVariant.put(sVariant, numAttemptsPerVariant.get(sVariant) + 1);
					totalScorePerVariant.put(sVariant, totalScorePerVariant.get(sVariant) + score);
				} else {
					numAttemptsPerVariant.put(sVariant, 1);
					totalScorePerVariant.put(sVariant, score);
				}
			}
			private String getCount(String variant) {
				if (numAttemptsPerVariant.containsKey(variant)) {
					return numAttemptsPerVariant.get(variant) + "";
				} else {
					return "0";
				}
			}
			private String getAverage(String variant) {
				if (numAttemptsPerVariant.containsKey(variant)) {
					return (totalScorePerVariant.get(variant) / numAttemptsPerVariant.get(variant)) + "";
				} else {
					return "-";
				}
			}
			private Map<String, String> toRow() {
				Map<String, String> row = new HashMap<String, String>();
				row.put("testposition", position + "");
				row.put("questionid", questionid);
				if (minVariant > maxVariant) {
					// This was a question where all the answers were of an unknown variant.
					minVariant = overallMinVariant;
					maxVariant = overallMinVariant - 1;
				}
				for (int i = overallMinVariant; i < minVariant; ++i) {
					row.put("variant" + i + "count", getCount(""));
					row.put("variant" + i + "average", getAverage(""));
				}
				for (int i = minVariant; i <= maxVariant; ++i) {
					row.put("variant" + i + "count", getCount(i + ""));
					row.put("variant" + i + "average", getAverage(i + ""));
				}
				for (int i = maxVariant + 1; i <= overallMaxVariant; ++i) {
					row.put("variant" + i + "count", getCount(""));
					row.put("variant" + i + "average", getAverage(""));
				}
				if (hasUnknown) {
					row.put("unknownvariantcount", getCount(UNKNOWNVARIANT));
					row.put("unknownvariantaverage", getAverage(UNKNOWNVARIANT));
				}
				return row;
			}
		}

		VariantsTabularReport(String testId) throws OmException {
			this.ns = VariantsTestReport.this.ns;
			batchid = null;
			title = "Variants report for test " + testId;

			// Query from database for PIs and questions
			DatabaseAccess.Transaction dat;
			try {
				dat = ns.getDatabaseAccess().newTransaction();
			} catch (SQLException e1) {
				throw new OmUnexpectedException("Cannot connect to the database");
			}
			try
			{
				ResultSet rs = ns.getOmQueries().queryVariantReport(dat, testId);
				while(rs.next())
				{
					int position = rs.getInt(1);
					String questionid = rs.getString(2);
					String questionLine = rs.getString(3);
					double score = rs.getInt(4);

					String key = fixedFormat.format(position) + "." + questionid;
					if (!questionData.containsKey(key)) {
						questionData.put(key, new QuestionData(position, questionid));
					}

					Integer variant;
					Matcher m = variantRegexp.matcher(questionLine);
					if (m.find()) {
						variant = Integer.parseInt(m.group(1));
					} else {
						variant = null;
					}

					questionData.get(key).recordVariantScore(variant, score);

				}
			} catch (Exception e) {
				throw new OmUnexpectedException("Error generating report.", e);
			}
			finally
			{
				dat.finish();
			}
		}

		/* (non-Javadoc)
		 * @see om.tnavigator.reports.TabularReportBase#init(javax.servlet.http.HttpServletRequest)
		 */
		@Override
		public List<ColumnDefinition> init(HttpServletRequest request) {
			List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
			columns.add(new ColumnDefinition("testposition", "Question #"));
			columns.add(new ColumnDefinition("questionid", "Question id"));
			for (int i = overallMinVariant; i <= overallMaxVariant; ++i) {
				columns.add(new ColumnDefinition("variant" + i + "count", "Variant " + i + " count"));
				columns.add(new ColumnDefinition("variant" + i + "average", "Variant " + i + " average"));
			}
			if (hasUnknown) {
				columns.add(new ColumnDefinition("unknownvariantcount", "Unknown variant count"));
				columns.add(new ColumnDefinition("unknownvariantaverage", "Unknown variant average"));
			}
			return columns;
		}

		/* (non-Javadoc)
		 * @see om.tnavigator.reports.TabularReportBase#generateReport(om.tnavigator.reports.TabularReportWriter)
		 */
		@Override
		public void generateReport(TabularReportWriter reportWriter) {
			for (QuestionData qd : questionData.values()) {
				reportWriter.printRow(qd.toRow());
			}
		}

		@Override
		public void extraHtmlContent(Element mainElement) {
			super.extraHtmlContent(mainElement);
			printMessage("This report only makes sense if the questions in your test include " +
					"'Variant = NN' somewhere in the 'questionline' for each question.", mainElement);
			printMessage("This report only counts non-admin attempts.", mainElement);
		}
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmTestReport#handleTestReport(om.tnavigator.UserSession, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void handleTestReport(UserSession us, String suffix,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		VariantsTabularReport report = new VariantsTabularReport(us.getTestId());
		report.handleReport(request, response);
	}
}