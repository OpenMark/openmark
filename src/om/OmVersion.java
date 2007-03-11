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
package om;

import java.io.IOException;

import util.misc.IO;

/** Static class that only provides information about the build version */
public abstract class OmVersion
{
	private static String loadVersion()
	{
		try
		{
			return IO.loadString(OmVersion.class.getResourceAsStream("version.txt"));
		}
		catch(IOException ioe)
		{
			return "0000-00-00 00:00:00|Err";
		}
	}
	
	/** @return Version identifier for this version of the system, e.g. "M12" or "1.0" */
	public static String getVersion()
	{
		return loadVersion().split("\\|")[0];
	}
	
	/** @return Build date for this version of the system, e.g. "2005-10-06 14:45:22" */
	public static String getBuildDate()
	{
		return loadVersion().split("\\|")[1];
	}
}
