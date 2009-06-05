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
/*
 * Title:       framework.Helper
 * Copyright:   � Open University 2002
 * Created by:  Pete Mitton
 * Date:        May/June 2002
 */

package samples.shared;

/**
 *  Description:        Assorted methods that don't really fit anywhere else
 *
 *  @author             $Author: gpb7 $
 *  @version            $Revision: 1.12 $ $Date: 2007/01/05 10:19:22 $
 **/
public class Helper {

	///////////////////////////////////////////////////////////////////////////

    /**
     *  Extracts the first number from a sequence of characters
     *  Assumes ScientificNotationToE() has been called first
     *
     *  Allows the returned string to be parsed as a double by Double.parseDouble( String string )
     *  @param str The string to be parsed
     *  @return If the string is abc1.23e-7ms<sup>-1<sup/> then 1.23e-7 is extracted.
     *  If no number is found an empty string is returned.
     **/

    public static String extractNumber(String str)
    {
        boolean			done;
        boolean			decimalFound = false;
        boolean			eFound = false;
        char			ch;
        int				sptr;
        int				length;
        int 			startOfNumber = 0, endOfNumber = 0, exponentPosition;
        StringBuffer 	sb = new StringBuffer(64);

        length = str.length();

        if (length == 0)
        	return(sb.toString());

        // ok string contains characters; look for start of number

        // are there any non-numerics at start of string
    	sptr = -1;
    	done = false;
    	do {
    		++sptr;
    	   	ch = str.charAt(sptr);
    		// check for number
    	   	if (Character.isDigit(ch)) {
    			done = true;
    	    	startOfNumber = sptr;
    			// check if preceeded by '.'
    			if (startOfNumber > 0) {
    				ch = str.charAt(startOfNumber-1);
    				if (ch == '.') {
    					// move pointer into string back by one;
    					--startOfNumber;
    					decimalFound = true;
    				}
    			}
    			// check if preceeded by + or -
    			if (startOfNumber > 0) {
    				ch = str.charAt(startOfNumber-1);
    				if ((ch == '+') || (ch == '-')) {
    					// move pointer into string back by one;
    					--startOfNumber;
    				}
    			}
    	   }
    	} while (!done && (sptr < (length - 1)));

    	if (!done) {	// no numeric found; return empty string
    	   	return(sb.toString());
    	}

    	// is first character also last character
    	if (sptr == (length - 1)) {
    		endOfNumber = length;
    	}
    	else {
    		done = false;
    		do {
    			++sptr;
    			ch = str.charAt(sptr);
    			// check for not a number number
    			if (!Character.isDigit(ch)) {
    				done = true;
    				// end of number now found unless character is . or E or e
    				// is it the decimal point?
    				if (!decimalFound) {
    					if (ch == '.') {
    						decimalFound = true;
    						done = false;
    					}
    				}
    				if (!eFound) {
    					if ((ch == 'e') || (ch == 'E')) {
    						// for this to be an exponent we must have the sequence
    						// e<digit>, e+<digit> or e-<digit>
    						exponentPosition = sptr;
							++sptr;
    						ch = str.charAt(sptr);
    						if ((ch == '+') || (ch == '-')) {
    							++sptr;
        						ch = str.charAt(sptr);
    						}
    						if (Character.isDigit(ch)) {
    							eFound = true;
    							done = false;
    						}
    						else {
    							// set sptr back to pointing at letter e which is now seen
    							// to be just a letter
    							sptr = exponentPosition;
    						}
    					}
    				}
    			}
    		} while (!done && (sptr < (length - 1)));

    		if (done)
    			endOfNumber = sptr;
    		else // last character in string is a digit; endOfNumber needs to point one beyond
    			endOfNumber = length;
    	}

    	if (endOfNumber == length)
			sb.append(str.substring(startOfNumber));
		else
			sb.append(str.substring(startOfNumber, endOfNumber));
	   	return(sb.toString());
    }
    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Obtains a number from a string
     *
     *  @param input The initial string
     *  @return The number or Double.NaN if no number entered
     **/

    public static double inputNumber(String str)
    {
    	double	ldbl;
    	String lstr;

    	lstr = extractNumber(str);
    	try {
    		ldbl=Double.parseDouble(lstr);
    	}
    	catch (NumberFormatException nfe)
    	{
    		// If they typed something that wasn't a number, just treat as wrong answer
    		ldbl = Double.NaN;
    	}
    	return(ldbl);
    }
    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Obtains a number, which may be in scientific notation(x10<sup>exp</sup>, from a string
     *
     *  @param input The initial string
     *  @return The number or Double.NaN if no number entered
     **/
    public static double inputScientificNumber(String str)
    {
    	double dbla;
    	String rsp;

		rsp = scientificNotationToE(str);
		dbla = inputNumber(rsp);

		return(dbla);
    }
    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Replaces a substring from the beginning of a string
     *  Useful for adding '1x' when users just enter 10<sup>power</sup>
     *  before reading the answer as scientific notation
     *
     *  @param  The initial string
     *  @return The initial string with '1x' inserted
     **/

    public static String insertAtStartOfString(String str, String toStartWith, String toInsert)
    {
    	String	str2= "";

    	// check if main string is longer than string to be removed
    	if (toStartWith.length() < str.length()) {
    		// dos the string start with toStartWith?
    		if (str.substring(0, toStartWith.length()).equalsIgnoreCase(toStartWith)) {
    			str2 = toInsert + str;
    		}
    		else
    			str2 = str;
    	}
    	return str2;
    }
    ///////////////////////////////////////////////////////////////////////////

    /**
     * To be in scientific notation a number must be in the form
     * 1E2, 1e2, 1.00E2, 1.00e2 not forgeting minus numbers
     *
     * Numbers of the sort 10E1 or 10.0e1 are not scientific notation as is 100 etc
     *
     * Note that the number part of the input should already have been isolated
     * and scientific input of the type *10 etc converted to E notation before
     * being passed to this method.
     *
     *  @param  The string to be checked.
     *  @return True if a number in scientific notation is present
     **/

    public static boolean isScientificNotation(String numberStr)
    {
    	boolean scientific = false;

    	if ((numberStr != null) && (numberStr.length() >= 3)) {
    		int plusMinusAtStart = 0;
    	    if ((numberStr.charAt(0) == '+') || (numberStr.charAt(0) =='-')) {
    	          plusMinusAtStart = 1;
    	    }

    	    char current = ' ';
    	    int eFoundPoint = 0;
    	    boolean eFound = false;

    	    //first character or first character after any + or - symbols must be a number
    	    //between 1 and 9 inclusive
    	    current = numberStr.charAt(0 + plusMinusAtStart);
    	    if (Character.isDigit(current) && (current != '0')) {
    	    	//next character after the first digit must be . e or E
    	        current = numberStr.charAt(1 + plusMinusAtStart);
    	        if ((current == 'E') || (current == 'e')) {
    	        	eFound = true;
    	            eFoundPoint = 2 + plusMinusAtStart;
    	        }
    	        else if ((current == '.') && (numberStr.length() >= 4)) {
    	        	// if there is a stop, next character must be a digit or an e
    	            current = numberStr.charAt(2 + plusMinusAtStart);
    	            if (Character.isDigit(current) || (current == 'E') || (current == 'e')) {
    	                eFoundPoint = 2 + plusMinusAtStart;
    	                while (!eFound && (eFoundPoint < numberStr.length())) {
    	                	current = numberStr.charAt(eFoundPoint++);
    	                	if (current == 'E' || current == 'e') {
    	                		eFound = true;
    	                	}
    	                }
    	            }
    	        }
    	        // if number is of the type 1e 1.e or 1.0E then check to see if there
    	        // is  + - or digit after
    	        if (eFound) {
    	        	current = numberStr.charAt(eFoundPoint);
    	            if ((eFoundPoint < numberStr.length()) && Character.isDigit(current)) {
    	            	scientific = true;
    	            }
    	            else if (eFoundPoint < (numberStr.length() - 1)) {
    	            	if ((current == '+') || (current =='-')) {
    	            		current = numberStr.charAt((eFoundPoint + 1));
    	                    if (Character.isDigit(current)){
    	                    	scientific = true;
    	                    }
    	                }
    	            }
    	        }
    	    }
    	}
    	return scientific;
    }
    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Writes a number in scientific notation to a specified number of significant figures
     *
     *  @param input the number and the number of significant figures
     *  @return the number as a string in scientific notation
     **/
    public static String outputScientificNumber(double dbl, int sf)
    {
    	boolean	positive = true;
    	int		i, length;
    	double	ldbl;
    	String  format, lstr, output;

    	ldbl = dbl;
    	if (ldbl < 0) {
    		positive = false;
    		ldbl = -ldbl;
    	}
    	// create E format in form 0.#[#]E0"
    	format = "0.";
    	for (i = 1; i < sf; i++) {
    		format = format + "#";
    	}
    	format = format + "E0";

    	java.text.DecimalFormat df = new java.text.DecimalFormat(format);

    	lstr = df.format(ldbl);
    	length = lstr.length();
    	i = lstr.indexOf("E");

    	if (positive)
    		output = lstr.substring(0, i) + " � 10^{" + lstr.substring(i+1, length) + "}";
    	else
    		output = "�" + lstr.substring(0, i) + " � 10^{" + lstr.substring(i+1, length) + "}";

    	return(output);
    }
    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Checks if a number is in a given range
     *
     *  @param  The number to be checked. The centre of the range.
     *  		The tolerance either side of the centre.
     *  @return True if within range, false otherwise
     **/
    public static boolean range(double testAnswer, double target, double tolerance)
    {
    	if ( (testAnswer >= (target - tolerance)) &&
    		 (testAnswer <= (target + tolerance)) ) {
    		return(true);
    	}
    	else {
    		return(false);
    	}
    }
    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Checks if a number is of the correct value apart from its magnitude.
     *  Routine checks up and down by factors of 10^1 to 10^10 and 10^-1 to 10^-10
     *  respectively.
     *
     *  @param  The number to be checked. The centre of the correct range.
     *  		The tolerance either side of the centre.
     *  @return True if within range modified by a factor of 10, false otherwise
     **/
    public static boolean rangeButWrongFactorOf10(double testAnswer, double target, double tolerance)
    {
    	int		i;
    	double	ltarget, ltolerance;

    	// check powers of ten 10^10 up
    	for (i = 1; i <= 10; ++ i) {
    		ltarget = target * Math.pow(10.0, (double) i);
    		ltolerance = tolerance * Math.pow(10.0, (double) i);
        	if ( (testAnswer >= (ltarget - ltolerance)) &&
           		 (testAnswer <= (ltarget + ltolerance)) ) {
           		return(true);
           	}
    	}
    	// check powers of ten 10^10 down
    	for (i = 1; i <= 10; ++ i) {
    		ltarget = target / Math.pow(10.0, (double) i);
    		ltolerance = tolerance / Math.pow(10.0, (double) i);
        	if ( (testAnswer >= (ltarget - ltolerance)) &&
           		 (testAnswer <= (ltarget + ltolerance)) ) {
           		return(true);
           	}
    	}

   		return(false);
    }
    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Removes colons from a string
     *
     *  @param  The initial string
     *  @return The initial string with colons removed
     **/

    public static String removeColons(String input) {
        StringBuffer sb = new StringBuffer(64);
        char ch;
        for (int i = 0; i < input.length(); i++) {
            ch = input.charAt(i);
            if (input.charAt(i) != ':') {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Removes colons and whitespace from a string
     *
     *  @param input The initial string
     *  @return The initial string with colons and whitespace removed
     **/

    public static String removeColonsAndWhitespace(String input) {
        String input1 = removeWhitespace(input);
        String input2 = removeColons(input1);
        return input2;
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Removes commas from a string
     *
     *  @param  The initial string
     *  @return The initial string with commas removed
     **/

    public static String removeCommas(String input) {
        StringBuffer sb = new StringBuffer(64);
        char ch;
        for (int i = 0; i < input.length(); i++) {
            ch = input.charAt(i);
            if (input.charAt(i) != ',') {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Removes characters 'x' 'X' '*' from a string
     *
     *  @param input The initial string
     *  @return The initial string with xX* characters removed
     **/

    public static String removeMultSigns(String input) {
        StringBuffer sb = new StringBuffer(64);
        char ch;
        for (int i = 0; i < input.length(); i++) {
            ch = input.charAt(i);
            if (input.charAt(i) != 'x' && input.charAt(i) != 'X' && input.charAt(i) != '*') {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Removes whitespace and 'x' 'X' '*' from a string
     *
     *  @param input The initial string
     *  @return The initial string with whitespace and xX* characters removed
     **/

    public static String removeMultSignsAndWhitespace(String input) {
        String input1 = removeWhitespace(input);
        String input2 = removeMultSigns(input1);
        return input2;
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Removes colons, whitespace and 'x' 'X' '*' from a string
     *
     *  @param input The initial string
     *  @return The initial string with colons, whitespace and xX* characters removed
     **/

    public static String removeMultSignsColonsAndWhitespace(String input) {
        String input1 = removeWhitespace(input);
        String input2 = removeMultSigns(input1);
        String input3 = removeColons(input2);
        return input3;
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Removes '+' from a string
     *
     *  @param  The initial string
     *  @return The initial string + removed
     **/

    public static String removePlusSigns(String input)
    {
        StringBuffer sb = new StringBuffer(64);
        char ch;

        for (int i = 0; i < input.length(); i++) {
        	ch = input.charAt(i);
        	if (input.charAt(i) != '+') {
        		sb.append(ch);
        	}
        }
        return sb.toString();
    }
    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Removes a substring from the beginning of a string
     *  Useful for removing e.g. "y =" from the beginning of an equation
     *  where the "y =" is correct but is included outside the input box
     *
     *  @param  The initial string
     *  @return The initial string less the removed string
     **/

    public static String removeStartOfString(String str, String toRemove)
    {
    	// check if main string is longer than string to be removed
    	if (toRemove.length() < str.length()) {
    		// try removal
    		if (str.substring(0, toRemove.length()).equalsIgnoreCase(toRemove)) {
    			str = str.replaceFirst(toRemove, "");
    		}
      }
      return str;
    }
    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Removes a substring from a string
     *
     *  @param  The initial string
     *  @return The initial string less the removed string
     **/

    public static String removeString(String str, String toRemove)
    {
    	int	i, j;
    	StringBuffer sb = new StringBuffer(64);

    	for (i = 0; i < str.length(); i++) {
    		j = (str.length());
    		if ((j - i) > toRemove.length()) {
    			j = i + toRemove.length();
    		}
            if (str.substring(i, j).equalsIgnoreCase(toRemove)) {
                i = i + toRemove.length() - 1;
            }
            else {
                sb.append(str.substring(i, i+1));
            }
    	}
    	return sb.toString();
    }
    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Removes whitespace characters ' ' '\t' '\n' from a string
     *
     *  @param input The initial string
     *  @return The initial string with whitespace characters removed
     **/

    public static String removeWhitespace(String input) {
        StringBuffer sb = new StringBuffer(64);
        char ch;
        for (int i = 0; i < input.length(); i++) {
            ch = input.charAt(i);
            if (!Character.isWhitespace(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
    ///////////////////////////////////////////////////////////////////////////

    /**
     *  Converts advanced field superscript format into E notation.
     *
     *  If the input string contains "x10<sup>+-n</sup>" or "*10<sup>+-n</sup>"
     *  indicating a power of 10 followed by a superscript
     *  replaces with "E+-n"
     *  otherwise just return the original string.
     *
     *  Allows the returned string to be parsed as a double by Double.parseDouble( String string )
     *  @param str The string to be parsed
     *  @return If "x10<sup>+-n</sup>" present replaced with "E+-n" otherwise
     *  returns the original string
     **/

    public static String scientificNotationToE(String str)
    {
      	char 	ch;
        int		sptr, start;
        String 	lstr;
        StringBuffer sb = new StringBuffer(64);

        lstr = removeWhitespace(str);
        sptr = -1;
        start = -1;

    	while ((start == -1) && (sptr < (lstr.length() - 3))) {
        	++sptr;
            if (lstr.substring(sptr, sptr + 3).equalsIgnoreCase("x10") ||
                    lstr.substring(sptr, sptr + 3).equalsIgnoreCase("*10") ||
                    lstr.substring(sptr, sptr + 3).equalsIgnoreCase("�10")) {
                // found x10
            	start = sptr;
            }
        }
        if (start == -1) {	// no x10 found
        	return(str);
        }

		// copy the string up to the x10
		sb.append(lstr.substring(0, start));

		// if end of line reached there is no exponent assume 1 meant
    	if ((start + 3) == lstr.length()) {
    		sb.append("E1");
    		// and return
    		return(sb.toString());
    	}

    	// if the next character is a number (as in x100) then this is not in
    	// scientific notation and no change is made
    	sptr = start + 3;
    	ch = lstr.charAt(sptr);
    	if (Character.isDigit(ch))
    		return(lstr);

    	if (ch == '<') {
    		// start to look for superscript
        	// does <sup>+-nn</sup> follow?

    		if (lstr.length() > (sptr + 5)) {
    			if (lstr.substring(sptr, sptr + 5).equalsIgnoreCase("<sup>")) {
        			// add an 'E'
    				sb.append("E");
    				// add all chars up to </sup
    				sptr = sptr + 5;
    				do {
    					sb.append(lstr.substring(sptr, sptr + 1));
    					++sptr;
    				} while ((sptr != lstr.length()) &&
    						 (lstr.charAt(sptr) != '<'));
    				if (lstr.length() >= (sptr + 6)) {
    					// check for </sup>
    					if (lstr.substring(sptr, sptr + 6).equalsIgnoreCase("</sup>")) {
    						// hooray - proper formatting
    						sptr = sptr + 6;
    						// and copy rest of string
    						sb.append(lstr.substring(sptr));
    						return(sb.toString());
    					}
    				}
    				else {
    					// a confused string; return original
    					return(str);
    				}
    			}
    			else {
    				// not normal x10<sup>n</sup> formatting; return original
    				return(str);
    			}
      		}
    		else {
				// not normal x10<sup>n</sup> formatting; return original
    			return(str);
    		}
    	}

    	// who knows what follows; could be units
    	// replace "x10" by E1
    	sb.append("E1");
    	// and copy the rest
    	sb.append(lstr.substring(sptr));
    	// and return
    	return(sb.toString());
    }
    ///////////////////////////////////////////////////////////////////////////
    /**
     *  Returns a number to the given number of significant figures using the selected
     *  rounding mode
     *
     *@param  answer  The answer to be queried
     *@param  number  Tne number of significant figures
     *@param  type    The rounding mode
     *@return         The original number rounded
     */
    public static double toSigFigs(final double answer, final int number)
    {
        double sigFigsValue = answer;

        boolean isNegative = false;
        boolean isZero = false;
        // First find the absolute value of the correct answer
        if (sigFigsValue < 0.0) {
            sigFigsValue *= -1;
            isNegative = true;
        } else if (sigFigsValue == 0.0) {
            isZero = true;
        }

        // use exponent to keep track of transformation
        int exponent = 0;
        // Next we transform the correct answer to an integer (transform) in
        // the range 0 <= transform < 10 to the power of the number of significant figures
        double upperLimit = StrictMath.pow(10.0, number);
        // if our transform is greater than the range divide by ten until it is.
        // counting the divisions in exponent
        while ((sigFigsValue > upperLimit) && !isZero) {
            sigFigsValue /= 10;
            exponent++;
        }
        // if correct answer is zero then do nothing otherwise multiply by ten
        // until it is greater than 10 to the number of significant figures - 1
        // counting the divions (as negative numbers) in the exponent variable
        double lowerLimit = StrictMath.pow(10.0, number - 1);
        while ((sigFigsValue < lowerLimit) && !isZero) {
            sigFigsValue *= 10;
            exponent--;
        }
        // now transform should be either 0 or in the range
        // lowerLimit < transform < upperLimit
        // exponent should hold the number of division required (negative divisions
        // are multiplications) to achieve this.

        sigFigsValue += 0.50000001;
        sigFigsValue = StrictMath.floor(sigFigsValue);
        //System.err.println("sigfigsvalue = " + sigFigsValue );
        //System.err.println("Exponent = " + exponent );
        sigFigsValue *= StrictMath.pow(10.0, exponent);
        if (isNegative) {
            sigFigsValue *= -1;
        }
        return(sigFigsValue);
    }
    ///////////////////////////////////////////////////////////////////////////

    /**
     *  @param input The initial string
     *  @return The initial string with + characters replaced by plus
     *  = characters replaced equals and whitespace removed
     **/

    public static String transfromCGINotAllowedChars(String s) {
        String input = removeWhitespace(s);

        StringBuffer sb = new StringBuffer();
        //char ch;
        for (int i = 0; i < input.length(); i++) {
            //ch = input.charAt(i);
            if (input.charAt(i) == '+') {
                sb.append("plus");
            }
            else if(input.charAt(i) == '='){
                sb.append("equals");
            }
            else if(input.charAt(i) == '?'){
                sb.append("questionmark");
            }
            else if(input.charAt(i) == ':'){
                sb.append("_");
            }
            else{
              sb.append(input.charAt(i));
            }
        }
        return sb.toString();
    }
    ///////////////////////////////////////////////////////////////////////////

// end of class
}
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
