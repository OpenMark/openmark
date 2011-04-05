package util.misc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import om.tnavigator.Log;

public class GeneralUtils {

	public static File generateFile(String name, String content)
		throws FileNotFoundException, IOException {
		File f = null;
		if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(content)) {
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

	public static Object invoke(Method m, Object obj, Object[] args)
		throws UtilityException {
		Object result = null;
		if (null != m && null != obj) {
			try {
				result = m.invoke(obj, args);
			} catch (IllegalArgumentException x) {
				throw new UtilityException(x);
			} catch (IllegalAccessException x) {
				throw new UtilityException(x);
			} catch (InvocationTargetException x) {
				throw new UtilityException(x);
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
		if (StringUtils.isNotEmpty(name) && null != clazz) {
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
			} catch (IllegalArgumentException x) {
				throw new UtilityException();
			} catch (SecurityException x) {
				throw new UtilityException();
			} catch (InstantiationException x) {
				throw new UtilityException();
			} catch (IllegalAccessException x) {
				throw new UtilityException();
			} catch (InvocationTargetException x) {
				throw new UtilityException();
			} catch (NoSuchMethodException x) {
				throw new UtilityException();
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
}
