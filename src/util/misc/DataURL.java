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
import java.net.*;

/** Allows you to create a URL object that refers to a byte array */
public abstract class DataURL
{
	/** 
	 * Creates a URL referring to the given data.
	 * @param abData Data bytes
	 * @return URL object
	 */
	public static URL create(byte[] abData)
	{
		try
		{
			return new URL("data","bytes",-1,""+abData.length,
				new MyStreamHandler(abData));
		}
		catch(MalformedURLException mue)
		{
			throw new Error("Can't happen",mue);
		}
	}

	private static class MyStreamHandler extends URLStreamHandler
	{
		private byte[] abData;
		
		public MyStreamHandler(byte[] abData)
		{
			this.abData=abData;
		}
		
		protected URLConnection openConnection(URL u) throws IOException
		{
			return new MyConnection(u,abData);
		}
	}
	
	private static class MyConnection extends URLConnection
	{
		private ByteArrayInputStream bais;
		
		public MyConnection(URL u,byte[] abData)
		{
			super(u);
			bais=new ByteArrayInputStream(abData);
		}
		
		public void connect() throws IOException
		{
		}
		
		public InputStream getInputStream() throws IOException
		{
			return bais;
		}
	}
	
}
