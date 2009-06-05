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
package om.qengine;

import java.io.IOException;

import om.OmUnexpectedException;
import om.question.Rendering;
import om.question.Resource;
import util.xml.XHTML;


/**
 * Returned data from {@link OmService#start(String,String,String,String[],String[],String[])} call.
 * Wraps information from Rendering.
 * <p>
 * API CLASS: This class is used in SOAP returns and should probably not be
 * altered (after initial release).
 */
public class StartReturn
{
	private String sQuestionSession,sXHTML,sCSS,sProgressInfo;
	private Resource[] arResources;

	StartReturn(String sQuestionSession,Rendering r)
	{
		this.sQuestionSession=sQuestionSession;
		this.sProgressInfo=r.getProgressInfo();
		try
		{
			sXHTML=XHTML.saveString(r.getXHTML());
		}
		catch(IOException ioe)
		{
			throw new OmUnexpectedException(ioe);
		}
		sCSS=r.getCSS();
		arResources=r.getResources();
	}

	/** @return Question session ID.
	 * (Not a user session ID! Used to refer to the period between a start() call
	 * and the end of the question or stop().) */
	public String getQuestionSession() { return sQuestionSession; }
	/**
	 * Obtains XHTML content. XHTML content must:
	 * <ul>
	 * <li> Be well-formed, with single root element</li>
	 * <li> Be suitable for placing within &lt;body&gt;</li>
	 * <li> Not use named entities except the basic XML set.</li>
	 * </ul>
	 * The following placeholder strings may be included in the content and
	 * will be replaced by the test navigator:
	 * <table border="1">
	 * <tr><th>Placeholder</th><th>Replacement</th></tr>
	 * <tr><td>%%RESOURCE%%</td><td>Path [relative or absolute] at which
	 *   resources will become available. This should not include the
	 *   terminating /.<br>Example: If a resource has the name myfile.png,
	 *   then &lt;img src="%%RESOURCES%%/myfile.png"/&gt; should work to include
	 *   that image.</td></tr>
	 * <tr><td>%%IDPREFIX%%</td><td>must be put at the start of all id and name attributes
	 * in the XHTML (and references to them in Javascript.</td></tr>
	 * </table>
	 * @return XHTML content as string.
	 */
	public String getXHTML() { return sXHTML; }
	/** @return CSS file. Null if none is required. */
	public String getCSS() { return sCSS; }
	/** @return Resource files that should be made available */
	public Resource[] getResources() { return arResources; }

	/**
	 * @return Short textual information of progress on question, which should
	 *   be displayed alongside the question. (For example, this might indicate
	 *   how many attempts at the question are remaining.)
	 *   Null indicates no change from previously-returned info (or blank if this
	 *   is the first time).
	 */
	public String getProgressInfo() { return sProgressInfo; }
}