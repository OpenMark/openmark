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
package om;

/**
 * For throwing when an exception occurs that was not expected by the developer.
 */
public class OmUnexpectedException extends RuntimeException
{
	/**
	 * @param t Exception that wasn't expected to happen
	 */
	public OmUnexpectedException(Throwable t)
	{
		super("An unexpected error occurred.",t);
	}

	/**
	 * @param s Description of error
	 */
	public OmUnexpectedException(String s)
	{
		super("An unexpected error occurred: "+s);
	}

	/**
	 * @param s Description of error
	 * @param t Exception that wasn't expected to happen
	 */
	public OmUnexpectedException(String s, Throwable t)
	{
		super("An unexpected error occurred: "+s,t);
	}
}
