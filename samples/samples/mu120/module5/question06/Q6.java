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
package samples.mu120.module5.question06;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;
import samples.mu120.lib.SimpleQuestion2;

import om.OmDeveloperException;
import om.OmException;
import samples.mu120.lib.graph54.Graph54;
import om.stdcomponent.CanvasComponent;
import samples.shared.Helper;

public class Q6 extends SimpleQuestion2 {

    private static final double tolerance = 1E-6;

    // Numbers for question variants
    private final static String [] months = {"January", "March", "July"};
    private final static int [] baseTemps = {10, 15, 20};

    private final static double ansTime = 2.0; // time scale
    private final static double ansTemp = 1.0; // temp scale

    // Graph data
    int [] xFromTo = {8, 22};
    int [] yFromTo = {1, 8};

    int [] pXs = { 8, 10, 12, 14, 16, 18}; // hours
    int [] pYs = { 1,  3,  5,  7,  4,  2}; // add base temperature for actual reading
    String [] nullLabels = {"  ", "  ", "  ", "  ", "  ", "  ", "  ", "  "};
    String [] xLabels = { "8", "10", "12", "14", "16", "18", "20", "22"};
    String [] yLabels = {"11", "12", "13", "14", "15", "16", "17", "18"};

    protected void init() throws OmException {

        Random r = getRandom();
        int i = r.nextInt(3);
        String month = months [i];
        int X = baseTemps [i];
        yFromTo [0] += X;
        yFromTo [1] += X;
        for (i=0; i<pYs.length; i++)
            pYs [i] += X;
        for (i=0; i<yLabels.length; i++)
            yLabels [i] = "" + (1+i+X);

        setPlaceholder("month", month);
        setPlaceholder("X1", ""+(X+1));
        setPlaceholder("X3", ""+(X+3));
        setPlaceholder("X5", ""+(X+5));
        setPlaceholder("X7", ""+(X+7));
        setPlaceholder("X4", ""+(X+4));
        setPlaceholder("X2", ""+(X+2));

        Graph54 g54a = new Graph54(xFromTo, yFromTo, "hours", nullLabels, "temperature �C", nullLabels);
        CanvasComponent cc = (CanvasComponent) getComponent("graph1");
        Graphics g = cc.getGraphics().create();
        g.setColor(Color.BLACK); // was: g.setColor(new Color(115,111,234));
        g54a.paint(g, 10, 0, 224, 224);
        //g.setColor(new Color(92,47,138));
        //g54.drawLine(g, -100, -100, 100, 100); g54.drawLine(g, 50, -50, -50, 50);
        g.dispose();

        Graph54 g54b = new Graph54(xFromTo, yFromTo, "hours", xLabels, "temperature �C", yLabels);
        cc = (CanvasComponent) getComponent("graph2");
        g = cc.getGraphics().create();
        g.setColor(Color.BLACK); // was: g.setColor(new Color(115,111,234));
        g54b.paint(g, 10, 0, 224, 224);
        g.setColor(new Color(92,47,138));
        for (i=0; i<pXs.length; i++)
            g54b.drawPoint(g, pXs [i], pYs [i]);

        g.dispose();

        getComponent("inputbox").setDisplay(true);
        //cc.markChanged();

        // store question information
        getResults().setQuestionLine("What are the best divisions for time and temperature?");
        getResults().setAnswerLine("2 hours and 1 degree C");
    }

    protected boolean isRight(int iAttempt) throws OmDeveloperException {

        String rTime = (getEditField("rtime").getValue().trim());
        String rTemp = (getEditField("rtemp").getValue().trim());
        double rDTime = Helper.inputNumber(rTime);
        double rDTemp = Helper.inputNumber(rTemp);

        // store response information
        getResults().appendActionSummary("Attempt " + iAttempt + ": " + "time: " + rTime + ", temperature: " + rTemp);

        // correct answer ?
        boolean bTime = Helper.range(rDTime, ansTime, tolerance);
        boolean bTemp = Helper.range(rDTemp, ansTemp, tolerance);
        if (bTime && bTemp)
            return true;

        // wrong answer handled from here on .....

        if (iAttempt == 3)
            return false;

        if (Double.isNaN(rDTime) || Double.isNaN(rDTemp)) {
            getComponent("nonumber").setDisplay(true);
            return false;
        }

        // feeback, given attempt 1, 2
        switch (iAttempt) {
            case 1:
                if (!bTime && !bTemp)
                    setFeedbackID("hint1");
                else if (bTime && !bTemp && rDTemp > ansTemp)
                    setFeedbackID("feedback1a");
                else if (bTime && !bTemp && rDTemp < ansTemp)
                    setFeedbackID("feedback1b");
                else if (bTemp && !bTime && rDTime > ansTime)
                    setFeedbackID("feedback1c");
                else if (bTemp && !bTime && rDTime < ansTime)
                    setFeedbackID("feedback1d");
                break;
            case 2:
                if (!bTime && !bTemp)
                    setFeedbackID("hint2");
                else if (bTime && !bTemp && rDTemp > ansTemp)
                    setFeedbackID("feedback2a");
                else if (bTime && !bTemp && rDTemp < ansTemp)
                    setFeedbackID("feedback2b");
                else if (bTemp && !bTime && rDTime > ansTime)
                    setFeedbackID("feedback2c");
                else if (bTemp && !bTime && rDTime < ansTime)
                    setFeedbackID("feedback2d");
                break;
            case 3:
                break;
        }

        return false;
    }

} // end class