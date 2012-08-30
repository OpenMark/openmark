package om.misc;

import junit.framework.TestCase;

import org.junit.Test;

import util.misc.GeneralUtils;

public class TestGeneralUtils extends TestCase {

	@Test public void testFileNamePrefixNull() {
		String s = GeneralUtils.questionNamePrefix(null);
		assertNull(s);
	}

	@Test public void testFileNamePrefixEmpty() {
		String s = GeneralUtils.questionNamePrefix("");
		assertNull(s);
	}
	
	@Test public void testFileNamePrefixInvalid() {
		String s = GeneralUtils.questionNamePrefix("noDotHereThen");
		assertNull(s);
	}

	@Test public void testFileNamePrefixValid() {
		String s = GeneralUtils.questionNamePrefix("sdk125b6.question02f.1.2.jar");
		assertNotNull(s);
		assertEquals(s, "sdk125b6.question02f");
	}

	@Test public void testFileNamePrefixValidMajor() {
		String s = GeneralUtils.questionNamePrefix("sdk125b6.question02f.1223.12.jar");
		assertNotNull(s);
		assertEquals(s, "sdk125b6.question02f");
	}

	@Test public void testFileNamePrefixValidMinor() {
		String s = GeneralUtils.questionNamePrefix("sdk125b6.question02f.1.12.jar");
		assertNotNull(s);
		assertEquals(s, "sdk125b6.question02f");
	}

	@Test public void testFileNamePrefixInValidName() {
		String s = GeneralUtils.questionNamePrefix("sdk125b6.question02f..1.2.jar");
		assertNotNull(s);
		assertEquals(s, "sdk125b6.question02f.");
	}

	@Test public void testFileNamePrefixNoJarSuffix() {
		String s = GeneralUtils.questionNamePrefix("sdk125b6.question02f.1.2.");
		assertNull(s);
	}

	@Test public void testFileNamePrefixNoJarSuffixOrDot() {
		String s = GeneralUtils.questionNamePrefix("sdk125b6.question02f.1.2");
		assertNotNull(s);
		assertEquals(s, "sdk125b6");
	}
}
