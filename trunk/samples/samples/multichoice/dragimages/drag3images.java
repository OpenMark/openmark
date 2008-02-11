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
package samples.multichoice.dragimages;

import om.OmDeveloperException;
import om.helper.SimpleQuestion1;

public class drag3images extends SimpleQuestion1
{
	/** For each dropbox which dragbox is the correct answer */
	private final static String[] RIGHTANSWERS={"d1", "d2", "d3"};

	protected boolean isRight(int iAttempt) throws OmDeveloperException
	{
		getComponent("one").setDisplay(false);
		getComponent("two").setDisplay(false);

		int		i;
		int 	numCorrect = 0;
		String	respon;
		String answerLine = "";

		for (i = 0; i < RIGHTANSWERS.length; i++) {
			respon = getDropBox("a" + (i+1)).getValue();
			answerLine += i + " - ";
			if (respon != "") {
				answerLine += respon;
				if (respon.equals(RIGHTANSWERS[i])) {
					numCorrect++;
				}
				else if (iAttempt == 2) {
					getDropBox("a" + (i+1)).clear();
				}
			}
			else {
				answerLine += "empty";
			}
			if (i < (RIGHTANSWERS.length-1)) {
				answerLine += ", ";
			}
		}

		getResults().setAnswerLine(answerLine);
		getResults().appendActionSummary("Attempt " + iAttempt + ": " + answerLine);

		if (numCorrect == 3)
			return(true);

		if (numCorrect == 1) getComponent("one").setDisplay(true);
		else if (numCorrect == 2) getComponent("two").setDisplay(true);

		if (iAttempt == 2) {
				setFeedbackID("default");
		}

		return(false);
	}
}