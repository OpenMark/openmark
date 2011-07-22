package om.administration.questionbank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClearanceResponse {

	private Map<String, IdentifiedSuperfluousQuestion> superfluousQuestions
		= new HashMap<String, IdentifiedSuperfluousQuestion>();

	private List<TestSynchronizationCheck> outOfSyncTests
		= new ArrayList<TestSynchronizationCheck>();

	private List<BrokenTestQuestionReferences> brokenTests;
	
	private Map<IdentifiedSuperfluousQuestion, RemovalIssueDetails> problemRemoving
		= new HashMap<IdentifiedSuperfluousQuestion, RemovalIssueDetails>();

	private Map<IdentifiedSuperfluousQuestion, UndoIssue> undoIssues;

	public Map<IdentifiedSuperfluousQuestion, UndoIssue> getUndoIssues() {
		if (null == undoIssues) {
			undoIssues = new HashMap<IdentifiedSuperfluousQuestion, UndoIssue>();
		}
		return undoIssues;
	}

	public void addUndoIssue(IdentifiedSuperfluousQuestion q, UndoIssue ui) {
		if (null != q && null != ui) {
			getUndoIssues().put(q, ui);
		}
	}

	public List<BrokenTestQuestionReferences> getBrokenTests() {
		if (null == brokenTests) {
			brokenTests = new ArrayList<BrokenTestQuestionReferences>(); 
		}
		return brokenTests;
	}

	public void addBrokenTest(Set<BrokenTestQuestionReferences> itqr) {
		if (null != itqr ? itqr.size() > 0 : false) {
			getBrokenTests().addAll(itqr);
		}
	}

	public boolean hasSupersluousQuestions() {
		return null != superfluousQuestions ? superfluousQuestions.size() > 0 : false;
	}

	public void addSuperfluousQuestion(IdentifiedSuperfluousQuestion q) {
		if (null != q ? null != q.getName() ? q.getName().length() > 0 : false : false) {
			superfluousQuestions.put(q.getName(), q);
		}
	}

	public void addOutOfSyncTest(TestSynchronizationCheck check) {
		if (null != check) {
			outOfSyncTests.add(check);
		}
	}

	public void addProblemRemoving(IdentifiedSuperfluousQuestion q,
		RemovalIssueDetails rid) {
		if (null != q && null != rid) {
			problemRemoving.put(q, rid);
		}
	}

	public Map<String, IdentifiedSuperfluousQuestion> getSuperfluousQuestions() {
		return Collections.unmodifiableMap(superfluousQuestions);
	}

	public List<TestSynchronizationCheck> getOutOfSyncTests() {
		return Collections.unmodifiableList(outOfSyncTests);
	}

	public Map<IdentifiedSuperfluousQuestion, RemovalIssueDetails> getProblemRemoving() {
		return Collections.unmodifiableMap(problemRemoving);
	}

}
