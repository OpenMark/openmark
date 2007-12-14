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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import om.OmDeveloperException;
import om.OmException;
import om.OmUnexpectedException;
import om.question.ActionParams;
import om.stdquestion.QComponent;
import om.stdquestion.QContent;
import om.stdquestion.QDocument;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

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
	
	private ArrayList<WordBlock> alWordBlocks;

	private static class WordBlock
	{
		String sID;
		String words;
		boolean checkedHighlight = false;
		boolean secondHighlight = false;
		boolean isSW = false;
		
	}
	
	String str = "";
	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		
		str="";
		
		StringBuffer sbText=new StringBuffer();
		for(Node n=eThis.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element e=(Element)n;
				if(e.getTagName().equals("sw"))
				{
					//str += XML.getText(e);
					//str += " found sw element";
					if (alWordBlocks == null) alWordBlocks = new ArrayList<WordBlock>(10);
					WordBlock p =new WordBlock();
					p.words = XML.getText(e);
					p.isSW = true;
					if(e.hasAttribute("highlight")){
						
					}
						 
					
					
					alWordBlocks.add(p);
				}
				else
				{
					throw new OmDeveloperException("<selectword> can only contain <sw> tags");
				}
			}
			else if(n instanceof Text)
			{
				// Appending text to buffer allows us to join up text nodes where
				// there are multiple nodes for one string (e.g. if there's CDATA
				// in the middle or something)
				sbText.append(n.getNodeValue());
				if(sbText.length()>0)
				{
					if (alWordBlocks == null) alWordBlocks = new ArrayList<WordBlock>(10);
					WordBlock p =new WordBlock();
					p.words = (sbText.toString());
					
					alWordBlocks.add(p);
					
					//str += (sbText.toString());
					sbText.setLength(0);
				}
			}
		}
	}
	


	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{

		
		String s = "";
		if (alWordBlocks != null){
			for (int i=0; i < alWordBlocks.size(); i++)
			{
				WordBlock ip = alWordBlocks.get(i);
				s = ip.words;
				//s +=" text ";
			
			
				int wordNo = -1;
				char current;
				String clickword = "";
				String checkwordID = "";
				StringBuffer sb = new StringBuffer();
				for(int j =0; j < s.length(); j++){
					current = s.charAt(j);
					if (Character.isLetterOrDigit(current)) {
						sb.append(current);
						//end = end + 1;
					}
					else{
						clickword = sb.toString();
						sb.delete(0,sb.length());
						if(clickword.length() > 0){
							wordNo++;
							checkwordID = "b" + i + "c" + wordNo;
							Element eInput=qc.getOutputDocument().createElement("input");
							eInput.setAttribute("type","checkbox");
							eInput.setAttribute("class", "offscreen");
							eInput.setAttribute("name", QDocument.ID_PREFIX+QDocument.VALUE_PREFIX+getID());
							eInput.setAttribute("value", "1_" + checkwordID);
							eInput.setAttribute("onclick","wordOnClick('"+getID()+checkwordID+"','"+QDocument.ID_PREFIX+QDocument.VALUE_PREFIX+"');");
							eInput.setAttribute("id",QDocument.ID_PREFIX+QDocument.VALUE_PREFIX+getID()+checkwordID);
							qc.addInlineXHTML(eInput);
							
							Element eLabel=qc.getOutputDocument().createElement("label");
							eLabel.setAttribute("for",QDocument.ID_PREFIX+QDocument.VALUE_PREFIX+getID()+checkwordID);
							eLabel.setAttribute("class","lime");
							eLabel.setAttribute("id","label" + QDocument.ID_PREFIX+QDocument.VALUE_PREFIX+getID()+checkwordID);
							
							XML.createText(eLabel,clickword);
							
							qc.addInlineXHTML(eLabel);
							
							
							String curr = "" + current;
							Element eCurrent=qc.createElement("span");
							XML.createText(eCurrent,curr);
							qc.addInlineXHTML(eCurrent);	
							
						}
						
					}
					
				}
			
			}
			//s += " ";
		}
		

	/*	int wordNo = -1;
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
			
			
			
		}*/
		
		
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
	
	File f = new File("C:/temp/omdebug.txt");
	PrintWriter debugOutput = null;
	protected void formSetValue(String sValue,ActionParams ap) throws OmException
	{
		if (debugOutput == null) {
			try {
				debugOutput = new PrintWriter(new FileOutputStream(f));
			} catch (FileNotFoundException e) {
			}
		}
		debugOutput.println("In formSetValue. sValue = " + sValue);
	}

	protected void formAllValuesSet(ActionParams ap) throws OmException
	{
		if (debugOutput == null) {
			try {
				debugOutput = new PrintWriter(new FileOutputStream(f));
			} catch (FileNotFoundException e) {
			}
			debugOutput.println("formSetValue was never called.");
		}
		debugOutput.println("In formAllValuesSet.");
		debugOutput.close();
		debugOutput = null;
	}

}
