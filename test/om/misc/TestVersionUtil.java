package om.misc;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import util.misc.QuestionName;
import util.misc.VersionUtil;

public class TestVersionUtil extends TestCase {

	@Test public void testRepresentedWithNullName() {
		QuestionName qn = VersionUtil.represented(null);
		assertNull(qn);
	}

	@Test public void testRepresentedWithEmptyName() {
		QuestionName qn = VersionUtil.represented("");
		assertNull(qn);
	}

	@Test public void testRepresentedWithWrongName() {
		QuestionName qn = VersionUtil.represented("sdfsdf");
		assertNull(qn);
	}

	@Test public void testRepresentedWithName() {
		QuestionName qn = VersionUtil.represented("sdk125b6.question02f.1.2.jar");
		assertNotNull(qn);
		assertTrue(StringUtils.isNotEmpty(qn.getPrefix()));
		assertEquals(qn.getPrefix(), "sdk125b6.question02f");
		assertNotNull(qn.getQuestionVersion());
		assertEquals(qn.getQuestionVersion().iMajor, 1);
		assertEquals(qn.getQuestionVersion().iMinor, 2);
	}

	@Test public void testRepresentedWithValidName() {
		QuestionName qn = VersionUtil.represented("sdk125b6.question02f.112.22.jar");
		assertNotNull(qn);
		assertTrue(StringUtils.isNotEmpty(qn.getPrefix()));
		assertEquals(qn.getPrefix(), "sdk125b6.question02f");
		assertNotNull(qn.getQuestionVersion());
		assertEquals(qn.getQuestionVersion().iMajor, 112);
		assertEquals(qn.getQuestionVersion().iMinor, 22);
	}

}
