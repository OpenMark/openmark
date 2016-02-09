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

import org.junit.Assert;
import org.junit.Test;

import util.misc.QuestionName;

public class TestQuestionName
{
	private QuestionVersion qv = new QuestionVersion(13, 14);

	@Test public void testGetMajor()
	{
		QuestionName question = new QuestionName("example.question", qv);
		Assert.assertEquals("example.question", question.getQuestionId());
	}

	@Test public void testGetMinor()
	{
		QuestionName question = new QuestionName("example.question", qv);
		Assert.assertEquals(qv, question.getQuestionVersion());
	}

	@Test public void testToString()
	{
		QuestionName question = new QuestionName("example.question", qv);
		Assert.assertEquals("example.question.13.14", question.toString());
	}

	@Test public void testHashCode()
	{
		QuestionName question = new QuestionName("example.question", qv);
		Assert.assertEquals("example.question.13.14".hashCode(), question.hashCode());
	}

	@Test public void testEqualsWrongType()
	{
		QuestionName question = new QuestionName("example.question", qv);
		Assert.assertFalse(question.equals(new Object()));
	}

	@Test public void testEqualsNotEqualDifferentId()
	{
		QuestionName question1 = new QuestionName("example.question", qv);
		QuestionName question2 = new QuestionName("example.question2", qv);
		Assert.assertFalse(question1.equals(question2));
	}

	@Test public void testEqualsNotEqualDifferentVersion()
	{
		QuestionName question1 = new QuestionName("example.question", qv);
		QuestionName question2 = new QuestionName("example.question", new QuestionVersion(13, 15));
		Assert.assertFalse(question1.equals(question2));
	}

	@Test public void testEqualsEqual()
	{
		QuestionName question1 = new QuestionName("example.question", qv);
		QuestionName question2 = new QuestionName("example.question", qv);
		Assert.assertTrue(question1.equals(question2));
	}
}
