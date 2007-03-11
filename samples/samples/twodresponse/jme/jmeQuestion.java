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
package samples.twodresponse.jme;

import om.*;
import om.helper.SimpleQuestion1;

/** Example of JME usage */
public class jmeQuestion extends SimpleQuestion1
{
	private static String sSmiles;
	
	protected void init() throws OmException
	{
		getResults().setQuestionLine("Draw the structure of the compound that gives the 13C shown.");				
	}
	
	protected boolean isRight(int iAttempt) throws OmDeveloperException
	{
		int oxygenCount, carbonCount, nitrogenCount, tripleCount;
		
		getComponent("nooxygen").setDisplay(false);
		getComponent("rightgroupwrongcarbons").setDisplay(false);
		getComponent("tooManyCarbons").setDisplay(false);
		getComponent("tooFewCarbons").setDisplay(false);
		getComponent("rightoxygens").setDisplay(false);
		getComponent("rightcarbons").setDisplay(false);
		getComponent("notriple").setDisplay(false);
		getComponent("nonitrogen").setDisplay(false);
		getComponent("formula").setDisplay(false);
		
		//debug
		setPlaceholder("SMILES",getJME("myjme").getSMILES());		
		
		//get the SMILES string of the entered structure
		sSmiles = getJME("myjme").getSMILES();
		
		getResults().setAnswerLine(sSmiles);
		getResults().appendActionSummary("Attempt "+iAttempt+": "+sSmiles);

		//convert any wedge bonds into plain bonds
		removeWedgeBonds();
		
		//match against correct answer
		//two possibilities: CN drawn out or entered with the 'X' tool
		if (sSmiles.equals("CCCCCCC=O")) {
			return true;
		}
		
		if (iAttempt < 3) {
			if (sSmiles.equals("CCCCCC=O") || sSmiles.equals("CCCCCCC=O")) {
	        	getComponent("rightgroupwrongcarbons").setDisplay(true);
			}
			else {
				//example of how to count atoms
				//could do similar for other atoms, multiple bonds etc
				oxygenCount = countElement(sSmiles, "O");
				if (oxygenCount < 1) {
					getComponent("nooxygen").setDisplay(true);
				}
				if (oxygenCount == 1) {
					getComponent("rightoxygens").setDisplay(true);
				}

				carbonCount = countElement(sSmiles, "C");
				if (carbonCount > 7) {
					getComponent("tooManyCarbons").setDisplay(true);
				}
				if (carbonCount < 7) {
					getComponent("tooFewCarbons").setDisplay(true);
				}
				if (carbonCount == 7) {
					getComponent("rightcarbons").setDisplay(true);
				}
	        
				nitrogenCount = countElement(sSmiles, "N");
				if (nitrogenCount > 0)
					getComponent("nonitrogen").setDisplay(true);
				
				tripleCount = countElement(sSmiles, "#");
				if (tripleCount > 0)
					getComponent("notriple").setDisplay(true);
				
				carbonCount = countElement(sSmiles, "C");
				//general feedback
				if (iAttempt == 1)
					setFeedbackID("default");
				if (iAttempt == 2)
					getComponent("formula").setDisplay(true);
			}
		}
		return false;
	}
	
	protected void doAdditionalAnswerProcessing(
	  	boolean bRight,boolean bWrong,
	    boolean bPass,int iAttempt) throws OmDeveloperException
	{
		//if correct, passed or wrong at third attempt
		if(bRight || bPass || iAttempt == 3)
		{
			//update the reaction scheme to show the complete reaction
			//CanvasComponent cc = getCanvas("reaction");
			//cc.setString("filePath","reaction2.png");
			//cc.repaint();
//			getComponent("reaction").setString("filePath","reaction2.png");
			log("done");
		}
	}

	//method to remove wedge bonds from a drawn structure
	//applicable when stereochemistry is not important to the answer
	
	public void removeWedgeBonds() {
	    int atIndex = sSmiles.lastIndexOf('@'), sqIndex;
	    int counter = 0;

	    while (atIndex > -1) {
	      counter++;
	      String temp1 = "";
	      // check for @@ and remove @ character(s) from string
	      if (sSmiles.charAt(atIndex-1)=='@') {
	        temp1 = temp1.concat(sSmiles.substring(0,atIndex-1));
	      }
	      else {
	        temp1 = temp1.concat(sSmiles.substring(0,atIndex));
	      }
	      temp1 = temp1.concat(sSmiles.substring(atIndex+1));
	      sSmiles = temp1;
	      // search from @ for next ]
	      sqIndex = sSmiles.indexOf(']',atIndex-1);
	      temp1 = sSmiles.substring(0,sqIndex);
	      temp1 = temp1.concat(sSmiles.substring(sqIndex+1));
	      sSmiles = temp1;

	      // sarch back from @ for first [
	      sqIndex = sSmiles.lastIndexOf('[',atIndex);
	      temp1 = sSmiles.substring(0,sqIndex);
	      temp1 = temp1.concat(sSmiles.substring(sqIndex+1));
	      sSmiles = temp1;

	      atIndex = sSmiles.lastIndexOf('@');
	    }
	}

	public int countElement(String SmileStr, String element)
	//This function counts elements in a compound represented by a SMILES string. The only
	//two letter element it deals with is "Cl". It takes account of the possible
	//confusion between C and Cl but not elements such as Cr.
	//You can count triple bonds by entering '#' and double bonds by entering '='.
	//Altered by JAJ 11/07/2002 as follows:
	//For carbon counting, added a 'clash string' if letter i+1 is in this string
	//it means any 'C' isn't a carbon...
	{
	    //System.out.println("string= " + SmileStr);
	    String mystring = SmileStr;
	    int n=mystring.length();
	    int m=element.length();
	    String lowstring = element.toLowerCase();
	    boolean noClash = true;

	    int count=0;
	    for(int i=0;i<n;i++)
	    {
	      if (m<2) { //JAJ: Single letter element
	        //Counts carbons
	        if((element.charAt(0)=='C')&&(i<(n-1)))
	        {
	          //JAJ: Possible carbon clash with: Ca, Cd, Ce, Cf, Cl, Cm, Co, Cr, Cs, Cu
	          String carbonClash = "adeflmorsu";
	          int j=0;
	          while(j<carbonClash.length() && noClash)
	          {
	            if (mystring.charAt(i+1) == carbonClash.charAt(j)) {noClash = false;}
	            j++;
	          }
	          if(((mystring.charAt(i)=='C') || (mystring.charAt(i)=='c')) && noClash)
	          {
	            count++;
	          }
	          noClash = true;
	        }
	        //Counts other single elements taking no account of any possible confusions
	        //JAJ: Could extend the following as above, or pass a 'clash string' when called...
	        else if((mystring.charAt(i)==element.charAt(0))||(mystring.charAt(i)==lowstring.charAt(0)))
	        {
	          count++;
	        }
	      }
	      else
	      {
	        //counts bromines
	        if((element.charAt(0)=='B')&&(i<(n-1)))
	        {
	         if((mystring.charAt(i)=='B')&&(mystring.charAt(i+1)=='r'))
	         {
	            count++;
	          }
	        }
	        //counts chlorines
	        if((element.charAt(0)=='C')&&(i<(n-1)))
	        {
	          if(((mystring.charAt(i)=='C')||(mystring.charAt(i)=='c'))&&(mystring.charAt(i+1)=='l'))
	          {
	            count++;
	          }
	        }
	        //counts magnesiums
	        if((element.charAt(0)=='M')&&(i<(n-1)))
	        {
	          if((mystring.charAt(i)=='M')&&(mystring.charAt(i+1)=='g'))
	          {
	            count++;
	          }
	        }
	        //counts tins
	        if((element.charAt(0)=='S')&&(i<(n-1)))
	        {
	          if((mystring.charAt(i)=='S')&&(mystring.charAt(i+1)=='n'))
	          {
	            count++;
	          }
	        }
	      }
	      //System.out.println("count= " + count);
	    }
	    return(count);
	}
}
