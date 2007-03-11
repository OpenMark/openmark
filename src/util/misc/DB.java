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
import java.sql.*;

/** Database utilities */
public abstract class DB
{
	public static void connectDirect(Connection c) 
	{
		try
		{
			Statement s=c.createStatement();	
			PrintStream ps=System.out;
			
			BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
			while(true)
			{
				Thread.sleep(250);
				System.err.println("Enter SQL query:");
				if(!c.getAutoCommit()) c.commit();
				String sLine=br.readLine();
				
				ResultSet rs;
				try
				{
					rs=s.executeQuery(sLine);
				}
				catch(SQLException se)
				{
					System.err.println(se.getMessage());
					continue;
				}
				
				File fCSV=new File("c:/lastquery.csv");
				PrintStream psFile=new PrintStream(new FileOutputStream(fCSV),false);
				
				// Do headers
				ResultSetMetaData rsmd=rs.getMetaData();
				int[] aiColWidth=new int[rsmd.getColumnCount()+1];
				for(int i=1;i<aiColWidth.length;i++)
				{
					String sName=rsmd.getColumnName(i);
					aiColWidth[i]=Math.max(sName.length(),rsmd.getColumnDisplaySize(i));
					
					ps.print("| "+pad(sName,aiColWidth[i],' ')+" ");
					psFile.print(sName+",");
				}
				ps.println();
				psFile.println();
				
				// Do underline
				for(int i=1;i<aiColWidth.length;i++)
				{
					ps.print("| "+pad("",aiColWidth[i],'-')+" ");
				}
				ps.println();
				
				// Do values
				while(rs.next())
				{
					for(int i=1;i<aiColWidth.length;i++)
					{
						ps.print("| "+pad(rs.getString(i),aiColWidth[i],' ')+" ");
						psFile.print(rs.getString(i)+",");
					}
					ps.println();
					psFile.println();
				}
				
				ps.println();
				psFile.close();
			}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}	
	
	private static String pad(String sInput,int iLength,char c)
	{
		if(sInput==null) sInput="";
		if(iLength>100) iLength=100;
		while(sInput.length()<iLength) sInput=sInput+c;
		if(sInput.length() > iLength) sInput=sInput.substring(0,iLength);
		return sInput;
	}
	


}
