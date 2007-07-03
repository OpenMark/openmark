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

import java.io.IOException;

import org.w3c.dom.Element;

import om.*;
import om.OmDeveloperException;
import om.stdquestion.*;
import om.stdquestion.QComponent;
import util.xml.XML;

/** 
Allows users to play MP3 audio files. 
<h2>XML usage</h2>
&lt;centre&gt;...&lt;/centre&gt;
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates all children</td></tr>
<tr><td>filePath</td><td>(string)</td><td>Path of mp3 file relative to question class</td></tr>
</table>
*/
public class AudioComponent extends QComponent
{
	// We are currently using this mp3 player from 
	// http://web.uvic.ca/hrd/halfbaked/howto/audio.htm
	private static final String MP3PLAYERFLASH="hbs_mp3_player.swf";
	
	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "audio";
	}
	
	/** Path of mp3 file relative to question class. */
	public final static String PROPERTY_FILEPATH="filePath";
	
	/** Audio file that was last sent to user */
	private String sSentAudio=null;
	
	@Override
	protected String[] getRequiredAttributes()
	{
		return new String[]
		{
			PROPERTY_FILEPATH,
		};
	}
	
	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineString(PROPERTY_FILEPATH);
	}

	@Override
	protected void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain)
		throws OmException
	{		
		// Add the audio player. Because the resource list is a hashmap it doesn't
		// matter if we do this with several instances of the component
		if(bInit)
		{
			qc.addResource(MP3PLAYERFLASH,
				"application/x-shockwave-flash",getClassResource(MP3PLAYERFLASH));		
		}

		// Work out audio file and name
		String sFilePath=getQuestion().applyPlaceholders(getString(PROPERTY_FILEPATH));
		String sName=sFilePath;
		int iLastSlash=sName.lastIndexOf('/');
		if(iLastSlash!=-1) sName=sName.substring(iLastSlash+1);
		
		if(!sFilePath.equals(sSentAudio))
		{
			sSentAudio=sFilePath;
			byte[] abAudio;
			try 
			{
				abAudio=getQuestion().loadResource(sFilePath);
			} 
			catch (IOException e) 
			{
				throw new OmException("<audio>: Audio file not found: "+sFilePath,e);
			}
			qc.addResource(sName,"audio/mp3",abAudio);
		}
		
		// Create html object tag (based on sample code at 
		// http://web.uvic.ca/hrd/halfbaked/howto/audio.htm)
		Element eObject=qc.createElement("object");
		qc.addInlineXHTML(eObject);
		String sPlayer="%%RESOURCES%%/"+MP3PLAYERFLASH;
		eObject.setAttribute("data",sPlayer);
		eObject.setAttribute("width","24");
		eObject.setAttribute("height","17");
		eObject.setAttribute("type","application/x-shockwave-flash");
		createParam(eObject,"type","application/x-shockwave-flash");
		createParam(eObject,"src",sPlayer);
		createParam(eObject,"data",sPlayer);
		createParam(eObject,"codebase",sPlayer);
		createParam(eObject,"FlashVars","TheSound=%%RESOURCES%%/"+sName);
		createParam(eObject,"allowScriptAccess","sameDomain");
		createParam(eObject,"movie",sPlayer);
		createParam(eObject,"loop","false");
		createParam(eObject,"quality","high");
		createParam(eObject,"wmode","transparent");
		
		// Alternate version for if object tag fails
		Element eA=XML.createChild(eObject,"a");
		eA.setAttribute("href","%%RESOURCES%%/"+sName);
		XML.createText(eA,sName);
	}

	private static void createParam(Element eObject,String sName,String sValue)
	{
		Element eParam=XML.createChild(eObject,"param");
		eParam.setAttribute("name",sName);
		eParam.setAttribute("value",sValue);		
	}
}
