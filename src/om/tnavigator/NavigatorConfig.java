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
package om.tnavigator;

import java.io.*;
import java.net.*;
import java.util.*;

import om.tnavigator.db.OmQueries;

import org.w3c.dom.*;

import util.xml.XML;

/** Loads navigator configuration file. */
public class NavigatorConfig
{
	/** Om services */
	private URL[] omServices;
	
	/** URL of this navigator */
	private URL thisTN;
	
	/** URLs of other test navigators */
	private String[] otherNavigators;
	
	/** Database server machine e.g. "codd" */
	private String dbServer;
	
	/** DB username */
	private String dbUsername;
	
	/** DB password */
	private String dbPassword;
	
	/** Database name e.g. "oms-dev" */
	private String dbName;
	
	/** Set of InetAddress; Trusted IP addresses for question engines */
	private Set trustedQEs=new HashSet();
	
	/** Set of InetAddress; Trusted IP addresses for other test navigators */
	private Set trustedTNs=new HashSet();
	
	/** Debug flags */
	private Set debugFlags=new HashSet();
	
	/** Sending server alerts */
	private String[] alertMailTo,alertMailCC;
	/** Source for server alerts */
	private String alertMailFrom;
	
	/** Class of authentication plugin */
	private String authClass;
	
	/** Class of database plugin */
	private String dbClass;
	
	/** Parameters for auth */
	private Map authParams=null;
	
	String getAuthClass()
	{
		return authClass;
	}
	
	String getDBClass()
	{
		return dbClass;
	}
	
	private static Map getParams(Element parent) throws IOException
	{
		Map m=new HashMap();
		Element[] params=XML.getChildren(parent,"param");
		for(int i=0;i<params.length;i++)
		{
			m.put(XML.getRequiredAttribute(params[i],"name"),
				XML.getText(params[i]));
		}
		return m;
	}
	
	public Object getAuthParam(String name,Class type,boolean required) throws IOException
	{
		String value=(String)authParams.get(name);
		if(value==null && required)
			throw new IOException("navigator.xml: <authentication> - missing <param name='"+name+"'>");
		if(type==String.class)
			return value;
		else if(type==URL.class)
		{
			try
			{
				return new URL(value);
			}
			catch(MalformedURLException e)
			{
				throw new IOException("navigator.xml: <authentication> - <param name='"+name+"'> is not a valid URL");
			}
		}
		else
			throw new IOException("getAuthParam does not support type: "+type.getName());
	}
	
	/** IP addresses that are trusted for server status */
	private String[] trustedAddresses;
	
	/**
	 * Initialises config.
	 * @param fConfig Config file
	 * @throws IOException If there's any problem parsing the file etc.
	 */
	NavigatorConfig(File fConfig) throws IOException
	{
		Document dConfig=XML.parse(fConfig);
		Element eRoot=dConfig.getDocumentElement();
		
		Element eDB=XML.getChild(eRoot,"database");
		dbClass=XML.getRequiredAttribute(eDB,"plugin");
		dbServer=XML.getText(eDB,"server");
		dbName=XML.getText(eDB,"name");
		dbUsername=XML.getText(eDB,"username");
		dbPassword=XML.getText(eDB,"password");
		
		Element eAuth=XML.getChild(eRoot,"authentication");
		authClass=XML.getRequiredAttribute(eAuth,"plugin");
		authParams=getParams(eAuth);
		
		trustedAddresses=XML.getTextFromChildren(XML.getChild(eRoot,"trustedaddresses"),"address");
		for(int i=0;i<trustedAddresses.length;i++)
		{
			if(!("."+trustedAddresses[i]).matches("(.(([0-9]+(-[0-9]+)?)|\\*)){4}"))
				throw new IOException(
					"navigator.xml: <trustedaddresses> <address> not in valid format: "+trustedAddresses[i]);
		}
		
		Mail.setSMTPHost(XML.getText(eRoot,"smtpserver"));
		
		Element eQE=XML.getChild(eRoot,"questionengines");
		String[] asURLs=XML.getTextFromChildren(eQE,"url");
		omServices=new URL[asURLs.length];
		for(int i=0;i<asURLs.length;i++)
		{
			URL u=new URL(asURLs[i]);
			omServices[i]=u;
			trustedQEs.add(InetAddress.getByName(u.getHost()));
		}
		if(omServices.length==0) throw new IOException(
			"navigator.xml: requires at leat one <url> inside <questionengines>");
		
		Element eTN=XML.getChild(eRoot,"testnavigators");
		Element[] aeTN=XML.getChildren(eTN,"url");
		if(aeTN.length==0) throw new IOException(
			"navigator.xml: requires at least one <url> inside <testnavigators>");
		List lNavigators=new LinkedList();		
		boolean bGotThis=false;
		for(int i=0;i<aeTN.length;i++)
		{
			String sURL=XML.getText(aeTN[i]);
			if("yes".equals(aeTN[i].getAttribute("this")))
			{
				bGotThis=true;
				thisTN=new URL(sURL);
			}
			else
			{
				lNavigators.add(sURL);
				trustedTNs.add(InetAddress.getByName((new URL(sURL)).getHost()));
			}
		}
		if(!bGotThis) throw new IOException(
			"navigator.xml: requires one <url this='yes'> inside <testnavigators>");
		otherNavigators=(String[])lNavigators.toArray(new String[lNavigators.size()]);
		
		if(XML.hasChild(eRoot,"debugflags"))
		{
			Element[] aeFlags=XML.getChildren(XML.getChild(eRoot,"debugflags"));
			for(int i=0;i<aeFlags.length;i++)
			{
				debugFlags.add(aeFlags[i].getTagName());
			}
		}
		
		if(XML.hasChild(eRoot,"alertmail"))
		{
			Element eAlertMail=XML.getChild(eRoot,"alertmail");
			alertMailFrom=XML.getText(eAlertMail,"from");
			alertMailTo=XML.getTextFromChildren(eAlertMail,"to");
			alertMailCC=XML.getTextFromChildren(eAlertMail,"cc");
		}
		else
		{
			alertMailTo=alertMailCC=new String[0];			
		}
	}
	
	/** @return Full JDBC URL of database including username and password */
	String getDatabaseURL(OmQueries oq) throws ClassNotFoundException
	{
		return oq.getURL(dbServer,dbName,dbUsername,dbPassword);
	}
	
	/** @return Array of trusted IP addresses in the form 0.1.2.3 or 0.1.2.* or
	 *    0.1.26-49.*
	 */
	public String[] getTrustedAddresses() 
	{
		return trustedAddresses;
	}
	
	/** @return URLs for all Om question engines */
	public URL[] getOmServices()
	{	
		return omServices;
	}	
	
	/** @return URLs (ending in /) of each other test navigator */
	public String[] getOtherNavigators()
	{
		return otherNavigators;
	}
	
	/**
	 * @param sTagName Name of tag/flag looked for
	 * @return True if it's present
	 */
	public boolean hasDebugFlag(String sTagName)
	{
		return debugFlags.contains(sTagName);
	}
	
	/** 
	 * @param ia Address under consideration
	 * @return True if the given address belongs to a known question engine 
	 */
	public boolean isTrustedQE(InetAddress ia)
	{
		return trustedQEs.contains(ia);
	}
	
	/** 
	 * @param ia Address under consideration
	 * @return True if the given address belongs to a known test navigator 
	 */
	public boolean isTrustedTN(InetAddress ia)
	{
		return trustedTNs.contains(ia);
	}
	
	/** @return Real (not load-balancing) URL of this test navigator */ 
	public URL getThisTN() { return thisTN; }

	/** @return List of mail addresses to send server alerts to */
	public String[] getAlertMailTo()
	{
		return alertMailTo;
	}		

	/** 
	 * @return Mail addresses that server alerts (and possibly student mails)
	 * come from
	 */
	public String getAlertMailFrom()
	{
		return alertMailFrom;
	}		

	/** @return List of mail addresses to CC with server alerts */
	public String[] getAlertMailCC()
	{
		return alertMailCC;
	}			
}
