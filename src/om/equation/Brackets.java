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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import org.w3c.dom.Element;

/** Puts stretchy (height-matching) brackets around a child item */
public class Brackets extends Item
{
	private char cLeftSymbol,cRightSymbol;
	
	private int getSymbolSize(char c)
	{
		switch(c)
		{
		case '.' : return getZoomed(0);
		case '|' : return getZoomed(1);
		case '{' :
		case '}' : return getZoomed(3);
		default : return getZoomed(2);
		}
	}
	
	private void drawSymbol(char c,Graphics2D g2,int iX,int iY)
	{
		GeneralPath gp=new GeneralPath();
		int iSize=getSymbolSize(c);
		
		switch(c)
		{
		case '.' : 
			return;
		case '|':
			gp.moveTo(iX,iY);
			gp.lineTo(iX,iY+iHeight-1);
			break;
		case '[':
			gp.moveTo(iX+iSize,iY);
			gp.lineTo(iX,iY);
			gp.lineTo(iX,iY+iHeight-1);
			gp.lineTo(iX+iSize,iY+iHeight-1);
			g2.draw(gp);
			break;
		case ']':
			gp.moveTo(iX,iY);
			gp.lineTo(iX+iSize,iY);
			gp.lineTo(iX+iSize,iY+iHeight-1);
			gp.lineTo(iX,iY+iHeight-1);
			break;
		case '(':
			gp.moveTo(iX+iSize,iY);
			gp.quadTo(iX,iY,iX,iY+iHeight/2);
			gp.quadTo(iX,iY+iHeight-1,iX+iSize,iY+iHeight-1);
			break;
		case ')':
			gp.moveTo(iX,iY);
			gp.quadTo(iX+iSize,iY,iX+iSize,iY+iHeight/2);
			gp.quadTo(iX+iSize,iY+iHeight-1,iX,iY+iHeight-1);
			break;
		case '{':
		{
			float iA=iX,iB=iX+iSize-iSize/2,iC=iX+iSize;
			gp.moveTo(iC,iY);
			gp.quadTo(iB,iY,iB,iY+iHeight/5f);
			gp.lineTo(iB,iY+(2*iHeight)/5f);
			gp.lineTo(iA,iY+iHeight/2f);
			gp.lineTo(iB,iY+(3*iHeight)/5f);
			gp.lineTo(iB,iY+(4*iHeight)/5f);
			gp.quadTo(iB,iY+iHeight-1,iC,iY+iHeight-1);
		}	break;
		case '}':
		{
			// Exact mirror image of other one's points
			float iA=iX+iSize,iB=iX+iSize/2,iC=iX;
			gp.moveTo(iC,iY);
			gp.quadTo(iB,iY,iB,iY+iHeight/5f);
			gp.lineTo(iB,iY+(2*iHeight)/5f);
			gp.lineTo(iA,iY+iHeight/2f);
			gp.lineTo(iB,iY+(3*iHeight)/5f);
			gp.lineTo(iB,iY+(4*iHeight)/5f);
			gp.quadTo(iB,iY+iHeight-1,iC,iY+iHeight-1);
			g2.draw(gp);			
		}	break;
		default:
			throw new Error("wtf?");
		}
		g2.draw(gp);
	}
	
	@Override
	public void render(Graphics2D g2,int iX,int iY)
	{
		if(getChildren().length==0)
		{
			return;
		}
		Item i=getChildren()[0];
		
		Color cForeground=getForeground();
		g2.setColor(cForeground);
		
		int iRight=iX+i.getWidth()+getSuitableGap()-1;
		if(cLeftSymbol!='.') iRight+=getSuitableGap()+getSymbolSize(cLeftSymbol);
		
		drawSymbol(cLeftSymbol,g2,iX,iY);
		drawSymbol(cRightSymbol,g2,iRight,iY);
		
		i.render(g2,iX+getSymbolSize(cLeftSymbol)+getSuitableGap(),iY);			
	}
	
	@Override
	protected void internalPrepare()
	{		
//		if(1==1) throw new Error("Do we even get here?!");
		// Don't really support empty brackets, won't display anything
		if(getChildren().length==0)
		{
			iWidth=0;
			iHeight=0;
			return;
		}
		
		// Get child details
		Item i=getChildren()[0];
		iHeight=i.getHeight();
		iWidth=i.getWidth();
		if(cLeftSymbol!='.') iWidth+=getSuitableGap()+getSymbolSize(cLeftSymbol);
		if(cRightSymbol!='.') iWidth+=getSuitableGap()+getSymbolSize(cRightSymbol);
		iBaseline=i.getBaseline();
		iLeftMargin=iRightMargin=getZoomed(2);
	}
	
	@Override
	protected void internalInit(Element e) throws EquationFormatException
	{
		String SUPPORTEDSYMBOLS=".|[](){}";		
		if(!e.hasAttribute("leftsymbol")) throw new EquationFormatException(this,
			"<int_brackets> requires leftsymbol=");
		String sLeftSymbol=e.getAttribute("leftsymbol");
		if(sLeftSymbol.length()!=1) throw new EquationFormatException(this,
			"<int_brackets> requires leftsymbol=<character>");
		cLeftSymbol=sLeftSymbol.charAt(0);
		int iLeftSymbol=SUPPORTEDSYMBOLS.indexOf(cLeftSymbol);
		if(iLeftSymbol==-1) throw new EquationFormatException(this,
			"<int_brackets> symbol not supported: "+sLeftSymbol);		
		if(!e.hasAttribute("rightsymbol")) throw new EquationFormatException(this,
			"<int_brackets> requires rightsymbol=");
		String sRightSymbol=e.getAttribute("rightsymbol");
		if(sRightSymbol.length()!=1) throw new EquationFormatException(this,
			"<int_brackets> requires rightsymbol=<character>");
		cRightSymbol=sRightSymbol.charAt(0);
		int iRightSymbol=SUPPORTEDSYMBOLS.indexOf(cRightSymbol);
		if(iRightSymbol==-1) throw new EquationFormatException(this,
			"<int_brackets> symbol not supported: "+sRightSymbol);		
	}	
	
	/**
	 * @param f ItemFactory to register this class with.
	 */
	public static void register(ItemFactory f)
	{
		f.addItemClass("int_brackets",new ItemCreator()
			{	public Item newItem()	 {	return new Brackets();	}	});
	}
}
