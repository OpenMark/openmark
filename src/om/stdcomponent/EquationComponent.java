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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import om.*;
import om.equation.Equation;
import om.stdquestion.*;

import org.w3c.dom.*;

import util.xml.XML;

/** 
Represents  equations that are produced as bitmaps. Uses a LaTeX-like format
that isn't sensitive to whitespace; supports a very, very small subset of what LaTeX
can do.
<p/>
If the equation is simple enough to be displayed in plain XHTML, use 
{@link TextEquationComponent} instead.
<h2>Format examples</h2>
(These examples are intended to show everything we support.) 
<ul>
<li>x+y=z<br/>Simple text. Variables (letters) will be made italic</li>
<li>x^2<br/>Superscript</li>
<li>x_1<br/>Subscript</li>
<li>x^{2y}<br/>Subscript with more than one character</li>
<li>x^1_3<br/>Subscript and superscript on same thing</li>
<li>^1_3<br/>Subscript and superscript on their own rather than being attached
to a letter, e.g. for atomic numbers</li>
<li>\frac{3}{4}<br/>Fraction (3/4 in this case)</li>
<li>\sqrt{x^2}<br/>Square root</li>
<li>\sin y<br/>Displays some standard functions so they don't come out in italics</li>
<li>\mbox{text with space}<br/>Displays text non-italic with whitespace</li>
<li>\sum_{i=1}^{10}<br/>Sum from i=1 to 10</li>
<li>\int_{i=1}^{10}<br/>Integral from i=1 to 10</li>
<li>\Delta x = 2 \pi r<br/>Include any Greek letter, upper-case ones as shown.
Must have space after letter name, i.e. \Deltax or \pir would confuse it.</li>
<li>\textstyle\sum_{i=1}^{10}<br/>Sum from i=1 to 10, but using the style it 
would normally use inside a fraction (with limits to the right rather than 
above/below). The full set of textsize-changing constants are \displaystyle 
(= the normal 'outer' size), \textstyle (in one level of fractions), 
\scriptstyle (super/subscript), and \scriptscriptstyle. </li>
<li>\left( i^2 \right)<br/>Use 'stretchy brackets' that get taller to match their
content. You need to pair \left and \right, but the symbols after them don't have
to match. Available symbols are ( ) { } [ ] | and . - this last doesn't display
at all, so it can be used when you don't really want a matching \right.</li> 
</ul>
<h2>XML usage</h2>
&lt;equation alt="z squared = x squared plus y squared"&gt;z^2=x^2+y^2&lt;/equation&gt;
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates children</td></tr>
<tr><td>alt</td><td>(string)</td><td>Alternative text for those who can't read the bitmap</td></tr>
<tr><td>textfont</td><td>(boolean)</td><td>Uses default text font instead of the equation font</td></tr>
</table>
<h2>Contents</h2>
You can put other things within equations using &lt;eplace&gt; tags. At the point
in your equation that you'd like to include something, do 
&lt;eplace width="100" height="50" label="Numerator"&gt;...Om component/s...&lt;/place&gt;. 
A few components (currently just {@link EditFieldComponent}) are capable of 
estimating their own width and height, so you can leave out width and height
if what you're putting in is one of these. The label is used for accessibility
and should probably be a repeat of something from the equation's alt text (so you can tell which 
bit of the equation it is). You may also include a for= attribute for the label,
which makes it apply to a specific component; otherwise it by default applies
to any single labelable component inside the &lt;eplace>.  
*/
public class EquationComponent extends QComponent
{
	/** String property: text alternative for equation (can include placeholders) */
	public final static String PROPERTY_ALT="alt";
	/** Boolean property: if true, uses Verdana instead of Times */
	public final static String PROPERTY_TEXTFONT="textfont";
	
	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "equation";
	}
	
	/** Actual content of required equation */
	private String sEquation;
	
	/** Equation hash/filename last sent (to track whether we need to send it again) */
	private String sSent=null;
	
	/** Array of placeholders */
	private Place[] apPlaces;
	
	/** Represents one &lt;place&gt; item */
	private static class Place
	{
		String sID;
		QComponent qc;
		int iWidth,iHeight;
		int iActualX,iActualY;
		String sLabel,sLabelFor=null;		
		boolean bImplicit;
	}
	
	/** True if there was whitespace before or after the &lt;equation&gt; tag */
	private boolean bSpaceBefore,bSpaceAfter;
	
	@Override
	protected String[] getRequiredAttributes()
	{
		return new String[]
		{
			PROPERTY_ALT,
		};
	}
	
	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineString(PROPERTY_ALT);		
		defineBoolean(PROPERTY_TEXTFONT);
		setBoolean(PROPERTY_TEXTFONT,false);
	}
	
	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		Node nPrevious=eThis.getPreviousSibling();
		if(nPrevious!=null && nPrevious instanceof Text)
		{
			String sText=((Text)nPrevious).getData();
			if(sText.length()>0 && Character.isWhitespace(sText.charAt(sText.length()-1)))
				bSpaceBefore=true;
		}
		Node nAfter=eThis.getNextSibling();
		if(nAfter!=null && nAfter instanceof Text)
		{
			String sText=((Text)nAfter).getData();
			if(sText.length()>0 && Character.isWhitespace(sText.charAt(0)))
				bSpaceAfter=true;
		}
		
		List<Place> lPlaces=new LinkedList<Place>();
		int iPlace=0;
		StringBuffer sbText=new StringBuffer();
		for(Node n=eThis.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element eplace=(Element)n;
				if(!eplace.getTagName().equals("eplace"))
					throw new OmFormatException(
						"<equation> may only contain text and <eplace> tags");
				Element[] aeChildren=XML.getChildren(eplace);
				QComponent qcChild;
				boolean bImplicit=false;
				if(aeChildren.length!=1) // Treats more than one child as inside <t>
				{
					qcChild=getQDocument().build(this,eplace,"t");
					bImplicit=true;
				}
				else // Treats single child as specific component (auto-sizing works) 
					qcChild=getQDocument().build(this,aeChildren[0],null);
				addChild(qcChild); 	// Must be stored in standard child array so it
														// can be found etc.
				
				// See if width/height is specified
				int iWidth,iHeight;
				if(eplace.hasAttribute("width") && eplace.hasAttribute("height"))
				{
					try
					{
						iWidth=Integer.parseInt(eplace.getAttribute("width"));
						iHeight=Integer.parseInt(eplace.getAttribute("height"));
					}
					catch(NumberFormatException nfe)
					{
						throw new OmFormatException(
							"<equation> <eplace>: width= and height= must be integers");
					}
				}
				else
				{
					Dimension d=qcChild.getApproximatePixelSize(); 
					if(d==null)
						throw new OmFormatException(
							"<equation> <eplace>: Except for components that support automatic " +
							"size estimation and fixing, <eplace> must include width= and height=");
					iWidth=d.width;
					iHeight=d.height;
				}
				
				Place p=new Place();
				p.sID="p"+(iPlace++);
				p.qc=qcChild;
				p.iWidth=iWidth;
				p.iHeight=iHeight;
				p.bImplicit=bImplicit;
				if(!eplace.hasAttribute("label"))
					throw new OmFormatException(
						"<equation> <eplace>: Must include label=");
				if(eplace.hasAttribute("label"))
					p.sLabel=eplace.getAttribute("label");
				else
					p.sLabel=null;
				if(eplace.hasAttribute("for"))
					p.sLabelFor=eplace.getAttribute("for");
				else if(qcChild instanceof Labelable)
					p.sLabelFor=qcChild.getID();
				lPlaces.add(p);
				
				// Add in the equation format text representing the placeholder
				sbText.append("\\placeholder{"+p.sID+"}{"+p.iWidth+","+p.iHeight+"}");
			}
			else if(n instanceof Text)
			{
				sbText.append(n.getNodeValue());
			}			
		}
		sEquation=sbText.toString();
		apPlaces=lPlaces.toArray(new Place[0]);
	}
	
	/** @return Text of equation */
	public String getEquation() { return sEquation; }
	
	/** 
	 * Sets the equation to a different value.
	 * <p>
	 * Note that you cannot change equations that include placeholder &lt;eplace&gt; 
	 * tags; nor can you add these to an equation that doesn't have them.
	 * Please scream viscerally at sam, while jabbing a pin threateningly toward 
	 * your voodoo doll, if you require this feature.
	 * <p>
	 * However, you may find that simple text placeholders of the __A__ variety, 
	 * which <em>are</em> supported and which can include LaTeX-like content if
	 * necessary, are sufficient for your needs.
	 * @param sEquation New value for equation content
	 * @param sAlt New value for screenreader alternative 
	 * @throws OmDeveloperException If you try to change an equation that has
	 *   placeholder XML tags
	 */
	public void setEquation(String sEquation,String sAlt) throws OmDeveloperException
	{ 
		if(apPlaces.length!=0) throw new OmDeveloperException(
			"Cannot change equations that contain placeholder XML tags");
		this.sEquation=sEquation;
		setString("alt",sAlt);
	}
	
	private Equation e=null;
	
	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		// Get actual current value of string
		String sCurrent=getQuestion().applyPlaceholders(sEquation);
		
		if(bPlain)
		{
			// Put text equivalent
			Element eDiv=qc.createElement("div"); // Can't use span because they aren't allowed to contain things
			eDiv.setAttribute("style","display:inline");
			qc.addInlineXHTML(eDiv);
			XML.createText(eDiv,(bSpaceBefore?" " : "")+getString("alt")+(bSpaceAfter?" " : ""));
			qc.addTextEquivalent(getString("alt"));
			
			// Put each placeholder
			for(int i=0;i<apPlaces.length;i++)
			{
				// Check content is not hidden
				Place p=apPlaces[i];
				if(
					(!p.bImplicit && !p.qc.isDisplayed()) || 
					(p.bImplicit && !p.qc.isChildDisplayed()))
					continue;
				
				// Label followed by content of placeholder
				Element ePlace=XML.createChild(eDiv,"div");
				if(p.sLabel!=null)
				{
					Element eLabel=XML.createChild(ePlace,p.sLabelFor==null ? "span" : "label");
					XML.createText(eLabel,p.sLabel+" ");
					qc.addTextEquivalent(p.sLabel);
					if(p.sLabelFor!=null) eLabel.setAttribute("for",
						LabelComponent.getLabel(getQDocument(),bPlain,p.sLabelFor));
				}
				qc.setParent(ePlace);
				p.qc.produceOutput(qc,bInit,bPlain);
				qc.unsetParent();
			}
		}
		else
		{			
			// Check background colour, foreground, and zoom
			Color cBackground=getBackground();
			if(cBackground==null) cBackground=Color.white;
			Color cForeground=getQuestion().isFixedColour() 
				? convertRGB(getQuestion().getFixedColourFG()) 
				: Color.black;		
			double dZoom=getQuestion().getZoom();
			
			// Hash to filename/identifier
			String sFilename="eq"+(sCurrent.hashCode()+cBackground.hashCode()*3+
				cForeground.hashCode()*7+(new Double(dZoom)).hashCode()*11)+
				(getBoolean(PROPERTY_TEXTFONT) ? "t" : "e")+".png";
			
			// Make actual image if needed, also get placeholder positions
			if(!sFilename.equals(sSent))
			{
				e=Equation.create(sCurrent,(float)dZoom);
				if(getBoolean(PROPERTY_TEXTFONT))
					e.setFont("Verdana",new int[]{13,11,9});
				BufferedImage bi=e.render(cForeground,cBackground,true);
				qc.addResource(sFilename,"image/png",QContent.convertPNG(bi));
				for(int i=0;i<apPlaces.length;i++)
				{
					Point p=e.getPlaceholder(apPlaces[i].sID);
					apPlaces[i].iActualX=p.x;
					apPlaces[i].iActualY=p.y;				
				}
				sSent=sFilename;
			}
			
			Element eEnsureSpaces=qc.createElement("div");
			eEnsureSpaces.setAttribute("class","equation");
			qc.addInlineXHTML(eEnsureSpaces);
			
			// If there's a space before, add one here too (otherwise IE eats it)
			if(bSpaceBefore)
				XML.createText(eEnsureSpaces," ");			
			
			String sImageID=QDocument.ID_PREFIX+getID()+"_img";
			Element eImg=XML.createChild(eEnsureSpaces,"img");			
			eImg.setAttribute("id",sImageID);
			eImg.setAttribute("onmousedown","return false;"); // Prevent Firefox drag/drop
			eImg.setAttribute("src","%%RESOURCES%%/"+sFilename);
			eImg.setAttribute("alt",getString("alt"));
			eImg.setAttribute("style","vertical-align:-"+(e.getHeight()-e.getBaseline())+"px;");
			
			if(bSpaceAfter)
				XML.createText(eEnsureSpaces," ");
			
			qc.addTextEquivalent(getString("alt"));
			
			String sJavascript="addOnLoad(function() { inlinePositionFix('"+sImageID+"'";
			
			for(int i=0;i<apPlaces.length;i++)
			{
				Place p=apPlaces[i];
				
				// Must get the label even though not using it, just to indicate that
				// it's been used 
				if(p.sLabelFor!=null)
					LabelComponent.getLabel(getQDocument(),bPlain,p.sLabelFor);

				int 
					iEffectiveWidth=(int)Math.round(dZoom*p.iWidth),
					iEffectiveHeight=(int)Math.round(dZoom*p.iHeight);
				
				String sPlaceholderID=QDocument.ID_PREFIX+getID()+"_"+p.sID;
				Element ePlace=XML.createChild(eEnsureSpaces,"div");
				ePlace.setAttribute("class","placeholder");
				ePlace.setAttribute("id",sPlaceholderID);																							
				ePlace.setAttribute("style",
					"width:"+iEffectiveWidth+"px; " +
					"height:"+iEffectiveHeight+"px; " +
					"visibility:hidden;");
				
				QComponent qcPlaceComponent=p.qc;
				
				if(qcPlaceComponent.isPropertyDefined(PROPERTY_FORCEWIDTH) &&
					qcPlaceComponent.isPropertyDefined(PROPERTY_FORCEHEIGHT))
				{
					qcPlaceComponent.setInteger(PROPERTY_FORCEWIDTH,iEffectiveWidth);
					qcPlaceComponent.setInteger(PROPERTY_FORCEHEIGHT,iEffectiveHeight);
				}
			
				if(p.sLabel!=null) qc.addTextEquivalent(p.sLabel);
				qc.setParent(ePlace);
				qcPlaceComponent.produceOutput(qc,bInit,bPlain);
				qc.unsetParent();
				
				sJavascript+=
					",['"+sPlaceholderID+"',"+p.iActualX+","+p.iActualY+"]";
			}
			sJavascript+="); });";
			
			if(apPlaces.length>0) // No JS needed if there weren't any placeholders
			{
				Element eScript=XML.createChild(eEnsureSpaces,"script");
				eScript.setAttribute("type","text/javascript");
				XML.createText(eScript,sJavascript);
			}
		}
	}
}
