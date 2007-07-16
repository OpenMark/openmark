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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Implement our own mapping of extensions to mime-types. */
public class MimeTypes
{
	/** Map from extension (after .) to MIME type */
	private static Map<String,String> mTypes=null;

	/** Pattern matches a valid non-comment line in mime.types. Captures are
	 * $1 = MIME type, $2 = whitespace-separated list of extensions */
	private final static Pattern TYPESLINE=Pattern.compile(
		"(?!#)(\\S+)((?:\\s+(?:\\S+))+)");

	/**
	 * Given a file or path name, return the mime-type
	 * @param sPath The path/file name
	 * @return the deduced mime-type
	 */
	public static String getMimeType(String sPath)
	{
		if(mTypes==null)
		{
			mTypes=new HashMap<String,String>();
			try
			{
				// Read mime.types
				BufferedReader br=new BufferedReader(new InputStreamReader(
					MimeTypes.class.getResourceAsStream("mime.types"),"US-ASCII"));
				while(true)
				{
					// Read line at time until EOF
					String sLine=br.readLine();
					if(sLine==null) break;

					// Ignore comments, blank lines, etc.
					Matcher m=TYPESLINE.matcher(sLine);
					if(!m.matches()) continue;
					String sType=m.group(1);
					String[] asExtensions=m.group(2).split("\\s+");
					for(int iExtension=0;iExtension<asExtensions.length;iExtension++)
					{
						if(asExtensions[iExtension].length()>0)
						{
							mTypes.put(asExtensions[iExtension].toLowerCase(),sType);
						}
					}
				}
			}
			catch(Throwable t)
			{
				mTypes=null;
				throw new Error("Unexpected error reading mime.types",t);
			}
		}

		String sExtension="";
		int iDot=sPath.lastIndexOf('.');
		if(iDot!=-1)
		{
			sExtension=sPath.substring(iDot+1).toLowerCase();
		}

		String sType=mTypes.get(sExtension);
		if(sType!=null)
			return sType;
		else
			return "application/octet-stream";
	}

	/**
	 * main method for testing.
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.err.println(getMimeType("jre-1_5_0-windows-i586.exe"));
	}
}