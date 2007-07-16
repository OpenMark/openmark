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
package samples.mu120.module5.question05;

import java.util.Random;
import samples.mu120.lib.SimpleQuestion2;

import om.OmDeveloperException;
import om.OmException;
import om.graph.World;
import om.stdcomponent.CanvasComponent;
import samples.shared.Helper;

public class Q5 extends SimpleQuestion2 {

    // Graph data

    int [] pXs = { 20, 20,  70,  70,   20,  20,  70,  70, -20, -20, -70, -70, -20, -20, -70, -70};
    int [] pYs = { 20, 70,  20,  70,  -20, -70, -20, -70,  20,  70,  20,  70, -20, -70, -20, -70};

    int pXa, pYa;
    int pXb, pYb;
    int pXc, pYc;
    int pXd, pYd;


    protected void init() throws OmException {

        Random r = getRandom();
        int p1 = r.nextInt(4);
        int p2 = 4+r.nextInt(4);
        int p3 = 8+r.nextInt(4);
        int p4 = 12+r.nextInt(4);

        pXa = pXs [p1]; pYa = pYs [p1];
        pXb = pXs [p2]; pYb = pYs [p2];
        pXc = pXs [p3]; pYc = pYs [p3];
        pXd = pXs [p4]; pYd = pYs [p4];

        setPlaceholder("pXA", "" + pXa);
        setPlaceholder("pXB", "" + pXb);
        setPlaceholder("pXC", "" + pXc);
        setPlaceholder("pXD", "" + pXd);

        setPlaceholder("pYA", "" + pYa);
        setPlaceholder("pYB", "" + pYb);
        setPlaceholder("pYC", "" + pYc);
        setPlaceholder("pYD", "" + pYd);

        // store question information
        getResults().setQuestionLine("Drag the following points on the graph at"
                );
        getResults().setAnswerLine(
                "(" +
                "(A, " + pXa + ", " + pYa + ")" + ", " +
                "(B, " + pXb + ", " + pYb + ")" + ", " +
                "(C, " + pXc + ", " + pYc + ")" + ", " +
                "(D, " + pXd + ", " + pYd + ")" +
                ")"
                );
    }

    protected boolean isRight(int iAttempt) throws OmDeveloperException {

        CanvasComponent cc = getCanvas("graph");
        World w = cc.getWorld("w1");

        java.awt.Point mPa = cc.getMarkerPos(0);
        java.awt.Point mPb = cc.getMarkerPos(1);
        java.awt.Point mPc = cc.getMarkerPos(2);
        java.awt.Point mPd = cc.getMarkerPos(3);
        int mXa = (int) Math.round(w.convertXBack(mPa.x));
        int mYa = (int) Math.round(w.convertYBack(mPa.y));
        int mXb = (int) Math.round(w.convertXBack(mPb.x));
        int mYb = (int) Math.round(w.convertYBack(mPb.y));
        int mXc = (int) Math.round(w.convertXBack(mPc.x));
        int mYc = (int) Math.round(w.convertYBack(mPc.y));
        int mXd = (int) Math.round(w.convertXBack(mPd.x));
        int mYd = (int) Math.round(w.convertYBack(mPd.y));

        // correct answer ?
        int correct = 0;
        if (Helper.range(mXa, pXa, 10) && Helper.range(mYa, pYa, 10)) {
            correct++;
        } else {
            setFeedbackID("pointamisplaced");
        }
        if (Helper.range(mXb, pXb, 10) && Helper.range(mYb, pYb, 10)) {
            correct++;
        } else {
            setFeedbackID("pointbmisplaced");
        }
        if (Helper.range(mXc, pXc, 10) && Helper.range(mYc, pYc, 10)) {
            correct++;
        } else {
            setFeedbackID("pointcmisplaced");
        }
        if (Helper.range(mXd, pXd, 10) && Helper.range(mYd, pYd, 10)) {
            correct++;
        } else {
            setFeedbackID("pointdmisplaced");
        }

        // store response information
        getResults().appendActionSummary("Attempt " + iAttempt + ": " +
                "(" +
                "(A, " + mXa + ", " + mYa + ")" + ", " +
                "(B, " + mXb + ", " + mYb + ")" + ", " +
                "(C, " + mXc + ", " + mYc + ")" + ", " +
                "(D, " + mXd + ", " + mYd + ")" +
                ")"
                );

        switch (correct) {
            case 0:
                setPlaceholder("CORRECT", "All four points are misplaced");
                break;
            case 1:
                setPlaceholder("CORRECT", "One point is in the right place");
                break;
            default:
                setPlaceholder("CORRECT", correct + " points are in the right place");
                break;
        }

        if (correct == 4)
            return true;

        // wrong answer handled from here on .....

        // feeback, given attempt 1, 2, or 3
        switch (iAttempt) {
            case 1:
                setFeedbackID("feedback1");
                break;
            case 2:
                setFeedbackID("feedback2");
                break;
            case 3:
                cc.removeMarker(3);
                cc.removeMarker(2);
                cc.removeMarker(1);
                cc.removeMarker(0);
                cc.addMarker(w.convertX(pXa+4), w.convertY(pYa-3), "label='A'; font='bold';", "w1");
                cc.addMarker(w.convertX(pXb+4), w.convertY(pYb-3), "label='B'; font='bold';", "w1");
                cc.addMarker(w.convertX(pXc+4), w.convertY(pYc-3), "label='C'; font='bold';", "w1");
                cc.addMarker(w.convertX(pXd+4), w.convertY(pYd-3), "label='D'; font='bold';", "w1");
                break;
        }

        return false;
    }

} // end class