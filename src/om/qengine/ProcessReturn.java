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
import om.question.*;
import util.xml.XHTML;


/**
 * Returned data from {@link OmService#process(java.lang.String, java.lang.String[], java.lang.String[])} call.
 * <p>
 * API CLASS: This class is used in SOAP returns and should probably not be
 * altered (after initial release).
 */
public class ProcessReturn
{
	private boolean bQuestionEnd;

	private String sXHTML,sCSS,sProgressInfo;
	private Resource[] arResources;

	private Results r;

	ProcessReturn(ActionRendering ar)
	{
		bQuestionEnd=ar.isSessionEnd();
		try
		{
			if(ar.getXHTML()==null)
				sXHTML=null;
			else
				sXHTML=XHTML.saveString(ar.getXHTML());
		}
		catch(IOException ioe)
		{
			throw new OmUnexpectedException(ioe);
		}
		sCSS=ar.getCSS();
		sProgressInfo=ar.getProgressInfo();
		arResources=ar.getResources();
		r=ar.getResults();
	}

	/**
	 * @return Results from this question if provided this time; null if
	 *   not provided. (Results are sometimes provided before the end of the
	 *   question, if the last page shows answers etc.)
	 */
	public Results getResults() { return r; }

	/**
	 * @return True if the question has now ended and the test navigator should
	 * proceed to show a new question.
	 */
	public boolean isQuestionEnd() { return bQuestionEnd; }

	/**
	 * Obtains new XHTML content, which replaces the previous content. See
	 * {@link StartReturn#getXHTML()} for full description. Must be null if
	 * isQuestionEnd() returns true, not-blank otherwise.
	 * @return XHTML content as string.
	 */
	public String getXHTML() { return sXHTML; }
	/** @return CSS file. Null if no change from previous. */
	public String getCSS() { return sCSS; }
	/** @return Resource files that should be made available. These apply
	 *   in addition to previously-sent resources, not instead; they replace
	 *   previous resources if the filename is the same. */
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