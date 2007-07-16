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

import java.util.Map;
import java.util.TreeMap;

import om.OmDeveloperException;

/** 
 * Parameters provided when the user makes an action (e.g. clicks a button)  
 * on the question.
 */
public class ActionParams
{
	/** Map of parameters (sorted so getParameterList works more nicely) */
	private Map<String, String> mParameters=new TreeMap<String, String>();
	
	/**
	 * Add parameter into map.
	 * @param sName Parameter name
	 * @param sValue Value of parameter
	 */
	public void setParameter(String sName,String sValue)
	{
		mParameters.put(sName,sValue);
	} 
	
	/**
	 * Obtains a parameter value.
	 * @param sName Name of parameter
	 * @return Value
	 * @throws OmDeveloperException If parameter doesn't exist
	 */
	public String getParameter(String sName) throws OmDeveloperException
	{
		String sValue=mParameters.get(sName);
		if(sValue==null) throw new OmDeveloperException(
			"Parameter does not exist: "+sName+".");
		return sValue;
	}
	
	/**
	 * @param sName Name of parameter 
	 * @return True if parameter exists.
	 */
	public boolean hasParameter(String sName)
	{
		return mParameters.containsKey(sName);
	}
	
	/**
	 * @return Sorted list of all parameter names.
	 */
	public String[] getParameterList()
	{
		return mParameters.keySet().toArray(new String[0]);
	}
}
