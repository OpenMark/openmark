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

import java.awt.Color;

import om.*;
import om.stdquestion.QComponent;
import om.stdquestion.QContent;

import org.w3c.dom.Element;

/**
A top-level box that goes within the root &lt;question&gt; tag (after the
&lt;layout&gt; section). Contains other controls and text.
<h2>XML usage</h2>
&lt;box gridx="0" gridy="0"&gt;...&lt;/box&gt;
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates all children</td></tr>
<tr><td>gridx</td><td>(integer)</td><td>Controls position in overall question grid (0 = leftmost)</td></tr>
<tr><td>gridy</td><td>(integer)</td><td>Controls position in question grid (0 = topmost)</td></tr>
<tr><td>gridwidth</td><td>(integer)</td><td>Width in grid cells, default 1</td></tr>
<tr><td>gridheight</td><td>(integer)</td><td>Height in grid cells, default 1</td></tr>
<tr><td>background</td><td>(string; colour)</td><td>Predefined colour constant
  to use for background: "input", "answer", or "other"</td></tr>
</table>
*/
public class BoxComponent extends QComponent
{
	/** Colour to use for the background. */
	public final static String PROPERTY_BACKGROUND="background";
	/** If true, never show this box in plain mode. */
	public final static String PROPERTY_PLAINHIDE="plainhide";

	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "box";
	}

	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineInteger("gridx");
		defineInteger("gridy");
		defineInteger("gridwidth");
		defineInteger("gridheight");
		setInteger("gridwidth",1);
		setInteger("gridheight",1);
		defineBoolean(PROPERTY_PLAINHIDE);
		setBoolean(PROPERTY_PLAINHIDE,false);

		defineString(PROPERTY_BACKGROUND,COLOURCONSTANTS_REGEXP);
		setString(PROPERTY_BACKGROUND,"input");

		// Decided not to do it this way as there isn't a mechanism for actually
		// changing/setting the colours and the system needs to know what they are
//		defineString("type","(question)|(answer)|(other)");
//		setString("type","other");
	}

	@Override
	protected String[] getRequiredAttributes()
	{
		return new String[] { "gridx","gridy" };
	}

	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		initAsText(eThis);
	}

	@Override
	protected Color getChildBackground(QComponent qcChild)
	{
		try
		{
			return convertRGB(getString("background"));
		}
		catch(OmDeveloperException ode)
		{
			throw new OmUnexpectedException(ode);
		}
	}

	@Override
	protected void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain)
	  throws OmException
	{
		if(!bPlain || !getBoolean(PROPERTY_PLAINHIDE))
			super.produceVisibleOutput(qc,bInit,bPlain);
	}
}
