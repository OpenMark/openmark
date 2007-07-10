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
package om.stdquestion;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import om.*;
import om.question.ActionParams;

import org.w3c.dom.*;

import util.misc.IO;

/** Base class for question components. */
public abstract class QComponent
{
	private final static boolean DEBUG_LOGIDS=false;
	
	// Property names
	
	/** 'display' boolean property (whether something should appear) */
	public static final String PROPERTY_DISPLAY="display";
	/** 'enabled' boolean property (whether something should be greyed out or active) */
	public static final String PROPERTY_ENABLED="enabled";
	/** 'id' string property (unique ID for component; see getID() */
	public static final String PROPERTY_ID="id";	
	/** 'forcewidth' int property (if set, fixes component width) */
	public static final String PROPERTY_FORCEWIDTH="forcewidth";
	/** 'forceheight' int property (if set, fixes component height) */
	public static final String PROPERTY_FORCEHEIGHT="forceheight";
	
	// Colour constant information
	
	/** Colour constants (name, hex equiv, whether it counts as fg or bg for access) */
	private static final String[] COLOURCONSTANTS=
	{
		// FG
		"text","#000","fg",
		"bg","#fff","bg",
		
		// Three main background panes
		"input","#e4f1fa","bg",
		"answer","#fff3bf","bg",
		"other","#f3f0e0","bg",
		
		// Background for checkboxes/radioboxes/other things inside (white) 
		"innerbg","#ffffff","bg",
		
		// Highlight version of background
		"innerbghi","#bfcbd8","bg",
		
		// Background for dragboxes and anything that needs a cycle of up to 4 groups
		"innerbg0","#ffffff","bg",
		"innerbg1","#d6dbd7","bg",
		"innerbg2","#bfcbd8 ","bg",
		"innerbg3","#ddbfcc","bg",
		
		"disabledtext","#666666","disabledfg",
		"disabledbg",  "#eeeeee","disabledbg"
	};
	/** Regular expression allowing all colour constants */ 
	protected static final String COLOURCONSTANTS_REGEXP;
	static
	{
		String s="";
		for(int i=0;i<COLOURCONSTANTS.length;i+=3)
		{
			if(i!=0) s+="|";
			s+="("+COLOURCONSTANTS[i]+")";
		}
		COLOURCONSTANTS_REGEXP=s;
	}
	
	// Other data
	
	/** Children in tree. List of either QComponent or String */
	private LinkedList<Object> llChildren=new LinkedList<Object>();
	
	/** Properties (map of String -> Object) */
	private Map<String,Object> mProperties=new TreeMap<String,Object>();
	
	/** Permitted/defined properties (map of String -> PropertyDefinition) */
	private Map<String,PropertyDefinition> mDefinedProperties=new TreeMap<String,PropertyDefinition>();
	
	/** Owner document */
	private QDocument qdOwner; 
	
	/** Parent component */
	private QComponent qcParent;
	
	/** True if ID was autogenerated */
	private boolean bGeneratedID;
	
	/** Regular expression restricting ID values */
	public final static String PROPERTYRESTRICTION_ID="[A-Za-z][A-Za-z0-9_]*";
	
	/** Prefix used on generated IDs */
	public static final String GENERATEDID_PREFIX="gen_";

	
	// Required interface
	/////////////////////
	
	/** 
	 * Initialises from a particular XML element. 
	 * <p>
	 * Default implementation sets properties (unless the component is implicit 
	 * in which case it doesn't have properties) and sets up the owner document.
	 * It then calls initChildren() and finally initSpecific().
	 * <p>
	 * If you override this, you <em>must</em> call superclass version first 
	 * otherwise document won't get set and things will break. 
	 * @param parent The parent component.
	 * @param qd Document within which this QComponent resides
	 * @param eThis Element that provides the contents of this component
	 * @param bImplicit True if this component doesn't actually have its own 
	 *   parent element and eThis belongs to some other component
	 * @throws OmException If whatever error occurs
	 */
	public void init(QComponent parent,QDocument qd,Element eThis,boolean bImplicit) throws OmException
	{
		qdOwner=qd;
		this.qcParent=parent;
		
		// Set up properties
		defineProperties();
		if(!bImplicit) setPropertiesFrom(eThis); 

		// Build any children (not text)
		initChildren(eThis);		
		
		// Does initialisation specific to this component (if any)
		initSpecific(eThis);
		
		// Initialise ID if required
		if(!isPropertySet(PROPERTY_ID))
		{
			bGeneratedID=true;
			setString(PROPERTY_ID,GENERATEDID_PREFIX+qd.getSequence(eThis));
			if(DEBUG_LOGIDS)
				getQuestion().log("ID "+getString(PROPERTY_ID)+" set for "+getClass().getName());
		}
	}
	
	/**
	 * Initialises children based on the given XML element. 
	 * <p>
	 * This default implementation treats all child elements
	 * as components. If you want to include text as well or treat elements 
	 * differently (if they have a specific meaning for your components) you need
	 * to override this method. QDocument.buildInside() and similar will be 
	 * useful; or you may wish to call initAsText.
	 * @param eThis XML element representing this component
	 * @throws OmException If there's an error initialising the children
	 */
	protected void initChildren(Element eThis) throws OmException 
	{
		// Build any children (not text)
		getQDocument().buildInside(this,eThis);		
	}
	
	/**
	 * Carries out initialisation specific to component. Override this method
	 * if you need any. Default does nothing.
	 * @param eThis XML element representing this component
	 * @throws OmException If there's an error
	 */
	protected void initSpecific(Element eThis) throws OmException
	{
	}
	
	/**
	 * Designed for calling from an override of initChildren. Treats the
	 * contents of this element as if they are within a text component.
	 * @param eThis Element
	 * @throws OmException If there's an error initialising any children
	 */
	protected void initAsText(Element eThis) throws OmException
	{
		// Single synthetic test child
		addChild(getQDocument().build(this,eThis,"t"));
	}
	
	/**
	 * Optional override to estimate pixel size of the component. This is useful
	 * for components which expect to be placed within equations or graphs.
	 * Components that implement this method (to do anything other than return
	 * null) MUST also implement forcewidth and forceheight 
	 * <p>
	 * Default returns null.
	 * @return Pixel size if known, or null
	 * @throws OmDeveloperException
	 */
	public Dimension getApproximatePixelSize() throws OmDeveloperException
	{
		return null;
	}
	
	// Information access
	/////////////////////
	
	/** @return QDocument that this component is within */
	protected QDocument getQDocument()
	{
		return qdOwner;
	}
	
	/** @return StandardQuestion that owns this component */
	protected StandardQuestion getQuestion()
	{
		return qdOwner.getQuestion();
	}
	
	/** @return Parent component or null if this is root */
	protected QComponent getParent()
	{
		return qcParent;
	}
	
	/** @return Array of ancestors. Array element 0 is parent, 1 is parent's parent, etc */
	protected QComponent[] getAncestors()
	{
		List<QComponent> l=new LinkedList<QComponent>();
		QComponent qc=qcParent;
		while(qc!=null)
		{
			l.add(qc);
			qc=qc.qcParent;
		}
		return l.toArray(new QComponent[l.size()]);		
	}
	
	/** 
	 * @param cType Desired class of ancestor 
	 * @return The nearest ancestor of that class, or null if none
	 */ 
	protected QComponent findAncestor(Class<? extends QComponent> cType)
	{
		if(qcParent==null) return null;
		if(cType.isAssignableFrom(qcParent.getClass())) return qcParent;
		return qcParent.findAncestor(cType);
	}
	
	/**
	 * Returns the background colour of the nearest ancestor that has a background
	 * colour. Components which set a background should override this. 
	 * @param qcChild Child that's asking or null if we're looking for background
	 * of this component itself.
	 * @return The colour. 
	 */
	protected Color getChildBackground(QComponent qcChild)
	{
		// If no background's set (to set, this must be overridden), 
		// use the parent's
		if(qcParent==null) return null;
		return qcParent.getChildBackground(this);
	}
	
	/**
	 * Returns background colour of this component. Actually just calls 
	 * getChildBackground(null). If changing background colour, override 
	 * getChildBackground and not this.
	 * @return The colour.
	 */
	protected final Color getBackground()
	{
		return getChildBackground(null);
	}
	
	/**
	 * Some layout components want children to fill their allotted spaces by 
	 * setting height and width to 100%.
	 * @return Default is false
	 */
	protected boolean wantsFilledChildren()
	{
		return false;
	}
	
	/**
	 * If this returns true, child should set height and width to 100% or 
	 * otherwise try to fill its parent (where possible)
	 * @return True if parent wantsFilledChildren.
	 */
	protected boolean shouldFillParent()
	{
		return getParent()!=null && getParent().wantsFilledChildren();
	}

	/**
	 * Build up current content of this component that should end up in the 
	 * question output.
	 * <p>
	 * Default implementation just calls produceVisibleOutput() if 'display' is true.
	 * @param qc Output variable that accepts question content
	 * @param bInit True if this is first call in question initialisation
	 * @param bPlain True if in plain mode
	 * @throws OmException If whatever error occurs
	 */
	public void produceOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		if(isDisplayed())
		{
			produceVisibleOutput(qc,bInit,bPlain);
		}
	}
	
	/**
	 * Builds up content of this component that should end up in question output.
	 * Only called if the 'display' property is set to true.
	 * <p>
	 * Default implementation just calls produceChildOutput().
	 * @param qc Output variable that accepts question content
	 * @param bInit True if this is first call in question initialisation
	 * @throws OmException If whatever error occurs
	 */
	protected void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		produceChildOutput(qc,bInit,bPlain);
	}
	
	/**
	 * Called on question init to produce the CSS needed in a question that 
	 * includes this component. Happens after init() but before produceOutput()
	 * <p>
	 * Default implementation looks for a CSS file with same name as class and
	 * returns that; or returns null if not first instance.
	 * <p>
	 * This method calls convertCSS to process the CSS file so that it may contain
	 * special commands.
	 * <p>
	 * Not called when question is in plain mode. Plain mode questions may not
	 * include CSS. 
	 * @param bFirstInstance True if this is the first instance of this class of
	 *   component
	 * @return CSS to include for this component or null if none
	 * @throws OmException Any error
	 */
	public String getCSS(boolean bFirstInstance) throws OmException
	{
		if(!bFirstInstance) return null;
		
		try
		{
			// Have a go at loading it from a .css file
			String sClassBaseName=getClass().getName().replaceAll("^.*\\.","");
			InputStream is=getClass().getResourceAsStream(
				sClassBaseName+".css");
			if(is==null) 
				return null;
			else
				return "/* ---"+sClassBaseName+"--- */\n"+convertCSS(IO.loadString(is))+"\n";
		}
		catch(IOException ioe)
		{
			// No CSS file, no CSS
			return null;
		}
	}
	
	private final static Pattern CONVERTCSSTOKENS=Pattern.compile(
		"(.*?)%%(.*?)%%",Pattern.DOTALL); // DOTALL allows . to match CR/LF
	private final static Pattern NUMBERUNITS=Pattern.compile(
		"([0-9]+)([a-z]*)");
	
	/**
	 * Converts a CSS file ready for output, by filling in templated colours and
	 * sizes from the accessibility controls. 
	 * <table border="1">
	 * <tr><th>Command format</th><th>Result</th></tr>
	 * <tr><td>%%COLOUR:text%%</td><td>Actual colour e.g. #ff3321  
	 *   (Constants are those supported by convertHash, convertRGB)</td></tr>
	 * <tr><td>%%SIZE:16px%%</td><td>Converted size e.g. 24px</td></tr>
	 * </table>
	 * @param sInput CSS file as loaded
	 * @return Resulting file with all special control commands processed
	 * @throws OmDeveloperException If there are invalid commands in the CSS
	 */
	protected String convertCSS(String sInput) throws OmDeveloperException
	{
		// Get value and look for tokens. If there aren't any, bail now.
		if(sInput.indexOf("%%")==-1) return sInput;

		StringBuffer sbResult=new StringBuffer();
		Matcher m=CONVERTCSSTOKENS.matcher(sInput);
		while(m.lookingAt())
		{
			sbResult.append(m.group(1));
			String sKey=m.group(2);
			if(sKey.equals(""))
				sbResult.append("%%");
			else
			{
				if(sKey.startsWith("COLOUR:"))
				{
					sbResult.append(convertHash(
						sKey.substring("COLOUR:".length())));
				}
				else if(sKey.startsWith("SIZE:"))
				{
					// Split into number and units
					Matcher mSize=NUMBERUNITS.matcher(sKey.substring("SIZE:".length()));
					if(!mSize.matches())
						throw new OmDeveloperException("Unexpected content of %%SIZE:%% - "+sKey);
					double dNumber;
					try
					{
						dNumber=Double.parseDouble(mSize.group(1));
					}
					catch(NumberFormatException nfe)
					{
						throw new OmDeveloperException("Not a valid number in %%SIZE:%% - "+mSize.group(1));
					}
					
					sbResult.append(roundDouble(dNumber * getQuestion().getZoom())+mSize.group(2));
				}
				else throw new OmDeveloperException("Unrecognised command in CSS: "+sKey);
			}

			sInput=sInput.substring(m.end());
			m=CONVERTCSSTOKENS.matcher(sInput);
		}
		sbResult.append(sInput);

		return sbResult.toString();
	}
	
	/** 
	 * Rounds the given float to one decimal place. If it's .0, doesn't include
	 * the fraction part at all.
	 * @param d Input number
	 * @return Rounded string
	 */
	private static String roundDouble(double d)
	{
		NumberFormat nf=NumberFormat.getNumberInstance(Locale.UK);
		nf.setMaximumFractionDigits(1);
		String sNumber=nf.format(d);
		if(sNumber.endsWith(".0")) 
			return sNumber.substring(0,sNumber.length()-2);
		else
			return sNumber;
	}

	/**
	 * Called on question init to produce the JS needed in a question that 
	 * includes this component. Happens after init() but before produceOutput()
	 * <p>
	 * Default implementation looks for a JS file with same name as class and
	 * returns that; or returns null if not first instance.
	 * <p>
	 * Not called when question is in plain mode. Plain mode questions may not
	 * include JS.
	 * @param bFirstInstance True if this is the first instance of this class of
	 *   component
	 * @return JS to include for this component or null if none
	 * @throws OmException Any error
	 */
	public String getJS(boolean bFirstInstance) throws OmException
	{
		if(!bFirstInstance) return null;
		
		try
		{
			// Have a go at loading it from a .js file
			String sClassBaseName=getClass().getName().replaceAll("^.*\\.","");
			InputStream is=getClass().getResourceAsStream(
				sClassBaseName+".js");
			if(is==null) 
				return null;
			else
				return "// ---"+sClassBaseName+"---\n"+IO.loadString(is)+"\n";
		}
		catch(IOException ioe)
		{
			// No JS file, no JS
			return null;
		}
	}
	
	/**
	 * On receiving this call, the component should update its internal data
	 * structures based on the given value from XHTML.
	 * <p>
	 * Called automatically by framework after receiving a form, when an item
	 * in the action parameters begins with QDocument.VALUE_PREFIX.
	 * <p>
	 * Default implementation throws exception; suitable for components that have
	 * no value such as buttons. Consequently, do not call base class when 
	 * overriding.
	 * @param sValue Value string for this item 
	 * @param ap All parameters (needed if this item uses more than one)
	 * @throws OmException If overriders need to.
	 */
	protected void formSetValue(String sValue,ActionParams ap) throws OmException
	{		
		throw new OmUnexpectedException(
			"Shouldn't get value set for component without one!");
	}
	
	/**
	 * On receiving this call, the component knows that if it hasn't received
	 * a formSetValue then its value was not submitted in the current form 
	 * submission.
	 * <p>
	 * All components receive this call after each form submit. The default 
	 * implementation does nothing.
	 * @param ap All form parameters 
	 * @throws OmException If overriders need to
	 */
	protected void formAllValuesSet(ActionParams ap) throws OmException
	{
	}
	
	/**
	 * On receiving this call, the component should pass on its action to an
	 * appropriate action handler in the question.
	 * <p>
	 * Called automatically by framework after receiving a form, when an item
	 * in the action parameters begins with QDocument.ACTION_PREFIX. The call 
	 * happens after all calls to formSetValue, so you can rely on the value of 
	 * this and all other components without needing to example the ActionParams. 
	 * <p>
	 * Default implementation throws exception; suitable for components that have
	 * no action such as input boxes. Consequently, do not call base class when 
	 * overriding.
	 * @param sValue Value string from the form item
	 * @param ap All parameters
	 * @throws OmException
	 */
	protected void formCallAction(String sValue,ActionParams ap) 
	  throws OmException
	{
			throw new OmUnexpectedException(
				"Shouldn't get action call for component without one!");
	}
	
	// Property handling
	////////////////////
	
	/** Definition of a property (surprise!) */
	private static class PropertyDefinition
	{
		Class cType;
		boolean bAllowFromXML;
		String sRestriction;
	}

	/**
	 * Define a property which this component supports. Once defined, the property
	 * can be set using setProperty, retrieved by getProperty methods, etc.
	 * @param sName Property name (should generally be all lower-case)
	 * @param cType Java class of property (String.class, Integer.class,
	 *   Boolean.class, or Double.class)
	 * @param bAllowFromXML True if it should be interpreted from XML attribute
	 *   of same name
	 * @param sRestriction Additional restriction regex (used only when reading
	 *   from xml); may be null
	 * @throws OmDeveloperException If you've tried to allow a property from XML
	 *   that isn't one of the standard classes.
	 */	
	protected void defineProperty(String sName,Class cType,boolean bAllowFromXML,
		String sRestriction)
	  throws OmDeveloperException
	{
		// Most properties we can't parse from XML
		if(bAllowFromXML &&
			(!(cType==String.class || cType==Boolean.class || cType==Integer.class
				|| cType==Double.class)))
		{
			throw new OmDeveloperException("Property '"+sName+"' can't be allowed " +
				"from XML because it isn't one of the standard supported classes.");
		}
		
		PropertyDefinition pd=new PropertyDefinition();
		pd.cType=cType;
		pd.bAllowFromXML=bAllowFromXML;
		pd.sRestriction=sRestriction;
		
		mDefinedProperties.put(sName,pd);
	}
	
	/**
	 * Defines an unrestricted string property that can be supplied as an attribute. 
	 * @param sName Name of property
	 */
	protected void defineString(String sName) throws OmDeveloperException
	{
		defineProperty(sName,String.class,true,null);
	}
	
	/**
	 * Defines an restricted string property that can be supplied as an attribute. 
	 * @param sName Name of property
	 * @param sRestriction Restriction regexp
	 */
	protected void defineString(String sName,String sRestriction) throws OmDeveloperException
	{
		defineProperty(sName,String.class,true,sRestriction);
	}
	
	/**
	 * Defines an integer property that can be supplied as an attribute.
	 * @param sName Name of property
	 */
	protected void defineInteger(String sName) throws OmDeveloperException
	{
		defineProperty(sName,Integer.class,true,null);
	}
	
	/**
	 * Defines a boolean property that can be supplied as an attribute.
	 * @param sName Name of property
	 */
	protected void defineBoolean(String sName) throws OmDeveloperException
	{
		defineProperty(sName,Boolean.class,true,null);
	}
	
	/**
	 * Defines a double property that can be supplied as an attribute.
	 * @param sName Name of property
	 */
	protected void defineDouble(String sName) throws OmDeveloperException
	{
		defineProperty(sName,Double.class,true,null);
	}
	
	/**
	 * Intended for overriding. Default returns empty array.
	 * @return List of required attribute names; applied by setPropertiesFrom()
	 */
	protected String[] getRequiredAttributes()
	{
		return new String[] {};
	}
	
	/**
	 * Intended for overriding. Default allows 'id' and 'display', so do remember 
	 * to call superclass if you override or you won't get these.
	 * <p>
	 * Basically call defineProperty a bunch of times to set up all your 
	 * properties. If you wish to set initial property values you can do that 
	 * here too.
	 */
	protected void defineProperties() throws OmDeveloperException
	{
		defineProperty(PROPERTY_ID,String.class,true,PROPERTYRESTRICTION_ID);
		defineBoolean(PROPERTY_DISPLAY);
		defineBoolean(PROPERTY_ENABLED);
		setBoolean(PROPERTY_DISPLAY,true);
		setBoolean(PROPERTY_ENABLED,true);
	}
	
	/**
	 * Fills the property map from element attributes.
	 * @param eThis Element to eat attributes from
	 */
	protected void setPropertiesFrom(Element eThis) throws OmException
	{
		// Check any missing attributes
		String[] asRequired=getRequiredAttributes();
		for(int iRequired=0;iRequired<asRequired.length;iRequired++)
		{
			if(!eThis.hasAttribute(asRequired[iRequired]))
				throw new OmFormatException(
					"<"+eThis.getTagName()+">: required attribute '"+asRequired[iRequired]+"' omitted");			
		}
		
		// Put in other properties
		NamedNodeMap nnm=eThis.getAttributes();
		for(int i=0;i<nnm.getLength();i++)
		{
			Attr a=(Attr)nnm.item(i);
			String sName=a.getName();
			PropertyDefinition pd=mDefinedProperties.get(sName);
			if(pd==null)
				throw new OmFormatException(
					"<"+eThis.getTagName()+">: property '"+a.getName()+"' not defined");
			if(!pd.bAllowFromXML)
				throw new OmFormatException(
					"<"+eThis.getTagName()+">: property '"+a.getName()+"' may not be set from XML");			
			
			String sValue=a.getValue();
			if(pd.cType==String.class)
			{
				if(pd.sRestriction!=null && !sValue.matches(pd.sRestriction))
					throw new OmFormatException(
						"<"+eThis.getTagName()+">: property '"+a.getName()+"' has an invalid value");			
				mProperties.put(sName,sValue);	
			}
			else if(pd.cType==Integer.class)
			{
				try
				{
					mProperties.put(sName,new Integer(sValue));
				}
				catch(NumberFormatException nfe)
				{
					throw new OmFormatException(
						"<"+eThis.getTagName()+">: property '"+a.getName()+"' is not a valid integer");			
				}
			}
			else if(pd.cType==Double.class)
			{
				try
				{
					mProperties.put(sName,new Double(sValue));
				}
				catch(NumberFormatException nfe)
				{
					throw new OmFormatException(
						"<"+eThis.getTagName()+">: property '"+a.getName()+"' is not a valid double");			
				}
			}
			else if(pd.cType==Boolean.class)
			{
				if(sValue.equals("yes"))
					mProperties.put(sName,Boolean.TRUE);
				else if(sValue.equals("no"))
					mProperties.put(sName,Boolean.FALSE);
				else throw new OmFormatException(
					"<"+eThis.getTagName()+">: property '"+a.getName()+"' must be either 'yes' or 'no'");			
			}
			else
				throw new OmUnexpectedException("Unexpected property class: "+pd.cType);
		}		
	}
	

	/**
	 * Sets a given string property.
	 * @param sName Property name
	 * @param sValue New value
	 * @return Previous value (null if none)
	 * @throws OmDeveloperException If the property wasn't defined etc.
	 */
	public String setString(String sName,String sValue) throws OmDeveloperException
	{
		checkProperty(sName,String.class);
		String sReturn=(String)mProperties.put(sName,sValue);
		// Are they messing with the ID?
		if(sName.equals(PROPERTY_ID) && sReturn!=null)
		{
			// If they are, fix it back and tell them to p*** off
			mProperties.put(sName,sReturn);
			throw new OmDeveloperException("Cannot change id once it has been set");
		}
		return sReturn;
	}
	
	/**
	 * Sets a given int property.
	 * @param sName Property name
	 * @param iValue New value
	 * @return Previous value (-1 if none)
	 * @throws OmDeveloperException If the property wasn't defined etc.
	 */
	public int setInteger(String sName,int iValue) throws OmDeveloperException
	{
		checkProperty(sName,Integer.class);
		Integer i=(Integer)mProperties.put(sName,new Integer(iValue));
		if(i==null) 
			return -1;
		else
			return i.intValue();
	}
	
	/**
	 * Sets a given boolean property.
	 * @param sName Property name
	 * @param bValue New value
	 * @return Previous value (false if none)
	 * @throws OmDeveloperException If the property wasn't defined etc.
	 */
	public boolean setBoolean(String sName,boolean bValue) throws OmDeveloperException
	{
		checkProperty(sName,Boolean.class);
		Boolean b=(Boolean)mProperties.put(sName,new Boolean(bValue));
		if(b==null) 
			return false;
		else
			return b.booleanValue();
	}
	
	/**
	 * Sets a given double property.
	 * @param sName Property name
	 * @param dValue New value
	 * @return Previous value (0.0 if none)
	 * @throws OmDeveloperException If the property wasn't defined etc.
	 */
	public double setDouble(String sName,double dValue) throws OmDeveloperException
	{
		checkProperty(sName,Double.class);
		Double d=(Double)mProperties.put(sName,new Double(dValue));
		if(d==null) 
			return 0.0;
		else
			return d.doubleValue();
	}
	
	/**
	 * Returns value of given property.
	 * @param sName Property name
	 * @return Current value
	 * @throws OmDeveloperException If property was never set etc.
	 */
	public String getString(String sName) throws OmDeveloperException
	{
		checkSetProperty(sName,String.class);
		return (String)mProperties.get(sName);
	}

	/**
	 * Returns value of given property.
	 * @param sName Property name
	 * @return Current value
	 * @throws OmDeveloperException If property was never set etc.
	 */
	public int getInteger(String sName) throws OmDeveloperException
	{
		checkSetProperty(sName,Integer.class);
		return ((Integer)mProperties.get(sName)).intValue();
	}

	/**
	 * Returns value of given property.
	 * @param sName Property name
	 * @return Current value
	 * @throws OmDeveloperException If property was never set etc.
	 */
	public boolean getBoolean(String sName) throws OmDeveloperException
	{
		checkSetProperty(sName,Boolean.class);
		return ((Boolean)mProperties.get(sName)).booleanValue();
	}

	/**
	 * Returns value of given property.
	 * @param sName Property name
	 * @return Current value
	 * @throws OmDeveloperException If property was never set etc.
	 */
	public double getDouble(String sName) throws OmDeveloperException
	{
		checkSetProperty(sName,Double.class);
		return ((Double)mProperties.get(sName)).doubleValue();
	}
	
	/**
	 * @param sName Property name
	 * @return True if it's been set
	 */
	public boolean isPropertySet(String sName)
	{
		return mProperties.containsKey(sName);
	}
	
	/**
	 * @param sName Property name
	 * @return True if it's been defined
	 */
	public boolean isPropertyDefined(String sName)
	{
		return mDefinedProperties.containsKey(sName);
	}
	
	/**
	 * Checks a given property exists and is of the right type.
	 * @param sName Name of property
	 * @param c Desired class
	 * @throws OmDeveloperException If it doesn't exist or doesn't match class
	 */
	private void checkProperty(String sName,Class c) throws OmDeveloperException
	{
		PropertyDefinition pd=mDefinedProperties.get(sName);
		if(pd==null) throw new OmDeveloperException(
			"Attempt to access undefined property: "+sName);
		if(pd.cType!=c) throw new OmDeveloperException(
			"Attempt to access property as "+c.getName()+" when it was defined as "+
			pd.cType.getName());		
	}
	
	/**
	 * Checks a property exists and has been set.
	 * @param sName Name of property
	 * @param c Desired class
	 * @throws OmDeveloperException If it's not defined, wrong class, or wasn't set
	 */
	private void checkSetProperty(String sName,Class c) throws OmDeveloperException
	{
		checkProperty(sName,c);
		if(!mProperties.containsKey(sName))
			throw new OmDeveloperException("Property not set: "+sName);
	}
	

	// Utility/helpers
	//////////////////
	
	/**
	 * Calls produceOutput for each child, adding their content to
	 * the given question content object. Text children get added directly.
	 * @param qc Output variable that accepts question content
	 * @param bInit True if this is first call in question initialisation
	 * @throws OmException If whatever error occurs
	 */
	protected void produceChildOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		for(Iterator i=llChildren.iterator();i.hasNext();)
		{
			Object o=i.next();
			if(o instanceof String)
			{
				qc.addInlineXHTML(qc.getOutputDocument().createTextNode((String)o));
				qc.addTextEquivalent((String)o);
			}
			else
				((QComponent)o).produceOutput(qc,bInit,bPlain);
		}
	}
	
	// Specially-handled property access
	////////////////////////////////////
	
	/**
	 * Obtains a unique ID for component. If one wasn't specified, an automatically
	 *   generated one based on the position within the xml document will be used.
	 * @return Unique ID for component 
	 * @throws OmDeveloperException
	 */
	public String getID() throws OmDeveloperException
	{
		return getString(PROPERTY_ID);
	}
	
	/**
	 * @return True if the user set an ID on this component
	 * @throws OmDeveloperException 
	 */
	public boolean hasUserSetID() throws OmDeveloperException
	{
		return !bGeneratedID && isPropertySet(PROPERTY_ID);
	}
	
	/**
	 * @param bEnabled True to enable all editing components within this, false to disable
	 */
	public void setEnabled(boolean bEnabled)
	{
		try
		{
			setBoolean(PROPERTY_ENABLED,bEnabled);
		}
		catch(OmDeveloperException e)
		{
			throw new OmUnexpectedException(e);
		}
	}
	
	/**
	 * @param bDisplay True to display this component
	 */
	public void setDisplay(boolean bDisplay)
	{
		try
		{
			setBoolean(PROPERTY_DISPLAY,bDisplay);
		}
		catch(OmDeveloperException e)
		{
			throw new OmUnexpectedException(e);
		}
	}
	
	/**
	 * Calls setDisplay on this component and all its children, and
	 * their children, and so on recursively.
	 * 
	 * @param bDisplay passed to setDisplay on each component.
	 */
	public void setDisplayRecursive(boolean bDisplay)
	{
		try
		{
			setBoolean(PROPERTY_DISPLAY,bDisplay);
			Object[] children = getChildren();
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof QComponent) {
					((QComponent) children[i]).setDisplayRecursive(bDisplay);
				}
			}
		}
		catch(OmDeveloperException e)
		{
			throw new OmUnexpectedException(e);
		}
	}
	
	/**
	 * Calls setDisplay on this component and all its children of a particular type, and
	 * their children, and so on recursively.
	 * 
	 * @param bDisplay passed to setDisplay on each component.
	 * @param componentType the type of componet to hide. For example <code>TextComponent.class</code>.
	 */
	public void setDisplayRecursive(boolean bDisplay, Class<? extends QComponent> componentType)
	{
		try
		{
			if (componentType.isAssignableFrom(getClass())) {
				setBoolean(PROPERTY_DISPLAY,bDisplay);
			}
			Object[] children = getChildren();
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof QComponent) {
					((QComponent) children[i]).setDisplayRecursive(bDisplay, componentType);
				}
			}
		}
		catch(OmDeveloperException e)
		{
			throw new OmUnexpectedException(e);
		}
	}
	
	/**
	 * @return True if display is on
	 */
	public boolean isDisplayed()
	{
		try
		{
			return getBoolean(PROPERTY_DISPLAY);
		}
		catch(OmDeveloperException e)
		{
			throw new OmUnexpectedException(e);
		}
	}
	
	/**
	 * @return True if display is on for one or more children.
	 */
	public boolean isChildDisplayed()
	{
		QComponent[] acKids=getComponentChildren();
		for(int i=0;i<acKids.length;i++)
		{
			if(acKids[i].isDisplayed()) return true;
		}
		return false;
	}
	
	/**
	 * @return True if currently enabled (checks all parents up the tree)
	 */
	public boolean isEnabled()
	{
		try
		{
			if(!getBoolean(PROPERTY_ENABLED)) return false;
			QComponent parent=getParent();
			if(parent==null)
				return true;
			else
				return parent.isEnabled();
		}
		catch(OmDeveloperException e)
		{
			throw new OmUnexpectedException(e);
		}
	}
	
	
	// Tree access
	//////////////
	
	/** @return Array of all child items (either QComponent or String) */
	public Object[] getChildren()
	{
		return llChildren.toArray();
	}
	
	/**
	 * Finds child component with given ID. Not designed for question developer
	 * use (hence package-only access), use QDocument.find().
	 * @param sID Desired ID
	 * @return Sub-component or self with given ID, or null if none
	 */
	final QComponent findSubComponent(String sID)
	{
		if(sID.equals(mProperties.get(PROPERTY_ID))) return this;
		for(Iterator i=llChildren.iterator();i.hasNext();)
		{
			Object o=i.next();
			if(o instanceof QComponent)
			{
				QComponent qc=((QComponent)o).findSubComponent(sID);
				if(qc!=null) return qc;
			}
		}
		return null;
	}	

	/**
	 * Lists subcomponents of given class. Not designed for question developer use
	 * (hence package-only access), use QDocument.find().
	 * @param cClass Desired Java class
	 * @param cList Array of matching components
	 */
	final void listSubComponents(Class<?> cClass,Collection<QComponent> cList)
	{
		if(getClass()==cClass) cList.add(this);
		for(Object o : llChildren)
		{
			if(o instanceof QComponent)
			{
				((QComponent)o).listSubComponents(cClass,cList);
			}
		}
	}
	
	/**
	 * Obtains list of all children which are components. 
	 * @return Array of all children that are components
	 */ 
	public QComponent[] getComponentChildren() 
	{
		List<QComponent> lResult=new LinkedList<QComponent>();
		for(Object o : llChildren)
		{
			if(o instanceof QComponent) lResult.add((QComponent)o);
		}
		return lResult.toArray(new QComponent[lResult.size()]);
	}
	
	/**
	 * Adds a string as last child.
	 * @param s Text to add
	 */
	public void addChild(String s)
	{
		llChildren.addLast(s);
	}
	/**
	 * Adds a component as last child.
	 * @param qc Component to add
	 */
	public void addChild(QComponent qc)
	{
		llChildren.addLast(qc);
	}
	
	/**
	 * Remove child from this component.
	 * @param qc Child to remove
	 */
	public void removeChild(QComponent qc)
	{
		if(llChildren.remove(qc))
		{
			getQDocument().informRemoved(qc);
		}
	}
	
	/**
	 * Clear all children from this node.
	 */
	public void removeChildren()
	{
		// List component children and get rid of them all
		QComponent[] acRemoved=getComponentChildren();
		for(int i=0;i<acRemoved.length;i++)
		{
			removeChild(acRemoved[i]);
		}
		// Get rid of text too
		llChildren.clear();
	}

	/**
	 * Converts an RGB value from a hex string (#rrggbb or #rgb), or a colour
	 * constant, into a Java colour.
	 * @param sRGB String
	 * @return Colour
	 * @throws OmDeveloperException If there's any problem converting
	 */
	public Color convertRGB(String sRGB) throws OmDeveloperException
	{
		// Convert colour constants to hash
		sRGB=convertHash(sRGB);
		Color c=convertRGBOnly(sRGB);
		return c;
	}
	
	/**
	 * Converts an RGB value from a hex string only (#rrggbb or #rgb), and not a colour
	 * constant, into a Java colour.
	 * @param sRGB String
	 * @return Colour
	 * @throws OmDeveloperException If there's any problem converting
	 */
	public static Color convertRGBOnly(String sRGB)throws OmDeveloperException
	{
		if(!sRGB.startsWith("#")) throw new OmDeveloperException("RGB colours must begin with #");
		
		// OK it's RGB. Get rid of # and process
		sRGB=sRGB.substring(1);
		try
		{
			// Convert to integers
			int iR,iG,iB;
			if(sRGB.length()==3)
			{
				iR=Integer.parseInt(sRGB.substring(0,1),16);
				iR=16*iR+iR;
				iG=Integer.parseInt(sRGB.substring(1,2),16);
				iG=16*iG+iG;
				iB=Integer.parseInt(sRGB.substring(2,3),16);
				iB=16*iB+iB;
			}
			else
			{
				iR=Integer.parseInt(sRGB.substring(0,2),16);
				iG=Integer.parseInt(sRGB.substring(2,4),16);
				iB=Integer.parseInt(sRGB.substring(4,6),16);
			}
	
			return new Color(iR,iG,iB);
		}
		catch(NumberFormatException nfe)
		{
			throw new OmDeveloperException("Invalid RGB string: "+sRGB,nfe);
		}
	}


	
	/**
	 * Given a colour, returns the 3/6-digit HTML # version.
	 * @param sColour Colour (either #rgb, #rrggbb, or colour constant)
	 * @return #rgb or #rrggbb
	 * @throws OmDeveloperException 
	 */
	public String convertHash(String sColour) throws OmDeveloperException
	{
		// Handle colours that are already hash
		if(sColour.startsWith("#")) return sColour;
		
		// Process colour constants
		for(int i=0;i<COLOURCONSTANTS.length;i+=3)
		{
			if(sColour.equals(COLOURCONSTANTS[i]))
			{
				if(getQuestion().isFixedColour())
				{
					if("fg".equals(COLOURCONSTANTS[i+2]))
						return getQuestion().getFixedColourFG();
					else if("bg".equals(COLOURCONSTANTS[i+2]))
						return getQuestion().getFixedColourBG();
					else if("disabledfg".equals(COLOURCONSTANTS[i+2]))
					  return interColour(0.25);					
					else if("disabledbg".equals(COLOURCONSTANTS[i+2]))
					  return interColour(0.75);
					else
						throw new OmUnexpectedException("Unexpected fg/bg constant");						
				}
				else
					return COLOURCONSTANTS[i+1];
			}
		}
		throw new OmDeveloperException("Unknown colour constant: "+sColour);
	}
	
	/**
	 * @param i Integer 0-255
	 * @return Two-digit hex string e.g. 08, F3
	 */
	private static String convertHex(int i)
	{
		if(i<16) 
			return "0"+Integer.toHexString(i);
		else 
			return Integer.toHexString(i);
	}
	
	/**
	 * @param c Java colour
	 * @return #rrggbb equivalent
	 */
	public static String convertHash(Color c) 
	{
		return "#"+convertHex(c.getRed())+convertHex(c.getGreen())+convertHex(c.getBlue());
	}
	
	
	/**
	 * Returns a weighted intermediate between the question 
	 * foreground and background colours as # version.
	 * @param weight (=1 returns background, =0 returns foregound)
	 * @return #rgb or #rrggbb
	 * @throws OmDeveloperException 
	 */
	public String interColour(double weight) throws OmDeveloperException
	{
		String sbg=getQuestion().getFixedColourBG();
		String sfg=getQuestion().getFixedColourFG();
		if (sbg==null) {sbg="#FFFFFF"; if (weight > 0.6) weight = 0.95; }
		if (sfg==null)  sfg="#000000"; 

		Color cbg = convertRGBOnly(sbg);
		Color cfg = convertRGBOnly(sfg);
		double weight2 = 1.0-weight;
		int iR = (int)(cbg.getRed()*weight + cfg.getRed()*weight2);
		int iG =(int)(cbg.getGreen()*weight + cfg.getGreen()*weight2);
		int iB =(int)(cbg.getBlue()*weight + cfg.getBlue()*weight2);
		return convertHash(new Color(iR,iG,iB));
	}
	
	
	/**
	 * Loads a resource from the classpath.
	 * @param sFilename File to load
	 * @return Byte array of data from file
	 * @throws OmDeveloperException If the file doesn't exist etc.
	 */
	protected byte[] getClassResource(String sFilename) throws OmDeveloperException
	{
		try
		{
			return IO.loadBytes(getClass().getResourceAsStream(sFilename));
		}
		catch(IOException ioe)
		{
			throw new OmDeveloperException(
				"Failure to load "+getClass()+" resource: "+sFilename);
		}
	}
	
	/**
	 * To be called on user input to restrict its length.
	 * @param sInput Input string
	 * @param iMaxLength Maximum allowed number of characters
	 * @return Original or trimmed strings
	 */
	protected static String trim(String sInput,int iMaxLength)
	{
		if(sInput.length() < iMaxLength) return sInput;
		return sInput.substring(0,iMaxLength);		
	}

}
