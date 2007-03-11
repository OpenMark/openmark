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

/** Just holds an item, reducing its text size */
public class SuperSub extends Item
{
	/** Indicates that an item should be displayed higher */
	final static int TYPE_SUPERSCRIPT=1;
	/** Indicates that an item should be displayed lower */
	final static int TYPE_SUBSCRIPT=2;
	
	/** Type of this item */
	private int iType;
	
	/** @return one of the TYPE_xxx constants */
	public int getType() { return iType; }

	/**
	 * Constructs item with given type
	 * @param iType TYPE_xxx constant
	 */
	private SuperSub(int iType)
	{
		this.iType=iType;
	}
	
	public void render(Graphics2D g2,int iX,int iY)
	{
		Item iChild=getChildren()[0];
		iChild.render(g2,iX,iY);
	}
	
	protected void internalPrepare()
	{
		Item iChild=getChildren()[0];
		iWidth=iChild.getWidth();
		iHeight=iChild.getHeight();
		iBaseline=iChild.getBaseline();
	}
	
	public int getTextSize()
	{
		switch(super.getTextSize())
		{
		case TEXTSIZE_DISPLAY: 
			return TEXTSIZE_SUB;
		default: 
			return decreaseTextSize(super.getTextSize());
		}
	}
	
	// Overriding this allows children size to not take the 'two-step' jump
	// that getTextSize implies, i.e. if you have fractions inside superscript
	public int getChildReferenceTextSize()
	{
		switch(super.getTextSize())
		{
		case TEXTSIZE_DISPLAY: 
			return TEXTSIZE_TEXT;
		default: 
			return getTextSize();
		}
	}
	
	public static void register(ItemFactory f)
	{
		f.addItemClass("int_sup",new ItemCreator()
			{	public Item newItem()	 {	return new SuperSub(TYPE_SUPERSCRIPT);	}	});
		f.addItemClass("int_sub",new ItemCreator()
			{	public Item newItem()	 {	return new SuperSub(TYPE_SUBSCRIPT);	}	});
	}
}
