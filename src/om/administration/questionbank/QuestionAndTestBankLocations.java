package om.administration.questionbank;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple bean holding the location values of the configured Tests and Questions
 * @author Trevor Hinson
 */

public class QuestionAndTestBankLocations {

	private List<String> testBanks = new ArrayList<String>();

	private List<String> questionBanks = new ArrayList<String>();

	public List<String> getTestBanks() {
		return testBanks;
	}

	public void setTestBanks(List<String> tb) {
		if (null != tb) {
			testBanks = tb;
		}
	}

	public List<String> getQuestionBanks() {
		return questionBanks;
	}

	public void setQuestionBanks(List<String> qb) {
		if (null != qb) {
			questionBanks = qb;
		}
	}

	public String toString() {
		return new StringBuffer().append("TestBanks : ")
			.append(testBanks).append(" - ")
			.append("QuestionBanks : ").append(questionBanks).toString();
	}

}
