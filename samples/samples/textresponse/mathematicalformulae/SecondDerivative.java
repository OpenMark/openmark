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
/** S151 PA Chapter 10 Question 6
 	G P Black September 2005
*/

package samples.textresponse.mathematicalformulae;

import java.util.Random;

import om.*;
import om.helper.*;
import samples.shared.Helper;

/** Chapter 2 Question 7: tests students can convert to scientific notation */
public class SecondDerivative extends SimpleQuestion1
{
	/** Numbers for different variants */

	String[] numbersA = {"2", "3", "2", "2", "3"};
	String[] numbersB = {"3", "2", "4", "5", "4"};
	String[] numbersC = {"6", "9", "6", "6", "9"};
	String numberC;
	String[] numbersD = {"6", "4", "8", "10", "8"};
	String numberD;
	String[] numbersE = {"12", "18", "12", "12", "18"};
	String numberE;

	/** Selected question */
	private int iVariant;

	protected void init() throws OmException
	{
		Random r = getRandom();
		iVariant = r.nextInt(numbersA.length);

		numberC = numbersC[iVariant];
		numberD = numbersD[iVariant];
		numberE = numbersE[iVariant];

		String strNumA = numbersA[iVariant];
		String strNumB = numbersB[iVariant];

		setPlaceholder("A",strNumA);
		setPlaceholder("B",strNumB);
		setPlaceholder("C",numbersC[iVariant]);
		setPlaceholder("D",numbersD[iVariant]);
		setPlaceholder("E",numbersE[iVariant]);

		getResults().setQuestionLine("What is the second derivative of " +
									 							 "z = " + strNumA + "t^3 + " + strNumB + "t^2 " +
									 							 "- 2t + 3 with respect to t?");
	}

	protected boolean isRight(int iAttempt) throws OmDeveloperException
	{
		getComponent("wrongDerivative").setDisplay(false);

		String sInput = Helper.removeMultSignsAndWhitespace(getAdvancedField("input").getValue());

		getResults().setAnswerLine(sInput);
		getResults().appendActionSummary("Attempt "+iAttempt+": "+sInput);

		boolean correct = false;

		String answer1 = numberE + "t+" + numberD;
		String answer2 = numberD + "+" + numberE + "t";

		if(sInput.equals(answer1)
			|| sInput.equals(answer1 + "+0")
			|| sInput.equals(answer1 + "-0")
			|| sInput.equals(answer2)
			|| sInput.equals(answer2 + "+0")
			|| sInput.equals(answer2 + "-0")){
			correct = true;
		}

		String[] firstDerivatives = {numberC + "t<sup>2</sup>+" + numberD + "t-2",
									 							 numberC + "t<sup>2</sup>-2+" + numberD + "t",
									 							 numberD + "t+" + numberC + "t<sup>2</sup>-2",
									 							 numberD + "t-2+" + numberC + "t<sup>2</sup>",
									 							 "-2+" + numberD + "t+" + numberC + "t<sup>2</sup>",
									 							 "-2+" + numberC + "t<sup>2</sup>+" + numberD + "t"
																};

		boolean wrongDerivative = false;

		int i = 0;

		while(!wrongDerivative && i < firstDerivatives.length)
		{
			if(sInput.equals(firstDerivatives[i]))
			{
				wrongDerivative = true;
			}
			i++;
		}

		if(iAttempt == 2)
		{
			if(wrongDerivative)
			{
				getComponent("wrongDerivative").setDisplay(true);
			}
			else
			{
				setFeedbackID("default");
			}
		}

		return correct;
	}
}

