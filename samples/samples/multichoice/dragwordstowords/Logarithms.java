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
package samples.multichoice.dragwordstowords;

import java.util.Random;

import om.*;
import om.helper.SimpleQuestion1;


/** Reactions Q8a: tests understanding of pH */
public class Logarithms extends SimpleQuestion1
{
  public int iVariant;
  public int nwrong = 0;
  private final static String[] ACID_OR_BASE = {"acid", "basic"};
  private final static String[] GREATER_OR_LESS = {"greater", "less"};
    
  protected void init() throws OmException
  {
    Random r = new Random();
    iVariant = r.nextInt(2);
    iVariant++;
    iVariant %= 2;

    setPlaceholder("ACIDORBASE", ""+ACID_OR_BASE[iVariant]);
    setPlaceholder("GREATERORLESS", ""+GREATER_OR_LESS[iVariant]);

    if(iVariant == 0)
    	getComponent("answer1").setDisplay(false);
    else
    	getComponent("answer0").setDisplay(false);

  }
    
  protected boolean isRight(int iAttempt) throws OmDeveloperException
	{
     
  	String b1OK, b2OK, b3OK, b4OK;
  	nwrong = 0;
	
    b1OK=getDropBox("mw1").getValue();
    b2OK=getDropBox("mw2").getValue();
    b3OK=getDropBox("mw3").getValue();
    b4OK=getDropBox("mw4").getValue();
 
    if(iVariant == 0) // acid
    {
     	if(b1OK.equals("a1")){}
     	else
     	{
     		nwrong++;
     	}
     	if(b2OK.equals("a8")){}
    	else
    	{
    		nwrong++;
    	}
     	if(b3OK.equals("a10")){}
    	else
    	{
    		nwrong++;
    	}
     	if(b4OK.equals("a6")){}
    	else
    	{
    		nwrong++;
    	}
    }
    else //base
    {
     	if (b1OK.equals("a1")){}
     	else
     	{
     		nwrong++;
     	}
     	if(b2OK.equals("a8")){}
    	else
    	{
    		nwrong++;
    	}
     	if(b3OK.equals("a9")){}
    	else
    	{
    		nwrong++;
    	}
     	if(b4OK.equals("a6")){}
    	else
    	{
    		nwrong++;
    	} 
    }

    //  store response information
    getResults().appendActionSummary("Variant:" + iVariant + " Attempt " + iAttempt + ": 1:" + b1OK + " 2:" + b2OK + " 3:" + b3OK + " 4:" + b4OK);
 
   	if(nwrong == 0)
   	{
   	    return true;
   	}
   	
    if(iAttempt == 2)
  	{ 
  		setFeedbackID("still");
  	   	if(nwrong > 1)
  	   	{
  	      setPlaceholder("N", ""+nwrong);
  	   	  setFeedbackID("manyWrong");
  	   	}
  	   	else if(nwrong == 1)
  	   	{
  	      setFeedbackID("oneWrong");
  	   	}
  	}
    if(iAttempt == 3)
  	{ 
  		setFeedbackID("showanswer");
  	}
  
		return false;  
	}  
}
