package om.tnavigator.teststructure;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import om.OmFormatException;
import om.tnavigator.NavigatorServlet;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.OmQueries;
import om.tnavigator.scores.CombinedScore;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.misc.LabelSets;
import util.xml.XML;

/**
 * This class helps output a summary of a users test with one row for each question.
 *
 * It is used from the three methods processFinalTags, handleEnd and handleSummary
 * in NavigatorServlet.
 *
 * We work using the results of the following DB query:
 *     SELECT tq.questionnumber, q.finished, r.questionline, r.answerline, tq.question, r.attempts, tq.sectionname
 *
 *     FROM      testquestions tq
 *     LEFT JOIN questions     q  ON tq.question = q.question AND tq.ti = q.ti
 *     LEFT JOIN results       r  ON q.qi = r.qi
 *
 *     WHERE tq.ti = [insert ti here]
 *
 *     ORDER BY tq.questionnumber,
 *              CASE WHEN q.finished = 0 THEN 0 ELSE 1 END DESC,
 *              q.attempt DESC
 *
 * So, we have at least one row for each question in the test. There may be more
 * than one attempt at each question, but we get the most recent finished
 * attempt first, thanks to the
 * ORDER BY [...], CASE WHEN q.finished = 0 THEN 0 ELSE 1 END DESC, q.attempt DESC.
 */
public class SummaryTableBuilder {

	/** User passed on question. Should match the definition in Om.question.Results. */
	public final static int ATTEMPTS_PASS = 0;
	/** User got question wrong after all attempts. Should match the definition in om.question.Results. */
	public final static int ATTEMPTS_WRONG = -1;
	/** User got question partially correct after all attempts. Should match the definition in om.question.Results. */
	public final static int ATTEMPTS_PARTIALLYCORRECT = -2;
	/** If developer hasn't set the value. Should match the definition in om.question.Results. */
	public final static int ATTEMPTS_UNSET = -99;

	private LabelSets labelSets;

	private DatabaseAccess da;

	private OmQueries oq;

	/** Information about what should be displayed in the table. */
	private SummaryDetails sd;

	/** The &lt;table&gt; node in the output that is being built. */
	private Element eTable;

	/** The number of columns in the table. (Used as colspan on section headings.) */
	private int columnCount;

	/** The number of columns in the table that come before the score columns. */
	private int introColumnCount;

	/** Map tq.questionnumber => DataForQuestion. */
	Map<Integer, DataForQuestion> data = new LinkedHashMap<Integer, DataForQuestion>();

	/** The different score axes that need table columns, in order. */
	List<String> axes;

	private class DataForQuestion {
		String questionId;
		boolean finished;
		String sectionName;
		String questionline;
		String answerline;
		int attempts;
	}

	public SummaryTableBuilder(DatabaseAccess dba, OmQueries queries, LabelSets labelSets) {
		da = dba;
		oq = queries;
		this.labelSets = labelSets;
	}

	/**
	 * Add a summary table to a HTML DOM, based on the settings in sd.
	 * @param sd the SummaryDetails.
	 * @return database time used.
	 * @throws Exception
	 */
	public long addSummaryTable(SummaryDetails sd) throws Exception {
		this.sd = sd;

		setUpStructure();

		Long time = null;
		DatabaseAccess.Transaction dat = da.newTransaction();
		try {
			ResultSet rs = oq.querySummary(dat, sd.getDbTi());
			iterateThroughResults(rs);
			rs = null;
		} finally {
			time = dat.finish();
		}

		outputTableStart();
		outputHeaderRow();
		outputTableBody();
		if (sd.isIncludeScore()) {
			outputTotals();
		}

		return time;
	}

	/**
	 * Prepare the data structure to hold the data for the table.
	 */
	private void setUpStructure() throws OmFormatException {
		if (sd.isIncludeScore()) {
			CombinedScore ps = sd.getRootTestGroup().getFinalScore();
			axes = Arrays.asList(ps.getAxesOrdered());
		}
	}

	/**
	 * Walk through the results set, outputting the rows of the table.
	 * @param rs the ResultSet
	 */
	void iterateThroughResults(ResultSet rs)
			throws SQLException, OmFormatException, IOException {

		while (rs.next()) {
			int questionNumber = rs.getInt(1);

			DataForQuestion questionData = data.get(questionNumber);
			if (questionData == null) {
				// We have not seen this question before, create the data for it.
				questionData = new DataForQuestion();
				questionData.questionId = rs.getString(5);
				questionData.finished = rs.getInt(2) != 0;
				questionData.sectionName = rs.getString(7);
				questionData.questionline = rs.getString(3);
				questionData.answerline = rs.getString(4);
				questionData.attempts = rs.getInt(6);
				data.put(questionNumber, questionData);
			}

			// Otherwise a previous attempt at the same question. Ignore.
		}
	}

	/**
	 * Create the main table element, and the wraper for the plain mode too, if required.
	 */
	private void outputTableStart() {
		Node nTableParent = sd.getParent();

		if (sd.isPlain()) {
			nTableParent = outputPlainModeWrapperHtml(nTableParent);
		}

		eTable = XML.createChild(nTableParent, "table");
		eTable.setAttribute("class", "topheaders");
	}

	/**
	 * In plain mode, we create both a plain text summary and the table. These
	 * go in various divs, with headings, and this method creates that wrapping
	 * HTML structure.
	 * @param nTableParent parent node, where all the HTML is added.
	 */
	private Node outputPlainModeWrapperHtml(Node nTableParent) {
		Element eSkip = XML.createChild(sd.getParent(), "a");
		XML.setText(eSkip, "Skip table");
		eSkip.setAttribute("href", "#plaintable");
		Node newTableParent = XML.createChild(sd.getParent(), "div");
		Element eAnchor = XML.createChild(sd.getParent(), "a");
		eAnchor.setAttribute("name", "plaintable");
		XML.createText(sd.getParent(), "h3",
				"Text-only version of preceding table");

		eSkip = XML.createChild(sd.getParent(), "a");
		XML.setText(eSkip, "Skip text-only version");
		eSkip.setAttribute("href", "#endtable");
		Node nPlainParent = XML.createChild(sd.getParent(), "div");
		sd.setPlainParent(nPlainParent);
		eAnchor = XML.createChild(sd.getParent(), "a");
		eAnchor.setAttribute("name", "endtable");

		return newTableParent;
	}

	/**
	 * Work out which columns should be in the table, and add their headings to
	 * a row at the start of the table.
	 * @param nTableParent parent node to add the table to.
	 * @param sd the summary details.
	 * @throws OmFormatException
	 */
	private void outputHeaderRow() throws OmFormatException {

		Element currentRow = XML.createChild(eTable, "tr");
		columnCount = 1;
		introColumnCount = 1;

		// Question number column heading.
		Element eTH = XML.createChild(currentRow, "th");
		eTH.setAttribute("scope", "col");
		Element eAbbr = XML.createChild(eTH, "abbr");
		XML.createText(eAbbr, "#");
		eAbbr.setAttribute("title", "Question number");

		// Question and answer column headings.
		if (sd.isIncludeQuestions()) {
			eTH = XML.createChild(currentRow, "th");
			eTH.setAttribute("scope", "col");
			XML.createText(eTH, "Question");
			eTH = XML.createChild(currentRow, "th");
			eTH.setAttribute("scope", "col");
			XML.createText(eTH, "Your answer");
			columnCount += 2;
			introColumnCount += 2;
		}

		// Result column.
		if (sd.isIncludeAttempts()) {
			eTH = XML.createChild(currentRow, "th");
			eTH.setAttribute("scope", "col");
			XML.createText(eTH, "Result");
			columnCount += 1;
			introColumnCount += 1;
		}

		// Score columns.
		if (sd.isIncludeScore()) {
			for (String sAxis : axes) {
				eTH = XML.createChild(currentRow, "th");
				eTH.setAttribute("scope", "col");
				XML.createText(eTH, "Marks" + (sAxis == null ? "" : " (" + sAxis + ")"));
				eTH = XML.createChild(currentRow, "th");
				eTH.setAttribute("scope", "col");
				XML.createText(eTH, "Out of");
				columnCount += 2;
			}
		}
	}

	private void outputTableBody() throws OmFormatException, IOException {
		String lastSectionNameOutput = null;

		int displayedQuestionNumber = 1;
		for (Map.Entry<Integer, DataForQuestion> entry : data.entrySet()) {
			DataForQuestion questionData = entry.getValue();

			if (questionData.sectionName != null && !questionData.sectionName.equals(lastSectionNameOutput)) {
				outputSectionRow(questionData.sectionName);
				lastSectionNameOutput = questionData.sectionName;
				if (sd.isNumberBySection()) {
					displayedQuestionNumber = 1;
				}
			}
			outputQuestionRow(displayedQuestionNumber, questionData);
			displayedQuestionNumber += 1;
		}
	}

	/**
	 * Add a section heading row.
	 * @param sd the SummaryDetails
	 * @param dd the DisplayDetails
	 * @param sCurrentSection
	 * @return
	 */
	private void outputSectionRow(String sectionName) {
		Element eTR = XML.createChild(eTable, "tr");
		eTR.setAttribute("class", "sectionname");
		Element eTD = XML.createChild(eTR, "td");
		eTD.setAttribute("colspan", "" + columnCount);
		XML.createText(eTD, sectionName);

		if (sd.isPlain()) {
			XML.createText(sd.getPlainParent(), "h4", sectionName);
		}
	}

	private void outputQuestionRow(int displayedQuestionNumber, DataForQuestion questionData)
			throws IOException, OmFormatException {
		// Create the tr.
		Element currentRow = XML.createChild(eTable, "tr");
		if (questionData.finished) {
			currentRow.setAttribute("class", "answered");
		} else {
			currentRow.setAttribute("class", "unanswered");
		}

		// Output the question number.
		XML.createText(currentRow, "td", "" + displayedQuestionNumber);
		Element ePlainRow = null;
		if (sd.isPlain()) {
			ePlainRow = XML.createChild(sd.getPlainParent(), "div");
			XML.createText(ePlainRow, "Question " + displayedQuestionNumber + ". ");
		}

		// Output the question text and answer text.
		if (sd.isIncludeQuestions()) {
			if (questionData.finished) {
				XML.createText(currentRow, "td", questionData.questionline);
				XML.createText(currentRow, "td", questionData.answerline);
				if (sd.isPlain()) {
					XML.createText(ePlainRow, "Question: " + questionData.questionline +
							". Your answer: " + questionData.answerline + ". ");
				}
			} else {
				XML.createText(currentRow, "td", "-");
				XML.createText(currentRow, "td", "-");
			}
		}

		// Information about the question state.
		if (sd.isIncludeAttempts()) {
			String sAttempts;
			if (questionData.finished) {
				sAttempts = NavigatorServlet.getAttemptsString(questionData.attempts,
						sd.getTestDefinition(), labelSets);
			} else {
				sAttempts = "Not completed";
			}
			XML.createText(currentRow, "td", sAttempts);
			if (sd.isPlain()) {
				XML.createText(ePlainRow, "Result: " + sAttempts + ". ");
			}
		}

		if (sd.isIncludeScore()) {
			if (questionData.finished) {
				outputQuestionScores(currentRow, ePlainRow, questionData.questionId);
			} else {
				for (@SuppressWarnings("unused") String axis : axes) {
					XML.createText(currentRow, "td", "");
					XML.createText(currentRow, "td", "");
				}
			}
		}
	}

	/**
	 * Add the scores for a question.
	 * @param currentRow the table row we are building.
	 * @param ePlainRow if in plain mode, the place to add scores in the plain display.
	 * @param sQuestion the question id to display scores for.
	 * @throws OmFormatException
	 */
	private void outputQuestionScores(Element currentRow, Element ePlainRow,
			String questionId) throws OmFormatException {

		// Find question.
		TestQuestion tq = null;
		for (int i = 0; i < sd.getTestLeavesInOrder().length; i++) {
			if (sd.getTestLeavesInOrder()[i] instanceof TestQuestion) {
				TestQuestion tempTQ = (TestQuestion) sd.getTestLeavesInOrder()[i];
				if (tempTQ.getID().equals(questionId)) {
					tq = (TestQuestion)sd.getTestLeavesInOrder()[i];
					break;
				}
			}
		}

		// Get score (scaled).
		CombinedScore ps = tq.getScoreContribution(sd.getRootTestGroup());
		for (String axis : axes) {
			String score = formatScore(ps.getScore(axis));
			String max = formatScore(ps.getMax(axis));

			XML.createText(currentRow, "td", score);
			XML.createText(currentRow, "td", max);

			if (ePlainRow != null) {
				XML.createText(ePlainRow, "Marks" +
					(axis == null ? "" : " (" + axis + ")") +
					": " + score + ". Out of: " + max + ". ");
			}
		}
	}

	/**
	 * Add a row with the total scores, if it should be there.
	 */
	private void outputTotals() throws OmFormatException {

		CombinedScore ps = sd.getRootTestGroup().getFinalScore();

		Element currentRow = XML.createChild(eTable, "tr");
		currentRow.setAttribute("class", "totals");
		Element eTD = XML.createChild(currentRow, "td");
		eTD.setAttribute("colspan", "" + introColumnCount);
		XML.createText(eTD, "Total");

		Element ePlainRow = null;
		if (sd.isPlain()) {
			ePlainRow = XML.createChild(sd.getPlainParent(), "div");
			XML.createText(ePlainRow, "Totals: ");
		}

		for (String sAxis : axes) {
			String score = formatScore(ps.getScore(sAxis));
			String max = formatScore(ps.getMax(sAxis));

			XML.createText(currentRow, "td", score);
			XML.createText(currentRow, "td", max);

			if (ePlainRow != null) {
				XML.createText(ePlainRow, "Marks"
						+ (sAxis == null ? "" : " (" + sAxis + ")")
						+ ": " + score + ". Out of: " + max + ". ");
			}
		}
	}

	/**
	 * Convert a score to a string for display.
	 * @param dScore a score.
	 * @return The score formatted as a String.
	 */
	private static String formatScore(double dScore) {
		if (Math.abs(dScore - Math.round(dScore)) < 0.001) {
			return (int) Math.round(dScore) + "";
		} else {
			NumberFormat nf = DecimalFormat.getNumberInstance();
			nf.setMaximumFractionDigits(1);
			return nf.format(dScore);
		}
	}
}
