package om.tnavigator;

import om.OmFormatException;

import org.w3c.dom.Element;

public class JUnitTestCaseTestGroup extends TestGroup {

	public JUnitTestCaseTestGroup(TestGroup tc, Element e) throws OmFormatException {
		super(tc.getParent(), e);
		lItems = tc.lItems;
	}
}
