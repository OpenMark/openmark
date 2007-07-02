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
import java.awt.geom.*;

/** Draws straight lines in the graph space. */
public class LineItem extends GraphItem
{
	/**
	 * @param w coordinate system.
	 * @throws GraphFormatException
	 */
	public LineItem(World w) throws GraphFormatException
	{
		super(w);
	}
	
	/** Corners */
	private GraphPoint gp1=GraphPoint.ZERO,gp2=GraphPoint.ONE;
	
	/** Colour */
	private Color cLine=null;
	
	/** Outline thickness */
	private int iLineWidth=1;
	
	@Override
	public void init() throws GraphFormatException
	{
		if(cLine==null) cLine=getWorld().convertColour("fg");
	}		
	
	@Override
	public void paint(Graphics2D g2)
	{
		// Work out the two points and convert to pixels
		Point2D.Float p1=gp1.convertFloat(getWorld()),p2=gp2.convertFloat(getWorld());
		
		// Set line thickness and draw
		g2.setStroke(new BasicStroke(iLineWidth));
		g2.setColor(cLine);
		g2.draw(new Line2D.Float(p1.x,p1.y,p2.x,p2.y));				
	}
	
	/**
	 * Sets first point of line, X co-ordinate.
	 * @param gsX Co-ordinate
	 */
	public void setX(GraphScalar gsX)
	{
		gp1=gp1.newX(gsX);
	}
	/**
	 * Sets first point of line, Y co-ordinate. 
	 * @param gsY Co-ordinate
	 */
	public void setY(GraphScalar gsY)
	{
		gp1=gp1.newY(gsY);
	}
	/**
	 * Sets second point of line, X co-ordinate. 
	 * @param gsX Co-ordinate
	 */
	public void setX2(GraphScalar gsX) 
	{
		gp2=gp2.newX(gsX);
	}
	/**
	 * Sets second point of line, Y co-ordinate. 
	 * @param gsY Co-ordinate
	 */
	public void setY2(GraphScalar gsY) 
	{
		gp2=gp2.newY(gsY);
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
	 * setting its colour to the colour constant 'text'.
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
