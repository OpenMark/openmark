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
package om.question;

import java.util.*;

import org.w3c.dom.Element;

/** 
 * Information a Question provides in order that it may be rendered as XHTML
 * for the user's browser.
 */
public class Rendering
{
	/** Question XHTML content */
  private Element eXHTML=null;

  /** CSS stylesheet */
  private String sCSS=null;
  
  /** Progress information */
  private String sProgressInfo=null;
  
	/** List of returned resources (QResource) */
	private List<Resource> lResources=new LinkedList<Resource>();
	
	/**
	 * @param eXHTML Root element for question
	 */
	public void setXHTML(Element eXHTML)
	{
		this.eXHTML=eXHTML;
	}
	
	/** @return XHTML content; null only in QActionRendering */
	public Element getXHTML() { return eXHTML; }
	
	/**
	 * Call to set CSS stylesheet. 
	 * <p>
	 * In QActionRendering: This overrides any stylesheet that was 
	 * previously set. You do not need to call this if you want to continue 
	 * using the same stylesheet.
	 * @param sCSS CSS stylesheet contents
	 */
	public void setCSS(String sCSS)
	{
		this.sCSS=sCSS;
	}
	
	/** @return CSS content or null if not set */
	public String getCSS() { return sCSS; }
	
	/**
	 * Adds a new resource.
	 * <p>
	 * In QActionRendering: The new resource is added to any existing ones, or
	 * replaces them if the name is identical. You do not need to re-add 
	 * resources each time.
	 * @param qr
	 */
	public void addResource(Resource qr)
	{
		lResources.add(qr);
	}
	
	/** @return All resources added to question */
	public Resource[] getResources() 
	{ 
		return lResources.toArray(new Resource[0]);
	}
	
	/** 
	 * @param sProgressInfo Brief information on progress through question that
	 *   will be displayed alongside the question (for example, number of attempts
	 *   remaining). Progress information is retained between actions if it is
	 *   set to null in future ones.
	 */
	public void setProgressInfo(String sProgressInfo)
	{
		this.sProgressInfo=sProgressInfo;
	}
	
	/** @return Progress information or null if no change */
	public String getProgressInfo() { return sProgressInfo; }
}
