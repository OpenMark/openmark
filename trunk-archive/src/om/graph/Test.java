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
import java.awt.image.BufferedImage;

import javax.swing.*;

import org.w3c.dom.Document;

import util.xml.XML;

/** Test code for graph routines */
public class Test
{
	/**
	 * Main method for testing the graph routines.
	 * @param args Not used.
	 */
	public static void main(String[] args)
	{
		try
		{
			String sCFTest="<world id='w1' px='0' py='0' pw='200' ph='200' xleft='0.0' xright='1.0' ybottom='0.0' ytop='1.0'><colourField id='cf'/></world>";


			String sRoundingTest=
				"			<world id=\"w1\" px=\"140\" py=\"58\" pw=\"290\" ph=\"255\" \r\n" +
				"				xleft=\"0.0\" xright=\"140.0\" ybottom=\"0.0\" ytop=\"2.2\">\r\n" +
				"				<xAxis ticks=\"20,10\" tickSide=\"-\" numbers=\"20\" omitNumbers=\"0\" label=\"temperature / �C\" />\r\n" +
				"				<yAxis maxY=\"2.0\" ticks=\"1,0.1\" tickSide=\"-\" numbers=\"0.5\" />\r\n" +
				"				<text x=\"15\" y=\"2.1\" text=\"pressure / 10���Pa\" />\r\n" +
				"				<text x=\"31\" y=\"2.14\" text=\"5\" />\r\n" +
				"			    <grid maxY=\"2.01\" xspacing=\"10\" yspacing=\"0.1\" />\r\n" +
				" <line x='0' y='1.4' x2='100' y2='1.4' lineColour='#f00,graph1'/>"+
				"			</world>";
/*
			String sAxisTest=
				"<world id='w1' px='50' py='10' pw='500' ph='500' "+
				"  xleft='-1.0' xright='1.0' ytop='1.0' ybottom='-4.0'>"+
				"  <yAxis ticks='0.5,0.1' minY='-0.5' x='-0.5' numbers='0.5' tickSide='-' rotateLabel='no' label='Frogs' numbersFont='10px' labelMargin='-4'/>"+
				"  <yAxis ticks='0.5,0.1' minY='-0.5' numbers='0.5' rotateNumbers='yes' tickSide='+' label='Tadpoles' labelMargin='8'/>"+
				"  <yAxis ticks='0.5,0.1' minY='-0.5' x='0.5' numbers='0.5' rotateNumbers='yes' rotateFlip='yes' tickSide='both' label='Frogspawn'/>"+
				"  <xAxis ticks='0.5,0.1' y='-1.5' numbers='0.5' tickSide='-' label='Frogs'/>"+
				"  <xAxis ticks='0.5,0.1' y='-2.5' numbers='0.5' rotateNumbers='yes' tickSide='+' label='Tadpoles'/>"+
				"  <xAxis ticks='0.5,0.1' y='-3.5' numbers='0.5' rotateNumbers='yes' rotateFlip='yes' tickSide='both' label='Frogspawn'/>"+
				"  <grid xSpacing='0.2,0.1' ySpacing='0.2' maxY='-3' minY='-3.5'/>"+
				"</world>";

			String sShapeTest=
				"<world id='w1' px='50' py='10' pw='500' ph='500' "+
				"  xleft='0' xright='1.0' ytop='1.0' ybottom='0'>"+
				"<rectangle x='0.1' y='0.1' width='0.1' height='0.1' fillColour='#800'/>"+
				"<rectangle x='0.2' y='0.2' width='0.1' height='0.1' fillColour='#800'/>"+
				"<rectangle x='0.3' y='0.1' x2='0.4' y2='0.2' lineColour='#800'/>"+
				"<rectangle x='0.4' y='0.2' width='0.1' height='0.1' lineColour='#800'/>"+
				"<rectangle x='0.5' y='0.1' width='0.1' height='0.1' fillColour='#8ff' lineColour='#800' lineWidth='4'/>"+
				"<rectangle x='0.7' y='0.1' x2='0.8' y2='0.2' lineColour='#800'/>"+
				"<rectangle x='0.7:4' y='0.1:4' x2='0.8:4' y2='0.2:4' lineColour='#800'/>"+

				"<ellipse x='0.1' y='0.5' width='0.1' fillColour='#800'/>"+
				"<ellipse x='0.2' y='0.6' height='0.1' fillColour='#800'/>"+
				"<ellipse x='0.3' y='0.5' width='0.07' height='0.3' lineColour='#800'/>"+
				"<ellipse x='0.4' y='0.6' width='0.1' lineColour='#800'/>"+
				"<ellipse x='0.5' y='0.5' width='0.1' fillColour='#8ff' lineColour='#800' lineWidth='4'/>"+
				"<ellipse x='0.7' y='0.5' width='0.1' lineColour='#800'/>"+
				"<ellipse x='0.7:4' y='0.5:4' width='0.1' lineColour='#800'/>"+

				"<line x='0.3' y='0.5' x2='0.35' y2='0.15'/>"+
				"<line x='0.5' y='0.5' x2='0.55' y2='0.15' lineWidth='4' lineColour='#800'/>"+
				"</world>";

			String sTextTest=
				"<world id='w1' px='50' py='10' pw='500' ph='500' "+
				"  xleft='-1.0' xright='1.0' ytop='1.0' ybottom='-1.0'>"+
				"<text x='0' y='0.9' align='left' text='Hello Graph!'/>"+
				"<text x='0' y='0.8' align='centre' text='Hello Graph!'/>"+
				"<text x='0' y='0.7' align='right' text='Hello Graph!'/>"+
				"<text x='0.9' y='0' align='left' angle='90' text='Hello Graph!'/>"+
				"<text x='0.8' y='0' align='centre' angle='90' text='Hello Graph!'/>"+
				"<text x='0.7' y='0' align='right' angle='90' text='Hello Graph!'/>"+
				"<text x='-0.9' y='0' align='left' angle='270' text='Hello Graph!'/>"+
				"<text x='-0.8' y='0' align='centre' angle='270' text='Hello Graph!'/>"+
				"<text x='-0.7' y='0' align='right' angle='270' text='Hello Graph!'/>"+
				"<text x='0' y='-0.9' align='left' angle='360' text='Hello Graph!'/>"+
				"<text x='0' y='-0.8' align='centre' angle='360' text='Hello Graph!'/>"+
				"<text x='0' y='-0.7' align='right' angle='360' text='Hello Graph!'/>"+
				"<text x='0' y='0' angle='255' colour='#efe' text='Hello Graph!' font='bold 30px'/>"+
				"<text x='0' y='0' angle='270' colour='#cfc' text='Hello Graph!' font='bold 30px'/>"+
				"<text x='0' y='0' angle='285' colour='#afa' text='Hello Graph!' font='bold 30px'/>"+
				"<text x='0' y='0' angle='300' colour='#8f8' text='Hello Graph!' font='bold 30px'/>"+
				"<text x='0' y='0' angle='315' colour='#6f6' text='Hello Graph!' font='bold 30px'/>"+
				"<text x='0' y='0' angle='330' colour='#4f4' text='Hello Graph!' font='bold 30px'/>"+
				"<text x='0' y='0' angle='345' colour='#2f2' text='Hello Graph!' font='bold 30px'/>"+
				"<text x='0' y='0' angle='0' colour='#000' text='Hello Graph!' font='bold 30px'/>"+
				"</world>";
	*/

			String sXML=
			"<world id='w1' px='90' py='10' pw='200' ph='200' "+
			"  xleft='-1.0' xright='1.0' ytop='1.0' ybottom='-1.0'>"+
			"	 <rectangle x='0' y='0.4' width='1' height='0.2'/>"+
			"	 <rectangle x='0' y='0' width='0.5' height='0.4' fillColour='#fee'/>"+
			"	 <rectangle x='0.5' y='0.3' width='0.5' height='0.2' lineColour='#f0f' fillColour='#fff' linewidth='4'/>"+
			"  <xAxis ticks='0.2,0.1' numbers='0.5' colour='#800' omitNumbers='0.0' tickSide='both'/>"+
			"  <yAxis ticks='0.2,0.1' numbers='0.5' colour='#800' omitNumbers='0.0' tickSide='-'/>"+
			"  <function minX='0' colour='#080' lineWidth='2.0' id='thingy'/>"+
			"  <parametricFunction minT='0' maxT='6.3' colour='#008' steps='300' lineWidth='1.0' id='thingy2'/>"+
			"  <colourField minX='-1' maxX='-0.5' minY='-1' maxY='-0.5' blockSize='1' id='thingy3'/>"+
			""+
			""+
			""+
			"</world>";
			String sThisTest=sRoundingTest;
			Document d=XML.parse(sThisTest);
			World w=new World(new World.Context()
				{
					public Color getColour(String sConstant)
					{
						if(sConstant.equals("fg")) return Color.black;
						return null;
					}

					public boolean useAlternates()
					{
						return false;
					}

					public String getFontFamily()
					{
						return "Verdana";
					}

					public int getFontSize()
					{
						return 13;
					}
				},d.getDocumentElement());

			if(sThisTest==sCFTest)
			{
				ColourFieldItem cfi=(ColourFieldItem)w.getItem("cf");
				cfi.setFunction(new ColourFieldItem.Function()
				{
					public Color f(double x,double y)
					{
						if(x>1.0 || y>1.0 || x<0.0 || y<0.0)
						{
							System.err.println("Got: "+x+","+y);
							return Color.black;
						}

						return new Color((float)x,(float)y,0.5f);
					}
				});
			}

			if(sThisTest==sXML)
			{
				((FunctionItem)w.getItem("thingy")).setFunction(
					new FunctionItem.Function()
					{
						public double f(double x)
						{
							return Math.sin(x * 2 * Math.PI);
						}
					});

				((ParametricFunctionItem)w.getItem("thingy2")).setFunction(
					new ParametricFunctionItem.Function()
					{
						public GraphPoint f(double t)
						{
							return new GraphPoint(
								 (Math.cos(3.0*t)),Math.sin(5.0*t));
						}
					});

				((ColourFieldItem)w.getItem("thingy3")).setFunction(
					new ColourFieldItem.Function()
					{
						public Color f(double x,double y)
						{
							float f1=(float)(3*Math.sqrt(Math.pow(x-(-0.8),2) + Math.pow(y-(-0.8),2)));
							float f2=(float)(3*Math.sqrt(Math.pow(x-(-0.6),2) + Math.pow(y-(-0.5),2)));
							return new Color(
								Math.min(f1,1.0f),Math.min(f2,1.0f),0.0f
								);
						}
					});
			}

			int iW=600,iH=600,iGraphX=50,iGraphY=10,iGraphW=500,iGraphH=500;
			BufferedImage bi=new BufferedImage(iW,iH,BufferedImage.TYPE_INT_RGB);
			Graphics2D g2=bi.createGraphics();
			boolean bAntiAlias=true;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				bAntiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				bAntiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			g2.setColor(Color.yellow);
			g2.fillRect(0,0,iW,iH);
			g2.setColor(Color.white);
			g2.fillRect(iGraphX,iGraphY,iGraphW,iGraphH);


			w.paint(g2);

			JFrame f=new JFrame("Graph test");
			JLabel l=new JLabel(new ImageIcon(bi));
			l.setBorder(BorderFactory.createEmptyBorder(100,100,100,100));

			f.getContentPane().add(l);
			f.pack();
			f.setVisible(true);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
}
