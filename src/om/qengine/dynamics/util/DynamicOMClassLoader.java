package om.qengine.dynamics.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import util.misc.OmClassLoaderContract;

public class DynamicOMClassLoader extends ClassLoader
	implements OmClassLoaderContract {

	private String pathLocation;

	public DynamicOMClassLoader(String path, ClassLoader clParent) throws IOException {
		super(clParent);
		pathLocation = path;
	}

	/**
	 * Close classloader and its jar file connection.
	 * <p>
	 * Be sure to null all references to the classloader and to classes loaded by
	 * it before (or shortly after) calling this method.
	 * <p>
	 * You shouldn't need to use this method if you close it and do System.gc
	 * a bit, but it's probably safer if you do.
	 */
	public synchronized void close() {
		// TODO ...
	}

	@Override
	protected synchronized Class<?> findClass(String sName) throws ClassNotFoundException {
		Class<?> cla = null;
		if (null == cla) {
			if (null != pathLocation ? pathLocation.length() > 0 : false) {
				File f = new File(pathLocation + sName.replace('.','/') + ".class");
				if (f.exists()) {
					try {
						FileInputStream fio = new FileInputStream(f);
						byte[] abData=new byte[ (int) f.length() ];
						for(int iRead=0;iRead<abData.length;) {
							int iThisTime=fio.read(abData,iRead,abData.length-iRead);
							if(iThisTime==0) throw new ClassNotFoundException(
								"Unexpected error reading class: "+sName);
							iRead+=iThisTime;
						}
						cla = defineClass(sName,abData,0,abData.length);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return cla;
	}

	public URL identifyResource(String sName) {
		return findResource(sName);
	}

	public Class<?> identifyClass(String sName) throws ClassNotFoundException {
		return findClass(sName);
	}
}
