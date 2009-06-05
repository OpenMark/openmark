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
package om.tnavigator.scores;

import java.util.HashSet;
import java.util.Set;

import om.OmFormatException;

import org.w3c.dom.Element;

import util.xml.XML;

/**
 * A mapping of scores to scores. The output scores on each axis is computed by
 * combining a multiples of the scores on some or all of the input axes.
 *
 * That is, in mathematical terms, a score remapping is a linear map from the space of
 * scores to the space of scores, that preserves the positive space s >= 0 in the product order.
 */
public class ScoreRemapping
{
	// Details of how the scores on the new axes are calculated from the scores
	// on the old axes. If empty, we are the identity transform.
	private Set<RemappingPart> components;

	/**
	 * Constructs item.
	 * @param eThis XML tag for this item (that may contain &lt;rescore&gt; tags)
	 * @throws OmFormatException Any format error
	 */
	public ScoreRemapping(Element eThis) throws OmFormatException
	{
		Element[] rescoreElements = XML.getChildren(eThis, "rescore");
		components = new HashSet<RemappingPart>();
		for(int i = 0; i<rescoreElements.length; i++)
		{
			components.add(RemappingPart.fromXML(rescoreElements[i]));
		}
	}

	/**
	 * Computes a new score be remapping a base score.
	 * @param baseScore Current score
	 * @return the new score.
	 * @throws OmFormatException if the baseScore does not contain a score for a fromAxis
	 */
	public CombinedScore remap(CombinedScore baseScore) throws OmFormatException
	{
		// If no changes, we return the existing score unmolested, axes and all
		if (components.isEmpty()) return (CombinedScore)baseScore.clone();

		// OK, time to remap.
		CombinedScore newScore = new CombinedScore();
		for (RemappingPart component : components)
		{
			component.rescore(newScore,baseScore);
		}
		return newScore;
	}
}