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

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/** 
 * Implemented by code that uses LogProcessor to process each log entry in
 * turn.
 */
public interface LogProcessorHandler
{
	/**
	 * Called when the log begins. The 'log' element is passed in here.
	 * @param e Log element (no contents apart from the attributes)
	 * @throws SAXException 
	 */
	void start(Element e) throws SAXException;

	/**
	 * Called after the log has finished.
	 * @throws SAXException 
	 */
	void finish() throws SAXException;

	/**
	 * Called for each log entry.
	 * @param e Entry tag and all children
	 * @throws SAXException 
	 */
	void entry(Element e) throws SAXException;
}
