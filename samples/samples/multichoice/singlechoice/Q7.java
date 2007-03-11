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
package samples.multichoice.singlechoice;

import java.util.Random;

import om.*;
import om.helper.SimpleQuestion1;

/** Example 4: tests students can reduce simple equations */
public class Q7 extends SimpleQuestion1
{ 
	/** Numbers for question variants */
	private final static String[]	LETTERS_A = {"F",  "F", "W",  "F_\\mbox{e}", "F"};
	// now provide text for use in plain mode
	private final static String[]	LETTERS_APM = {"F",  "F", "W",  "F subscript e", "F"};
	
	private final static String[]	LETTERS_B = {"d",  "r", "R_\\mbox{E}",  "R", "D"};
	private final static String[]	LETTERS_BPM = {"d",  "r", "R subscript E",  "R", "D"};
	
	private final static String[]	LETTERS_C = {"G",  "q_\\mbox{1}", "m",  "Q_\\mbox{1}", "q"};
	private final static String[]	LETTERS_CPM = {"G",  "q subscript 1", "m",  "Q subscript 1", "q"};
	
	private final static String[]	LETTERS_D = {"m_\\mbox{1}",  "q_\\mbox{2}", "M_\\mbox{E}",  "Q_\\mbox{2}", "Q"};
	private final static String[]	LETTERS_DPM = {"m subscript 1",  "q subscript 2", "M subscript E",  "Q subscript 2", "Q"};
	
	private final static String[]	LETTERS_E = {"m_\\mbox{2}",  "\u03B5_\\mbox{0}", "G",  "\u03B5_\\mbox{0}", "\u03B5_\\mbox{0}"};
	private final static String[]	LETTERS_EPM = {"m subscript 2",  "\u03B5 subscript 0", "G",  "\u03B5 subscript 0", "\u03B5 subscript 0"};
	  
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
		setPlaceholder("APM", LETTERS_APM[iVariant]);

		setPlaceholder("B", LETTERS_B[iVariant]);
		setPlaceholder("BPM", LETTERS_BPM[iVariant]);
		
		setPlaceholder("C", LETTERS_C[iVariant]);
		setPlaceholder("CPM", LETTERS_CPM[iVariant]);

		setPlaceholder("D", LETTERS_D[iVariant]);
		setPlaceholder("DPM", LETTERS_DPM[iVariant]);
		
		setPlaceholder("E", LETTERS_E[iVariant]);
		setPlaceholder("EPM", LETTERS_EPM[iVariant]);
	
		switch (iVariant) {
			case 0:	getComponent("formula0").setDisplay(true);
					break;
			case 1:	getComponent("formula1").setDisplay(true);
					break;
			case 2:	getComponent("formula2").setDisplay(true);
					break;
			case 3:	getComponent("formula3").setDisplay(true);
					break;
			case 4:	getComponent("formula4").setDisplay(true);
					break;
		}

		getResults().setQuestionLine("Which plot gives a straight line?");		
	}
	//--------------------------------------------------------------------------------------------
	
	protected boolean isRight(int iAttempt) throws OmDeveloperException
	{
  		int		i, answerBox = -1;
  		
  		for (i = 0; i < 5; ++i) {
  			if (getRadioBox("box"+i).isChecked())
  				answerBox = i;
  		}
  		getResults().setAnswerLine("Selected equation box:" + answerBox);
  		getResults().appendActionSummary("Attempt "+iAttempt+": box"+answerBox);
  	
       	if (answerBox == 3)
       		return(true);
       	else if (iAttempt == 2) {
  			setFeedbackID("answer"+answerBox);
       	}

  		return false;
	}
 	//--------------------------------------------------------------------------------------------

}	// class ends
///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
