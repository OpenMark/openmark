package om.tnavigator.request.tinymce;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.OmUnexpectedException;
import om.abstractservlet.AbstractRequestHandler;
import om.abstractservlet.RequestAssociates;
import om.abstractservlet.RequestHandlingException;
import om.abstractservlet.RequestResponse;
import util.misc.FileTypesEnum;
import util.misc.IO;
import util.misc.MimeTypes;
import util.misc.Strings;

public class TinyMCERequestHandler extends AbstractRequestHandler {

	private static final long serialVersionUID = 1907168380166795247L;

	private static String SHARED_AREA = "WEB-INF/shared/";

	public static String FILE_PATH = "filePath";

	private static String HEIGHT = "[HEIGHT]";

	private static String WIDTH = "[WIDTH]";

	private static String EDITOR_SELECTOR = "[EDITOR_SELECTOR]";

	private static String BUTTONS = "[BUTTONS]";

	private static String VALID_ELEMENTS = "[VALID_ELEMENTS]";

	private static String ELEMENTS = "[ELEMENTS]";

	private static String READ_ONLY = "[READ_ONLY]";

	private static String UTF_8 = "UTF-8";

	private static String SETTINGS = "settings";

	private static List<String> imageTypes = new ArrayList<String>();

	static {
		imageTypes.add(FileTypesEnum.png.toString());
		imageTypes.add(FileTypesEnum.jpg.toString());
		imageTypes.add(FileTypesEnum.gif.toString());
		imageTypes = Collections.unmodifiableList(imageTypes);
	}

	public RequestResponse handle(HttpServletRequest request,
		HttpServletResponse response, RequestAssociates associates)
		throws RequestHandlingException {
		RequestResponse rr = super.handle(request, response, associates);
		String filePath = associates.getRequestParameters().get(FILE_PATH);
		return handle(response, filePath, associates, rr);
	}

	private RequestResponse handle(HttpServletResponse response, String filePath,
		RequestAssociates ra, RequestResponse rr) throws RequestHandlingException {
		String fullPath = determineTinyMCEResourceFullPath(filePath,
			ra.getServletContext());
		byte[] bytes = {};
		String mimeType = UTF_8;
		String suffix = getFileNameSuffix(fullPath);
		try {
			boolean addExpiresHeader = true;
			if (imageTypes.contains(suffix)) {
				mimeType = MimeTypes.getMimeType(fullPath);
				ImageIO.setUseCache(false);
				BufferedImage bi = ImageIO.read(new File(fullPath));
				bytes = convert(bi, suffix);
			} else {
				File f = new File(fullPath);
				String s = IO.loadString(new FileInputStream(f));
				if (null != f ? f.getName().contains(SETTINGS) : false) {
					s = caterForDynamicSettings(f, s, ra);
					addExpiresHeader = false;
				}
				bytes = s.getBytes(mimeType);
			}
			response.setCharacterEncoding(mimeType);
			response.setContentLength(bytes.length);
			if (addExpiresHeader) {
				// Set expiry for 4 hours ...
				response.addDateHeader("Expires", System.currentTimeMillis()
					+ 4L * 60L * 60L * 1000L);
			}
			rr.setByteOutput(bytes);
		} catch (OmUnexpectedException x) {
			throw new RequestHandlingException(x);
		} catch (FileNotFoundException x) {
			throw new RequestHandlingException(x);
		} catch (UnsupportedEncodingException x) {
			throw new RequestHandlingException(x);
		} catch (IOException x) {
			throw new RequestHandlingException(x);
		}
		return rr;
	}

	private String caterForDynamicSettings(File f, String fileContent,
		RequestAssociates ra) {
		String accommodatedFor = fileContent;
		if (null != f ? f.getName().contains(SETTINGS) : false) {
			String height = ra.getRequestParameters().get("h");
			String width = ra.getRequestParameters().get("w");
			String buttons = ra.getRequestParameters().get("t");
			String elements = ra.getRequestParameters().get("e");
			String isEnabled = ra.getRequestParameters().get("ro");
			String editor_selector = ra.getRequestParameters().get("es");
			if (!valid(height)) {
				height = "100";
			}
			if (!valid(width)) {
				width = "100";
			}
			if (!valid(buttons)) {
				buttons = "sup,sub";
			}
			if (!valid(elements)) {
				elements = "elm1";
			}
			if (!valid(editor_selector)) {
				editor_selector = "elm1";
			}
			if (!valid(isEnabled)) {
				isEnabled = "readonly : false";
			} else {
				if ("true".equalsIgnoreCase(isEnabled)) {
					isEnabled = "readonly : false";
				} else {
					isEnabled = "readonly : true";
				}
			}
			fileContent = fileContent.replace(HEIGHT, height);
			fileContent = fileContent.replace(WIDTH, width);
			fileContent = fileContent.replace(BUTTONS, buttons);
			fileContent = fileContent.replace(READ_ONLY, isEnabled);
			fileContent = fileContent.replace(VALID_ELEMENTS,
				determineValidElements(buttons));
			fileContent = fileContent.replace(EDITOR_SELECTOR, editor_selector);
			fileContent = fileContent.replace(ELEMENTS, elements);
			accommodatedFor = fileContent;
		}
		return accommodatedFor;
	}

	private boolean valid(String s) {
		return null != s ? s.length() > 0 : false;
	}

	private String determineValidElements(String s) {
		String validElements = "-sup,-sub";
		if (null != s ? s.length() > 0 : false) {
			if (!s.contains(",")) {
				validElements = "-" + s;
			}
		}
		return validElements;
	}

	private String getFileNameSuffix(String filePath) {
		String suffix = null;
		if (Strings.isNotEmpty(filePath)) {
			int n = filePath.lastIndexOf(".");
			if (n > -1 ? filePath.length() > n + 1 : false) {
				suffix = filePath.substring(n + 1, filePath.length());
			}
		}
		return suffix;
	}

	private String determineTinyMCEResourceFullPath(String filePath,
		ServletContext sc) {
		if (filePath.startsWith("/")) {
			filePath = filePath.substring(1, filePath.length());
		}
		int n = filePath.indexOf("/");
		String fileName = filePath.substring(n + 1, filePath.length());
		if (fileName.contains("?")) {
			int k = fileName.indexOf("?");
			fileName = fileName.substring(0, k);
		}
		String fullPath = null;
		if (fileName.startsWith(SHARED_AREA)) {
			fullPath = sc.getRealPath(fileName);
		} else {
			fullPath = sc.getRealPath(SHARED_AREA + fileName);
		}
		return fullPath;
	}

	/**
	 * Converts an image to a PNG file.
	 * @param bi BufferedImage to convert
	 * @return PNG data
	 * @throws OmUnexpectedException Any error in conversion (shouldn't happen, but...)
	 */
	public static byte[] convert(BufferedImage bi, String formatName)
		throws OmUnexpectedException {
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			ImageIO.setUseCache(false);
			if(!ImageIO.write(bi, formatName, baos))
				throw new IOException("No image writer for " + formatName);
			return baos.toByteArray();
		}
		catch(IOException ioe) {
			throw new OmUnexpectedException(ioe);
		}
	}

}
