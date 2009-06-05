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
 * @author
 * @version 1.0
 */

public class RangeChecker
{

//  private double midValue;
//  private double range;
  private int toSigFigs;
  private boolean wantedInSciNotation = false;
  private String[] unitsExactMatch;
  private String[] unitsIgnoreCaseMatch;

  private boolean isCorrect;

  public boolean unitsMatch;
  public boolean unitsExactMatchButWrongCase;
  public boolean missingUnits;
  public boolean numberInRange;
  public boolean correctSigFigs;
  public boolean inRangeButTooManySF;
  public boolean inRangeButTooFewSF;
  public boolean isScientificNotation;

  DoubleRangeChecker dRChecker;

  //As the very minimum the range checker requires the arguments midValue and
  //range to work
  public RangeChecker(double midValue, double range){
    this(midValue, range, 0, false, null, null);
  }

  public RangeChecker(double midValue, double range, boolean wantedInSciNot){
    this(midValue, range, 0, wantedInSciNot, null, null);
  }

  public RangeChecker(double midValue, double range, int toSigFigs){
    this(midValue, range, toSigFigs, false, null, null);
  }

  public RangeChecker(double midValue, double range, String[] unitsExactMatch, String[] unitsIgnoreCaseMatch){
    this(midValue, range, 0, false, unitsExactMatch, unitsIgnoreCaseMatch);
  }

  public RangeChecker(double midValue, double range,
                        int toSigFigs, boolean wantedInSciNot,
                          String[] unitsExactMatch, String[] unitsIgnoreCaseMatch)
  {
//    this.midValue = midValue;
//    this.range = range;
    this.toSigFigs = toSigFigs;
    this.wantedInSciNotation = wantedInSciNot;
    this.unitsExactMatch = unitsExactMatch;
    this.unitsIgnoreCaseMatch = unitsIgnoreCaseMatch;

    dRChecker = new DoubleRangeChecker(midValue, range);
  }

  public boolean check(String input){
    resetBooleans();
    //splits the input into number and units parts
    StringConverterAndSplitter splitInput = new StringConverterAndSplitter(input);

    //check the units part first
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
    //without doing any other checks on the number and returns false;
    double numberPartOfInput = splitInput.getDoubleNumber();
    if(Double.isNaN(numberPartOfInput)){
      return false;
    }

    //checking that the first number in any input is within the specified range
    // System.out.println("numberpart = " + numberPartOfInput);
    if(dRChecker.check(numberPartOfInput)){
      numberInRange = true;
    }

    isCorrect = isCorrect && numberInRange;

    //only checking for the number of significant figures if the input is within
    //the requested range. Using 0 as the default value for the number of
    //sigFigs. If 0 used in the constructor then sigFigs not nessecary for the answer
    if(toSigFigs != 0){
      int sigFigs = splitInput.getNumberOfSigFigs();
      if(numberInRange){
          if(toSigFigs == sigFigs){
            correctSigFigs = true;
          }
          else if(sigFigs > toSigFigs){
            inRangeButTooManySF = true;
          }
          else if(sigFigs < toSigFigs){
            inRangeButTooFewSF = true;
          }
      }
      isCorrect = isCorrect && correctSigFigs;
    }

    isScientificNotation = splitInput.getIsSciNotation();
    //if the answer is required to be in scientific notation
    if(wantedInSciNotation){
      isCorrect = isCorrect && isScientificNotation;
    }

//    System.out.println("isCorrect = " + isCorrect);
//
//    System.out.println("number in range = " + numberInRange + " correctSigFigs = " + correctSigFigs);
//
//    System.out.println("inRangeButTooManySF = " + inRangeButTooManySF + " inRangeButTooFewSF = " + inRangeButTooFewSF);
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
    numberInRange = false;
    correctSigFigs = false;
    inRangeButTooManySF = false;
    inRangeButTooFewSF = false;
    isScientificNotation = false;
    unitsExactMatchButWrongCase = false;
    missingUnits = false;

  }
}