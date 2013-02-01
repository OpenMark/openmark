package om.tnavigator.teststructure;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import om.OmException;
import om.OmFormatException;

import org.w3c.dom.Document;

public class JUnitTestCaseTestDefinition extends TestDefinition {

	private static String GET_CLASS = "getClass";

	private static String GET = "get";

	public JUnitTestCaseTestDefinition(Document d) throws OmException {
		super(d);
	}

	TestGroup getResolvedContent(long lRandomSeed) throws OmFormatException {
		Random r = new Random(lRandomSeed);
		TestGroup tg = (TestGroup) getTestItem(r, eContent,null);
		JUnitTestCaseTestGroup tgroup = new JUnitTestCaseTestGroup(tg, eContent);
		tgroup.numberQuestions();
		return tgroup; 
	}

	public void deleteDocumentForTesting() throws Exception {
		setField("dTest", null);
	}

	public Boolean getEndSummaryValue() throws Exception {
		Boolean value = null;
		Object obj = getParentFieldValue("bEndSummary");
		if (null != obj ? obj instanceof Boolean : false) {
			value = (Boolean) obj ;
		}
		return value;
	}

	public String getsQuestionNumberHeader() throws Exception {
		String value = null;
		Object obj = getParentFieldValue("sQuestionNumberHeader");
		if (null != obj ? obj instanceof String : false) {
			value = (String) obj ;
		}
		return value;
	}

	public void setField(String name, Object value) throws Exception {
		Field fi = getClass().getSuperclass().getDeclaredField(name);
		fi.setAccessible(true);
		fi.set(this, value);
	}

	private Object getParentFieldValue(String name) throws Exception {
		Field f = getClass().getSuperclass().getDeclaredField(name);
		f.setAccessible(true);
		return f.get(this);
	}

	/**
	 * This is here so that we can dynamically override the options of an actual
	 *  Test dynamically within the JUnit test cases so to determine specific
	 *  outcomes at a unit test level.
	 * @param options
	 * @author Trevor Hinson
	 */
	public void optionsOverride(JUnitTestCaseTestDefinitionOptions options) {
		if (null != options) {
			Method[] me = options.getClass().getMethods();
			for (int i = 0; i < me.length; i++) {
				Method m = me[i];
				if (null != m ? m.getName().startsWith(GET) : false) {
					String name = m.getName();					
					if (!name.equalsIgnoreCase(GET_CLASS)) {
						Object[] args = {};
						try {
							Object value = m.invoke(options, args);
							if ((null != value ? value instanceof Boolean : false)
								|| (null != value ? value instanceof String : false)) {
								String actual = name.substring(3, name.length());
								Field field = getClass().getSuperclass()
									.getDeclaredField(actual);
								field.setAccessible(true);
								field.set(this, value);
							}
						} catch (NoSuchFieldException x) {
						} catch (IllegalArgumentException x) {
						} catch (IllegalAccessException x) {
						} catch (InvocationTargetException x) {
						}
					}
				}
			}
		}
	}

}
