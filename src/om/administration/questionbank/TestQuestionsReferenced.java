package om.administration.questionbank;

import java.util.ArrayList;
import java.util.List;

public class TestQuestionsReferenced {

	private List<String> namesOfQuestions = new ArrayList<String>();

	public TestQuestionsReferenced(List<String> names) {
		if (null != names) {
			namesOfQuestions.addAll(names);
		}
	}

	public int getNumberOfQuestionsHeld() {
		return null != namesOfQuestions ? namesOfQuestions.size() : 0;
	}

	public List<String> getNamesOfQuestions() {
		return namesOfQuestions;
	}

	public boolean containsQuestionReference(String name) {
		boolean contains = false;
		if (null != name ? name.length() > 0 : false) {
			contains = null != namesOfQuestions ? namesOfQuestions.contains(name) : false;
		}
		return contains;
	}

	@Override
	public boolean equals(Object obj) {
		boolean is = false;
		if (null != obj ? obj instanceof TestQuestionsReferenced : false) {
			TestQuestionsReferenced qr = (TestQuestionsReferenced) obj;
			if (null == qr.getNamesOfQuestions()) {
				if (null == getNamesOfQuestions()) {
					is = true;
				}
			} else {
				if (qr.getNamesOfQuestions().size() == getNamesOfQuestions().size()) {
					is = qr.getNamesOfQuestions().containsAll(
						getNamesOfQuestions());
					if (is) {
						is = getNamesOfQuestions().containsAll(qr.getNamesOfQuestions());
					}
				}
			}
		}
		return is;
	}

	public int size() {
		return null != namesOfQuestions ? namesOfQuestions.size() : 0;
	}

	public String toString() {
		return null != getNamesOfQuestions()
			? getNamesOfQuestions().toString() : super.toString();
	}
}
