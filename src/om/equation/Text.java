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
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.util.*;

import org.w3c.dom.Element;

import util.misc.Fonts;
import util.xml.XML;

/** Simple mathematical text item */
public class Text extends Item
{
	private final static int THIS_IS_A_ZED = 0x2000000; // Must be different from SPECIAL_CHAR_FONT.
	/** Shared font render context */
	public static FontRenderContext frc;
	static
	{
		BufferedImage biTemp=new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
		frc=biTemp.createGraphics().getFontRenderContext();
	}

	/** Runs of text w/ same font */
	private LinkedList<Run> lRuns=new LinkedList<Run>();

	/** Original text input */
	private String sOriginalText;

	/** True if this text block is first in its parent */
	private boolean bFirst;
	
	private Map<Integer, Font> fonts = new HashMap<Integer, Font>(5);

	/**
	 * @return Original text of input (used for handling parameters in software
	 *   rather than as text items)
	 */
	public String getOriginalText()
	{
		return sOriginalText;
	}	

	private static class Run
	{
		String sText;
		int iStyle;
		private Run(String text, int style) {
			sText = text;
			iStyle = style;
		}
	}
	
	

	@Override
	public void render(Graphics2D g2,int iX,int iY)
	{
		g2.setColor(getForeground());

		Run rBefore=null;
		int iChangeGap=0;

		for(Run r : lRuns)
		{
			Font fThis = fonts.get(r.iStyle);

			boolean bChangeStyle=rBefore!=null && rBefore.iStyle!=r.iStyle;

			// Space at right of last run, after change of style
			if(bChangeStyle)
			{
				iX+=iChangeGap+getZoomed(1);
				iChangeGap=0;
			}
			// Space at left of this one, at left end or after change of style
			if(rBefore==null || bChangeStyle)
				iX+=Fonts.getLeftOverlap(fThis,r.sText.charAt(0));

			g2.setFont(fThis);
			g2.drawString(r.sText,iX,iY+iBaseline);

			// Note: This hack is needed because Java (1.4, 1.5) does not render the
			// diagonal stroke on italic Times New Roman z at below 26px.
			if(r.sText.equals("z") && r.iStyle==Font.ITALIC && getFontFamily().equals("Times New Roman"))
			{
				switch(fThis.getSize())
				{
				case 16:
				case 15:
				case 14:
				case 13:
				case 12:
					g2.setStroke(new BasicStroke(0.6f));
					g2.drawLine(iX+4,iY+iBaseline-6,iX-1,iY+iBaseline-1);
					break;
				case 11:
					g2.setStroke(new BasicStroke(0.6f));
					g2.drawLine(iX+3,iY+iBaseline-5,iX-1,iY+iBaseline-1);
					break;
				case 10:
				case 9:
					g2.setStroke(new BasicStroke(0.5f));
					g2.drawLine(iX+3,iY+iBaseline-5,iX-1,iY+iBaseline-1);
					break;
				default:
					if(fThis.getSize()<=26)
					{
					  float fFactor=fThis.getSize() / 13.0f;
						g2.setStroke(new BasicStroke(0.6f*fFactor));
						g2.drawLine(
							Math.round(iX+(4*fFactor)),
							Math.round(iY+iBaseline-(5.4f*fFactor)),
							Math.round(iX-(1*fFactor)),
							Math.round(iY+iBaseline-(0.4f*fFactor)));
					}
					break;
				}
			}

			// Actual claimed size
			iX+=Math.round(getFont(r.iStyle).getStringBounds(r.sText,frc).getWidth());

			if(r.sText.length()>0)
			{
				iChangeGap=Fonts.getRightOverlap(fThis,r.sText.charAt(r.sText.length()-1));
			}

			rBefore=r;
		}

	}

	/**
	 * Character used to indicate spaces. This should ideally be a thinner space
	 * but Java 1.4 won't render them.
	 */
	private final static char SPACE='\u0020';

	private static void fixupSignOperator(StringBuffer sbText,char c,boolean bFirst)
	{
		// Sign operators attach to next thing at start of context or start of
		// whole expression (bFirst), otherwise have
		// spaces either side
		int iPos=0;
		while(true)
		{
			iPos=sbText.indexOf(""+c,iPos);
			if(iPos==-1) return; // Not found

			if((iPos==0 && bFirst) || (iPos>0 && isContextStart(sbText.charAt(iPos-1))))
			{
				// Only add space on left
				sbText.insert(iPos,SPACE);
				iPos++;
			}
			else
			{
				// Add space to left and right
				sbText.insert(iPos,SPACE);
				iPos++;
				sbText.insert(iPos+1,SPACE);
				iPos++;
			}

			iPos++;
		}

	}

	// Fix up all operators at once, using the list of operators in 
	// SimpleNode.STANDARDOPERATORS.
	private static void fixupOperators(StringBuffer sbText)
	{
		// Operators get spaces either side
		int iPos=0;
		while(iPos < sbText.length())
		{
			if (SimpleNode.STANDARDOPERATORS.contains("" + sbText.charAt(iPos))) {
				sbText.insert(iPos, SPACE);
				sbText.insert(iPos+2, SPACE);
				iPos+=2;
			}

			iPos++;
		}
	}

	private static void trimSpaces(StringBuffer sbText,boolean bFirst)
	{
		// We want spaces at start and end for operators so keep those - unless it's
		// first in a run
		if(bFirst)
		{
			while(sbText.length()>0 && Character.isWhitespace(sbText.charAt(0)))
			{
				sbText.deleteCharAt(0);
			}
		}

		// But get rid of any doubles
		for(int iPos=0;iPos<sbText.length();iPos++)
		{
			if(sbText.charAt(iPos)==SPACE)
			{
				while(iPos+1<sbText.length() && sbText.charAt(iPos+1)==SPACE)
				{
					sbText.deleteCharAt(iPos+1);
				}
			}
		}
	}

	/**
	 * Characters after which we're assumed to have entered a new context
	 * and therefore treat signs as signs, not anything else.
	 */
	private final static String CONTEXTSTART="(,+-\u00f7\u00d7=\u2248";

	private static boolean isContextStart(char c)
	{
		return CONTEXTSTART.indexOf(c)!=-1;
	}

	static String fixupText(String sText,boolean bFirst)
	{
		// Get rid of user-typed whitespace
		sText=sText.replaceAll(" ","");

		// Add whitespace around operators
		StringBuffer sb=new StringBuffer(sText);
		fixupSignOperator(sb,'-',bFirst);
		fixupSignOperator(sb,'+',bFirst);
		fixupOperators(sb);
		trimSpaces(sb,bFirst);

		sText=sb.toString();

		// Fix up characters that people can't be arsed to type properly
		sText=sText.replaceAll("-","\u2212"); // Minus
		sText=sText.replaceAll("(\"|'')","\u2033"); // Double prime
		sText=sText.replaceAll("'","\u2032"); // Prime

		return sText;
	}

	@Override
	protected void internalInit(Element e) throws EquationFormatException
	{
		String sText=XML.getText(e);
		sOriginalText=sText;
		bFirst=e.getPreviousSibling()==null;

		if(Fonts.isBrokenMacOS) 	sText=sText.replaceAll("[^\\x00-\\xff]","?");

		// See if there's an mbox ancestor
		if(getAncestor(MBox.class)!=null)
		{
			// Simple text
			lRuns.add(new Run(sText, Font.PLAIN));
		}
		else
		{
			// Convert to equation
			sText=fixupText(sText,bFirst);
			if(Fonts.isBrokenMacOS)	sText=sText.replaceAll("[^\\x00-\\xff]","?");
			// Now convert into runs
			buildRuns(sText);
		}
	}

	private static boolean isItalic(char c)
	{
		if(!Character.isLetter(c)) return false;
		if(Character.UnicodeBlock.of(c) == Character.UnicodeBlock.GREEK)
			return Character.isLowerCase(c);
		else
			return true;
	}
	
	//Returns true if any one of parent items is Bold.
	private boolean isTextBold() {
		Item parent = getParent();
		while (parent != null) {
			if(parent instanceof Bold) {
				return true;
			}
			parent = parent.getParent();
		}		
		return false;
	}

	private static boolean isSpecialChar(char c, Font f)
	{
		return !f.canDisplay(c);
	}

	private int styleForChar(char c, Font f) {
		if (c == 'z') {
			return THIS_IS_A_ZED;
		} else if (isSpecialChar(c, f)) {
			return SPECIAL_CHAR_FONT;
		} else if (isItalic(c)) {
			return Font.ITALIC;
		} else {
			return Font.PLAIN;
		}
	}

	private void buildRuns(String sText)
	{
		if(sText.length() == 0) return;
		Font f = new Font(getFontFamily(), Font.PLAIN, 14);
		
		StringBuffer currentRun = new StringBuffer();
		int currentStyle = -1;
		for (int pos = 0; pos < sText.length(); pos++) {
			char c = sText.charAt(pos);
			int style = styleForChar(c, f);	
			//Change the style to Bold if isTextBold() returns true.
			if (isTextBold() && style != THIS_IS_A_ZED) {
				if (style == SPECIAL_CHAR_FONT) {
					style = SPECIAL_CHAR_BOLD_FONT;
				} else {
					style = Font.BOLD;
				}
			}
			if (style == currentStyle) {
				// Style stays the same, just add to the current run.
				currentRun.append(c);
			} else {
				// Style changed, we'll need to start a new run.

				// But first process any currently open run.
				if (currentStyle != -1) {
					lRuns.add(new Run(currentRun.toString(), currentStyle));
					currentRun.setLength(0);
				}

				// Now process the new character.
				if (style == THIS_IS_A_ZED) {
					// Special case. Each z goes in a run on its own.
					if (isTextBold()) {
						lRuns.add(new Run("z", Font.BOLD));
					} else {
						lRuns.add(new Run("z", Font.ITALIC));
					}
					currentStyle = -1;
				} else {
					// Start a new run of the specified style.
					currentRun.append(c);
					currentStyle = style;
				}
			}
		}
		if (currentStyle != -1) {
			lRuns.add(new Run(currentRun.toString(), currentStyle));
		}
	}

	private void createFonts() {
		fonts.put(Font.PLAIN, getFont(Font.PLAIN));
		fonts.put(Font.BOLD, getFont(Font.BOLD));
		fonts.put(Font.ITALIC, getFont(Font.ITALIC));
		fonts.put(SPECIAL_CHAR_FONT, getSpecialCharacterFont(Font.PLAIN));
		fonts.put(SPECIAL_CHAR_BOLD_FONT, getSpecialCharacterFont(Font.BOLD));
	}

	@Override
	protected void internalPrepare()
	{
		createFonts();

		int iAscent,iDescent;

		iAscent=0; iDescent=0;
		for(Run r : lRuns)
		{
			iAscent=Math.max(Fonts.getMaxAscent(fonts.get(r.iStyle),r.sText),iAscent);
			iDescent=Math.max(Fonts.getMaxDescent(fonts.get(r.iStyle),r.sText),iDescent);
		}

		iBaseline=iAscent;
		iHeight=iBaseline+iDescent;

		iWidth=0;
		Run rBefore=null;
		int iChangeGap=0;

		for(Run r : lRuns)
		{
			Font fThis = fonts.get(r.iStyle);

			boolean bChangeStyle=rBefore!=null && rBefore.iStyle!=r.iStyle;

			// Space at right of last run, after change of style
			if(bChangeStyle)
			{
				iWidth+=iChangeGap+getZoomed(1);
				iChangeGap=0;
			}
			// Space at left of this one, at left end or after change of style
			if(rBefore==null || bChangeStyle)
				iWidth+=Fonts.getLeftOverlap(fThis,r.sText.charAt(0));

			// Actual claimed size
			iWidth+=Math.round(fThis.getStringBounds(r.sText,frc).getWidth());

			// Space to right (only used if we change style)
			if(r.sText.length()>0)
			{
				iChangeGap=Fonts.getRightOverlap(fThis,r.sText.charAt(r.sText.length()-1));
			}

			rBefore=r;
		}
		// Use last right offset at end, but not for advance width
		iAdvanceWidth=iWidth;

		iWidth+=iChangeGap;

		// Calculate final slope
		Run rLast=lRuns.getLast();
		if(rLast!=null && rLast.iStyle==Font.ITALIC)
		{
			fEndSlope=fonts.get(Font.ITALIC).getItalicAngle();
			iAdvanceWidth+=Fonts.getItalicRightOverlap(fonts.get(Font.ITALIC),
				rLast.sText.charAt(rLast.sText.length()-1));
		}
	}

	/**
	 * @param f ItemFactory to register this class with.
	 */
	public static void register(ItemFactory f)
	{
		f.addItemClass("int_text",new ItemCreator()
			{	public Item newItem()	 {	return new Text();	}	});
	}
	
}
