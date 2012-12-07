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

import java.io.IOException;

/**
 * A class for an IO exception that was caused by another exception. The main purpose of this
 * class is to allow you to rethrow another exception as in IOException in one line of code.
 * @version $Revision: 1.1 $
 */
public class CausedIOException extends IOException {
	/** Required by the Serializable interface. */
	private static final long serialVersionUID = 2757846255722860059L;

	/**
	 * @param e the exception that is causing this one. This exception takes its message from e.
	 */
	public CausedIOException(Exception e) {
		super(e.getMessage());
		initCause(e);
	}

	/**
	 * @param sMessage the error message.
	 * @param e the exception that is causing this one - can be null.
	 */
	public CausedIOException(String sMessage, Exception e) {
		super(sMessage);
		initCause(e);
	}
}
