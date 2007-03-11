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

/** Class used for parts of code that aren't implemented */
public class OmTodoException extends OmException
{
	/** The to-do exception needs no parameters */ 
	public OmTodoException()
	{
		super("TODO: attempt to use area of code that hasn't been written");
	}
	
	/** 
	 * @param sText Text of exception (included after a to-do indicator) 
	 */ 
	public OmTodoException(String sText)
	{
		super("TODO: "+sText);
	}
}
