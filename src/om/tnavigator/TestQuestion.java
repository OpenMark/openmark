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
package om.tnavigator;

import java.util.*;

import om.OmFormatException;
import om.tnavigator.scores.CombinedScore;

import org.w3c.dom.Element;

import util.xml.*;


/** Represents a question */
class TestQuestion extends TestMarkedItem implements TestLeaf
{
	final static int VERSION_UNSPECIFIED=-1;
	private String sID;
	private int iVersion=VERSION_UNSPECIFIED;
	
	private String sSection;
	private int iNumber;
	private boolean bDone=false;
	CombinedScore psActual=null;
	
	TestQuestion(TestItem iParent,Element eQuestion) throws OmFormatException
	{
		super(iParent,eQuestion);
		try
		{
			sID=XML.getRequiredAttribute(eQuestion,"id");
		}
		catch(XMLException e)
		{
			throw new OmFormatException("<question>: Missing id= attribute");
		}
		
		try
		{
			String sVersion=eQuestion.getAttribute("version");
			if(sVersion!=null && !sVersion.equals("")) iVersion=Integer.parseInt(sVersion);
		}
		catch(NumberFormatException nfe)
		{
			throw new OmFormatException("<question>: version= must be an integer");
		}
	}
	
	/** @return Required question ID */
	String getID()
	{
		return sID;
	}
	
	/** @return Required question version or VERSION_UNSPECIFIED for latest */
	int getVersion()
	{
		return iVersion;			
	}
	
	public boolean isDone()
	{
		return bDone;
	}
	
	/**
	 * Marks question as done/equivalent (or not)
	 * @param bDone True if question has been done
	 */
	public void setDone(boolean bDone)
	{
		this.bDone=bDone;
	}

	/** 
	 * Sets the number for this question (called by TestGroup). 
	 * @param i Question number
	 */
	void setNumber(int i)
	{
		iNumber=i;
	}
	
	/** @return Number to display for this question */
	int getNumber()
	{
		return iNumber;
	}
	
	public void setSection(String s)
	{
		sSection=s;
	}
	
	public String getSection()
	{
		return sSection;
	}
	
	/** Array of questions on which this depends */
	private TestQuestion[] atqDepends = null;
	
	public boolean isAvailable()
	{
		for(int i=0;i<atqDepends.length;i++)
		{
			if(!atqDepends[i].isDone()) return false;
		}
		return true;
	}
	
	
	/** 
	 * Resolves the dependencies list down to actual questions.
	 * @param tgRoot Root to look up dependencies in. 
	 */
	void resolveDepends(TestGroup tgRoot)
	{
		List<TestQuestion> l=new LinkedList<TestQuestion>();
		TestItem ti=this;
		while(ti!=null)
		{
			String sThisDepends=ti.getDepends();
			if(sThisDepends!=null)
			{
				tgRoot.listQuestionsUnderID(sThisDepends,l);				
			}
			ti=ti.getParent();
		}
		atqDepends=l.toArray(new TestQuestion[0]);
	}
	
	/**
	 * Call to fill in user's actual score on this question.
	 * @param ps Score (use null to clear)
	 */
	void setActualScore(CombinedScore ps)
	{
		this.psActual = ps;
	}
	
	/**
	 * @return True if a score has been set
	 */
	boolean hasActualScore()
	{
		return psActual!=null;
	}
	
	@Override
	CombinedScore getFinalScore(String sOnly,boolean bMax) throws OmFormatException
	{
		CombinedScore psRescored=rescore(psActual);
		
		if(sOnly!=null && !sOnly.equals(sID))
		{
			psRescored = CombinedScore.zeroScore(psRescored);
		}
		else if(bMax)
		{
			psRescored = CombinedScore.maxScore(psRescored);
		}
		
		return psRescored;
	}
	
	/** 
	 * @return Contribution toward final marks after all scaling
	 */
	CombinedScore getScoreContribution(TestGroup tgRoot) throws OmFormatException
	{
		// Work out contributions this question only has to final results if (a)
		// they score what they actually scored, and (b) they score the max possible
		CombinedScore contribution = tgRoot.getFinalScore(getID(),false);
		CombinedScore max = tgRoot.getFinalScore(getID(),true);
		return CombinedScore.scoreOutOfMax(contribution, max);
	}
}