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
package om.loadtest;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * Represents a test script created by saving from the Firefox 
 * LiveHTTPHeaders extension (with minor modifications).
 */
public class HttpScript
{
	/** Regexp to match content-length header in POST request */
	public final static Pattern CONTENTLENGTH=Pattern.compile(
		"Content-Length: ([0-9]+)");
		
	/** Single thing in HTTP script */
	public static class Item
	{
		/** Requested URL */
		private String sURL;
		
		/** Request lines, each terminated by \r\n (does not include extra closing \r\n) */
		private String sRequest;
		
		/** Data for POST request or NULL if none */
		private String sData;
		
		/** Response status code */
		private int iExpectedResponse;
		
		/** @return Requested URL */
		public String getURL() { return sURL; }		
		/** @return Request lines, each terminated by \r\n (does not include extra closing \r\n) */
		public String getRequest() { return sRequest; }		
		/** @return Data for POST request or NULL if none */		
		public String getData() { return sData; }		
		/** @return Response */
		public int getExpectedResponse() { return iExpectedResponse; }
		
		/** Reads next item from file */
		boolean read(BufferedReader br) throws IOException
		{
			// Read URL
			sURL=br.readLine();
			if(sURL==null) return false;
			
			// Blank line
			check(br,"");
			
			// Header
			int iDataSize=0;
			sRequest="";
			while(true)
			{
				String sHeaderLine=br.readLine();
				if(sHeaderLine==null) throw new IOException("Unexpected EOF in request headers");
				if(sHeaderLine.equals("")) break;
				sRequest+=sHeaderLine+"\r\n";
				Matcher m=CONTENTLENGTH.matcher(sHeaderLine);
				if(m.matches())
				{
					iDataSize=Integer.parseInt(m.group(1));
					break;
				}					
			}		
			
			// Data
			if(iDataSize!=0)
			{
				sData="";
				for(int i=0;i<iDataSize;i++)
				{
					int iChar=br.read();
					if(iChar==-1) throw new IOException("Unexpected EOF in POST data");
					sData+=(char)iChar;
				}
				check(br,"");
			}
			
			// Response
			iExpectedResponse=0;
			while(true)
			{
				String sHeaderLine=br.readLine();
				if(sHeaderLine==null) throw new IOException("Unexpected EOF in response headers");
				if(sHeaderLine.startsWith("----------------------------------------------------------")) break;
				if(iExpectedResponse==0) // First line, get response
				{
					Matcher m=STATUSCODE.matcher(sHeaderLine);
					if(!m.matches())
						throw new IOException("Unexpected response first line");
					iExpectedResponse=Integer.parseInt(m.group(1));
				}
			}		
			
			return true;
		}
	}
	
	/** Regexp for extracting HTTP status code. */
	public final static Pattern STATUSCODE=Pattern.compile(
		"HTTP/1.[01x] ([0-9]+) .*");
	
	/** List of items */
	private List<HttpScript.Item> lItems=new LinkedList<HttpScript.Item>();
	
	/** @return List of all items in script */
	public HttpScript.Item[] getItems()
	{
		return lItems.toArray(new HttpScript.Item[lItems.size()]);
	}
	
	/** 
	 * Constructs script.
	 * @param is Input stream containing script
	 * @throws IOException If the script format is incorrect in any way
	 */
	public HttpScript(InputStream is) throws IOException
	{
		// Note that this lazily assumes everything is plaintext in default
		// character set (in the test script I'm using, it is)
		BufferedReader br=new BufferedReader(new InputStreamReader(is));
		
		while(true)
		{
			HttpScript.Item i=new Item();
			if(!i.read(br)) break;
			lItems.add(i);				
		}			
	}
	
	/**
	 * Check for required line.
	 * @param br Reader
	 * @param sTest Required text
	 * @throws IOException If they don't match
	 */
	private static void check(BufferedReader br,String sTest) throws IOException
	{
		String sLine=br.readLine();
		if(sLine==null) throw new IOException("Unexpected EOF in HTTP script");
		if(!sLine.equals(sTest)) throw new IOException("Unexpected line in HTTP script: "+sLine);
	}
}