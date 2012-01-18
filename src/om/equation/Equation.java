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
import java.awt.image.BufferedImage;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import om.OmUnexpectedException;
import om.equation.generated.EquationFormat;
import om.equation.generated.ParseException;

import org.w3c.dom.Element;

import util.xml.XML;

/**
 * Includes static utility methods to convert equations. Also represents the
 * outer &lt;int_equation&gt; tag in each equation.
 */
public class Equation extends Line
{
	// Standard Item implementation
	///////////////////////////////

	/** Colour used to draw equation */
	private Color cForeground=Color.black;

	public static void register(ItemFactory f)
	{
		f.addItemClass("int_equation",new ItemCreator()
			{	public Item newItem()	 {	return new Equation();	}	});
	}

	@Override
	public Color getForeground()
	{
		return cForeground;
	}

	// Top level-specific stuff
	///////////////////////////

	/** Map of placeholder locations (String -> Point) */
	private Map<String, Point> mPlaceholders=new HashMap<String, Point>();

	/**
	 * @param foreground Foreground colour
	 * @param cBackground Background (used to clear bitmap with)
	 * @param bAntiAlias True if text (& everything else) should be anti-aliased
	 * @return BufferedImage of equation
	 */
	public BufferedImage render(Color foreground,Color cBackground,boolean bAntiAlias)
	{
		this.cForeground=foreground;
		BufferedImage bi=new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics2D g=bi.createGraphics();
		g.setColor(cBackground);
		g.fillRect(0,0,getWidth(),getHeight());
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			bAntiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			bAntiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		render(g,0,0);
		return bi;
	}

	/**
	 * Called by a Placeholder component to inform the equation of its location
	 * @param sID Placeholder ID
	 * @param iX X position in equation co-ordinates (pixels, usually)
	 * @param iY Y position
	 */
	void informPlaceholder(String sID,int iX,int iY)
	{
		mPlaceholders.put(sID,new Point(iX,iY));
	}

	/**
	 * Obtains location of a given placeholder.
	 * @param sID ID of desired placeholder
	 * @return Co-ordinates in equation units (usually pixels)
	 * @throws OmUnexpectedException If placeholder can't be found
	 */
	public Point getPlaceholder(String sID) throws OmUnexpectedException
	{
		Point p=mPlaceholders.get(sID);
		if(p==null) throw new OmUnexpectedException("<equation>: Missing placeholder");
		return p;
	}

	// Static methods for external access
	/////////////////////////////////////

	private static ItemFactory ifDefault=new ItemFactory();
	static
	{
		Bold.register(ifDefault);
		Italic.register(ifDefault);
		Brackets.register(ifDefault);
		ContourIntegral.register(ifDefault);
		Equation.register(ifDefault);
		Fraction.register(ifDefault);
		Hat.register(ifDefault);
		Integral.register(ifDefault);
		Line.register(ifDefault);
		MBox.register(ifDefault);
		Placeholder.register(ifDefault);
		Sum.register(ifDefault);
		SuperSub.register(ifDefault);
		SuperSubHolder.register(ifDefault);
		SquareRoot.register(ifDefault);
		Text.register(ifDefault);
		TextSizeChange.register(ifDefault);
	}

	/**
	 * Creates an equation from text (LaTeX-like) format, using the default
	 * ItemFactory that includes all standard equation items.
	 * @param sEquation Equation text
	 * @param fZoom Zoom factor (1.0 = default)
	 * @return Equation object
	 * @throws EquationFormatException If there's anything wrong with the input
	 */
	public static Equation create(String sEquation,float fZoom) throws EquationFormatException
	{
		return create(sEquation,ifDefault,fZoom);
	}

	/**
	 * Creates an equation from text (LaTeX-like) format.
	 * @param sEquation Equation text
	 * @param f ItemFactory to use
	 * @param fZoom Zoom factor (1.0 = default)
	 * @return Equation object
	 * @throws EquationFormatException If there's anything wrong with the input
	 */
	public static Equation create(String sEquation,ItemFactory f,float fZoom) throws EquationFormatException
	{
		try
		{
			// Ensure equation is a single line (makes it easier to show errors)
			sEquation=sEquation.replaceAll("[\r\n]"," ").trim();
			EquationFormat ef=new EquationFormat(new StringReader(sEquation));
			return create(ef.equation().createDOM(XML.createDocument()),f,fZoom);
		}
		catch(ParseException pe)
		{
			throw new EquationFormatException(pe,sEquation);
		}
	}

	/**
	 * Creates an equation from XML format, using the default
	 * ItemFactory that includes all standard equation items.
	 * @param eEquation Top-level equation element
	 * @param fZoom Zoom factor (1.0 = default)
	 * @return Equation object
	 * @throws EquationFormatException If there's anything wrong with the input
	 */
	public static Equation create(Element eEquation,float fZoom) throws EquationFormatException
	{
		return create(eEquation,ifDefault,fZoom);
	}

	/**
	 * Creates an equation from XML format.
	 * @param eEquation Top-level equation element
	 * @param f ItemFactory to use
	 * @param fZoom Zoom factor (1.0 = default)
	 * @return Equation object
	 * @throws EquationFormatException If there's anything wrong with the input
	 */
	public static Equation create(Element eEquation,ItemFactory f,float fZoom)
		throws EquationFormatException
	{
		Equation e=(Equation)f.newItem(eEquation,null,fZoom);
		e.prepare();
		return e;
	}

	/**
	 * Changes font.
	 * @param sFont Font family for equation (null = leave unchanged)
	 * @param aiSizes 3-element array of text size (large, small, smaller) (null = leave unchanged)
	 */
	public void setFont(String sFont,int[] aiSizes)
	{
		if(sFont!=null) sDefaultFont=sFont;
		if(aiSizes!=null) aiTextSize=aiSizes;
		prepare();
	}
}
