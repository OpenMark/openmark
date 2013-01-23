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
package om.tnavigator.teststructure;

/**
 * This is the interface used by the navigation panel to get the
 * information it needs to display each actual thing in the test
 * (questions or text pages) in the appropriate state, and with a
 * section heading before each section.
 */
public interface TestLeaf
{
	/**
	 * @param s Section name for question (or null for none)
	 */
	void setSection(String s);

	/**
	 * @return Section name of question (null if not in a section)
	 */
	String getSection();

	/**
	 * @return True if user has finished the item
	 */
	boolean isDone();

	/**
	 * @return True if item is available for display
	 */
	boolean isAvailable();
}
