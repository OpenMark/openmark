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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import om.tnavigator.db.OmQueries;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.misc.Strings;
import util.xml.XML;
import util.xml.XMLException;

/** Loads navigator configuration file. */
public class NavigatorConfig
{
	private static String REQUEST_MANAGEMENT_NODE = "RequestManagement";

	private static String REQUEST_BASED = "requestBased";

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

	/** Database prefix e.g. "nav_" */
	private String dbPrefix;

	/** Set of InetAddress; Trusted IP addresses for question engines */
	private Set<InetAddress> trustedQEs=new HashSet<InetAddress>();

	/** Set of InetAddress; Trusted IP addresses for other test navigators */
	private Set<InetAddress> trustedTNs=new HashSet<InetAddress>();

	/** Debug flags */
	private Set<String> debugFlags=new HashSet<String>();

	/** Sending server alerts */
	private String[] alertMailTo,alertMailCC;
	/** Source for server alerts */
	private String alertMailFrom;

	/** Class of authentication plugin */
	private String authClass;

	/** Class of database plugin */
	private String dbClass;

	/** Extra report plugins */
	private String[] extraReports;	

	/** Parameters for auth */
	private Map<String,String> authParams=null;
	
	/** Additional databases */
	private Map<String, ExtraDatabase> extraDatabases = new HashMap<String, ExtraDatabase>();
	
	/** Standard admin usernames that may be required to be present in test deploy files */
	private List<String> standardAdmins = new ArrayList<String>();
	
	/** list of optional fieatures switched on */
	private List<String> optionalFeatures = new ArrayList<String>();

	private static String PRE_PROCESSOR = "preprocessor";

	private Class<?> preProcessingRequestHandler;

	private boolean requestManagementRequestBased;

	public boolean isRequestManagementRequestBased() {
		return requestManagementRequestBased;
	}

	public Class<?> retrievePreProcessingRequestHandler() {
		return preProcessingRequestHandler;
	}

	public String getAuthClass() {
		return authClass;
	}

	public String getDBClass() {
		return dbClass;
	}

	public String getDBName() {
		return dbName;
	}
	public String getDBPrefix() {
		return dbPrefix;
	}	

	private static Map<String,String> getParams(Element parent) throws IOException
	{
		Map<String,String> m=new HashMap<String,String>();
		Element[] params=XML.getChildren(parent,"param");
		for(int i=0;i<params.length;i++)
		{
			m.put(XML.getRequiredAttribute(params[i],"name"),
				XML.getText(params[i]));
		}
		return m;
	}

	/**
	 * Get a parameter from the <authentication> section of navigator.xml as a String.
	 * @param name as in, <param name=''>.
	 * @param required if false, this method will return null if the parameter is missing. If true,
	 * 		it will throw an exception.
	 * @return The value of the named parameter, as a String.
	 * @throws IOException
	 */
	public String getAuthParamString(String name,boolean required) throws IOException
	{
		String value=authParams.get(name);
		if(value==null && required)
			throw new IOException("navigator.xml: <authentication> - missing <param name='"+name+"'>");
		return value;
	}

	/**
	 * Get a parameter from the <authentication> section of navigator.xml as a URL.
	 * @param name as in, <param name=''>.
	 * @param required if false, this method will return null if the parameter is missing. If true,
	 * 		it will throw an exception.
	 * @return The value of the named parameter, as a URL. And exception will be thrown if the 
	 *      parameter is not a valid URL.
	 * @throws IOException
	 */
	public URL getAuthParamURL(String name,boolean required) throws IOException
	{
		String value=getAuthParamString(name,required);
		try
		{
			return new URL(value);
		}
		catch(MalformedURLException e)
		{
			throw new IOException("navigator.xml: <authentication> - <param name='"+name+"'> is not a valid URL");
		}
	}

	
	/**
	 * Get a parameter from the <authentication> section of navigator.xml as a array of strings, 
	 * splitting the content of the parameter on whitespace.
	 * @param name as in, <param name=''>.
	 * @param required if false, this method will return null if the parameter is missing. If true,
	 * 		it will throw an exception.
	 * @return The value of the named parameter, as a string array.
	 * @throws IOException
	 */
	public String[] getAuthParamStrings(String name,boolean required) throws IOException
	{
		String value=getAuthParamString(name,required);
		return value.split("\\s+");
	}

	/** IP addresses that are trusted for server status */
	private String[] trustedAddresses;

	/** IP addresses from which you can run system reports. */
	private String[] secureAddresses;

	/**
	 * Initialises config.
	 * @param fConfig Config file
	 * @throws IOException If there's any problem parsing the file etc.
	 */
	public NavigatorConfig(File fConfig) throws IOException {
		Document dConfig=XML.parse(fConfig);
		Element eRoot=dConfig.getDocumentElement();

		Element eDB=XML.getChild(eRoot,"database");
		dbClass=XML.getRequiredAttribute(eDB,"plugin");
		dbServer=XML.getText(eDB,"server");
		dbName=XML.getText(eDB,"name");
		dbPrefix=XML.getText(eDB,"prefix");
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

		secureAddresses=XML.getTextFromChildren(XML.getChild(eRoot,"secureaddresses"),"address");
		for(int i=0;i<secureAddresses.length;i++)
		{
			if(!("."+secureAddresses[i]).matches("(.(([0-9]+(-[0-9]+)?)|\\*)){4}"))
				throw new IOException(
					"navigator.xml: <secureaddresses> <address> not in valid format: "+secureAddresses[i]);
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
		List<String> lNavigators=new LinkedList<String>();
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
		otherNavigators=lNavigators.toArray(new String[lNavigators.size()]);

		if(XML.hasChild(eRoot,"extrareports"))
		{
			extraReports=XML.getTextFromChildren(XML.getChild(eRoot,"extrareports"),"report");
		}
		else
		{
			extraReports = new String[0];
		}		

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
		
		if(XML.hasChild(eRoot,"extradatabases")) {
			Element eExtraDB=XML.getChild(eRoot, "extradatabases");
			Element[] eEDbs=XML.getChildren(eExtraDB, "database");
			for(Element e : eEDbs) {
				String key = XML.getText(e, "key");
				if (!Strings.isEmpty(key)) {
					extraDatabases.put(key, 
							new ExtraDatabase(key, XML.getText(e, "driverclass"), 
							XML.getText(e, "connectionurl"), XML.getText(e, "username"),
							XML.getText(e, "password")));
				}
			}
		}		
		
		if(XML.hasChild(eRoot, "standardadmins")) {
			Element[] eUsers=XML.getChildren(XML.getChild(eRoot,"standardadmins"));
			for (Element eUser : eUsers) {
				// OUCU is what the OU calls usernames
				if ("oucu".equals(eUser.getTagName()) || "username".equals(eUser.getTagName())) {
					standardAdmins.add(XML.getText(eUser));
				}				
			}
		}
		/* allows us to turn features on and off */
		
		if(XML.hasChild(eRoot, "optionalfeatures")) {
			Element eOptionalFeatures=XML.getChild(eRoot,"optionalfeatures");
			Element[] eFeatures=XML.getChildren(eOptionalFeatures, "feature");
			for (Element eFeature : eFeatures) 
			{	
				if (eFeature.hasAttribute("name") && eFeature.hasAttribute("enable"))
				{
					if (eFeature.getAttribute("enable").compareToIgnoreCase("yes") ==0)
					{
						optionalFeatures.add(eFeature.getAttribute("name"));
					}	
				}
			}
		}
		establishPreProcessor(eRoot);
		establishRequestManagementPosition(eRoot);
	}

	/**
	 * Checks to see if there is a <RequestManagement> node set and if it has
	 *  been configured with REQUEST_BASED.  If it has then the composite
	 *  requestManagementRequestBased boolean is set to true.
	 * @param eRoot
	 * @author Trevor Hinson
	 */
	private void establishRequestManagementPosition(Element eRoot) {
		String s = retrieveElementText(eRoot, REQUEST_MANAGEMENT_NODE);
		if (StringUtils.isNotEmpty(s)
			? REQUEST_BASED.equalsIgnoreCase(s) : false) {
			requestManagementRequestBased = true;
		}
	}

	/**
	 * Identifies and returns the textContent of a specified Element child
	 * 	within the provided eRoot element.  Wraps XML.getChild() with some
	 *  additional checks.
	 * @param eRoot
	 * @param nodeName
	 * @return
	 * @author Trevor Hinson
	 */
	private String retrieveElementText(Element eRoot, String nodeName) {
		String s = null;
		if (null != eRoot && StringUtils.isNotEmpty(nodeName)
			? XML.hasChild(eRoot, nodeName) : false) {
			try {
				Element e = XML.getChild(eRoot, nodeName);
				s = e.getTextContent();
			} catch (XMLException x) {
				x.printStackTrace();
			} catch (DOMException x) {
				x.printStackTrace();
			}
		}
		return s;
	}

	/**
	 * Used to pick up the configured "pre processor" from the navigator.xml
	 *  which is then invoked before other stages of the test rendering to the
	 *  user.
	 * @param eRoot
	 * @author Trevor Hinson
	 */
	private void establishPreProcessor(Element eRoot) {
		String pre = retrieveElementText(eRoot, PRE_PROCESSOR);
		if (StringUtils.isNotEmpty(pre)) {
			try {
				preProcessingRequestHandler = getClass().getClassLoader()
					.loadClass(pre);
			} catch (ClassNotFoundException x) {
				x.printStackTrace();
			}
		}
	}

	/** @return Full JDBC URL of database including username and password */
	public String getDatabaseURL(OmQueries oq) throws ClassNotFoundException {
		return oq.getURL(dbServer,dbName,dbUsername,dbPassword);
	}

	/**
	 * @return Array of trusted IP addresses in the form 0.1.2.3 or 0.1.2.* or
	 *    0.1.26-49.*
	 */
	public String[] getTrustedAddresses()
	{
		return trustedAddresses;
	}

	/**
	 * @return Array of trusted IP addresses in the form 0.1.2.3 or 0.1.2.* or
	 *    0.1.26-49.*
	 */
	public String[] getSecureAddresses()
	{
		return secureAddresses;
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
	 * @return the extraReports
	 */
	public String[] getExtraReports() {
		return extraReports;
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
	
	/**
	 * Get addtional database details from <extradatabases> tag.
	 * @param key for the database configuration
	 * @return database details
	 */
	public ExtraDatabase getExtraDatabase(String key) {		
		return extraDatabases.get(key);
	}
	
	/**
	 * @return array of standard admin usernames
	 */
	public String[] getStandardAdminUsernames() {
		return standardAdmins.toArray(new String[0]);
	}
	
	/** Additional database details */
	public static class ExtraDatabase {		
		private String key;
		
		private String driverClass;
		
		private String connectionUrl;
		
		private String username;
		
		private String password;
		
		public ExtraDatabase(String key, String driverClass,
				String connectionUrl, String username, String password) {
			this.key = key;
			this.driverClass = driverClass;
			this.connectionUrl = connectionUrl;
			this.username = username;
			this.password = password;
		}

		public String getKey() {
			return key;
		}

		public String getDriverClass() {
			return driverClass;
		}

		public String getConnectionUrl() {
			return connectionUrl;
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}		
		
	}
	
	public boolean isOptionalFeatureOn(String what)
	{	
		return optionalFeatures.contains(what);		
	}
}
