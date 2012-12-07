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
package om.equation;

import om.OmDeveloperException;
import om.equation.generated.ParseException;

/** Error in format of equation XML */
public class EquationFormatException extends OmDeveloperException
{
	/** Required by the Serializable interface. */
	private static final long serialVersionUID = -5626001910334901892L;

	EquationFormatException(Item i,String sError)
	{
		super("Error in <equation> format:\n"+
			i+" - "+sError);
	}

	private static String displayException(ParseException pe,String sEquation)
	{
		// Find error location
		int iColumn=-1,iLine=-1;
		if(pe.currentToken!=null && pe.currentToken.next!=null)
		{
			iColumn=pe.currentToken.next.beginColumn;
			iLine=pe.currentToken.next.beginLine;
		}

		String sReturn="Error in <equation>:\n\n"+sEquation+"\n";
		if(iLine==1 && iColumn>=1 && iColumn<=sEquation.length());
		{
			for(int iSpace=1;iSpace<iColumn;iSpace++)
			  sReturn+=" ";
			sReturn+="^\n";
		}

 		sReturn+="\n"+pe.getMessage();
		return sReturn;
	}

	EquationFormatException(ParseException pe,String sEquation)
	{
		super(displayException(pe,sEquation));
	}
}
