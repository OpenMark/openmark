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
package om.tnavigator;

import java.io.*;

/** 
 * Input stream used for reading logs from a file that makes .log files into
 * valid XML.
 */
public class LogInputStream extends InputStream
{	
	/** Actual underlying inputstream */
	private InputStream is;
	
	/** Current position in append section (0 if none) */
	private int iCursor=0;
	
	/** Bytes to append */
	private byte[] abAppend={'<','/','l','o','g','>'};

	/** 
	 * Reads log from the given file, terminating it with /log to make it into 
	 * proper XML.
	 * @param f Log file
	 * @throws IOException If file errors occur
	 */	
	public LogInputStream(File f) throws IOException
	{
		// Use buffered input stream as this stream only ever reads anything one
		// byte at a time, which is not heroically efficient when coming from
		// a file.
		is=new BufferedInputStream(new FileInputStream(f));
	}

	@Override
	public void close() throws IOException
	{
		is.close();
	}
	
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException
	{
		// If we already passed EOF, return the next append part or -1 if we ran
		// out of that too
		if(iCursor>0)
		{
			if(iCursor < abAppend.length) return abAppend[iCursor++];			
			return -1;
		}
		
		// Read byte from file and use that if not EOF
		int iReturn=is.read();
		if(iReturn!=-1) return iReturn;
		
		// EOF; set cursor to second byte of append, and return first
		iCursor=1;
		return abAppend[0];
	}
}
