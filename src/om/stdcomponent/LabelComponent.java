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

import org.w3c.dom.Element;

/**
Indicates that the contents are a label for another control.
Necessary for accessibility purposes. Should
be used in conjunction with editfields or advancedfields.
<h2>XML usage</h2>
&lt;label for='myedit'>...&lt;/label>
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates all children</td></tr>
<tr><td>for</td><td>(string)</td><td>Om component ID of thing being labelled</td></tr>
</table>
*/
public class LabelComponent extends QComponent
{
	/** Om component ID of thing being labelled */
	public final static String PROPERTY_FOR="for";

	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "label";
	}

	@Override
	protected String[] getRequiredAttributes()
	{
		return new String[] { PROPERTY_FOR };
	}

	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineString(PROPERTY_FOR,PROPERTYRESTRICTION_ID);
	}

	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		initAsText(eThis);
	}

	/**
	 * @param qd The question definition.
	 * @param bPlain True if in plain mode.
	 * @param sID the component that we label.
	 * @return The XHTML id of the thing that should be labelled.
	 * @throws OmException
	 */
	public static String getLabel(QDocument qd,boolean bPlain,String sID) throws OmException
	{
		QComponent qcTarget=qd.find(sID);
		if(qcTarget instanceof Labelable)
		{
			return ((Labelable)qcTarget).getLabelTarget(bPlain);
		}
		else
		{
			throw new OmFormatException("<label>: Cannot apply to component "+
				qcTarget.getID()+"; not a labelable component");
		}
	}

	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		Element eLabel=qc.getOutputDocument().createElement("label");
		eLabel.setAttribute("for",getLabel(getQDocument(),bPlain,getString(PROPERTY_FOR)));

		qc.addInlineXHTML(eLabel);
		qc.setParent(eLabel);
		produceChildOutput(qc,bInit,bPlain);
		qc.unsetParent();
	}
}
