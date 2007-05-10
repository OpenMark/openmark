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
package om.equation;

import java.io.*;

import javax.xml.transform.Transformer;

import om.OmUnexpectedException;
import om.equation.generated.*;

import org.w3c.dom.*;
import org.w3c.dom.Node;

import util.xml.*;

/** Interprets simple equations into XHTML */
public class TextEquation
{
	private static Transformer t;
	
	private static boolean hasMboxParent(Element e)
	{
		Node nParent=e.getParentNode();
		if(nParent instanceof Document) return false;
		Element eParent=(Element)nParent;
		if(eParent.getTagName().equals("mbox")) return true;
		return hasMboxParent(eParent);
	}
	
	/**
	 * Turns equation into simple XHTML, using italic for letters. 
	 * @param sEquation Input equation
	 * @param dOutput Output document in which equation will be created
	 * @return Element ready for adding to output document
	 * @throws EquationFormatException
	 */
	public static Element process(String sEquation,Document dOutput)
		throws EquationFormatException
	{
		return process(sEquation,dOutput,true);
	}
	
	/**
	 * Turns equation into simple XHTML. 
	 * @param sEquation Input equation
	 * @param dOutput Output document in which equation will be created
	 * @param bItalic True if italic should be used for letters
	 * @return Element ready for adding to output document
	 * @throws EquationFormatException
	 */
	public static Element process(String sEquation,Document dOutput,boolean bItalic)
	  throws EquationFormatException
	{
		try
		{
			EquationFormat ef=new EquationFormat(new StringReader(sEquation));
			Element eEquation=ef.equation().createDOM(XML.createDocument());
			eEquation.getOwnerDocument().appendChild(eEquation);
			
			NodeList nl=eEquation.getOwnerDocument().getElementsByTagName("int_text");
			for(int i=0;i<nl.getLength();i++)
			{
				Element e=(Element)nl.item(i);
				// See if there's an mbox parent...
				if(!hasMboxParent(e))
					XML.setText(e,Text.fixupText(XML.getText(e),e.getPreviousSibling()==null));
			}
			
			Document dResult;
			synchronized(TextEquation.class)
			{
				if(t==null)
				{
					t=XSL.newTransformer(
						TextEquation.class.getResourceAsStream("TextEquation.xsl"));
				}
				
				t.setParameter("ITALIC",bItalic?"y":"n");
				dResult=XSL.transform(t,eEquation.getOwnerDocument());
			}
			return (Element)dOutput.importNode(dResult.getDocumentElement(),true);
		}
		catch(ParseException pe)
		{
			throw new EquationFormatException(pe,sEquation);
		}
		catch(IOException e)
		{
			throw new OmUnexpectedException(e);
		}		
	}
	
	/**
	 * Test method.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
//		test("N_0");		
//		test("–λ");		
//		test("-N_0");		
//		test("\\ln(–λ)");		
//		test("\\ln N_0");		
//		test("-\\ln λ");		
//		test("\\ln N");		
	test("N+3");		
//		test("-\\ln N_0");		
//		test("\\ln λ");		
//		test("λ");		
//		test("\\ln(-N_0)");		
		/*
		<dragbox id="a1"><eq>N_0</eq></dragbox>
		<dragbox id="a2"><eq>–λ</eq></dragbox>
		<dragbox id="a3"><eq>–N_0</eq></dragbox>
		<dragbox id="a4"><eq>\ln(–λ)</eq></dragbox>
		<break/>
		<dragbox id="a5"><eq>\ln N_0</eq></dragbox>
		<dragbox id="a6"><eq>-\ln λ</eq></dragbox>
		<dragbox id="a7"><eq>\ln N</eq></dragbox>
		<dragbox id="a8"><eq>N</eq></dragbox>
		<break/>
		<dragbox id="a9"><eq>-\ln N_0</eq></dragbox>
		<dragbox id="a10"><eq>\ln λ</eq></dragbox>
		<dragbox id="a11"><eq>λ</eq></dragbox>
		<dragbox id="a12"><eq>\ln(-N_0)</eq></dragbox>*/
	}

	private static void test(String sEq) throws Exception
	{
		Document d=XML.createDocument();
		Element e=TextEquation.process(sEq,d);
		System.out.println("Alt:");
		System.out.println(e.getAttribute("alt"));
		System.out.println("XHTML:");
		System.out.println(XML.saveString(e));
		
		e=TextEquation.process(sEq,d,false);
		System.out.println("Alt:");
		System.out.println(e.getAttribute("alt"));
		System.out.println("XHTML [no italic]:");
		System.out.println(XML.saveString(e));
	}
}
