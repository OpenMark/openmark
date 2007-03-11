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
package samples.mu120.module5.question03;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;
import samples.mu120.lib.SimpleQuestion2;

import om.OmDeveloperException;
import om.OmException;

import samples.mu120.lib.barchart.BarChart;
import om.stdcomponent.CanvasComponent;

import samples.shared.Helper;

public class Q3 extends SimpleQuestion2 {
    
    private static final double tolerance = 1E-6;
    
    double dAns;
    
    // BarChart data
    String xCaption = "Number of children per household";
    String [] xLabels = {"0", "1", "2", "3", "4", "5"};
    String yCaption = "Number of households";
    String [] yLabels = {"0", "", "", "", "", "", "", "", "40"};
    int [] yValues = {35, 20, 25, 15, 5, 0};
    
    protected void init() throws OmException {
        
        Random r = getRandom();
        int X;
        do { // avoid the question that asks for a bar of height one division
            X= r.nextInt(5);
        } while (X == 5);
        int Y = yValues [X];
        
        dAns = Y;
        
        setPlaceholder("X", "" + X);
        setPlaceholder("CHILD(REN)", (X==1) ? "child" : "children");
        setPlaceholder("Y", "" + Y);
        setPlaceholder("Y/5", "" + (Y/5));
        
        BarChart bc = new BarChart(xCaption, xLabels, yCaption, yLabels, yValues);
        CanvasComponent cc = (CanvasComponent) getComponent("barchart");
        Graphics g = cc.getGraphics().create();
        g.setColor(new Color(115,111,234));
        bc.paint(g, 0, 0, 196, 196);
        g.dispose();
        getComponent("inputbox").setDisplay(true);
        //cc.markChanged();
        
        // store question information
        getResults().setQuestionLine("How many households have " + X + " children?");
        getResults().setAnswerLine(""+Y);
    }
    
    protected boolean isRight(int iAttempt) throws OmDeveloperException {
        
        String response = (getEditField("response").getValue().trim());
        double dbl = Helper.inputNumber(response);
        
        // store response information
        getResults().appendActionSummary("Attempt " + iAttempt + ": " + response);
        
        // correct answer (can be wrong way round) ?
        if (Helper.range(dbl, dAns, tolerance))
            return true;
        
        // wrong answer handled from here on .....
        
        if (iAttempt < 3) {
            if (Double.isNaN(dbl))
                getComponent("nonumber").setDisplay(true);
            else {
                if (dbl < dAns)
                    setFeedbackID("toosmall");
                else
                    setFeedbackID("toolarge");
            }
        }
        
        // feeback, given attempt 1, 2, or 3
        switch (iAttempt) {
            case 1:
                setFeedbackID("feedback1");
                break;
            case 2:
                setFeedbackID("feedback2");
                break;
            case 3:
                break;
        }
        
        return false;
    }
    
}