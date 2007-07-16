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

/**
 * Custom question result (ignored by test navigator system but may be used
 * in custom interpretation of results).
 * <p>
 * API CLASS: This class is used in SOAP returns and should probably not be
 * altered (after initial release).
 */
public class CustomResult
{
	/** Name (ID) of result */
	private String sName;
	/** Value of result */
	private String sValue;

	/**
	 * @param sName Name (ID) of result
	 * @param sValue Value of result
	 */
	CustomResult(String sName,String sValue)
	{
		this.sName=sName;
		this.sValue=sValue;
	}

	/** @return Name (ID) of custom result */
	public String getName() { return sName; }
	/** @return Value of custom result */
	public String getValue() { return sValue; }
}
