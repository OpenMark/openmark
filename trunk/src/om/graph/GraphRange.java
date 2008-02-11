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
package om.graph;

/** Represents a range in graph co-ordinate space */
public class GraphRange
{
	/** Range */
	private double dMin,dMax;

	/**
	 * Constructs a range. Ranges are inclusive and you can specify the ends in
	 * any order.
	 * @param d1 One end of range
	 * @param d2 Other end of range
	 */
	public GraphRange(double d1,double d2)
	{
		dMin=Math.min(d1,d2);
		dMax=Math.max(d1,d2);
	}

	/**
	 * Constructs from XML attribute.
	 * @param sRange Either a single double e.g. 0.0 or a pair with commas -0.1,0.3
	 * @throws GraphFormatException
	 */
	GraphRange(String sRange) throws GraphFormatException
	{
		try
		{
			int iComma=sRange.indexOf(',');
			if(iComma==-1)
			{
				dMin=Double.parseDouble(sRange);
				dMax=dMin;
			}
			else
			{
				double
					d1=Double.parseDouble(sRange.substring(0,iComma)),
					d2=Double.parseDouble(sRange.substring(iComma+1));
				dMin=Math.max(d1,d2);
				dMax=Math.max(d1,d2);
			}
		}
		catch(NumberFormatException nfe)
		{
			throw new GraphFormatException("<world>: Invalid range spec: "+sRange);
		}
	}

	/** @return Minimum end of range */
	public double getMin()
	{
		return dMin;
	}

	/** @return Maximum end of range */
	public double getMax()
	{
		return dMax;
	}

	/**
	 * @param dPoint Point to check
	 * @return True if it's within the range (including at either end)
	 */
	public boolean inRange(double dPoint)
	{
		return dPoint>=dMin && dPoint<=dMax;
	}
}
