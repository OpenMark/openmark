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

import java.awt.*;

/** Arranges two child Items one above the other with a line between, centred */
public class Fraction extends Item
{
	public void render(Graphics2D g2,int iX,int iY)
	{
		Color cForeground=getForeground();
		int iGap=getSuitableGap();
		int iYPos=0;
		for(int iChild=0;iChild<getChildren().length;iChild++)
		{
			if(iChild>0)
			{
				iYPos+=iGap;
				g2.setColor(cForeground);
				g2.fillRect(iX,iY+iYPos,iWidth,getZoomed(1)); 
				iYPos+=getZoomed(1);		
				iYPos+=iGap;
			}
			
			Item i=getChildren()[iChild];
			
			int iXPos=(iWidth-i.getWidth())/2;
			i.render(g2,iX+iXPos,iY+iYPos);
			
			iYPos+=i.getHeight();		
		}
	}

	protected void internalPrepare()
	{		
		// Init metrics	
		int iGap=getSuitableGap();
		iWidth=0;
		iHeight=0;
		for(int iChild=0;iChild<getChildren().length;iChild++)
		{
			Item i=getChildren()[iChild];

			if(iChild>0)
			{
				iHeight+=iGap*2+getZoomed(1);
			}
			iHeight+=i.getHeight();
			
			iWidth=Math.max(iWidth,i.getWidth());
		}
		
		// Set baseline so that the bar aligns with minus/equals/etc signs,
		// as well as we can make it
		if(getChildren().length==2)
		{
			// First height + gap
			iBaseline=getChildren()[0].getHeight()+iGap;
			
			// Now move it down a bit to allow for the height of - in different fonts
			switch(getTextSize())
			{
		  case TEXTSIZE_DISPLAY:
		  case TEXTSIZE_TEXT:
		  	iBaseline+=getZoomed(5); break;
		  case TEXTSIZE_SUB:
		  	iBaseline+=getZoomed(5); break;
		  case TEXTSIZE_SUBSUB:
		  	iBaseline+=getZoomed(4); break;
			}
		}
		
		iLeftMargin=iRightMargin=getZoomed(2);
	}
	
	public int getTextSize()
	{
		return decreaseTextSize(super.getTextSize());
	}

	public static void register(ItemFactory f)
	{
		f.addItemClass("frac",new ItemCreator()
			{	public Item newItem()	 {	return new Fraction();	}	});
	}
}
