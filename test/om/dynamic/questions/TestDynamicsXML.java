package om.dynamic.questions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import javax.tools.JavaFileObject;

import om.AbstractTestCase;
import om.qengine.dynamics.util.DynamicJavaFile;
import om.qengine.dynamics.util.DynamicQuestionsCompilationUtil;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import util.xml.XML;

public class TestDynamicsXML extends AbstractTestCase {

	private static String TESTING_DYNAMIC_QUESTION = "testing-dynamic-question.omxml";

	@Ignore // Looks like the test was committed, even though the dynamic question stuff wasn't.
	@Test public void testLoading() throws Exception {
		File f = pickUpFile(TESTING_DYNAMIC_QUESTION);
		Document doc = XML.parse(f);
		assertNotNull(doc);
		Node node = doc.getFirstChild();
		assertTrue(XML.hasChild(node, "handler"));
		Node handler = XML.getChild(node, "handler");
		assertNotNull(handler);
		String s = XML.getText(handler);

		String classOutputFolder = "/Temp/dynamics/";
		DynamicJavaFile tjf = new DynamicJavaFile("om.dynamic.questions.TestingDynamicQuestion", s);
		assertNotNull(tjf);
		Iterable<? extends JavaFileObject> files = Arrays.asList(tjf);
		
		DynamicQuestionsCompilationUtil.compile(
			System.getProperty("java.class.path"),files, classOutputFolder);
		invokeIt(classOutputFolder);
	}

	private void invokeIt(String classOutputFolder) throws Exception {
		File file = new File(classOutputFolder);
        URI uri = file.toURI();
		URL url = uri.toURL();
        URL[] urls = new URL[] { url };
        ClassLoader loader = new URLClassLoader(urls);
        Class<?> thisClass = loader.loadClass("om.dynamic.questions.TestingDynamicQuestion");
        Class<?> params[] = {Integer.class};
        Object paramsObj[] = {new Integer(7)};
        Object instance = thisClass.newInstance();
        Method thisMethod = thisClass.getDeclaredMethod("tester", params);
        Object result = thisMethod.invoke(instance, paramsObj);
        assertNotNull(result);
        assertTrue(result instanceof Integer);
        assertEquals(new Integer(21), (Integer) result);
        thisClass = null;
	}
	
}
