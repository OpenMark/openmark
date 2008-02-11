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
package samples.textresponse.simpletextentry;

import om.*;
import om.helper.SimpleQuestion1;

/** Reactions Q7d: tests understanding of equilibrium */
public class EquationEquivalence extends SimpleQuestion1
{

  protected boolean isRight(int iAttempt) throws OmDeveloperException
	{
  	//Get answer text and trim whitespace
    String sAnswer=getEditField("input").getValue().trim();

  	// store response information
    getResults().appendActionSummary("Attempt" + iAttempt + ":" + sAnswer);

    sAnswer = sAnswer.toLowerCase();

    //Compare it against the right answer
    if ((sAnswer.indexOf("heat") != -1) ||
        (sAnswer.indexOf("raise") != -1 && sAnswer.indexOf("temp") != -1) ||
        (sAnswer.indexOf("raising") != -1 && sAnswer.indexOf("temp") != -1) ||
        (sAnswer.indexOf("rise") != -1 && sAnswer.indexOf("temp") != -1) ||
        (sAnswer.indexOf("up") != -1 && sAnswer.indexOf("temp") != -1) ||
        (sAnswer.indexOf("high") != -1 && sAnswer.indexOf("temp") != -1) ||
        (sAnswer.indexOf("incre") != -1 && sAnswer.indexOf("temp") != -1) ||
        (sAnswer.indexOf("hotter") != -1))
      return true;

    //Give feedback on second attempt only
    else if(sAnswer.indexOf("temp") != -1)
    {
      if(iAttempt == 2)
      {
      	//Compare against each possible feedback situation we know
        setFeedbackID("Message1");
      }
    }

      return false;
	}

}
