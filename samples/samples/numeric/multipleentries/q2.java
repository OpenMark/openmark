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
/** MU120 Module 1 question02 */
package samples.numeric.multipleentries;

import java.util.Random;

import om.OmDeveloperException;
import om.OmException;
import om.helper.SimpleQuestion1;

import samples.shared.Helper;

///////////////////////////////////////////////////////////////////////////////////////////////

public class q2 extends SimpleQuestion1
{
	/** Numbers for question variants */
	private final static int[]	NUMBERS_A={ 15,  16,  18,  20,  22},
								NUMBERS_B={ 5,  2,  3,  5, 11},
								NUMBERS_C={ 8,  3,  4,  6, 12};

	/** Selected question */
	private int iVariant;

//--------------------------------------------------------------------------------------------

protected void init() throws OmException
{
	Random r = getRandom();
	iVariant=r.nextInt(NUMBERS_A.length);

	switch (iVariant) {
		case 0: getComponent("votes15").setDisplay(true);
				break;
		case 1: getComponent("votes16").setDisplay(true);
				break;
		case 2: getComponent("votes18").setDisplay(true);
				break;
		case 3: getComponent("votes20").setDisplay(true);
				break;
		case 4: getComponent("votes22").setDisplay(true);
				break;
	}
	setPlaceholder("SHADES",""+NUMBERS_A[iVariant]);
	setPlaceholder("B",""+NUMBERS_B[iVariant]);
	setPlaceholder("C",""+NUMBERS_C[iVariant]);

	// store question information
    getResults().setQuestionLine("What fraction voted? [" + NUMBERS_B[iVariant] + "/" + NUMBERS_C[iVariant] + "]");
}
//--------------------------------------------------------------------------------------------

protected boolean isRight(int iAttempt) throws OmDeveloperException
{
	double	dblN, dblD;
	double	targetN, targetD;
	double	tolerance = 1E-6;
	String	responN, responD;

	getComponent("notsimplest").setDisplay(false);
	getComponent("toosmall").setDisplay(false);
	getComponent("toolarge").setDisplay(false);
	getComponent("reference").setDisplay(false);

	responN=(getEditField("numerator").getValue().trim());
	dblN = Helper.inputNumber(responN);

	responD=(getEditField("denominator").getValue().trim());
	dblD = Helper.inputNumber(responD);

	// store response information
	getResults().setAnswerLine(responN + "/" + responD);
    getResults().appendActionSummary("Attempt " + iAttempt + ": " + responN + "/" + responD);

    targetN = (double) NUMBERS_B[iVariant];
	targetD = (double) NUMBERS_C[iVariant];

	// Compare against the right answer
	if ((Helper.range(dblN, targetN, tolerance)) &&
		(Helper.range(dblD, targetD, tolerance)))
			return true;

	// Compare against the not simplified answer
	if ((Helper.range(dblN, NUMBERS_A[iVariant], tolerance)) &&
		(Helper.range(dblD, 24, tolerance))) {
		getComponent("notsimplest").setDisplay(true);
	}
	else if ((dblN/dblD) < (targetN/targetD)) {
		getComponent("toosmall").setDisplay(true);
	}
	else if ((dblN/dblD) > (targetN/targetD)) {
		getComponent("toolarge").setDisplay(true);
	}

	// Give hint on second attempt only
	if (iAttempt == 2) {
			setFeedbackID("default");
	}

	if (iAttempt == 3)
		getComponent("reference").setDisplay(true);

	return false;
}
//--------------------------------------------------------------------------------------------

}	// class ends
///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
