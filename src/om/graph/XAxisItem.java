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

import java.awt.Graphics2D;
import java.awt.geom.*;

import util.misc.Fonts;

/** 
 * An X axis. See {@link AxisItemBase} for most of the properties and a 
 * more detailed explanation. 
 */
public class XAxisItem extends AxisItemBase
{
	/**
	 * @param w coordinate system.
	 * @throws GraphFormatException
	 */
	public XAxisItem(World w) throws GraphFormatException
	{
		super(w);
		// Init range to full size
		gr=new GraphRange(getWorld().getLeftX(),getWorld().getRightX());
	}

	/** Range (x) of axis */
	private GraphRange gr;
	
	/** Y co-ordinate of axis */
	private double dY=0.0;

	@Override
	protected GraphRange getRange()
	{
		return gr;
	}
	
	@Override
	protected void paintLine(Graphics2D g2,GraphRange range)
	{
		float fY=getWorld().convertYFloat(dY);
		Line2D l=new Line2D.Float(
			getWorld().convertXFloat(range.getMin()),fY,
			getWorld().convertXFloat(range.getMax()),fY);
		g2.draw(l);
	}
	
	@Override
	protected boolean isLabelRotated()
	{
		return false;
	}
	
	@Override
	protected int paintAxisText(Graphics2D g2,double dPoint,String sText,
		boolean bRotate,boolean bFlip,boolean bNumbers,int iExtraOffset)
	{
		int 
	  iAscent=bNumbers 
			?	Fonts.getAscent(g2.getFont(),'0') 
			: Math.max(Fonts.getAscent(g2.getFont(),'0'),
				Fonts.getAscent(g2.getFont(),'A')),
	  iDescent=bNumbers ? 0 : g2.getFontMetrics().getDescent(); 		
		int iSize=(int)g2.getFontMetrics().getStringBounds(sText,g2).getWidth();
		
		int iCentreX=getWorld().convertX(dPoint),iCentreY=getWorld().convertY(dY);
		
		if(bRotate)
		{
			AffineTransform at=g2.getTransform();
			
			if(bFlip)
			{
				g2.rotate(-Math.PI/2.0,iCentreX,iCentreY);			
				g2.drawString(sText,
					iCentreX-iSize-iExtraOffset,
					iCentreY+(iAscent+iDescent+1)/2-iDescent);				
			}
			else
			{
				g2.rotate(Math.PI/2.0,iCentreX,iCentreY);			
				g2.drawString(sText,
					iCentreX+iExtraOffset,
					iCentreY+(iAscent+iDescent+1)/2-iDescent);
			}
			
			g2.setTransform(at);
			
			return iSize+iExtraOffset;
		}
		else
		{
			g2.drawString(sText,iCentreX-(iSize+1)/2,iCentreY+iAscent+iExtraOffset);
			
			return iAscent+iExtraOffset;
		}
	}
	
	
	@Override
	protected void paintTick(Graphics2D g2,double dPoint,int iSize,boolean bPos)
	{
		float
			fX=getWorld().convertXFloat(dPoint),
		  fY=getWorld().convertYFloat(dY);
		
		Line2D l;
		if(bPos)
			l=new Line2D.Float(fX,fY,fX,fY-iSize);
		else
			l=new Line2D.Float(fX,fY,fX,fY+iSize);
		g2.draw(l);
	}
	
	/**
	 * @param d New maximum extent of axis
	 */
	public void setMaxX(double d)
	{
		gr=new GraphRange(gr.getMin(),d);
	}
	
	/**
	 * @param d New minimum extent of axis
	 */
	public void setMinX(double d)
	{
		gr=new GraphRange(d,gr.getMax());
	}
	
	/**
	 * @param d Y co-ordinate of axis
	 */
	public void setY(double d)
	{
		dY=d;
	}

}
