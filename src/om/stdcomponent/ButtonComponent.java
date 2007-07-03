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
import om.question.ActionParams;
import om.stdquestion.*;

import org.w3c.dom.*;

/** 
A standard XHTML button. 
<h2>XML usage</h2>
&lt;button action="actionOK" label="OK"/&gt;
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates this control</td></tr>
<tr><td>action</td><td>(string)</td><td>Name of method in question class that is called if user clicks button</td></tr>
<tr><td>label</td><td>(string)</td><td>Text of button label</td></tr>
</table>
*/
public class ButtonComponent extends QComponent
{
	/** Name of method in question class that is called if user clicks button. */
	public static final String PROPERTY_ACTION="action";
	/** Text of button label */
	public static final String PROPERTY_LABEL="label";
	
	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "button";
	}
	
	@Override
	protected String[] getRequiredAttributes()
	{
		return new String[]
		{
			PROPERTY_LABEL,
			PROPERTY_ACTION,
		};		
	}
	
	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineString(PROPERTY_LABEL);
		defineString(PROPERTY_ACTION);
	}
	
	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		if(eThis.getFirstChild()!=null) throw new OmFormatException(
			"<button> may not contain other content");
	}
	
	@Override
	protected void initSpecific(Element eThis) throws OmException
	{
		getQuestion().checkCallback(getString(PROPERTY_ACTION));
	}
	
	@Override
	protected void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		Element eInput=qc.getOutputDocument().createElement("input");
		eInput.setAttribute("type","submit");
		String sLabel=getString(PROPERTY_LABEL);
		if(sLabel.length()<4) sLabel=" "+sLabel+" ";
		eInput.setAttribute("value",sLabel);
		eInput.setAttribute("name",QDocument.ACTION_PREFIX+getID());
		if(!bPlain)
		{
			eInput.setAttribute("id",QDocument.ACTION_PREFIX+getID());
			eInput.setAttribute("onclick","if(this.hasSubmitted) { return false; } this.hasSubmitted=true; preSubmit(this.form); return true;");
		}
		if(!isEnabled()) eInput.setAttribute("disabled","yes");
				
		qc.addInlineXHTML(eInput);
		if(isEnabled())	qc.informFocusable(QDocument.ACTION_PREFIX+getID(),bPlain);
		
		qc.addTextEquivalent("[Button: "+getString(PROPERTY_LABEL)+"]");
	}
	
	@Override
	protected void formCallAction(String sValue,ActionParams ap) throws OmException
	{
		getQuestion().callback(getString(PROPERTY_ACTION));
	}
}
