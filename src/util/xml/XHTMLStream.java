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
import java.util.LinkedList;

import javax.servlet.http.*;

import org.w3c.dom.Element;

/** Handles XHTML content streamed out to a Writer */
public class XHTMLStream
{
	/** Actual writer used to output data */
	private Writer w;

	/** Stack of tags that need to be closed */
	private LinkedList<String> llTagStack=new LinkedList<String>();

	/**
	 * Set up automatically for the given servlet parameter and write the
	 * beginning of the XHTML file.
	 * @param request Request (used to get Accept header)
	 * @param response Response (MIME type will be set)
	 * @param e Element from which root attributes are taken (may be null)
	 * @throws IOException Any problem writing to stream
	 */
	public XHTMLStream(
		HttpServletRequest request,HttpServletResponse response,Element e)
		throws IOException
	{
		// Write the start of the XHTML file
		boolean bXHTML=XHTML.setContentType(request,response);
		response.setCharacterEncoding("UTF-8");
		w=response.getWriter();
		XHTML.writePrologue(w,bXHTML,"en",e);
		llTagStack.addLast("html");
	}

	/**
	 * Set up to write XHTML tags, not necessarily at the beginning of the file.
	 * @param w Wrtier to use
	 */
	public XHTMLStream(Writer w)
	{
		this.w=w;
	}

	/**
	 * Opens a tag with no attributes.
	 * @param sTag Tag name
	 * @throws IOException 
	 */
	public void tag(String sTag) throws IOException
	{
		w.write("<"+sTag+">");
		llTagStack.addLast(sTag);
	}

	/**
	 * Opens a tag with attributes.
	 * @param sTag Tag name
	 * @param sAttributes List of attributes exactly as it should be written to
	 *   the output, after the tag name and a space
	 * @throws IOException 
	 */
	public void tag(String sTag,String sAttributes) throws IOException
	{
		w.write("<"+sTag+" "+sAttributes+">");
		llTagStack.addLast(sTag);
	}

	/**
	 * Writes a tag with (optional) attributes, and containing text.
	 * @param sTag Tag name
	 * @param sAttributes List of attributes exactly as it should be written to
	 *   the output, after the tag name and a space.  null to omit
	 * @param sText Text to insert within the tag.  null to omit (this will add
	 * a closing tag with no space, rather than an empty tag).
	 * @throws IOException 
	 */
	public void completeTag(String sTag,String sAttributes, String sText) throws IOException
	{
		w.write("<"+sTag);
		if (sAttributes != null)
		{
		    w.write(" "+sAttributes);
		}
		w.write(">");
		if (sText != null)
		{
		    w.write(XHTML.escape(sText, XHTML.ESCAPE_TEXT));
		}
		w.write("</" + sTag + ">");
	}

	/**
	 * Writes a comment
	 * @param sComment Comment text
	 * @throws IOException 
	 */
	public void comment(String sComment) throws IOException
	{
		w.write("<!-- "+sComment+" -->");
	}

	/**
	 * Writes an empty tag with attributes.
	 * @param sTag Tag name
	 * @param sAttributes List of attributes exactly as it should be written to
	 *   the output, after the tag name and a space
	 * @throws IOException 
	 */
	public void empty(String sTag,String sAttributes) throws IOException
	{
		w.write("<"+sTag+" "+sAttributes+" />");
	}

	/**
	 * Writes an empty tag with no attributes.
	 * @param sTag Tag name
	 * @throws IOException 
	 */
	public void empty(String sTag) throws IOException
	{
		w.write("<"+sTag+" />");
	}


	/**
	 * Closes a tag.
	 * @throws IOException
	 */
	public void untag() throws IOException
	{
		String sTag=llTagStack.removeLast();
		w.write("</"+sTag+">");
	}

	/**
	 * Finishes off the XHTML file, optionally closing the writer.
	 * @param bClose If true, closes the writer
	 * @throws IOException Any error
	 */
	public void finish(boolean bClose) throws IOException
	{
		while(!llTagStack.isEmpty()) untag();
		if(bClose) w.close();
	}

	/**
	 * Writes a string to the stream. (String is automatically escaped.)
	 * @param s String to write
	 * @throws IOException 
	 */
	public void write(String s) throws IOException
	{
		w.write(XHTML.escape(s,XHTML.ESCAPE_TEXT));
	}


	/** Flushes output to the stream 
	 * @throws IOException */
	public void flush() throws IOException
	{
		w.flush();
	}
}
