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
package om.stdcomponent;

import java.util.LinkedList;

import om.OmDeveloperException;
import om.OmException;
import om.stdquestion.QComponent;
import om.stdquestion.QContent;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.xml.XML;

/***
This is a component to layout bulletted and numbered lists.<br/>
The list component will arrange the components directly below as a list.
<h2>Example XML usage</h2>
This is a list of fruit <br/>
&lt;list type='number'>
 <br/>&lt;t>Apple&lt;/t>
 <br/>&lt;t>Pear&lt;/t>
 <br/>&lt;t>Orange&lt;/t>
 <br/>
&lt;/list>

<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>type</td><td>(optional string; 'bullet'|number')</td><td>type of list</td></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
</table>
The default list type is 'bullet'.
<br/>

*/
public class ListComponent extends QComponent
{
	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "list";
	}
	
	
	/** Defines possible attributes */
	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineString("type","bullet|number");
		setString("type","bullet");
	}
	
	private QComponent[] aListItems;

	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		LinkedList<QComponent> llItems = new LinkedList<QComponent>();
		for(Node nChild=eThis.getFirstChild();nChild!=null;nChild=nChild.getNextSibling())
		{
			if(nChild instanceof Element)
			{
				Element e=(Element)nChild;
				QComponent qcItem = getQDocument().build(this,e,null);
				llItems.add(qcItem); // Store in Linked list of cells
				addChild(qcItem);// Also store in standard child array so it can be found
			}	
		}
		
		aListItems = llItems.toArray(new QComponent[0]);
	}
	
	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		Element eList;
		if (getString("type").equals("number")) 
			eList=qc.createElement("ol");
		else 
			eList=qc.createElement("ul");
		qc.addInlineXHTML(eList);
		for (int i=0; i<aListItems.length ;i++)
		{
			Element eItem = XML.createChild(eList,"li");
			qc.setParent(eItem);
			aListItems[i].produceOutput(qc,bInit,bPlain);
			qc.unsetParent();
		}
	}
	
	
}  // end of class
