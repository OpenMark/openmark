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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** Utilities related to strings and string formatting */
public class Strings
{
	private final static DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static Random random;

	private static synchronized Random getRandom() {
		if (random == null) {
			random = new Random();
		}
		return random;
	}

	/**
	 * Makes a string comprising the toString of all the elements of collection c,
	 * with a copy of border between each item.
	 * @param <T> Type of things in the collection.
	 * @param sBorder the separator to put between items of c
	 * @param c the collection to join.
	 * @return the joined string.
	 */
	public static <T> String join(String sBorder, Collection<T> c)
	{
		boolean started=false;
		StringBuffer sb=new StringBuffer();
		for (T item : c) {
			if(started) {
				sb.append(sBorder);
			} else {
				started=true;
			}
			sb.append(item.toString());
		}
		return sb.toString();
	}

	/**
	 * Split string on runs of , and space. Handle the empty case as you would expect.
	 *
	 * So, for example
	 *     "frog, toad" -> { "frog", "toad" }
	 *     "" -> {}
	 *     ",,,," -> {}
	 *
	 * @param string the string to split
	 * @return the separate items from the list.
	 */
	public static String[] splitSensibly(String string)
	{
		return splitSensibly(", ", string);
	}

	/**
	 * Split string on runs of the separator characters. Handle the empty case as you would expect.
	 *
	 * So, for example
	 *     ", ", "frog, toad" -> { "frog", "toad" }
	 *     ", ", "" -> {}
	 *     ", ", ",,,," -> {}
	 *
	 * @param separators the characters that are separators.
	 * @param string the string to split
	 * @return the separate items from the list.
	 */
	public static String[] splitSensibly(String separators, String string)
	{
		if (string.matches("^[" + separators + "]*$"))
		{
			return new String[0];
		}
		else
		{
			return string.split("[" + separators + "]+");
		}
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
	 * Replaces tokens in a string.
	 * @param string String to process
	 * @param sBorder Border string e.g. %%; must be made of characters that don't
	 *   need escaping in regexps
	 * @param mReplace Replacement map (containing strings only)
	 * @return the modified string.
	 */
	public static String replaceTokens(String string, String sBorder, Map<String, ? extends Object> mReplace)
	{
		// Get value and look for tokens. If there aren't any, bail now.
		if (mReplace == null || string.indexOf(sBorder) == -1)
		{
			return string;
		}

		Pattern pTokens=Pattern.compile(sBorder+"(.*?)"+sBorder, Pattern.DOTALL);

		StringBuffer sbResult=new StringBuffer();
		Matcher m = pTokens.matcher(string);
		while(m.find())
		{
			String sKey = m.group(1);
			if(sKey.equals(""))
			{
				m.appendReplacement(sbResult,sBorder);
			}
			else
			{
				String sMatch = (String) mReplace.get(sKey);
				if (sMatch == null)
				{
					m.appendReplacement(sbResult,sBorder+sKey+sBorder);
				}
				else
				{
					sMatch=sMatch.replaceAll("\\\\","\\\\\\\\").replaceAll("\\$","\\\\\\$");
					m.appendReplacement(sbResult, sMatch);
				}
			}
		}
		m.appendTail(sbResult);

		return sbResult.toString();
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

	/**
	 * Quotes a date for SQL.
	 * @param date the date/time to insert in a query.
	 * @return value ready to use.
	 */
	public static String sqlQuote(Calendar date)
	{
		if (date == null)
		{
			return "NULL";
		}
		else
		{
			return "'" + sqlDateFormat.format(date.getTime()) + "'";
		}
	}

	/** @return A random alphanumeric character */
	public static char randomAlNum()
	{
		Random r = getRandom();
		int i = 0;
		synchronized (r) {
			i = r.nextInt(62);
		}

		if(i<26)
			return (char)(i+'A');
		i-=26;
		if(i<26)
			return (char)(i+'a');
		i-=26;
			return (char)(i+'0');
	}

	/**
	 * @param length the length of the string to generate.
	 * @return an alphanumeric string of a given length.
	 */
	public static String randomAlNumString(int length)
	{
		StringBuffer sb = new StringBuffer(length);
		for(int i = 0 ; i < length; i++)
		{
			sb.append(randomAlNum());
		}
		return sb.toString();
	}

	/**
	 * @param length the length of the string to generate.
	 * @return a string of digits of a given length.
	 */
	public static String randomDigits(int length)
	{
		StringBuffer sb = new StringBuffer(length);
		Random r = getRandom();
		synchronized (r) {
			for(int i = 0 ; i < length; i++)
			{
				sb.append(r.nextInt(10));
			}
		}
		return sb.toString();
	}

	/**
	 * @param bytes
	 * @return a string of hex made from the bytes.
	 */
	public static String byteArrayToHexString(byte[] bytes) {
		StringBuffer sb = new StringBuffer(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {
			String bit = Integer.toHexString(bytes[i] + 0x100);
			sb.append(bit.substring(bit.length() - 2));
		}
		return sb.toString();
	}
	
	/**
	 * @param str
	 * @return true if the str is null or empty
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * @param str
	 * @return true if the str is null or empty
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	public static String uppercaseFirstCharacter(String initial) {
		String result = initial;
		if (Strings.isNotEmpty(initial) ? initial.length() > 0 : false) {
			String c = initial.substring(0, 1);
			result = c.toUpperCase() + initial.substring(1, initial.length());
		}
		return result;
	}
}