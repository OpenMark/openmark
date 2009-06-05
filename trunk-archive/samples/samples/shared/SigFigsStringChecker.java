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
package samples.shared;

import samples.shared.Helper;

/**
 * Title: checking.SigFigsStringChecker
 * Description: Class to check a string against a string representation of a number
 *              to a particular number of significant figures
 * Copyright:   Copyright (c) 2002
 * Company:     Open University
 *
 *@author       prm96
 *@created      10 May 2002
 *@version      $Id: SigFigsStringChecker.java,v 1.1 2006/02/16 10:34:05 pgb2 Exp $
 */

public class SigFigsStringChecker extends AbstractSplitStringChecker implements StringChecker {

    double approximateCeilingAnswer;
    double approximateFlooredAnswer;
    double ceilingCorrectAnswer;
    double correctlyFormattedAnswer;
    double flooredCorrectAnswer;

    public boolean isCorrect;
    public boolean isNotQuiteRight;
    public boolean isNumericallyCorrect;

    public boolean isRounded = true;
    public boolean isTooPrecise;
    public boolean isWronglyRounded;
    public boolean numberFormatError;

    double numericallyCorrectAnswer;
    boolean onlySigFigsWrong;
    boolean scientificNotation;
    boolean showSigFigsErrorMessage;
    double studentAnswer;
    double tooPreciseCeilingAnswer;
    double tooPreciseFlooredAnswer;
//    private String correctString;
    private String responseString;
    private int sigFigs;
    /**
     *  un-typesafe enum !? for CEILING
     */
    protected final static int CEILING = 1002;
    /**
     *  un-typesafe enum !? for FLOOR
     */
    protected final static int FLOOR = 1001;

    /**
     * un-typesafe enum !? for ROUND
     */
    protected final static int ROUND = 1000;

    boolean ignoreTrailingZeros;


    /**
     *  Constructor for the s151.SigFigsStringChecker object
     *
     *@param  input    A string representation of the accurate number
     *@param  sigFigs  The number of signficant figures for the correct answer
     */

     //Default is where trailing zeros are not ignored ie if a number of 1.5000
        //is asked for to two sigfigs then 1.5 is marked as correct and 1.50 will
        //be marked as incorrect.
        //However if the ignoreTrailingZeros is set to true then 1.5 , 1.50, 1.500 ...
        //will all be marked as correct

     public SigFigsStringChecker(String input, int sigFigs) {
        this(input, sigFigs, false);
     }

     public SigFigsStringChecker(String input, int sigFigs, boolean ignoreTrailingZeros) {
        this.sigFigs = sigFigs;
        this.ignoreTrailingZeros = ignoreTrailingZeros;


        try {
            numericallyCorrectAnswer = Double.parseDouble(input);
        }
        catch (NumberFormatException ex) {
            System.err.println("Constructor for SigFigStringChecker requires " +
                    "input string that can parse to a double.");
        }
        correctlyFormattedAnswer = roundToSigFigs(numericallyCorrectAnswer, sigFigs);
        flooredCorrectAnswer = floorToSigFigs(numericallyCorrectAnswer, sigFigs);
        ceilingCorrectAnswer = ceilingToSigFigs(numericallyCorrectAnswer, sigFigs);
        tooPreciseFlooredAnswer = floorToSigFigs(numericallyCorrectAnswer, sigFigs + 1);
        tooPreciseCeilingAnswer = ceilingToSigFigs(numericallyCorrectAnswer, sigFigs + 1);
        approximateFlooredAnswer = floorToSigFigs(numericallyCorrectAnswer, sigFigs - 1);
        approximateCeilingAnswer = ceilingToSigFigs(numericallyCorrectAnswer, sigFigs - 1);
    }


    /**
     *  Checks the input against the correct answer
     *
     *@param  input  A string representing the answer to be checked
     *@return        Is the answer correct to the required significant figures
     */
    public boolean check(String input) {
        //smh357 has extended this checker from abstractSplitStringChecker
        //The checker now works by splitting off the number from the front of any
        //string that is passed to it and then checking the number


        //this returns a string which has whitespace removed and x10 converted to E
        //eg if input is 1.5 * 10^5: cm^3 then 1.5E5cm^3 is returned

    	// next line changed by pgb for OpenMark S input format
    	//      String formattedInput = Helper.scientificXToE(input);
        String formattedInput = Helper.scientificNotationToE(input);

        //this returns the number from the front of a string
        //eg if formattedInput is 1.5E5cm^3 then 1.5E5 is returned
        //Below this value is turned into a double and checked
        responseString = getNumberString(formattedInput);

        //this returns the mantissa part of the number
        //eg if responseString is 1.5E5 the mantissa is set to 1.5
        //the mantissa is passed to getNumberOfSignificantFiguresInString(String string)
        //to determine if the input
        //has been given to the correct number of significant figures
        splitNumberFromString(responseString);

        //System.out.println("input = " + input + " formattedInput = " + formattedInput + " responseString = " + responseString + " mantissa = " + mantissa);


        try {
            studentAnswer = Double.parseDouble(responseString);
        }
        catch (NumberFormatException ex) {
            studentAnswer = Double.NaN;
            numberFormatError = true;
        }
        return check(studentAnswer);
    }


    /**
     *  Checks the input against the correct answer
     *
     *@param  response  The answer to be checked
     *@return           Is the answer correct to the required significant figures
     */
    public boolean check(double response) {
        resetBooleans();

        if (Math.abs(studentAnswer - correctlyFormattedAnswer)
                < Math.abs(correctlyFormattedAnswer / Math.pow(10.0, 6.0))) {
            if(ignoreTrailingZeros){
              isCorrect = true;
            }
            else{
                if ( getNumberOfSignificantFiguresInString(mantissa) != sigFigs){
                    isCorrect = false;
                } else {
                    isCorrect = true;
                }
            }
        }
        else {
            isCorrect = false;
        }

        if (Math.abs(studentAnswer - numericallyCorrectAnswer)
                < Math.abs(numericallyCorrectAnswer / Math.pow(10.0, 6.0))) {
            isNumericallyCorrect = true;
            //return false ;
        } else {
            isNumericallyCorrect = false;
        }
        if (Math.abs(studentAnswer - flooredCorrectAnswer)
                < Math.abs(flooredCorrectAnswer / Math.pow(10.0, 6.0))) {
            isWronglyRounded = true;
            //return false ;
        } else {
            isWronglyRounded = false;
        }
        if (correctlyFormattedAnswer > 0) {
            if (tooPreciseFlooredAnswer <= (studentAnswer + 0.000000001)
                   && studentAnswer <= (tooPreciseCeilingAnswer + 0.000000001)) {
                isTooPrecise = true;
            } else {
                isTooPrecise = false;
            }
            if (approximateFlooredAnswer <= studentAnswer
                    && studentAnswer <= approximateCeilingAnswer) {
                isNotQuiteRight = true;
                //return false ;
            } else {
                isNotQuiteRight = false;
            }
        } else if (correctlyFormattedAnswer < 0) {
            if (tooPreciseFlooredAnswer >= (studentAnswer - 0.000000001)
                    && studentAnswer >= (tooPreciseCeilingAnswer - 0.000000001)) {
                isTooPrecise = true;
            } else {
                isTooPrecise = false;
            }
            if (approximateFlooredAnswer >= studentAnswer
                    && studentAnswer >= approximateCeilingAnswer) {
                isNotQuiteRight = true;
                //return false ;
            } else {
                isNotQuiteRight = false;
            }
        }

        return isCorrect;
        //return false ;
    }


    /**
     *  Determines the ceiling of the number to the set significant figures
     *
     *@param  answer  The answer to be queried
     *@param  number  The number of significant figures
     *@return         The ceiling of the answer to the given number of significant figures
     */
    protected double ceilingToSigFigs(double answer, int number) {
        return toSigFigs(answer, number, CEILING);
    }


    /**
     *  Determines the floor of the number to the set significant figures
     *
     *@param  answer  The answer to be queried
     *@param  number  The number of significant figures
     *@return         The floor of the answer to the given number of significant figures
     */
    protected double floorToSigFigs(double answer, int number) {
        return toSigFigs(answer, number, FLOOR);
    }


    /**
     *  Rounds a number to the set significant figures
     *
     *@param  answer  The answer to be queried
     *@param  number  The number of significant figures
     *@return         The number rounded to the given number of significant figures
     */
    protected double roundToSigFigs(double answer, int number) {
        return toSigFigs(answer, number, ROUND);
    }


    /**
     *  Returns a number to the given number of significant figures using the selected
     *  rounding mode
     *
     *@param  answer  The answer to be queried
     *@param  number  Tne number of significant figures
     *@param  type    The rounding mode
     *@return         The original number rounded
     */
    protected static double toSigFigs(final double answer, final int number, final int type) {
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

        // now get the right type of significant figure
        switch (type) {
            case ROUND:
                //sigFigsValue += 0.5; changed to the value below as wasn't always rounding values which ended in 5 correctly
                sigFigsValue += 0.50000001;
                sigFigsValue = StrictMath.floor(sigFigsValue);
                break;
            case FLOOR:
                sigFigsValue = StrictMath.floor(sigFigsValue);
                break;
            case CEILING:
                sigFigsValue = StrictMath.ceil(sigFigsValue);
                if (StrictMath.floor(sigFigsValue) == StrictMath.ceil(sigFigsValue)) {
                    sigFigsValue++;
                }
                break;
            default:
                System.err.println("Error in SigFigsFieldQuestion.toSigFigs" +
                        "(double,int,int) unknown value in switch statement");
        }
        //System.err.println("sigfigsvalue = " + sigFigsValue );
        //System.err.println("Exponent = " + exponent );
        sigFigsValue *= StrictMath.pow(10.0, exponent);
        if (isNegative) {
            sigFigsValue *= -1;
        }
        return sigFigsValue;
    }

    /**
     *  Returns an appropriate error string if the query string is not correct
     *
     * @return    An error string
     */
    public String incorrect() {
        // spencer add error handling code here
        if (responseString.equals("")) {
            return "";
        }
        if (isTooPrecise) {
            //return Prompt.sigFigs();
        }

        if (isWronglyRounded) {
            //return Prompt.incorrectRounding();
        }
        return "";
    }

    private void resetBooleans() {

        isCorrect = false;
        isNotQuiteRight = false;
        isNumericallyCorrect = false;

        isRounded = false;
        isTooPrecise = false;
        isWronglyRounded = false;
        numberFormatError = false;
    }


    public int getNumberOfSignificantFiguresInString(String string) {
        boolean startedCount = false;
        int numberOfSigFigs = 0;
        for (int i = 0; i < string.length(); i++) {

            char ch = string.charAt(i);
//            Character character = new Character(ch);
            if (Character.isDigit(ch) && (ch != '0')) {
                startedCount = true;
            }
            if (startedCount && Character.isDigit(ch)) {
                numberOfSigFigs = numberOfSigFigs + 1;
            }
        }
        return numberOfSigFigs;
    }

    public String getNumberString(String input) {

          String complete = Helper.removePlusSigns(Helper.removeColons(input));
          int cutPoint = 0;
          char current = ' ';

          boolean letterFound = false;

          if (complete != null) {
              while (!letterFound && cutPoint < complete.length()) {
                  current = complete.charAt(cutPoint++);
                  if (Character.isLetter(current)) {
                      letterFound = true;
                      //if there is no digit after the e
                      //then it is classed as a letter and not part of the number
                      //if it is part of the number then we reset letterFound to false;
                      //the x's have been added so that inputs such as 18x2 are not split
                      //at the 18 but that the function returns 18x2 and hence will be
                      //marked as incorrect.
                      if(current == 'E' || current == 'e' || current == 'x' || current == 'X'){
                         if(cutPoint < complete.length()){
                            char temp = complete.charAt(cutPoint);
                            //System.out.println("char temp = " + temp);
                            if(Character.isDigit(temp) || temp == '-'){
                              letterFound = false;
                              //System.out.println("not resetting letter found");
                            }
                         }
                      }
                  }
              }
          }

          String number = "";
          // if we still haven't found a letter the whole string is the number
          if (!letterFound) {
            number = complete;
          }
          // otherwise just the last letter
          else {
              number = complete.substring(0, cutPoint - 1);
          }
          return number;
    }
}
