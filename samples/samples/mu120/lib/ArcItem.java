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
/*
 * ArcItem.java
 *
 * Created on 15 September 2006, 15:02
 *
 * Copyright:   � BoDiDog, and all contributors
 *		http://bodidog.mysite.wanadoo-members.co.uk/
 * License:	Lesser GNU Public License (LGPL)
 *		http://www.opensource.org/licenses/lgpl-license.html
 */

package samples.mu120.lib;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import om.graph.GraphFormatException;
import om.graph.GraphItem;
import om.graph.GraphPoint;
import om.graph.GraphScalar;
import om.graph.World;

/** Creates a new instance of ArcItem.
 * Draws simple ellipses in the graph space.
 *
 * @author ag
 */
public class ArcItem extends GraphItem {

    public ArcItem(World world)
    throws GraphFormatException {
        super(world);
    }

    public ArcItem(World world, GraphScalar x, GraphScalar y, GraphScalar w, GraphScalar h, float start, float extent)
    throws GraphFormatException {
        super(world);
        setX(x);
        setY(y);
        setWidth(w);
        setHeight(h);
        setStart(start);
        setExtent(extent);
        setLineWidth(1);
        setLineColour(world.convertColour("fg"));
    }

    /** Centre */
    private GraphPoint gpCentre=GraphPoint.ZERO;

    /** Width and height */
    private GraphPoint gpSize = GraphPoint.ONE;

    /** Start and Arc angle */
    private float start = 0.0f;
    private float extent = 0.0f;

    /** Colours */
    private Color cFill = null, cLine = null;

    /** Outline thickness */
    private int iLineWidth = -1;

    public void init() throws GraphFormatException {
    }

    public void paint(Graphics2D g2) {
        // Work out the corner points and convert to pixels
        Point2D.Float p1 = (Point2D.Float) gpCentre.offset(new GraphPoint(-gpSize.getX().getWorldPosition()/2, -gpSize.getY().getWorldPosition()/2)).convertFloat(getWorld());
        Point2D.Float p2 = (Point2D.Float) gpCentre.offset(new GraphPoint( gpSize.getX().getWorldPosition()/2,  gpSize.getY().getWorldPosition()/2)).convertFloat(getWorld());
        Arc2D.Float eWhole = new Arc2D.Float(
                Math.min(p1.x, p2.x),
                Math.min(p1.y, p2.y),
                Math.abs(p1.x - p2.x),
                Math.abs(p1.y - p2.y),
                start,
                extent,
                Arc2D.PIE);
        //g2.setColor(Color.PINK);
        //g2.drawString("ABCDEFGHIJKLMNOPQR", 30, 30);
        //cFill = Color.MAGENTA;
        //g2.fillArc(0,0,100,100,0,45);
        if (iLineWidth == 0) {
            if (cFill != null) {
                g2.setColor(cFill);
                g2.fill(eWhole);
            }
        } else {
            Arc2D ePartial=new Arc2D.Float(
                    eWhole.x+(float)iLineWidth,
                    eWhole.y+(float)iLineWidth,
                    eWhole.width-(float)(iLineWidth*2),
                    eWhole.height-(float)(iLineWidth*2),
                    start,
                    extent,
                    Arc2D.PIE);
            Area aOuter=new Area(eWhole);
            aOuter.subtract(new Area(ePartial));

            g2.setColor(cLine);
            g2.fill(aOuter);

            if(cFill!=null) {
                g2.setColor(cFill);
                g2.fill(ePartial);
            }
        }
    }

    /**
     * Sets centre of ellipse, X co-ordinate.
     * @param gsX Co-ordinate
     */
    public void setX(GraphScalar gsX) {
        gpCentre = new GraphPoint(gsX, gpCentre.getY());
    }

    /**
     * Sets centre of ellipse, Y co-ordinate.
     * @param gsY Co-ordinate
     */
    public void setY(GraphScalar gsY) {
        gpCentre = new GraphPoint(gpCentre.getX(), gsY);
    }

    /**
     * Sets the start angle for the arc.
     * @param x angle
     */
    public void setStart(float x) {
        start = x;
    }

    /**
     * Sets the extent angle for the arc.
     * @param x Co-ordinate
     */
    public void setExtent(float x) {
        extent = x;
    }

    /**
     * Sets width (horizontal diameter) of ellipse.
     * Height defaults to the same value.
     * @param gsW Width
     */
    public void setWidth(GraphScalar gsW) throws GraphFormatException {
        if (gpSize == null)
            gpSize = new GraphPoint(gsW,gsW);
        else
            gpSize = new GraphPoint(gsW, gpSize.getY());
    }
    /**
     * Sets height (vertical diameter) of ellipse.
     * Width defaults to the same value.
     * @param gsH Height
     */
    public void setHeight(GraphScalar gsH) throws GraphFormatException {
        if(gpSize==null)
            gpSize=new GraphPoint(gsH,gsH);
        else
            gpSize = new GraphPoint(gpSize.getX(), gsH);
    }

    /**
     * Sets fill colour.
     * <p>
     * Appropriate colours can be obtained from {@link World#convertColour(String)}.
     * @param c Fill colour
     */
    public void setFillColour(Color c) {
        cFill=c;
    }

    /**
     * Sets outline colour. Calling this (except with null) also turns on the
     * line in the first place.
     * <p>
     * Appropriate colours can be obtained from {@link World#convertColour(String)}.
     * @param c New colour (set null for no outline)
     */
    public void setLineColour(Color c) {
        cLine=c;
        if (c!=null && iLineWidth==-1)
            iLineWidth=1;
    }

    /**
     * Sets line width in pixels. Also turns on outline if it wasn't already,
     * setting its colour to the colour constant 'fg'.
     * @param i Line width
     * @throws GraphFormatException If 'text' isn't defined
     */
    public void setLineWidth(int i) throws GraphFormatException {
        iLineWidth=i;
        if (i>0 && cLine==null)
            cLine=getWorld().convertColour("fg");
    }
}

