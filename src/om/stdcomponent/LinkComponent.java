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
Inserts a link on the web page. 
This link is followed in a new window 
when any of the contents are clicked upon.
<h2>XML usage</h2>
&lt;link url="http://www.open.ac.uk">Open University&lt;/link>
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>url</td><td>(string)</td><td>Specifies URL to link to</td></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>No effect</td></tr>
</table>
*/
public class LinkComponent extends QComponent
{
	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "link";
	}
	
	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		initAsText(eThis);
	}	
	
	/** Specifies attributes required */
	@Override
	protected String[] getRequiredAttributes()
	{
		return new String[]	{"url"};
	}
	
	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineString("url");
	}
	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		Element eLink = qc.createElement("a");
		eLink.setAttribute("target","_blank");
		eLink.setAttribute("href",getString("url"));
		qc.addInlineXHTML(eLink);
		qc.setParent(eLink);
		produceChildOutput(qc,bInit,bPlain);
		qc.unsetParent();
	}
}
