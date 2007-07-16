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
package samples.mu120.module5.question04;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;
import samples.mu120.lib.SimpleQuestion2;

import om.OmDeveloperException;
import om.OmException;
import samples.mu120.lib.graph54.Graph54;
import om.stdcomponent.CanvasComponent;

import samples.shared.Helper;

public class Q4 extends SimpleQuestion2 {

    private static final double tolerance = 1E-6;

    // Graph data

    int [] xFromTo = {-2, 2};
    int [] yFromTo = {-2, 2};
    String [] xLabels = {"-2", "-1", "0", "1", "2"};
    String [] yLabels = {"-2", "-1", "0", "1", "2"};
    String [] pLabels = {"A", "B", "C", "D", "E", "F", "G", "H"};
    int [] pXs = {1, 2, -1, -2, 2, 1, -1, -2};
    int [] pYs = {2, 1, 2, 1, -1, -2, -2, -1};

    String ABs; int ABx, ABy;
    String CDs; int CDx, CDy;
    String EFs; int EFx, EFy;
    String GHs; int GHx, GHy;


    protected void init() throws OmException {

        Random r = getRandom();
        int ab = r.nextInt(2);
        int cd = 2+r.nextInt(2);
        int ef = 4+r.nextInt(2);
        int gh = 6+r.nextInt(2);

        ABs = pLabels [ab]; ABx = pXs [ab]; ABy = pYs [ab];
        CDs = pLabels [cd]; CDx = pXs [cd]; CDy = pYs [cd];
        EFs = pLabels [ef]; EFx = pXs [ef]; EFy = pYs [ef];
        GHs = pLabels [gh]; GHx = pXs [gh]; GHy = pYs [gh];

        setPlaceholder("ABs", ABs);
        setPlaceholder("CDs", CDs);
        setPlaceholder("EFs", EFs);
        setPlaceholder("GHs", GHs);

        setPlaceholder("ABx", "" + ABx);
        setPlaceholder("CDx", "" + CDx);
        setPlaceholder("EFx", "" + EFx);
        setPlaceholder("GHx", "" + GHx);

        setPlaceholder("ABy", "" + ABy);
        setPlaceholder("CDy", "" + CDy);
        setPlaceholder("EFy", "" + EFy);
        setPlaceholder("GHy", "" + GHy);

        Graph54 g54 = new Graph54(xFromTo, yFromTo, "x", xLabels, "y", yLabels);

        CanvasComponent cc = (CanvasComponent) getComponent("graph1"); // question graph
        Graphics g = cc.getGraphics().create();
        // draw background grid, i.e. the graph paper
        g.setColor(Color.LIGHT_GRAY);
        g54.fit(g, 10, 0, 256, 256);
        for (int j=xFromTo[0]; j<=xFromTo[1]; j++)
            g54.drawLine(g, j, yFromTo[0], j, yFromTo[1]);
        for (int i=yFromTo[0]; i<=yFromTo[1]; i++)
            g54.drawLine(g, xFromTo[0], i, xFromTo[1], i);
        // draw the graph
        g.setColor(Color.BLACK); // was: g.setColor(new Color(115,111,234));
        g54.paint(g, 10, 0, 256, 256);
        // draw the points
        g.setColor(new Color(92,47,138));
        for (int i=0; i<pLabels.length; i++)
            g54.drawPoint(g, pLabels [i], pXs [i], pYs [i]);
        g.dispose();

        cc = (CanvasComponent) getComponent("graph2"); // answer graph
        g = cc.getGraphics().create();
        // draw lines to points refered to
        g.setColor(Color.LIGHT_GRAY);
        g54.drawLine(g, ABx, 0, ABx, ABy); g54.drawLine(g, 0, ABy, ABx, ABy);
        g54.drawLine(g, CDx, 0, CDx, CDy); g54.drawLine(g, 0, CDy, CDx, CDy);
        g54.drawLine(g, EFx, 0, EFx, EFy); g54.drawLine(g, 0, EFy, EFx, EFy);
        g54.drawLine(g, GHx, 0, GHx, GHy); g54.drawLine(g, 0, GHy, GHx, GHy);
        // draw graph
        g.setColor(Color.BLACK); // was: g.setColor(new Color(115,111,234));
        g54.paint(g, 10, 0, 256, 256);
        // draw the points
        g.setColor(new Color(92,47,138));
        for (int i=0; i<pLabels.length; i++)
            g54.drawPoint(g, pLabels [i], pXs [i], pYs [i]);
        g.dispose();

        getComponent("inputbox").setDisplay(true);
        //cc.markChanged();

        // store question information
        getResults().setQuestionLine("find (x,y) coordinates for: " + ABs + ", " + CDs + ", " + EFs + ", " + GHs);
        getResults().setAnswerLine(
                "(" +
                "(" + ABx + "," + ABy + ")" + ", " +
                "(" + CDx + "," + CDy + ")" + ", " +
                "(" + EFx + "," + EFy + ")" + ", " +
                "(" + GHx + "," + GHy + ")" +
                ")"
                );
    }

    protected boolean isRight(int iAttempt) throws OmDeveloperException {

        String rABx = (getEditField("rABx").getValue().trim()); double dABx = Helper.inputNumber(rABx);
        String rABy = (getEditField("rABy").getValue().trim()); double dABy = Helper.inputNumber(rABy);
        String rCDx = (getEditField("rCDx").getValue().trim()); double dCDx = Helper.inputNumber(rCDx);
        String rCDy = (getEditField("rCDy").getValue().trim()); double dCDy = Helper.inputNumber(rCDy);
        String rEFx = (getEditField("rEFx").getValue().trim()); double dEFx = Helper.inputNumber(rEFx);
        String rEFy = (getEditField("rEFy").getValue().trim()); double dEFy = Helper.inputNumber(rEFy);
        String rGHx = (getEditField("rGHx").getValue().trim()); double dGHx = Helper.inputNumber(rGHx);
        String rGHy = (getEditField("rGHy").getValue().trim()); double dGHy = Helper.inputNumber(rGHy);

        // store response information
        getResults().appendActionSummary("Attempt " + iAttempt + ": " +
                "(" +
                "(" + rABx + "," + rABy + ")" + ", " +
                "(" + rCDx + "," + rCDy + ")" + ", " +
                "(" + rEFx + "," + rEFy + ")" + ", " +
                "(" + rGHx + "," + rGHy + ")" +
                ")"
                );

        // correct answer ?
        if (
                (Helper.range((double) ABx, dABx, tolerance) && Helper.range((double) ABy, dABy, tolerance)) &&
                (Helper.range((double) CDx, dCDx, tolerance) && Helper.range((double) CDy, dCDy, tolerance)) &&
                (Helper.range((double) EFx, dEFx, tolerance) && Helper.range((double) EFy, dEFy, tolerance)) &&
                (Helper.range((double) GHx, dGHx, tolerance) && Helper.range((double) GHy, dGHy, tolerance))
                )
            return true;

        // wrong answer handled from here on .....

        if (
                (Double.isNaN(dABx) || Double.isNaN(dABy)) ||
                (Double.isNaN(dCDx) || Double.isNaN(dCDy)) ||
                (Double.isNaN(dEFx) || Double.isNaN(dEFy)) ||
                (Double.isNaN(dGHx) || Double.isNaN(dGHy))
                )
            getComponent("nonumber").setDisplay(true);

        int correct = 0;
        if (Helper.range((double) ABx, dABx, tolerance) && Helper.range((double) ABy, dABy, tolerance)) {
            getComponent("right0").setDisplay(true);
            getComponent("wrong0").setDisplay(false);
            correct++;
        } else {
            getComponent("right0").setDisplay(false);
            getComponent("wrong0").setDisplay(true);
        }
        if (Helper.range((double) CDx, dCDx, tolerance) && Helper.range((double) CDy, dCDy, tolerance)) {
            getComponent("right1").setDisplay(true);
            getComponent("wrong1").setDisplay(false);
            correct++;
        } else {
            getComponent("right1").setDisplay(false);
            getComponent("wrong1").setDisplay(true);
        }
        if (Helper.range((double) EFx, dEFx, tolerance) && Helper.range((double) EFy, dEFy, tolerance)) {
            getComponent("right2").setDisplay(true);
            getComponent("wrong2").setDisplay(false);
            correct++;
        } else {
            getComponent("right2").setDisplay(false);
            getComponent("wrong2").setDisplay(true);
        }
        if (Helper.range((double) GHx, dGHx, tolerance) && Helper.range((double) GHy, dGHy, tolerance)) {
            getComponent("wrong3").setDisplay(false);
            getComponent("right3").setDisplay(true);
            correct++;
        } else {
            getComponent("right3").setDisplay(false);
            getComponent("wrong3").setDisplay(true);
        }
        if (correct < 4) {
            setPlaceholder("CORRECT", "" + correct);
            getComponent("correct").setDisplay(true);
        }

        int switched = 0;
        if (Helper.range((double) ABx, dABy, tolerance) && Helper.range((double) ABy, dABx, tolerance)) switched++;
        if (Helper.range((double) CDx, dCDy, tolerance) && Helper.range((double) CDy, dCDx, tolerance)) switched++;
        if (Helper.range((double) EFx, dEFy, tolerance) && Helper.range((double) EFy, dEFx, tolerance)) switched++;
        if (Helper.range((double) GHx, dGHy, tolerance) && Helper.range((double) GHy, dGHx, tolerance)) switched++;
        if (switched > 0) {
            setPlaceholder("SWITCHED", "" + switched);
            getComponent("switched").setDisplay(true);
        }

        // feeback, given attempt 1, 2, or 3
        switch (iAttempt) {
            case 1:
                break;
            case 2:
                setFeedbackID("feedback2");
                break;
            case 3:
                break;
        }

        return false;
    }

    public void actionOK() throws OmException {
        for (int i=0; i<4; i++) {
            getComponent("wrong"+i).setDisplay(false);
            getComponent("right"+i).setDisplay(false);
        }
        super.actionOK();
    }

}