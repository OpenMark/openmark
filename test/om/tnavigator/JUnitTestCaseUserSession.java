package om.tnavigator;

import java.lang.reflect.Field;

public class JUnitTestCaseUserSession extends UserSession {

	public JUnitTestCaseUserSession(NavigatorServlet owner, String cookie) {
		super(owner, cookie);
	}
	
	public JUnitTestCaseUserSession(NavigatorServlet owner, String cookie,
		JUnitTestCaseTestDefinition td, TestDeployment deploy,
		JUnitTestCaseTestRealisation realisation) {
		super(owner, cookie);
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
