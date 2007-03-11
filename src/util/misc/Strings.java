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

import java.text.*;
import java.util.*;

/** Utilities related to strings and string formatting */
public class Strings
{
	/**
	 * Makes a string comprising the toString of all the elements of collection c,
	 * with a copy of border between each item.
	 * @param sBorder the separator to put between items of c
	 * @param c the collection to join.
	 * @return the joined string.
	 */
	public static String join(String sBorder,Collection c)
	{
		boolean started=false;
		StringBuffer sb=new StringBuffer();
		for(Iterator iter=c.iterator();iter.hasNext();)
		{
			if(started)
			{
				sb.append(sBorder);
			}
			else
			{
				started=true;
			}
			sb.append(iter.next().toString());
		}
		return sb.toString();
	}

	/**
	 * Converts an array of items into a textual list depending on the number of
	 * items:
	 * <ol>
	 * <li>a</li>
	 * <li>a and b</li>
	 * <li>a, b and c</li>
	 * <li>a, b, c and d</li>
	 * </ol>
	 * I believe this list now matches OU house style in omitting the comma before
	 * 'and'.
	 * @param asItems List of any number of string items (0..)
	 * @return String representing those items
	 */
	public static String displayList(String[] asItems)
	{
		if(asItems.length==0) return "";
		if(asItems.length==1) return asItems[0];
		
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<asItems.length;i++)
		{
			if(i==asItems.length-1)
				sb.append(" and ");
			else if(i>0)
				sb.append(", ");
			sb.append(asItems[i]);
		}
		return sb.toString();
	}
	
	/**
	 * Replaces text in a string. Like Java built-in replaceAll, but replaces only
	 * literal strings not regular expressions (so you don't have to escape
	 * anything). 
	 * @param sSource Source string
	 * @param sSearch Thing to search for
	 * @param sReplace Thing to replace with
	 * @return New string
	 */
	public static String replace(String sSource,String sSearch,String sReplace)
	{
		StringBuffer sb=new StringBuffer();
		while(true)
		{
			int iFound=sSource.indexOf(sSearch);
			if(iFound==-1)
			{
				sb.append(sSource);
				return sb.toString();
			}
			sb.append(sSource.substring(0,iFound));
			sb.append(sReplace);
			sSource=sSource.substring(iFound+sSearch.length());
		}		
	}
	
	/**
	 * @param iBytes Number of bytes
	 * @return String containing size and unit in sensible units
	 */
	public static String formatBytes(int iBytes)
	{
		if(iBytes<1024) return iBytes+" bytes";
		if(iBytes<1024*1024) return formatOneDecimal(iBytes,1024)+" KB";
		return formatOneDecimal(iBytes,1024*1024)+" MB";
	}
	
	/**
	 * @param lBytes Number of bytes
	 * @return String containing size and unit in sensible units
	 */
	public static String formatBytes(long lBytes)
	{
		return formatBytes((int)lBytes);
	}
	
	/** Format used for numbers to one DP. */
	private static final NumberFormat ONEDP = new DecimalFormat("#########0.0");
	/**
	 * @param iNumber
	 * @param iDivisor
	 * @return a string that is iNumber/iDivisor to one DP.
	 */
	public static String formatOneDecimal(int iNumber, int iDivisor)
	{
		if(iDivisor==0) return "??";
		return formatOneDecimal(iNumber/(double) iDivisor);
	}
	/**
	 * @param dNumber
	 * @return Formats the number to one DP.
	 */
	public static String formatOneDecimal(double dNumber)
	{
		return ONEDP.format(dNumber);
	}

	/**
	 * Quotes a string for SQL - doubles any inner quotes and add the two 
	 * surrounding quotes. Use this for *any* quote in SQL, even if sure
	 * the string doesn't have ' inside (just to be on the safe side). There 
	 * should be no ' character literals in SQL queries/update strings in this 
	 * code.
	 * @param s Input string
	 * @return String surrounded by single quotes and escaped as necessary
	 */
	public static String sqlQuote(String s)
	{
		if(s==null) 
			return "NULL";
		else 
			return "'"+s.replaceAll("'","''")+"'";
	}
}