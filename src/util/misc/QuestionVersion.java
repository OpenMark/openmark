/* OpenMark online assessment system
   Copyright (C) 2011 The Open University

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
package util.misc;

/**
 * Stores the version (major.minor) of a question.
 */
public class QuestionVersion
{
	/** The major version. */
	private int major;

	/** The minor version. */
	private int minor;

	/**
	 * Create a QuestionVersion, setting the fields.
	 * @param major
	 * @param minor
	 */
	public QuestionVersion(int major, int minor)
	{
		this.major = major;
		this.minor = minor;
	}

	/**
	 * The major version.
	 */
	public int getMajor() {
		return major;
	}

	/**
	 * The minor version.
	 */
	public int getMinor() {
		return minor;
	}

	@Override
	public String toString() {
		return major + "." + minor;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof QuestionVersion)) {
			return false;
		}
		return toString().equals(obj.toString());
	}
}
