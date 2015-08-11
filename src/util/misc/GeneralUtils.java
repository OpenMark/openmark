package util.misc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import om.Log;
import om.OmException;



public class GeneralUtils implements Serializable {

	private static final long serialVersionUID = -4641935806195155600L;

	private static String DOT = ".";

	public static String toString(Object o) {
		StringBuffer sb = new StringBuffer();
		if (null != o) {
			Method[] methods = o.getClass().getMethods();
			for (int i = 0; i < methods.length; i++) {
				Method m = methods[i];
				if (null != m ? m.getName().startsWith("get") : false) {
					Class<?>[] clas = m.getParameterTypes();
					if (clas.length == 0) {
						sb.append(" \n").append(m.getName()).append(" = ");
						try {
							sb.append(m.invoke(o, new Object[]{}));
						} catch (IllegalArgumentException x) {
							sb.append(x.getMessage());
						} catch (IllegalAccessException x) {
							sb.append(x.getMessage());
						} catch (InvocationTargetException x) {
							sb.append(x.getMessage());
						}
					}
				}
			}
		}
		return sb.toString();
	}

	public static File generateFile(String name, String content)
		throws FileNotFoundException, IOException {
		File f = null;
		if (Strings.isNotEmpty(name) && Strings.isNotEmpty(content)) {
			f = new File(name);
			f.createNewFile();
			copyToFile(f, content);
		} else {
			throw new IOException("Unable to create the file with the name of : "
				+ name + " and with the contents of " + content
				+ "\n\n both need to not be empty / null.");
		}
		return f;
	}

	public static void copyToFile(File f, String content)
		throws FileNotFoundException, IOException {
		InputStream in = new ByteArrayInputStream(content.getBytes());
		FileOutputStream out = new FileOutputStream(f);
		byte[] buf = new byte[1024];
		int r;
		while ((r = in.read(buf)) != -1)
			out.write(buf, 0, r);
		in.close();
		out.close();
	}

	/**
	 * Used to copy the contents of one directory to another.
	 * @param sourceLocation
	 * @param targetLocation
	 * @throws IOException
	 */
	public static void copyDirectory(File sourceLocation, File targetLocation)
		throws IOException {
		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}
			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
			}
		} else {
			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);
			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	public static Object invoke(Method m, Object obj, Object[] args)
		throws OmException {
		Object result = null;
		if (null != m && null != obj) {
			try {
				result = m.invoke(obj, args);
			} catch (IllegalArgumentException x) {
				throw new OmException(x);
			} catch (IllegalAccessException x) {
				throw new OmException(x);
			} catch (InvocationTargetException x) {
				throw new OmException(x);
			}
		}
		return result;
	}

	public static void copyFile(File source, File target) throws IOException {
		FileInputStream in = new FileInputStream(source);
		FileOutputStream out = new FileOutputStream(target);
		byte[] buf = new byte[1024];
		int r;
		while ((r = in.read(buf)) != -1)
			out.write(buf, 0, r);
		in.close();
		out.close();
	}

	@SuppressWarnings("unchecked")
	public static <T> T loadComponent(Class<T> clazz, String name)
			throws UtilityException {
		T t = null;
		if (Strings.isNotEmpty(name) && null != clazz) {
			try {
				Class<?> cla = Class.forName(name);
				Object obj = load(cla);
				if (null != obj ? clazz.isAssignableFrom(obj.getClass())
						: false) {
					t = (T) obj;
				}
			} catch (ClassNotFoundException x) {
				throw new UtilityException(x);
			}
		}
		if (null == t) {
			throw new UtilityException("The component was not loaded : " + name
					+ " - check if it is assignable from the class : " + clazz);
		}
		return t;
	}

	public static Properties loadPropertiesFromClassPath(String name)
			throws IOException {
		Properties properties = null;
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		URL url = loader.getResource(name);
		if (null == url) {
			url = loader.getResource("/" + name);
		}
		if (null != url) {
			InputStream is = url.openStream();
			properties = new Properties();
			properties.load(is);
		}
		return properties;
	}

	/**
	 * Only possible with a class that has an empty constructor.
	 * 
	 * @param cla
	 * @return
	 */
	public static Object load(Class<?> cla) throws UtilityException {
		Object obj = null;
		if (null != cla) {
			try {
				obj = cla.getConstructor(new Class[] {}).newInstance(
						new Object[] {});
			} catch (Exception x) {
				throw new UtilityException(x);
			}
		}
		return obj;
	}

	public static Log getLog(Class<?> cla, String path, boolean showDebug)
			throws UtilityException {
		Log log = null;
		try {
			log = new Log(new File(path), cla.getName(), showDebug);
		} catch (IOException x) {
			throw new UtilityException("Error creating log", x);
		}
		return log;
	}

	public static String timeNow(String format) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(cal.getTime());
	}

	public static String questionNamePrefix(String fileName) {
		String s = null;
		if (Strings.isNotEmpty(fileName)) {
			int n = fileName.lastIndexOf(DOT);
			if (n > -1 ? fileName.length() > n + 1 : false) {
				s = fileName.substring(0, n);
				s = stripLastDot(s);
				s = stripLastDot(s);
			}
		}
		return s;
	}

	public static String stripLastDot(String str) {
		String s = null;
		if (Strings.isNotEmpty(str) ? str.contains(DOT) : false) {
			int n = str.lastIndexOf(DOT);
			if (n > -1 ? str.length() > n + 1 : false) {
				s = str.substring(0, n);
			}
		}
		return s;
	}
	
	/**
	 * The 'SAMS2session' cookie contains invalid characters and cannot be
	 * read by normal means.
	 * @return Cookie value or null if not included
	 */
	private final static Pattern REGEX_BROKEN_SAMS_COOKIE =	Pattern.compile("(?:^|; )SAMS2session=([^;]+)(?:$|; )");

	private final static String SAMS2COOKIE = "SAMS2session";

	/**
	 * Obtains cookie based on its name.
	 *
	 * @param request HTTP request
	 * @param sName Name of cookie
	 * @return Cookie value, or null if the cookie does not exist.
	 */
	public static String getCookie(HttpServletRequest request, String sName) {
		// If we are looking at the sams 2 cookie, then use the special function
		// that reads it from headers.
		if(sName.equals(SAMS2COOKIE))
		{
			return GeneralUtils.getBrokenSamsCookie(request);
		}
		else
		{
			Cookie[] ac = request.getCookies();
			if (ac == null)
				ac = new Cookie[0];
			for (int iCookie = 0; iCookie < ac.length; iCookie++) {
				if (ac[iCookie].getName().equals(sName))
					return ac[iCookie].getValue();
			}
		}
		return null;
	}

	public static String getBrokenSamsCookie(HttpServletRequest request)
	{
		Enumeration<?> e = request.getHeaders("Cookie");
		while(e.hasMoreElements())
		{
			String header = (String)e.nextElement();
			Matcher m = REGEX_BROKEN_SAMS_COOKIE.matcher(header);
			if(m.find())
			{
				return m.group(1);
			}
		}
		return null;
	}
	
	/* return true if not a temporary user, and oucu/pui are not null, or empty AND they are equal
	 * 
	 * */
	public static boolean isOUCUPIequalButNotTemp(String sOUCU, String sPi)
	{
		String trimOUCU= sOUCU.replace(" ", "");
		String trimPi= sPi.replace(" ", "");

		boolean isEqual=(!(sOUCU == null || sOUCU.isEmpty()) && !(sOUCU.startsWith("_")) && 
				(!(sPi == null || sPi.isEmpty())  && trimPi.equals(trimOUCU)) );
		return isEqual;
	}
	

}
