package om;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.junit.Before;
import org.w3c.dom.Document;

import util.misc.GeneralUtils;
import util.xml.XML;

public class AbstractTestCase {

	protected static String LINE_SEPERATOR = System.getProperty("line.separator");

	protected static String BASIC_TEST_DEFINITION = "mu120.module5.test.xml";

	protected static String BASIC_TEST_DEPLOYMENT = "mu120.module5.deploy.xml";

	protected static String TEST_CASE_PROGRESS_SUMMARY = "testcase-progresssummary.xhtml";

	protected Log log;

	protected <T> Document getPickUpDocument(String name, Class<T> c) throws Exception {
		InputStream is = pickUpStream(name, c);
		assertNotNull(is);
		return XML.parse(is);
	}

	@Before
	public void setUp() throws Exception{
		String logPath = getLogPath();
		log = GeneralUtils.getLog(getClass(), logPath, true);
		log.logDebug("Running tests ...");
	}

	protected String getLogPath() {
		String configuredLocation = System.getProperty("buildDirectory");
		if (configuredLocation != null) {
			return configuredLocation + File.separator + "test-data";
		} else {
			File fileLocation = pickUpFile(TEST_CASE_PROGRESS_SUMMARY);
			assertNotNull(fileLocation);
			return fileLocation.getAbsolutePath().substring(0,
					fileLocation.getAbsolutePath().lastIndexOf(File.separator) + 1);
		}
	}

	protected <T> InputStream pickUpStream(String name, Class<T> c) {
		return c.getResourceAsStream(name);
	}

	protected <T> File pickUpFile(String name, Class<T> c) {
		URL url = c.getResource(name);
		File f = null;
		if (null != url) {
			String path = url.getPath();
			path = path.replace("%20", " ");
			f = new File(path);
		}
		return f;
	}

	protected File pickUpFile(String name) {
		return pickUpFile(name, AbstractTestCase.class);
	}
}
