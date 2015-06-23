package om.tnavigator.teststructure;

import om.tnavigator.sessions.UserSession;

import org.w3c.dom.Node;

public class SummaryDetails {

	private int dbTi;

	// From us.getRootTestGroup().getFinalScore().
	private TestGroup rootTestGroup;

	// From us.getTestLeavesInOrder.
	private TestLeaf[] testLeavesInOrder;

	private TestDefinition testDefinition;

	private boolean numberBySection;

	private boolean includeQuestions;

	private boolean includeAttempts;

	private boolean includeScore;

	private boolean plain;

	private Node parent;

	private Node plainParent;

	public SummaryDetails() {
	}

	public SummaryDetails(UserSession us,
			Node nParent,boolean bPlain, boolean bIncludeQuestions,
			boolean bIncludeAttempts,boolean bIncludeScore) {
		includeAttempts = bIncludeAttempts;
		includeQuestions = bIncludeQuestions;
		includeScore = bIncludeScore;
		plain = bPlain;
		parent = nParent;
		dbTi = us.getDbTi();
		numberBySection = us.getTestDefinition().isNumberBySection();
		rootTestGroup = us.getRootTestGroup();
		testLeavesInOrder = us.getTestLeavesInOrder();
		testDefinition = us.getTestDefinition();
	}

	public int getDbTi() {
		return dbTi;
	}

	public TestGroup getRootTestGroup() {
		return rootTestGroup;
	}

	public TestDefinition getTestDefinition() {
		return testDefinition;
	}

	public TestLeaf[] getTestLeavesInOrder() {
		return testLeavesInOrder;
	}

	public boolean isNumberBySection() {
		return numberBySection;
	}

	public boolean isIncludeQuestions() {
		return includeQuestions;
	}

	public boolean isIncludeAttempts() {
		return includeAttempts;
	}

	public boolean isIncludeScore() {
		return includeScore;
	}

	public boolean isPlain() {
		return plain;
	}

	public Node getPlainParent() {
		return plainParent;
	}

	public void setPlainParent(Node n) {
		plainParent = n;
	}

	public Node getParent() {
		return parent;
	}
}
