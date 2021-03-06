package om.tnavigator;

import java.lang.reflect.Field;

import om.tnavigator.sessions.UserSession;
import om.tnavigator.teststructure.JUnitTestCaseTestDefinition;
import om.tnavigator.teststructure.JUnitTestCaseTestRealisation;
import om.tnavigator.teststructure.TestDeployment;

public class JUnitTestCaseUserSession extends UserSession {

	public JUnitTestCaseUserSession(String cookie) {
		super(cookie);
	}
	
	public JUnitTestCaseUserSession(String cookie,
			JUnitTestCaseTestDefinition td, TestDeployment deploy,
			JUnitTestCaseTestRealisation realisation) {
		super(cookie);
		testDefinition = td;
		tdDeployment = deploy;
		testRealisation = realisation;
	}

	public JUnitTestCaseTestDefinition getJUnitTestDefinition() {
		return (JUnitTestCaseTestDefinition) getTestDefinition();
	}

	public void setTestCaseDbTi(int n) throws Exception {
		Field fi = getClass().getSuperclass().getDeclaredField("dbTi");
		fi.setAccessible(true);
		fi.set(this, n);
	}
	
}
