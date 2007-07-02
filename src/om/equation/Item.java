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
import java.awt.Font;
import java.awt.image.BufferedImage;

import om.OmUnexpectedException;

import org.w3c.dom.Element;

import util.xml.XML;

abstract class Item
{
	/** Parent item */
	private Item iParent;
	
	/** Child items */
	private Item[] aiChildren;
	
	/** Width and height of item */
	protected int iWidth=0,iHeight=0,iBaseline=-1,iAdvanceWidth=-1,iLeftMargin=0,iRightMargin=0;
	
	/** True if there was whitespace before this item */
	private boolean bWhitespaceBefore;
	
	/** 
	 * Italic slope of end character (e.g. if the character has width 100, height
	 * 10, and italic slope 0.5 then it is considered to extend to 100,0 but only 
	 * to 95,10 (the bottom edge ends (italic slope) * (height) pixels left)
	 */
	protected float fEndSlope=0.0f;
	
	/** @return Child items */
	protected Item[] getChildren() { return aiChildren; }
	
	/** @return Total width in pixels */
	public int getWidth() { return iWidth; }
	
	/** @return Advance width in pixels (place at which next text, if compatible,
	 *   should be drawn) */
	public int getAdvanceWidth() { if(iAdvanceWidth==-1) return getWidth(); else return iAdvanceWidth; }

	/** @return Height in pixels */
	public int getHeight() { return iHeight; }

	/** @return Text baseline (if set, otherwise return same as height) relative to top of image */
	public int getBaseline() { if(iBaseline==-1) return getHeight(); else return iBaseline; }
	
	/** @return Descent below baseline */
	public int getDescent() { return getHeight()-getBaseline(); }
	
	/** @return Italic slope of end (used to move subscripts left) */
	public float getEndSlope() { return fEndSlope; }
	
	/** @return Margin used to left of this when included in line */
	public int getLeftMargin() { return iLeftMargin; }
	/** @return Margin used to right of this when included in line */
	public int getRightMargin() { return iRightMargin; }
	
	/** Zoom factor */
	private float fZoom=1.0f;
	
	/** Single-line stroke at zoom factor */
	private Stroke sStroke;
	
	/**
	 * Converts a pixel distance to zoomed pixels. 
	 * @param iPixels Distance/size
	 * @return Zoomed distance/size
	 */
	public int getZoomed(int iPixels)
	{
		return Math.round(iPixels*fZoom);
	}
	
	/** @return Zoom factor */
	public float getZoom() { return fZoom; }
	
	/** @return Default stroke (1-pixel if not zoomed) */
	public Stroke getStroke()
	{
		if(sStroke==null) sStroke=new BasicStroke(fZoom);
		return sStroke;
	}
	
	
	/**
	 * Override to indicate that the previous item's advance width, not width,
	 * should be used when positioning this item.
	 * @param iBefore Item before this one
	 * @return True if previous item's advance width should be used
	 */
	public boolean useAdvanceAfter(Item iBefore)
	{
		return false;
	}

	/**
	 * Render onto image
	 * @param g2 thing to render onto.
	 * @param iX Position to start rendering at.
	 * @param iY Position to start rendering at.
	 */
	public abstract void render(Graphics2D g2,int iX,int iY);
	
	/** Debug method to fill a background colour */
	protected void showDebug(BufferedImage biTarget,int iX,int iY)
	{		
		Graphics g=biTarget.getGraphics();
		g.setColor(new Color(255,255,200));
		g.fillRect(iX,iY,iWidth,iHeight);
		g.setColor(Color.red);
		g.drawLine(iX,iY+iBaseline,iX+iWidth,iY+iBaseline);
	}

	/** @return Parent Item or null if none */
	public Item getParent()
	{
		return iParent;
	}
	
	/**
	 * @param c A subclass of item.
	 * @return Ancestor or this item of given class or null if none
	 */
	public Item getAncestor(Class<? extends Item> c)
	{
		if(c.isAssignableFrom(getClass())) return this;
		if(iParent!=null) return iParent.getAncestor(c);
		return null;
	}
	
	/** @return Root-level Item */
	public Item getRoot()
	{
		if(iParent==null) 
			return this;
		else
			return iParent.getRoot();
	}
	
	/** 
	 * @return True if there was whitespace before this item in the equation
	 *   and it therefore shouldn't coagulate with anything before
	 */
	public boolean isWhitespaceBefore()
	{
		return bWhitespaceBefore;
	}

	/**
	 * @return Sibling before this item (if there is one)
	 */
	public Item getSiblingBefore()
	{
		if(iParent==null) return null;
		for(int i=0;i<iParent.aiChildren.length;i++)
		{
			if(iParent.aiChildren[i]==this)
			{
				if(i==0) return null;
				return iParent.aiChildren[i-1];
			}
		}
		throw new Error("?");		
	}
	
	/**
	 * Called by ItemFactory to construct this item and all children.
	 * @param f Factory
	 * @param e This XML element
	 * @param parent Parent item or null if none
	 * @param zoom New zoom factor (1.0 = normal)
	 * @throws EquationFormatException If internalInit doesn't like the XML
	 *   or if there is a problem in constructing any child item.
	 */
	void init(ItemFactory f,Element e,Item parent,float zoom) throws EquationFormatException
	{
		this.iParent=parent;
		this.fZoom=zoom;
		
		bWhitespaceBefore=("yes".equals(e.getAttribute("whitespacebefore")));
		
		// Construct rest of tree first
		Element[] aeChildren=XML.getChildren(e);
		aiChildren=new Item[aeChildren.length];
		for(int iChild=0;iChild<aeChildren.length;iChild++)
		{
			aiChildren[iChild]=f.newItem(aeChildren[iChild],this,zoom);
		}		
		
		internalInit(e);
	}
	
	/**
	 * Initialise based on any necessary parameters from the element. Children are
	 * are already set up and inited; parents are set up, but not inited. Default
	 * does nothing.
	 * @param e Element matching this item
	 * @throws EquationFormatException If anything is wrong with the definition
	 */
	protected void internalInit(Element e) throws EquationFormatException
	{
	}
	
	/** Called after initialising everything to prepare for rendering */
	void prepare()
	{
		// Prepare children first
		for(int iChild=0;iChild<aiChildren.length;iChild++)
		{
			aiChildren[iChild].prepare();
		}
		// Prepare ourselves
		internalPrepare();
	}
	
	/** 
	 * Override to do any internal preparation you need before method return 
	 * values will be right. 
	 */
	protected void internalPrepare()
	{		
	}
	
	/** Text size constant: display size (outer level) */
	public final static int TEXTSIZE_DISPLAY=4;
	/** Text size constant: normal size (actually same as display for most cases) */
	public final static int TEXTSIZE_TEXT=3;
	/** Text size constant: subscript size */
	public final static int TEXTSIZE_SUB=2;
	/** Text size constant: sub-subscript size */
	public final static int TEXTSIZE_SUBSUB=1;
	
	/**
	 * @return TEXTSIZE_xx constant (default to same as parent)
	 */ 
	public int getTextSize()
	{
		Item i=this;
		while(i!=null)
		{
			if(i instanceof TextSizeChange)
			{
				TextSizeChange tsc=(TextSizeChange)i;
				return tsc.getNewSize();
			}
			i=i.getSiblingBefore();
		}
		if(getParent()==null)
			return TEXTSIZE_DISPLAY;
		else
			return getParent().getChildReferenceTextSize();
	}
	
	/** @return Textsize used as a reference by children */
	public int getChildReferenceTextSize()
	{
		return getTextSize();
	}
	
	/** Default text sizes */
	protected int[] aiTextSize={14,12,10};
	
	/** Default font */
	protected String sDefaultFont="Times New Roman";
	
	/**
	 * Converts a constant to an actual pixel size. (Note that if equations are
	 * being scaled for accessibility, the graphics context will be scaled when
	 * drawn, so we don't need to use different sizes here.) 
	 * @param iTextSize TEXTSIZE_xxx constant
	 * @return Actual size in pixels
	 */
	public int convertTextSize(int iTextSize)
	{
		if(getParent()!=null)
			return getParent().convertTextSize(iTextSize);
		
		switch(iTextSize)
		{
		case TEXTSIZE_DISPLAY:
		case TEXTSIZE_TEXT:
		 	return aiTextSize[0];
		case TEXTSIZE_SUB:
			return aiTextSize[1];
		case TEXTSIZE_SUBSUB:
			return aiTextSize[2];
		default:
			throw new OmUnexpectedException("Invalid textsize constant: "+iTextSize);
		}
	}
	
	/**
	 * Obtains a smaller text size, but only if we haven't run out of the range.
	 * @param iTextSize Current size
	 * @return One size smaller (or same if smallest)
	 */
	public static int decreaseTextSize(int iTextSize)
	{
		if(iTextSize>TEXTSIZE_SUBSUB) iTextSize--;
		return iTextSize;
	}
	
	/**
	 * @param iTextSize Text size of item
	 * @return A suitable gap for use as spacing of some sort
	 */
	public static int getSuitableGap(int iTextSize)
	{
		switch(iTextSize)
		{
		  case TEXTSIZE_DISPLAY:
		  case TEXTSIZE_TEXT:
		  	return 3;
		  case TEXTSIZE_SUB:
		  	return 2;
		  case TEXTSIZE_SUBSUB:
		  	return 1;
      default: throw new OmUnexpectedException("Incorrect textsize");
		}
	}
	
	/** @return Suitable gap for use as spacing in this item (pre-zoomed) */
	public int getSuitableGap()
	{
		return getZoomed(getSuitableGap(getTextSize()));
	}
	
	/**
	 * @return A font face name (default to same as parent)
	 */
	public String getFontFamily()
	{
		if(getParent()==null)
			return sDefaultFont;
		else
			return getParent().getFontFamily();
	}

	/** 
	 * @param iStyle Font style e.g. Font.PLAIN
	 * @return Font (zoomed as appropriate)
	 */
	public final Font getFont(int iStyle)
	{
		return new Font(getFontFamily(),iStyle,getZoomed(convertTextSize(getTextSize())));
	}
	
	/**
	 * @return Foreground colour (default to same as parent) 
	 */
	public Color getForeground()
	{
		if(getParent()==null)
			return Color.black;
		else
			return getParent().getForeground();
	}
}