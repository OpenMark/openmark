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
package samples.mu120.module5.question01;

import java.util.Random;
import samples.mu120.lib.SimpleQuestion2;

import om.OmDeveloperException;
import om.OmException;

import samples.shared.Helper;

public class Q1 extends SimpleQuestion2 {

    private static final double tolerance = 1E-6;

    // Numbers for question variants
    private final static double [] numbers = {0.5, 2.0, 3.0};
    private final static String [] features = {"Patio", "Summer House", "Flowerbed", "Vegetable Plot", "Pond"};
    private final static int [] widths = {4, 3, 2, 3, 2};
    private final static int [] lengths = {7, 5, 7, 10, 3};

    private String Y;
    private int W, L;
    private double X, AnsW, AnsL;

    protected void init() throws OmException {

        Random r = getRandom();
        X = numbers [r.nextInt(numbers.length)];
        int i = r.nextInt(features.length);
        Y = features [i];
        W = widths [i];
        L = lengths [i];
        AnsW = W * X;
        AnsL = L * X;

        setPlaceholder("X", "" + X);
        setPlaceholder("Y", Y);
        setPlaceholder("W", "" + W);
        setPlaceholder("L", "" + L);
        setPlaceholder("AnsW", "" + AnsW);
        setPlaceholder("AnsL", "" + AnsL);

        // store question information
        getResults().setQuestionLine("What is (X*W) " + X + "*" + W + ", (X*L)"	+ X + "*" + L + "?");
    }

    protected boolean isRight(int iAttempt) throws OmDeveloperException {

        String response1 = (getEditField("response1").getValue().trim());
        String response2 = (getEditField("response2").getValue().trim());
        double dbl1 = Helper.inputNumber(response1);
        double dbl2 = Helper.inputNumber(response2);

        // store response information
        getResults().setAnswerLine(" " + dbl1 + " " + dbl2);
        getResults().appendActionSummary("Attempt " + iAttempt + ": " + response1 + ", " + response2);

        // correct answer (can be wrong way round) ?
        if (Helper.range(dbl1, AnsW, tolerance) && Helper.range(dbl2, AnsL, tolerance))
            return true;
        if (Helper.range(dbl1, AnsL, tolerance) && Helper.range(dbl2, AnsW, tolerance))
            return true;

        // wrong answer handled from here on .....

        if (Double.isNaN(dbl1) || Double.isNaN(dbl2))
            getComponent("nonumber").setDisplay(true);
        else { // so we have two numbers
            if (dbl1 > dbl2) { // ensure dbl1 is the width
                double d = dbl1;
                dbl1 = dbl2;
                dbl2 = d;
            }
            if (Helper.range(dbl1, AnsW, tolerance)) { // width Ok here
                if (dbl2 < AnsL)
                    setFeedbackID("wokltoosmall");
                else
                    setFeedbackID("wokltoobig");
            } else if (Helper.range(dbl2, AnsL, tolerance)) { // length Ok here
                if (dbl1 < AnsW)
                    setFeedbackID("lokwtoosmall");
                else
                    setFeedbackID("lokwtoobig");
            } else
                setFeedbackID("allwrong");
        }

        // feeback, given attempt 1, 2, or 3
        switch (iAttempt) {
            case 1:
                break;
            case 2: //if (dbl1 < AnsW)
                setFeedbackID("feedback1");
                break;
            case 3:
                break;
        }

        return false;
    }

}