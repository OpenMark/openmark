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

/** Exception-related utilities (static class) */
public abstract class Exceptions
{
	/** 
	 * Strings used to indicate exception trace within the message of another
	 * exception.
	 */ 
	public final static String BEGIN="[[BEGINEXCEPTION]]",END="[[ENDEXCEPTION]]";
	
	/**
	 * Converts an exception (including class, message, and trace) into a nice 
	 * string to display to user. Automatically trims unwanted crap such as Axis 
	 * fault details and similar. 
	 * <p>
	 * If Web service places a string to be displayed here (e.g. a stack trace)
	 * in the Axis fault string (which corresponds to the message of the 
	 * exception that you threw inside the actual service) beginning with a 
	 * [[BEGINEXCEPTION]] and ending with [[ENDEXCEPTION]] then that will
	 * be used instead as the complete thing to display.
	 * @param tException Exception to display
	 * @param asPrefixes Array of package prefixes you're actually interested
	 *   in; trace lines in these packages and subpackages will always be 
	 *   displayed, while others are subject to possible removal. Do not include
	 *   the final . (e.g. "om", not "om.")
	 * @return String describing exception
	 */
	public static String getString(Throwable tException,String[] asPrefixes)
	{
		String NL=System.getProperty("line.separator");
		
		StringWriter sw=new StringWriter();
		PrintWriter pw=new PrintWriter(sw);
		tException.printStackTrace(pw);
		pw.close();
		String sResult=sw.toString();
		
		// Check for a fixed exception name
		String sIncludedException=null;
		int iBegin=sResult.indexOf(BEGIN);
		if(iBegin!=-1)
		{
			int iEnd=sResult.indexOf(END,iBegin);
			if(iEnd!=-1)
			{
				sIncludedException=sResult.substring(iBegin+BEGIN.length(),iEnd);
			}
		}

		// Trim exceptions down
		try
		{
			StringBuffer sbRelevant=new StringBuffer(tException.getClass().getName());
			if(tException.getMessage()!=null)
			{
				String sTidied=tException.getMessage().replaceFirst("<html>.*<pre>","  ");
				sTidied=sTidied.replaceFirst("</pre>.*</html>","");
				sTidied=sTidied.replaceFirst("Response message.*,","");
				sTidied=sTidied.replaceAll("\\t","  ");
				sTidied=sTidied.replaceAll("[\\n\\r]+","\n");
				sbRelevant.append(": "+sTidied);
			}
			sbRelevant.append(NL+NL);

			boolean bIncludeAll=false,bInitialStage=true,bIncludeNext=false,
			  bDoneDots=false;
			BufferedReader br=new BufferedReader(new StringReader(sResult));
			while(true)
			{
				String sLine=br.readLine();
				if(sLine==null) break;

				// See if it's a causes line, after that we include everything
				if(sLine.startsWith("Caused by: ")) bIncludeAll=true;
				// Are we including everything?
				if(bIncludeAll)
				{
					sbRelevant.append(sLine);
					sbRelevant.append(NL);
					bDoneDots=false;
					continue;
				}

				// If it's not a trace line, skip it
				if(!sLine.startsWith("\tat "))
				{
					if(bInitialStage)
						continue;
					else
						break;
				}

				// Remove the 'at' part
				sLine=sLine.substring(4);

				// Lines beginning with any prefix we always include
				boolean bPrefix=false;
				for(int iPrefix=0;iPrefix<asPrefixes.length;iPrefix++)
				{
					if(sLine.startsWith(asPrefixes[iPrefix]+"."))
					{
						bPrefix=true;
						break;
					}
				}
				
				if(bPrefix)
				{
					bInitialStage=false;
					bIncludeNext=true;
					sbRelevant.append(sLine);
					sbRelevant.append(NL);
					bDoneDots=false;
					continue;
				}

				// Initially we add everything until we get to promises.
				if(bInitialStage)
				{
					// Axis faults we don't list anything until we hit relevant bits
					// [org.apache.axis.AxisFault]
					if(tException.getClass().getName().equals("AxisFault"))
					{
						if(!bDoneDots)
						{
							sbRelevant.append("...");
							sbRelevant.append(NL);
							bDoneDots=true;
						}
					}
					// Other exceptions we list everything before hitting promises
					else
					{
						sbRelevant.append(sLine);
						sbRelevant.append(NL);
						bDoneDots=false;
					}
					continue;
				}

				// Lines after promises.
				if(bIncludeNext)
				{
					sbRelevant.append(sLine);
					sbRelevant.append(NL);
					bIncludeNext=false;
					bDoneDots=false;
					continue;
				}

				// Otherwise do ellipsis
				if(!bDoneDots)
				{
					sbRelevant.append("...");
					sbRelevant.append(NL);
					bDoneDots=true;
				}
			}
			sResult=sbRelevant.toString();
		}
		catch(IOException ioe)
		{
			return "Exception occurred while processing exception string (basically impossible): "+ioe;
		}
		
		if(sIncludedException!=null)
		{
			// Include a few lines just before the Axis invoke call, if present
			String[] asLines=sResult.split("\r?\n");
			int iLastInvoke=-1;
			for(int i=asLines.length-1;i>=0;i--)
			{
				if(asLines[i].matches(
						".*org\\.apache\\.axis\\.client\\.Call\\.invoke\\(.*"))
				{
					iLastInvoke=i;
					break;
				}
			}
			
			if(iLastInvoke!=-1)
			{
				sIncludedException+="\n\nAxis client call:\n\n";
				for(int i=iLastInvoke+1;i<iLastInvoke+9 && i<asLines.length;i++)
				{
					sIncludedException+=asLines[i].replaceAll("^\\s*at ","")+"\n";					
				}
			}
			return sIncludedException;
		}

		return sResult;
	}

	public static void main(String[] args)
	{		
		String sTest="	at org.apache.axis.client.Call.invoke(Call.java:1804)\n";
		System.out.println(sTest.matches(
				".*org\\.apache\\.axis\\.client\\.Call\\.invoke\\(.*"
				));
	}
	
}
