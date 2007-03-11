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
package samples.misc.equations;

import java.util.Random;

import om.OmDeveloperException;
import om.OmException;
import om.helper.SimpleQuestion1;

import samples.shared.Helper;

///////////////////////////////////////////////////////////////////////////////////////////////

public class q1 extends SimpleQuestion1
{
	private final static double[]	CORRECT_N   = { 3,  3,  1,   9, 16},
									CORRECT_D	= { 4,  4,  4,  25, 25},
									INCORRECT_1	= {1, 1, 1, 1, 1},
									INCORRECT_LN = { 1,    1,    3,   16,  9},
									INCORRECT_LD = { 4,    4,    4,   25, 25},
									INCORRECT_HN = { 1.73, 1.73, 1,    3,  4},
									INCORRECT_HD = { 2,    2,    2,    5,  5},
									INCORRECT_JN = { 1,    1,    1.73, 4,  3},
									INCORRECT_JD = { 2,    2,    2,    5,  5};
	
	private static final String an[] = {"1", "1", "", "4", ""};
	private static final String ad[] = {"2", "2", "", "5", ""};
	private static final String bn[] = {"", "", "1", "", "3"};
	private static final String bd[] = {"", "", "2", "", "5"};
	private static final String cn[] = {"\\sqrt{3}", "", "", "3", ""};
	private static final String cd[] = {"2", "", "", "5", ""};
	private static final String dn[] = {"", "\\sqrt{3}", "\\sqrt{3}", "", "4"};
	private static final String dd[] = {"", "2", "2", "", "5"};
	private static final String e2n[] = {"\\sqrt{3}", "\\sqrt{3}", "1", "3", "4"};
	private static final String e2d[] = {"2", "2", "2", "5", "5"};
	private static final String LETTERS_E[] = {"2", "3", "", "2", "3"};
	private static final String LETTERS_F[] = {"5", "7", "3", "5", "7"};
	private static final String LETTERS_EE[] = {"2", "3", "1", "2", "3"};
	private static final String LETTERS_G[] = {"c_2", "c_3", "c_1", "c_2", "c_3"};

	private static String equation;
	private static String cStr;
	private static String e2Str;

	/** Selected question */
	private int iVariant;
	
//--------------------------------------------------------------------------------------------
	
protected void init() throws OmException
{
	Random r = getRandom("eigen");
	iVariant=r.nextInt(5);

	equation = "\\Psi (x,t) = ";
	if (an[iVariant] != "") {
		equation += "\\frac{" + an[iVariant] + "}{" + ad[iVariant] + "}"
				+ "\\psi _0 (x) exp\\left( \\frac{-i\\omega _{0}t}{2}\\right) + ";
	}
	if (bn[iVariant] != "") {
		equation += "\\frac{" + bn[iVariant] + "}{" + bd[iVariant] + "}"
				+ "\\psi _0 (x) exp\\left( \\frac{-3i\\omega _{0}t}{2}\\right) + ";
	}
	if (cn[iVariant] != "") {
		equation += "\\frac{" + cn[iVariant] + "}{" + cd[iVariant] + "}"
				+ "\\psi _3 (x) exp\\left( \\frac{-5i\\omega _{0}t}{2}\\right)";
	}
	if (dn[iVariant] != "") {
		equation += "\\frac{" + dn[iVariant] + "}{" + dd[iVariant] + "}"
				+ "\\psi _3 (x) exp\\left( \\frac{-7i\\omega _{0}t}{2}\\right)";
	}

	if (cn[iVariant] == "")
		cStr = "0";
	else
		cStr = "\\frac{" + cn[iVariant] +" }{" + cd[iVariant] + "}";
	
	e2Str = "\\frac{" + e2n[iVariant] +" }{" + e2d[iVariant] + "}";
	
	setPlaceholder("EQ", equation);
	setPlaceholder("C", cStr);
	setPlaceholder("E", LETTERS_E[iVariant]);
	setPlaceholder("EE", LETTERS_EE[iVariant]);
	setPlaceholder("E2", e2Str);
	setPlaceholder("F", LETTERS_F[iVariant]);
	setPlaceholder("G", LETTERS_G[iVariant]);
	setPlaceholder("N", "" + (int) CORRECT_N[iVariant]);
	setPlaceholder("D", "" + (int) CORRECT_D[iVariant]);
	
	// store question information
    getResults().setQuestionLine("Variant (counting from zero): " + iVariant);	
}
//--------------------------------------------------------------------------------------------
  
protected boolean isRight(int iAttempt) throws OmDeveloperException
{
	double	dblN, dblD;
	double	targetN, targetD;
	double	tolerance = 1E-6, lowerTolerance = 0.01;
	String	responN, responD;

	getComponent("wrongfunction").setDisplay(false);
	getComponent("coefficient").setDisplay(false);
	getComponent("total").setDisplay(false);

	responN=(getEditField("numerator").getValue().trim());	
	dblN = Helper.inputNumber(responN); 

	responD=(getEditField("denominator").getValue().trim());	
	dblD = Helper.inputNumber(responD);

	// store response information
	getResults().setAnswerLine(responN + "/" + responD);
    getResults().appendActionSummary("Attempt " + iAttempt + ": " + responN + "/" + responD);
    
    targetN = CORRECT_N[iVariant];
	targetD = CORRECT_D[iVariant];
	
	// Compare against the right answer
	if ((Helper.range(dblN, targetN, tolerance)) &&
		(Helper.range(dblD, targetD, tolerance))) {
		return true;
	}

	if (iAttempt < 3) {
		// Compare against the total probability
		targetN = INCORRECT_1[iVariant];
		targetD = INCORRECT_1[iVariant];
	
		if ((Helper.range(dblN, targetN, tolerance)) &&
				(Helper.range(dblD, targetD, tolerance))) {
				getComponent("total").setDisplay(true);
			}
		
		// Compare against coefficient
		targetN = INCORRECT_HN[iVariant];
		targetD = INCORRECT_HD[iVariant];
	
		if ((Helper.range(dblN, targetN, lowerTolerance)) &&
			(Helper.range(dblD, targetD, lowerTolerance))) {
			getComponent("coefficient").setDisplay(true);
		}

		// Compare against other coefficient
		targetN = INCORRECT_JN[iVariant];
		targetD = INCORRECT_JD[iVariant];
	
		if ((Helper.range(dblN, targetN, lowerTolerance)) &&
			(Helper.range(dblD, targetD, lowerTolerance))) {
			getComponent("coefficient").setDisplay(true);
		}
		
		// Compare against the wrong eigenfunction
		targetN = (double) INCORRECT_LN[iVariant];
		targetD = (double) INCORRECT_LD[iVariant];
	
		if ((Helper.range(dblN, targetN, tolerance)) &&
			(Helper.range(dblD, targetD, tolerance))) {
			getComponent("wrongfunction").setDisplay(true);
		}
	
		// Give hint on second attempt only
		else if (iAttempt == 2) {
			setFeedbackID("default");
		}
	}
	
	return false;
}
//--------------------------------------------------------------------------------------------

}	// class ends
///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
