package om.misc;

import org.apache.commons.lang.StringUtils;

import util.misc.QuestionName;
import util.misc.VersionUtil;
import junit.framework.TestCase;

public class TestVersionUtil extends TestCase {

	public void testRepresentedWithNullName() {
		QuestionName qn = VersionUtil.represented(null);
		assertNull(qn);
	}

	public void testRepresentedWithEmptyName() {
		QuestionName qn = VersionUtil.represented("");
		assertNull(qn);
	}

	public void testRepresentedWithWrongName() {
		QuestionName qn = VersionUtil.represented("sdfsdf");
		assertNull(qn);
	}

	public void testRepresentedWithName() {
		QuestionName qn = VersionUtil.represented("sdk125b6.question02f.1.2.jar");
		assertNotNull(qn);
		assertTrue(StringUtils.isNotEmpty(qn.getPrefix()));
		assertEquals(qn.getPrefix(), "sdk125b6.question02f");
		assertNotNull(qn.getQuestionVersion());
		assertEquals(qn.getQuestionVersion().iMajor, 1);
		assertEquals(qn.getQuestionVersion().iMinor, 2);
	}

	public void testRepresentedWithValidName() {
		QuestionName qn = VersionUtil.represented("sdk125b6.question02f.112.22.jar");
		assertNotNull(qn);
		assertTrue(StringUtils.isNotEmpty(qn.getPrefix()));
		assertEquals(qn.getPrefix(), "sdk125b6.question02f");
		assertNotNull(qn.getQuestionVersion());
		assertEquals(qn.getQuestionVersion().iMajor, 112);
		assertEquals(qn.getQuestionVersion().iMinor, 22);
	}

}
