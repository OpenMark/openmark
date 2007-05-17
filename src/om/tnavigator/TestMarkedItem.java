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

import om.OmFormatException;

import org.w3c.dom.Element;

import util.xml.*;


/**
 * Represents a test item that may include &lt;rescore&gt; tags.
 */
abstract class TestMarkedItem extends TestItem
{
	/** Each rescore tag */
	private MarkInfo[] ami;
	
	/** Information from a rescore tag */
	private static class MarkInfo	
	{
		/** New total marks range */
		int iMarks;
		
		/** Axis that marks came from (null if default) */
		String sFromAxis=null;
		
		/** Axis that marks go to */
		String sAxis=null;
		
		/**
		 * Construct from given XML element.
		 * @param e XML element
		 * @throws OmFormatException If any attributes are invalid
		 */
		private MarkInfo(Element e) throws OmFormatException
		{
			try
			{
				iMarks=Integer.parseInt(
					XML.getRequiredAttribute(e,"marks"));
			}
			catch(NumberFormatException nfe)
			{
				throw new OmFormatException("<rescore> - Invalid number for marks=: "+e.getAttribute("marks"));
			}
			catch(XMLException xe)
			{
				throw new OmFormatException("<rescore> - Must have marks= attribute");
			}
			if(e.hasAttribute("axis"))
			{
				sAxis=e.getAttribute("axis");
				if(sAxis.equals("")) sAxis=null;
				sFromAxis=sAxis; // Make fromaxis default to axis
			}
			if(e.hasAttribute("fromaxis"))
			{
				sFromAxis=e.getAttribute("fromaxis");
				if(sFromAxis.equals("")) sFromAxis=null;
			}
		}
		
		/**
		 * Adds the mark contribution from this rescore to the new value. 
		 * @param psNew New partial score we're building up 
		 * @param psBase Base (source)
		 */
		private void rescore(PartialScore psNew,PartialScore psBase) 
			throws OmFormatException
		{
			// Score contribution from this tag
			double dScore=
				(iMarks * psBase.getScore(sFromAxis)) / 
				psBase.getMax(sFromAxis);
			
			// Either set as new...
			if(!psNew.hasScore(sAxis))
			{
				psNew.setScore(sAxis,dScore,iMarks);
			}
			else // ...or add existing
			{
				psNew.setScore(sAxis,
					dScore+psNew.getScore(sAxis),iMarks+psNew.getMax(sAxis));
			}
		}
	}
	
	/**
	 * Constructs item.
	 * @param iParent Parent or null if none
	 * @param eThis XML tag for this item (that may contain &lt;rescore&gt; tags)
	 * @throws OmFormatException Any format error
	 */
	TestMarkedItem(TestItem iParent,Element eThis) throws OmFormatException
	{
		super(iParent,eThis);
		
		Element[] aeRescore=XML.getChildren(eThis,"rescore");
		ami=new MarkInfo[aeRescore.length];
		for(int i=0;i<aeRescore.length;i++)
		{
			ami[i]=new MarkInfo(aeRescore[i]);
		}
	}
	
	/**
	 * Uses the details from &lt;rescore&gt; elements to adjust the score 
	 * calculated thus far, returning a new score. 
	 * @param ps Current score
	 */
	PartialScore rescore(PartialScore ps) throws OmFormatException
	{
		// If no changes, we return the existing score unmolested, axes and all
		if(ami.length==0) return (PartialScore)ps.clone();
	
		// OK, time to rescore
		PartialScore psNew=new PartialScore();
		for(int i=0;i<ami.length;i++)
		{
			ami[i].rescore(psNew,ps);
		}
		
		return psNew;		
	}
	
	/**
	 * @param sOnly If non-null, counts only the final score contribution from
	 *   the given ID'd question 
	 * @param bMax If true, forces all questions that are counted (usually just
	 *   sOnly) to have max contribution
	 * @return The calculated score resulting from this item
	 * @throws OmFormatException
	 */
	abstract PartialScore getFinalScore(String sOnly,boolean bMax) throws OmFormatException;
}