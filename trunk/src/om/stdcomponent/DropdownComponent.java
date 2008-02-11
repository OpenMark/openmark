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
import java.util.List;

import om.*;
import om.question.ActionParams;
import om.stdquestion.*;

import org.w3c.dom.Element;

import util.xml.XML;

/**
A component that produces a dropdown box with several available options.
<h2>XML usage</h2>
&lt;dropdown&gt;
&lt;option display="A choice" value="x"/&gt;
&lt;option display="Another choice" value="y"/&gt;
&lt;/dropdown&gt;
<p>
Note that the options are <em>not</em> question components; you can only use
plain text.
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates this control</td></tr>
<tr><td>lang</td><td>(string)</td><td>Specifies the language of the content, like the HTML lang attribute. For example 'en' = English, 'el' - Greek, ...</td></tr>
<tr><td>selected</td><td>(string)</td><td>Value ID of the selected entry</td></tr>
</table>
*/
public class DropdownComponent extends QComponent
{
	/** Property name for value of box */
	public final static String PROPERTY_SELECTED="selected";

	/** Class representing one option */
	private static class Option
	{
		String sValue;
		String sDisplay;
	}

	/** List of Option */
	private List<Option> lOptions=new LinkedList<Option>();

	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "dropdown";
	}

	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineString(PROPERTY_SELECTED);
	}

	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		Element[] aeOptions=XML.getChildren(eThis);
		for(int i=0;i<aeOptions.length;i++)
		{
			if(!aeOptions[i].getTagName().equals("option"))
				throw new OmFormatException("<dropdown>: May only include <option>s");
			if(!aeOptions[i].hasAttribute("value") || !aeOptions[i].hasAttribute("display"))
				throw new OmFormatException("<dropdown>: Must include value= and display=");
			addOption(
				aeOptions[i].getAttribute("value"),aeOptions[i].getAttribute("display"));
		}
	}

	/**
	 * Adds an option to the dropdown.
	 * @param sValue Value for option
	 * @param sDisplay Display text of option
	 * @throws OmException
	 */
	public void addOption(String sValue,String sDisplay) throws OmException
	{
		Option o=new Option();
		o.sValue=sValue;
		o.sDisplay=sDisplay;
		lOptions.add(o);
		// Default selection to first value
		if(!isPropertySet(PROPERTY_SELECTED))
			setString(PROPERTY_SELECTED,o.sValue);
	}

	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		// Create select
		Element eSelect=qc.createElement("select");
		qc.addInlineXHTML(eSelect);
		eSelect.setAttribute("name",QDocument.ID_PREFIX+QDocument.VALUE_PREFIX+getID());
		eSelect.setAttribute("id",QDocument.ID_PREFIX+QDocument.VALUE_PREFIX+getID());
		addLangAttributes(eSelect);

		// Create all options
		String sSelectedValue=null;
		for(Option o : lOptions)
		{
			Element eOption=XML.createChild(eSelect,"option");
			if(getString(PROPERTY_SELECTED).equals(o.sValue))
			{
				eOption.setAttribute("selected","selected");
				sSelectedValue=o.sDisplay;
			}
			XML.createText(eOption,o.sDisplay);
		}

		if(isEnabled())
		{
			qc.informFocusable(QDocument.ID_PREFIX+QDocument.VALUE_PREFIX+getID(),bPlain);
		}
		else
		{
			eSelect.setAttribute("disabled","disabled");
		}

		qc.addTextEquivalent("["+sSelectedValue+"]");
	}

	@Override
	protected void formSetValue(String sValue,ActionParams ap) throws OmException
	{
		for(Option o : lOptions)
		{
			if(o.sDisplay.equals(sValue))
			{
				setString(PROPERTY_SELECTED,o.sValue);
				return;
			}
		}

		throw new OmException("Unexpected dropdown value: "+sValue);
	}

	/** @return ID of dragbox that's placed in box (null if none) */
	public String getSelected()
	{
		try
		{
			return getString(PROPERTY_SELECTED);
		}
		catch(OmDeveloperException e)
		{
			throw new OmUnexpectedException(e);
		}
	}

	/** @param sValue ID of dragbox that should be placed in box */
	public void setSelected(String sValue)
	{
		try
		{
			setString(PROPERTY_SELECTED,sValue);
		}
		catch(OmDeveloperException e)
		{
			throw new OmUnexpectedException(e);
		}
	}

	@Override
	public String setString(String sName,String sValue) throws OmDeveloperException
	{
		if(sName.equals(PROPERTY_SELECTED))
		{
			boolean bOK=false;
			for(Option o : lOptions)
			{
				if(o.sValue.equals(sValue))
				{
					bOK=true;
					break;
				}
			}
			if(!bOK)
				throw new OmDeveloperException("Not a valid dropdown option: "+sValue);
		}
		return super.setString(sName,sValue);
	}
}
