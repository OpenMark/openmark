package om.misc;

import java.io.File;

import om.AbstractTestCase;
import util.misc.MimeTypes;

public class TestMimeTypes extends AbstractTestCase {

	public void testMimeType() {
		File f = pickUpFile(BASIC_TEST_DEFINITION);
		assertNotNull(f);
		String mimeType = MimeTypes.getMimeType(f.getAbsolutePath());
		assertNotNull(mimeType);
		System.out.println(mimeType);
		assertEquals("application/xml", mimeType);
	}

	public void testJarMimeType() {
		File f = pickUpFile("s383.icma55.q04angular.1.7.jar");
		assertNotNull(f);
		String mimeType = MimeTypes.getMimeType(f.getAbsolutePath());
		assertNotNull(mimeType);
		System.out.println(mimeType);
		assertEquals("application/octet-stream", mimeType);
	}

	public void testGifMimeType() {
		File f = pickUpFile("oulogo_hor.gif");
		assertNotNull(f);
		String mimeType = MimeTypes.getMimeType(f.getAbsolutePath());
		assertNotNull(mimeType);
		System.out.println(mimeType);
		assertEquals("image/gif", mimeType);
	}

	public void testPNGMimeType() {
		File f = pickUpFile("pngMimeTypeTestImage.PNG");
		assertNotNull(f);
		String mimeType = MimeTypes.getMimeType(f.getAbsolutePath());
		assertNotNull(mimeType);
		System.out.println(mimeType);
		assertEquals("image/png", mimeType);
	}
}
