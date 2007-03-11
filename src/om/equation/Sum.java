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
import java.awt.Graphics2D;

import util.misc.Fonts;

/** Displays sum using sigma notation */
public class Sum extends LimitsThing implements SuperSubHolder.EatsOwnSuperSub
{
	private final static char SIGMA='\u2211';
	
	protected void renderContents(Graphics2D g2,int iX,int iY,int iOffsetX)
	{
		g2.setColor(Color.black);
		Font f=getBigFont();
		g2.setFont(f);
		g2.drawString(SIGMA+"",iX+iOffsetX+Fonts.getLeftOverlap(f,SIGMA),iY+iBaseline);
	}
	
	public static void register(ItemFactory f)
	{
		f.addItemClass("sum",new ItemCreator()
			{	public Item newItem()	 {	return new Sum();	}	});
	}
	
	protected void internalPrepare()
	{
		// Cannot completely prepare because the super/sub aren't attached yet.
		// But, because there may be none coming, we need to do additional 
		// preparation.
		
		Font f=getBigFont();
		int 
			iAscent=Fonts.getAscent(f,SIGMA),
			iDescent=Fonts.getDescent(f,SIGMA);
		
		iHeight=iAscent+iDescent;
		iWidth=Fonts.getRightExtent(f,SIGMA)+Fonts.getLeftOverlap(f,SIGMA);
		iBaseline=iAscent;
		
		iLeftMargin=iRightMargin=getZoomed(2);
	}

	protected boolean isAlongside()
	{
		return getTextSize()!=TEXTSIZE_DISPLAY;
	}
	
	private Font getBigFont()
	{
		return new Font(getFontFamily(),Font.PLAIN,(getZoomed(convertTextSize(getTextSize()))*3)/2);
	}		
}
