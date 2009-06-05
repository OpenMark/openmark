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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.*;

/** Draws simple ellipses in the graph space. */
public class EllipseItem extends GraphItem
{
	/**
	 * @param w coordinate system.
	 * @throws GraphFormatException
	 */
	public EllipseItem(World w) throws GraphFormatException
	{
		super(w);
	}

	/** Centre */
	private GraphPoint gpCentre=GraphPoint.ZERO;

	/** Width and height */
	private GraphPoint gpSize=null;

	/** Colours */
	private Color cFill=null,cLine=null;

	/** Outline thickness */
	private int iLineWidth=-1;

	@Override
	public void init() throws GraphFormatException
	{
		if(gpSize==null) gpSize=GraphPoint.ONE;
	}

	@Override
	public void paint(Graphics2D g2)
	{
		// Work out the corner points and convert to pixels
		Point2D.Float
			p1=gpCentre.offset(new GraphPoint(
				-gpSize.getX().getWorldPosition()/2,
				-gpSize.getY().getWorldPosition()/2)).convertFloat(getWorld()),
			p2=gpCentre.offset(new GraphPoint(
				gpSize.getX().getWorldPosition()/2,
				gpSize.getY().getWorldPosition()/2)).convertFloat(getWorld());
		Ellipse2D.Float eWhole=new Ellipse2D.Float(
			Math.min(p1.x,p2.x),Math.min(p1.y,p2.y),
			Math.abs(p1.x-p2.x),Math.abs(p1.y-p2.y));

		if(iLineWidth==0)
		{
			if(cFill!=null)
			{
				g2.setColor(cFill);
				g2.fill(eWhole);
			}
		}
		else
		{
			Ellipse2D ePartial=new Ellipse2D.Float(
				eWhole.x+iLineWidth,eWhole.y+iLineWidth,
				eWhole.width-(iLineWidth*2),eWhole.height-(iLineWidth*2)
				);
			Area aOuter=new Area(eWhole);
			aOuter.subtract(new Area(ePartial));

			g2.setColor(cLine);
			g2.fill(aOuter);

			if(cFill!=null)
			{
				g2.setColor(cFill);
				g2.fill(ePartial);
			}
		}
	}

	/**
	 * Sets centre of ellipse, X co-ordinate.
	 * @param gsX Co-ordinate
	 */
	public void setX(GraphScalar gsX)
	{
		gpCentre=gpCentre.newX(gsX);
	}
	/**
	 * Sets centre of ellipse, Y co-ordinate.
	 * @param gsY Co-ordinate
	 */
	public void setY(GraphScalar gsY)
	{
		gpCentre=gpCentre.newY(gsY);
	}

	/**
	 * Sets width (horizontal diameter) of ellipse. Height defaults to the
	 * same value.
	 * @param gsW Width
	 * @throws GraphFormatException
	 */
	public void setWidth(GraphScalar gsW) throws GraphFormatException
	{
		if(gpSize==null)
			gpSize=new GraphPoint(gsW,gsW);
		else
			gpSize=gpSize.newX(gsW);
	}
	/**
	 * Sets height (vertical diameter) of ellipse. Width defaults to the
	 * same value.
	 * @param gsH Height
	 * @throws GraphFormatException
	 */
	public void setHeight(GraphScalar gsH) throws GraphFormatException
	{
		if(gpSize==null)
			gpSize=new GraphPoint(gsH,gsH);
		else
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
