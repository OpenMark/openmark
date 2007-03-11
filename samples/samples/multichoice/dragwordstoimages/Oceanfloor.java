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
package samples.multichoice.dragwordstoimages;

import om.*;
import om.helper.SimpleQuestion1;

public class Oceanfloor extends SimpleQuestion1
{
  private static String[] order= {"a6", "a7", "a0", "a2", "a5", "a3", "a4", "a1"}; // this will be altered below
  
  protected void init() throws OmException
  {
  }
    
  protected boolean isRight(int iAttempt) throws OmDeveloperException
  {
	String resp0, resp1, resp2, resp3, resp4, resp5, resp6, resp7;
  	int ticks = 0;
	int r0, r1, r2, r3, r4, r5, r6, r7;
	r0 = r1 = r2 = r3 = r4 = r5 = r6 = r7 = 0;
		
	getComponent("oneWrong").setDisplay(false);
	getComponent("manyWrong").setDisplay(false);
	getComponent("plain").setDisplay(false);
	getComponent("rise").setDisplay(false);
	getComponent("shelf").setDisplay(false);
	getComponent("slope").setDisplay(false);
	getComponent("ridge").setDisplay(false);
	getComponent("belt").setDisplay(false);
	getComponent("arc").setDisplay(false);
	getComponent("trench").setDisplay(false);
	getComponent("corrected").setDisplay(false);

	resp0 = getDropBox("d0").getValue();
    resp1 = getDropBox("d1").getValue();
    resp2 = getDropBox("d2").getValue();
    resp3 = getDropBox("d3").getValue();
    resp4 = getDropBox("d4").getValue();
    resp5 = getDropBox("d5").getValue();
    resp6 = getDropBox("d6").getValue();
    resp7 = getDropBox("d7").getValue();

    // store response information
    // getResults().setAnswerLine(respon);
    
    if (resp0.equals(order[0])) {
    	ticks++;
    	r0 = 1;
    }
    else if (iAttempt == 2) {
    	getDropBox("d0").clear();
       	getComponent("belt").setDisplay(true);
    }

    if (resp1.equals(order[1])) {
    	ticks++;
    	r1 = 1;
    }
    else if (iAttempt == 2) {
    	getDropBox("d1").clear();
       	getComponent("shelf").setDisplay(true);
    }

    if (resp2.equals(order[2])) {
    	ticks++;
    	r2 = 1;
    }
    else if (iAttempt == 2) {
    	getDropBox("d2").clear();
       	getComponent("arc").setDisplay(true);
    }

    if (resp3.equals(order[3])) {
    	ticks++;
    	r3 = 1;
    }
    else if (iAttempt == 2) {
    	getDropBox("d3").clear();
       	getComponent("plain").setDisplay(true);
    }

    if (resp4.equals(order[4])) {
    	ticks++;
    	r4 = 1;
    }
    else if (iAttempt == 2) {
    	getDropBox("d4").clear();
       	getComponent("slope").setDisplay(true);
    }

    if (resp5.equals(order[5])) {
    	ticks++;
    	r5 = 1;
    }
    else if (iAttempt == 2) {
    	getDropBox("d5").clear();
       	getComponent("rise").setDisplay(true);
    }

    if (resp6.equals(order[6])) {
    	ticks++;
    	r6 = 1;
    }
    else if (iAttempt == 2) {
    	getDropBox("d6").clear();
       	getComponent("trench").setDisplay(true);
    }

    if (resp7.equals(order[7])) {
    	ticks++;
    	r7 = 1;
    }
    else if (iAttempt == 2) {
    	getDropBox("d7").clear();
       	getComponent("ridge").setDisplay(true);
    }

    // store response information
    getResults().appendActionSummary("Attempt " + iAttempt + ": 1 is right 0 is wrong:"
    		+ r0 + r1 + r2 + r3 + r4 + r5 + r6 + r7);

    if (ticks == 8) {
   	  return true;
   	}
   	
    if (iAttempt == 2) 	{ 
  		setFeedbackID("still");
  	}

    if (iAttempt < 3) {
    	if (ticks == 7) getComponent("oneWrong").setDisplay(true);
    	else getComponent("manyWrong").setDisplay(true);
    }

    setPlaceholder("CROSSES",""+(8-ticks));

    if (iAttempt == 3) {
    	getDropBox("d0").setValue(order[0]);
    	getDropBox("d1").setValue(order[1]);
    	getDropBox("d2").setValue(order[2]);
    	getDropBox("d3").setValue(order[3]);
    	getDropBox("d4").setValue(order[4]);
    	getDropBox("d5").setValue(order[5]);
    	getDropBox("d6").setValue(order[6]);
    	getDropBox("d7").setValue(order[7]);

    	getComponent("corrected").setDisplay(true);
    }

    return false;
  }	
}
