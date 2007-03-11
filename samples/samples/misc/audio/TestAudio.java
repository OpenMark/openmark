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
package samples.misc.audio;

import java.util.Random;

import om.OmException;
import om.stdquestion.StandardQuestion;

public class TestAudio extends StandardQuestion
{
	private static final String 
		HIDARI="hidari",PANYA="panya",KISSATEN="kissaten",TOSHOKAN="toshokan",
		DEPAATO="depaato",MIGI="migi";
	
	private String sJunction,sPlace;	
	
	protected void init() throws OmException
	{
		Random r=getRandom();
		if(r.nextBoolean())
		{
			sJunction=MIGI;
			sPlace=r.nextBoolean() ? DEPAATO : TOSHOKAN;
		}
		else
		{
			sJunction=HIDARI;
			sPlace=r.nextBoolean() ? KISSATEN : PANYA;
		}
		
		setPlaceholder("JUNCTION",sJunction);
		setPlaceholder("PLACE",sPlace);
	}
	
	public void actionOK() throws OmException
	{
		end();
	}

	public void actionLeft() throws OmException
	{
		if(sJunction!=HIDARI)
		{
			getComponent("wrong1").setDisplay(true);
			getComponent("hereL").setEnabled(false);
		}
		else beginPart2();
	}
	
	private void beginPart2() throws OmException
	{
		getComponent("hereL").setDisplay(false);
		getComponent("hereR").setDisplay(false);
		getComponent("audio1").setDisplay(false);
		getComponent("part1").setDisplay(false);
		
		getComponent("part2").setDisplay(true);
		if(sJunction==MIGI)
		{
			getComponent("audio2r").setDisplay(true);
			getComponent("hereD").setDisplay(true);
			getComponent("hereT").setDisplay(true);			
		}
		else
		{
			getComponent("audio2l").setDisplay(true);
			getComponent("hereK").setDisplay(true);
			getComponent("hereP").setDisplay(true);			
		}
	}

	public void actionRight() throws OmException
	{
		if(sJunction!=MIGI)
		{
			getComponent("wrong1").setDisplay(true);
			getComponent("hereR").setEnabled(false);
		}
		else beginPart2();
	}
	
	public void actionDepaato() throws OmException
	{
		getComponent("right1").setDisplay(false);
		if(sPlace!=DEPAATO) 
		{
			getComponent("hereD").setEnabled(false);
			getComponent("wrong2").setDisplay(true);
		}
		else beginFinal();
	}
	
	public void actionToshokan() throws OmException
	{
		getComponent("right1").setDisplay(false);
		if(sPlace!=TOSHOKAN) 
		{
			getComponent("hereT").setEnabled(false);
			getComponent("wrong2").setDisplay(true);
		}
		else beginFinal();
	}
	
	public void actionKissaten() throws OmException
	{
		getComponent("right1").setDisplay(false);
		if(sPlace!=KISSATEN) 
		{
			getComponent("hereK").setEnabled(false);
			getComponent("wrong2").setDisplay(true);
		}
		else beginFinal();
	}
	
	public void actionPanya() throws OmException
	{
		getComponent("right1").setDisplay(false);
		if(sPlace!=PANYA) 
		{
			getComponent("hereP").setEnabled(false);
			getComponent("wrong2").setDisplay(true);
		}
		else beginFinal();
	}
	
	private void beginFinal() throws OmException
	{
		getComponent("part2").setDisplay(false);
		getComponent("hereP").setEnabled(false);
		getComponent("hereT").setEnabled(false);
		getComponent("hereK").setEnabled(false);
		getComponent("hereD").setEnabled(false);
		getComponent("audio2l").setEnabled(false);
		getComponent("audio2r").setEnabled(false);		
		
		getComponent("part3").setDisplay(true);
		
	}
	
}
