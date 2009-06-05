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
 * @author       S. M. Harben
 * @version 1.0
 */

public class NumberChecker
{
  private double numberRequired;
  private int toSigFigs;
  private boolean wantedInSciNotation = false;
  private String[] unitsExactMatch;
  private String[] unitsIgnoreCaseMatch;
  private boolean ignoreTrailingZeros = false;

  private boolean isCorrect;

  public boolean unitsMatch;
  public boolean unitsExactMatchButWrongCase;
  public boolean missingUnits;
  public boolean numberCorrect;
  public boolean isTooPrecise;
  public boolean isWronglyRounded;
  public boolean isScientificNotation;

  SigFigsDoubleChecker sFDChecker;


  public NumberChecker(double numberRequired, int toSigFigs, boolean wantedInSciNot,
                          String[] unitsExactMatch, String[] unitsIgnoreCaseMatch,
                              boolean ignoreTrailingZeros)
  {
    this.numberRequired = numberRequired;
    this.toSigFigs = toSigFigs;
    this.wantedInSciNotation = wantedInSciNot;
    this.unitsExactMatch = unitsExactMatch;
    this.unitsIgnoreCaseMatch = unitsIgnoreCaseMatch;

    //the boolean ignoreTrailingZeros has been add so that the class can be used to check
  //for numbers such as 1 where it does not matter if the input is 1, 1.0, 1.00 etc.
  //for such cases ignoreTrailingZeros should be set to true otherwise zeros after
  //a digit are counted as significant figures
    this.ignoreTrailingZeros = ignoreTrailingZeros;

    sFDChecker = new SigFigsDoubleChecker(numberRequired, toSigFigs, ignoreTrailingZeros);
  }

  public boolean check(String input){
    //making sure that all the booleans used in the check method are set to
    //their appropriate starting values
    resetBooleans();

    //splits the input into number and units parts
    StringConverterAndSplitter splitInput = new StringConverterAndSplitter(input);

    //check the units part first if required
    if(unitsExactMatch != null || unitsIgnoreCaseMatch != null){
      String unitInput = splitInput.getUnitsString();
      boolean exactMatch = false;
      boolean ignoreCaseMatch = false;
      if(unitsExactMatch != null){
        StringArrayChecker uEMChecker = new StringArrayChecker(unitsExactMatch);
        exactMatch = uEMChecker.check(unitInput);
        if(!exactMatch){
          unitsExactMatchButWrongCase = uEMChecker.getIsCaseIgnoredMatch();
        }
      }
      if(unitsIgnoreCaseMatch != null){
        StringArrayChecker uICMChecker = new StringArrayChecker(unitsIgnoreCaseMatch);
        uICMChecker.check(unitInput);
        ignoreCaseMatch = uICMChecker.getIsCaseIgnoredMatch();
      }
      if(unitInput.equals("")){
        missingUnits = true;
      }

      unitsMatch = exactMatch || ignoreCaseMatch;

      isCorrect = isCorrect && unitsMatch;
    }



    //getting the number part. If there is no number leaving the check method
    //without doing any other checks on the number and returning false;
    double numberPartOfInput = splitInput.getDoubleNumber();
    if(Double.isNaN(numberPartOfInput)){
      return false;
    }

    //getting the number of significant figures in the student input
    int sigFigs = splitInput.getNumberOfSigFigs();

    //checking that the number is correct to the specified number of sig figs
    //setting the boolean numberCorrect
    numberCorrect = sFDChecker.check(numberPartOfInput, sigFigs);

    isCorrect = isCorrect && numberCorrect;

    //setting the boolean isWronglyRounded
    isWronglyRounded = sFDChecker.getIsWronglyRounded();

    //setting the boolean isTooPrecise. Note that a seperate checker is created here
    //that checks the number to one more significant figure than is asked for.
    //This is because the SigFigsDoubleChecker method that sets its isTooPrecise flag
    //is accurate only about 95% of the time. Occasionally it is set to true
    //for two numbers. e.g the number required is 1.34247 to 3 sig figs
    //the check method will work 100% of the time but isTooPrecise may be set to
    //true if the input is 1.342 or 1.343 ( and 1.3425 , 1.34247 etc. as it should).
    //By creating the extra checker we minimise the impact of this error.
    SigFigsDoubleChecker isTooPreciseChecker = new SigFigsDoubleChecker(numberRequired, toSigFigs + 1, ignoreTrailingZeros);
    isTooPreciseChecker.check(numberPartOfInput, sigFigs);
    if(isTooPreciseChecker.check(numberPartOfInput, sigFigs) || isTooPreciseChecker.getIsTooPrecise()){
      isTooPrecise = true;
    }

    isScientificNotation = splitInput.getIsSciNotation();
    //if the answer is required to be in scientific notation
    if(wantedInSciNotation){
      isCorrect = isCorrect && isScientificNotation;
    }

//    System.out.println("isCorrect = " + isCorrect);
//
//    System.out.println("numberCorrect = " + numberCorrect);
//
//    System.out.println("isTooPrecise = " + isTooPrecise + " isWronglyRounded = " + isWronglyRounded);
//
//    System.out.println("isScientificNotation = " + isScientificNotation + " unitsMatch = " + unitsMatch);

    return isCorrect;

  }

  private void resetBooleans(){
    //isCorrect is initially set to true
    //if any of the checks asked for fail then
    //it is set to false;
    isCorrect = true;

    unitsMatch = false;
    numberCorrect = false;
    isTooPrecise = false;
    isWronglyRounded = false;
    isScientificNotation = false;
    unitsExactMatchButWrongCase = false;
    missingUnits = false;

  }
}