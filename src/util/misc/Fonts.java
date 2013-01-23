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
package util.misc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Obtains information about fonts that is more accurate then the built-in
 * metrics.
 */
public abstract class Fonts
{
	/** Turn on when checking metrics */
	private final static boolean DEBUGMETRICS=false;

	/** Map of Font -> String (character) -> CharacterInfo */
	private static Map<Font,Map<String,CharacterInfo> > mCache=
			new HashMap<Font,Map<String,CharacterInfo> >();

	/**
	 * BufferedImage used for calculating character information (retained to
	 * save creation time)
	 */
	private static BufferedImage biCache=null;

	/** Information stored about a single character */
	private static class CharacterInfo
	{
		int iAscent,iDescent,iLeftOverlap,iRightExtent,iRightItalicOverlap,
		  iReportedWidth;
	}

	/**
	 * Obtain the number of pixels that a given character overlaps to the left
	 * of where you try to draw it.
	 * @param f Font
	 * @param cEdge Character being considered
	 * @return Number of pixels (min 0)
	 */
	public static int getLeftOverlap(Font f,char cEdge)
	{
		return getCharacterInfo(f,cEdge).iLeftOverlap;
	}

	/**
	 * Obtain the number of pixels that a given character overlaps to the right
	 * of its claimed advance width.
	 * @param f Font
	 * @param cEdge Character being considered
	 * @return Number of pixels (min 0)
	 */
	public static int getRightOverlap(Font f,char cEdge)
	{
		CharacterInfo ci=getCharacterInfo(f,cEdge);
		return ci.iRightExtent-ci.iReportedWidth;
	}

	/**
	 * Returns the reported width of a character (from Java).
	 * @param f Font
	 * @param c Character
	 * @return Width in pixels
	 */
	public static int getReportedWidth(Font f,char c)
	{
		return getCharacterInfo(f,c).iReportedWidth;
	}

	/**
	 * Returns the actual right extent of a character, in pixels from its
	 * origin point (i.e. does not include the left overlap).
	 * @param f Font
	 * @param c Character
	 * @return Right extent in pixels
	 */
	public static int getRightExtent(Font f,char c)
	{
		return getCharacterInfo(f,c).iRightExtent;
	}

	/**
	 * Obtain the number of pixels that a given character overlaps to the right
	 * of its claimed advance width, when the italic slope is taken into account.
	 * (In other words, it is expected that the top part of an I will overlap by
	 * a bit, so we don't count that here whereas getRightOverlap does.)
	 * @param f Font
	 * @param cEdge Character being considered
	 * @return Number of pixels (min 0)
	 */
	public static int getItalicRightOverlap(Font f,char cEdge)
	{
		return getCharacterInfo(f,cEdge).iRightItalicOverlap;
	}

	/**
	 * Display character metrics (for testing).
	 * @param g2 Graphics context
	 * @param f Font
	 * @param c Character
	 * @param iX X co-ordinate to start drawing
	 * @param iY Y co-ordinate
	 * @return Number of X pixels used
	 */
	public static int drawCharacterTest(Graphics2D g2,Font f,char c,int iX,int iY)
	{
		CharacterInfo ci=getCharacterInfo(f,c);
		g2.setRenderingHint(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setFont(f);
		g2.setColor(Color.black);
		g2.drawString(c+"",iX+3+ci.iLeftOverlap,iY);
		g2.setColor(Color.red);
		g2.fillRect(iX,iY-ci.iAscent,1,ci.iAscent);
		g2.setColor(Color.blue);
		g2.fillRect(iX+1,iY,1,ci.iDescent);
		g2.setColor(Color.red);
		g2.fillRect(iX+3,iY+ci.iDescent+3,ci.iLeftOverlap,1);
		g2.setColor(Color.blue);
		g2.fillRect(iX+3+ci.iLeftOverlap,iY+ci.iDescent+2,ci.iRightExtent,1);
		g2.setColor(Color.green);
		g2.fillRect(iX+3+ci.iLeftOverlap,iY+ci.iDescent+4,ci.iReportedWidth,1);
		g2.setColor(Color.magenta);
		g2.fillRect(iX+3+ci.iLeftOverlap+ci.iReportedWidth,iY+ci.iDescent+5,ci.iRightItalicOverlap,1);

		return 3+ci.iLeftOverlap+ci.iRightExtent;
	}

	/**
	 * Mac OS X 10.3.9, at least in Java version 1.4.2_12, throws nasty
	 * exceptions when trying to render Unicode characters in headless mode
	 * (only if they don't exist in the current font, I think, but still).
	 * We replace characters with question marks in that case; running on 10.3
	 * is therefore not supported.
	 */
	public static boolean isBrokenMacOS=
		System.getProperty("os.name").indexOf("Mac")!=-1 &&
		System.getProperty("os.version").startsWith("10.3");

	/**
	 * Obtains information for a single character.
	 * @param f Font
	 * @param c Character
	 * @return Information
	 */
	private static CharacterInfo getCharacterInfo(Font f,char c)
	{
		synchronized(mCache)
		{
			// Get map for character
			Map<String,CharacterInfo> mCharacters=mCache.get(f);
			if(mCharacters==null)
			{
				mCharacters=new HashMap<String,CharacterInfo>();
				mCache.put(f,mCharacters);
			}
			if(isBrokenMacOS && c>255) c='?';
			String sCharacter=c+"";
			CharacterInfo ci=mCharacters.get(sCharacter);
			if(ci==null)
			{
				ci=new CharacterInfo();
				mCharacters.put(sCharacter,ci);

				// Get image for calculations (reuse if possible)
				int iSize=f.getSize();
				if(DEBUGMETRICS) System.err.println("=== "+c+" ("+f+") ===");
				if(biCache==null || !(biCache.getWidth()==iSize*3 && biCache.getHeight()==iSize*3))
					biCache=new BufferedImage(iSize*3,iSize*3,BufferedImage.TYPE_BYTE_GRAY);

				// Clear image and draw character in it
				Graphics2D g2=biCache.createGraphics();
				g2.setColor(Color.black);
				g2.fillRect(0,0,iSize*3,iSize*3);
				g2.setRenderingHint(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2.setFont(f);
				g2.setColor(Color.white);
				g2.drawString(sCharacter,iSize,iSize*2);

				// Check for ascent (highest non-blank line)
				// Defined as number of pixels above (not including) baseline
				byte[] abLine=new byte[iSize*3];
				{
					int iY;
					yloop1: for(iY=0;iY<iSize*2;iY++)
					{
						biCache.getData().getDataElements(0,iY,iSize*3,1,abLine);
						for(int iX=0;iX<abLine.length;iX++)
						{
							if(filled(abLine[iX]))
							{
								break yloop1;
							}
						}
					}
					ci.iAscent=iSize*2-iY;
					if(DEBUGMETRICS) System.err.println("Ascent: "+ci.iAscent);
				}

				// Check for descent (lowest non-blank line)
				// Defined as number of pixels below (or including) baseline
				{
					int iY;
					yloop2: for(iY=iSize*3-1;iY>=iSize*2;iY--)
					{
						biCache.getData().getDataElements(0,iY,iSize*3,1,abLine);
						for(int iX=0;iX<abLine.length;iX++)
						{
							if(filled(abLine[iX]))
							{
								break yloop2;
							}
						}
					}
					ci.iDescent=iY-(iSize*2-1);
					if(DEBUGMETRICS) System.err.println("Descent: "+ci.iDescent);
				}

				// Check for left overlap
				{
					byte[] abCol=new byte[ci.iAscent+ci.iDescent];
					int iX;
					xloop1: for(iX=0;iX<iSize;iX++)
					{
						biCache.getData().getDataElements(
							iX,iSize*2-ci.iAscent,1,abCol.length,abCol);
						for(int iRow=0;iRow<abCol.length;iRow++)
						{
							if(filled(abCol[iRow])) break xloop1;
						}
					}
					ci.iLeftOverlap=iSize-iX;
					if(DEBUGMETRICS) System.err.println("Left overlap: "+ci.iLeftOverlap);
				}

				// Check for width and italic right overlap
				{
					ci.iReportedWidth=(int)f.getStringBounds(	sCharacter,
						biCache.createGraphics().getFontRenderContext()).getWidth();

					for(int iY=iSize*2-ci.iAscent;iY<iSize*2+ci.iDescent;iY++)
					{
						biCache.getData().getDataElements(0,iY,iSize*3,1,abLine);
						int iX;
						for(iX=iSize*3-1;iX>=iSize;iX--)
						{
							if(filled(abLine[iX]))
							{
								break;
							}
						}
						ci.iRightExtent=Math.max(ci.iRightExtent,iX-iSize+1);
						if(f.isItalic())
						{
							ci.iRightItalicOverlap=Math.max(ci.iRightItalicOverlap,
								(iX-iSize+1) - (ci.iReportedWidth+(int)(f.getItalicAngle()*(iSize*2-iY))));
						}
					}

					// Handle spaces/blank characters
					if(ci.iRightExtent==0) ci.iRightExtent=ci.iReportedWidth;
				}
				if(DEBUGMETRICS) System.err.println("Right extent: "+ci.iRightExtent);
				if(DEBUGMETRICS) System.err.println("Right italic overlap: "+ci.iRightItalicOverlap);
			}
			return ci;
		}
	}

	/**
	 * Do we count a pixel as being filled and not disposable?
	 * @param b Byte value
	 * @return True if it's solid enough to count
	 */
	private final static boolean filled(final byte b)
	{
		return (b&0xff) > 10;
	}

	/**
	 * Obtains the actual ascent of a given character. Ascent is defined as pixels
	 * above the baseline.
	 * @param f Font
	 * @param c Character
	 * @return Ascent in pixels
	 */
	public static int getAscent(Font f,char c)
	{
		return getCharacterInfo(f,c).iAscent;
	}

	/**
	 * Obtains the actual descent of a given character. Ascent is defined as
	 * pixels below and including the baseline.
	 * @param f Font
	 * @param c Character
	 * @return Descent in pixels
	 */
	public static int getDescent(Font f,char c)
	{
		return getCharacterInfo(f,c).iDescent;
	}

	/**
	 * Returns ascent for a piece of text.
	 * @param f Font
	 * @param sText Text to examine
	 * @return Ascent for that text (low if it's just  lower-case, for instance)
	 */
	public static int getMaxAscent(Font f,String sText)
	{
		int iAscent=0;
	  for(int i=0;i<sText.length();i++)
	  {
	  	iAscent=Math.max(iAscent,getAscent(f,sText.charAt(i)));
	  }
	  return iAscent;
	}

	/**
	 * Returns descent for a piece of text.
	 * @param f Font
	 * @param sText Text to examine
	 * @return Descent for that text (could be zero if no descenders, for instance)
	 */
	public static int getMaxDescent(Font f,String sText)
	{
		int iDescent=0;
	  for(int i=0;i<sText.length();i++)
	  {
	  	iDescent=Math.max(iDescent,getDescent(f,sText.charAt(i)));
	  }
	  return iDescent;
	}

	/**
	 * @param f Font to consider
	 * @return Maximum ascent for alphanumeric characters in font (does NOT
	 *   include accents)
	 */
	public static int getMaxNormalAscent(Font f)
	{
		int iAscent=0;
	  for(char c='A';(c>='A' && c<='Z') || (c>='a' && c<='z') || (c>='0' && c<='9') ;c++)
	  {
	  	iAscent=Math.max(iAscent,getAscent(f,c));

	  	// Makes loop work
	  	if(c=='Z') c='a'-1;
	  	if(c=='z') c='0'-1;
	  }
	  return iAscent;
	}

	/**
	 * @param f Font to consider
	 * @return Maximum descent for alphanumeric characters in font (does NOT
	 *   include accents)
	 */
	public static int getMaxNormalDescent(Font f)
	{
		int iDescent=0;
	  for(char c='A';(c>='A' && c<='Z') || (c>='a' && c<='z') || (c>='0' && c<='9') ;c++)
	  {
	  	iDescent=Math.max(iDescent,getDescent(f,c));

	  	// Makes loop work
	  	if(c=='Z') c='a'-1;
	  	if(c=='z') c='0'-1;
	  }
	  return iDescent;
	}
}
