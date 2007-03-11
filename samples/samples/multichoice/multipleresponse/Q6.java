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
package samples.multichoice.multipleresponse;

import java.util.Random;

import om.*;
import om.helper.SimpleQuestion1;

/** Example 4: tests students can reduce simple equations */
public class Q6 extends SimpleQuestion1
{ 
	/** Numbers for question variants */
	private final static String[]	LETTERS_A = {"m",  "n", "p",  "q", "r",
												 "s",  "t", "u",  "v", "w",
												 "y", "z"};
									
	  
	/** Selected question */
	private int iVariant;
	
	/** For each answer 0-7, whether it's right or not */
	final boolean[] RIGHTANSWERS= {false,true,true,true,false,true,true,false};

//	--------------------------------------------------------------------------------------------
	
protected void init() throws OmException
{
	Random r = getRandom();
	iVariant=r.nextInt(LETTERS_A.length);

	setPlaceholder("A", LETTERS_A[iVariant]);

	getResults().setQuestionLine("Which equations are equivalent to 4" + LETTERS_A[iVariant] + "?");		
}
//--------------------------------------------------------------------------------------------
	
 protected boolean isRight(int iAttempt) throws OmDeveloperException
{
    boolean bRight = true;
  	boolean bChecked;
  	int 	i, iCount = 0, correct = 0;
  	String 	sSummary = "";
  	
	// ensure all messages are off such that there are no hang-overs from previous attempts
  	getComponent("incomplete").setDisplay(false);
  	getComponent("toofew").setDisplay(false);
  	getComponent("toomany").setDisplay(false);
  	getComponent("four").setDisplay(false);
  	getComponent("three").setDisplay(false);
  	getComponent("two").setDisplay(false);
  	getComponent("one").setDisplay(false);
  	
  	getResults().setAnswerLine("(Selected equations)");
  	
  	for (i = 0; i < 8; i++) {
  		bChecked = getCheckbox("box"+i).isChecked();
  		if (bChecked) { 
  			iCount++;
  			if (RIGHTANSWERS[i])
  				++correct;
  		}
  		if (bChecked != RIGHTANSWERS[i]) {
  			bRight=false;
  		}
  		sSummary += bChecked ? "[Yes]" : "[No]";
  	}
    getResults().appendActionSummary("Attempt "+iAttempt+": "+sSummary);
  	
    if (bRight) return true;

    if ((iAttempt < 3) && (iCount < 5))
		getComponent("toofew").setDisplay(true);
    else if ((iAttempt < 3) && (iCount > 5))
		getComponent("toomany").setDisplay(true);
    
    if (correct == 4)		getComponent("four").setDisplay(true);
    else if (correct == 3)	getComponent("three").setDisplay(true);
    else if (correct == 2)	getComponent("two").setDisplay(true);
    else if (correct == 1)	getComponent("one").setDisplay(true);

    if ((iAttempt == 2) && (iCount == 5)) {
  		setFeedbackID("simplify");
  	}
  	else if (iAttempt == 3) {
  		getComponent("incomplete").setDisplay(true);
  	}

  	return false;
}
//--------------------------------------------------------------------------------------------
  
public void actionRedo() throws OmException
{
	int	i;
	
	for (i = 0; i < 8; i++) {
  		getCheckbox("box"+i).setChecked(false);
  	}
}
//--------------------------------------------------------------------------------------------

}	// class ends
///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
