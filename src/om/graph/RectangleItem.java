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
package om.graph;

import java.awt.*;

/** Draws simple rectangles in the graph space. */
public class RectangleItem extends GraphItem
{
	/**
	 * @param w coordinate system.
	 * @throws GraphFormatException
	 */
	public RectangleItem(World w) throws GraphFormatException
	{
		super(w);
	}
	
	/** Corners */
	private GraphPoint gp1=GraphPoint.ZERO,gp2=null;
	
	/** Width and height */
	private GraphPoint gpSize=null;
	
	/** Colours */
	private Color cFill=null,cLine=null;
	
	/** Outline thickness */
	private int iLineWidth=-1;
	
	@Override
	public void init() throws GraphFormatException
	{
		if(gpSize==null && gp2==null)
			throw new GraphFormatException(
				"<rectangle>: Must specify either x2/y2 or width/height");
	}

	@Override
	public void paint(Graphics2D g2)
	{
		// Work out the two points and convert to pixels
		GraphPoint gpOther=(gp2!=null ? gp2 : gp1.offset(gpSize));
		Point p1=gp1.convert(getWorld()),p2=gpOther.convert(getWorld());
		int 
			iX=Math.min(p1.x,p2.x),
			iY=Math.min(p1.y,p2.y),
			iWidth=Math.max(p1.x,p2.x)-iX,
			iHeight=Math.max(p1.y,p2.y)-iY;
				
		// Draw the fill
		if(cFill!=null)
		{
			g2.setColor(cFill);
			g2.fillRect(iX,iY,iWidth,iHeight);
		}
		// Draw outline
		if(iLineWidth>0)
		{
			g2.setColor(cLine);
			g2.fillRect(iX,iY,iWidth,iLineWidth);
			g2.fillRect(iX,iY+iHeight-iLineWidth,iWidth,iLineWidth);
			g2.fillRect(iX,iY+iLineWidth,iLineWidth,iHeight-2*iLineWidth);
			g2.fillRect(iX+iWidth-iLineWidth,iY+iLineWidth,iLineWidth,iHeight-2*iLineWidth);			
		}
	}
	
	/**
	 * Sets first corner of rectangle, X co-ordinate.
	 * @param gsX Co-ordinate
	 */
	public void setX(GraphScalar gsX)
	{
		gp1=gp1.newX(gsX);
	}
	/**
	 * Sets first corner of rectangle, Y co-ordinate. 
	 * @param gsY Co-ordinate
	 */
	public void setY(GraphScalar gsY)
	{
		gp1=gp1.newY(gsY);
	}
	/**
	 * Sets second corner of rectangle, X co-ordinate. (May not use in conjunction
	 * with width or height.) 
	 * @param gsX Co-ordinate
	 * @throws GraphFormatException If width/height is set
	 */
	public void setX2(GraphScalar gsX) throws GraphFormatException
	{
		if(gpSize!=null) throw new GraphFormatException(
			"<rectangle>: Can't specify both x2/y2 and width/height");
		if(gp2==null) gp2=GraphPoint.ZERO;
		gp2=gp2.newX(gsX);
	}
	/**
	 * Sets second corner of rectangle, Y co-ordinate. (May not use in conjunction
	 * with width or height.) 
	 * @param gsY Co-ordinate
	 * @throws GraphFormatException If width/height is set
	 */
	public void setY2(GraphScalar gsY) throws GraphFormatException
	{
		if(gpSize!=null) throw new GraphFormatException(
			"<rectangle>: Can't specify both x2/y2 and width/height");
		if(gp2==null) gp2=GraphPoint.ZERO;
		gp2=gp2.newY(gsY);
	}
	/**
	 * Sets width of rectangle. (May not use in conjunction with X2/Y2.)
	 * @param gsW Width
	 * @throws GraphFormatException If x2/y2 is set
	 */
	public void setWidth(GraphScalar gsW) throws GraphFormatException
	{
		if(gp2!=null) throw new GraphFormatException(
			"<rectangle>: Can't specify both x2/y2 and width/height");
		if(gpSize==null) gpSize=GraphPoint.ZERO;
		gpSize=gpSize.newX(gsW);
	}
	/**
	 * Sets height of rectangle. (May not use in conjunction with X2/Y2.)
	 * @param gsH Height
	 * @throws GraphFormatException If x2/y2 is set
	 */
	public void setHeight(GraphScalar gsH) throws GraphFormatException
	{
		if(gp2!=null) throw new GraphFormatException(
			"<rectangle>: Can't specify both x2/y2 and width/height");
		if(gpSize==null) gpSize=GraphPoint.ZERO;
		gpSize=gpSize.newY(gsH);
	}

	/**
	 * Sets fill colour.
	 * <p>
	 * Appropriate colours can be obtained from {@link World#convertColour(String)}.
	 * @param c Fill colour
	 */
	public void setFillColour(Color c)
	{
		cFill=c;
	}
	/**
	 * Sets outline colour. Calling this (except with null) also turns on the
	 * line in the first place.
	 * <p>
	 * Appropriate colours can be obtained from {@link World#convertColour(String)}.
	 * @param c New colour (set null for no outline)
	 */
	public void setLineColour(Color c)
	{
		cLine=c;
		if(c!=null && iLineWidth==-1) iLineWidth=1;
	}
	/**
	 * Sets line width in pixels. Also turns on outline if it wasn't already,
	 * setting its colour to the colour constant 'fg'.
	 * @param i Line width
	 * @throws GraphFormatException If 'text' isn't defined
	 */
	public void setLineWidth(int i) throws GraphFormatException
	{
		iLineWidth=i;
		if(i>0 && cLine==null)
			cLine=getWorld().convertColour("fg");
	}
}
