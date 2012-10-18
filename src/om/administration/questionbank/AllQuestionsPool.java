package om.administration.questionbank;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds reference to the name of every question found within the question
 *  banks.  These are held for analysis against the Test and for user selection
 *  of those which are superfluous to requirements.
 * 
 * @author Trevor Hinson
 */

public class AllQuestionsPool {

	private static String JAR = ".jar";

	public Map<String, QuestionPoolDetails> questionDetails
		= new HashMap<String, QuestionPoolDetails>();

	public QuestionPoolDetails getDetails(String questionPrefixName) {
		return questionDetails.get(questionPrefixName);
	}

	public Map<String, QuestionPoolDetails> getQuestionDetails() {
		return questionDetails;
	}

	public void removeDetails(String questionPrefixName) {
		questionDetails.remove(questionPrefixName);
	}

	/**
	 * Adds reference to the Question with its' full reference number but
	 *  without the file suffix.
	 * @param questionPrefixName
	 * @param qpd
	 * @author Trevor Hinson
	 */
	public void addDetails(String questionPrefixName, QuestionPoolDetails qpd) {
		if (null != questionPrefixName ? questionPrefixName.length() > 0 : false) {
			if (null != qpd) {
				if (!questionPrefixName.endsWith(JAR)) {
					if (null == getDetails(questionPrefixName)) {
						questionDetails.put(questionPrefixName, qpd);
					}
				}
			}
		}
	}

}
