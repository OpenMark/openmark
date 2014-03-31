package om.tnavigator.teststructure;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import om.OmFormatException;
import om.tnavigator.NavigatorServlet;
import om.tnavigator.db.DatabaseAccess;
import om.tnavigator.db.OmQueries;
import om.tnavigator.scores.CombinedScore;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.misc.LabelSets;
import util.xml.XML;

public class SummaryTableBuilder {

	/** User passed on question. Should match the definition in Om.question.Results. */
	public final static int ATTEMPTS_PASS=0;
	/** User got question wrong after all attempts. Should match the definition in om.question.Results. */
	public final static int ATTEMPTS_WRONG=-1;
	/** User got question partially correct after all attempts. Should match the definition in om.question.Results. */
	public final static int ATTEMPTS_PARTIALLYCORRECT=-2;
	/** If developer hasn't set the value. Should match the definition in om.question.Results. */
	public final static int ATTEMPTS_UNSET=-99;

	private static final String SUMMARYTABLE_NOTANSWERED="Not completed";

	private LabelSets labelSets;

	private DatabaseAccess da;

	private OmQueries oq;

	public SummaryTableBuilder(DatabaseAccess dba, OmQueries queries, LabelSets labelSets) {
		da = dba;
		oq = queries;
		this.labelSets = labelSets;
	}

	public long addSummaryTable(SummaryDetails sd) throws Exception {
		Node nTableParent = null;
		// check whether too renumber within section
		if (sd.isPlain()) {
			nTableParent = XML.createChild(sd.getParent(), "div");
			plain(sd, nTableParent);
		} else {
			nTableParent = sd.getParent();
		}
		createBasicTable(nTableParent, sd);
		return applySummary(sd);
	}

	private void plain(SummaryDetails sd, Node nTableParent) {
		Element eSkip = XML.createChild(sd.getParent(), "a");
		XML.setText(eSkip, "Skip table");
		eSkip.setAttribute("href", "#plaintable");
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
	}

	private void createBasicTable(Node nTableParent, SummaryDetails sd)
		throws OmFormatException {
		TableComponents tableComponents = new TableComponents();
		tableComponents.tableParent = nTableParent;
		tableComponents.eTable = XML.createChild(nTableParent, "table");
		tableComponents.eTable.setAttribute("class", "topheaders");
		tableComponents.eTR = XML.createChild(tableComponents.eTable, "tr");
		Element eTH = XML.createChild(tableComponents.eTR, "th");
		eTH.setAttribute("scope", "col");
		Element eAbbr = XML.createChild(eTH, "abbr");
		XML.createText(eAbbr, "#");
		eAbbr.setAttribute("title", "Question number");
		if (sd.isIncludeQuestions()) {
			eTH = XML.createChild(tableComponents.eTR, "th");
			eTH.setAttribute("scope", "col");
			XML.createText(eTH, "Question");
			eTH = XML.createChild(tableComponents.eTR, "th");
			eTH.setAttribute("scope", "col");
			XML.createText(eTH, "Your answer");
		}
		if (sd.isIncludeAttempts()) {
			eTH = XML.createChild(tableComponents.eTR, "th");
			eTH.setAttribute("scope", "col");
			XML.createText(eTH, "Result");
		}
		if (sd.isIncludeScore()) {
			CombinedScore ps = sd.getRootTestGroup().getFinalScore();
			tableComponents.asAxes = ps.getAxesOrdered();
			for (int iAxis = 0; iAxis < tableComponents.asAxes.length; iAxis++) {
				String sAxis = tableComponents.asAxes[iAxis];
				eTH = XML.createChild(tableComponents.eTR, "th");
				eTH.setAttribute("scope", "col");
				XML.createText(eTH, "Marks"
						+ (sAxis == null ? "" : " (" + sAxis + ")"));
				eTH = XML.createChild(tableComponents.eTR, "th");
				eTH.setAttribute("scope", "col");
				XML.createText(eTH, "Out of");
			}
		}
		sd.setTableComponents(tableComponents);
	}

	class TableComponents {
		String[] asAxes;
		Element eTable;
		Element eTR;
		Node tableParent;
	}

	class DisplayDetails {
		int iCurrentQuestion = 1;
		int iOutputCurrentQuestionNumber = 0;
		int iMaxQuestion = 0;
		String sLastQuestion = null;
		String sDisplayedSection = null; // Section heading that has already been displayed
		String sPreviousSection = null; // Section of last row (null if none)
		String sSection;  // pulled from the db in the iteration.
	}

	Long applySummary(SummaryDetails sd)
		throws SQLException, OmFormatException, IOException {
		Long time = null;
		// Query for questions and answers
		DatabaseAccess.Transaction dat = da.newTransaction();
		try {
			ResultSet rs = oq.querySummary(dat, sd.getDbTi());
			DisplayDetails dd = new DisplayDetails();
			iterateResults(sd, rs, dd);
			// If we didn't do the last one, put that out
			if (dd.iCurrentQuestion <= dd.iMaxQuestion) {
				dd.iOutputCurrentQuestionNumber++;
				applyLast(sd, dd, dd.sDisplayedSection);
			}
			includeScore(sd);
		} finally {
			//rt.lDatabaseElapsed += dat.finish();
			time = dat.finish();
		}
		return time;
	}

	void iterateResults(SummaryDetails sd, ResultSet rs, DisplayDetails dd)
			throws SQLException, OmFormatException, IOException {
		while (rs.next()) {
			// Keep track of max number
			int iQuestionNumber = rs.getInt(1);
			dd.iMaxQuestion = Math.max(dd.iMaxQuestion, iQuestionNumber);

			// Ignore answers after we're looking for next question
			if (iQuestionNumber < dd.iCurrentQuestion)
				continue;
			// Get section
			dd.sSection = rs.getString(7);
			boolean checkForNumberingRestart = sd.isNumberBySection()
				&& !(dd.sPreviousSection == null || dd.sPreviousSection
					.equals(dd.sDisplayedSection));
			if (checkForNumberingRestart) {
				dd.iOutputCurrentQuestionNumber = 0;
			}
			if (iQuestionNumber > dd.iCurrentQuestion) {
				// If we didn't put out an answer for current question,
				// chuck one out now
				dd.iOutputCurrentQuestionNumber++;
				applyAnswerForCurrentQuestion(sd, dd, dd.sDisplayedSection);
			}
			dd.sLastQuestion = rs.getString(5);
			// Ignore unfinished attempts, wait for a finished one
			if (rs.getInt(2) != 0) {
				// Woo! We have an answer
				dd.iOutputCurrentQuestionNumber++;
				dd.iOutputCurrentQuestionNumber = applyFinishedAttempt(sd, rs, dd, dd.sDisplayedSection);
				// Start looking for next question now
				dd.iCurrentQuestion++;
			}
			dd.sPreviousSection = dd.sSection;
		}
	}

	private void applyAnswerForCurrentQuestion(SummaryDetails sd,
		DisplayDetails dd, String currentSection) throws OmFormatException {
		dd.sDisplayedSection = addSectionRow(sd, dd, currentSection);
		addUnfinishedRow(sd, dd);
		dd.iCurrentQuestion++;
		// This only works because there always will be at least one
		// line per question thanks to the LEFT JOIN
	}

	private int applyFinishedAttempt(SummaryDetails sd, ResultSet rs,
		DisplayDetails dd, String sDisplayedSection) throws SQLException, OmFormatException, IOException {

		dd.sDisplayedSection = addSectionRow(sd, dd, sDisplayedSection);


		if (sd.isNumberBySection() && !(dd.sPreviousSection == null)
				&& !(dd.sPreviousSection.equals(dd.sDisplayedSection))) {
			dd.iOutputCurrentQuestionNumber = 1;
		}
		sd.getTableComponents().eTR = XML.createChild(sd.getTableComponents().eTable, "tr");
		sd.getTableComponents().eTR.setAttribute("class", "answered");
		XML.createText(sd.getTableComponents().eTR, "td", ""
			+ dd.iOutputCurrentQuestionNumber);
		Element ePlainRow = null;
		if (sd.isPlain()) {
			ePlainRow = XML.createChild(sd.getPlainParent(), "div");
			XML.createText(ePlainRow, "Question "
					+ dd.iOutputCurrentQuestionNumber + ". ");
		}
		if (sd.isIncludeQuestions()) {
			String sQ = rs.getString(3), sA = rs.getString(4);
			String socqn = Integer
					.toString(dd.iOutputCurrentQuestionNumber);
			// XML.createText(eTR,"td",sQ);
			XML.createText(sd.getTableComponents().eTR, "td", socqn);
			XML.createText(sd.getTableComponents().eTR, "td", sA);
			if (sd.isPlain()) {
				XML.createText(ePlainRow, "Question: " + sQ
						+ ". Your answer: " + sA + ". ");
			}
		}
		if (sd.isIncludeAttempts()) {
			String sAttempts = NavigatorServlet.getAttemptsString(
					rs.getInt(6), sd.getTestDefinition(), labelSets);
			XML.createText(sd.getTableComponents().eTR, "td", sAttempts);
			if (sd.isPlain()) {
				XML.createText(ePlainRow, "Result: " + sAttempts + ". ");
			}
		}
		addSummaryTableScore(sd.getTableComponents().eTR, ePlainRow,
			dd.sLastQuestion, sd);
		return dd.iOutputCurrentQuestionNumber;
	}

	private void includeScore(SummaryDetails sd) throws OmFormatException {
		if (sd.isIncludeScore()) {
			CombinedScore ps = sd.getRootTestGroup().getFinalScore();
			Element eTR = XML.createChild(sd.getTableComponents().eTable, "tr");
			eTR.setAttribute("class", "totals");
			Element eTD = XML.createChild(eTR, "td");
			eTD.setAttribute("colspan", ""
				+ (1 + (sd.isIncludeQuestions() ? 2 : 0) + (sd.isIncludeAttempts() ? 1 : 0)));
			XML.createText(eTD, "Total");
			Element ePlainRow = null;
			if (sd.isPlain()) {
				ePlainRow = XML.createChild(sd.getPlainParent(), "div");
				XML.createText(ePlainRow, "Totals: ");
			}
			for (int iAxis = 0; iAxis < sd.getTableComponents().asAxes.length; iAxis++) {
				String sAxis = sd.getTableComponents().asAxes[iAxis];
				String sScore = displayScore(ps.getScore(sAxis)), sMax = displayScore(ps
						.getMax(sAxis));
				XML.createText(eTR, "td", sScore);
				XML.createText(eTR, "td", sMax);
				if (ePlainRow != null) {
					XML.createText(ePlainRow, "Marks"
						+ (sAxis == null ? "" : " (" + sAxis + ")")
						+ ": " + sScore + ". Out of: " + sMax + ". ");
				}
			}
		}
	}

	private int applyLast(SummaryDetails sd, DisplayDetails dd,
		String displayedSection) throws OmFormatException {
		// check if we need to restart the numbering of the questions
		if (sd.isNumberBySection() && !(dd.sPreviousSection == null)
			&& !dd.sPreviousSection.equals(dd.sDisplayedSection)) {
			dd.iOutputCurrentQuestionNumber = 1;
		}
		dd.sDisplayedSection = addSectionRow(sd, dd, displayedSection);
		addUnfinishedRow(sd, dd);
		return dd.iOutputCurrentQuestionNumber;
	}

	private String addSectionRow(SummaryDetails sd, DisplayDetails dd,
		String sCurrentSection) {
		// Don't output anything if there has been no change to the current section
		if(dd.sSection==null || dd.sSection.equals(sCurrentSection)) {
			return sCurrentSection;
		}
		Element eTR = XML.createChild(sd.getTableComponents().eTable, "tr");
		eTR.setAttribute("class", "sectionname");
		Element eTD = XML.createChild(eTR, "td");
		eTD.setAttribute("colspan", ""
			+ (1 + (sd.isIncludeQuestions() ? 2 : 0)
			+ (sd.isIncludeAttempts() ? 1 : 0)
			+ (sd.isIncludeScore() ? sd.getTableComponents().asAxes.length * 2 : 0)));
		XML.createText(eTD, dd.sSection);
		if (sd.isPlain()) {
			XML.createText(sd.getPlainParent(), "h4", dd.sSection);
		}
		return dd.sSection;
	}

	private void addUnfinishedRow(SummaryDetails sd,
		DisplayDetails dd) throws OmFormatException {
		Element eTR=XML.createChild(sd.getTableComponents().eTable,"tr");
		Element ePlainRow=null;
		eTR.setAttribute("class","unanswered");
		XML.createText(eTR,"td","" + dd.iOutputCurrentQuestionNumber);
		if(sd.isPlain()) {
			ePlainRow=XML.createChild(sd.getPlainParent(),"div");
			XML.createText(ePlainRow,"Question " + dd.iOutputCurrentQuestionNumber + ". ");
		}
		if(sd.isIncludeQuestions()) {
			XML.createText(eTR,"td",SUMMARYTABLE_NOTANSWERED);
			XML.createChild(eTR,"td");
		}
		if(sd.isIncludeAttempts()) {
			if(sd.isIncludeQuestions())
				XML.createChild(eTR,"td");
			else
				XML.createText(eTR,"td",SUMMARYTABLE_NOTANSWERED);
		}
		if(sd.isPlain() && (sd.isIncludeAttempts() || sd.isIncludeQuestions())) {
			XML.createText(ePlainRow,SUMMARYTABLE_NOTANSWERED+". ");
		}
		addSummaryTableScore(eTR,ePlainRow, dd.sLastQuestion, sd);
	}

	private void addSummaryTableScore(Element eTR,Element ePlainRow,
		String sQuestion, SummaryDetails sd) throws OmFormatException {
		if (sd.isIncludeScore()) {
			// Find question
			TestQuestion tq=null;
			for(int i=0; i < sd.getTestLeavesInOrder().length; i++) {
				if(sd.getTestLeavesInOrder()[i] instanceof TestQuestion) {
					TestQuestion tempTQ = (TestQuestion) sd.getTestLeavesInOrder()[i];
					//((TestQuestion)sd.getTestLeavesInOrder()[i]).getID().equals(sQuestion)
					if (tempTQ.getID().equals(sQuestion)) {
						tq=(TestQuestion)sd.getTestLeavesInOrder()[i];
						break;

					}
				}
			}
			// Get score (scaled)
			CombinedScore ps=tq.getScoreContribution(sd.getRootTestGroup());
			String[] asAxes=ps.getAxesOrdered();
			for(int iAxis=0;iAxis<asAxes.length;iAxis++) {
				String sAxis=asAxes[iAxis];
				String
					sScore=displayScore(ps.getScore(sAxis)),
					sMax=displayScore(ps.getMax(sAxis));

				XML.createText(eTR,"td",sScore);
				XML.createText(eTR,"td",sMax);

				if(ePlainRow!=null) {
					XML.createText(ePlainRow,"Marks" +
						(sAxis==null ? "" : " ("+sAxis+")")+
						": "+sScore+". Out of: "+sMax+". ");
				}
			}
		}
	}

	private static String displayScore(double dScore) {
		if(Math.abs(dScore - Math.round(dScore)) < 0.001) {
			return (int)Math.round(dScore)+"";
		} else {
			NumberFormat nf=DecimalFormat.getNumberInstance();
			nf.setMaximumFractionDigits(1);
			return nf.format(dScore);
		}
	}
}
