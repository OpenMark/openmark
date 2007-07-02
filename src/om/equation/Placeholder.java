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

/** Blank area that informs equation of its position within the target image. */
public class Placeholder extends Item
{
	/** Unique ID for placeholder */
	private String sID;
	
	@Override
	public void render(Graphics2D g2,int iX,int iY)
	{
		// Fix placeholder position in parent equation
		((Equation)getRoot()).informPlaceholder(sID,iX,iY);
	}
	
	@Override
	protected void internalPrepare()
	{		
		sID=((Text)(getChildren()[0].getChildren()[0])).getOriginalText();
		String sSize=((Text)(getChildren()[1].getChildren()[0])).getOriginalText();
		String[] asWH=sSize.split(",");
		iWidth=getZoomed(Integer.parseInt(asWH[0]));
		iHeight=getZoomed(Integer.parseInt(asWH[1]));
	}
	
	/**
	 * @param f ItemFactory to register this class with.
	 */
	public static void register(ItemFactory f)
	{
		f.addItemClass("placeholder",new ItemCreator()
			{	public Item newItem()	 {	return new Placeholder();	}	});
	}
}
