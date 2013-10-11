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
package samples.misc.interactions;

import java.awt.*;
import java.awt.Color;
import java.awt.geom.*;

import om.*;
import om.OmDeveloperException;
import om.helper.SimpleQuestion1;
import om.stdcomponent.CanvasComponent;

/** Reactions Q5d: tests students can balance a chemical equation */
public class EquationBalancing extends SimpleQuestion1
{
	CanvasComponent cc;

	private static final int BALANCED = 0;
	private static final int LEFT = 1;
	private static final int RIGHT = 2;

	//seesaw dimensions
	private final int w = 70;
	private final int h = 16;

	//equation coefficients which appear to the left of each compound
	private int n1=1, n2=1, n3=1, n4=1;

	//numbers of C, H and O atoms displayed in the table
	private int cLeft, cRight, hLeft, hRight, oLeft, oRight;

	private boolean scaled;

	protected void init() throws OmException
	{
		cc=getCanvas("seesaws");

		CalculateAtomNumbers();
		CheckBalancing();

		//set the initial values etc
		setPlaceholder("N1", "\u0010\u0010");
		setPlaceholder("N2", "\u0010\u0010");
		setPlaceholder("N3", "\u0010\u0010");
		setPlaceholder("N4", "\u0010\u0010");
		setPlaceholder("T1", Integer.toString(n1*3));
		setPlaceholder("T2", Integer.toString(n1*8));
		setPlaceholder("T3", Integer.toString(n2*2));
		setPlaceholder("T4", Integer.toString(n3));
		setPlaceholder("T5", Integer.toString(n4*2));
		setPlaceholder("T6", Integer.toString(n3*2+n4));
		getComponent("down1").setEnabled(false);
		getComponent("down2").setEnabled(false);
		getComponent("down3").setEnabled(false);
		getComponent("down4").setEnabled(false);

		getResults().setQuestionLine("Use the + and - buttons to balance this chemical equation.");
	}

	protected boolean isRight(int iAttempt) throws OmDeveloperException
	{
		getComponent("scaled").setDisplay(false);

		String att = Integer.toString(n1) + ", " + Integer.toString(n2) + ", " +
						Integer.toString(n3) + ", " + Integer.toString(n4);

		getResults().setAnswerLine(att);
		getResults().appendActionSummary("Attempt " + iAttempt + ": " + att);

		//equation correctly balanced
		if ((n1==1)&&(n2==5)&&(n3==3)&&(n4==4)) return true;

		//equation balanced but not simplest whole numbers
		if ((cLeft==cRight)&&(hLeft==hRight)&&(oLeft==oRight)) scaled = true;

		if(scaled && iAttempt <= 2) {
			getComponent("scaled").setDisplay(true);
		}
		else if(iAttempt == 2) {
			setFeedbackID("general");
		}
      	return false;
	}

	//the up button increases the number to the left of the compound by 1
	//numbers between 1 and 50 are allowed (1 is not shown explicitly)
	//the number of atoms is recalculated and displayed in the table

	public void actionBalanceUp1() throws OmException
	{
		cc.clear();

		if (n1 < 50) {
			n1++;
			CalculateAtomNumbers();
			setPlaceholder("N1",Integer.toString(n1));
			setPlaceholder("T1", Integer.toString(cLeft));
			setPlaceholder("T2", Integer.toString(hLeft));
		}

		if (n1 == 50) {
			getComponent("up1").setEnabled(false);
		}

		getComponent("down1").setEnabled(true);
		CheckBalancing();
	}

	public void actionBalanceUp2() throws OmException
	{
		cc.clear();

		if (n2 < 50) {
			n2++;
			CalculateAtomNumbers();
			setPlaceholder("N2",Integer.toString(n2));
			setPlaceholder("T3", Integer.toString(oLeft));
		}

		if (n2 == 50) {
			getComponent("up2").setEnabled(false);
		}

		getComponent("down2").setEnabled(true);
		CheckBalancing();
	}

	public void actionBalanceUp3() throws OmException
	{
		cc.clear();

		if (n3 < 50) {
			n3++;
			CalculateAtomNumbers();
			setPlaceholder("N3",Integer.toString(n3));
			setPlaceholder("T4", Integer.toString(cRight));
			setPlaceholder("T6", Integer.toString(oRight));
		}

		if (n3 == 50) {
			getComponent("up3").setEnabled(false);
		}

		getComponent("down3").setEnabled(true);
		CheckBalancing();
	}

	public void actionBalanceUp4() throws OmException
	{
		cc.clear();

		if (n4 < 50) {
			n4++;
			CalculateAtomNumbers();
			setPlaceholder("N4",Integer.toString(n4));
			setPlaceholder("T5", Integer.toString(hRight));
			setPlaceholder("T6", Integer.toString(oRight));
		}

		if (n4 == 50) {
			getComponent("up4").setEnabled(false);
		}

		getComponent("down4").setEnabled(true);
		CheckBalancing();
	}

	public void actionBalanceDown1() throws OmException
	{
		cc.clear();

		if (n1 > 1) {
			n1--;
			CalculateAtomNumbers();
			setPlaceholder("T1", Integer.toString(cLeft));
			setPlaceholder("T2", Integer.toString(hLeft));

			if (n1 == 1) {
				setPlaceholder("N1", "\u0010\u0010");
				getComponent("down1").setEnabled(false);
			}
			else {
				setPlaceholder("N1", Integer.toString(n1));
			}

			getComponent("up1").setEnabled(true);
		}

		CheckBalancing();
	}

	public void actionBalanceDown2() throws OmException
	{
		cc.clear();

		if (n2 > 1) {
			n2--;
			CalculateAtomNumbers();
			setPlaceholder("T3", Integer.toString(oLeft)); //Integer.toString(n2));

			if (n2 == 1) {
				setPlaceholder("N2", "\u0010\u0010");
				getComponent("down2").setEnabled(false);
			}
			else {
				setPlaceholder("N2", Integer.toString(n2));
			}

			getComponent("up2").setEnabled(true);
		}
		CheckBalancing();
	}

	public void actionBalanceDown3() throws OmException
	{
		cc.clear();

		if (n3 > 1) {
			n3--;
			CalculateAtomNumbers();
			setPlaceholder("T4",Integer.toString(cRight));
			setPlaceholder("T6",Integer.toString(oRight));

			if (n3 == 1) {
				setPlaceholder("N3", "\u0010\u0010");
				getComponent("down3").setEnabled(false);
			}
			else {
				setPlaceholder("N3", Integer.toString(n3));
			}

			getComponent("up3").setEnabled(true);
		}
		CheckBalancing();
	}

	public void actionBalanceDown4() throws OmException
	{
		cc.clear();

		if (n4 > 1) {
			n4--;
			CalculateAtomNumbers();
			setPlaceholder("T5", Integer.toString(hRight));
			setPlaceholder("T6", Integer.toString(oRight));

			if (n4 == 1) {
				setPlaceholder("N4", "\u0010\u0010");
				getComponent("down4").setEnabled(false);
			}
			else {
				setPlaceholder("N4", Integer.toString(n4));
			}

			getComponent("up4").setEnabled(true);
		}
		CheckBalancing();
	}

	public void CalculateAtomNumbers() throws OmException
	{
		//calculate the number of each type of atom on each side of the equation
		//depends on the current coefficient and the number of atoms in a compound

		cLeft = n1*3;
		hLeft = n1*8;
		oLeft = n2*2;
		cRight = n3;
		hRight = n4*2;
		oRight = (n3*2) + n4;
	}

	public void CheckBalancing() throws OmException
	{
		//checks the balance of atoms on each side of the equation
		//draws a see-saw left-heavy, right-heavy or level
		//when all the see-saws are level the equation is balanced

		if (cLeft == cRight)
			DrawSeeSaw(0,BALANCED);
		else if(cLeft < cRight)
			DrawSeeSaw(0,RIGHT);
		else if(cLeft > cRight)
			DrawSeeSaw(0,LEFT);

		if (hLeft == hRight)
			DrawSeeSaw(1,BALANCED);
		else if(hLeft < hRight)
			DrawSeeSaw(1,RIGHT);
		else if(hLeft > hRight)
			DrawSeeSaw(1,LEFT);

		if (oLeft == oRight)
			DrawSeeSaw(2,BALANCED);
		else if(oLeft < oRight)
			DrawSeeSaw(2,RIGHT);
		else if(oLeft > oRight)
			DrawSeeSaw(2,LEFT);
	}

	private void DrawSeeSaw(int whichSeesaw, final int state) throws OmException
	{
		Graphics2D g = cc.getGraphics();

		//adjust position to line up with table rows
		int offset = whichSeesaw * 20 + 20 + whichSeesaw;

		//need to change this to use the current text colour or something
		g.setColor(Color.black);

		//draw the line
		switch(state) {
			case BALANCED:
				g.drawLine(0,offset+h/2,w,offset+h/2);
				break;
			case LEFT:
				g.drawLine(0,offset+h-1,w,offset);
				break;
			case RIGHT:
				g.drawLine(0,offset,w,offset+h-1);
		}

	  //draw the filled triangle
	  int xPoints[] = {w/2, w/2-6, w/2+6}; //height = 10px, base = 12px
	  int yPoints[] = {offset+h/2, offset+h, offset+h};

	  //draw the filled triangle
		//top point is half way along the straight line, height = 8px, base = 12px

	  GeneralPath filledPolygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xPoints.length);
	  filledPolygon.moveTo(xPoints[0], yPoints[0]);

	  for(int index = 1; index<xPoints.length; index++)
	  {
	  	filledPolygon.lineTo(xPoints[index], yPoints[index]);
	  };

	  filledPolygon.closePath();
	  g.setPaint(Color.black);
	  g.fill(filledPolygon);

	  //update the canvas
		cc.markChanged();
	}
}
