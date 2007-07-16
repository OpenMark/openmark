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
package util.misc;

import java.util.LinkedList;
import java.util.List;

/** Represents named parameters that can be turned into two arrays */ 
public class NameValuePairs
{
	/** Names and values */
	private List<String> lNames=new LinkedList<String>();
	private List<String> lValues=new LinkedList<String>();
	
	/** @return Names array */
	public String[] getNames()
	{
		return lNames.toArray(new String[0]);
	}
	/** @return Values array */
	public String[] getValues()
	{
		return lValues.toArray(new String[0]);
	}
	/**
	 * Add new entry to arrays.
	 * @param sName Name
	 * @param sValue Value
	 */
	public void add(String sName,String sValue)
	{
		lNames.add(sName);
		lValues.add(sValue);
	}
	
	@Override
	public int hashCode()
	{		
		int iCode=0;
		String[] asNames=getNames(),asValues=getValues();
		for (int i=0; i < asNames.length; i++)
		{
			iCode+=asNames[i].hashCode()*(i+1);
			iCode+=31*asValues[i].hashCode()*(i+1);
		}
		return iCode;
	}
}