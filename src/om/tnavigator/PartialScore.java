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

import java.util.*;

import om.OmFormatException;

/** 
 * Represents a score in the process of being calculated. These scores are
 * stored as double precision until they are rounded, when they are converted
 * to nearest integer (not rounded down).
 */
public class PartialScore
{
	/** Map from String (axis name/null for default) -> Double (score) */ 
	private Map<String,Axis> mAxes=new HashMap<String,Axis>();
	
	/** Score details on single axis */
	private static class Axis
	{
		double d;
		double dMax;
	}
	
	@Override
	protected Object clone()
	{
		PartialScore psNew=new PartialScore();
		for(Map.Entry<String,Axis> me : mAxes.entrySet())
		{
			Axis aNew=new Axis(),aOld=me.getValue();
			aNew.d=aOld.d;
			aNew.dMax=aOld.dMax;
			psNew.mAxes.put(me.getKey(),aNew);
		}
		return psNew;
	}

	/**
	 * Gets score on an axis.
	 * @param sAxis Axis name; null for default axis
	 * @return Score on that axis
	 * @throws OmFormatException If axis is not defined in this partial score
	 */
	double getScore(String sAxis) throws OmFormatException
	{
		Axis a=mAxes.get(sAxis);
		if(a==null) throw new OmFormatException("Axis not defined: "+sAxis);
		
		return a.d;
	}
	
	/**
	 * Gets maximum available marks on an axis.
	 * @param sAxis Axis name; null for default axis
	 * @return Maximum marks on that axis 
	 * @throws OmFormatException If axis is not defined in this partial score
	 */
	double getMax(String sAxis) throws OmFormatException
	{
		Axis a=mAxes.get(sAxis);
		if(a==null) throw new OmFormatException("Axis not defined: "+sAxis);
		
		return a.dMax;
	}
	
	/**
	 * @param sAxis Axis in question
	 * @return True if we have a score on that axis
	 */
	boolean hasScore(String sAxis)
	{
		return mAxes.containsKey(sAxis);
	}
	
	/**
	 * Sets score on an axis.
	 * @param sAxis Axis name
	 * @param d Score to set
	 * @param dMax Maximum possible score
	 */
	void setScore(String sAxis,double d,double dMax)
	{
		Axis a=new Axis();
		a.d=d;
		a.dMax=dMax;
		mAxes.put(sAxis,a);
	}
	
	/**
	 * Adds another partial score to this one.
	 * @param ps Score to add
	 */
	void add(PartialScore ps)
	{
		for(Iterator iAxis=ps.mAxes.entrySet().iterator();iAxis.hasNext();)
		{
			Map.Entry me=(Map.Entry)iAxis.next();
			String sName=(String)me.getKey();
			Axis a=(Axis)me.getValue();
			
			Axis aExisting=mAxes.get(sName);
			if(aExisting==null)
			{
				aExisting=new Axis();
				mAxes.put(sName,aExisting);
			}
			aExisting.d+=a.d;
			aExisting.dMax+=a.dMax;
		}
	}
	
	@Override
	public String toString()
	{
		StringBuffer sb=new StringBuffer();
		sb.append("[");
		boolean bFirst=true;
		for(Map.Entry<String,Axis> me : mAxes.entrySet())
		{
			String sName=me.getKey();
			if(sName==null) sName="<default>";
			Axis a=me.getValue();
			if(bFirst)
				bFirst=false;
			else 
				sb.append("; ");
			sb.append(sName+"="+a.d+" (of "+a.dMax+")");
		}
		sb.append("]");
		return sb.toString();
	}
	
	/** @return List of all axis names (including null for default) */
	String[] getAxes()
	{
		String[] asAxes=mAxes.keySet().toArray(new String[0]);
		// Need to sort axes so they're in reliable order for result tables etc.
		Arrays.sort(asAxes,new NullOKComparator<String>()); // Null axis comes first,
		return asAxes;
	}
	
	private static class NullOKComparator<T extends Comparable<T>> implements Comparator<T>
	{
		public int compare(T o1,T o2)
		{
			if(o1==null && o2==null) return 0;
			if(o1==null) return -1;
			if(o2==null) return 1;
			
			return o1.compareTo(o2);
		}
	}
}
