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
package om.question;

import org.w3c.dom.Document;

import om.OmException;

/** Basic low-level interface that all questions must implement. */
public interface Question
{
	/**
	 * Called to initialise new question.
	 * @param d XML document containing question
	 * @param ip Initialisation parameters for question
	 * @return Information needed to render question as XHTML
	 * @throws OmException
	 */
	public Rendering init(Document d,InitParams ip) throws OmException;

	/**
	 * Called when an action is received for the question, i.e. when the user
	 * clicks a form submit button or similar.
	 * @param ap Parameters of action (e.g. form parameters)
	 * @return Revised information needed to render question as XHTML
	 * @throws OmException
	 */
	public ActionRendering action(ActionParams ap) throws OmException;
	
	/**
	 * Called when question is finished with, either after an ActionResponse is
	 * returned indicating that the question ended, or if the Web Presenter
	 * terminates the question early.
	 */
	public void close();
}
