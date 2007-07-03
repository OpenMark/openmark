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

import java.awt.Dimension;

import om.*;
import om.question.ActionParams;
import om.stdquestion.*;

import org.w3c.dom.*;

import util.xml.XML;

/** 
Standard XHTML edit field for entering a single line or multiple lines of plain text. Can estimate
its own size for inclusion within {@link EquationComponent}s.  Default is one line. 
<h2>XML usage</h2>
&lt;editfield/&gt;
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates this control</td></tr>
<tr><td>cols</td><td>(integer)</td><td>Number of columns (approx) to allow space for</td></tr>
<tr><td>rows</td><td>(integer)</td><td>Number of rows (approx) to allow space for</td></tr>
<tr><td>value</td><td>(string)</td><td>Current value of field (text content)</td></tr>
<tr><td>label</td><td>(string)</td><td>Label for accessibility purposes; this
  label will be displayed only in plain mode. If you want a visible label,
  use a {@link LabelComponent}.</td></tr>
</table>
This component also supports the forcewidth and forceheight properties that
are required as part of size approximation.
<br/>
The "value" attribute cannot include carriage returns in the xml file.  
If you want to include these in an initial value then you can use
setValue(String) in the "init" function (java code).  
<br/>
eg. getEditField("input1").setValue("1\n2\n3");
*/
public class EditFieldComponent extends QComponent implements Labelable
{
	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "editfield";
	}

	/** Number of columns (approx) to allow space for */
	public static final String PROPERTY_COLS="cols";
	/** Number of rows (approx) to allow space for */
	public static final String PROPERTY_ROWS="rows";
	/** Label for accessibility purposes */
	public static final String PROPERTY_LABEL="label";
	/** Property name for value of editfield */
	public final static String PROPERTY_VALUE="value";

	private boolean bGotLabel=false;

	/** If true, none of parents do anything particular - needed for some IE-specific stylin'. */
	private boolean bNonScaryParents;
	
	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineString(PROPERTY_VALUE);

		defineInteger(PROPERTY_COLS);
		defineInteger(PROPERTY_ROWS);

		defineString(PROPERTY_LABEL);

		defineInteger(PROPERTY_FORCEWIDTH);
		defineInteger(PROPERTY_FORCEHEIGHT);
		
		setString(PROPERTY_VALUE,"");
		setInteger(PROPERTY_COLS,20);
		setInteger(PROPERTY_ROWS,1);
	}
	
	@Override
	public Dimension getApproximatePixelSize() throws OmDeveloperException
	{
		return new Dimension(6*getInteger(PROPERTY_COLS)+26,11*getInteger(PROPERTY_ROWS)+11);
	}
	
	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		if(eThis.getFirstChild()!=null) throw new OmFormatException(
			"<editfield> may not contain other content");
		
		// Check whether there are any parents that might do scary things (i.e. anything other than 
		// TextComponent, Indent, etc.)
		bNonScaryParents=true;
		QComponent[] aqc=getAncestors();
		for(int i=0;i<aqc.length;i++)			
		{
			if(aqc[i] instanceof BoxComponent) break;
			if(!(aqc[i] instanceof TextComponent || aqc[i] instanceof IndentComponent ||
				aqc[i] instanceof CentreComponent))
			{
				bNonScaryParents=false;
				break;
			}
		}
	}
	
	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		int iRows = getInteger(PROPERTY_ROWS);
		Element eInput;
		if (iRows == 1)
		{
			eInput=qc.getOutputDocument().createElement("input");
			eInput.setAttribute("type","text");
			eInput.setAttribute("value",getString(PROPERTY_VALUE));
			eInput.setAttribute("size",""+getInteger(PROPERTY_COLS));
		}
		else if (iRows>1 && iRows<31)
		{
			eInput=qc.getOutputDocument().createElement("textarea");
			eInput.setAttribute("wrap","soft");
			eInput.setAttribute("cols",""+getInteger(PROPERTY_COLS));
			eInput.setAttribute("rows",""+getInteger(PROPERTY_ROWS));
			XML.createText(eInput,getString(PROPERTY_VALUE));
		}
		else 
			throw new OmFormatException(
			"<editfield> rows must be in the range 1 to 30"); 

		eInput.setAttribute("name",QDocument.VALUE_PREFIX+getID());		
		eInput.setAttribute("id",QDocument.VALUE_PREFIX+getID());

		if(!isEnabled()) eInput.setAttribute("readonly","yes");
		
		if(!(bGotLabel || isPropertySet(PROPERTY_LABEL)))
		{
			throw new OmFormatException("<editfield> "+getID()+": Requires <label> or label=");
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
		}
		else
		{
			if(isPropertySet(PROPERTY_FORCEWIDTH) && isPropertySet(PROPERTY_FORCEHEIGHT))
			{
				eInput.setAttribute("style",
					"width:"+(getInteger(PROPERTY_FORCEWIDTH)-6)+"px; " +
					"height:"+(getInteger(PROPERTY_FORCEHEIGHT)-6)+"px;");
				eInput.setAttribute("class","editfieldforced");
			}
			else if(bNonScaryParents)
			{
				eInput.setAttribute("class","editfieldnonscary");
			}
		}
		if (!isEnabled())
		{
			if(getQuestion().isFixedColour())
				eInput.setAttribute("class","editfielddisabledfixed");
			else
				eInput.setAttribute("class","editfielddisabled");
		}
		
		qc.addInlineXHTML(eInput);
		if(isEnabled()) qc.informFocusable(eInput.getAttribute("id"),bPlain);
		qc.addTextEquivalent("[Editfield: "+getValue()+"]");
	}
	
	/** Maximum length of single line */
	private final static int MAXCHARS_SINGLELINE=100;
	
	@Override
	protected void formSetValue(String sValue,ActionParams ap) throws OmException
	{
		sValue=sValue.replaceAll("\r\n","\n");
		if(getInteger(PROPERTY_ROWS)==1) sValue=trim(sValue,MAXCHARS_SINGLELINE);
		setString(PROPERTY_VALUE,sValue);
	}
	
	/** @return Current value of edit field. For single-line editfield, 
	 * value is trimmed to be no more than 100 characters. 
	 */
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
	
	/** @param sValue New value for edit field */
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
				"<editfield>: You cannot have both a label= and a <label> for the same field");
		else
		{
			bGotLabel=true;
			return QDocument.VALUE_PREFIX+getID();
		}
	}
}
