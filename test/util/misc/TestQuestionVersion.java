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

import util.misc.QuestionVersion;

public class TestQuestionVersion
{
	@Test public void testGetMajor()
	{
		QuestionVersion qv = new QuestionVersion(13, 14);
		Assert.assertEquals(13, qv.getMajor());
	}

	@Test public void testGetMinor()
	{
		QuestionVersion qv = new QuestionVersion(13, 14);
		Assert.assertEquals(14, qv.getMinor());
	}

	@Test public void testToString()
	{
		QuestionVersion qv = new QuestionVersion(13, 14);
		Assert.assertEquals("13.14", qv.toString());
	}

	@Test public void testHashCode()
	{
		QuestionVersion qv = new QuestionVersion(13, 14);
		Assert.assertEquals("13.14".hashCode(), qv.hashCode());
	}

	@Test public void testEqualsWrongType()
	{
		QuestionVersion qv = new QuestionVersion(13, 14);
		Assert.assertFalse(qv.equals(new Object()));
	}

	@Test public void testEqualsNotEqual()
	{
		QuestionVersion qv1 = new QuestionVersion(13, 14);
		QuestionVersion qv2 = new QuestionVersion(13, 15);
		Assert.assertFalse(qv1.equals(qv2));
	}

	@Test public void testEqualsEqual()
	{
		QuestionVersion qv1 = new QuestionVersion(13, 14);
		QuestionVersion qv2 = new QuestionVersion(13, 14);
		Assert.assertTrue(qv1.equals(qv2));
	}
}
