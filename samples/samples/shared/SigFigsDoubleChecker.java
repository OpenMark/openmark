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

/**
 * Title:        S151 Course Team Version
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      Open University
 * @author       P. Mitton / S. M. Harben
 * @version 1.0
 */

public class SigFigsDoubleChecker
{
    double approximateCeilingAnswer;
    double approximateFlooredAnswer;
    double correctlyFormattedAnswer;
    double flooredCorrectAnswer;

    private boolean isCorrect;
    private boolean isNotQuiteRight;
    private boolean isNumericallyCorrect;

    private boolean isTooPrecise;
    private boolean isWronglyRounded;

    double numericallyCorrectAnswer;
    double tooPreciseCeilingAnswer;
    double tooPreciseFlooredAnswer;

    private int requiredSigFigs;

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

    public SigFigsDoubleChecker(double requiredNumber, int requiredSigFigs) {
    	this(requiredNumber, requiredSigFigs, false);
    }

    // the boolean ignoreTrailingZeros has been add so that the class can be used to check
    // for numbers such as 1 where it does not matter if the input is 1, 1.0, 1.00 etc.
    // for such cases ignoreTrailingZeros should be set to true
    public SigFigsDoubleChecker(double requiredNumber, int requiredSigFigs, boolean ignoreTrailingZeros) {
    	this.numericallyCorrectAnswer = requiredNumber;
        this.requiredSigFigs = requiredSigFigs;
        this.ignoreTrailingZeros = ignoreTrailingZeros;

        correctlyFormattedAnswer = roundToSigFigs(numericallyCorrectAnswer, requiredSigFigs);
        flooredCorrectAnswer = floorToSigFigs(numericallyCorrectAnswer, requiredSigFigs);
        //ceilingCorrectAnswer = ceilingToSigFigs(numericallyCorrectAnswer, sigFigs);
        tooPreciseFlooredAnswer = floorToSigFigs(numericallyCorrectAnswer, requiredSigFigs + 1);
        tooPreciseCeilingAnswer = ceilingToSigFigs(numericallyCorrectAnswer, requiredSigFigs + 1);
        approximateFlooredAnswer = floorToSigFigs(numericallyCorrectAnswer, requiredSigFigs - 1);
        approximateCeilingAnswer = ceilingToSigFigs(numericallyCorrectAnswer, requiredSigFigs - 1);
    }

    /**
     *  Checks the input against the correct answer
     *
     *@param  response  The answer to be checked
     *@return           Is the answer correct to the required significant figures
     */
    public boolean check(double studentAnswer, int sigFigs) {
        resetBooleans();

        if ((Math.abs(studentAnswer - correctlyFormattedAnswer)
                < Math.abs(correctlyFormattedAnswer / Math.pow(10.0, 6.0)))
            //smh added the line below as the as the above code didn't work for the number zero
            //note though that this will only ever return true if ignoreTrailZeros is true
            || (correctlyFormattedAnswer == 0.0 && studentAnswer == correctlyFormattedAnswer)) {
            if (ignoreTrailingZeros) {
            	isCorrect = true;
            }
            else {
                if (requiredSigFigs != sigFigs){
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
        if (!isCorrect && (Math.abs(studentAnswer - flooredCorrectAnswer)
                < Math.abs(flooredCorrectAnswer / Math.pow(10.0, 6.0)))) {
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

    private void resetBooleans() {

        isCorrect = false;
        isNotQuiteRight = false;
        isNumericallyCorrect = false;

        //isRounded = false;
        isTooPrecise = false;
        isWronglyRounded = false;
        //numberFormatError = false;
    }

    //isTooPrecise is set to true in the check method if the studentAnswer is numerically the same
    //as the required answer but has been given to more significant figures than
    //was specified
    public boolean getIsTooPrecise(){
        return isTooPrecise;
      }

    //isWronglyRounded is set to true in the check method if the studentAnswer is
    //given to the specified number of significant figures but has not been rounded
    //up when it should have been
    public boolean getIsWronglyRounded(){
      return isWronglyRounded;
    }

    // this seems to have never been used in S151. Check closely if you use it.
    public boolean getIsNotQuiteRight() {
        return isNotQuiteRight;
    }

    // this seems to have never been used in S151. Check closely if you use it.
    public boolean getisNumericallyCorrect() {
        return isNumericallyCorrect;
    }

}