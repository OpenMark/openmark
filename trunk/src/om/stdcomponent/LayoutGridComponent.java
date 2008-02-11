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

import java.util.Random;

import om.*;
import om.stdquestion.QComponent;
import om.stdquestion.QContent;

import org.w3c.dom.Element;

import util.xml.XML;

/**
A layout grid that arranges child components in a certain number of equal-size
columns.
<h2>XML usage</h2>
&lt;layoutgrid cols='2'&gt;...&lt;/layoutgrid&gt;
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>cols</td><td>(integer)</td><td>Number of columns</td></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates children</td></tr>
<tr><td>lang</td><td>(string)</td><td>Specifies the language of the content, like the HTML lang attribute. For example 'en' = English, 'el' - Greek, ...</td></tr>
<tr><td>shuffle</td><td>(boolean)</td><td>If true, randomises order of children</td></tr>
<tr><td>widths</td><td>(string)</td><td>Comma-separated lists of percentages
for each column (defaults to equal division) e.g. "30%,30%,40%". Must add up
to 100% and specify every column.</td></tr>
</table>
*/
public class LayoutGridComponent extends QComponent
{
	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "layoutgrid";
	}

	/** Int property: number of columns in grid */
	public final static String PROPERTY_COLS="cols";

	/** Boolean property: whether or not to shuffle the contents */
	public final static String PROPERTY_SHUFFLE="shuffle";

	/** String property: comma-separated list of percentage widths */
	public final static String PROPERTY_WIDTHS="widths";

	/** Order of components */
	private int[] aiShuffleMap;

	@Override
	protected String[] getRequiredAttributes()
	{
		return new String[]
		{
			PROPERTY_COLS
		};
	}

	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineInteger(PROPERTY_COLS);
		defineBoolean(PROPERTY_SHUFFLE);
		defineString(PROPERTY_WIDTHS);
		setBoolean(PROPERTY_SHUFFLE,false);
	}


	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		Element eOuter=qc.createElement("div");
		qc.addInlineXHTML(eOuter);

		if(!bPlain)
			eOuter.setAttribute("class","layoutgrid");

		addLangAttributes(eOuter);

		int iCols=getInteger(PROPERTY_COLS);
		boolean bShuffle=getBoolean(PROPERTY_SHUFFLE);

		// Calculate widths
		String[] asWidthCSS=new String[iCols];
		if(isPropertySet(PROPERTY_WIDTHS))
		{
			String[] as=getString(PROPERTY_WIDTHS).split(",");
			if(as.length!=iCols)
				throw new OmFormatException("<layoutgrid>: widths= must contain a " +
					"comma-separated list of entries, one per column");
			try
			{
				int iTotal=0;
				for(int i=0;i<iCols;i++)
				{
					if(!as[i].endsWith("%"))
						throw new OmFormatException("<layoutgrid>: Each entry in widths= " +
							"must end with a % sign");

					int iW=Integer.parseInt(as[i].substring(0,as[i].length()-1));
					iTotal+=iW;
					if(i==iCols-1)
						asWidthCSS[i]=(iW-1)+".99%";
					else
						asWidthCSS[i]=iW+"%";
				}
				if(iTotal!=100)
					throw new OmFormatException("<layoutgrid>: Entries in widths= " +
					"must sum to 100%");
			}
			catch(NumberFormatException nfe)
			{
				throw new OmFormatException("<layoutgrid>: Each entry in widths= " +
					"must be a valid integer followed by % sign");
			}
		}
		else
		{
			int iLeftOver=100;
			int iW=(100/iCols);
			for(int i=0;i<iCols-1;i++)
			{
				iLeftOver-=iW;
				asWidthCSS[i]=iW+"%";
			}
			asWidthCSS[iCols-1]=(iLeftOver-1)+".99%";
		}

		QComponent[] aqcChildren=getComponentChildren();

		if(aiShuffleMap==null || aiShuffleMap.length!=aqcChildren.length)
		{
			// Init default shuffle map (unshuffled)
			aiShuffleMap=new int[aqcChildren.length];
			for(int i=0;i<aiShuffleMap.length;i++)
			{
				aiShuffleMap[i]=i;
			}

			// Shuffle it if desired
			if(bShuffle)
			{
				// Get new RNG (means it's repeatable, and doesn't affect question's
				// other use of random numbers; does mean that two lists of same length
				// in same question will be shuffled in same way).
				Random r=getQuestion().getRandom();

				int[] aiNew=new int[aiShuffleMap.length];
				for(int iOut=0;iOut<aiNew.length;iOut++)
				{
					int iPick=r.nextInt(aiNew.length-iOut);
					int iCount=0;
					for(int iIn=0;iIn<aiShuffleMap.length;iIn++)
					{
						if(aiShuffleMap[iIn]!=-1)
						{
							if(iPick==iCount)
							{
								aiNew[iOut]=aiShuffleMap[iIn];
								aiShuffleMap[iIn]=-1;
								break;
							}
							iCount++;
						}
					}
				}
				aiShuffleMap=aiNew;
			}
		}

		Element eRow=null;
		int iChild;
		for(iChild=0;iChild<aqcChildren.length;iChild++)
		{
			int iThisCol=iChild%iCols;
			if(!bPlain)
			{
				if(iThisCol==0)
				{
					eRow=XML.createChild(eOuter,"div");
					eRow.setAttribute("class","layoutgridrow");
				}
			}

			Element eInner;
			if(!bPlain)
			{
				Element eItem=XML.createChild(eRow,"div");
				eItem.setAttribute("style","width:"+asWidthCSS[iThisCol]);
				eItem.setAttribute("class","layoutgriditem");
				eInner=XML.createChild(eItem,"div");
				eInner.setAttribute("class","layoutgridinner");
			}
			else
			{
				eInner=XML.createChild(eOuter,"div");
			}
			qc.setParent(eInner);
			aqcChildren[aiShuffleMap[iChild]].produceOutput(qc,bInit,bPlain);
			qc.unsetParent();
			if( ((iChild+1) % iCols)==0 || (iChild+1)==aqcChildren.length )
			{
				// Exit float
				if(!bPlain)
					XML.createChild(eRow,"div").setAttribute("class","clear");
			}
		}
	}

	@Override
	protected boolean wantsFilledChildren()
	{
		return true;
	}
}
