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
import java.awt.font.LineMetrics;

import om.OmUnexpectedException;

/** Arranges (up to) two child items above/below a parent */
public class SuperSubHolder extends Item
{
	Item getMain2()
	{
		return isWhitespaceBefore() ? null : getSiblingBefore();
	}
	
	/** 
	 * Interface items (appearing in 'main' slot) should implement if they eat 
	 * their own super and sub items and don't want a SuperSubHolder to render
	 * them.
	 */
	public interface EatsOwnSuperSub
	{
		public boolean wantsSuperSub();
		public void attachSuperSub(Item iSuper,Item iSub);
	}
	
	private Item getSuper()
	{
		for(int iChild=0;iChild<getChildren().length;iChild++)
		{
			SuperSub ss=(SuperSub)getChildren()[iChild];
			if(ss.getType()==SuperSub.TYPE_SUPERSCRIPT) return ss;
		}
		return null;
	}
	private boolean hasSuper() { return getSuper()!=null; }
	private Item getSub()
	{
		for(int iChild=0;iChild<getChildren().length;iChild++)
		{
			SuperSub ss=(SuperSub)getChildren()[iChild];
			if(ss.getType()==SuperSub.TYPE_SUBSCRIPT) return ss;
		}
		return null;
	}
	private boolean hasSub() { return getSub()!=null; }

	/** @return Appropriate offset to use based on text size */
	private int getSuperOffset()
	{
		switch(getTextSize())
		{
		  case TEXTSIZE_DISPLAY:
		  case TEXTSIZE_TEXT:
		  	return getZoomed(-4);
		  case TEXTSIZE_SUB:
		  	return getZoomed(-3);
		  case TEXTSIZE_SUBSUB:
		  	return getZoomed(-3);
	    default: throw new OmUnexpectedException("Incorrect textsize");
		}
	}
	
	/** @return Appropriate offset to use based on text size */
	private int getSubOffset()
	{
		switch(getTextSize())
		{
		  case TEXTSIZE_DISPLAY:
		  case TEXTSIZE_TEXT:
		  	return getZoomed(5);
		  case TEXTSIZE_SUB:
		  	return getZoomed(4);
		  case TEXTSIZE_SUBSUB:
		  	return getZoomed(3);
	    default: throw new OmUnexpectedException("Incorrect textsize");
		}
	}
	
	public void render(Graphics2D g2,int iX,int iY)
	{
		Item iMain=getMain2();
		if(iMain!=null && iMain instanceof EatsOwnSuperSub)
			return;
		
		if(hasSuper()) getSuper().render(g2,iX+iSupOffset,iY+iBaseline+iSupBaseline-getSuper().getBaseline());
		if(hasSub()) getSub().render(g2,iX+iSubOffset,iY+iBaseline+iSubBaseline-getSub().getBaseline());
	}
	
	int iSubBaseline,iSupBaseline,iSubOffset,iSupOffset;
	
	public boolean useAdvanceAfter(Item iBefore)
	{
		return true;
	}
	
	protected void internalPrepare()
	{
		// Get details of main item (or default)
		Item iMain=getMain2();
		if(iMain!=null && iMain instanceof EatsOwnSuperSub)
		{
			EatsOwnSuperSub e=(EatsOwnSuperSub)iMain;
			e.attachSuperSub(getSuper(),getSub());
			return;			
		}
		
		int iMainAscent;
		float fMainSlope;
		if(iMain==null) 
		{
			Font fPlain=getFont(Font.PLAIN);
			LineMetrics lm=fPlain.getLineMetrics("Ay",Text.frc);
			iMainAscent=(int)(lm.getAscent()+0.5f);
			fMainSlope=0f;
		}
		else
		{
			iMainAscent=iMain.getBaseline();
			fMainSlope=iMain.getEndSlope();
		}
		
		// Get details of sup/sub
		int iSupDescent=0,iSupAscent=0,iSupWidth=0;
		if(hasSuper())
		{
			iSupDescent=getSuper().getDescent();
			iSupAscent=getSuper().getBaseline();
			iSupWidth=getSuper().getWidth();
		}
		int iSubAscent=0,iSubDescent=0,iSubWidth=0;
		if(hasSub())
		{
			iSubDescent=getSub().getDescent();
			iSubAscent=getSub().getBaseline();
			iSubWidth=getSub().getWidth();
		}
		
		// Use to define preferred baselines relative to main baseline
		iSubBaseline=getSubOffset();
		iSupBaseline=getSuperOffset();
		
		// Shift baselines if super gets near the main baseline...
		iSupBaseline=Math.min(iSupBaseline,-(iMainAscent/3)-iSupDescent);
		// ... or if sub gets near the top
		iSubBaseline=Math.max(iSubBaseline,iSubAscent-((iMainAscent*2)/3) );
			
		// Shift baselines if children will run into each other
		if(hasSub() && hasSuper())
		{
			int iGap = (iSubBaseline-iSubAscent) - (iSupBaseline+iSupDescent); 
			int iRequiredGap=getSuitableGap(getSuper().getTextSize());
			if( iGap< iRequiredGap)
			{
				iSupBaseline-=(iRequiredGap-iGap)/2;
				iSubBaseline+=(iRequiredGap-iGap+1)/2; // +1 is to round it correctly
			}
		}
		
		// Apply offset for each baseline
		iSubOffset=Math.round(fMainSlope*(-iSubBaseline))+SPACEBEFORE;
		iSupOffset=Math.round(fMainSlope*(-iSupBaseline))+SPACEBEFORE;
		
		// Calculate height specs
		iBaseline=Math.max(
			hasSuper() ? iSupAscent-iSupBaseline : 0,
			hasSub() ? iSubAscent-iSubBaseline : 0);
		iHeight=Math.max(
			hasSub()? iSubBaseline+iSubDescent+iBaseline : 0,
			hasSuper()? iSupBaseline+iSupDescent+iBaseline : 0);
		
		// Calculate width specs
		iWidth=Math.max(iSubOffset+iSubWidth,iSupOffset+iSupWidth);
		
		// Allow a little space afterward
		iWidth+=SPACEAFTER;
	}
	
	/** Extra space before each scripted item */
	private final static int SPACEBEFORE=1;
	/** Extra space after the thing */
	private final static int SPACEAFTER=1;
	
	
	
	public static void register(ItemFactory f)
	{
		f.addItemClass("int_supersub",new ItemCreator()
			{	public Item newItem()	 {	return new SuperSubHolder();	}	});
	}
}
