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
 * Graph53.java
 *
 * Created on 18 August 2006, 13:13
 *
 * Copyright:   � BoDiDog, and all contributors
 *		http://bodidog.mysite.wanadoo-members.co.uk/
 * License:	Lesser GNU Public License (LGPL)
 *		http://www.opensource.org/licenses/lgpl-license.html
 */

package samples.mu120.lib.graph54;

import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 *
 * @author ag
 */
public class Graph54 {

    private final static int tickSize = 4;

    private int xFrom, xTo;
    private int yFrom, yTo;
    private String xCaption;
    private String [] xLabels;
    private String yCaption;
    private String [] yLabels;

    private int textWidth;
    private int textHeight;
    private int textDescent;

    private int header;
    private int footer;
    private int leftMargin;
    private int rightMargin;

    private int i0=-1, j0=-1; // index for (0,0), may not be there of course
    private int xbl, ybl; // bottom left corner of the graph
    private int dx, dy; // pixels per unit label or tick, >=1, an int
    private double dDX; // pixels per unit x value, can be <1, a double
    private double dDY; // pixels per unit y value, can be <1, a double



    /** Creates a new instance of Graph54 */
    public Graph54(
            int [] xFromTo,
            int [] yFromTo,
            String xCaption,
            String [] xLabels,
            String yCaption,
            String [] yLabels
            ) {
        this.xFrom = xFromTo [0];
        this.xTo = xFromTo [1];
        this.yFrom = yFromTo [0];
        this.yTo = yFromTo [1];
        this.xCaption = xCaption;
        this.xLabels = xLabels;
        this.yCaption = yCaption;
        this.yLabels = yLabels;
    }

    /**
     * Utility for determining the width in pixels of the
     * given string as drawn on the given Graphics object.
     */
    public static int stringWidth(Graphics g, String str) {
        if (g instanceof Graphics2D)
            return (int)(g.getFont().getStringBounds(str, ((Graphics2D)g).getFontRenderContext()).getWidth()+.5);
        else
            return g.getFontMetrics().stringWidth(str);
    }

    /**
     * Utility for determining the height in pixels of the
     * given string as drawn on the given Graphics object.
     */
    public static int stringHeight(Graphics g, String str) {
        if (g instanceof Graphics2D)
            return (int)(g.getFont().getStringBounds(str, ((Graphics2D)g).getFontRenderContext()).getHeight()+.5);
        else
            return g.getFontMetrics().getHeight();
    }

    /**
     * Scale the graph given a graphic context, positiom and size
     */
    public void fit(Graphics g, int x, int y, int w, int h) {
        int i, j;
        textWidth = 0;
        textHeight = g.getFontMetrics().getHeight();
        textDescent = g.getFontMetrics().getDescent();

        // determine x0, y0, if its there, from the labelling
        // calculate header, footer, margin sizes including ticks and labels
        for (j=0; j<xLabels.length; j++)
            if (xLabels[j].equals("0"))
                j0=j;
        for (i=0; i<yLabels.length; i++) {
            if (yLabels[i].equals("0"))
                i0=i;
            if (textWidth < stringWidth(g, yLabels[i]))
                textWidth = stringWidth(g, yLabels[i]);
        }
        header = textHeight;
        footer =
                ((yFrom >= 0) ? tickSize : 0) +
                textHeight +
                ((xCaption==null || xCaption.equals("")) ? 0 : textHeight) +
                textDescent;
        leftMargin = (xFrom < 0) ? textWidth : tickSize + 2*textWidth;
        rightMargin = 2*textWidth;

        dx = (w-leftMargin-rightMargin)/(xLabels.length-1); // no pixles between ticks
        dy = (h-header-footer)/(yLabels.length-1); // no pixles between ticks
        xbl = x+leftMargin;
        ybl = y+header+(yLabels.length-1)*dy;
        dDX = dx*(xLabels.length-1) / ((double) (xTo-xFrom));
        dDY = dy*(yLabels.length-1) / ((double) (yTo-yFrom));
    }

    /**
     * Render the bar chart given a graphic context
     */
    public void paint(Graphics g, int x, int y, int w, int h) {
        int i, j, xx, yy;

        fit(g, x, y, w, h);

        // scale, draw X axis, ticks and labels, and caption
        yy = ybl - ((i0 < 1) ? 0 : i0 * dy);
        g.drawLine(xbl, yy, xbl+(xLabels.length-1)*dx, yy);
        for (j=0; j<xLabels.length; j++) {
            if (j == j0) {
                g.drawString("0", xbl+j0*dx-tickSize-stringWidth(g,"0")-1, ybl-i0*dy+tickSize+textHeight);
            } else {
                xx = xbl+j*dx;
                g.drawLine(xx, yy, xx, yy+tickSize);
                String lab = xLabels [j];
                g.drawString(lab, xx-stringWidth(g,lab)/2, yy+tickSize+textHeight);
            }
        }
        if (xCaption != null && ! xCaption.equals(""))
            g.drawString(xCaption, x+w-rightMargin, ((i0 == -1) ? ybl : ybl-i0*dy));

        // scale, draw Y axis, ticks and labels
        xx = xbl + ((j0 < 1) ? 0 : j0 * dx);
        if (yCaption != null && ! yCaption.equals(""))
            g.drawString(yCaption, xx+1, y+header-g.getFontMetrics().getDescent());
        g.drawLine(xx, ybl, xx, ybl-(xLabels.length-1)*dy);
        for (i=0; i<xLabels.length; i++) {
            if (i != i0) {
                yy = ybl-i*dy;
                g.drawLine(xx, yy, xx-tickSize, yy);
                String lab = yLabels [i];
                g.drawString(lab, xx-tickSize-stringWidth(g,lab)-1, yy+textHeight/3);
            }
        }
    }

    public void drawPoint(Graphics g, int x, int y) {
        // calclate (x,y)
        int px = xbl + (int)((x - xFrom) * dDX + 0.5);
        int py = ybl - (int)((y - yFrom) * dDY + 0.5);
        // diamond shape
        int [] xPoints = {px+3,px,px-3,px};
        int [] yPoints = {py,py-3,py,py+3};
        g.fillPolygon(xPoints, yPoints, 4);
    }

    public void drawPoint(Graphics g, String label, int x, int y) {
        double tw = stringWidth(g, "0") / 2.0;
        double th = stringHeight(g, "0") / 2.0;
        double thw = th+tw;
        drawPoint(g, x, y);
        // calclate (x,y)
        int px = xbl + (int)((x - xFrom) * dDX + 0.5);
        int py = ybl - (int)((y - yFrom) * dDY + 0.5);
        // diamond shape
        int [] xPoints = {px+3,px,px-3,px};
        int [] yPoints = {py,py-3,py,py+3};
        g.fillPolygon(xPoints, yPoints, 4);
        double theta = Math.atan2(((double) y), ((double) x));
        //System.err.println(i + ": " + (theta*180.0/Math.PI) + " | " + stringWidth(g, "0") + ", " + stringHeight(g, "0"));
        g.drawString(label, (int) (px-tw + thw*Math.cos(theta)), (int) (py+th - thw*Math.sin(theta)));
    }

    public void drawLine(Graphics g, int x1, int y1, int x2, int y2) {
        g.drawLine(xbl+(int)((x1-xFrom)*dDX+0.5), ybl-(int)((y1-yFrom)*dDY+0.5), xbl+(int)((x2-xFrom)*dDX+0.5), ybl-(int)((y2-yFrom)*dDY+0.5));
    }

} // class end
