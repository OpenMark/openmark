package om.misc;

import util.misc.GeneralUtils;
import junit.framework.TestCase;

public class TestGeneralUtils extends TestCase {

	public void testFileNamePrefixNull() {
		String s = GeneralUtils.questionNamePrefix(null);
		assertNull(s);
	}

	public void testFileNamePrefixEmpty() {
		String s = GeneralUtils.questionNamePrefix("");
		assertNull(s);
	}
	
	public void testFileNamePrefixInvalid() {
		String s = GeneralUtils.questionNamePrefix("noDotHereThen");
		assertNull(s);
	}

	public void testFileNamePrefixValid() {
		String s = GeneralUtils.questionNamePrefix("sdk125b6.question02f.1.2.jar");
		assertNotNull(s);
		assertEquals(s, "sdk125b6.question02f");
	}

	public void testFileNamePrefixValidMajor() {
		String s = GeneralUtils.questionNamePrefix("sdk125b6.question02f.1223.12.jar");
		assertNotNull(s);
		assertEquals(s, "sdk125b6.question02f");
	}

	public void testFileNamePrefixValidMinor() {
		String s = GeneralUtils.questionNamePrefix("sdk125b6.question02f.1.12.jar");
		assertNotNull(s);
		assertEquals(s, "sdk125b6.question02f");
	}

	public void testFileNamePrefixInValidName() {
		String s = GeneralUtils.questionNamePrefix("sdk125b6.question02f..1.2.jar");
		assertNotNull(s);
		assertEquals(s, "sdk125b6.question02f.");
	}

	public void testFileNamePrefixNoJarSuffix() {
		String s = GeneralUtils.questionNamePrefix("sdk125b6.question02f.1.2.");
		assertNull(s);
	}

	public void testFileNamePrefixNoJarSuffixOrDot() {
		String s = GeneralUtils.questionNamePrefix("sdk125b6.question02f.1.2");
		assertNotNull(s);
		assertEquals(s, "sdk125b6");
	}
}
