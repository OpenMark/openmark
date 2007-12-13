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
import om.stdquestion.QDocument;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.xml.XML;

/**
Indents contained text or components.
<h2>XML usage</h2>
&lt;indent&gt;...&lt;/indent&gt;
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates all children</td></tr>
</table>
*/
public class WordSelectComponent extends QComponent
{
	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "wordselect";
	}

	private static class Select
	{
		String sID;
		QComponent qcPlaceContent;
		
	}
	
	String str = "";
	//String[] list = null;
	
	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		//this works if there are no tags within the wordselect component
		//getQDocument().buildInsideWithText(this,eThis);
		//String[] exclude = {"select"};
		//getQDocument().buildInsideExcept(this, eThis, exclude);
		
		str = XML.getText(eThis);
		//list = XML.getTextFromChildren(eThis,"select");
		
		
		
		for(Node n=eThis.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{

				Element e=(Element)n;
				if(e.getTagName().equals("select"))
				{
					str += XML.getText(e);
					//str += "found element";
				}
			}
		}
		
		
		
	
	}
	


	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{

		String s ="";
		if(str.equalsIgnoreCase("")){
			s = "no value";
		}
		else s = str;
		
		/*if(list != null){
			for(int i = 0; i<list.length; i++){
				s += list[i];
			}
		}
		else{
			s += "null list";
		}*/
	/*	Object eList[] = getChildren();
		for (int i=0; i < eList.length;i++)
		{
			if (eList[i] instanceof String)
			{
				s += (String) (eList[i]);
			}
		}*/
		

		int wordNo = -1;
		char current;
		String clickword = "";
		String checkwordID = "";
		StringBuffer sb = new StringBuffer();
		for(int i =0; i < s.length(); i++){
			current = s.charAt(i);
			if (Character.isLetterOrDigit(current)) {
				sb.append(current);
				//end = end + 1;
			}
			else{
				clickword = sb.toString();
				sb.delete(0,sb.length());
				if(clickword.length() > 0){
					wordNo++;
					checkwordID = "c" + wordNo;
					Element eInput=qc.getOutputDocument().createElement("input");
					eInput.setAttribute("type","checkbox");
					eInput.setAttribute("class", "offscreen");
					eInput.setAttribute("value", "1");
					eInput.setAttribute("onclick","wordOnClick('"+getID()+checkwordID+"','"+QDocument.ID_PREFIX+"');");
					eInput.setAttribute("id",QDocument.ID_PREFIX+getID()+checkwordID);
					qc.addInlineXHTML(eInput);
					
					Element eLabel=qc.getOutputDocument().createElement("label");
					eLabel.setAttribute("for",QDocument.ID_PREFIX+getID()+checkwordID);
					eLabel.setAttribute("class","lime");
					eLabel.setAttribute("id","label" + QDocument.ID_PREFIX+getID()+checkwordID);
					
					XML.createText(eLabel,clickword);
					
					qc.addInlineXHTML(eLabel);
					
					
					String curr = "" + current;
					Element eCurrent=qc.createElement("span");
					XML.createText(eCurrent,curr);
					qc.addInlineXHTML(eCurrent);
					
					
					
					
				}
				
			}
			
			
			
		}
		
		
		/*Element eInput=qc.getOutputDocument().createElement("input");
		eInput.setAttribute("type","checkbox");
		eInput.setAttribute("class", "offscreen");
		eInput.setAttribute("value", "1");
		eInput.setAttribute("onclick","wordOnClick('"+getID()+"','"+QDocument.ID_PREFIX+"');");
		eInput.setAttribute("id",QDocument.ID_PREFIX+getID());
		qc.addInlineXHTML(eInput);
		
		Element eLabel=qc.getOutputDocument().createElement("label");
		eLabel.setAttribute("for",QDocument.ID_PREFIX+getID());
		eLabel.setAttribute("class","lime");
		eLabel.setAttribute("id","label" + QDocument.ID_PREFIX+getID());
		
		XML.createText(eLabel,s);
		XML.createText(eLabel,"s" + len + " ");
		
		qc.addInlineXHTML(eLabel);*/
		//qc.setParent(eLabel);

		//produceChildOutput(qc,bInit,bPlain);

		//qc.unsetParent();
	}
}
