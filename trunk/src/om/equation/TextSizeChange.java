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
package om.equation;

import java.awt.Graphics2D;

/** Changes text size in force from this point in the equation onward. */
public class TextSizeChange extends Item
{
	/** TEXTSIZE_xx constant */
	private int iSize;

	/**
	 * @param iSize TEXTSIZE_xx constant
	 */
	private TextSizeChange(int iSize)
	{
		this.iSize=iSize;
	}

	/** @return New text size */
	int getNewSize()
	{
		return iSize;
	}

	/**
	 * @param f ItemFactory to register this class with.
	 */
	public static void register(ItemFactory f)
	{
		f.addItemClass("displaystyle",new ItemCreator()
			{	public Item newItem()	 {	return new TextSizeChange(TEXTSIZE_DISPLAY);	}	});
		f.addItemClass("textstyle",new ItemCreator()
			{	public Item newItem()	 {	return new TextSizeChange(TEXTSIZE_TEXT);	}	});
		f.addItemClass("scriptstyle",new ItemCreator()
			{	public Item newItem()	 {	return new TextSizeChange(TEXTSIZE_SUB);	}	});
		f.addItemClass("scriptscriptstyle",new ItemCreator()
			{	public Item newItem()	 {	return new TextSizeChange(TEXTSIZE_SUBSUB);	}	});
	}

	@Override
	public void render(Graphics2D g2,int iX,int iY)
	{
	}
}
