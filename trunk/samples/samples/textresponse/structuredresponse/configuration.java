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
package samples.textresponse.structuredresponse;

import java.util.Random;

import om.OmDeveloperException;
import om.OmException;
import om.helper.SimpleQuestion1;

import samples.shared.Helper;

///////////////////////////////////////////////////////////////////////////////////////////////

public class configuration extends SimpleQuestion1
{
	/** question variants */
	private final static String[]	ELEMENTS = {"lithium",
												"beryllium",
												"boron",
												"carbon",
												"nitrogen",
												"oxygen",
												"fluorine",
												"neon"};

	private final static String[]	SUBSHELLS = {"1s<sup>",
												 "2s<sup>",
												 "2p<sup>",
												 "3s<sup>",
												 "3p<sup>",
												 "4s<sup>",
												 "4p<sup>",
												 "3d<sup>"};

		/** Selected question */
	private int iVariant;
	private	int	nElectrons, onesElectrons, twosElectrons, twopElectrons;
	private int[] populations={0,0,0,0,0,0,0,0};
							// allow for 8 sub-shells: 1s, 2s, 2p, 3s, 3p, 4s, 4p, 3d

//--------------------------------------------------------------------------------------------

protected void init() throws OmException
{
	int totalElectrons;
	Random r = getRandom();
	iVariant=r.nextInt(ELEMENTS.length);

	nElectrons = iVariant + 3;
	totalElectrons = nElectrons;
	onesElectrons = 2;
	totalElectrons = totalElectrons - 2;	// remove 1s
	if (totalElectrons > 2)
		twosElectrons = 2;
	else
		twosElectrons = totalElectrons;
	totalElectrons = totalElectrons - 2;	// remove 2s
	if (totalElectrons > 0)
		twopElectrons = totalElectrons;

	setPlaceholder("ELEMENT", ELEMENTS[iVariant]);
	setPlaceholder("NUMBEROFELECTRONS","" + nElectrons);
	setPlaceholder("TWOSELECTRONS","" + twosElectrons);
	setPlaceholder("TWOPELECTRONS","" + twopElectrons);

	getComponent("ptplain").setDisplay(true);
	getComponent("ptoverlay").setDisplay(false);

    getResults().setQuestionLine("What is the configuration of " + ELEMENTS[iVariant] + "?");
}
//--------------------------------------------------------------------------------------------

protected boolean isRight(int iAttempt) throws OmDeveloperException
{
	boolean	sentMsg = false, sentFewMsg = false;
	int		i, index1, index2, sumPopulations = 0;
	String	respon, extract;
	String  rightAsFarAs = "";

	getComponent("formtogive").setDisplay(false);
	getComponent("toomany").setDisplay(false);
	getComponent("toofew").setDisplay(false);
	getComponent("toomanyin1s").setDisplay(false);
	getComponent("toofewin1s").setDisplay(false);
	getComponent("toomanyin2s").setDisplay(false);
	getComponent("toofewin2s").setDisplay(false);
	getComponent("toomanyin2p").setDisplay(false);
	getComponent("toofewin2p").setDisplay(false);
	getComponent("okasfaras").setDisplay(false);

	respon=(getAdvancedField("response").getValue().trim());
	// don't worry about case
	respon = respon.toLowerCase();
	// remove all white space
	respon = Helper.removeWhitespace(respon);

	// store response information
	getResults().setAnswerLine(respon);
    getResults().appendActionSummary("Attempt " + iAttempt + ": " + respon);

	// ensure all messages are off such that there are no hang-overs from previous attempts
    getComponent("formtogive").setDisplay(false);

    if ((iAttempt == 1) && (respon.length() == 0)) {
    	getComponent("formtogive").setDisplay(true);
		return(false);
    }

    // extract the number of electrons for each sub-shell
    for (i = 0; i <= 7; ++i) {
    	index1 = respon.indexOf(SUBSHELLS[i]);
    	if (index1 > -1) {
    		index1 += 7;
    		index2 = respon.indexOf("<", index1);
    		extract = respon.substring(index1, index2);
    		try {
    			populations[i] = Integer.parseInt(extract);
    		}
    		catch(NumberFormatException nfe) { // failure
    			populations[i] = 0;
    		}
    	}
    	else {
    		populations[i] = 0;
    	}
   	}
    setPlaceholder("EXTRACT1S", "" + populations[0]);
    setPlaceholder("EXTRACT2S", "" + populations[1]);
    setPlaceholder("EXTRACT2P", "" + populations[2]);

    //Compare it against the right answer
    for (i = 0; i <= 7; ++ i)
    	sumPopulations += populations[i];

    if ((sumPopulations == nElectrons) &&
    	(populations[0] == onesElectrons) &&
       	(populations[1] == twosElectrons) &&
    	(populations[2] == twopElectrons)) {
    	getComponent("ptplain").setDisplay(false);
		getComponent("ptoverlay").setDisplay(true);
    	if (twopElectrons > 0)
    		getComponent("with2p").setDisplay(true);
		return(true);
    }

    if (iAttempt < 3) {
    	if (populations[0] == onesElectrons) {
    		rightAsFarAs = rightAsFarAs + " 1s^2";
    		sentMsg = true;
    	}
    	else if (populations[0] > 2) {
    		getComponent("toomanyin1s").setDisplay(true);
    		sentMsg = true;
    	}
    	else if (populations[0] < 2) {
    		getComponent("toofewin1s").setDisplay(true);
    		sentFewMsg = true;
    	}

     	if (populations[1] == twosElectrons) {
    		rightAsFarAs = rightAsFarAs + " 2s^" + twosElectrons;
    		sentMsg = true;
    	}
     	else if (populations[1] > 2) {
    		getComponent("toomanyin2s").setDisplay(true);
    		sentMsg = true;
    	}
     	else if ((populations[1] > 0) && (populations[1] < twosElectrons)) {
    		getComponent("toofewin2s").setDisplay(true);
    		sentFewMsg = true;
    	}

     	if ((twopElectrons > 0) && (populations[2] == twopElectrons)) {
    		rightAsFarAs = rightAsFarAs + " 2p^" + twopElectrons;
    		sentMsg = true;
    	}
     	else if (populations[2] > 6) {
    		getComponent("toomanyin2p").setDisplay(true);
    		sentMsg = true;
    	}
     	else if ((populations[2] > 0) && (populations[2] < twopElectrons)) {
    		getComponent("toofewin2p").setDisplay(true);
    		sentFewMsg = true;
    	}

     	if (sumPopulations > nElectrons) {
    		getComponent("toomany").setDisplay(true);
    		sentMsg = true;
    	}
    	if (sumPopulations < nElectrons) {
    		if (!sentFewMsg) {
    			getComponent("toofew").setDisplay(true);
    			sentMsg = true;
    		}
    	}

    	if (rightAsFarAs.length() > 0) {
           	getComponent("okasfaras").setDisplay(true);
            setPlaceholder("ASFARAS", rightAsFarAs);
    	}

    }

    switch (iAttempt) {
    	case 1:	if ((sumPopulations == 0) || !(sentMsg || sentFewMsg))
    				getComponent("formtogive").setDisplay(true);
    			break;
    	case 2:	getComponent("ptplain").setDisplay(false);
				getComponent("ptoverlay").setDisplay(true);
				setFeedbackID("default");
				if (!sentMsg) getComponent("formtogive").setDisplay(true);
				break;
    	case 3: if (twopElectrons > 0) getComponent("with2p").setDisplay(true);
    			break;
    }

	return(false);
}
//--------------------------------------------------------------------------------------------

}	// class ends
///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
