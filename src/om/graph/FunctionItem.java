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
import java.awt.geom.GeneralPath;

/** Draws f(x) functions in the graph space */
public class FunctionItem extends GraphItem
{
	/**
	 * @param w coordinate system.
	 * @throws GraphFormatException
	 */
	public FunctionItem(World w) throws GraphFormatException
	{
		super(w);
		gr=new GraphRange(getWorld().getLeftX(),getWorld().getRightX());
	}

	/** Range within which to calculate function */
	private GraphRange gr=null;

	/** Actual function */
	private Function f=null;

	/** Line thickness */
	private double dLineWidth=1.0;

	/** Colour of line */
	private Color cLine=null;


	/** Interface callers must provide */
	public static interface Function
	{
		/**
		 * Evaluates function. This will be called once per pixel in the given
		 * range.
		 * @param x X value (world co-ordinates)
		 * @return Y value (world co-ordinates)
		 */
		public double f(double x);
	}

	@Override
	public void init() throws GraphFormatException
	{
		if(cLine==null) cLine=getWorld().convertColour("fg");
	}

	/**
	 * Set the actual function implementation.
	 * @param f Function implementor (may be null to prevent drawing)
	 */
	public void setFunction(Function f)
	{
		this.f=f;
	}

	@Override
	public void paint(Graphics2D g2)
	{
		if(f==null) return;

		int
			iX1=getWorld().convertX(gr.getMin()),
			iX2=getWorld().convertX(gr.getMax());
		int iMinX=Math.min(iX1,iX2),iMaxX=Math.max(iX1,iX2);

		GeneralPath gpPath=new GeneralPath();
		for(int iX=iMinX;iX<=iMaxX;iX++)
		{
			double dX=getWorld().convertXBack(iX);
			float fY=getWorld().convertYFloat(f.f(dX));
			if(iX==iMinX)
				gpPath.moveTo(iX,fY);
			else
				gpPath.lineTo(iX,fY);
		}

		g2.setColor(cLine);
		g2.setStroke(new BasicStroke((float)dLineWidth));
		g2.draw(gpPath);
	}

	/**
	 * Sets function colour.
	 * <p>
	 * Appropriate colours can be obtained from {@link World#convertColour(String)}.
	 * @param c New colour (set null for no outline)
	 */
	public void setColour(Color c)
	{
		cLine=c;
	}

	/**
	 * Sets line width in pixels.
	 * @param d Line width
	 * @throws GraphFormatException
	 */
	public void setLineWidth(double d) throws GraphFormatException
	{
		dLineWidth=d;
	}

	/**
	 * @param d New maximum extent of function
	 */
	public void setMaxX(double d)
	{
		gr=new GraphRange(gr.getMin(),d);
	}

	/**
	 * @param d New minimum extent of function
	 */
	public void setMinX(double d)
	{
		gr=new GraphRange(d,gr.getMax());
	}
}
