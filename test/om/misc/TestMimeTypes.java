package om.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import om.AbstractTestCase;

import org.junit.Test;

import util.misc.MimeTypes;

public class TestMimeTypes extends AbstractTestCase {

	@Test public void testMimeType() {
		File f = pickUpFile(BASIC_TEST_DEFINITION);
		assertNotNull(f);
		String mimeType = MimeTypes.getMimeType(f.getAbsolutePath());
		assertNotNull(mimeType);
		assertEquals("application/xml", mimeType);
	}

	@Test public void testJarMimeType() {
		File f = pickUpFile("s383.icma55.q04angular.1.7.jar");
		assertNotNull(f);
		String mimeType = MimeTypes.getMimeType(f.getAbsolutePath());
		assertNotNull(mimeType);
		assertEquals("application/octet-stream", mimeType);
	}

	@Test public void testGifMimeType() {
		File f = pickUpFile("oulogo_hor.gif");
		assertNotNull(f);
		String mimeType = MimeTypes.getMimeType(f.getAbsolutePath());
		assertNotNull(mimeType);
		assertEquals("image/gif", mimeType);
	}

	@Test public void testPNGMimeType() {
		File f = pickUpFile("pngMimeTypeTestImage.PNG");
		assertNotNull(f);
		String mimeType = MimeTypes.getMimeType(f.getAbsolutePath());
		assertNotNull(mimeType);
		assertEquals("image/png", mimeType);
	}
}
