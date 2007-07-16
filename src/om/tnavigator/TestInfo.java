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


import java.io.IOException;

import om.OmUnexpectedException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.xml.XML;
import util.xml.XMLException;


/** Represents a text page */
class TestInfo extends TestItem implements TestLeaf
{
	private String sSection;
	private Element eThis;
	private boolean bDone=false;
	
	/** 
	 * Construct from given XML element.
	 * @param eText Element containing &lt;p&gt; tags etc.
	 */		
	TestInfo(TestItem iParent,Element eText)
	{
		super(iParent,eText);
		eThis=eText;
	}
	
	/** @return Title or default if none */
	String getTitle()
	{
		if(XML.hasChild(eThis,"title"))
		{
			try
			{
				return XML.getText(eThis,"title");
			}
			catch(XMLException e)
			{
				throw new OmUnexpectedException(e);
			}
		}
		else
			return "Test information";
	}
	
	/**
	 * Copy the text node DOM into a target XHTML document.
	 * @param eTargetParent Parent that the text elements/nodes will be added 
	 *   to.
	 */
	void copyDOM(Element eTargetParent)
	{
		for(Node n=eThis.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(!(n instanceof Element)) 
				continue;			
			
			if(((Element)n).getTagName().equals("title"))
				continue;
			
			eTargetParent.appendChild(eTargetParent.getOwnerDocument().importNode(
				n,true));
		}
	}
	
	String getXHTMLString() throws IOException
	{
		StringBuffer sb=new StringBuffer();
		
		for(Node n=eThis.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(!(n instanceof Element)) 
				continue;

			if(((Element)n).getTagName().equals("title"))
				continue;
			
			sb.append(XML.saveString((Element)n));
		}
		return sb.toString();
	}
	
	public void setSection(String s)
	{
		sSection=s;
	}
	
	public String getSection()
	{
		return sSection;
	}

	public boolean isDone()
	{
		return bDone;
	}
	
	/**
	 * Marks text as seen (or not)
	 * @param bDone True if text has been shown
	 */
	void setDone(boolean bDone)
	{
		this.bDone=bDone;
	}	
	
	public boolean isAvailable() { return true; }
}