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
package om.stdcomponent;

import java.awt.Color;
import java.util.*;

import om.*;
import om.stdquestion.*;

import org.w3c.dom.*;

import util.xml.XML;

/***
This is a component to layout components as a table.<br/>
The table component can only contain &lt;row> and &lt;title> elements directly.<br/>
Rows can only contain &lt;t> (text) elements (each defines a cell), but these can contain things such as 
images, text input areas and also formatting elements such as  &lt;centre> <br/>
The title is placed as a string a the top of the table and is set 
to be the caption for improved accessibillity.
<h2>Example XML usage</h2>
&lt;table cols='2' rows='3' left='1' head='1' &gt; <br/>
&lt;title>This is a table of fruit colours&lt;/title><br/>
&lt;row>&lt;t>Fruit&lt;/t>&lt;t>Colour&lt;/t>&lt;/row><br/>
&lt;row>&lt;t>Apple&lt;/t>&lt;t>red   &lt;/t>&lt;/row><br/>
&lt;row>&lt;t>Lemon&lt;/t>&lt;t>yellow&lt;/t>&lt;/row><br/>
&lt;/table>

<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>cols</td><td>(integer)</td><td>total number of columns in the table</td></tr>
<tr><td>rows</td><td>(integer)</td><td>total number of rows in the table</td></tr>
<tr><td>head</td><td>(integer)</td><td>number of header rows</td></tr>
<tr><td>foot</td><td>(integer)</td><td>number of footer rows</td></tr>
<tr><td>left</td><td>(integer)</td><td>number of label columns on left</td></tr>
<tr><td>right</td><td>(integer)</td><td>number of label columns on right</td></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
</table>
<br/>

*/
public class TableComponent extends QComponent
{
	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "table";
	}
	
	
	/** @return array of required attributes for tag */
	protected String[] getRequiredAttributes()
	{
		return new String[]
		{
			"cols","rows"
		};
	}
	
	/** Defines possible attributes */
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineInteger("cols");
		defineInteger("rows");
		defineInteger("head");
		defineInteger("foot");
		defineInteger("left");
		defineInteger("right");
	}
	
	/** Question author callable funtion sets dimensions of table */
	public void setSize(int iNewRows, int iNewCols)
	{
		iRows = iNewRows;
		iCols = iNewCols;
	}
	
	private int iCols;  // columns for table		
	private int iRows;  // rows for table
	private int iHead;  // number of header rows
	private int iFoot;  // number of footer rows
	private int iLeft;  // number of label columns on left
	private int iRight; // number of label columns on right
	
	private ArrayList tableElements; // array of table elements (columns etc) 
	private QComponent qcTitle; // title of table
	
	/** inner class which represents a table row */
	class tableRow
	{
		QComponent[] aqcCells;
	}

	private int getIntegerIfDefined(String sName, int iDefault)throws OmException
	{
		if (isPropertySet(sName)) return getInteger(sName);
		else return iDefault;
	}
	
	protected void initChildren(Element eThis) throws OmException
	{
		iRows = getInteger("rows");
		iCols = getInteger("cols");
		
		iHead = getIntegerIfDefined("head",0);
		iFoot = getIntegerIfDefined("foot",0);
		iLeft = getIntegerIfDefined("left",0);
		iRight = getIntegerIfDefined("right",0);

		tableElements = new ArrayList(10);
		for(Node nChild=eThis.getFirstChild();nChild!=null;nChild=nChild.getNextSibling())
		{
			if(nChild instanceof Element)
			{
				Element e=(Element)nChild;
				if(e.getTagName().equals("row"))
				{
					tableRow tr = new tableRow();
					buildRow(tr,e);
					tableElements.add(tr);
				}
				else if (e.getTagName().equals("title"))
				{
					qcTitle = getQDocument().build(this,e,"t");
				}
				else throw new OmFormatException(
				e.getTagName()+"<table> may only contain <row> and <title> tags");
			}	
		}
	}
	
	protected void buildRow(tableRow tr, Element eRow) throws OmException
	{
		LinkedList lCells = new LinkedList();

		for(Node n=eRow.getFirstChild();n!=null;n=n.getNextSibling()) // loop through cells
		{
			if(n instanceof Element)
			{
				Element e=(Element)n;
				QComponent qcCell = getQDocument().build(this,e,null);
				lCells.add(qcCell); // Store in Linked list of cells
				addChild(qcCell);// Also store in standard child array so it can be found etc.
			}
		}
		tr.aqcCells = (QComponent[]) lCells.toArray(new QComponent[0]);
		// throw new OmException("size of row =" + lCells.size());
	}
	
	
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		Element eTable=qc.createElement("table");
		eTable.setAttribute("border","1");
		eTable.setAttribute("cellpadding","4");
		qc.addInlineXHTML(eTable);

		int iTableElement = 0;
		
		if (qcTitle != null) // title set
		{ // create caption from title tag
			Element eTitle = XML.createChild(eTable,"caption");	
			qc.setParent(eTitle);
			qcTitle.produceOutput(qc,bInit,bPlain);
			qc.unsetParent();
		}
		Element eTBody=XML.createChild(eTable,"tbody");
		for (int iRow = 0; iRow < iRows; iRow++)
		{ // create rows
			tableRow tr = (tableRow) tableElements.get(iTableElement++);

			Element eRow = XML.createChild(eTBody,"tr");
			Element eCell;
			
			for(int iCell = 0; iCell < iCols; iCell++)
			{ // create cells
				if (iRow < iHead)
				{ // column heading
					eCell = XML.createChild(eRow,"th");
					eCell.setAttribute("scope","col");
				}
				else if (iCell < iLeft)  
				{ // row label
					eCell = XML.createChild(eRow,"th");
					eCell.setAttribute("scope","row");
					if (iRow >= (iRows-iFoot)) eCell.setAttribute("class","foot");
					else eCell.setAttribute("class","left");
				}
				else // td cells
				{
					eCell = XML.createChild(eRow,"td");
					if (iRow >= (iRows-iFoot)) eCell.setAttribute("class","foot");
					else if (iCell >= (iCols - iRight))eCell.setAttribute("class","right");
				}
				
				qc.setParent(eCell);
				if (iCell < tr.aqcCells.length) 
					tr.aqcCells[iCell].produceOutput(qc,bInit,bPlain);
				qc.unsetParent();
			}
			
		}
	}
	
	protected Color getChildBackground(QComponent qcChild)
	{
		try
		{
			// Find out whether it's in a heading or not...
			int iTableElement = 0;
			for (int iRow = 0; iRow < iRows; iRow++)
			{ // rows
				tableRow tr = (tableRow) tableElements.get(iTableElement++);
				for(int iCell = 0; iCell < iCols; iCell++)
				{ // cells
					if (iCell < tr.aqcCells.length && tr.aqcCells[iCell]==qcChild)
					{
						// Found it! Check heading...
						if (iRow < iHead || iCell < iLeft || iRow >= (iRows-iFoot) || iCell >= (iCols - iRight))
							return convertRGB("innerbghi");
						else
							return convertRGB("innerbg");
					}
				}
			}
			
			return convertRGB("innerbg");
		}
		catch(OmDeveloperException e)
		{
			throw new OmUnexpectedException(e);
		}
	}
	
	
}  // end of class
