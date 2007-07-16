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

import om.OmDeveloperException;
import om.OmException;
import om.stdquestion.QComponent;
import om.stdquestion.QContent;

import org.w3c.dom.Element;

/**
Emphasises the contained text (by default rendered as bold).
<h2>XML usage</h2>
&lt;emphasis type="italic"&gt;...&lt;/emphasis&gt;
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>type</td><td>(string 'bold'|'italic'|'bolditalic')</td><td>Specifies style of emphasis</td></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates children</td></tr>
</table>
*/
public class EmphasisComponent extends QComponent
{


	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "emphasis";
	}

	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		initAsText(eThis);
	}

	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		Element eEm=qc.getOutputDocument().createElement("em");
		eEm.setAttribute("class",getString(PROPERTY_TYPE));
		qc.addInlineXHTML(eEm);
		qc.setParent(eEm);

		produceChildOutput(qc,bInit,bPlain);

		qc.unsetParent();
	}
	/** Specifies style of emphasis */
	public final static String PROPERTY_TYPE="type";
	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineString(PROPERTY_TYPE,"bold|italic|bolditalic");
		setString(PROPERTY_TYPE,"bold"); //default
	}
}
