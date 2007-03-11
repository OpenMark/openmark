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

import om.*;

import org.w3c.dom.Element;

import util.xml.*;

/** Represents a group of test items in the tree */
class TestGroup extends TestMarkedItem
{
	/** Items within this group */
	private List lItems=new LinkedList();

	/** Title of group, or null if none */
	private String sTitle=null;
	
	/** ID of group, or null if none */
	private String sID=null;
	
	/**
	 * Construct group.
	 * @param iParent Parent item (null if root)
	 * @param eGroup Group tag
	 * @throws OmFormatException I dunno, if the format's wrong
	 */
	TestGroup(TestItem iParent,Element eGroup) throws OmFormatException
	{
		super(iParent,eGroup);
		if(XML.hasChild(eGroup,"title"))
		{
			try
			{
				sTitle=XML.getText(eGroup,"title");
			}
			catch(XMLException xe)
			{
				throw new OmUnexpectedException(xe);
			}
		}
		if(eGroup.hasAttribute("id"))
			sID=eGroup.getAttribute("id");
	}
	
	/** Get ID or null if none */
	String getID() { return sID; }
	
	/**
	 * Adds item to group.
	 * @param i Item to add
	 */
	void add(TestItem i)
	{
		lItems.add(i);
	}
	
	/** Add correct number to all questions (call at top level only) */
	void numberQuestions()
	{
		TestLeaf[] atl=getLeafItems();
		int iQuestion=1;
		for(int i=0;i<atl.length;i++)
		{
			if(atl[i] instanceof TestQuestion)
			{
				TestQuestion tq=(TestQuestion)atl[i];
				tq.setNumber(iQuestion++);
			}
		}
	}
	
	/**
	 * @return Array of all leaf items within this group (recursively). 
	 */
	TestLeaf[] getLeafItems()
	{
		List l=new LinkedList();
		addLeafItems(this,l,null);
		return (TestLeaf[])l.toArray(new TestLeaf[0]);
	}
	
	/**
	 * Recursively finds questions 'under' a particular ID.
	 * @param sMatchID Relevant ID
	 * @param c Receives a list of all questions either with that ID or inside 
	 *   groups that have that ID
	 */
	public void listQuestionsUnderID(String sMatchID,Collection c)
	{
		listQuestionsUnderID(sMatchID,c,false);
	}
	
	/**
	 * Recursively finds questions 'under' a particular ID - either questions
	 * with that ID, or questions inside groups with that ID.
	 * @param sMatchID Relevant ID
	 * @param cQuestions Collection to which TestQuestions will be added
	 * @param bMatched True if the ID has already been matched
	 */
	private void listQuestionsUnderID(String sMatchID,Collection cQuestions,boolean bMatched)
	{
		if(sID!=null && sID.equals(sMatchID)) bMatched=true;
		
		for(Iterator iChildren=lItems.iterator();iChildren.hasNext();)
		{
			TestItem i=(TestItem)iChildren.next();
			if(i instanceof TestGroup)
			{
				((TestGroup)i).listQuestionsUnderID(sMatchID,cQuestions,bMatched);
			}
			else if(i instanceof TestQuestion)
			{				 
				if(bMatched || ((TestQuestion)i).getID().equals(sMatchID)) cQuestions.add(i);
			}
		}
	}
	
	/** @return Title of group, or null if it doesn't have one */
	String getTitle()
	{
		return sTitle;
	}
	
	/**
	 * Recursively builds a list of all leaf items within this group. Also sets
	 * up their section titles and does other initialisation.
	 * @param tgRoot Root group
	 * @param c List to add to
	 * @param sCurrentTitle Section title (null if none)
	 */
	private void addLeafItems(TestGroup tgRoot,Collection c,String sCurrentTitle)
	{
		if(sTitle!=null) sCurrentTitle=sTitle;
		
		for(Iterator iChildren=lItems.iterator();iChildren.hasNext();)
		{
			TestItem i=(TestItem)iChildren.next();
			if(i instanceof TestGroup)
			{
				((TestGroup)i).addLeafItems(tgRoot,c,sCurrentTitle);
			}
			else 
			{
				((TestLeaf)i).setSection(sCurrentTitle);
				if(i instanceof TestQuestion)
					((TestQuestion)i).resolveDepends(tgRoot);
				c.add(i);
			}
		}
	}
	
	PartialScore getFinalScore() throws OmFormatException
	{
		return getFinalScore(null,false);
	}
	
	PartialScore getFinalScore(String sOnly,boolean bMax) throws OmFormatException
	{
		// Build score from all children
		PartialScore ps=new PartialScore();
		for(Iterator iChildren=lItems.iterator();iChildren.hasNext();)
		{
			TestItem i=(TestItem)iChildren.next();
			if(!(i instanceof TestMarkedItem)) continue;
			TestMarkedItem tmi=(TestMarkedItem)i;			
			ps.add(tmi.getFinalScore(sOnly,bMax));
		}
		
		return rescore(ps);
	}
}