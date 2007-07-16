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
package om.question;

/** Represents a resource file provided by a question */
public class Resource
{
	/** Resource filename */
	private String sFilename;
	/** Resource MIME type */
	private String sMimeType;
	/** Resource character encoding if appropriate */
	private String sEncoding;
	/** Resource content */
	private byte[] abContent;

	/**
	 * Stores the three pieces of information together.
	 * @param sFilename Resource filename
	 * @param sMimeType Resource MIME type
	 * @param sEncoding Character encoding
	 * @param abContent Resource content
	 */
	public Resource(String sFilename,String sMimeType,String sEncoding,byte[] abContent)
	{
		this.sFilename=sFilename;
		this.sMimeType=sMimeType;
		this.sEncoding=sEncoding;
		this.abContent=abContent;
	}

	/**
	 * Stores the three pieces of information together.
	 * @param sFilename Resource filename
	 * @param sMimeType Resource MIME type
	 * @param abContent Resource content
	 */
	public Resource(String sFilename,String sMimeType,byte[] abContent)
	{
		this.sFilename=sFilename;
		this.sMimeType=sMimeType;
		this.abContent=abContent;
	}

	/** @return Resource filename */
	public String getFilename() { return sFilename; }
	/** @return Resource MIME type */
	public String getMimeType() { return sMimeType; }
	/** @return Character encoding (null if not a text type) */
	public String getEncoding() { return sEncoding; }
	/** @return Resource content */
	public byte[] getContent() { return abContent; }
}