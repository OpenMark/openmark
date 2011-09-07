package om.qengine;

import java.io.File;
import java.net.URI;
import java.net.URL;

import om.AbstractTestCase;
import om.helper.SimpleQuestion1;
import om.question.Question;
import util.misc.DynamicOMClassLoader;

public class TestDynamicQuestionsLoader extends AbstractTestCase {

	private static String TESTING_DYNAMIC_QUESTION = "simplequestion-testing-dynamic-question.xml";

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
		//DynamicOMClassLoader d = new DynamicOMClassLoader(getUrls());
		DynamicOMClassLoader d = new DynamicOMClassLoader("/Temp/dynamics/", getClass().getClassLoader());
		qs.omclc = d;
		File f = pickUpFile(TESTING_DYNAMIC_QUESTION);
		assertNotNull(f);
		dql.loadClass(qs, f);
		assertNotNull(qs.c);
		System.out.println(">> " + qs.c.toString());
		Question q = getDynamicClass(qs.c, d);
		assertNotNull(q);
//		Object obj = qs.c.newInstance();
//		assertNotNull(obj);
//		assertTrue(obj instanceof Question);
//		assertTrue(obj instanceof SimpleQuestion1);
//		Question q = (Question) obj;
//		System.out.println("TMH >> " + q.toString());
		
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
