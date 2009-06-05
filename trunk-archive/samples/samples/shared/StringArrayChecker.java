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

public class StringArrayChecker
{
  private String possibleInputs[];

  private boolean isExactMatch;

  private boolean isCaseIgnoredMatch;

  public StringArrayChecker(String inputs[])
  {
    this.possibleInputs = inputs;
  }

  //the check method only returns true if an exact match (including case) is
  //found for the reponse string in the input array
  public boolean check(String response){

    isExactMatch = false;
    isCaseIgnoredMatch = false;

    int i = 0;
    while( i < possibleInputs.length && !isExactMatch && !isCaseIgnoredMatch) {
        if (response.equals(possibleInputs[i])) {
            isExactMatch = true;
            isCaseIgnoredMatch = true;
        }
        else if (response.equalsIgnoreCase(possibleInputs[i])) {
            isCaseIgnoredMatch = true;
        }
        i++;
    }

    return isExactMatch;
  }

  public boolean getIsCaseIgnoredMatch(){
    return isCaseIgnoredMatch;
  }
}