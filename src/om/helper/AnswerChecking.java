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
package om.helper;

/** 
 * Static utility routines designed to help in checking user answers. 
 */
public abstract class AnswerChecking
{
	/** Generic result class that most methods return. */ 
	public static class Result
	{
		boolean bCorrect;
		/** @return True if user got answer right */
		public boolean isCorrect() { return bCorrect; }
		
		int iReason;
		/** @return A reason code if user got answer wrong; reason codes are
		 *    defined in individual Result subclasses. */
		public int getReason() { return iReason; }
	}
	
	/** Result of scientific notation check */ 
	public static class SciNotationResult extends Result
	{
		/** Input didn't contain a multiply sign */
		public final static int NOMULTIPLY=10;

		/** Mantissa (number before *) is wrong */
		public final static int WRONGMANTISSA=20;
		
		/** After *, doesn't have 10^ something */
		public final static int NOT10TOPOWER=30;

		/** Exponent is wrong */
		public final static int WRONGEXPONENT=40;
	}
	
	/**
	 * Checks for scientific notation of the form 1.4 * 10<sup>18</sup>. Allows
	 * several variants.
	 * @param sInput User's input
	 * @param sMantissa Correct number before the *
	 * @param sExponent Correct exponent after the 10^
	 * @return Indication of whether or not user's right
	 */
	public static SciNotationResult checkSciNotation(
		String sInput,String sMantissa,String sExponent)
	{
		SciNotationResult r=new SciNotationResult();
		
		// Strip whitespace and de-case-sensitise
		sInput=stripWhitespace(sInput).toLowerCase();
		
		// Find the multiply
		int iTimes=-1;
		iTimes=sInput.indexOf("\u00d7");
		if(iTimes==-1) iTimes=sInput.indexOf("*");
		if(iTimes==-1) iTimes=sInput.indexOf("x");
		
		if(iTimes==-1)
		{
			r.iReason=SciNotationResult.NOMULTIPLY;
			return r;
		}
		
		// Split string
		String 
			sBeforeMultiply=sInput.substring(0,iTimes),
			sAfterMultiply=sInput.substring(iTimes+1);
		
		// Check mantissa
		if(!checkString(sBeforeMultiply,sMantissa).isCorrect())
		{
			r.iReason=SciNotationResult.WRONGMANTISSA;
			return r;
		}
		
		// Look for 10<sup>...</sup>
		if(!sAfterMultiply.matches("10<sup>.*</sup>"))
		{
			r.iReason=SciNotationResult.NOT10TOPOWER;
			return r;
		}
		
		// Check exponent
		String sExponentInput=sAfterMultiply.replaceAll("10<sup>(.*)</sup>","$1");
		if(!checkString(sExponentInput,sExponent).isCorrect())
		{
			r.iReason=SciNotationResult.WRONGMANTISSA;
			return r;
		}
		
		r.bCorrect=true;
		return r;		
	}
	
	/**
	 * Checks against a precise string after stripping all whitespace.
	 * @param sInput Input string
	 * @param sExpected Expected string
	 * @return Result of check
	 */
	public static Result checkString(String sInput,String sExpected)
	{
		Result r=new Result();
		sInput=stripWhitespace(sInput);		
		r.bCorrect=sInput.equals(sExpected);
		return r;
	}

	/**
	 * Strips all whitespace characters from the input (not just at start and end).
	 * @param sInput Input text
	 * @return Same string but without any whitespace
	 */
	public static String stripWhitespace(String sInput)
	{
		StringBuffer sbResult=new StringBuffer();
		for(int i=0;i<sInput.length();i++)
		{
			char c=sInput.charAt(i);
			if(!Character.isWhitespace(c)) sbResult.append(c);
		}
		return sbResult.toString();	
	}
	
}
