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
 * Stores an qustionId and version.
 */
public class QuestionName
{
	/** The question id. */
	private String questionId;

	/** The question version. */
	private QuestionVersion questionVersion;

	/**
	 * Constructor.
	 * @param name The question id
	 * @param qv The question version
	 */
	public QuestionName(String name, QuestionVersion qv) {
		questionId = name;
		questionVersion = qv;
	}

	/**
	 * @return the questionId.
	 */
	public String getQuestionId() {
		return questionId;
	}

	/**
	 * @return the question version.
	 */
	public QuestionVersion getQuestionVersion() {
		return questionVersion;
	}

	/**
	 * @return whether the name and version are valid.
	 */
	public boolean isValid() {
		return null != questionId && questionId.length() > 0 && null != getQuestionVersion();
	}

	@Override
	public String toString() {
		return questionId + "." + questionVersion;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof QuestionName)) {
			return false;
		}
		return toString().equals(obj.toString());
	}
}
