package util.misc;

import java.net.URL;

public interface OmClassLoaderContract {

	void close();

	Class<?> identifyClass(String sName) throws ClassNotFoundException;

	URL identifyResource(String sName);

	Class<?> loadClass(String className) throws ClassNotFoundException;

}
