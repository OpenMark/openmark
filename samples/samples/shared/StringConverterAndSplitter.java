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
 * Title:        S151 Course Team Version
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      Open University
 * @author       S. M. Harben
 * @version 1.0
 */
/**
 * This class is designed to take a string (usually an input by a student into either
 * an editField or formattedEditField) and to split the string into the first number part only
 * and the remainder parts. If there is a number part it will convert the string to a double.
 * The number part is also checked to see if it is in a scientific form and the number
 * of significant figures in the number are counted.
 *
 */
public class StringConverterAndSplitter
{
  private String numberString;
  private String unitsString;
  private double number;
  private int numberOfSigFigs;
  private boolean isScientificNotation;

  public StringConverterAndSplitter(String input)
  {
  	// next line changed by pgb July 2005
//  String converted = convertToENotation(input);
  	String converted = Helper.scientificNotationToE(input);
    splitString(converted); //this gives numberString and unitsString their values.

    try {
            number = Double.parseDouble(numberString);
        } catch (NumberFormatException ex) {
            number = Double.NaN;
        }

    isScientificNotation = checkForSciNotation(numberString);

    numberOfSigFigs = countNumberOfSignificantFiguresInString(numberString);

//    System.out.println("getUnitsString = " + unitsString);
//
//    System.out.println("getNumberString = " + numberString + " getDoubleNumber = " + number);
//
//     System.out.println("getNumberOfSigFigs = " + numberOfSigFigs + " getIsSciNotation = " + isScientificNotation);
  }

  /*
   * commented out by pgb July 2005
     *  If the input string s contains "x10^" or "*10^" indicating a power of 10
     *  followed by a superscript - replaces the phrase with a single letter "E"
     *  otherwise just return the original string.

  private String convertToENotation(String s){
    String input = removeWhitespaceAndPlusSigns(s);
        char ch;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < input.length() - 2; i++) {
            if (input.substring(i, i + 3).equalsIgnoreCase("x10")
                    || input.substring(i, i + 3).equalsIgnoreCase("*10")) {
                sb.append(input.substring(0, i));
                // if there is no exponent and we are at the end of the string
                // assume 1 meant and return
                if ((i + 3) == input.length()) {
                    return sb.append("E1").toString();
                }
                //check to see if there is a superscript next if there is we can replace
                // the x10^ or *10^ with the E notation.
                //If not then we check to see what the next character is. If it is
                //a digit then the input is not scientific and contains an
                //input of the type x101 in this case we make no changes to the student input string.
                //Otherwise if the input is of the type x10m we assume 1 is meant so we replace
                //the x10 with E1.
                if (input.charAt(i + 3) == '^' ){
                    sb.append("E");
                }
                else if(Character.isDigit(input.charAt(i + 3))){
                    sb.append(input.substring(i, i + 4));
                }
                else{
                    sb.append("E1");
                }

                //adds any remaining characters in the string we do not convert any other numbers
                //that may be in the string only the first instance of x10 or *10 is checked and changed
                for (int j = (i + 4); j < input.length() - 1; j++) {
                    sb.append(input.charAt(j));
                }

                if (input.charAt(input.length() - 1) != ':') {
                    sb.append(input.charAt(input.length() - 1));
                }

                return sb.toString();
            }
        }
        // not in "x10" format so return original
        return input;
  }
*/
  /**
   * splits the string complete into a part that contains the number and a remainder part
   * It will look for the first number that it comes across ignoring any characters
   * before it
   */
  private void splitString(String complete){
          //int cutPoint = 0;
          int startOfNumber = 0;
          int endOfNumber = 0;
          int startOfRemainder = 0;
          char current = ' ';
          boolean numberFound = false;
          boolean endOfNumberFound = false;
          boolean letterFound = false;

          if (complete != null) {
              //looking for any digit/first digit in the string
              while(!numberFound && startOfNumber < complete.length()){
                current = complete.charAt(startOfNumber);
                if(Character.isDigit(current)){
                  numberFound = true;
                  //moving startOfNumber back by 1 as incremented at the end of the loop.
                  //However, this does mean that if the first character is a
                  //digit startOfNumber is put back to -1 at this point.
                  //Next check is to see if there is minus symbol or a stop
                  //before the digit
                  //The java string to double convert handles inputs of the type
                  //.3 and -.3 as well as -0.3 so we are allowing them by moving back from the digit
                  //and checking for them.
                  startOfNumber = startOfNumber - 1;
                  if(startOfNumber >= 0){
                    if(complete.charAt(startOfNumber) == '-'){
                      startOfNumber = startOfNumber - 1;
                    }
                    else if(complete.charAt(startOfNumber) == '.'){
                      startOfNumber = startOfNumber - 1;
                      //startNumber can be -1 here if the stop is the first character in the string
                      if(startOfNumber >= 0 && complete.charAt(startOfNumber) == '-'){
                        startOfNumber = startOfNumber - 1;
                      }
                    }
                  }
                }
                startOfNumber++;
              }

              endOfNumber = startOfNumber + 1;
              while (!endOfNumberFound && endOfNumber < complete.length()) {
                  current = complete.charAt(endOfNumber);
                  //looking for the first character that is not a digit or a stop
                  //or a minus sign after the start of the number
                  //note that this method will let through number strings of the type
                  //9. or 0.99.9 or 0.9- etc that will then throw Double.NaN exceptions
                  //on attempting to convert to a double
                  if (!Character.isDigit(current) && (current != '+') && (current != '-') && (current != '.')) {
                      endOfNumberFound = true;
                      //if there is no digit after the e
                      //then it is classed as a letter and not part of the number
                      //if it is part of the number then we reset endOfNumberFound to false
                      //and move on.
                      if(current == 'E' || current == 'e'){
                         if(endOfNumber < complete.length() - 1){
                            char temp = complete.charAt(endOfNumber + 1);
                            if(Character.isDigit(temp) || (temp == '+') || (temp == '-') || (temp == '.')){
                              // System.out.println("resetting letter found");
                              endOfNumberFound = false;
                              endOfNumber++;
                            }
                         }
                      }
                      endOfNumber = endOfNumber - 1;
                  }
                  endOfNumber++;
              }

              //to see if there are any letters in the string after any
              //number is found. Note that we are looking only for letters
              //and any symbols before the first letter will be ignored and discarded.
              //If a number has not been found then it will start at the beginning of the string
              if(numberFound){
                startOfRemainder = endOfNumber;
              }
              while(!letterFound && startOfRemainder < complete.length()){
                current = complete.charAt(startOfRemainder++);
                if(Character.isLetter(current)){
                  letterFound = true;
                  startOfRemainder--;
                }
              }
          }

          if(numberFound){
            numberString = complete.substring(startOfNumber, endOfNumber);
          }
          else{
           numberString = "";
          }

          if(letterFound){
            unitsString = complete.substring(startOfRemainder, complete.length());
          }
          else{
            unitsString = "";
          }
          //System.out.println("numberString = " + numberString + " unitsString = " + unitsString);
  }

  private boolean checkForSciNotation(String numberStr){
  //to be in scientific notation a number must be in the form
  //1E2, 1e2, 1.00E2, 1.00e2 not forgeting minus numbers

  //numbers of the sort 10E1 or 10.0e1 are not scientific notation as is 100 etc

  //note that the number part of the input has already been
  //isolated and scientific input of the type *10 etc converted to E notation before
  //being passed to this method.

    boolean scientific = false;
    if(numberStr != null && numberStr.length() >= 3){

        int plusMinusAtStart = 0;
        if(numberStr.charAt(0) == '+' || numberStr.charAt(0) =='-'){
          plusMinusAtStart = 1;
        }

        char current = ' ';
        int eFoundPoint = 0;
        boolean eFound = false;
        //first character or first character after any + or - symbols must be a number
        //between 1 and 9 inclusive
        current = numberStr.charAt(0 + plusMinusAtStart);
        if(Character.isDigit(current) && current != '0'){
            //next character after the first digit must be . e or E
            current = numberStr.charAt(1 + plusMinusAtStart);
            if(current == 'E' || current == 'e'){
              eFound = true;
              eFoundPoint = 2 + plusMinusAtStart;
            }
            else if(current == '.' && numberStr.length() >= 4){
              //if there is a stop, next character must be a digit or an e
              current = numberStr.charAt(2 + plusMinusAtStart);
              if(Character.isDigit(current) || current == 'E' || current == 'e'){
                eFoundPoint = 2 + plusMinusAtStart;
                while (!eFound && eFoundPoint < numberStr.length()) {
                  current = numberStr.charAt(eFoundPoint++);
                  if (current == 'E' || current == 'e') {
                      eFound = true;
                  }
                }
              }
            }
            //if number is of the type 1e 1.e or 1.0E then check to see if there
            // is  + - or digit after
            if(eFound){
                current = numberStr.charAt(eFoundPoint);
                if(eFoundPoint < numberStr.length() && Character.isDigit(current)){
                      scientific = true;
                }
                else if(eFoundPoint < numberStr.length() - 1){
                    if(current == '+' || current =='-'){
                        current = numberStr.charAt((eFoundPoint + 1));
                        if(Character.isDigit(current)){
                            scientific = true;
                        }
                    }
                }
            }
          }
      }
      return scientific;
    }

    private int countNumberOfSignificantFiguresInString(String string) {
        boolean startedCount = false;
        boolean eFound = false;
        int numberSigFigs = 0;
        int i = 0;
        while(!eFound && i < string.length()) {

            char ch = string.charAt(i);
//            Character character = new Character(ch);
            if (Character.isDigit(ch) && (ch != '0')) {
                startedCount = true;
            }
            if (startedCount && Character.isDigit(ch)) {
                numberSigFigs = numberSigFigs + 1;
            }
            if(ch == 'e' || ch == 'E'){
                eFound = true;
            }
            i++;
        }
        return numberSigFigs;
    }


    /*
     * commented out by pgb July 2005
     * 
     *  Removes whitespace characters ' ' '\t' '\n'  and '+' from a string
     *
     *  @param input The initial string
     *  @return The initial string with whitespace characters  + symbols removed
    private String removeWhitespaceAndPlusSigns(String input) {
        StringBuffer sb = new StringBuffer();
        char ch;
        for (int i = 0; i < input.length(); i++) {
            ch = input.charAt(i);
            if (!Character.isWhitespace(ch) && ch != '+') {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
*/
    public double getDoubleNumber(){
      return number;
    }

    public int getNumberOfSigFigs(){
      return numberOfSigFigs;
    }

    public boolean getIsSciNotation(){
      return isScientificNotation;
    }

    public String getNumberString(){
      return numberString;
    }

    public String getUnitsString(){
      return unitsString;
    }

}