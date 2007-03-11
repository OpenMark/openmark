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
package om.devservlet;

import java.io.File;
import java.util.*;

import javax.servlet.ServletContext;

import om.*;
import util.misc.IO;

/**
 * Manages the list of questions available in the web application's
 * questions/ subfolder. Caches data for 30 seconds only so you can 
 * safely change it.
 */
public class QuestionDefinitions
{
	/** Amount of time to cache data */
	final static int CACHEMILLISECONDS=30*1000;
	
	/** Map of String (id) -> QuestionDefinition */
	private Map mCache=new HashMap();

	/** Folder where question definitions are kept */
	private File fFolder;
	
	/** Ant home */
	private File fAntHome;
	
	/** JDK home */
	private String sJDKHome;

	
	/**
	 * Constructs for a given servlet.
	 * @param sc Servlet to create for
	 */
	public QuestionDefinitions(ServletContext sc) throws OmException
	{
		fFolder=new File(sc.getRealPath("questions"));
		if(!fFolder.exists()) 
		{
			if(!	fFolder.mkdir()) 
			{
				throw new OmDeveloperException(
					"Unable to create 'questions' folder within Om webapp. Ensure that " +
					"your application server has access to write to this folder.");
			}
		}

		String sAntHome=sc.getInitParameter("ant-home");
		if(sAntHome==null)
			throw new OmDeveloperException(
				"ant-home context parameter not set");
		fAntHome=new File(sAntHome);
		if(!fAntHome.exists())
			throw new OmDeveloperException(
				"ant-home context parameter appears to be set wrong");
		//fAntBat=new File(sc.getRealPath("WEB-INF/apache-ant-1.6.2/bin/ant.bat"));
			
		sJDKHome=sc.getInitParameter("jdk-home");
		if(sJDKHome==null)
			throw new OmDeveloperException(
				"jdk-home context parameter not set");
	}

	
	/** @return Questions folder, surprisingly enough */
	File getQuestionsFolder() { return fFolder; }
	
	/** @return JDK home folder for compiling */
	String getJDKHome() { return sJDKHome; }
	
	String[] getAntCommand()
	{
		String os=System.getProperty("os.name");
		if(os.indexOf("Windows")!=-1)
		{
			return 
				new String[] {
					"cmd","/c",new File(fAntHome,"bin/ant.bat").getAbsolutePath(),
					"-buildfile","questionbuild.ant"
					};
		}
		else
		{
			return 
				new String[] {
					new File(fAntHome,"bin/ant").getAbsolutePath(),
					"-buildfile","questionbuild.ant"
					};
		}
	}
	
	/**
	 * Gets question ID from the definition file.
	 * @param f Definition file
	 * @return Quesiton ID
	 */
	static String getNameFromFile(File f)
	{
		return f.getName().replaceAll(".xml","");	
	}
	
	
	
	/**
	 * Obtains a question definition.
	 * @param sID ID of question definition (= filename before .xml)
	 * @return Question definition object
	 * @throws OmDeveloperException If it doesn't exist or there is an error
	 *   in the format
	 */
	public QuestionDefinition getQuestionDefinition(String sID) throws OmDeveloperException
	{
		// See if it's cached
		QuestionDefinition qd=(QuestionDefinition)mCache.get(sID);
		if(qd!=null && !qd.isOutdated()) return qd;
		
		// Load the file
		qd=new QuestionDefinition(this,new File(fFolder,sID+".xml"));
		mCache.put(sID,qd);
		return qd;
	}
	
	/**
	 * Gets list of all currently-available question definitions.
	 * @return Array of each definition
	 * @throws OmDeveloperException If there's an error in the format of any
	 *   question definition
	 */
	public QuestionDefinition[] getQuestionDefinitions() throws OmDeveloperException
	{
		// List all files in the folder and get or make question definition for each one
		File[] af=IO.listFiles(fFolder);
		List l=new LinkedList();
		for(int i=0;i<af.length;i++)
		{
			if(!af[i].getName().endsWith(".xml")) continue;
			String sID=getNameFromFile(af[i]);
			l.add(getQuestionDefinition(sID));
		}
		
		return (QuestionDefinition[])l.toArray(new QuestionDefinition[0]);
	}

}
