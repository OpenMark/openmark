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

/** 
 * Doesn't do anything in itself, but ensures that text boxes format like
 * normal text instead of like an equation e.g. including whitespace.
 * (There is also special processing to not throw away whitespace in text
 * nodes within an mbox, in SimpleNode.)
 */
public class MBox extends Item
{
	@Override
	public void render(Graphics2D g2,int iX,int iY)
	{
		Item iChild=getChildren()[0];
		iChild.render(g2,iX,iY);
	}
	
	@Override
	protected void internalPrepare()
	{
		Item iChild=getChildren()[0];
		iWidth=iChild.getWidth();
		iHeight=iChild.getHeight();
		iBaseline=iChild.getBaseline();
	}
	
	/**
	 * @param f ItemFactory to register this class with.
	 */
	public static void register(ItemFactory f)
	{
		f.addItemClass("mbox",new ItemCreator()
			{	public Item newItem()	 {	return new MBox();	}	});
	}
}
