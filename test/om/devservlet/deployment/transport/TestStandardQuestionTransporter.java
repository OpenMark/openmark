package om.devservlet.deployment.transport;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import om.abstractservlet.DisplayUtils;
import om.abstractservlet.RenderedOutput;
import om.devservlet.deployment.QuestionHolder;
import om.devservlet.deployment.transport.StandardQuestionTransporter.LatestNameAndVersion;
import om.devservlet.deployment.transport.StandardQuestionTransporter.Versioner;

import org.junit.After;
import org.junit.Test;

import util.misc.GeneralUtils;

public class TestStandardQuestionTransporter extends TestCase {

	/**
	 * Here we tidy up .jar files that have been created within the directory
	 *  for these tests to run with.
	 */
	@After
	public void tearDown() {
		String name = "samples.mu120.module5.question01.jar";
		URL url = TestStandardQuestionTransporter.class.getResource(name);
		assertNotNull(url);
		String replacedPath = url.getPath().replace("%20", " ");
		File source = new File(replacedPath);
		String subPath = source.getAbsolutePath().substring(0,
			source.getAbsolutePath().lastIndexOf(File.separator) + 1);
		File f = new File(subPath);
		assertTrue(f.isDirectory());
		File[] files = f.listFiles();
		for (int i = 0; i < files.length; i++) {
			File fil = files[i];
			if (null != fil) {
				if (fil.getName().contains(".jar")
					? !name.equals(fil.getName()) : false) {
					assertTrue(fil.delete());
				}
			}
		}
	}

	@Test public void testVersionNumberDetermination() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Versioner num = sqt.retrieveVersionerFromFileName("tester.3.7.jar");
		assertNotNull(num);
		assertEquals(7, num.secondary);
	}

	@Test public void testBadVersionNumberDetermination() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Versioner num = sqt.retrieveVersionerFromFileName("tester.1.3..jar");
		assertNull(num);
	}

	@Test public void testReducedVersionNumberDetermination() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Versioner num = sqt.retrieveVersionerFromFileName("tester.1.1.jar");
		assertNotNull(num);
		assertTrue(num.primary == 1);
		assertTrue(num.secondary == 1);
	}

	@Test public void testInvalidVersionNumberDetermination() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Versioner num = sqt.retrieveVersionerFromFileName("tester.jar");
		assertNull(num);
	}

	@Test public void testCompletelyInvalidVersionNumberDetermination() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Versioner num = sqt.retrieveVersionerFromFileName("x.jar");
		assertNull(num);
	}

	@Test public void testLargest() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		List<String> which = new ArrayList<String>();
		which.add("25.3");
		which.add("7.2");
		which.add("7.1");
		which.add("7.4");
		which.add("8.5");
		which.add("2.6");
		which.add("19.2");
		which.add("0.8");
		List<StandardQuestionTransporter.Versioner> vers = new ArrayList<StandardQuestionTransporter.Versioner>();
		for (String num : which) {
			StandardQuestionTransporter.Versioner v = sqt.createVersioner(num);
			assertNotNull(v);
			vers.add(v);
		}
		StandardQuestionTransporter.Versioner mostRecent = sqt.getMostRecentVersion(vers);
		assertNotNull(mostRecent);
		assertTrue(mostRecent.primary == 25);
		assertTrue(mostRecent.secondary == 3);
	}

	@Test public void testLargestAgain() throws Exception {
		List<String> which = new ArrayList<String>();
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		which.add("0.8");
		which.add("0.4");
		which.add("0.1");
		List<StandardQuestionTransporter.Versioner> vers = new ArrayList<StandardQuestionTransporter.Versioner>();
		for (String num : which) {
			StandardQuestionTransporter.Versioner v = sqt.createVersioner(num);
			assertNotNull(v);
			vers.add(v);
		}
		StandardQuestionTransporter.Versioner mostRecent = sqt.getMostRecentVersion(vers);
		assertNotNull(mostRecent);
		assertTrue(mostRecent.primary == 0);
		assertTrue(mostRecent.secondary == 8);
	}

	@Test public void testRetrieveVersionerFromFileName() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		String name = "samples.mu120.module5.question02.4.5.jar";
		StandardQuestionTransporter.Versioner ver = sqt
			.retrieveVersionerFromFileName(name);
		assertNotNull(ver);
		assertTrue(ver.primary == 4);
		assertTrue(ver.secondary == 5);
	}

	@Test public void testMatchingVersioner() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		StandardQuestionTransporter.Versioner v = sqt.new Versioner();
		StandardQuestionTransporter.Versioner v2 = sqt.new Versioner();
		v.primary = 2;
		v.secondary = 3;
		v2.primary = 2;
		v2.secondary = 3;
		assertTrue(v.validMatch(v2));
	}

	@Test public void testInvalidMatchingVersioner() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		StandardQuestionTransporter.Versioner v = sqt.new Versioner();
		StandardQuestionTransporter.Versioner v2 = sqt.new Versioner();
		v.primary = 2;
		v.secondary = 3;
		v2.primary = 2;
		v2.secondary = 4;
		assertFalse(v.validMatch(v2));
	}

	@Test public void testAnotherInvalidMatchingVersioner() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		StandardQuestionTransporter.Versioner v = sqt.new Versioner();
		StandardQuestionTransporter.Versioner v2 = sqt.new Versioner();
		v.primary = 1;
		v.secondary = 3;
		v2.primary = 2;
		v2.secondary = 3;
		assertFalse(v.validMatch(v2));
	}

	@Test public void testAnotherMatchingVersioner() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		StandardQuestionTransporter.Versioner v = sqt.new Versioner();
		StandardQuestionTransporter.Versioner v2 = sqt.new Versioner();
		v.primary = 11;
		v.secondary = 317;
		v2.primary = 11;
		v2.secondary = 317;
		assertTrue(v.validMatch(v2));
	}
	
	@Test public void testCreationOfVersioner() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		StandardQuestionTransporter.Versioner v = sqt
			.retrieveVersionerFromFileName("tester.11.317.jar");
		StandardQuestionTransporter.Versioner v2 = sqt
			.retrieveVersionerFromFileName("tester.11.317.jar");
		assertTrue(v.validMatch(v2));
	}

	@Test public void testSecondaryInvalidMatchVersioner() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		StandardQuestionTransporter.Versioner v = sqt
			.retrieveVersionerFromFileName("tester.11.317.jar");
		StandardQuestionTransporter.Versioner v2 = sqt
			.retrieveVersionerFromFileName("tester.11.318.jar");
		assertFalse(v.validMatch(v2));
	}

	@Test public void testPrimaryInvalidMatchVersioner() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		StandardQuestionTransporter.Versioner v = sqt
			.retrieveVersionerFromFileName("tester.11.317.jar");
		StandardQuestionTransporter.Versioner v2 = sqt
			.retrieveVersionerFromFileName("tester.12.317.jar");
		assertFalse(v.validMatch(v2));
	}

	@Test public void testNewName() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		StandardQuestionTransporter.LatestNameAndVersion ver = sqt.new LatestNameAndVersion();
		assertNotNull(ver);
		ver.fileNamePrefix = "tester";
		StandardQuestionTransporter.Versioner v = sqt.new Versioner();
		v.primary = 2;
		v.secondary = 2;
		ver.version = v;
		String newName = sqt.newFileNamePrefix(ver);
		assertNotNull(newName);
		assertEquals("tester.2.3", newName);
	}

	@Test public void testMakingACopy() throws Exception {
		URL url = TestStandardQuestionTransporter.class.getResource("samples.mu120.module5.question01.jar");
		assertNotNull(url);
		String path = url.getPath();
		int n = path.lastIndexOf("/");
		String replacedPath = url.getPath().replace("%20", " ");
		File source = new File(replacedPath);
		assertNotNull(source);
		String newFileName = url.getPath().substring(0, n + 1);
		newFileName = newFileName.replace("%20", " ");
		newFileName = newFileName + "target" + System.currentTimeMillis() + ".jar";
		File target = new File(newFileName);
		assertTrue(target.createNewFile());
		GeneralUtils.copyFile(source, target);
	}

	@Test public void testAgreedLatestVersionExpectsNull() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Versioner v1 = sqt.new Versioner();
		v1.primary = 12;
		v1.secondary = 2;
		LatestNameAndVersion n1 = sqt.new LatestNameAndVersion();
		n1.fileNamePrefix = "v";
		n1.version = v1;
		Versioner v2 = sqt.new Versioner();
		v2.primary = 11;
		v2.secondary = 5;
		LatestNameAndVersion n2 = sqt.new LatestNameAndVersion();
		n2.fileNamePrefix = "v";
		n2.version = v2;
		Versioner v3 = sqt.new Versioner();
		v3.primary = 1;
		v3.secondary = 45;
		LatestNameAndVersion n3 = sqt.new LatestNameAndVersion();
		n2.fileNamePrefix = "v";
		n2.version = v3;
		Map<String, LatestNameAndVersion> vers = new HashMap<String, LatestNameAndVersion>();
		vers.put("v1", n1);
		vers.put("v2", n2);
		vers.put("v3", n3);
		LatestNameAndVersion ver = sqt.agreedLatestVersion(vers);
		assertNull(ver);
	}

	@Test public void testAgreedLatestVersion() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Versioner v1 = sqt.new Versioner();
		v1.primary = 12;
		v1.secondary = 2;
		LatestNameAndVersion n1 = sqt.new LatestNameAndVersion();
		n1.fileNamePrefix = "v";
		n1.version = v1;
		Versioner v2 = sqt.new Versioner();
		v2.primary = 12;
		v2.secondary = 2;
		LatestNameAndVersion n2 = sqt.new LatestNameAndVersion();
		n2.fileNamePrefix = "v";
		n2.version = v2;
		Map<String, LatestNameAndVersion> vers = new HashMap<String, LatestNameAndVersion>();
		vers.put("v1", n1);
		vers.put("v2", n2);
		LatestNameAndVersion ver = sqt.agreedLatestVersion(vers);
		assertNotNull(ver);
	}

	@Test public void testAppendConfiguredReportMessage() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		String message = "Testing the message";
		Map<String, String> metaData = new HashMap<String, String>();
		metaData.put(StandardQuestionTransporter.REPORT_TO_MESSAGE_KEY,
			message);
		RenderedOutput ro = new RenderedOutput();
		sqt.appendConfiguredReportMessage(metaData, ro);
		assertNotNull(ro);
		assertTrue(ro.toString().contains(message));
	}

	@Test public void testAppendConfiguredReportMessageInvalid() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		String message = "Testing the message";
		Map<String, String> metaData = new HashMap<String, String>();
		RenderedOutput ro = new RenderedOutput();
		sqt.appendConfiguredReportMessage(metaData, ro);
		assertNotNull(ro);
		assertFalse(ro.toString().contains(message));
	}

	@Test public void testCreateVersioner() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		String s = "7.2";
		Versioner ver = sqt.createVersioner(s);
		assertNotNull(ver);
		assertEquals(ver.primary, 7);
		assertEquals(ver.secondary, 2);
	}

	@Test public void testCreateVersionerWithInvalidDetails() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		String s = ".2";
		Versioner ver = sqt.createVersioner(s);
		assertNull(ver);
	}

	@Test public void testCreateVersionerWithInvalidNumbering() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		String s = ".";
		Versioner ver = sqt.createVersioner(s);
		assertNull(ver);
	}

	@Test public void testCreateVersionerWithEmptyText() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		String s = "";
		Versioner ver = sqt.createVersioner(s);
		assertNull(ver);
	}

	@Test public void testCreateVersionerWithInvalidText() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		String s = "tester";
		Versioner ver = sqt.createVersioner(s);
		assertNull(ver);
	}

	@Test public void testDeployCopy() throws Exception {
		File newFile = doDeploy(1, 1);
		assertTrue(newFile.delete());
	}

	private File doDeploy(int primary, int secondary) throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Map<String, String> metaData = new HashMap<String, String>();
		RenderedOutput ro = new RenderedOutput();
		URL url = TestStandardQuestionTransporter.class.getResource(
			"samples.mu120.module5.question01.jar");
		assertNotNull(url);
		String replacedPath = url.getPath().replace("%20", " ");
		File source = new File(replacedPath);
		String subPath = source.getAbsolutePath().substring(0,
			source.getAbsolutePath().lastIndexOf(File.separator) + 1);
		metaData.put(DisplayUtils.LOCATION + 1, subPath);
		List<String> locations = new ArrayList<String>();
		locations.add(subPath);
		File newFile = new File(subPath + File.separator
			+ "samples.mu120.module5.question01." + primary + "." + secondary + ".jar");
		boolean b = sqt.deployCopy(newFile, ro, metaData);
		assertTrue(b);
		return newFile;
	}

	@Test public void testDetermineCurrentVersionSingle() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		String name = "samples.mu120.module5.question01.jar";
		URL url = TestStandardQuestionTransporter.class.getResource(name);
		assertNotNull(url);
		String replacedPath = url.getPath().replace("%20", " ");
		File source = new File(replacedPath);
		String subPath = source.getAbsolutePath().substring(0,
			source.getAbsolutePath().lastIndexOf(File.separator) + 1);
		Map<String, String> metaData = new HashMap<String, String>();
		metaData.put(DisplayUtils.LOCATION + 1, subPath);
		List<String> locations = new ArrayList<String>();
		locations.add(subPath);
		RenderedOutput ro = new RenderedOutput();
		QuestionHolder qh = new QuestionHolder(null, source, metaData,name, new File(subPath));
		LatestNameAndVersion lat = sqt.determineCurrentVersion(locations, metaData, qh, ro);
		assertNotNull(lat);
		assertNotNull(lat.version);
		assertEquals(lat.version.primary, 1);
		assertEquals(lat.version.secondary, 0);
	}

	@Test public void testDetermineCurrentVersionMultiples() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		String name = "samples.mu120.module5.question01";
		String suffix = ".jar";
		URL url = TestStandardQuestionTransporter.class.getResource(name + suffix);
		assertNotNull(url);
		String replacedPath = url.getPath().replace("%20", " ");
		File source = new File(replacedPath);
		String subPath = source.getAbsolutePath().substring(0,
			source.getAbsolutePath().lastIndexOf(File.separator) + 1);
		Map<String, String> metaData = new HashMap<String, String>();
		metaData.put(DisplayUtils.LOCATION + 1, subPath);
		List<String> locations = new ArrayList<String>();
		locations.add(subPath);
		RenderedOutput ro = new RenderedOutput();
		File f1_1 = doDeploy(1, 1);
		assertNotNull(f1_1);
		File f1_2 = doDeploy(1, 2);
		assertNotNull(f1_2);
		File f1_3 = doDeploy(1, 3);
		assertNotNull(f1_3);
		File f1_4 = doDeploy(1, 4);
		assertNotNull(f1_4);
		File f1_5 = doDeploy(1, 5);
		assertNotNull(f1_5);
		QuestionHolder qh = new QuestionHolder(null, source, metaData,name, new File(subPath));
		LatestNameAndVersion lat = sqt.determineCurrentVersion(locations, metaData, qh, ro);
		assertNotNull(lat);
		assertNotNull(lat.version);
		assertEquals(lat.version.primary, 1);
		assertEquals(lat.version.secondary, 5);
		assertTrue(f1_1.delete());
		assertTrue(f1_2.delete());
		assertTrue(f1_3.delete());
		assertTrue(f1_4.delete());
		assertTrue(f1_5.delete());
	}

	@Test public void testEnsureLatestMatch() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		String name = "samples.mu120.module5.question01.jar";
		URL url = TestStandardQuestionTransporter.class.getResource(name);
		assertNotNull(url);
		String replacedPath = url.getPath().replace("%20", " ");
		File source = new File(replacedPath);
		String subPath = source.getAbsolutePath().substring(0,
			source.getAbsolutePath().lastIndexOf(File.separator) + 1);
		Map<String, String> metaData = new HashMap<String, String>();
		metaData.put(DisplayUtils.LOCATION + 1, subPath);
		List<String> locations = new ArrayList<String>();
		locations.add(subPath);
		List<Versioner> versions = new ArrayList<Versioner>();
		Versioner v1 = sqt.new Versioner();
		v1.primary = 2;
		v1.secondary = 4;
		Versioner v2 = sqt.new Versioner();
		v2.primary = 2;
		v2.secondary = 5;
		Versioner v3 = sqt.new Versioner();
		v3.primary = 2;
		v3.secondary = 1;
		versions.add(v1);
		versions.add(v2);
		versions.add(v3);
		QuestionHolder qh = new QuestionHolder(null, source, metaData,name,
			new File(subPath));
		LatestNameAndVersion lat = sqt.ensureLatestMatch(versions, qh);
		assertNotNull(lat);
		assertNotNull(lat.version);
		assertEquals(lat.version.primary, 2);
		assertEquals(lat.version.secondary, 5);
	}

	@Test public void testGetMostRecentEmpty() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		List<Versioner> vers = new ArrayList<Versioner>();
		Versioner v = sqt.getMostRecentVersion(vers);
		assertNull(v);
	}

	@Test public void testGetMostRecentNulled() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Versioner v = sqt.getMostRecentVersion(null);
		assertNull(v);
	}

	@Test public void testGetMostRecent() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		List<Versioner> versioners = new ArrayList<Versioner>();
		Versioner ver1 = sqt.new Versioner();
		ver1.primary = 1;
		ver1.secondary = 0;
		Versioner ver2 = sqt.new Versioner();
		ver2.primary = 1;
		ver2.secondary = 0;		
		Versioner ver3 = sqt.new Versioner();
		ver3.primary = 1;
		ver3.secondary = 0;
		versioners.add(ver1);
		versioners.add(ver2);
		versioners.add(ver3);
		Versioner v = sqt.getMostRecentVersion(versioners);
		assertNotNull(v);
		assertTrue(v.validMatch(ver1));
	}

	@Test public void testHandleInvalidVersioning() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Map<String, String> metaData = new HashMap<String, String>();
		RenderedOutput ro = new RenderedOutput();
		sqt.handleInvalidVersioning(ro, metaData);
		assertTrue(ro.toString().contains("The latest version could not be determined."));
	}

	@Test public void testHandleInvalidVersioningWithNullRenderedOutput() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Map<String, String> metaData = new HashMap<String, String>();
		sqt.handleInvalidVersioning(null, metaData);
	}

	@Test public void testIdentifyLatestVersion() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		RenderedOutput ro = new RenderedOutput();
		String name = "samples.mu120.module5.question01.jar";
		URL url = TestStandardQuestionTransporter.class.getResource(name);
		assertNotNull(url);
		String replacedPath = url.getPath().replace("%20", " ");
		File source = new File(replacedPath);
		String subPath = source.getAbsolutePath().substring(0,
			source.getAbsolutePath().lastIndexOf(File.separator) + 1);
		Map<String, String> metaData = new HashMap<String, String>();
		QuestionHolder qh = new QuestionHolder(null, source, metaData, name,
			new File(subPath));
		LatestNameAndVersion lat = sqt.identifyLatestVersion(qh, new File(subPath), ro);
		assertNotNull(lat);
		assertNotNull(lat.version);
		assertEquals(lat.version.primary, 1);
		assertEquals(lat.version.secondary, 0);
	}

	@Test public void testMakeLocalCopy() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		RenderedOutput ro = new RenderedOutput();
		String newNamePrefix = "tester.1.2";
		String name = "samples.mu120.module5.question01.jar";
		URL url = TestStandardQuestionTransporter.class.getResource(name);
		assertNotNull(url);
		String replacedPath = url.getPath().replace("%20", " ");
		File source = new File(replacedPath);
		String subPath = source.getAbsolutePath().substring(0,
			source.getAbsolutePath().lastIndexOf(File.separator) + 1);
		Map<String, String> metaData = new HashMap<String, String>();
		QuestionHolder qh = new QuestionHolder(null, source, metaData, name,
			new File(subPath));
		File newCopy = sqt.makeLocalCopy(qh, newNamePrefix, ro);
		assertNotNull(newCopy);
		assertTrue(newCopy.delete());
	}

	@Test public void testNewFileNamePrefix() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Versioner ver = sqt.new Versioner();
		ver.primary = 2;
		ver.secondary = 4;
		LatestNameAndVersion latVer = sqt.new LatestNameAndVersion();
		latVer.fileNamePrefix = "tester";
		latVer.version = ver;
		String name = sqt.newFileNamePrefix(latVer);
		assertNotNull(name);
		assertEquals(name, "tester.2.5");
	}

	@Test public void testProblemRemovingOutOfSyncCopy() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Map<String, String> metaData = new HashMap<String, String>();
		String name = "samples.mu120.module5.question01.jar";
		URL url = TestStandardQuestionTransporter.class.getResource(name);
		assertNotNull(url);
		String replacedPath = url.getPath().replace("%20", " ");
		File source = new File(replacedPath);
		RenderedOutput ro = new RenderedOutput();
		sqt.problemRemovingOutOfSyncCopy(ro, source, metaData);
		assertNotNull(ro);
		assertTrue(ro.toString().contains(
			"There was a problem removing the file from the server in order"));
		assertTrue(ro.toString().contains(source.getAbsolutePath()));
	}

	@Test public void testProblemRemovingOutOfSyncCopyNulled() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Map<String, String> metaData = new HashMap<String, String>();
		RenderedOutput ro = new RenderedOutput();
		sqt.problemRemovingOutOfSyncCopy(ro, null, metaData);
		assertNotNull(ro);
		assertTrue(ro.toString().contains(
			"There was a problem removing the file from the server in order"));
	}

	@Test public void testProblemWithQuestionBankDirectory() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		RenderedOutput ro = new RenderedOutput();
		sqt.problemWithQuestionBankDirectory("", ro);
		assertTrue(ro.toString().contains(
			"There was a problem with the questionbank directory."));
	}

	@Test public void testRetrievelatestVesions() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		RenderedOutput ro = new RenderedOutput();
		String name = "samples.mu120.module5.question01.jar";
		URL url = TestStandardQuestionTransporter.class.getResource(name);
		assertNotNull(url);
		String replacedPath = url.getPath().replace("%20", " ");
		File source = new File(replacedPath);
		String subPath = source.getAbsolutePath().substring(0,
			source.getAbsolutePath().lastIndexOf(File.separator) + 1);
		Map<String, String> metaData = new HashMap<String, String>();
		metaData.put(DisplayUtils.LOCATION + 1, subPath);
		QuestionHolder qh = new QuestionHolder(null, source, metaData, name,
			new File(subPath));
		Map<String, LatestNameAndVersion> vers = sqt.retrieveLatestVersions(
			metaData, qh, ro);
		assertNotNull(vers);
		assertTrue(vers.size() == 1);
		LatestNameAndVersion latVer = vers.get(subPath);
		assertNotNull(latVer);
		assertEquals(latVer.fileNamePrefix, "samples.mu120.module5.question01.jar");
		assertNotNull(latVer.version);
		assertEquals(latVer.version.primary, 1);
		assertEquals(latVer.version.secondary, 0);
	}

	@Test public void testRetrieveVersionerFromFileNameNOVersionSpecified() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Versioner ver = sqt.retrieveVersionerFromFileName("samples.mu120.module5.question01.jar");
		assertNull(ver);
	}

	@Test public void testRetrieveVersionerFromFileNameTypical() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Versioner ver = sqt.retrieveVersionerFromFileName("samples.mu120.module5.question01.2.3.jar");
		assertNotNull(ver);
		assertEquals(ver.primary, 2);
		assertEquals(ver.secondary, 3);
	}

	@Test public void testRetrieveVersionerFromFileNameNull() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Versioner ver = sqt.retrieveVersionerFromFileName(null);
		assertNull(ver);
	}

	@Test public void testRetrieveVersionerFromFileNameEmpty() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Versioner ver = sqt.retrieveVersionerFromFileName("");
		assertNull(ver);
	}

	@Test public void testRollBack() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		Map<String, String> metaData = new HashMap<String, String>();
		RenderedOutput ro = new RenderedOutput();
		File f = doDeploy(1, 1);
		assertTrue(f.exists());
		assertNotNull(f);
		String path = f.getAbsolutePath().substring(0,
			f.getAbsolutePath().lastIndexOf(File.separator));
		List<String> paths = new ArrayList<String>();
		paths.add(path);
		sqt.rollBack(paths, f, ro, metaData);
		assertFalse(ro.toString().contains(
			"There was a problem removing the file from the server in order"));
		assertFalse(f.exists());
	}

	@Test public void testStartReport() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		RenderedOutput ro = new RenderedOutput();
		String name = "samples.mu120.module5.question01.jar";
		URL url = TestStandardQuestionTransporter.class.getResource(name);
		assertNotNull(url);
		String replacedPath = url.getPath().replace("%20", " ");
		File source = new File(replacedPath);
		String subPath = source.getAbsolutePath().substring(0,
			source.getAbsolutePath().lastIndexOf(File.separator) + 1);
		Map<String, String> metaData = new HashMap<String, String>();
		QuestionHolder qh = new QuestionHolder(null, source, metaData, name,
			new File(subPath));
		sqt.startReport(ro, qh);
		assertTrue(ro.toString().contains(qh.toString()));
	}

	@Test public void testTidyUpNulled() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		RenderedOutput ro = new RenderedOutput();
		sqt.tidyUp(null, ro);
		assertTrue(ro.toString().contains("unsuccessful"));
	}

	@Test public void testTidyUp() throws Exception {
		StandardQuestionTransporter sqt = new StandardQuestionTransporter();
		RenderedOutput ro = new RenderedOutput();
		File newFile = doDeploy(1, 4);
		assertTrue(newFile.exists());
		sqt.tidyUp(newFile, ro);
		assertTrue(ro.toString().contains("successful"));
		assertFalse(newFile.exists());
	}

}
