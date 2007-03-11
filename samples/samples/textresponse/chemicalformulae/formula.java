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
package samples.textresponse.chemicalformulae;

import java.util.Random;

import om.OmDeveloperException;
import om.OmException;
import om.helper.SimpleQuestion1;

///////////////////////////////////////////////////////////////////////////////////////////////

public class formula extends SimpleQuestion1
{
	/** question variants */
	private final static String[]	COMPOUNDS = {"lithium nitride",
		 										 "sodium sulfide",
		 										 "magnesium phosphide",
		 										 "aluminium oxide",
		 										 "calcium hydride"};

	private final static String[]	FORMULAE = {"\\mbox{Li}_3\\mbox{N}",
												"\\mbox{Na}_2\\mbox{S}",
												"\\mbox{Mg}_3\\mbox{P}_2",
												"\\mbox{Al}_2\\mbox{O}_3",
												"\\mbox{CaH}_2"};

	private final static String[]	MATCHINGFORMULAE = {"Li<sub>3</sub>N",
														"Na<sub>2</sub>S",
														"Mg<sub>3</sub>P<sub>2</sub>",
														"Al<sub>2</sub>O<sub>3</sub>",
														"CaH<sub>2</sub>"};

	private final static String[]	MATCHINGFORMULAENOSUB = {"Li3N",
															 "Na2S",
															 "Mg3P2",
															 "Al2O3",
															 "CaH2"};

	private final static String[]	LETTERS_ANO = {"Three", "Two", "Three", "Two", "One"};
	private final static String[]	LETTERS_AIONS = {"ions have", "ions have", "ions have", "ions have", "ion has"};
	private final static String[]	LETTERS_ACHARGE = {"+3", "+2", "+6", "+6", "+2"};

	private final static String[]	LETTERS_BNO = {"one", "one", "two", "three", "two"};
	private final static String[]	LETTERS_BIONS = {"ion has", "ion has", "ions have", "ions have", "ions have"};
	private final static String[]	LETTERS_BCHARGE = {"�3", "�2", "�6", "�6", "�2"};
	
	private final static String[]	LETTERS_C = {"Li^{+}", "Na^{+}", "Mg^{2+}", "Al^{3+}", "Ca^{2+}"};
	private final static String[]	LETTERS_CPLAIN = {"Li+", "Na+", "Mg2+", "Al3+", "Ca2+"};
	
	private final static String[]	LETTERS_D = {"N^{3�}", "S^{2�}", "P^{3�}", "O^{2�}", "H^{�}"};
	private final static String[]	LETTERS_DPLAIN = {"N3-", "S2-", "P3-", "O2-", "H-"};

	/** Selected question */
	private int iVariant;
	
//--------------------------------------------------------------------------------------------
	
protected void init() throws OmException
{
	Random r = getRandom("secondcomp");
	iVariant=r.nextInt(COMPOUNDS.length);
	
	setPlaceholder("COMPOUND", COMPOUNDS[iVariant]);
	setPlaceholder("FORMULA", FORMULAE[iVariant]);

	setPlaceholder("ANO", LETTERS_ANO[iVariant]);
	setPlaceholder("AIONS", LETTERS_AIONS[iVariant]);
	setPlaceholder("ACHARGE", LETTERS_ACHARGE[iVariant]);

	setPlaceholder("BNO", LETTERS_BNO[iVariant]);
	setPlaceholder("BIONS", LETTERS_BIONS[iVariant]);
	setPlaceholder("BCHARGE", LETTERS_BCHARGE[iVariant]);

	setPlaceholder("C", LETTERS_C[iVariant]);
	setPlaceholder("CPLAIN", LETTERS_CPLAIN[iVariant]);
	
	setPlaceholder("D", LETTERS_D[iVariant]);
	setPlaceholder("DPLAIN", LETTERS_DPLAIN[iVariant]);
	
    getResults().setQuestionLine("What is the formula of " + COMPOUNDS[iVariant]);		
}
//--------------------------------------------------------------------------------------------
  
protected boolean isRight(int iAttempt) throws OmDeveloperException
{
	String	respon;
	
	respon=(getAdvancedField("response").getValue().trim());
	
	// store response information
	getResults().setAnswerLine(respon);
    getResults().appendActionSummary("Attempt " + iAttempt + ": " + respon);

	// ensure all messages are off such that there are no hang-overs from previous attempts
  	getComponent("wrongcase").setDisplay(false);
  	getComponent("usesub").setDisplay(false);

  	// Compare it against the right answer
    if (respon.equals(MATCHINGFORMULAE[iVariant]))
    	return true;
    else if (respon.equalsIgnoreCase(MATCHINGFORMULAE[iVariant]))
      	getComponent("wrongcase").setDisplay(true);
    else if (respon.equals(MATCHINGFORMULAENOSUB[iVariant]))
	  	getComponent("usesub").setDisplay(true);
    else if (respon.equalsIgnoreCase(MATCHINGFORMULAENOSUB[iVariant])) {
	  	getComponent("wrongcase").setDisplay(true);
	  	getComponent("usesub").setDisplay(true);
    }
    else if (iAttempt == 2) {
		setFeedbackID("default");
	}

	return(false);
}
//--------------------------------------------------------------------------------------------

}	// class ends
///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
