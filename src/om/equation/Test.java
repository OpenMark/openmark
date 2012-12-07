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
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.StringReader;

import javax.swing.*;

import om.equation.generated.EquationFormat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.misc.Fonts;
import util.xml.XML;

/**
 * Class for testing the equation compontent.
 */
public class Test extends JFrame
{
	/** Required by the Serializable interface. */
	private static final long serialVersionUID = 7648575150234190737L;

	/**
	 * @param args not used.
	 */
	public static void main(String[] args)
	{
		try
		{
			new Test();
//			 testTextEquation();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void testTextEquation() throws Exception
	{
		String sEquation="d = 1.8\\mbox{ m}";
		EquationFormat ef=new EquationFormat(new StringReader(
			sEquation
			));
		Element e=ef.equation().createDOM(XML.createDocument());
		e.getOwnerDocument().appendChild(e);
		System.out.println(XML.saveString(e.getOwnerDocument()));

		Document d=XML.createDocument();
		System.out.println(TextEquation.process(sEquation,d).getAttribute("alt"));

		d=XML.createDocument();
		System.out.println(XML.saveString(TextEquation.process(sEquation,d)));
	}

	Test() throws Exception
	{
		super("Equation test");

		boolean bAntiAlias=true;
		float fSize=1.0f;
		int iCols=4;

		String sSolo=null;
//		sSolo="\\left|\\frac{2n+6n}{2} + 4\\right|+\\left[\\frac{2n+6n}{2} + 4\\right]";
//		sSolo="2^{\\frac{1}{\\left(\\frac{2n+6n}{2} + 4\\right)+1}}";
//		sSolo="\\left{\\frac{2n+6n}{2} + 4\\right}";
//		sSolo="k_yx^2k";
//		sSolo="E_{str}x^2";
//		sSolo="r";
//		sSolo="\\sqrt{\\frac{\\sqrt{a}}{\\sqrt{a}\\sqrt{b}\\sqrt{M}\\sqrt{\\frac{a}{a}}}}";

		// This set of tests is intended to allow me to easily do regression testing
		// on the equation rendering.
		String[] asTest=
		{
			"z",

			"N",
			"\\frac{2n+6n}{2}",
			"\\frac{1}{2}(2n+6n)",
			"\\frac{2n^2}{n}",
			"4\\sqrt{n^2}",
			"\\sqrt{16}n^2",
			"N+\\sqrt{N}+\\sqrt{N+\\sqrt{N}}+1",
			"\\ln(2e^x) \\mbox{mbox test}",
			"6n",
			"e^x + N^x + a^x + e^y",
			"e",
			"1+azd","\\frac{1}{z+\\frac{1}{z}}","\\mbox{mbox with z in}",
			"z^{z^{z^{z^{z}}}}",

			"125^{2/3}", // From Phil
			"125^{\\frac{2}{3}}", // From Phil (alternate)
			"\\frac{1}{125_{\\frac{2}{3}}}",
			"8 - (-4) = -\\frac{8}{4}=-2", // From Phil


			"m = \\frac{1}{2n^2} \\sqrt{\\frac{2p}{q}}",
			"\\frac {n}{6} ÷ \\frac{6n}{p}",
			"\\frac{m^2 - nm - 2mn + 2n^2}{m - 2n}",

			"H = L\\mbox{sin}\u03b8", // From Greg

			"\\frac{\\sqrt{a}}{\\sqrt{b}}", // From Will

			// Greek letter tests
			"\\Delta + \\delta",

			// Super/sub testset
			"N^{N^{N^{N^{N^2}}}}",
			"N_{N_{N_{N_{N_2}}}}",
			"N^{N_{N_{N_3}}}_{N^{N^{N^2}}}",
			"N^{N^{N^N}}+\\sqrt{\\sqrt{\\sqrt{x}}}",
			"\\frac{1}{pq}",

			// Sigma tests
			"\\sum 4+z",
			"\\sum_{i=1}^{10} e^i",
			"\\int_{i=1}^{10} e^i \\frac{1}{2} \\sum_{i=1}^{10} e^i \\frac{1}{2}",
			"\\frac{1}{\\int_{i=1}^{10} e^i \\frac{1}{2} \\sum_{i=1}^{10} e^i \\frac{1}{2}}",

			// Displaysize test
			"\\frac{\\int_{i=1}^{10} e^i}{\\sum_{i=1}^{10} e^i + \\displaystyle\\sum_{i=1}^{10} e^i}",

			// Sub overlap
			"k_yx^2",

			// Brackets
			"\\left|\\frac{2n+6n}{2} + 4\\right}",
			"\\left[\\frac{2n+6n}{2} + 4\\right]",
			"\\left(\\frac{2n+6n}{2} + 4\\right)",
			"\\left{\\frac{2n+6n}{2} + 4\\right}",
			"2+\\left{\\frac{2n+6n}{2}+4\\right.",

			"\\frac{\\partial y}{\\partial x} \\propto \\hbar \\wr \\nabla \\ell \\forall x \\exists z_\\infty",

//			"x^2=n + N^N_N"
//			"N_N^N"
//			"= x ^N_N"
		};

		((JComponent)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		getContentPane().setLayout(new GridLayout(0,iCols,10,10));
		if(sSolo==null) {
			for(int i=0;i<asTest.length;i++) {
				getContentPane().add(getEquation(asTest[i],bAntiAlias,fSize));
			}
		} else {
			getContentPane().add(getEquation(sSolo,bAntiAlias,fSize));
		}

//		showOverlap('r',new Font("Times New Roman",Font.PLAIN,28));
//		stupidTest2(new Font("Times New Roman",Font.PLAIN,13));
//		stupidTest2(new Font("Times New Roman",Font.PLAIN,11));
//		stupidTest2(new Font("Times New Roman",Font.PLAIN,9));
//		stupidTest2(new Font("Times New Roman",Font.ITALIC,13));
//		stupidTest2(new Font("Times New Roman",Font.ITALIC,11));
//		stupidTest2(new Font("Times New Roman",Font.ITALIC,9));

		pack();
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	@SuppressWarnings("unused")
	private void showOverlap(char c,Font f)
	{
		BufferedImage bi=new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
		System.err.println(c+" width "+(f.getStringBounds(c+"",bi.createGraphics().getFontRenderContext()).getWidth()));
		System.err.println(c+" overlap "+
			Fonts.getLeftOverlap(f,c)+"/"+Fonts.getRightOverlap(f,c)+"/"+Fonts.getItalicRightOverlap(f,c));
		System.err.println(c+" ascent "+Fonts.getAscent(f,c)+", descent "+Fonts.getDescent(f,c));
	}

	void stupidTest() throws Exception
	{
		Font f=new Font("Times New Roman",Font.ITALIC,26);
		BufferedImage bi=new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
		Graphics2D g2=bi.createGraphics();
		boolean bAntiAlias=true;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			bAntiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			bAntiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		char c='X';

  	// Get outline of letter
  	GlyphVector gv=f.createGlyphVector(g2.getFontRenderContext(),new char[] {c});
  	Shape s=gv.getOutline(50,50);
  	Shape sRight;
  	if(f.isItalic())
  	{
  		// Use italic version for comparing right side
  		Area aItalic=new Area(s);
  		AffineTransform at=new AffineTransform();
  		at.translate(0,50);
  		at.shear(f.getItalicAngle(),0);
  		at.translate(0,-50);
  		aItalic.transform(at);
  		sRight=aItalic;
  	}
  	else
  	{
  		sRight=s;
  	}

  	g2.setColor(Color.white);
  	g2.fillRect(0,0,100,100);
  	g2.setColor(new Color(255,255,210));
  	g2.fillRect(0,50,100,100);
  	g2.setColor(new Color(255,210,255));
  	g2.fillRect(0,0,50,100);
  	g2.setColor(new Color(160,160,160));
  	g2.fill(s);
  	g2.setColor(Color.black);
  	g2.fill(sRight);

		JLabel l=new JLabel(new ImageIcon(bi));
		getContentPane().add(l);
	}

	void stupidTest2(Font f) throws Exception
	{
		int iSize=f.getSize();
		BufferedImage bi=new BufferedImage(27*(iSize*2),2*(iSize*3),BufferedImage.TYPE_INT_RGB);
		Graphics2D g2=bi.createGraphics();
		g2.setColor(Color.white);
		g2.fillRect(0,0,bi.getWidth(),bi.getHeight());

	for(char c='A';c<='Z';c++)
	{
		int iX=(c-'A')*(iSize*2)+iSize;
		char cLower=(char)(c-'A'+'a');

		Fonts.drawCharacterTest(g2,f,c,iX,iSize*2);
		Fonts.drawCharacterTest(g2,f,cLower,iX,iSize*5);
	}
	Fonts.drawCharacterTest(g2,f,' ',26*iSize*2+iSize,iSize*4);

//		boolean bAntiAlias=true;
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//			bAntiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
//		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//			bAntiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
//		g2.setFont(f);
//
//  	g2.setColor(Color.white);
//  	g2.fillRect(0,0,bi.getWidth(),bi.getHeight());
//
//		for(char c='A';c<='Z';c++)
//		{
//			int iX=(c-'A')*(iSize*2)+iSize;
//			char cLower=(char)(c-'A'+'a');
//
//			showChar(f,g2,c,iX,iSize*2);
//			showChar(f,g2,cLower,iX,iSize*5);
//		}

		JLabel l=new JLabel(new ImageIcon(bi));
		getContentPane().add(l);
	}

	@SuppressWarnings("unused")
	private void showChar(Font f,Graphics2D g2,char c,int iX,int iY)
	{
		g2.setColor(Color.red);
		g2.fillRect(iX-2,iY-Fonts.getAscent(f,c),1,Fonts.getAscent(f,c));
		g2.setColor(Color.black);
		g2.drawString(""+c,iX,iY);
	}

	private JLabel getEquation(String sEquation,
		boolean bAntiAlias,float fSize) throws Exception
	{
		EquationFormat ef=new EquationFormat(new StringReader(sEquation	));
		Element eXML=ef.equation().createDOM(XML.createDocument());
//		System.out.println("\nDocument:\n\n"+XML.saveString(eXML));

		Equation e=Equation.create(eXML,fSize);
		e.prepare();
		e.setFont("Verdana",null);
		e.setFont("Times New Roman",new int[] {14,12,10});

		BufferedImage bi=e.render(Color.black,Color.white,bAntiAlias);

		JLabel l=new JLabel(new ImageIcon(bi));
		return l;
	}
}
