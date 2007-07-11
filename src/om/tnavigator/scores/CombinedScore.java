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

import java.util.*;

import om.OmFormatException;
import om.axis.qengine.Score;
import util.misc.NullOKComparator;

/**
 * The score for a question, section or an entire test. In OpenMark, a score
 * can ba a collection of different numerical marks each against a different
 * axis. Axes are identified by strings, with the default axis being the 
 * one identified by the empty string. This class records the actual score
 * and the maximum possible on each axis.
 * 
 * In mathematical language, a score is a point in the vector space over the
 * field of doubles with basis the set of all strings, and this class actually
 * stores two points score and max, with 0 &lt;= score &lt;= max in product order.
 */
public class CombinedScore
{
	// Map from String (axis name/null for default) to score out of maximum. 
	private Map<String, ScoreOnAxis> scores = new HashMap<String, ScoreOnAxis>();
	
	/**
	 * Make a new CombinedScore from a map of scores and an array of maximums.
	 * @param rawScores 
	 * @param maximums 
	 * @return the new CombinedScore.
	 */
	public static CombinedScore fromArrays(Map<String, Double> rawScores, Score[] maximums) {
		CombinedScore score = new CombinedScore();
		for(int iAxis=0; iAxis < maximums.length; iAxis++)
		{
			Double part = rawScores.get(maximums[iAxis].getAxis());
			if (part == null) part = 0.0; 
			score.scores.put(maximums[iAxis].getAxis(),
					new ScoreOnAxis(part, maximums[iAxis].getMarks()));
		}
		return score;
	}
	
	/**
	 * @param baseScore
	 * @return a Combined score with the same axes and maximums as baseScore,
	 * but a zero score on each axis.
	 */
	public static CombinedScore zeroScore(CombinedScore baseScore) {
		CombinedScore score = new CombinedScore();
		for (Map.Entry<String, ScoreOnAxis> me : baseScore.scores.entrySet())
		{
			score.scores.put(me.getKey(), new ScoreOnAxis(0, me.getValue().max));
		}
		return score;
	}
	
	/**
	 * @param baseScore
	 * @return a Combined score with the same axes and maximums as baseScore,
	 * but a maximum score on each axis.
	 */
	public static CombinedScore maxScore(CombinedScore baseScore) {
		CombinedScore score = new CombinedScore();
		for (Map.Entry<String, ScoreOnAxis> me : baseScore.scores.entrySet())
		{
			double max = me.getValue().max;
			score.scores.put(me.getKey(), new ScoreOnAxis(max, max));
		}
		return score;
	}
	
	/**
	 * @param baseScore
	 * @param maximums 
	 * @return a Combined score where the scores are taken from the scores of baseScore,
	 * band the maximums are taken from the scores of maximums.
	 * @throws OmFormatException 
	 */
	public static CombinedScore scoreOutOfMax(CombinedScore baseScore, CombinedScore maximums)
			throws OmFormatException {
		CombinedScore score = new CombinedScore();
		for (Map.Entry<String, ScoreOnAxis> me : baseScore.scores.entrySet())
		{
			String axis = me.getKey();
			score.scores.put(axis, new ScoreOnAxis(me.getValue().score,
					maximums.getScore(axis)));
		}
		return score;
	}
	
	/**
	 * Gets score on an axis.
	 * @param axis Axis name; null for default axis.
	 * @return Score on that axis.
	 * @throws OmFormatException If there is no score against this axis in this score.
	 */
	public double getScore(String axis) throws OmFormatException
	{
		ScoreOnAxis part = scores.get(axis);
		if (part == null) throw new OmFormatException("No score for axis: "+axis);
		
		return part.score;
	}
	
	/**
	 * Gets maximum available marks on an axis.
	 * @param axis Axis name; null for default axis
	 * @return Maximum marks on that axis 
	 * @throws OmFormatException If there is no score against this axis in this score.
	 */
	public double getMax(String axis) throws OmFormatException
	{
		ScoreOnAxis part = scores.get(axis);
		if(part == null) throw new OmFormatException("No score for axis: "+axis);
		
		return part.max;
	}
	
	/**
	 * @param axis Axis name; null for default axis.
	 * @return True if we have a score on that axis
	 */
	public boolean hasScore(String axis)
	{
		return scores.containsKey(axis);
	}
	
	/**
	 * @return the set of axes we have scores against.
	 */
	public Set<String> getAxes() {
		return scores.keySet();
	}

	/**
	 * @return List of all axis names (including null for default) in 
	 * order, default axis first, then the normal String order.
	 */
	public String[] getAxesOrdered()
	{
		String[] asAxes=scores.keySet().toArray(new String[0]);
		// Need to sort axes so they're in reliable order for result tables etc.
		Arrays.sort(asAxes,new NullOKComparator<String>()); // Null axis comes first,
		return asAxes;
	}
	
	/**
	 * @return the set of all contributions on all axes.
	 */
	Set<Map.Entry<String, ScoreOnAxis>> getParts() {
		return scores.entrySet();
	}
	
	/**
	 * Sets score on an axis.
	 * @param axis Axis name
	 * @param score Score to set
	 * @param max Maximum possible score
	 */
	void setScore(String axis, double score, double max)
	{
		ScoreOnAxis newPart=new ScoreOnAxis(score, max);
		scores.put(axis,newPart);
	}
	
	/**
	 * Add a bit more score to a particular axis.
	 * @param axis
	 * @param otherScore
	 */
	void add(String axis, ScoreOnAxis otherScore) {
		ScoreOnAxis score = scores.get(axis);
		if(score == null)
		{
			scores.put(axis, new ScoreOnAxis(otherScore));
		} else {
			score.add(otherScore);
		}
	}
	
	/**
	 * Add a bit more score to a particular axis.
	 * @param axis
	 * @param otherScore
	 * @param otherMax
	 */
	void add(String axis, double otherScore, double otherMax) {
		ScoreOnAxis score = scores.get(axis);
		if(score == null)
		{
			scores.put(axis, new ScoreOnAxis(otherScore, otherMax));
		} else {
			score.add(otherScore, otherMax);
		}
	}
	
	/**
	 * Adds another score to this one.
	 * @param otherScore Score to add
	 */
	public void add(CombinedScore otherScore)
	{
		for (Map.Entry<String, ScoreOnAxis> me : otherScore.getParts())
		{
			add(me.getKey(), me.getValue());
		}
	}
	
	@Override
	protected Object clone()
	{
		CombinedScore newScore=new CombinedScore();
		for(Map.Entry<String,ScoreOnAxis> me : scores.entrySet())
		{
			newScore.scores.put(me.getKey(),new ScoreOnAxis(me.getValue()));
		}
		return newScore;
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		boolean first = true;
		
		for(Map.Entry<String,ScoreOnAxis> me : scores.entrySet())
		{
			String name=me.getKey();
			if (name == null) name = "<default>";
			ScoreOnAxis part = me.getValue();
			if(first)
				first = false;
			else 
				sb.append("; ");
			sb.append(name + "=" + part.score + " (of " + part.max + ")");
		}
		
		sb.append("]");
		return sb.toString();
	}
}