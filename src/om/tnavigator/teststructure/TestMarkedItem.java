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

import om.OmFormatException;
import om.tnavigator.scores.CombinedScore;
import om.tnavigator.scores.ScoreRemapping;

import org.w3c.dom.Element;


/**
 * Test items that (may) contribut a score towards the test.
 * Handles remapping of the raw scores returned by this test item
 * into the scores that this this item should report to the outside world.
 */
abstract class TestMarkedItem extends TestItem
{
	/** Each rescore tag */
	private ScoreRemapping scoreRemapping;

	/**
	 * Constructs item.
	 * @param iParent Parent or null if none
	 * @param eThis XML tag for this item (that may contain &lt;rescore&gt; tags)
	 * @throws OmFormatException Any format error
	 */
	TestMarkedItem(TestItem iParent,Element eThis) throws OmFormatException
	{
		super(iParent,eThis);
		scoreRemapping = new ScoreRemapping(eThis);
	}

	/**
	 * Uses the details from &lt;rescore&gt; elements to adjust the score
	 * calculated thus far, returning a new score.
	 * @param ps Current score
	 */
	CombinedScore rescore(CombinedScore ps) throws OmFormatException
	{
		return scoreRemapping.remap(ps);
	}

	/**
	 * @param sOnly If non-null, counts only the final score contribution from
	 *   the given ID'd question
	 * @param bMax If true, forces all questions that are counted (usually just
	 *   sOnly) to have max contribution
	 * @return The calculated score resulting from this item
	 * @throws OmFormatException
	 */
	abstract CombinedScore getFinalScore(String sOnly,boolean bMax) throws OmFormatException;
}