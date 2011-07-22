package util.misc;

public class QuestionName {

	private String prefix;

	private QuestionVersion questionVersion;

	public String getPrefix() {
		return prefix;
	}

	public QuestionVersion getQuestionVersion() {
		return questionVersion;
	}

	public QuestionName(String name, QuestionVersion qv) {
		prefix = name;
		questionVersion = qv;
	}

	public boolean isValid() {
		boolean is = false;
		if ((null != prefix ? prefix.length() > 0 : false)) {
			if (null != getQuestionVersion()) {
				is = true;
			}
		}
		return is;
	}
}
