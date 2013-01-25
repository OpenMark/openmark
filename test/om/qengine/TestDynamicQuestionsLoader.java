package om.qengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URL;

import om.AbstractTestCase;
import om.qengine.dynamics.util.DynamicOMClassLoader;
import om.qengine.dynamics.util.DynamicQuestionUtils;
import om.question.Question;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.misc.IO;
import util.xml.XML;

public class TestDynamicQuestionsLoader extends AbstractTestCase {

	private static String TESTING_DYNAMIC_QUESTION = "simplequestion-testing-dynamic-question.omxml";

	private static String DYNAMIC_QUESTION = "sdk125b7.question22.1.1.omxml";

	@Test public void testIsDynamicQuestion() throws Exception {
		File f = pickUpFile(DYNAMIC_QUESTION);
		assertNotNull(f);
		byte[] bytes = FileUtils.readFileToByteArray(f);
		String st = new String(bytes);
		Document d = XML.parse(st.getBytes("UTF-8"));
		assertNotNull(d);
		assertTrue(DynamicQuestionUtils.isDynamicQuestion(d));
	}

	@Test public void testIsNotDynamicQuestion() throws Exception {
		assertFalse(DynamicQuestionUtils.isDynamicQuestion(null));
	}

	@Test public void testIsInvalidDynamicQuestion() throws Exception {
		byte[] bytes = IO.loadBytes(ClassLoader.getSystemResourceAsStream("mu120.module5.test.xml"));
		Document d = XML.parse(bytes);
		assertFalse(DynamicQuestionUtils.isDynamicQuestion(d));
	}

	@Test public void testParse() throws Exception {
		File f = pickUpFile(DYNAMIC_QUESTION);
		assertNotNull(f);
		assertTrue(f.exists());
		byte[] bytes = FileUtils.readFileToByteArray(f);
		String st = new String(bytes);
		Document d = XML.parse(st.getBytes("UTF-8"));
		assertNotNull(d);
		XML.saveString(d);
	}

	@Test public void tester() throws Exception {
		File f = pickUpFile(DYNAMIC_QUESTION);
		//DynamicQuestionsLoader dql = new DynamicQuestionsLoader("");
		Element e = DynamicQuestionUtils.retrieveElement(f, "question");
		assertNotNull(e);
		String att = e.getAttribute("class");
		assertNotNull(att);
		assertEquals(att, "sdk125b7.question08.Percent");
	}

	@Test public void testLoadMetaData() throws Exception {
		DynamicQuestionsLoader dql = new DynamicQuestionsLoader(
			System.getProperty("java.class.path"));
		QuestionCache.QuestionStuff qs = new QuestionCache.QuestionStuff();
		File f = pickUpFile(TESTING_DYNAMIC_QUESTION);
		assertNotNull(f);
		dql.loadMetaData(qs, f);
		assertNotNull(qs.dMeta);
	}

	@Ignore // Looks like the test was committed, even though the dynamic question stuff wasn't.
	@Test public void testLoadClass() throws Exception {
		URL url = ClassLoader.getSystemResource(TESTING_DYNAMIC_QUESTION);
		assertNotNull(url);
		String out = "";
		int n = url.toString().lastIndexOf("/");
		if (n > 0) {
			out = url.toString().substring(0, n);
			if (out.contains("%20")) {
				out = out.replace("%20", " ");
			}
			if (out.startsWith("file:/")) {
				out = out.substring("file:/".length(), out.length());
			}
		}
		DynamicQuestionsLoader dql = new DynamicQuestionsLoader(out);
		QuestionCache.QuestionStuff qs = new QuestionCache.QuestionStuff();
		DynamicOMClassLoader d = new DynamicOMClassLoader("/Temp/dynamics/",
			getClass().getClassLoader());
		qs.omclc = d;
		File f = pickUpFile(TESTING_DYNAMIC_QUESTION);
		assertNotNull(f);
		dql.loadClass(qs, f);
		assertNotNull(qs.c);
		Question q = getDynamicClass(qs.c, d);
		assertNotNull(q);
		Object obj = qs.c.newInstance();
		assertNotNull(obj);
		assertTrue(obj instanceof Question);
	}

	 public Question getDynamicClass(Class<?> cla, DynamicOMClassLoader d)
	 	throws Exception {
		String className = cla.toString().substring("class ".length(),
				cla.toString().length());
		return (Question) Class.forName(className,true, d).newInstance();
	 }
	
	protected URL[] getUrls() throws Exception {
		File dynamicClassLoaderLocation = new File("/Temp/dynamics/");
		URI uri = dynamicClassLoaderLocation.toURI();
		URL url = uri.toURL();
		return new URL[] { url };
	}

}
