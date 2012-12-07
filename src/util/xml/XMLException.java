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
package util.xml;

import util.misc.CausedIOException;


/**
 * Exception thrown by the XML utilities. Extends IOException since this is
 * likely to be cause by data format errors, which are to do with I/O.
 */
public class XMLException extends CausedIOException
{
	/** Required by the Serializable interface. */
	private static final long serialVersionUID = 5953044501477270376L;

	/**
	 * @param s Error message
	 */
	public XMLException(String s)
	{
		super(s, null);
	}

	/**
	 * @param s Error message
	 * @param e Exception that caused error
	 */
	public XMLException(String s, Exception e) {
		super(s, e);
	}

	/**
	 * @param e Exception that caused error
	 */
	public XMLException(Exception e)
	{
		super(e);
	}
}
