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

/** Draws (x,y)=f(t) functions in the graph space */
public class ParametricFunctionItem extends GraphItem
{
	public ParametricFunctionItem(World w) throws GraphFormatException
	{
		super(w);
		gr=new GraphRange(0,1);		
	}

	/** Range of t within which to calculate function */
	private GraphRange gr=null;
	
	/** How many steps to calculate */
	private int iSteps=100;
	
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
		 * Evaluates function. This will be called once per t value step 
		 * @param t Value of t
		 * @return X and Y as a GraphPoint (world co-ordinates)
		 */
		public GraphPoint f(double t);
	}
	
	public void init() throws GraphFormatException
	{
		if(cLine==null) cLine=getWorld().convertColour("fg");
	}
	
	/**
	 * Set the actual function implementation.
	 * @param f Function implementor
	 */
	public void setFunction(Function f)
	{
		this.f=f;
	}
	
	public void paint(Graphics2D g2)
	{
		if(f==null) return;

		GeneralPath gpPath=new GeneralPath();		
		for(int i=0;i<iSteps;i++)
		{
			double t=((double)i / (double)(iSteps-1)) * (gr.getMax()-gr.getMin()) + gr.getMin();
			GraphPoint gp=f.f(t);
			float 
				fX=getWorld().convertXFloat(gp.getX().getWorldPosition())+gp.getX().getPixelOffset(),
				fY=getWorld().convertYFloat(gp.getY().getWorldPosition())+gp.getY().getPixelOffset();
			
			if(i==0) 
				gpPath.moveTo(fX,fY);
			else 
				gpPath.lineTo(fX,fY);
		}
		
		g2.setColor(cLine);
		g2.setStroke(new BasicStroke((float)dLineWidth));
		g2.draw(gpPath);
	}
	
	/**
	 * Sets line colour.
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
	 */
	public void setLineWidth(double d) throws GraphFormatException
	{
		dLineWidth=d;
	}
	
	/**
	 * @param iSteps Number of points at which t is evaluated
	 */
	public void setSteps(int iSteps)
	{
		this.iSteps=iSteps;
	}
	
	/**
	 * @param d New maximum extent of function
	 */
	public void setMaxT(double d)
	{
		gr=new GraphRange(gr.getMin(),d);
	}
	
	/**
	 * @param d New minimum extent of function
	 */
	public void setMinT(double d)
	{
		gr=new GraphRange(d,gr.getMax());
	}		
}
