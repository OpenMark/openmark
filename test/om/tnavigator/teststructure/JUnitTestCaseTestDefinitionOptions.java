package om.tnavigator.teststructure;

import java.lang.reflect.Method;

public class JUnitTestCaseTestDefinitionOptions {

	private Boolean bNavigation;

	private Boolean bRedoQuestion;
	
	private Boolean bRedoQuestionAuto;
	
	private Boolean bRedoTest;
	
	private Boolean bFreeSummary;
	
	private Boolean bFreeStop;
	
	private Boolean bSummaryScores;
	
	private Boolean bSummaryAttempts;
	
	private Boolean bSummaryQuestions;
	
	private Boolean bQuestionNames;
	
	private Boolean bEndSummary;
	
	private Boolean bNumberBySection;

	private String sLabelSet;
	
	private String sQuestionNumberHeader;

	public String getsLabelSet() {
		return sLabelSet;
	}

	public void setsLabelSet(String s) {
		sLabelSet = s;
	}

	public String getsQuestionNumberHeader() {
		return sQuestionNumberHeader;
	}

	public void setsQuestionNumberHeader(String s) {
		sQuestionNumberHeader = s;
	}

	public void allBooleans(boolean value) throws Exception {
		Method[] methods = getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			Object[] args = {value};
			Class<?>[] clas = m.getParameterTypes();
			if (clas.length == 1 ? Boolean.class.isAssignableFrom(clas[0]) : false) {
				if (m.getName().startsWith("set")) {
					m.invoke(this, args);
				}
			}
		}
	}

	public Boolean getbNavigation() {
		return bNavigation;
	}

	public void setbNavigation(Boolean b) {
		this.bNavigation = b;
	}

	public Boolean getbRedoQuestion() {
		return bRedoQuestion;
	}

	public void setbRedoQuestion(Boolean b) {
		this.bRedoQuestion = b;
	}

	public Boolean getbRedoQuestionAuto() {
		return bRedoQuestionAuto;
	}

	public void setbRedoQuestionAuto(Boolean b) {
		this.bRedoQuestionAuto = b;
	}

	public Boolean getbRedoTest() {
		return bRedoTest;
	}

	public void setbRedoTest(Boolean b) {
		this.bRedoTest = b;
	}

	public Boolean getbFreeSummary() {
		return bFreeSummary;
	}

	public void setbFreeSummary(Boolean b) {
		this.bFreeSummary = b;
	}

	public Boolean getbFreeStop() {
		return bFreeStop;
	}

	public void setbFreeStop(Boolean b) {
		this.bFreeStop = b;
	}

	public Boolean getbSummaryScores() {
		return bSummaryScores;
	}

	public void setbSummaryScores(Boolean b) {
		this.bSummaryScores = b;
	}

	public Boolean getbSummaryAttempts() {
		return bSummaryAttempts;
	}

	public void setbSummaryAttempts(Boolean b) {
		this.bSummaryAttempts = b;
	}

	public Boolean getbSummaryQuestions() {
		return bSummaryQuestions;
	}

	public void setbSummaryQuestions(Boolean b) {
		this.bSummaryQuestions = b;
	}

	public Boolean getbQuestionNames() {
		return bQuestionNames;
	}

	public void setbQuestionNames(Boolean b) {
		this.bQuestionNames = b;
	}

	public Boolean getbEndSummary() {
		return bEndSummary;
	}

	public void setbEndSummary(Boolean b) {
		this.bEndSummary = b;
	}

	public Boolean getbNumberBySection() {
		return bNumberBySection;
	}

	public void setbNumberBySection(Boolean b) {
		this.bNumberBySection = b;
	}
	
}
