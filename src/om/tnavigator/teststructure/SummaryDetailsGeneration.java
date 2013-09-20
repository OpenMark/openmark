package om.tnavigator.teststructure;

import om.tnavigator.sessions.UserSession;

import org.w3c.dom.Node;

public class SummaryDetailsGeneration {

	public static SummaryDetails generateSummaryDetails(UserSession us,
		Node nParent,boolean bPlain, boolean bIncludeQuestions,
		boolean bIncludeAttempts,boolean bIncludeScore) {
		SummaryDetails sd = new SummaryDetails();
		sd.setIncludeAttempts(bIncludeAttempts);
		sd.setIncludeQuestions(bIncludeQuestions);
		sd.setIncludeScore(bIncludeScore);
		sd.setPlain(bPlain);
		sd.setParent(nParent);
		sd.setDbTi(us.getDbTi());
		sd.setNumberBySection(us.getTestDefinition().isNumberBySection());
		sd.setRootTestGroup(us.getRootTestGroup());
		sd.setTestLeavesInOrder(us.getTestLeavesInOrder());
		sd.setTestDefinition(us.getTestDefinition());
		return sd;
	}

}
