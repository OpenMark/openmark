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
package util.servicebalancer;

/** Class provides a moving average that tracks a buffer of N integers */
public class MovingAverage
{
	/** Values in buffer */
	private int[] aiValues;
	/** Location in buffer */
	private int iCursor=0;
	/** Number of entries so far */
	private int iEntries=0;

	/**
	 * @param iSize Buffer size (number of integers to remember)
	 */
	public MovingAverage(int iSize)
	{
		aiValues=new int[iSize];
	}

	/**
	 * Adds new value to buffer (discarding the oldest one if necessary)
	 * @param iValue New value
	 */
	public synchronized void add(int iValue)
	{
		aiValues[iCursor++]=iValue;
		iCursor%=aiValues.length;
		iEntries++;
	}

	/**
	 * Adds new value to buffer (discarding the oldest one if necessary)
	 * @param lValue New value (will be converted to an int first)
	 */
	public void add(long lValue)
	{
		add((int)lValue);
	}

	/**
	 * Obtains average value of entries so far. If there are no entries, returns
	 * 0.0.
	 * @return Current average
	 */
	public synchronized double get()
	{
		double dTotal=0;
		int i;
		for(i=0;i<aiValues.length && i<iEntries;i++)
		{
			dTotal+=aiValues[i];
		}
		if(i==0) return 0.0;

		return dTotal/i;
	}
}