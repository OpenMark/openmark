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

import java.awt.Image;
import java.io.IOException;
import java.util.*;

import om.*;
import om.stdquestion.*;

import org.w3c.dom.*;

import util.xml.XML;

/**
Represents a displayed image from a file
<p/>
<h2>XML usage</h2>
&lt;image id="orangePic" filePath="images/orange.png" alt="picture of orange" width="100" height="80"/&gt;
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates children</td></tr>
<tr><td>lang</td><td>(string)</td><td>Specifies the language of the content, like the HTML lang attribute. For example 'en' = English, 'el' - Greek, ...</td></tr>
<tr><td>alt</td><td>(string)</td><td>Alternative text for those who can't use the actual image</td></tr>
<tr><td>width</td><td>(int)</td><td>default displayed width in pixels (optional)</td></tr>
<tr><td>height</td><td>(int)</td><td>default displayedheight in pixels (optional)</td></tr>
<tr><td>filePath</td><td>(string)</td><td>Path to file (relative to class)</td></tr>
</table>
<h3>Changing the image</h3>
The image file and the alt-text can be changed from Java via a call to
<br/>
setImage(String filePath, String altText)
<h3>Place tags</h3>
You can include place tags, &lt;iplace&gt;, within the image
object. Required attributes top, left, and label (for accessibility). Optional
attributes: for (if the label should apply to something other than a single
labelable component within)
*/
public class ImageComponent extends QComponent
{
	private static final String PROPERTY_FILEPATH="filePath";

	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "image";
	}

	/** Alternative text for those who can't use the actual image */
	public final static String PROPERTY_ALT="alt";

	/** path to image file */
	private String sFilePath=null; // Currently-loaded file
	private int iWidth = 30;
	private int iHeight = 30;
	private byte[] imageData;
	private String sMimeType;
	private ArrayList<IPlace> alIPlaces;

	/**
	 * Keep track of resources we added to users so we can save SOAP time by
	 * not transferring them again.
	 */
	private Set<String> sAddedResources=new HashSet<String>();

	/** Represents one &lt;iplace&gt; item -- a pixel location for adding objects*/
	private static class IPlace
	{
		QComponent qcPlaceContent;
		int iLeft, iTop;
		String sLabel,sLabelFor=null;
	}

	/** True if there was whitespace before or after the &lt;image&gt; tag */
	private boolean bSpaceBefore,bSpaceAfter;

	/** Specifies attributes required */
	@Override
	protected String[] getRequiredAttributes()
	{
		return new String[]	{PROPERTY_ALT,PROPERTY_FILEPATH};
	}

	/** Specifies possible attributes */
	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineString(PROPERTY_ALT);
		defineString(PROPERTY_FILEPATH);
		defineInteger("width");
		defineInteger("height");
	}

	/** parses internals of tag to create java component*/
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

		for(Node n=eThis.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{

				Element e=(Element)n;
				if(e.getTagName().equals("iplace"))
				{	//handle iplace
					if (alIPlaces == null) alIPlaces = new ArrayList<IPlace>(10);
					IPlace p =new IPlace();
					try
					{
						p.iLeft=Integer.parseInt(e.getAttribute("left"));
						p.iTop=Integer.parseInt(e.getAttribute("top"));
					}
					catch(NumberFormatException nfe)
					{
						throw new OmFormatException(
						"<image> <iplace>: top and left = must be integers");
					}
					p.qcPlaceContent = getQDocument().build(this,e,"t");
					if(!e.hasAttribute("label"))
						throw new OmFormatException(
							"<image> <iplace>: requires label= attribute");
					p.sLabel=e.getAttribute("label");
					if(e.hasAttribute("for"))
						p.sLabelFor=e.getAttribute("for");
					else
					{
						QComponent[] aqcKids=p.qcPlaceContent.getComponentChildren();
						if(aqcKids.length==1 && aqcKids[0] instanceof Labelable)
						p.sLabelFor=aqcKids[0].getID();
					}

					alIPlaces.add(p);
					addChild(p.qcPlaceContent);
				}
				else
				{
					throw new OmDeveloperException("<image> can only contain <iplace> tags");
				}

				// text data is ignored
			}
		}
	}

	/** @return FilePath of Image
	 * @throws OmDeveloperException */
	public String getFilePath() throws OmDeveloperException
	{
		return getString(PROPERTY_FILEPATH);
	}

	/**
	 * Sets the Image file path and alt text.
	 * <p>
	 * @param sFilePath New value for filePath
	 * @param sAlt New value for screenreader alternative
	 * @throws OmDeveloperException -- when?
	 */
	public void setImage(String sFilePath,String sAlt) throws OmDeveloperException
	{
		setString(PROPERTY_FILEPATH,sFilePath);
		setString(PROPERTY_ALT,sAlt);
	}


	/**
	 * Checks whether the image dimensions have been specified in the xml.
	 * <p> If not them it reads the size from the image data.
	 * <p>
	 * @throws OmException if image data does not contain size information
	 */
	protected void findImageSize() throws OmException
	{
		if (isPropertySet("width") && isPropertySet("height"))
		{
			iWidth = getInteger("width");
			iHeight = getInteger("height");
			return;
		}

		try
		{
			Image im = util.misc.ImageUtils.load(imageData);
			iWidth = im.getWidth(null);
			iHeight = im.getHeight(null);
		}
		catch(Exception e)
		{
			throw new OmException("Can't read image size from file "+sFilePath,e);
		}
	}

	/** creates web page output from java component */
	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		double dZoom=getQuestion().getZoom();
		if(bPlain)
		{
			// Put text equivalent
			Element eDiv=qc.createElement("div");
			addLangAttributes(eDiv);
			qc.addInlineXHTML(eDiv);
			XML.createText(eDiv,getString(PROPERTY_ALT));
			qc.addTextEquivalent(getString(PROPERTY_ALT));

			// Put each placeholder
			if (alIPlaces != null)
				for (int i=0; i < alIPlaces.size(); i++)
				{
					IPlace ip = alIPlaces.get(i);

					// Check content is not hidden
					if(!ip.qcPlaceContent.isChildDisplayed()) continue;

					Element ePlace=XML.createChild(eDiv,"div");
					if(!("".equals(ip.sLabel)))
					{
						Element eLabel=XML.createChild(ePlace,ip.sLabelFor==null ? "span" : "label");
						XML.createText(eLabel,ip.sLabel+" ");
						qc.addTextEquivalent(ip.sLabel);
						if(ip.sLabelFor!=null) eLabel.setAttribute("for",
							LabelComponent.getLabel(getQDocument(),bPlain,ip.sLabelFor));
					}
					qc.setParent(ePlace);
					ip.qcPlaceContent.produceOutput(qc,bInit,bPlain);
					qc.unsetParent();
				}
		}
		else
		{
			/** loads image data from file if not yet loaded */
			if (sFilePath == null || !sFilePath.equals(getString(PROPERTY_FILEPATH)))
			{
				sFilePath=getString(PROPERTY_FILEPATH);

				//get image mime type
				String sFL = sFilePath.toLowerCase();
				if (sFL.endsWith(".jpg")) sMimeType = "image/jpeg";
				else if(sFL.endsWith(".gif")) sMimeType = "image/gif";
				else if(sFL.endsWith(".png")) sMimeType = "image/png";
				else if (sFL.endsWith(".jpeg")) sMimeType = "image/jpeg";
				else if(sFL.endsWith(".jpe")) sMimeType = "image/jpeg";
				else throw new OmException("Invalid image file type: "+sFilePath);

				try
				{
					imageData = getQuestion().loadResource(sFilePath);
					findImageSize();
				}
				catch (IOException e)
				{
					throw new OmException("Image file not found: "+sFilePath,e);
				}
				if(!sAddedResources.contains(sFilePath))
				{
					sAddedResources.add(sFilePath);
					qc.addResource(sFilePath,sMimeType,imageData);
				}
			}

			// Outer element
			Element eEnsureSpaces=qc.createElement("div");
			eEnsureSpaces.setAttribute("class","image");
			addLangAttributes(eEnsureSpaces);
			qc.addInlineXHTML(eEnsureSpaces);

			// work out actual w/h
			int
				iActualWidth=(int)(iWidth*dZoom+0.5),
				iActualHeight=(int)(iHeight*dZoom+0.5);

			// If there's a space before, add one here too (otherwise IE eats it)
			if(bSpaceBefore)
				XML.createText(eEnsureSpaces," ");

			// Create image tag
			Element eImg=XML.createChild(eEnsureSpaces,"img");
			eImg.setAttribute("id",QDocument.ID_PREFIX+getID());
			eImg.setAttribute("onmousedown","return false;"); // Prevent Firefox drag/drop
			eImg.setAttribute("ondragstart","return false;"); // Prevent IE drag/drop
			eImg.setAttribute("src","%%RESOURCES%%/"+sFilePath);
			eImg.setAttribute("alt",getString(PROPERTY_ALT));
			eImg.setAttribute("style","vertical-align:0px;"); // Is this needed?
			eImg.setAttribute("width",""+iActualWidth);
			eImg.setAttribute("height",""+iActualHeight);

			if(bSpaceAfter)
				XML.createText(eEnsureSpaces," ");

			String sJavascript="addOnLoad(function() { inlinePositionFix('"+QDocument.ID_PREFIX+getID()+"'";

			/** if required, add span tags and insert content from alIplaces[] */
			if (alIPlaces != null && alIPlaces.size()>0)
			{
				for (int i=0; i < alIPlaces.size(); i++)
				{
					IPlace ip = alIPlaces.get(i);

					// Must get the label even though not using it, just to indicate that
					// it's been provided
					if(ip.sLabelFor!=null)
						LabelComponent.getLabel(getQDocument(),bPlain,ip.sLabelFor);

					int
						iActualX=(int)Math.round(dZoom * ip.iLeft),
						iActualY=(int)Math.round(dZoom * ip.iTop);

					// Should be div w/ display:inline not span as otherwise it won't
					// technically be valid if we put divs in it
					Element ePlace = XML.createChild(eEnsureSpaces,"div");
					String sPlaceholderID=QDocument.ID_PREFIX+getID()+"_"+i;
					ePlace.setAttribute("class","placeholder");
					ePlace.setAttribute("id",sPlaceholderID);

					// Build contents
					qc.addTextEquivalent(ip.sLabel);
					qc.setParent(ePlace);
					ip.qcPlaceContent.produceOutput(qc,bInit,bPlain);
					qc.unsetParent();

					// Update JS
					sJavascript+=
						",['"+sPlaceholderID+"',"+iActualX+","+iActualY+"]";
				}
				sJavascript+="); });";

				Element eScript=XML.createChild(eEnsureSpaces,"script");
				eScript.setAttribute("type","text/javascript");
				XML.createText(eScript,sJavascript);
			}

		}
	}

} // end of Image Component class
