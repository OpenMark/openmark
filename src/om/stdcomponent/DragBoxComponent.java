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
import om.stdquestion.*;

import org.w3c.dom.Element;

import util.xml.XML;

/** 
A component that can be dropped onto DropBoxComponents.
<h2>XML usage</h2>
&lt;dragbox/&gt;
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates this control and children</td></tr>
<tr><td>group</td><td>(string)</td><td>Optional group ID (should match that on target dropbox); will change colour and
  make it only droppable on that box</td></tr>
<tr><td>infinite</td><td>(boolean)</td><td>If true, user can drag from this same box more than once</td></tr>
<tr><td>sidelabel</td><td>(string)</td><td>Optional label that appears in small text inside the box at the right. If included, must be a single character</td></tr>
</table>
<h2>Sizing</h2>
<p>Drag boxes are automatically made the same size as the largest box in the
group (so they all have identical size with each other and with the dropbox). 
There is no way to control their size other than by changing their contents.</p>
*/
public class DragBoxComponent extends QComponent
{
	/** If true, user can drag from this same box more than once */
	public final static String PROPERTY_INFINITE="infinite";
	/** Optional label that appears in small text inside the box at the right. If included, must be a single character */
	public final static String PROPERTYREGEXP_SIDELABEL=".";
	
	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "dragbox";
	}
	
	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineString(DropBoxComponent.PROPERTY_GROUP,DropBoxComponent.PROPERTYREGEXP_GROUP);
		defineBoolean(PROPERTY_INFINITE);
		defineString(DropBoxComponent.PROPERTY_SIDELABEL,PROPERTYREGEXP_SIDELABEL);
		
		setString(DropBoxComponent.PROPERTY_GROUP,"");
		setBoolean(PROPERTY_INFINITE,false);
	}
	
	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		getQDocument().buildInsideWithText(this,eThis);
	}
	
	/**
	 * Produce plain version of content to go inside a dropbox. This is the
	 * text equivalent of everything inside it.
	 * @param bInit True on first produceOutput in question
	 * @return Text equivalent of content
	 * @throws OmException
	 */
	String getPlainDropboxContent(boolean bInit) throws OmException
	{
		QContent qc=new QContent(XML.createDocument());
		qc.beginTextMode();
		produceChildOutput(qc,bInit,true);
		if(isPropertySet(DropBoxComponent.PROPERTY_SIDELABEL))
		{
			qc.addTextEquivalent(
				"("+getString(DropBoxComponent.PROPERTY_SIDELABEL)+")");
		}
		return qc.endTextMode();		
	}
	
	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		// In plain mode we don't do anything at all since dragbox content appears
		// within dropboxes instead.
		if(bPlain) return;
		
		// Add image (initially invisible)
		Element eImg=qc.createElement("img");
		eImg.setAttribute("class","dragboximg");
		eImg.setAttribute("src","%%RESOURCES%%/clear.gif");
		eImg.setAttribute("id",QDocument.ID_PREFIX+getID()+"img");
		qc.addInlineXHTML(eImg);
		

		// Add a space (needed for IE)
		qc.addInlineXHTML(qc.getOutputDocument().createTextNode(" "));
		
		// Add actual content container
		Element eContainer=qc.createElement("div");
		eContainer.setAttribute("class","dragbox" + 
			(isEnabled() ? "" : " transparent")+
			(isPropertySet(DropBoxComponent.PROPERTY_SIDELABEL) ? " withsidelabel": ""));
		eContainer.setAttribute("id",QDocument.ID_PREFIX+getID());
		eContainer.setAttribute("style",
			"background:"+convertHash(getGroupColour())+";");
		//qc.addInlineXHTML(eContainer);
		qc.addTopLevelXHTML(eContainer);
		
		Element eInner=XML.createChild(eContainer,"div");
		eInner.setAttribute("class","dragboxinner");
		eInner.setAttribute("id",QDocument.ID_PREFIX+getID()+"inner");

		// Create drag pane
//		Element ePane=XML.createChild(eContainer,"div");
//		ePane.setAttribute("class","dragboxpane");
//		ePane.setAttribute("id",getID()+"pane");
		
		// Produce content		
		qc.setParent(eInner);
		produceChildOutput(qc,bInit,bPlain);
		qc.unsetParent();
		
		if(isPropertySet(DropBoxComponent.PROPERTY_SIDELABEL))
		{
			String sSideLabel=getString(DropBoxComponent.PROPERTY_SIDELABEL);
			Element eSideLabel=XML.createChild(eContainer,"div");
			eSideLabel.setAttribute("class","dragboxsidelabel");
			eSideLabel.appendChild(
					qc.getOutputDocument().createTextNode(sSideLabel));
		}
		
		// Add JS
		String sGroup=getString(DropBoxComponent.PROPERTY_GROUP);
		Element eScript=qc.createElement("script");
		eScript.setAttribute("type","text/javascript");
		XML.createText(eScript,"dragboxInform('"+getID()+"','"+QDocument.ID_PREFIX+"',"+
			(isEnabled() ? "true" : "false") + ",'"+sGroup+"'," +
			(getBoolean(PROPERTY_INFINITE) ? "true" : "false")+");");
		qc.addTopLevelXHTML(eScript);
	}
	
	private String getGroupColour() throws OmDeveloperException
	{
		String sGroup=getString(DropBoxComponent.PROPERTY_GROUP);
		return 	"innerbg"+(getQDocument().getGroupIndex(sGroup)%4);
	}
	
	/** Return correct background colour */
	@Override
	protected Color getChildBackground(QComponent qcChild)
	{
		try
		{
			return convertRGB(getGroupColour());
		}
		catch(OmDeveloperException e)
		{
			throw new OmUnexpectedException(e);
		}
	}	
}
