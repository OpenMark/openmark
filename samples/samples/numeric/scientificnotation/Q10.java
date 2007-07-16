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
/** S151 Chapter 3 Question 10
 	P G Butcher June 2005
 	from the original by Spencer Harben of June 2002
 */
package samples.numeric.scientificnotation;

import java.util.Random;

import om.OmDeveloperException;
import om.OmException;
import om.helper.SimpleQuestion1;

import samples.shared.NumberChecker;
import samples.shared.Helper;

///////////////////////////////////////////////////////////////////////////////////////////////

	public class Q10 extends SimpleQuestion1
	{
		/** Numbers for question variants */
		private final static String[]	NUMBERS_A = {"36",  "4", "25",  "9", "16"},
										NUMBERS_B = { "6",  "2",  "5",  "3",  "4"},
										NUMBERS_C = {"16", "16", "20", "20", "16"},
										NUMBERS_D = { "6",  "6",  "4",  "4",  "6"},
										NUMBERS_E = { "8",  "8", "10", "10",  "8"},
										NUMBERS_F = { "-1",  "-1", "1", "1",  "-1"};
		private final static double[]	sqrtTest =  { 6.0, 2.0, 5.0, 3.0, 4.0};

		/** Selected question */
		private int iVariant;

	//--------------------------------------------------------------------------------------------

	protected void init() throws OmException
	{
		Random r = getRandom();
		iVariant=r.nextInt(NUMBERS_A.length);

		setPlaceholder("A", NUMBERS_A[iVariant]);
		setPlaceholder("B", NUMBERS_B[iVariant]);
		setPlaceholder("C", NUMBERS_C[iVariant]);
		setPlaceholder("D", NUMBERS_D[iVariant]);
		setPlaceholder("E", NUMBERS_E[iVariant]);
		setPlaceholder("F", NUMBERS_F[iVariant]);

		getResults().setQuestionLine("What is sqrt(" + NUMBERS_A[iVariant] + " � 10^" + NUMBERS_C[iVariant] + ")/(" + NUMBERS_B[iVariant] + " � 10^{�" + NUMBERS_D[iVariant] + " � 10^5} in scientific notation");
	}
	//--------------------------------------------------------------------------------------------

	protected boolean isRight(int iAttempt) throws OmDeveloperException
	{
		double	dbl;
		String	resp;
		String	respon;

		respon=(getAdvancedField("response").getValue().trim());

		// store response information
		getResults().setAnswerLine(respon);
		getResults().appendActionSummary("Attempt " + iAttempt + ": " + respon);

		// pgb addition to replace old sig figs checking routine
		dbl = Helper.inputScientificNumber(respon);
		resp = Helper.scientificNotationToE(respon);
		resp = Helper.extractNumber(resp);

		// Compare it against the right answer
		NumberChecker checker = new NumberChecker(1.0e9, 1, true, null, null, false);

		if (checker.check(respon)) {
			return(true);
		}
		else if ((respon.equals("1000000000")) && (iAttempt < 3))
			setFeedbackID("notscinot");
		else if ((respon.equals("10<sup>9</sup>")) && (iAttempt < 3))
			setFeedbackID("onedigit");
		else if ((Helper.range(dbl, 1.0e9, 1.0e5)) && (!Helper.isScientificNotation(resp)))
			setFeedbackID("onedigit");
		else if ((checker.isTooPrecise) && (iAttempt < 3))	// give on first or second attempt
			setFeedbackID("toomanysigfigs");
		else if (iAttempt == 2) {
			if (respon.equals("1<sup>9</sup>"))
				setFeedbackID("default");
			else if (Helper.rangeButWrongFactorOf10(dbl, 1.0e9, 1.0e5))
				setFeedbackID("calccare");
			else if (Helper.range(dbl, sqrtTest[iVariant]*1.0e9, 1.0e5))
				setFeedbackID("sqrtcare");
			else
				setFeedbackID("default");
		}

		return(false);
	}
	//--------------------------------------------------------------------------------------------

}	// class ends
///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
