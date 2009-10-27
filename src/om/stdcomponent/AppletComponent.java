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

import om.OmDeveloperException;
import om.OmException;
import om.OmFormatException;
import om.question.ActionParams;
import om.stdquestion.QComponent;
import om.stdquestion.QContent;
import om.stdquestion.QDocument;

import org.w3c.dom.Element;

import util.xml.XML;

/**
 * Inserts a pushbutton that users can press to open the an applet specified in the &lt;applet&gt; tag
 * component. A popup window will appear, containing the applet itself plus
 * 'OK' button.
 * <p>
 * If the user clicks 'OK', the string returned by the function answer() in the applet is updated and
 * (if there is an action set) the form will be submitted.
 * <p>
 * The popup remains visible during one question. If the user begins a new
 * question (or restarts the same one) it will vanish. It will also vanish if they
 * navigate to another website.
 * <p>
 * <h2>XML usage</h2>
 * &lt;applet id='myapp' action='actionSubmit'/&gt;
 * <h2>Properties</h2>
 * <table border="1">
 * <tr><th>Property</th><th>Values</th><th>Effect</th></tr>
 * <tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
 * <tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
 * <tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates all children</td></tr>
 * <tr><td>lang</td><td>(string)</td><td>Specifies the language of the content, like the HTML lang attribute. For example 'en' = English, 'el' - Greek, ...</td></tr>
 * <tr><td>label</td><td>(string)</td><td>Label for button (default is 'Run Applet')</td></tr>
 * <tr><td>action</td><td>(string)</td><td>Name of method in question class that
 * is called after user clicks OK. Optional, default is to not submit form, but
 * you probably want to do it.</td></tr>
 * <tr><td>filePath</td><td>(string)</td><td>Path of applet jar file. 
 * (may be relative path including /; may not be absolute path beginning /)
 * Applet jar should be packaged with question jar.</td></tr>
 * <tr><td>className</td><td>(string)</td><td>Name of main applet class.</td></tr>
 * <tr><td>params</td><td>(string)</td><td>Parameter string for the applet. eg. params="param1=xxx,param2=yyy".
 * "params" is a predefined parameter in applet component, use getParameter("params") in your applet 
 * to get the parameter string and split that into individual paramaters.</td></tr>
 * <tr><td>width</td><td>(string)</td><td>Width of the applet window.</td></tr>
 * <tr><td>height</td><td>(string)</td><td>Height of the applet window.</td></tr>
 * </table>
 *
 */
public class AppletComponent extends QComponent
{
	/** Name of method in question class that is called after user clicks OK */
	public static final String PROPERTY_ACTION="action";
	/** Label for button. */
	public static final String PROPERTY_LABEL="label";
	/** Path of applet jar file. */
	private static final String PROPERTY_FILEPATH="filePath";
	/** Name of main applet class. */
	private static final String PROPERTY_CLASSNAME="className";
	/** Parameters for the applet. */
	private static final String PROPERTY_PARAMS="params";
	/** Width of the applet window. */
	private static final String PROPERTY_WIDTH="width";
	/** Height of the applet window. */
	private static final String PROPERTY_HEIGHT="height";		
	
	/** Current (most recently set) value */
	private String sValue;	

	/** Random token used to check when user goes to different window */
	private String sToken;

	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "applet";
	}

	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineString(PROPERTY_LABEL);
		setString(PROPERTY_LABEL,"Run Applet");
		defineString(PROPERTY_ACTION);
		defineString(PROPERTY_FILEPATH);
		defineString(PROPERTY_CLASSNAME);
		setString(PROPERTY_FILEPATH, null);
		setString(PROPERTY_CLASSNAME, null);
		defineString(PROPERTY_PARAMS);
		setString(PROPERTY_PARAMS, "");
		defineInteger(PROPERTY_WIDTH);
		setInteger(PROPERTY_WIDTH, 300);
		defineInteger(PROPERTY_HEIGHT);
		setInteger(PROPERTY_HEIGHT, 300);
	}

	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		if(XML.getChildren(eThis).length!=0)
			throw new OmFormatException("<applet>: Cannot contain child components");
	}

	@Override
	protected void initSpecific(Element eThis) throws OmException
	{
		sToken="t"+getQuestion().getRandom().nextInt()+getID().hashCode();
		getQuestion().checkCallback(getString(PROPERTY_ACTION));
	}

	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		Element eScript=qc.createElement("script");
		eScript.setAttribute("type","text/javascript");
		XML.createText(eScript,"appInit('"+sToken+"');");
		qc.addInlineXHTML(eScript);

		int iWidth = getInteger(PROPERTY_WIDTH);
		int iHeight = getInteger(PROPERTY_HEIGHT);
		String className=getString(PROPERTY_CLASSNAME);
		String sFilePath=getString(PROPERTY_FILEPATH);
		String params=getString(PROPERTY_PARAMS);
		String bLabel=getString(PROPERTY_LABEL);
		
		Element eInput=qc.createElement("input");
		eInput.setAttribute("type","hidden");
		String sInputID=QDocument.ID_PREFIX+QDocument.VALUE_PREFIX+getID();
		eInput.setAttribute("name",sInputID);
		eInput.setAttribute("id",sInputID);
		eInput.setAttribute("value",sValue);
		qc.addInlineXHTML(eInput);

		if(isPropertySet(PROPERTY_ACTION)){
			eInput=qc.createElement("input");
			eInput.setAttribute("type","hidden");
			String sActionID=QDocument.ID_PREFIX+QDocument.ACTION_PREFIX+getID();
			eInput.setAttribute("name",sActionID);
			eInput.setAttribute("id",sActionID);
			eInput.setAttribute("value","submit");
			eInput.setAttribute("disabled","disabled"); // Disabled unless submitted this way
			qc.addInlineXHTML(eInput);
		}

		eInput=qc.createElement("input");
		String sButtonID=QDocument.ID_PREFIX+getID()+"_button";
		eInput.setAttribute("id",sButtonID);
		eInput.setAttribute("type","button");
		eInput.setAttribute("value",bLabel);
		if(!isEnabled()) eInput.setAttribute("disabled","disabled");
		eInput.setAttribute("onclick","appClick('%%RESOURCES%%','"+getID()+"','"+QDocument.ID_PREFIX+"'" +
				",'"+sFilePath+"','"+className+"',"+iWidth+","+iHeight+",'"+params+"')");
		qc.addInlineXHTML(eInput);
		if(isEnabled())	qc.informFocusable(sButtonID,bPlain);

		if(bInit)		{
	
			try
			{
				qc.addResource(sFilePath,"application/java-archive", getQuestion().loadResource(sFilePath));
			}
			catch(IllegalArgumentException e)
			{
				throw new OmException("Error loading applet jar",e);
			}
			catch(IOException e)
			{
				throw new OmException("Error loading applet jar",e);
			}
		}
	}

	/**
	 * @return Response string that was set, or empty string if none was set.
	 */
	public String getResponse()
	{
		return sValue;
	}

	@Override
	protected void formSetValue(String newValue,ActionParams ap) throws OmException
	{
		this.sValue=newValue;
	}

	@Override
	protected void formCallAction(String newValue,ActionParams ap) throws OmException
	{
		getQuestion().callback(getString(PROPERTY_ACTION));
	}
}
