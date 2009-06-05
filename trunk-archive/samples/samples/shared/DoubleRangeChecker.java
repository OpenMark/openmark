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

public class DoubleRangeChecker
{
  private double midValue;
  private double range;

  public DoubleRangeChecker(double midValue, double range)
  {
    this.midValue = midValue;
    this.range = range;
  }

  public boolean check(double doubleToCheck) {
      boolean numberInRange = false;
      if(doubleToCheck >= (midValue - range) && doubleToCheck <= (midValue + range)){
          numberInRange = true;
      }
      return numberInRange;
  }
}