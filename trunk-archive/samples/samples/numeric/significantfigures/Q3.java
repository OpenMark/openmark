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
/** S151 Chapter 5 Question 03
 	P G Butcher September 2005
*/
package samples.numeric.significantfigures;

import java.awt.Color;
import java.util.Random;

import om.OmDeveloperException;
import om.OmException;
import om.OmFormatException;
//import om.graph.World;
import om.helper.SimpleQuestion1;

import om.graph.*;
import om.stdcomponent.CanvasComponent;

import samples.shared.RangeChecker;
import samples.shared.Helper;

///////////////////////////////////////////////////////////////////////////////////////////////

public class Q3 extends SimpleQuestion1
{
	private final static double[]	gradient = {0.004375, 0.00375, 0.0056, 0.0025, 0.00625};
	private final static double[]	yIntercept = {1.2, 1.1, 0.95, 1.4, 1.1};
	private final static double[]	answersArray = {440, 380, 560, 250, 630};
	private final static String[]	answersStringArray = {"440", "380", "560", "250", "630"};
	private final static String[]	yAt0 =  {"1.20", "1.10", "0.95", "1.40", "1.10"};
	private final static String[]	yAt80 = {"1.55", "1.40", "1.40", "1.60", "1.60"};
	private final static String[]	rise = {"0.35", "0.30", "0.45", "0.20", "0.50"};
	private final static String[]	gradInSF = {"4.4", "3.8", "5.6", "2.5", "6.3"};

	/** Selected question */
	private	double	answer;
	private int 	iVariant;

	//--------------------------------------------------------------------------------------------

	protected void init() throws OmException
	{
		int i;
		double	x1, x2, y1, y2;

		Random r = getRandom();
		iVariant=r.nextInt(gradient.length);

		answer = answersArray[iVariant];

		setPlaceholder("ZERO", yAt0[iVariant]);
		setPlaceholder("EIGHTY", yAt80[iVariant]);
		setPlaceholder("RISE", rise[iVariant]);
		setPlaceholder("GRADINSF", gradInSF[iVariant]);

		CanvasComponent cc=getCanvas("graph");
		World w=cc.getWorld("w1");

		// draw the main line
		x1 = 0.0;
		x2 = 140.0;
		y1 = yIntercept[iVariant];
		y2 = y1 + 140.0 * gradient[iVariant];
		try {
			LineItem wLine = new LineItem(w);
			wLine.setX(new GraphScalar(x1, 0));
			wLine.setY(new GraphScalar(y1, 0));
			wLine.setX2(new GraphScalar(x2, 0));
			wLine.setY2(new GraphScalar(y2, 0));
//			wLine.setLineColour(Color.black);
			wLine.setLineColour(w.convertColour("fg"));
			w.add(wLine);
		}
		catch(GraphFormatException gfe) {
			throw new OmFormatException("error creating line item");
		}

		// add three vertical and horizontal lines to aid reading main line
		for (i = 1; i < 4; ++ i) {
			x1 = x2 = i * 40.0;
			y1 = yIntercept[iVariant];
			y2 = y1 + x2 * gradient[iVariant];
			y1 = 0.0;
			try {
				LineItem vLine = new LineItem(w);
				vLine.setX(new GraphScalar(x1, 0));
				vLine.setY(new GraphScalar(y1, 0));
				vLine.setX2(new GraphScalar(x2, 0));
				vLine.setY2(new GraphScalar(y2, 0));
				vLine.setLineColour(w.convertColour("graph1"));
//				vLine.setLineColour(Color.red);
				w.add(vLine);

				x1 = 0.0;
				y1 = y2;
				LineItem hLine = new LineItem(w);
				hLine.setX(new GraphScalar(x1, 0));
				hLine.setY(new GraphScalar(y1, 0));
				hLine.setX2(new GraphScalar(x2, 0));
				hLine.setY2(new GraphScalar(y2, 0));
				hLine.setLineColour(w.convertColour("graph1"));
//				hLine.setLineColour(Color.red);
				w.add(hLine);
			}
			catch(GraphFormatException gfe) {
				throw new OmFormatException("error creating line item");
			}
		}

		cc.repaint();

		getResults().setQuestionLine("What is the gradient of a line passing through x=0, y=" + yAt0[iVariant] + " and x=80, y=" + yAt80[iVariant] + "?");
	}
	//--------------------------------------------------------------------------------------------

	protected boolean isRight(int iAttempt) throws OmDeveloperException
	{
		double	dbl;
		String	respon;
		String	resp2;

		// ensure all messages are off such that there are no hang-overs from previous attempts
		getComponent("wrongsfsn").setDisplay(false);
		getComponent("wrongsf").setDisplay(false);
		getComponent("notsn").setDisplay(false);
		getComponent("wrongfactorof10").setDisplay(false);
		getComponent("wrongfactorof10noteylabel").setDisplay(false);

		// trim() removes leading and trailing spaces
		respon=(getAdvancedField("response").getValue().trim());

		// store response information
		getResults().setAnswerLine(respon);
		getResults().appendActionSummary("Attempt " + iAttempt + ": " + respon);

		RangeChecker checker = new RangeChecker(answer, 10.0, 2, true, null, null);
		if (checker.check(respon)) {
			return(true);
		}

		if (checker.numberInRange && (iAttempt < 3)) {
			if (respon.equals(answersStringArray[iVariant])) {
				getComponent("notsn").setDisplay(true);
			}
			else if (!checker.correctSigFigs && !checker.isScientificNotation) {
				getComponent("wrongsfsn").setDisplay(true);
			}
			else if (!checker.isScientificNotation) {
				getComponent("notsn").setDisplay(true);
			}
			else if (!checker.correctSigFigs) {
				getComponent("wrongsf").setDisplay(true);
			}
		}
		else {
			// check for incorrect factor of 10
			resp2 = Helper.scientificNotationToE(respon);
			dbl = Helper.inputNumber(resp2);
			if ((Helper.range(dbl, answer/1e5, 1e-5)) && (iAttempt < 3)) {
				getComponent("wrongfactorof10noteylabel").setDisplay(true);
			}
			else if ((Helper.rangeButWrongFactorOf10(dbl, answer, 10.0)) && (iAttempt < 3)) {
				getComponent("wrongfactorof10").setDisplay(true);
			}
			else if (iAttempt == 2) {
				setFeedbackID("default");
			}
		}

		return(false);
	}
	//--------------------------------------------------------------------------------------------

}	// class ends
///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
