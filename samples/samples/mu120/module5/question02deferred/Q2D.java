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
package samples.mu120.module5.question02deferred;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
import om.helper.DeferredFeedbackQuestion1;
import om.OmDeveloperException;
import om.OmException;
import om.stdcomponent.CanvasComponent;
import samples.shared.Helper;

public class Q2D extends DeferredFeedbackQuestion1 {

    private static final double tolerance = 1E-6;

    String T;
    int N;
    double dAns;

    // Numbers for question variants
    private final static String [] transport = {"e-mail", "telephone", "letter"};
    private final static int [] [] numbers = {{3, 5, 6}, {3, 2, 1}, {2, 1, 1}};

    @Override
    protected void init() throws OmException {

        int i, j;
        Random r = getRandom();
        do {
            i = r.nextInt(numbers.length);
            T = transport [i];
            j = r.nextInt(transport.length);
            N = numbers [i][j];
            dAns = 12.5 * N;
        } while (N == 1); // want more than 1 seqment for the question

        setPlaceholder("T", T);
        setPlaceholder("N", "" + N);
        if (N == 1) // don't need this test now
            setPlaceholder("SLICES", "is one slice");
        else
            setPlaceholder("SLICES", "are " + N + " slices");
        setPlaceholder("ANS", "" + dAns);

        int e = numbers[0][j];
        int t = numbers[1][j];
        int l = numbers[2][j];

        // draw pie chart
        CanvasComponent cc = (CanvasComponent) getComponent("piechart");
        Graphics g = cc.getGraphics().create();
        g.setColor(new Color(235,159,187));
        g.fillArc(0, 0, 127, 127, 0, (int) Math.round(e*0.125*360));
        g.setColor(new Color(192,147,238));
        g.fillArc(0, 0, 127, 127, (int) Math.round(e*0.125*360), (int) Math.round(t*0.125*360));
        g.setColor(new Color(230,231,199));
        g.fillArc(0, 0, 127, 127, (int) Math.round((e+t)*0.125*360), (int) Math.round(l*0.125*360));
        g.setColor(new Color(0,0,0));
        g.drawArc(0, 0, 127, 127, 0, 360);
        for (int sg = 0; sg<8; sg++)
            g.fillArc(0, 0, 127, 127, (int) Math.round(sg*0.125*360) - 1, 2);

        g.setFont(new Font("Aerial", Font.PLAIN, 12));

        g.setColor(new Color(235,159,187));
        g.fillRect(148,20,24,24);
        g.setColor(new Color(0,0,0));
        g.drawString("E-mail", 184, 36);

        g.setColor(new Color(192,147,238));
        g.fillRect(148,56,24,24);
        g.setColor(new Color(0,0,0));
        g.drawString("Telephone", 184, 72);

        g.setColor(new Color(230,231,199));
        g.fillRect(148,92,24,24);
        g.setColor(new Color(0,0,0));
        g.drawString("Letter", 184, 108);

        g.dispose();
        getComponent("inputbox").setDisplay(true);

        // store question information
        getResults().setQuestionLine("What is " + N + "/8" + " as a percentage?");
        getResults().setAnswerLine(dAns + "%");
    }

    @Override
    protected boolean isRight() throws OmDeveloperException {

        String response = (getEditField("response").getValue().trim());
        double dbl = Helper.inputNumber(response);

        // store response information
        getResults().appendActionSummary("Response: " + response);

        // correct answer (can be wrong way round) ?
        if (Helper.range(dbl, dAns, tolerance))
            return true;

        // wrong answer handled from here on .....

        if (Double.isNaN(dbl))
            getComponent("nonumber").setDisplay(true);
        else {
            if (dbl < dAns)
                setFeedbackID("toosmall");
            else
                setFeedbackID("toolarge");
        }

        return false;
    }
}
