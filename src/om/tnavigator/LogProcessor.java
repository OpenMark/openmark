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
package om.tnavigator;

import java.io.*;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import util.misc.CausedIOException;
import util.xml.XML;

/** 
 * Use this in conjunction with something implementing LogProcessorHandler
 * to carry out tasks that require looping around every log entry. 
 */
public class LogProcessor implements ContentHandler
{
	/** Document used to create elements */
	private Document d;
	
	/** Handler that actually does stuff */
	private LogProcessorHandler lph;
	
	/**
	 * @param f
	 * @param lph
	 * @throws IOException
	 */
	public LogProcessor(File f,LogProcessorHandler lph) throws IOException
	{
		// Set up document
		d=XML.createDocument();
		this.lph=lph;
		
		// Do SAX parse
		try
		{
			XMLReader xr=XMLReaderFactory.createXMLReader();
			xr.setContentHandler(this);
			xr.parse(new InputSource(new InputStreamReader(
				new LogInputStream(f),"UTF-8")));
			lph.finish();
		}
		catch(SAXException se)
		{
			throw new CausedIOException("Error processing log file: "+se.getMessage(),se);
		}
	}
	
	// SAX ContentHandler implementation
	
	/** Stack for building up DOM elements via SAX parsing */
	Stack<Element> s=new Stack<Element>();

	// Ignored methods

	public void setDocumentLocator(Locator locator) {}
	public void startDocument() {}
	public void endDocument() {}
	public void startPrefixMapping(String prefix, String uri) {}
	public void endPrefixMapping(String prefix) {}
	public void processingInstruction(String target, String data) {}
	public void skippedEntity(String name) {}
	public void ignorableWhitespace(char[] ch, int start, int length) {}

	// Useful methods

	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
	{
		// Create DOM element
		Element e=d.createElement(localName);
		for(int i=0;i<atts.getLength();i++)
		{
			e.setAttribute(
				atts.getLocalName(i),atts.getValue(i));
		}

		// Check whether this needs to be added to the current tree
		if(!s.empty())
		{
			Element eParent=s.peek();
			eParent.appendChild(e);
			s.push(e);
		}
		else
		{
			// Our tree starts at <entry>
			if(e.getTagName().equals("entry"))
				s.push(e);
			else if(e.getTagName().equals("log"))
				lph.start(e);
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException
	{
		if(s.empty()) return;

		// Create string of characters
		String sText=new String(ch,start,length);

		// Add to current element
		Element eCurrent=s.peek();
		eCurrent.appendChild(d.createTextNode(sText));
	}

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	{
		if(s.empty()) return;

		Element e=s.pop();
		if(s.empty())
		{
			lph.entry(e);
		}
	}
	
}
