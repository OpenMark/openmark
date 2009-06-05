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
/** S151 PA Chapter 10 Question 1
 	G P Black September 2005
*/

package samples.twodresponse.line;

import java.util.Random;

import om.*;
import om.graph.*;
import om.helper.SimpleQuestion1;
import om.stdcomponent.CanvasComponent;

/** Chapter 10 Question 1: draw a tangent to a curve and estimate the gradient */
public class GraphGradient extends SimpleQuestion1
{
	/** For each answer 0-9, whether it's right or not */
	//all initialised to false, correct answers for each variant set below
	private int numOptions = 10;
	private final boolean[] RIGHTANSWERS = new boolean[numOptions];

	//multiple choice options to show for each variant
	private String[][] Options = {
																{"1","2","3","4","5","6","7","8","9","10"},
								  							{"-15","-7.5","-1.5","-0.75","-0.15","0.15","0.75","1.5","7.5","15"}
								 							 };

	//the x value at which to estimate the gradient for each variant
	private String xVal[] = {"2", "4"};

  /** Selected question */
  private int iVariant;

  protected void init() throws OmException
	{
		Random r = getRandom();
		iVariant = r.nextInt(xVal.length);

		//set correct answers for each variant
		if(iVariant == 0)
			RIGHTANSWERS[3] = true;
		else
			RIGHTANSWERS[8] = true;

		for(int i=0; i<numOptions; i++)
		{
			setPlaceholder("OPTION"+Integer.toString(i),Options[iVariant][i]);
		}

		setPlaceholder("XVAL",xVal[iVariant]);

		switch(iVariant){
    	case 1:
        setPlaceholder("Y1", "90");
      	setPlaceholder("Y2", "30");
       	setPlaceholder("X1", "8.0");
       	setPlaceholder("X2", "0");
       	setPlaceholder("DY", "60");
       	setPlaceholder("DX", "8.0");
       	setPlaceholder("GRAD", "7.5");
       	break;
      case 0:
       	setPlaceholder("Y1", "14");
       	setPlaceholder("Y2", "0");
       	setPlaceholder("X1", "4.0");
       	setPlaceholder("X2", "0.5");
       	setPlaceholder("DY", "14");
       	setPlaceholder("DX", "3.5");
       	setPlaceholder("GRAD", "4.0");
       	break;
    }

		getResults().setQuestionLine("Estimate the gradient of the curve shown in the " +
									 							 "plot at x = " + xVal[iVariant] + ".");

		//set up graph item
		CanvasComponent cc = getCanvas("graph"+Integer.toString(iVariant+1));
		World w = cc.getWorld("w"+Integer.toString(iVariant+1));

		//set up function item
		FunctionItem fi=(FunctionItem)w.getItem("f1");

		//show graph component
		getComponent("graph"+Integer.toString(iVariant+1)).setDisplay(true);

		//set function to plot for each variant
		fi.setFunction(new FunctionItem.Function()
		{
			public double f(double x)
			{
				if(iVariant == 0){
					return Math.pow(x,2.0)+2; //y=x^2 + 2
				}
				else{
					return 30 * Math.pow(x,0.5); //y=30x^0.5
				}
			}
		});
		cc.repaint();
	}

	protected boolean isRight(int iAttempt) throws OmDeveloperException
	{
	  //make sure all messages are turned off
		getComponent("wrongnumber").setDisplay(false);
		getComponent("readValues").setDisplay(false);

		boolean bRight = true;
	  int iCount = 0;
		StringBuffer sbAction=new StringBuffer();
	  for(int i=0; i<numOptions; i++){
  		//count number of checked options
  		boolean bChecked = getRadioBox("box"+i).isChecked();
  		if(bChecked)
  		{
  			iCount++;
  			sbAction.append("Box "+i+";");
	  	}
			if(bChecked != RIGHTANSWERS[i])
				bRight = false;
  	}

	  getResults().setAnswerLine("("+iCount+" selected)");
		getResults().appendActionSummary("Attempt "+iAttempt+": "+sbAction.toString());

		if(bRight)
			return true;

    if(iCount != 1 && iAttempt < 3)
  	{
    	getComponent("wrongnumber").setDisplay(true);
  	}
    if(iAttempt == 2)
		{
      setFeedbackID("default");

      if(iCount == 1){
       	if(getRadioBox("box6").isChecked() || getRadioBox("box0").isChecked()){
       		getComponent("readValues").setDisplay(true);
       	}
      }
		}

		return false;
	}

	protected void doAdditionalAnswerProcessing(
	  	boolean bRight,boolean bWrong,
	    boolean bPass,int iAttempt) throws OmDeveloperException
	{
		if(getComponent("wrongnumber").isDisplayed())
		{
			getComponent("wrong").setDisplay(false);
		}
		//draw the correct tangent on the graph
		//if correct, passed or wrong at third attempts
		if(bRight || bPass || iAttempt == 3)
		{
			CanvasComponent cc = getCanvas("graph"+Integer.toString(iVariant+1));
			World w = cc.getWorld("w"+Integer.toString(iVariant+1));

			try
			{
				LineItem li = new LineItem(w);
				switch(iVariant)
				{
					case 0:
						li.setX(new GraphScalar(0.5,0));
						li.setY(new GraphScalar(0,0));
						li.setX2(new GraphScalar(4,0));
						li.setY2(new GraphScalar(14,0));
						break;
					case 1:
						li.setX(new GraphScalar(0,0));
						li.setY(new GraphScalar(30,0));
						li.setX2(new GraphScalar(8,0));
						li.setY2(new GraphScalar(90,0));
				}
				li.setLineWidth(2);
				li.setLineColour(w.convertColour("graph2"));
				w.add(li);
			}
			catch(GraphFormatException gfe)
			{
				throw new OmFormatException("error creating line item");
			}

			cc.repaint();
		}
	}
}
