/**
 * 
 */
package om.tnavigator.reports.std;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.*;

import om.OmUnexpectedException;
import om.tnavigator.NavigatorServlet;
import om.tnavigator.reports.*;
import util.misc.ReverseComparator;
import util.misc.Strings;
import util.xml.XML;
import util.xml.XMLException;

/**
 * This report lists the questions that have been deployed on this server, with the
 * available versions in reverse date order.
 */
public class DeployedQuestionsReport implements OmReport {
	private final static String partRegexp = "[_a-z][_a-zA-Z0-9]*";
	private final static Pattern filenamePattern = Pattern.compile("^(" + partRegexp + "(?:\\." + partRegexp + ")*)\\.(\\d+\\.\\d+)\\.jar$");
	private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private NavigatorServlet ns;

	/**
	 * Create an instance of this report.
	 * @param ns the navigator servlet we belong to.
	 */
	public DeployedQuestionsReport(NavigatorServlet ns) {
		this.ns = ns;
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmReport#getUrlReportName()
	 */
	@Override
	public String getUrlReportName() {
		return "allquestions";
	}

	/* (non-Javadoc)
	 * @see om.tnavigator.reports.OmReport#handleReport(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handleReport(String suffix, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String prefix = request.getParameter("prefix");

		DeployedQuestionsTabularReport report = new DeployedQuestionsTabularReport(prefix);
		report.handleReport(request, response);
	}

	private static class Question {
		String questionId;
		String latestVersion;
		long lastModified;
		SortedSet<String> otherVersions = new TreeSet<String>(new ReverseComparator<String>());

		private Question(String questionId, String questionVersion, File questionFile) {
			this.questionId = questionId;
			this.latestVersion = questionVersion;
			this.lastModified = questionFile.lastModified();
		}

		private void update(String questionVersion, File questionFile) {
			if (questionVersion.compareTo(latestVersion) < 0) {
				otherVersions.add(questionVersion);
			} else {
				otherVersions.add(latestVersion);
				latestVersion = questionVersion;
				lastModified = questionFile.lastModified();
			}
		}

		private Map<String, String> toRow() {
			Map<String, String> row = new HashMap<String, String>();
			row.put("questionid", questionId);
			row.put("latestversion", latestVersion);
			row.put("lastupdated", dateFormat.format(lastModified));
			row.put("otherversions", Strings.join(", ", otherVersions));
			return row;
		}
	}

	private class DeployedQuestionsTabularReport extends TabularReportBase {
		private final String prefix;

		/**
		 * Constructor.
		 * @param prefix optional prefix. If specified, only return questions whose id starts with this prefix.
		 */
		public DeployedQuestionsTabularReport(String prefix) {
			this.prefix = prefix;
			batchid = null;
			if (prefix == null ) {
				title = "All deployed questions";
			} else {
				title = "Deployed questions with id " + prefix + "*";
			}
			this.ns = DeployedQuestionsReport.this.ns;
		}

		/* (non-Javadoc)
		 * @see om.tnavigator.reports.TabularReportBase#init(javax.servlet.http.HttpServletRequest)
		 */
		@Override
		public List<ColumnDefinition> init(HttpServletRequest request) {
			List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
			columns.add(new ColumnDefinition("questionid", "Question ID"));
			columns.add(new ColumnDefinition("latestversion", "Latest version"));
			columns.add(new ColumnDefinition("lastupdated", "Last updated"));
			columns.add(new ColumnDefinition("otherversions", "Other versions"));
			return columns;
		}

		/* (non-Javadoc)
		 * @see om.tnavigator.reports.TabularReportBase#generateReport(om.tnavigator.reports.TabularReportWriter)
		 */
		@Override
		public void generateReport(TabularReportWriter reportWriter) {
			File questionBank = ns.getQuestionbankFolder();
			File[] questionFiles = questionBank.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return filenamePattern.matcher(name).matches() && (prefix == null || name.startsWith(prefix));
				}
			});

			SortedMap<String, Question> questions = new TreeMap<String, Question>();
			for (int i = 0; i < questionFiles.length; i++) {
				File questionFile = questionFiles[i];
				Matcher m = filenamePattern.matcher(questionFile.getName());
				m.matches();
				String questionId = m.group(1);
				String questionVersion = m.group(2);
				Question question = questions.get(questionId);
				if (question != null) {
					question.update(questionVersion, questionFile);
				} else {
					questions.put(questionId, new Question(questionId, questionVersion, questionFile));
				}
			}

			for (Question question : questions.values()) {
				reportWriter.printRow(question.toRow());
			}
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
}
