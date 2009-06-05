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
 *  Title: StringChecker
 *  Description: Interface that declares the basic
 *               functionality of a routine to test a String against a correct input
 *  Copyright: (c) 2002 Open University
 *  $Id: StringChecker.java,v 1.1 2006/02/16 10:34:05 pgb2 Exp $
 *
 *@author
 *@created    26 June 2002
 *@version    1.0
 */

public interface StringChecker {
	/**
	 *  Method to compare a String against "correct" value(s)
	 *
	 *@param  input  The String to be compared
	 *@return        Whether the input matches the "correct" values.
	 */
	public boolean check(String input);
}
