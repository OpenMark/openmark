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

import om.OmException;
import om.stdquestion.QComponent;
import om.stdquestion.QContent;

import org.w3c.dom.Element;


/**
Represents ordinary text (and arbitrary contained components). These components
are automatically generated inside various other components whenever authors
enter text there.
<h2>XML usage</h2>
&lt;t&gt;Here's some text&lt;/t&gt;
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates children</td></tr>
<tr><td>lang</td><td>(string)</td><td>Specifies the language of the content, like the HTML lang attribute. For example 'en' = English, 'el' - Greek, ...</td></tr>
</table>
*/
public class TextComponent extends QComponent
{
	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "t";
	}

	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		getQDocument().buildInsideWithText(this,eThis);
	}

	@Override
	protected void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		if (isPropertySet(PROPERTY_LANG)) {
			Element eDiv=qc.getOutputDocument().createElement("div");
			eDiv.setAttribute("class", "t");
			addLangAttributes(eDiv);
			qc.addInlineXHTML(eDiv);
			qc.setParent(eDiv);

			produceChildOutput(qc,bInit,bPlain);

			qc.unsetParent();
		} else {
			super.produceVisibleOutput(qc, bInit, bPlain);
		}
	}

	/**
	 * Sets the text to a new value. Note that calling this method gets rid of
	 * any children of this text component.
	 * @param s New text
	 */
	public void setText(String s)
	{
		removeChildren();
		addChild(s);
	}

	/**
	 * Gets the string which represents the text of a text component.
	 * Note that this does not look in further components inside the text component.
	 * @return text as string
	 */
	public String getText()
	{
		String s="";
		Object eList[] = getChildren();
		for (int i=0; i < eList.length;i++)
		{
			if (eList[i] instanceof String)
			{
				s += (String) (eList[i]);
			}
		}
		return s;
	}

}
