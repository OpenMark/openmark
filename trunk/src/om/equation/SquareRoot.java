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
import java.awt.geom.GeneralPath;

/** Puts a square root around single child Item */
public class SquareRoot extends Item
{
	/** Space between overline and text */
	private int iGap,iLGap;

	/** Horizontal distance that the rightmost sloping line in the symbol covers */
	private int iRightWidth;

	/** Width of symbol part (not including rightmost line) */
	private int iSymbolWidth;

	/**
	 * On a width scale where 1 is the width allowed, and 0,0 is top left, these
	 * are the X,Y coordinates of the three points in the symbol. (I made them
	 * up based on a large glyph from some random font in Photoshop.)
	 */
	private final static float[] SHAPEPOINTS=
	{
		0.0f,-1.11f,
		0.36f,-1.27f,
		1.0f,0.0f
	};

	@Override
	public void render(Graphics2D g2,int iX,int iY)
	{
		//showDebug(biTarget,iX,iY);

		Item iChild=getChildren()[0];

		GeneralPath gp=new GeneralPath();
		for(int i=0;i<SHAPEPOINTS.length/2;i++)
		{
			float
				fX=(SHAPEPOINTS[i*2]*iSymbolWidth)+iX,
				fY=(SHAPEPOINTS[i*2+1]*iSymbolWidth) + (iHeight-1)+iY;

			if(i==0)
				gp.moveTo(fX,fY);
			else
				gp.lineTo(fX,fY);
		}

		gp.lineTo(iSymbolWidth+iRightWidth+iX,iY);
		gp.lineTo(iX+iWidth-1,iY);

		g2.setColor(getForeground());
		g2.setStroke(getStroke());
		g2.draw(gp);

		iChild.render(g2,iX+iSymbolWidth+iRightWidth+iLGap,iY+iGap+getZoomed(1));
	}

	@Override
	protected void internalPrepare()
	{
		// Init basic metrics
		Item iChild=getChildren()[0];
		iGap=getZoomed(getSuitableGap(iChild.getTextSize()));
		iLGap=getZoomed(1);
		iHeight=iChild.getHeight()+getZoomed(1)+iGap;
		iBaseline=iChild.getBaseline()+getZoomed(1)+iGap;

		// Work out the width of the rightmost line area in the shape
		iRightWidth=iHeight/6;

		// Two available sizes depending on size of content
		if(iHeight/8 > iGap)
			iSymbolWidth=iGap*2;
		else
			iSymbolWidth=(3*iGap)/4;

		iWidth=iChild.getWidth()+iRightWidth+iSymbolWidth+iLGap;

		iLeftMargin=getZoomed(2);
		iRightMargin=getZoomed(2);
	}

	/**
	 * @param f ItemFactory to register this class with.
	 */
	public static void register(ItemFactory f)
	{
		f.addItemClass("sqrt",new ItemCreator()
			{	public Item newItem()	 {	return new SquareRoot();	}	});
	}
}
