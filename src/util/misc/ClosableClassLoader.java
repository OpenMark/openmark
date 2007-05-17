/* OpenMark online assessment system
   Copyright (C) 2007 The Open University

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package util.misc;

import java.io.*;
import java.net.URL;
import java.util.jar.*;

/** 
 * Classloader for jar files that doesn't leave them open infinitely if you
 * read a resource.
 */
public class ClosableClassLoader extends ClassLoader
{
	/** Jar file */
	private JarFile jf;
	
	/**
	 * Constructs classloader.
	 * @param fJar Jar file to use
	 * @param clParent Parent classloader
	 * @throws IOException Any error opening the jar file
	 */
	public ClosableClassLoader(File fJar,ClassLoader clParent) throws IOException
	{
		super(clParent);
		jf=new JarFile(fJar);
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
	public synchronized void close() 
	{
		try
		{
			if(jf!=null) jf.close();
		}
		catch(IOException e)
		{
		}
		jf=null;
	}
	
	
	@Override
	protected synchronized Class<?> findClass(String sName) throws ClassNotFoundException
	{
		if(jf==null) throw new Error("Cannot load: "+sName+", classloader closed");
		try
		{
			JarEntry je=jf.getJarEntry(sName.replace('.','/')+".class");
			if(je==null) throw new ClassNotFoundException("Not found: "+sName);
			
			byte[] abData=new byte[(int)je.getSize()];
			InputStream is=jf.getInputStream(je);
			for(int iRead=0;iRead<abData.length;)
			{
				int iThisTime=is.read(abData,iRead,abData.length-iRead);
				if(iThisTime==0) throw new ClassNotFoundException(
					"Unexpected error reading class: "+sName);
				iRead+=iThisTime;
			}
			
			return defineClass(sName,abData,0,abData.length);
		}
		catch(IOException ioe)
		{
			throw new ClassNotFoundException("Error finding: "+sName,ioe);
		}		
	}
	
	@Override
	public synchronized URL findResource(String sName)
	{
		if(jf==null) throw new Error("Cannot load: "+sName+", classloader closed");
		try
		{
			JarEntry je=jf.getJarEntry(sName);
			if(je==null) return null;
			
			byte[] abData=new byte[(int)je.getSize()];
			InputStream is=jf.getInputStream(je);
			for(int iRead=0;iRead<abData.length;)
			{
				int iThisTime=is.read(abData,iRead,abData.length-iRead);
				if(iThisTime==0) throw new Error(
					"Unexpected error reading resource: "+sName);
				iRead+=iThisTime;
			}

			return DataURL.create(abData);
		}
		catch(IOException ioe)
		{
			throw new Error("Error reading resource: "+sName,ioe);
		}		
	}
	
}
