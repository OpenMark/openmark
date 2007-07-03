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

import org.w3c.dom.Element;

import util.xml.XML;

/** 
A text field that can allow the user to enter superscripts and subscripts, 
or performs automatic subscript formatting for chemical formulae.<br/>
Subscript and superscript modes can be changed using linked checkboxes or by 
using the up and down arrow keys. 
<br/>
The <b>plain</b> text version of the control asks the user to type the sub and sup
tags themselves and in the chemical formula mode to ignore the formatting.
<br/>

<h2>XML usage</h2>
&lt;advancedfield id='reaction' type='superscript' /&gt;
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates this control</td></tr>
<tr><td>cols</td><td>(integer)</td><td>Number of columns (approx) to allow space for the component</td></tr>
<tr><td>value</td><td>(string)</td><td>Current value of field. (See below) </td></tr>
<tr><td>type</td><td>(string; 'superscript' | 'subscript' | 'both' | 'chem')</td><td>Type of field.</td></tr>
</table>
<br/>
If type='chem' is specified then there are no subscript or subscript checkboxes 
and the text is automatically displayed formatted with subscripts appropriate to 
chemical formulae eg "3H<sub>2</sub>O + 2CO<sub>2</sub>".  
The value returned does <b>not</b> include the formatting tags in this case.
<br/>
For types other than 'chem', the value returned includes the
<b>sub</b> and <b>sup</b> elements for subscripts and superscripts.
Thus 10<sup>3</sup> is returned as '10&lt;sup>3&lt;/sup>'
*
**/
public class AdvancedFieldComponent extends QComponent implements Labelable
{
	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "advancedfield";
	}

	/** Property name for value of editfield */
	public final static String PROPERTY_VALUE="value";
	/** Number of columns */
	public static final String PROPERTY_COLS="cols";
	/** Label text */
	public static final String PROPERTY_LABEL="label";
	/** Type of editfield (affects JS) */
	public final static String PROPERTY_TYPE="type";
	
	private boolean bGotLabel=false;	
	
	@Override
	protected String[] getRequiredAttributes()
	{
		return new String[]
		{
			"type"
		};
	}
	
	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineString(PROPERTY_VALUE);
		defineInteger(PROPERTY_COLS);
		defineString(PROPERTY_LABEL);		
		defineString(PROPERTY_TYPE,"superscript|subscript|both|chem");
		
		setString(PROPERTY_VALUE,"");
		setInteger(PROPERTY_COLS,20);
	}
	
	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		if(eThis.getFirstChild()!=null) throw new OmFormatException(
			"<editfield> may not contain other content"); 
	}
	
	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{		
		if(bInit)
		{
			qc.addResource("blank.html","text/html;charset=UTF-8",
				new byte[0]);
		}
		
		String sType = getString(PROPERTY_TYPE);
		
		if(!(bGotLabel || isPropertySet(PROPERTY_LABEL)))
		{
			throw new OmFormatException("<advancedfield> "+getID()+": Requires <label> or label=");
		}
		
		if(bPlain)
		{
			if(isPropertySet(PROPERTY_LABEL) && !getString(PROPERTY_LABEL).equals(""))
			{
				Element eLabel=qc.createElement("label");
				qc.addInlineXHTML(eLabel);
				eLabel.setAttribute("for",QDocument.VALUE_PREFIX+getID());
				XML.createText(eLabel,getString(PROPERTY_LABEL));
			}
			
			Element eDiv=qc.createElement("div");
			qc.addInlineXHTML(eDiv);
			
			XML. createText(eDiv,"p",
					"(In the following edit field: " +
					(	(sType.equals("superscript") || sType.equals("both")) ?
							" Type { before, and " +
							"} after, any text you want superscripted." : ""
					)+
					(	(sType.equals("subscript") || sType.equals("both")) ?
							" Type [ before, and " +
							"] after, any text you want subscripted." : ""
					)+
					(	(sType.equals("chem")) ?
							" Type your answer ignoring subscripting. " +
							"For example, two water molecules should be entered as 2H2O." : ""
					)+	")"
				);
				
			Element eInput=XML.createChild(eDiv,"input");
			eInput.setAttribute("type","text");
			eInput.setAttribute("size",""+getInteger(PROPERTY_COLS));
			eInput.setAttribute("name",QDocument.VALUE_PREFIX+getID());
			eInput.setAttribute("id",QDocument.VALUE_PREFIX+getID());
			
			String sValue=getString(PROPERTY_VALUE);
			sValue=sValue.replaceAll("<sup>","{");
			sValue=sValue.replaceAll("</sup>","}");
			sValue=sValue.replaceAll("<sub>","[");
			sValue=sValue.replaceAll("</sub>","]");
			eInput.setAttribute("value",sValue);

			if(!isEnabled()) eInput.setAttribute("disabled","disabled");			
		}
		else
		{
			Element eDiv=qc.createElement("div");
			qc.addInlineXHTML(eDiv);
			eDiv.setAttribute("class","advancedfield");
			double dZoom=getQuestion().getZoom();
			Element eIframe=qc.createElement("iframe");
			eDiv.appendChild(eIframe);
			eIframe.setAttribute("id",QDocument.OM_PREFIX+getID()+"_iframe");
			eIframe.setAttribute("src","%%RESOURCES%%/blank.html");
			eIframe.setAttribute("scrolling","no");
			eIframe.setAttribute("width",""+(int)(10*dZoom*getInteger(PROPERTY_COLS)));
			eIframe.setAttribute("height",""+(int)(28*dZoom));
			eIframe.setAttribute("mysubtype",sType);
			eIframe.setAttribute("zoom",""+dZoom);
			eIframe.setAttribute("frameborder","no"); //suppresses 3D frame in IE
			if (!isEnabled())
			{
				if(getQuestion().isFixedColour())
					eIframe.setAttribute("class","advancedfielddisabledfixed");
				else
					eIframe.setAttribute("class","advancedfielddisabled");
			}
			
			Element eHidden=qc.createElement("input");
			eDiv.appendChild(eHidden);
			eHidden.setAttribute("type","hidden");
			eHidden.setAttribute("name",QDocument.VALUE_PREFIX+getID());
			eHidden.setAttribute("id",QDocument.VALUE_PREFIX+getID());
			eHidden.setAttribute("value",getString(PROPERTY_VALUE));
			
			eDiv.appendChild(qc.createElement("br"));
			eDiv.appendChild(qc.getOutputDocument().createTextNode("\n"));
			

			if(sType.equals("superscript") || sType.equals("both"))
			{
				Element eCheckbox=qc.createElement("input");
				eDiv.appendChild(eCheckbox);
				eCheckbox.setAttribute("type","checkbox");
				eCheckbox.setAttribute("id",QDocument.OM_PREFIX+getID()+"_sup");
				//was onclick below
				eCheckbox.setAttribute("onclick",
					"advancedfieldSup('"+getID()+"');");
				if(!isEnabled()) 
					eCheckbox.setAttribute("disabled","yes");
				Element eLabel=qc.createElement("label");
				eDiv.appendChild(eLabel);
				eLabel.setAttribute("for",QDocument.OM_PREFIX+getID()+"_sup");
				eLabel.setAttribute("title","Click checkbox or type up arrow to enable superscript"); 
				XML.createText(eLabel,"Superscript (\u2191) ");

			}
			
			if(sType.equals("subscript") || sType.equals("both"))
			{
				Element eCheckbox=qc.createElement("input");
				eDiv.appendChild(eCheckbox);
				eCheckbox.setAttribute("type","checkbox");
				eCheckbox.setAttribute("id",QDocument.OM_PREFIX+getID()+"_sub");
				eCheckbox.setAttribute("onclick",
					"advancedfieldSub('"+getID()+"');");
				if(!isEnabled()) eCheckbox.setAttribute("disabled","yes");
				Element eLabel=qc.createElement("label");
				eDiv.appendChild(eLabel);
				eLabel.setAttribute("for",QDocument.OM_PREFIX+getID()+"_sub");
				eLabel.setAttribute("title","Click checkbox or type down arrow to enable subscript"); 
				XML.createText(eLabel,"Subscript (\u2193)");
			}
			
			Element eScript=qc.createElement("script");
			eDiv.appendChild(eScript);
			eScript.setAttribute("type","text/javascript");
			String sfg=getQuestion().getFixedColourFG();
			String sbg=getQuestion().getFixedColourBG();
			if (sbg==null) sbg="#FFFFFF";
			if (sfg==null) 
				{
				if (isEnabled()) sfg="#000000";
				else sfg="#999999";
				}
			XML.createText(eScript,"addOnLoad( function() { advancedfieldFix('"+getID()+"',"+
				(isEnabled()?"true":"false")+ ",'" +sType+ "'," + dZoom+ ",'"+sfg+"','"+sbg+"'); } );");
			
			// Can be focused (hopefully)
			if(isEnabled()) qc.informFocusableFullJS(getID(),"document.getElementById('"+
				QDocument.OM_PREFIX+getID()+"_iframe').contentWindow",bPlain);
		}
	}
	
	/**
	 * Replaces something repeatedly until it's DEAD. (I think you might need this
	 * when replacing something where the source string might overlap with the
	 * next replacement.) 
	 * @param sValue Original string 
	 * @param sFind Thing to find 
	 * @param sReplace Thing to replace
	 * @return Resulting string
	 */
	private static String replaceContinuous(String sValue,String sFind,String sReplace)
	{
		while(true)
		{
			String sNew=sValue.replaceFirst(sFind,sReplace);
			if(sNew.equals(sValue)) return sNew;
			sValue=sNew;
		}
	}
		
	@Override
	protected void formSetValue(String sValue,ActionParams ap) throws OmException
	{
		// In plain mode they enter {} and []. We try hard to make it into valid xhtml
		// but uh, I don't guarantee it.
		if(ap.hasParameter("plain"))
		{
			// Replace double-opening {[ (nesting is not allowed)
			sValue=replaceContinuous(sValue,"(\\{[^}]*)\\{","$1");
			sValue=replaceContinuous(sValue,"(\\[[^\\]]*)\\[","$1");
			// Replace double-closing }] 
			sValue=replaceContinuous(sValue,"(\\}[^{]*)\\}","$1");
			sValue=replaceContinuous(sValue,"(\\][^\\[]*)\\]","$1");
			
			// Untangle tangled groups  {   [ becomes  {  }[
			sValue=sValue.replaceAll("(\\{[^}]*)\\[","$1}[");
			sValue=sValue.replaceAll("(\\[[^\\]]*)\\{","$1]{");
			// Add missing closes
			sValue=sValue.replaceFirst("(\\{[^}]*)$","$1}");
			sValue=sValue.replaceFirst("(\\[[^\\]]*)$","$1]");
			// Remove extra closes
			sValue=sValue.replaceFirst("(\\{[^}]*)$","$1}");
			sValue=sValue.replaceFirst("(\\[[^\\]]*)$","$1]");
			
			// The above might mess up the first part again, so...
			
			// Replace double-opening {[ (nesting is not allowed)
			sValue=replaceContinuous(sValue,"(\\{[^}]*)\\{","$1");
			sValue=replaceContinuous(sValue,"(\\[[^\\]]*)\\[","$1");
			// Replace double-closing }] 
			sValue=replaceContinuous(sValue,"(\\}[^{]*)\\}","$1");
			sValue=replaceContinuous(sValue,"(\\][^\\[]*)\\]","$1");
			
			// Replace with HTML tags
			sValue=sValue.replaceAll("\\[","<sub>");
			sValue=sValue.replaceAll("\\{","<sup>");
			sValue=sValue.replaceAll("\\]","</sub>");
			sValue=sValue.replaceAll("\\}","</sup>");
		}
		else
		{
			// firefox adds nbsp which should be replaced with space
			sValue=sValue.replaceAll("&nbsp;"," ");
			// uppercase tags
			sValue=sValue.replaceAll("<SUP>","<sup>");
			sValue=sValue.replaceAll("</SUP>","</sup>");
			sValue=sValue.replaceAll("<SUB>","<sub>");
			sValue=sValue.replaceAll("</SUB>","</sub>");
			// remove redundant pairs
			sValue=sValue.replaceAll("</sup><sup>|</sub><sub>","");
			// if not chemical formula need to save sup and sub
			if (!getString(PROPERTY_TYPE).equals("chem")) 
				sValue=sValue.replaceAll("<(sup|/sup|sub|/sub)>","`om~$1~mo`");
			// remove all other tags
			sValue=sValue.replaceAll("<[^<]+>",""); 
			// put sub and sup tags back again
			sValue=sValue.replaceAll("`om~(sup|/sup|sub|/sub)~mo`","<$1>"); 
	
			sValue=sValue.replaceAll("&lt;","<"); // in case typed literally
			sValue=sValue.replaceAll("&gt;",">"); // in case typed literally
		}
		
		setString(PROPERTY_VALUE,trim(sValue,MAXCHARS_ADVANCEDFIELD));
	}
	
	/** Maximum length of single line */
	private final static int MAXCHARS_ADVANCEDFIELD=100;
	
	/** @return Current value of edit field (may include HTML codes). Trimmed to 100 characters. */
	public String getValue()
	{
		try
		{
			return getString(PROPERTY_VALUE);
		}
		catch(OmDeveloperException e)
		{
			throw new OmUnexpectedException(e);
		}
	}
	
	/** @param sValue New value for edit field (may include HTML codes) */
	public void setValue(String sValue) 
	{
		try
		{
			setString(PROPERTY_VALUE,sValue);
		}
		catch(OmDeveloperException e)
		{
			throw new OmUnexpectedException(e);
		}
	}
	
	public String getLabelTarget(boolean bPlain) throws OmDeveloperException
	{
		if(isPropertySet(PROPERTY_LABEL))
			throw new OmFormatException(
				"<advancedfield>: You cannot have both a label= and a <label> for the same field");
		else
		{
			bGotLabel=true;
			if(bPlain)
				return QDocument.VALUE_PREFIX+getID();
			else
				return QDocument.OM_PREFIX+getID()+"_iframe";
		}
	}	
}
