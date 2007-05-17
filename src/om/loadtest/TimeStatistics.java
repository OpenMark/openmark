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
package om.loadtest;

import java.util.Arrays;

/** Keeps track of a time statistic */
public class TimeStatistics
{
	/** Size of array */
	private int iSize=0;
	
	/** Array of times */
	private int[] aiTimes=new int[1024];
	
	/**
	 * Adds a new value.
	 * @param iTime Time (milliseconds)
	 */
	public synchronized void add(int iTime)
	{
		if(iSize>=aiTimes.length)
		{
			int[] aiNew=new int[aiTimes.length*2];
			System.arraycopy(aiTimes,0,aiNew,0,aiTimes.length);				
			aiTimes=aiNew;
		}
		aiTimes[iSize++]=iTime;
	}
	
	/**
	 * Adds a new value.
	 * @param lTime Time (milliseconds)
	 */
	public void add(long lTime)
	{
		add((int)lTime);
	}
	
	/** 
	 * @return An array of 21 values. value 0 is minimum; 20 is maximum;
	 *   and 10 is the median. Others are at corresponding 5% points.
	 */ 
	public synchronized int[] getMedians()
	{
		Arrays.sort(aiTimes,0,iSize);
		int PORTIONS=20;
		int[] aiResults=new int[PORTIONS+1];
		for(int i=0;i<PORTIONS;i++)
		{
			aiResults[i]=aiTimes[(iSize*i) / PORTIONS];				
		}
		aiResults[PORTIONS]=aiTimes[iSize-1];
		return aiResults;
	}
	
	/** @return Mean time */
	public synchronized double getMean()
	{
		double dTotal=0.0;
		for(int i=0;i<iSize;i++)				
		{
			dTotal+=aiTimes[i];
		}
		return dTotal/iSize;
	}
	
	/** @return Tab-separated: mean followed by 20 medians */
	@Override
	public String toString()
	{
		StringBuffer sb=new StringBuffer();
		sb.append(getMean());
		int[] ai=getMedians();
		for(int i=0;i<ai.length;i++)
		{
			sb.append("\t");
			sb.append(ai[i]);
		}
		return sb.toString();
	}
}