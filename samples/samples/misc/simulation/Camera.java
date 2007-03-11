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
package samples.misc.simulation;

import java.util.regex.Pattern;

import om.OmException;
import om.stdquestion.StandardQuestion;

public class Camera extends StandardQuestion
{
	public void actionTake() throws OmException
	{
		getImage("photo").setString("filePath",
		  getDropdown("speed").getSelected()+
		  "f"+getDropdown("aperture").getSelected()+".jpg");
	}
	
	private final static Pattern 
		DARKER=Pattern.compile(
		"((more )?(dark|dim)(er)?)|(less (bright|light))"),
		LIGHTER=Pattern.compile(
		"((more )?(light|bright)(er)?)|(less (dark|dim))"),
		BLURRED=Pattern.compile(
		"((more )?(blurr(ed|y)(i?er)?|soft(er)|fuzz(y|i)(er)?))|out of focus|unfocus(s)?ed|less (sharp|clear)"),
		SHARP=Pattern.compile(
		"((more )?(sharp(er)|clear))|less (blur(ed|y)|soft|fuzzy)");

	public void actionBegin() throws OmException
	{
		getComponent("question1").setDisplay(false);
		getComponent("question2").setDisplay(true);
	}
	
	private String tidyValue(String sValue)
	{
		// Trim edges
		sValue=sValue.trim();
		// Lower-case
		sValue=sValue.toLowerCase();
		// Turn all whitespace runs into single spaces
		sValue=sValue.replaceAll("\\s+"," ");
		// Get rid of everything other than letters, numbers, and spaces
		sValue=sValue.replaceAll("[^a-z0-9 ]","");
		return sValue;
	}
	
	public void actionOK2() throws OmException
	{
		String sValue=tidyValue(getEditField("speedchange").getValue());
		
		getComponent("question2").setEnabled(false);
		if(DARKER.matcher(sValue).matches())
			getComponent("answer2y").setDisplay(true);
		else if(LIGHTER.matcher(sValue).matches())
			getComponent("answer2n").setDisplay(true);
		else
			getComponent("answer2q").setDisplay(true);
	}
	
	public void actionCont2() throws OmException
	{
		getComponent("question2").setEnabled(true);
		getComponent("answer2q").setDisplay(false);		
	}
	
	public void actionOK2n() throws OmException
	{
		getComponent("answer2n").setDisplay(false);
		getComponent("answer2y").setDisplay(false);
		getComponent("question2").setDisplay(false);
		getComponent("question3").setDisplay(true);
	}

	public void actionOK3() throws OmException
	{
		String sValue=tidyValue(getEditField("aperturechange1").getValue());
		
		getComponent("question3").setEnabled(false);
		if(LIGHTER.matcher(sValue).matches())
			getComponent("answer3y").setDisplay(true);
		else if(DARKER.matcher(sValue).matches())
			getComponent("answer3n").setDisplay(true);
		else if(BLURRED.matcher(sValue).matches())
			getComponent("answer3b").setDisplay(true);
		else
			getComponent("answer3q").setDisplay(true);
	}
	
	public void actionCont3() throws OmException
	{
		getComponent("question3").setEnabled(true);
		getComponent("answer3q").setDisplay(false);		
		getComponent("answer3b").setDisplay(false);		
	}
	
	public void actionOK3n() throws OmException
	{
		getComponent("answer3n").setDisplay(false);
		getComponent("answer3y").setDisplay(false);
		getComponent("question3").setDisplay(false);
		getComponent("question4").setDisplay(true);
	}
	
	public void actionOK4() throws OmException
	{
		String sValue=tidyValue(getEditField("aperturechange2").getValue());
		
		getComponent("question4").setEnabled(false);
		if(BLURRED.matcher(sValue).matches())
			getComponent("answer4y").setDisplay(true);
		else if(SHARP.matcher(sValue).matches())
			getComponent("answer4n").setDisplay(true);
		else if(LIGHTER.matcher(sValue).matches() || DARKER.matcher(sValue).matches())
			getComponent("answer4b").setDisplay(true);
		else
			getComponent("answer4q").setDisplay(true);
	}	
	
	public void actionCont4() throws OmException
	{
		getComponent("question4").setEnabled(true);
		getComponent("answer4q").setDisplay(false);		
		getComponent("answer4b").setDisplay(false);		
	}
	
	public void actionOK4n() throws OmException
	{
		end();
	}
}
