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
package samples.mu120.module5.question07;

import samples.mu120.lib.SimpleQuestion2;
import om.OmDeveloperException;
import om.OmException;

public class Q7 extends SimpleQuestion2 {
    
    protected void init() throws OmException {
    }
    
    protected boolean isRight(int iAttempt) throws OmDeveloperException {
        String response1 = (getEditField("response1").getValue().trim());
        String response2 = (getEditField("response2").getValue().trim());
        String response3 = (getEditField("response3").getValue().trim());
        String response4 = (getEditField("response4").getValue().trim());
        
        getResults().appendActionSummary("Time taken: " + response1);
        getResults().appendActionSummary("Enjoyable ? " + response2);
        getResults().appendActionSummary("Feeback useful ? " + response3);
        getResults().appendActionSummary("Suggestions: " + response4);
        
        return true;
    }
    
}