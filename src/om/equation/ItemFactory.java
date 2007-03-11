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
package om.equation;

import java.util.*;
import java.util.HashMap;

import org.w3c.dom.Element;

/** Creates new Item objects based on registered tag names */
class ItemFactory
{
	/** Map of String (tag name) -> ItemCreator */
	private Map mClasses=new HashMap();
	
	/**
	 * Called by item register methods to add themselves as creatable.
	 * @param sTagName XML tag name
	 * @param ic Creator object to use
	 */
	void addItemClass(String sTagName,ItemCreator ic)
	{
		mClasses.put(sTagName,ic);		
	}

	/**
	 * Creates a new Item from the given element.
	 * @param e Element 
	 * @param iParent Parent item (may be null)
	 * @return New item
	 * @throws EquationFormatException If there isn't an item class matching 
	 *   the element name
	 */
	Item newItem(Element e,Item iParent,float fZoom) throws EquationFormatException
	{
		ItemCreator ic=(ItemCreator)mClasses.get(e.getTagName());
		if(ic==null) throw new EquationFormatException(iParent,
			"Child element not allowed in equations: <"+e.getTagName()+">");
		
		Item iNew=ic.newItem();		
		iNew.init(this,e,iParent,fZoom);
		return iNew;
	}
}
