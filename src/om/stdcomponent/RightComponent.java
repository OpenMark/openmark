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

import om.*;
import om.stdquestion.*;
import om.stdquestion.QComponent;

import org.w3c.dom.Element;

/** 
Right-aligns contained text or components.  
<h2>XML usage</h2>
&lt;right&gt;...&lt;/right&gt;
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates all children</td></tr>
</table>
*/
public class RightComponent extends QComponent
{
	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "right";
	}
	
	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		initAsText(eThis);
	}
	
	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		Element eDiv=qc.getOutputDocument().createElement("div");
		if(!bPlain) eDiv.setAttribute("class","right");
		qc.addInlineXHTML(eDiv);
		qc.setParent(eDiv);
		
		produceChildOutput(qc,bInit,bPlain);
		
		qc.unsetParent();		
	}
}
