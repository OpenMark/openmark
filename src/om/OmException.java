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

/** Standard exception superclass */
public class OmException extends Exception
{
	/**
	 * @param sText Exception message (do not end in full stop)
	 */
	public OmException(String sText)
	{
		super(sText);
	}

	/**
	 * @param t Exception that caused this one
	 */
	public OmException(Throwable t)
	{
		super(t);
	}

	/**
	 * @param sText Exception message (do not end in full stop)
	 * @param t Exception that caused this one
	 */
	public OmException(String sText,Throwable t)
	{
		super(sText,t);
	}
}
