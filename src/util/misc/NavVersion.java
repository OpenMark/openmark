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
package util.misc;

import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;

import om.Log;
import om.tnavigator.db.DatabaseAccess;


/** get and process version details from the navconfig table */
public class NavVersion
{
	/** assume version of form x.y.z or x.y */
	private int majorV=0;
	private int minorV=0;
	private int incV=0;
	private boolean isValid=true;
	
	/* if not valid data then set everything to 0*/
	private void setInvalid()
	{
		this.majorV=0;
		this.minorV=0;
		this.incV=0;
		this.isValid=false;
	}
	/* if we were given invalid version details then all values will be zero so return false */
	public boolean isValid()
	{
		return isValid;
	}

	public NavVersion(String version)
	{	
		setVersion(version);
			
	}
	
	
	public String getVersion()
	{	

		String versionStr="";
		versionStr=Integer.toString(majorV);

		if (minorV > 0)
			{versionStr +="."+Integer.toString(minorV);}

		if (incV > 0)
			{versionStr+="."+Integer.toString(incV);}

		return versionStr;

	}
	
	public int majorVersion()
	{	
		return this.majorV;
		
	}
	
	public int minorVersion()
	{	
		return this.minorV;
		
	}	
	public int incVersion()
	{	
		return this.incV;
		
	}
	public void setVersion(String version)
	{
		/* set up a navVesrion var from a string
		 */
		majorV=0;
		minorV=0;
		incV=0;
	
		// split the strink up, if there are non integers, then set it to 0.0.0, and set isValid to true 
		/* if the string specified is blank then assume version 0.0.0 */
		try {			
			majorV=splitVersion(version,0);
			minorV=splitVersion(version,1);
			incV=splitVersion(version,2);
			this.isValid=true;
			} 
		catch (NumberFormatException e)
			{
			setInvalid();
			throw new IllegalArgumentException("Invalid version string used in setVersion" + version);
			}
	}
	
	public boolean isGreaterThan(NavVersion v2)
	{
		/* return true if v2 is less than this object and falsse if 
		 * v2 is greater than or equal to this object
		 */
		if (!this.isValid() || !v2.isValid())
		{
			throw new IllegalArgumentException("Invalid version number");
		}
		int ma=v2.majorVersion();
		int mi=v2.minorVersion();
		int inc=v2.incVersion();
		if ((this.majorV > ma) || (this.majorV == ma && this.minorV > mi) 
			|| (this.majorV == ma && this.minorV == mi && this.incV > inc))
			{ return true;}
		else
			{return false;}
	}
	
	public boolean isGreaterThanStr(String version)
	{
		/* return true if vesrion is less than this object and false if 
		 * version is greater than or equal to this object
		 */
	 	NavVersion v = new NavVersion(version);
	 	return isGreaterThan(v);
	}
	
	public boolean isLessThanStr(String version)
	{
		/* return true if vesrion is less than this object and false if 
		 * version is greater than or equal to this object
		 */
		if (!this.isValid())
		{
			throw new IllegalArgumentException("Invalid version number");
		}
		int ma=0;
		int mi=0;
		int inc=0;	
		try{
			ma=splitVersion(version,0);
			mi=splitVersion(version,1);
			inc=splitVersion(version,2);
			if ((this.majorV < ma) || (this.majorV == ma && this.minorV < mi) 
					|| (this.majorV == ma && this.minorV == mi && this.incV < inc))
				{ return true;}
			else
				{return false;}
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("Invalid version string offered for comparison " + version);
		}
	}
	/* splits the string and returns an integer whicch is at index i
	 * @param v version number in format a.b.c
	 * @param i index should be in rannge 0,1,2
	 * @throws  IllegalArgumentException
	 * @throws  NumberFormatException
	 */
	private int splitVersion(String v, int i) throws IllegalArgumentException,NumberFormatException
	{
		if ( i < 0 || i > 2)
		{ 
			throw new IllegalArgumentException("Invalid index specified to splitVersion " + i);
		}
		
		String tv = v.trim();
		
		if (!tv.equals(""))
		{
			String[] s=tv.split("\\.");
			if (s.length >= i+1) 
			{
				return Integer.parseInt(s[i]);			
			}
			else
			{
				return 0;
			}
		}
		else
		{
			return 0;
		}
	}
}



