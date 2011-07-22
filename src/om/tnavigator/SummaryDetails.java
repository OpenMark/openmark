package om.tnavigator;

import om.tnavigator.SummaryTableBuilder.TableComponents;

import org.w3c.dom.Node;

public class SummaryDetails {

	private int dbTi;

	// from us.getRootTestGroup().getFinalScore()
	private TestGroup rootTestGroup;

	// from us.getTestLeavesInOrder;
	private TestLeaf[] testLeavesInOrder;

	private boolean numberBySection;
	
	private boolean includeQuestions;
	
	private boolean includeAttempts;
	
	private boolean includeScore;
	
	private boolean plain;

	private Node parent;
	
	private Node plainParent;

	private TableComponents tableComponents;

	public int getDbTi() {
		return dbTi;
	}

	public void setDbTi(int n) {
		dbTi = n;
	}

	public TestGroup getRootTestGroup() {
		return rootTestGroup;
	}

	public void setRootTestGroup(TestGroup rtg) {
		rootTestGroup = rtg;
	}

	public TestLeaf[] getTestLeavesInOrder() {
		return testLeavesInOrder;
	}

	public void setTestLeavesInOrder(TestLeaf[] tl) {
		testLeavesInOrder = tl;
	}

	public boolean isNumberBySection() {
		return numberBySection;
	}

	public void setNumberBySection(boolean b) {
		numberBySection = b;
	}

	public boolean isIncludeQuestions() {
		return includeQuestions;
	}

	public void setIncludeQuestions(boolean b) {
		includeQuestions = b;
	}

	public boolean isIncludeAttempts() {
		return includeAttempts;
	}

	public void setIncludeAttempts(boolean b) {
		includeAttempts = b;
	}

	public boolean isIncludeScore() {
		return includeScore;
	}

	public void setIncludeScore(boolean b) {
		includeScore = b;
	}

	public boolean isPlain() {
		return plain;
	}

	public void setPlain(boolean b) {
		plain = b;
	}

	public Node getPlainParent() {
		return plainParent;
	}

	public void setPlainParent(Node n) {
		plainParent = n;
	}

	public TableComponents getTableComponents() {
		return tableComponents;
	}

	public void setTableComponents(TableComponents tc) {
		tableComponents = tc;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node n) {
		parent = n;
	}
}
