package om.administration.questionbank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BrokenTestQuestionReferences {

	private TestSynchronizationCheck testCheck;

	private Map<String, Set<String>> questionsFoundIn;

	private String questionName;

	private List<String> fullTestLocationPaths;

	public List<String> getFullTestLocationPaths() {
		if (null == fullTestLocationPaths) {
			fullTestLocationPaths = new ArrayList<String>();
		}
		return fullTestLocationPaths;
	}

	public void addFullTestLocationPaths(String path) {
		if (null != path ? path.length() > 0 : false) {
			getFullTestLocationPaths().add(path);
		}
	}

	public String getQuestionName() {
		return questionName;
	}

	public void setQuestionName(String s) {
		questionName = s;
	}

	public String getTestName() {
		return null != getTestCheck() ? getTestCheck().getName() : null;
	}

	public TestSynchronizationCheck getTestCheck() {
		return testCheck;
	}

	public BrokenTestQuestionReferences(TestSynchronizationCheck tc) {
		testCheck = tc;
	}

	public Map<String, Set<String>> getQuestionsFoundIn() {
		if (null == questionsFoundIn) {
			questionsFoundIn = new HashMap<String, Set<String>>();
		}
		return questionsFoundIn;
	}

	public void add(String latestVersionName, Set<String> foundIn) {
		if ((null != latestVersionName ? latestVersionName.length() > 0 : false)
			&& null != foundIn) {
			Set<String> found = getQuestionsFoundIn().get(latestVersionName);
			if (null != found) {
				found.addAll(foundIn);
			} else {
				getQuestionsFoundIn().put(latestVersionName, foundIn);
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		boolean is = false;
		if (null != obj ? obj instanceof BrokenTestQuestionReferences : false) {
			BrokenTestQuestionReferences ref = (BrokenTestQuestionReferences) obj;
			if (null != getQuestionName() ? getQuestionName().length() > 0 : false) {
				if (getQuestionName().equals(ref.getQuestionName())) {
					is = true;
				}
			}
		}
		return is;
	}

}
