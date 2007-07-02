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
import java.awt.geom.AffineTransform;

/** Draws text in the graph space. */
public class TextItem extends GraphItem
{
	/**
	 * @param w coordinate system.
	 * @throws GraphFormatException
	 */
	public TextItem(World w) throws GraphFormatException
	{
		super(w);
	}
	
	/** Actual text */
	private String sText;

	/** Location of text */
	private GraphPoint gpOrigin=GraphPoint.ZERO;
	
	/** Angle */
	private double dAngle=0.0;
	
	/** Font */
	private Font fText=null;
	
	/** Colour */
	private Color cText=null;	
	
	/** Alignment */
	private String sAlign=ALIGN_CENTRE;
	
	/** Alignment constant */
	public final static String ALIGN_LEFT="left";
	/** Alignment constant */
	public final static String ALIGN_RIGHT="right";
	/** Alignment constant */
	public final static String ALIGN_CENTRE="centre";
	
	@Override
	public void init() throws GraphFormatException
	{
		if(cText==null) cText=getWorld().convertColour("fg");
		if(fText==null) fText=getWorld().getDefaultFont(false);
	}

	@Override
	public void paint(Graphics2D g2)
	{
		g2.setFont(fText);
		g2.setColor(cText);
		
		// Origin point
		Point p=gpOrigin.convert(getWorld());
		
		AffineTransform at=null;
		if(dAngle!=0.0)
		{
			at=g2.getTransform();
			g2.rotate(dAngle / 180.0 * Math.PI,p.x,p.y);
		}
		
		if(sAlign.equals(ALIGN_LEFT))
			g2.drawString(sText,p.x,p.y);
		else
		{
			int iSize=(int)g2.getFontMetrics().getStringBounds(sText,g2).getWidth();
			if(sAlign.equals(ALIGN_CENTRE))
				g2.drawString(sText,p.x-(iSize+1)/2,p.y);
			else if(sAlign.equals(ALIGN_RIGHT))
				g2.drawString(sText,p.x-iSize,p.y);
		}
		
		if(at!=null)
		{
			g2.setTransform(at);
		}
	}
	
	/**
	 * Sets origin point, X co-ordinate.
	 * @param gsX Co-ordinate
	 */
	public void setX(GraphScalar gsX)
	{
		gpOrigin=gpOrigin.newX(gsX);
	}
	/**
	 * Sets origin point, Y co-ordinate. 
	 * @param gsY Co-ordinate
	 */
	public void setY(GraphScalar gsY)
	{
		gpOrigin=gpOrigin.newY(gsY);
	}

	/**
	 * Sets colour (defaults to 'fg' standard colour).
	 * <p>
	 * Appropriate colours can be obtained from {@link World#convertColour(String)}.
	 * @param c New colour
	 */
	public void setColour(Color c)
	{
		cText=c;		
	}
	
	/**
	 * Sets angle of text.
	 * @param dDegrees Degrees clockwise from normal horizontal text
	 */
	public void setAngle(double dDegrees)
	{
		dAngle=dDegrees;
	}
	
	/**
	 * @param f Font to use
	 */
	public void setFont(Font f)
	{
		fText=f;
	}

	/**
	 * Sets text alignment.
	 * @param s In code, use an ALIGN_x constant. In XML, valid options are 
	 *   "left", "right", and "centre"
	 * @throws GraphFormatException 
	 */
	public void setAlign(String s) throws GraphFormatException
	{
		if(s.equals(ALIGN_CENTRE) || s.equals(ALIGN_LEFT) || s.equals(ALIGN_RIGHT))
			sAlign=s;
		else
			throw new GraphFormatException("<text>: invalid align= value: "+s);
	}
	
	/**
	 * Sets the actual text.
	 * @param s New text string
	 */
	public void setText(String s)
	{
		sText=s;
	}	
}
