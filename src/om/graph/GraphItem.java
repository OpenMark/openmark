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
package om.graph;

import java.awt.Graphics2D;

/** 
 * Superclass for all graph items. In addition to the obvious requirements
 * of this class, other restrictions apply to graph items:
 * <ul>
 * <li> For a tag &lt;tagName&gt;, the class must be called TagNameItem - 
 *   case here is rigid and required</li>
 * <li> Class must have a public no-argument constructor</li>
 * <li> Class must have set methods matching each attribute name e.g. 
 *   myattribute requires setMyAttribute(...) - any case is permitted</li>
 * <li> Permitted types for attributes are String, double, int, boolean,
 *   GraphScalar, GraphRange, Color, and Font</li>
 * </ul>
 */
public abstract class GraphItem
{
	/** Owning World */
	private World wOwner;
	
	/** ID */
	private String sID=null;
	
	/** True if init has been called */
	private boolean bInited=false;
	
	/**
	 * Use when constructing items manually in code.
	 * @param w World object for item
	 */
	protected GraphItem(World w) throws GraphFormatException
	{
		setWorld(w);
	}
	
	/** @return Owner World object */
	public World getWorld() 
	{ 
		return wOwner; 
	}
	
	/** @param w Owning World object */ 
	public void setWorld(World w) 
	{
		this.wOwner=w; 
	} 
	
	/** @return ID or null if this item doesn't have one */
	public String getID()
	{
		return sID;
	}
	
	/**
	 * @param sID New ID for item
	 */  
	public void setID(String sID)
	{
		this.sID=sID;
	}
	
	/**
	 * Paints the graph item. 
	 * @param g2 Target graphics context
	 */
	public abstract void paint(Graphics2D g2);
	
	/** Internal method used by World which calls init if needed. */
	final void checkInit() throws GraphFormatException
	{
		if(!bInited)
		{
			init();
			bInited=true;
		}
	}
	
	/**
	 * Init method, for overriding. Called after all attributes are set,
	 * and before first paint.
	 * @throws GraphFormatException 
	 */
	public void init() throws GraphFormatException
	{
	}
}
