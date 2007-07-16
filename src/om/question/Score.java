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


/** Single score */
public class Score
{
	/**
	 * Create a score
	 * @param sAxis the axis.
	 * @param iMarks the score on that axis.
	 */
	public Score(String sAxis,int iMarks)
	{
		this.sAxis=sAxis;
		this.iMarks=iMarks;
	}

	private int iMarks;
	private String sAxis;

	/** @return Score axis (null for default) */
	public String getAxis() { return sAxis; }

	/** @return Number of marks achieved for question (or, maximum for this axis,
	 * in that context) */
	public int getMarks() { return iMarks; }
}