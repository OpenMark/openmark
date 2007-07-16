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
package samples.misc.multipleinputs;

import java.util.Random;

import om.*;
import om.helper.SimpleQuestion1;
import samples.shared.*;

public class EditBoxChoice extends SimpleQuestion1
{
	/** Numbers for different variants */
	private final static String[] MOLECULES = {"SnCl_2"};
	private final static String NS[] = {"3"};
	private final boolean[] RIGHTANSWERS = {false, false, false, true, false};

	private final static String[] GEOMETRIES = {
																						  "Trigonal bipyramid",
	  																					"Octahedron",
	  																					"Square pyramid",
	  																					"Trigonal planar",
	  																					"Square planar"
 	 																					 };

	private final static String[] ANSWERS = {"trigonal planar"};

	private int iVariant;

	protected void init() throws OmException
	{
		Random r = getRandom();
		iVariant = r.nextInt(MOLECULES.length);

		setPlaceholder("MOL", MOLECULES[iVariant]);
		setPlaceholder("N", NS[iVariant]);
		setPlaceholder("ANS", ANSWERS[iVariant]);

		for(int i=0; i<GEOMETRIES.length; i++)
		{
			setPlaceholder("GEOMETRY"+Integer.toString(i+1), GEOMETRIES[i]);
		}

		getResults().setQuestionLine("Sketch a Lewis structure for " + MOLECULES[iVariant] +
																 ". How many repulsion axes are there and how are they arranged?");
	}

	protected boolean isRight(int iAttempt) throws OmDeveloperException
	{
		getComponent("hint1").setDisplay(false);
		getComponent("hint2").setDisplay(false);
		getComponent("nWrong").setDisplay(false);
		getComponent("geometryWrong").setDisplay(false);

		//question requires three inputs: 2 editfields and 1 multiple choice
		boolean bNumberCorrect = false;
		boolean bGeometryCorrect = true;

		boolean bRight = false;

		String sInput = Helper.removeWhitespace(getEditField("input").getValue());

		if(sInput.equals(NS[iVariant])) bNumberCorrect =  true;

		int iCount = 0;
		StringBuffer sbAction = new StringBuffer();
		for(int i=0; i<RIGHTANSWERS.length; i++)
		{
			boolean bChecked = getRadioBox("box"+i).isChecked();
			if(bChecked)
			{
				iCount++;
				sbAction.append("Box " + i + " (" + GEOMETRIES[i] + ");");
			}
			if(bChecked != RIGHTANSWERS[i])
			{
				bGeometryCorrect = false;
			}
		}

		getResults().setAnswerLine(sInput + ", " + iCount + " selected)");
		getResults().appendActionSummary("Attempt " + iAttempt + ": " + sInput
																			+ ", " + sbAction.toString());

		bRight = bNumberCorrect && bGeometryCorrect;

		if(bRight || iAttempt == 3)
		{
			getComponent("ans").setString("filePath", "fig"+iVariant+".png");
		}
		//all correct
		if(bRight) return true;

		//specific feedback for the three inputs
		if(iAttempt < 3)
		{
			if(!bNumberCorrect) getComponent("nWrong").setDisplay(true);
			if(!bGeometryCorrect) getComponent("geometryWrong").setDisplay(true);
		}

		if(iAttempt == 1) getComponent("hint1").setDisplay(true);
		if(iAttempt == 2) getComponent("hint2").setDisplay(true);

		return bRight;
	}

}
