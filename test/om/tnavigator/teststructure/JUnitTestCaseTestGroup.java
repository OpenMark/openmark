package om.tnavigator.teststructure;

import om.OmFormatException;
import om.tnavigator.teststructure.TestGroup;

import org.w3c.dom.Element;

public class JUnitTestCaseTestGroup extends TestGroup {

	public JUnitTestCaseTestGroup(TestGroup tc, Element e) throws OmFormatException {
		super(tc.getParent(), e);
		lItems = tc.lItems;
	}
}
