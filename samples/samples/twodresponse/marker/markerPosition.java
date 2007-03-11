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
/** S151 PA Chapter 10 Question 1
 	G P Black September 2005
*/

package samples.twodresponse.marker;

import om.*;

import om.helper.SimpleQuestion1;
import om.stdcomponent.CanvasComponent;
import samples.shared.Helper;

/** Chapter 10 Question 1: draw a tangent to a curve and estimate the gradient */
public class markerPosition extends SimpleQuestion1
{
	
	protected void init() throws OmException
	{
		//set up graph item
	   	setMaxAttempts(100);
	}
	
	protected boolean isRight(int iAttempt) throws OmDeveloperException
	{
		getComponent("up").setDisplay(false);
		getComponent("down").setDisplay(false);
		getComponent("left").setDisplay(false);
		getComponent("goright").setDisplay(false);
	  	
		CanvasComponent cc = getCanvas("map");
		java.awt.Point pp = cc.getMarkerPos(0);
		
		if (Helper.range(pp.x, 319, 10) && Helper.range(pp.y, 241, 10))
			return true;
		
		if (pp.x < 310) getComponent("goright").setDisplay(true);
		else if (pp.x > 328) getComponent("left").setDisplay(true);
		if (pp.y < 232) getComponent("down").setDisplay(true);
		else if (pp.y > 250) getComponent("up").setDisplay(true);

		return false;
	}
	
}
