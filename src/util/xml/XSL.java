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
package util.xml;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/** Utilities for applying transformations */
public abstract class XSL
{
	/**
	 * Constructs a transformer.
	 * @param f Source XSL file
	 * @return Transformer object
	 * @throws IOException If there is any error
	 */
	public static Transformer newTransformer(File f) throws IOException
	{
		return newTransformer(XML.parse(f));
	}

	/**
	 * Constructs a transformer.
	 * @param is Source input stream (will not be closed)
	 * @return Transformer object
	 * @throws IOException If there is any error
	 */
	public static Transformer newTransformer(InputStream is) throws IOException
	{
		return newTransformer(XML.parse(is,null));
	}

	/**
	 * Constructs a transformer.
	 * @param d Source XML document
	 * @return Transformer object
	 * @throws IOException If there is any error
	 */
	public static Transformer newTransformer(Document d) throws IOException
	{
		return newTransformer(new DOMSource(d));
	}

	/**
	 * Constructs a transformer. (Lowest-level method.)
	 * @param s javax.xml.transform Source
	 * @return Transformer object
	 * @throws IOException If there is any error
	 */
	public static Transformer newTransformer(Source s) throws IOException
	{
		try
		{
			// Note: This synch is actually (according to spec) needed only when
			// creating transformers. Actually transforming requires only that the
			// same *transformer* isn't used twice at once.
			synchronized(XML.getTransformSynch())
			{
				return XML.getTransformerFactory().newTransformer(s);
			}
		}
		catch (TransformerConfigurationException e)
		{
			throw new XMLException("Error with XSL stylesheet ("+findLocation(e)+"): "+e.getMessage(),e);
		}
	}

	/**
	 * Transforms one DOM tree into another.
	 * @param t Transformer to use
	 * @param dSource Source document
	 * @return New DOM tree
	 * @throws IOException If any error occurs
	 */
	public static Document transform(Transformer t,Document dSource) throws IOException
	{
		Document dResult=XML.createDocument();
		transform(t,new DOMSource(dSource),new DOMResult(dResult));
		return dResult;
	}
	
	/**
	 * Transforms one DOM into a string. (Note: You need to use this version
	 * if you are relying on any disable-output-escaping. This code cannot 
	 * run in an applet unless the applet is signed, due to a Xalan bug in the
	 * way it loads resources.)
	 * @param t Transformer to use
	 * @param dSource Source document
	 * @return String value of result
	 * @throws IOException If any error occurs
	 */
	public static String transformToString(Transformer t,Document dSource) throws IOException
	{
		StringWriter sw=new StringWriter();
		transform(t,new DOMSource(dSource),new StreamResult(sw));
		return sw.toString();
	}

	/**
	 * Transforms one DOM tree into another.
	 * @param dXSL XSL DOM object
	 * @param dSource Source DOM
	 * @return New DOM tree
	 * @throws IOException If any error occurs
	 */
	public static Document transform(Document dXSL,Document dSource) throws IOException
	{
		return transform(newTransformer(dXSL),dSource);
	}

	/**
	 * Transforms one DOM tree into another, using a number of input parameters.
	 * @param dXSL XSL DOM object
	 * @param dSource Source DOM
	 * @param mParams input parameters.
	 * @return New DOM tree
	 * @throws IOException If any error occurs
	 */
	public static Document transform(Document dXSL,Document dSource,Map mParams) throws IOException
	{
		Transformer t = newTransformer(dXSL);
		setTransformerParameters(t, mParams);
		return transform(t,dSource);
	}

	/**
	 * Applies XSL transform. (Lowest-level method.)
	 * @param t Transformer to use
	 * @param s Source document
	 * @param r Result document
	 * @throws IOException If any error occurs
	 */
	public static void transform(Transformer t,Source s,Result r) throws IOException
	{
		try
		{
			t.transform(s,r);
		}
		catch (TransformerException e)
		{
			throw new XMLException("Error performing XSL transformation ("+findLocation(e)+"): "+e.getMessage(),e);
		}
	}
	
	/**
	 * Evaluate an XPath expression against a document using xsl:value-of, and return the resulting
	 * string.
	 * @param dInput the doucument to evaluate the expression against.
	 * @param sXPath the XPath expression to evaluate.
	 * @return the result.
	 * @throws IOException
	 */
	public static String evaluateXPathExpression(Document dInput, String sXPath) throws IOException {
		Document dXSLT = XML.parse(
				"<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>" +
				"<xsl:template match='/'><value><xsl:value-of/></value></xsl:template></xsl:stylesheet>");
		XML.getNestedChild(dXSLT, "xsl:value-of").setAttribute("select", sXPath);
		Document dResult = XSL.transform(dXSLT, dInput);
		return XML.getText(dResult.getDocumentElement());
	}

	/**
	 * Does best effort to find a location (note: this doesn't actually help)
	 * @param e Exception that might have a location in, but won't
	 * @return String with the location, or "location unknown" (in every case)
	 */
	private static String findLocation(TransformerException e)
	{
		// Try for location in exception
		String sLocation=e.getLocationAsString();
		if(sLocation!=null) return sLocation;

		// Not there, try causes
		Throwable tCause=e.getCause();
		if(tCause!=null && (tCause instanceof TransformerException))
		  return findLocation((TransformerException)tCause);
		return "location unknown";
	}

	/** Apply a number of parameters (held in a Map) to a transformer */
	private static void setTransformerParameters(Transformer t, Map mParameters)
	{
		if (mParameters != null)
		{
			Iterator i = mParameters.keySet().iterator();
			while (i.hasNext())
			{
				String sKey = (String)i.next();
				Object oValue = mParameters.get(sKey);
				t.setParameter(sKey, oValue);
			}
		}
	}
}