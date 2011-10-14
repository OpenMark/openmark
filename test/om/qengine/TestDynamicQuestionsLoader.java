package om.qengine;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import om.AbstractTestCase;
import om.question.Question;
import util.misc.DynamicOMClassLoader;
import util.misc.DynamicQuestionUtils;
import util.xml.XML;

public class TestDynamicQuestionsLoader extends AbstractTestCase {

	private static String TESTING_DYNAMIC_QUESTION = "simplequestion-testing-dynamic-question.omxml";

	private static String DYNAMIC_QUESTION = "sdk125b7.question22.1.1.omxml";

	public void testIsDynamicQuestion() throws Exception {
		File f = pickUpFile(DYNAMIC_QUESTION);
		assertNotNull(f);
		byte[] bytes = FileUtils.readFileToByteArray(f);
		String st = new String(bytes);
		Document d = XML.parse(st.getBytes("UTF-8"));
		assertNotNull(d);
		assertTrue(DynamicQuestionUtils.isDynamicQuestion(d));
	}

	public void testIsNotDynamicQuestion() throws Exception {
		assertFalse(DynamicQuestionUtils.isDynamicQuestion(null));
	}

	public void testIsInvalidDynamicQuestion() throws Exception {
		File f = pickUpFile("mu120.module5.test.xml");
		assertNotNull(f);
		byte[] bytes = FileUtils.readFileToByteArray(f);
		Document d = XML.parse(bytes);
		assertFalse(DynamicQuestionUtils.isDynamicQuestion(d));
	}

	public void testParse() throws Exception {
		File f = pickUpFile(DYNAMIC_QUESTION);
		assertNotNull(f);
		assertTrue(f.exists());
		byte[] bytes = FileUtils.readFileToByteArray(f);
		String st = new String(bytes);
		Document d = XML.parse(st.getBytes("UTF-8"));
		assertNotNull(d);
		String s = XML.saveString(d);
		System.out.println(s);
	}

	public void tester() throws Exception {
		File f = pickUpFile(DYNAMIC_QUESTION);
		//DynamicQuestionsLoader dql = new DynamicQuestionsLoader("");
		Element e = DynamicQuestionUtils.retrieveElement(f, "question");
		assertNotNull(e);
		String att = e.getAttribute("class");
		assertNotNull(att);
		assertEquals(att, "sdk125b7.question08.Percent");
	}

	public void testLoadMetaData() throws Exception {
		DynamicQuestionsLoader dql = new DynamicQuestionsLoader(
			System.getProperty("java.class.path"));
		QuestionCache.QuestionStuff qs = new QuestionCache.QuestionStuff();
		File f = pickUpFile(TESTING_DYNAMIC_QUESTION);
		assertNotNull(f);
		dql.loadMetaData(qs, f);
		assertNotNull(qs.dMeta);
	}

	public void testLoadClass() throws Exception {
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
		System.out.println("OUT > " + out);
		DynamicQuestionsLoader dql = new DynamicQuestionsLoader(out);
		QuestionCache.QuestionStuff qs = new QuestionCache.QuestionStuff();
		DynamicOMClassLoader d = new DynamicOMClassLoader("/Temp/dynamics/",
			getClass().getClassLoader());
		qs.omclc = d;
		File f = pickUpFile(TESTING_DYNAMIC_QUESTION);
		assertNotNull(f);
		dql.loadClass(qs, f);
		assertNotNull(qs.c);
		System.out.println(">> " + qs.c.toString());
		Question q = getDynamicClass(qs.c, d);
		assertNotNull(q);
		Object obj = qs.c.newInstance();
		assertNotNull(obj);
		assertTrue(obj instanceof Question);
		Question qu = (Question) obj;
		System.out.println("TMH >> " + qu.toString());
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
