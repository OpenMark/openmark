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

/** Draws a grid. */
public class GridItem extends GraphItem
{
	/** Represents major and minor spacing intervals */
	private static class MajorMinor
	{
		double dMajor=0.0,dMinor=0.0;
	}

	/** Grid spacing */
	private MajorMinor mmXSpacing=new MajorMinor(),mmYSpacing=new MajorMinor();
	
	/** Opacity of major and minor gridlines */
	private MajorMinor mmOpacity=new MajorMinor();
		
	/** Range (x,y) of grid lines */
	private GraphRange grX,grY;
	
	/** Default stroke used for axis lines */
	private final static BasicStroke DEFAULTSTROKE=new BasicStroke(1.0f);

	

	/**
	 * @param w coordinate system.
	 * @throws GraphFormatException
	 */
	public GridItem(World w) throws GraphFormatException
	{
		super(w);
		// Init range to full size
		grX=new GraphRange(getWorld().getLeftX(),getWorld().getRightX());
		grY=new GraphRange(getWorld().getTopY(),getWorld().getBottomY());
		mmOpacity.dMajor=0.25;
		mmOpacity.dMinor=mmOpacity.dMajor/2.0;
	}
	
	/** Colour */
	private Color cLine=null;
	
	@Override
	public void init() throws GraphFormatException
	{
		if(cLine==null) cLine=getWorld().convertColour("fg");
	}	
	
	private Color getMajorColour()
	{
		return new Color(cLine.getRed(),cLine.getGreen(),cLine.getBlue(),
			Math.min(255,(int)(256.0*mmOpacity.dMajor)));
	}
	private Color getMinorColour()
	{
		return new Color(cLine.getRed(),cLine.getGreen(),cLine.getBlue(),
			Math.min(255,(int)(256.0*mmOpacity.dMinor)));
	}
	
	@Override
	public void paint(Graphics2D g2)
	{
		g2.setStroke(DEFAULTSTROKE);

		g2.setColor(getMajorColour());		
		if(mmXSpacing.dMajor!=0.0)
		{
			// Draw all lines in range except within omit range
			for(
				double dPoint=(int)(grX.getMin() / mmXSpacing.dMajor) * mmXSpacing.dMajor;
				dPoint<=grX.getMax();
				dPoint+=mmXSpacing.dMajor)
			{
				float fX=getWorld().convertXFloat(dPoint);				
				g2.draw(new Line2D.Float(fX,getWorld().convertYFloat(grY.getMin()),
					fX,getWorld().convertYFloat(grY.getMax())));
			}
		}
		if(mmYSpacing.dMajor!=0.0)
		{
			// Draw all lines in range except within omit range
			for(
				double dPoint=(int)(grY.getMin() / mmYSpacing.dMajor) * mmYSpacing.dMajor;
				dPoint<=grY.getMax();
				dPoint+=mmYSpacing.dMajor)
			{
				float fY=getWorld().convertYFloat(dPoint);
				g2.draw(new Line2D.Float(getWorld().convertXFloat(grX.getMin()),fY,
					getWorld().convertXFloat(grX.getMax()),fY));
			}
		}

		g2.setColor(getMinorColour());		
		if(mmXSpacing.dMinor!=0.0)
		{
			// Draw all lines in range except within omit range
			for(
				double dPoint=(int)(grX.getMin() / mmXSpacing.dMinor) * mmXSpacing.dMinor;
				dPoint<=grX.getMax();
				dPoint+=mmXSpacing.dMinor)
			{
				// Was there a major tick here?
				if(mmXSpacing.dMajor!=0.0 && 
					Math.abs(Math.round(dPoint / mmXSpacing.dMajor) * mmXSpacing.dMajor - dPoint)<mmXSpacing.dMinor/10.0)
					continue;
				
				float fX=getWorld().convertXFloat(dPoint);				
				g2.draw(new Line2D.Float(fX,getWorld().convertYFloat(grY.getMin()),
					fX,getWorld().convertYFloat(grY.getMax())));
			}
		}
		if(mmYSpacing.dMinor!=0.0)
		{
			// Draw all lines in range except within omit range
			for(
				double dPoint=(int)(grY.getMin() / mmYSpacing.dMinor) * mmYSpacing.dMinor;
				dPoint<=grY.getMax();
				dPoint+=mmYSpacing.dMinor)
			{
				// Was there a major tick here?
				if(mmYSpacing.dMajor!=0.0 && 
					Math.abs(Math.round(dPoint / mmYSpacing.dMajor) * mmYSpacing.dMajor - dPoint)<mmYSpacing.dMinor/10.0)
					continue;
				
				float fY=getWorld().convertYFloat(dPoint);
				g2.draw(new Line2D.Float(getWorld().convertXFloat(grX.getMin()),fY,
					getWorld().convertXFloat(grX.getMax()),fY));
			}
		}		
		
	}
	

	/**
	 * Sets colour for grid lines. By default, major gridlines are drawn at 
	 * 50% and minor at 30% transparency of this colour. Colour defaults to 
	 * normal foreground (usually black).
	 * <p>
	 * Appropriate colours can be obtained from {@link World#convertColour(String)}.
	 * @param c New colour
	 */
	public void setColour(Color c)
	{
		cLine=c;
	}
	
	/**
	 * Sets X spacing of gridlines. You can have major and minor grid lines;
	 * minor ones are usually more transparent.
	 * @param sSpacing For example "0.5" if you want major gridlines every 
	 *   0.5, or "0.5,0.1" if you also want minor ones every 0.1.
	 * @throws GraphFormatException If spacing string is invalid
	 */
	public void setXSpacing(String sSpacing) throws GraphFormatException
	{
		mmXSpacing=convertMajorMinor(sSpacing);
	}
	
	/**
	 * Sets Y spacing of gridlines. You can have major and minor grid lines;
	 * minor ones are usually more transparent.
	 * @param sSpacing For example "0.5" if you want major gridlines every 
	 *   0.5, or "0.5,0.1" if you also want minor ones every 0.1.
	 * @throws GraphFormatException If spacing string is invalid
	 */
	public void setYSpacing(String sSpacing) throws GraphFormatException
	{
		mmYSpacing=convertMajorMinor(sSpacing);
	}
	
	/**
	 * Converts spacing from a string in format "" (=0.0,0.0) "0.3" (=0.3,0.0)
	 * or "0.3,0.1" (=0.3,0.1).
	 * @param sSpacing String defining major/minor spacing
	 * @return Object containing information as doubles. If a value was unspecified
	 *   it is set to 0.0
	 * @throws GraphFormatException If string format is invalid
	 */
	private MajorMinor convertMajorMinor(String sSpacing) throws GraphFormatException
	{
		MajorMinor s=new MajorMinor();
		try
		{
			int iComma=sSpacing.indexOf(",");
			if(sSpacing.length()==0)
			{
				s.dMajor=0.0;
				s.dMinor=0.0;
			}
			else if(iComma==-1)
			{
				s.dMajor=Double.parseDouble(sSpacing);
				s.dMinor=0.0;
			}
			else
			{
				s.dMajor=Double.parseDouble(sSpacing.substring(0,iComma));
				s.dMinor=Double.parseDouble(sSpacing.substring(iComma+1));
			}
		}
		catch(NumberFormatException nfe)
		{
			throw new GraphFormatException(
				"<grid>: Invalid spacing specification: "+sSpacing);
		}		
		return s;
	}
	
	/**
	 * Sets opacity of gridlines. The default is 0.25 for major and half that for
	 * minor.
	 * @param sOpacity For example "0.5" if you want major gridlines to be 50%,
	 *   or "0.5,0.1" if you also want minor ones to be 10%. (Minor ones default
	 *   to half the major one.)
	 * @throws GraphFormatException If opacity string is invalid
	 */
	public void setOpacity(String sOpacity) throws GraphFormatException
	{
		mmOpacity=convertMajorMinor(sOpacity);
		if(mmOpacity.dMinor==0.0) mmOpacity.dMinor=0.5*mmOpacity.dMajor;
	}
	
	/**
	 * @param d New maximum extent of grid
	 */
	public void setMaxX(double d)
	{
		grX=new GraphRange(grX.getMin(),d);
	}
	
	/**
	 * @param d New minimum extent of grid
	 */
	public void setMinX(double d)
	{
		grX=new GraphRange(d,grX.getMax());
	}
	
	/**
	 * @param d New maximum extent of grid
	 */
	public void setMaxY(double d)
	{
		grY=new GraphRange(grY.getMin(),d);
	}
	
	/**
	 * @param d New minimum extent of grid
	 */
	public void setMinY(double d)
	{
		grY=new GraphRange(d,grY.getMax());
	}
	
}
