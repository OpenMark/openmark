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
 * BarChart.java
 *
 * Created on 16 August 2006, 15:00
 *
 * Copyright:   � BoDiDog, and all contributors
 *		http://bodidog.mysite.wanadoo-members.co.uk/
 * License:	Lesser GNU Public License (LGPL)
 *		http://www.opensource.org/licenses/lgpl-license.html
 */

package samples.mu120.lib.barchart;

import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * A very basic BarChart.
 * @author ag
 */
public class BarChart {
    
    private final static int tickSize = 4;
    
    private String xCaption;
    private String yCaption;
    
    /**
     * Integer values for the bar chart
     */
    private int [] yValues;
    /**
     * Labels for the Y axis
     */
    private String [] yLabels;
    /**
     * Labels for the X axis
     */
    private String [] xLabels;
    
    /** Creates a new instance of BarChart */
    public BarChart(String xCaption, String [] xLabels, String yCaption, String [] yLabels, int [] yValues) {
        this.xCaption = xCaption;
        this.xLabels = xLabels;
        this.yCaption = yCaption;
        this.yLabels = yLabels;
        this.yValues = yValues;
    }
    
    /**
     * Utility for determining the width in pixels of the
     * given string as drawn on the given Graphics object.
     */
    public static int stringWidth(Graphics g, String str) {
        if (g instanceof Graphics2D)
            return (int)(g.getFont().getStringBounds(str, ((Graphics2D) g).getFontRenderContext()).getWidth()+.5);
        else
            return g.getFontMetrics().stringWidth(str);
    }
    
    /**
     * Utility for determining the height in pixels of the
     * given string as drawn on the given Graphics object.
     */
    public static int stringHeight(Graphics g, String str) {
        //System.out.println(": " + g.getFontMetrics().getHeight() + ", " + g.getFontMetrics().getAscent() + ", " + g.getFontMetrics().getDescent());
        if (g instanceof Graphics2D)
            return (int)(g.getFont().getStringBounds(str, ((Graphics2D) g).getFontRenderContext()).getHeight()+.5);
        else
            //return g.getFontMetrics().getHeight();
            return g.getFontMetrics().getAscent()+g.getFontMetrics().getDescent();
    }
    
    /**
     * Render the bar chart given a graphic context
     */
    public void paint(Graphics g, int x, int y, int w, int h) {
        int i;
        int textHeight = g.getFontMetrics().getHeight();
        int textDescent = g.getFontMetrics().getDescent();
        int yLabelsWidth = 0; // width of the widest Y label
        int yMax = 0; // maximum value for the Y axis
        
        //g.clipRect(x, y, w, h);
        //g.setColor(Color.black);
        //g.setFont(new Font("Aerial", Font.PLAIN, 12));
        
        // determine Y (vertical) axis maximum value
        // using either the final Y label, or the largest Y value.
        try {
            yMax = (new Integer((String) yLabels [yLabels.length - 1])).intValue();
        } catch (NumberFormatException e) {
            for (i=0; i<yValues.length; i++)
                if (yMax < yValues [i])
                    yMax = yValues [i];
        }
        
        // calculate header, footer, margin sizes including ticks and labels
        for (i=0; i<yLabels.length; i++)
            if (yLabelsWidth < stringWidth(g,yLabels[i]))
                yLabelsWidth = stringWidth(g,yLabels[i]);
        int header = textHeight;
        int footer = tickSize + textHeight + (xCaption.equals("") ? 0 : textHeight) + textDescent;
        int leftMargin = 1 + yLabelsWidth + tickSize;;
        
        // calculate origin for the Bar Chart
        int x0 = x+leftMargin;
        int y0 = y+h-footer;
        
        // scale, draw X axis, ticks and labels
        int dx = (x+w-x0)/xLabels.length;
        g.drawLine(x0-tickSize, y0, x0+dx*xLabels.length, y0);
        for (i=0; i<xLabels.length; i++) {
            int xx = x0 + (i+1)*dx;
            g.drawLine(xx, y0, xx, y0+tickSize);
            String lab = xLabels [i];
            g.drawString(lab, xx-stringWidth(g,lab)-1, y0+tickSize+textHeight-textDescent);
        }
        if (! xCaption.equals(""))
            g.drawString(xCaption, x0, y0+tickSize+textHeight+textHeight-textDescent);
        
        
        // scale, draw Y axis, ticks and labels
        if (! yCaption.equals(""))
            g.drawString(yCaption, x0, y+header);
        int dy = (y0 - y - header) / (yLabels.length - 1);
        g.drawLine(x0, y0-dy*(yLabels.length - 1), x0, y0+tickSize);
        for (i=0; i<yLabels.length; i++) {
            int yy = y0 - i*dy;
            g.drawLine(x0-tickSize, yy, x0, yy);
            String lab = yLabels [i];
            g.drawString(lab, x0-stringWidth(g,lab)-tickSize-1, yy);
        }
        
        // scale, draw the bars
        for (i=0; i<yValues.length; i++) {
            int xx = (int) (x0 + i*dx);
            int yVal = yValues [i] * dy*(yLabels.length - 1) / yMax;
            g.fillRect(xx+1, y0-yVal, (int) (dx-1), yVal);
        }
        
    }
    
}
