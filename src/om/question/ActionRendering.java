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
package om.question;

/** Extra data needed for action responses */
public class ActionRendering extends Rendering
{
	/** True if session should now end */
	private boolean bSessionEnd=false;
	
	/** Store question results (null if results should not be sent) */
	private Results r=null;
	
	/** 
	 * Set as end of session. The question will receive a close()
	 * call during processing of this response.
	 */
	public void setSessionEnd()
	{
		bSessionEnd=true;
	}
	
	/** @return True if session should now end */
	public boolean isSessionEnd() { return bSessionEnd; }

	/** @return Results object */
	public Results getResults() { return r; }
	
	/** 
	 * Call this once results are finalised; they'll be sent back to the 
	 * server. (Don't call it twice for the same question!)
	 * @param results the results to send.
	 */
	public void sendResults(Results results)
	{
		this.r=results;
	}
}
