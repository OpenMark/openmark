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

package om.tnavigator.scores;

public class AttemptForPI
{
	//students PI
	private String PI;
	//score for the whole test
	private double testscore=0;
	// all the score info for all axes
	private CombinedScore score=null;

	// 
	private String assignmentid="";
	private boolean hasFinished=false;
	
	public AttemptForPI( String PI, double testscore, CombinedScore score,String assignmentid,boolean hasFinished){

		this.PI=PI;
		this.score=score;
		this.testscore=testscore;
		this.assignmentid=assignmentid;
		this.hasFinished=hasFinished;

	}
	
	public void SetIfGreater(AttemptForPI attempt){
		if (attempt.gettestScore() >= this.testscore && attempt.gethasFinished())
		{
			this.PI=attempt.PI;
			this.score=attempt.score;
			this.testscore=attempt.testscore;
			this.assignmentid=attempt.assignmentid;
			this.hasFinished=true;

		}
	}
	
	public CombinedScore getScore()
	{
		return this.score;
	}
	public boolean gethasFinished()
	{
		return this.hasFinished;
	}
	public double gettestScore()
	{
		return this.testscore;
	}
	public String getPI()
	{
		return this.PI;
	}
	public String getassignmentid()
	{
		return this.assignmentid;
	}
	}
	

