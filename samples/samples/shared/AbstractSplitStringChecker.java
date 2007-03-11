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
package samples.shared;

/**
 *  Title:	checking.AbstractSplitStringChecker
 *  Description: Class that split a string into an initial number
 *  and the "rest" useful for parsing scientific notation etc
 *  User: prm96
 *  Date: 14-Jun-02
 *  Time: 09:36:56
 *
 *@author     prm96
 *@created    26 June 2002
 *@version    $Id: AbstractSplitStringChecker.java,v 1.1 2006/02/16 10:34:05 pgb2 Exp $
 */
public abstract class AbstractSplitStringChecker {
	/**
	 *  The complete string
	 */
	protected String complete;
	/**
	 *  The number that forms the start of the string
	 */
	protected String mantissa;
	/**
	 *  The string apart from the number at the start
	 */
	protected String residue;


	/**
	 *  Separates the initial number from the rest of the string
	 *
	 *@param  input  The string to be split
	 */
	protected void splitNumberFromString(String input) {
		StringBuffer sb = new StringBuffer();
		char current;
		for (int i = 0; i < input.length(); i++) {
			current = input.charAt(i);
			if (!Character.isWhitespace(current)) {
				sb.append(current);
			}
		}
		complete = sb.toString();

		int cutPoint = 0;
		current = ' ';

		boolean letterFound = false;

		if (complete != null) {

			while (!letterFound && cutPoint < complete.length()) {
				current = complete.charAt(cutPoint++);
				if (Character.isLetter(current)) {
					letterFound = true;
				}
			}
		}
		// if we still haven't found a letter the whole string is the number
		if (!letterFound) {
			mantissa = complete;
			residue = "";
		}
		// otherwise just the last letter
		else {
			mantissa = complete.substring(0, cutPoint - 1);
			residue = complete.substring(cutPoint - 1);
		}
	}
}

