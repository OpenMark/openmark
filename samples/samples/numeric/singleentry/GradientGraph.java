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
/** S151 PA Chapter 10 Question 3
 	G P Black September 2005
*/

package samples.numeric.singleentry;

import java.util.Random;

import om.*;
import om.helper.SimpleQuestion1;
import samples.shared.Helper;

/** Chapter 10 Question 3: differentiate to find the gradient of a graph */
public class GradientGraph extends SimpleQuestion1
{
	/** Numbers for question variants */
	String numbersA[] = {"32", "24", "20", "16", "12"};
	String numberA;

	String numbersB[] = {"64", "48", "40", "32", "24"};
	String numberB;

	String answersC[] = {"8", "6", "5", "4", "3"};
	String answerC;

	/** Selected question */
	private int iVariant;
  
	protected void init() throws OmException
	{
		Random r = getRandom();
		iVariant = r.nextInt(numbersA.length);
    
		String strNumA = numbersA[iVariant];
		
		setPlaceholder("NUMA",strNumA);
		setPlaceholder("NUMB",""+numbersB[iVariant]);
		setPlaceholder("ANSWER",""+answersC[iVariant]);

		getResults().setQuestionLine("z = -" + strNumA + " over t^2, what is the gradient " +
									 							 "of a graph of z against t at t = 2?");
	}
  
	protected boolean isRight(int iAttempt) throws OmDeveloperException
	{
		getComponent("wrongSign").setDisplay(false);

		//get answer text and trim whitespace
		String sInput = Helper.removeWhitespace(getEditField("input").getValue());
 
		getResults().setAnswerLine(sInput);
		getResults().appendActionSummary("Attempt "+iAttempt+": "+sInput);

		//compare it against the right answer
		if(sInput.equals(answersC[iVariant]))
			return true;
    
		//give feedback on second attempt only
		if(iAttempt==2) 
		{
			setFeedbackID("default");
			if(sInput.equals("-"+answersC[iVariant]))
			{
				getComponent("wrongSign").setDisplay(true);
			}
		}
    
		return false;
	}
  
}
