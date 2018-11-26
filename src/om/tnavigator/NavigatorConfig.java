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

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import om.OmException;
import om.tnavigator.util.IPAddressCheckUtil;
import util.misc.Strings;
import util.misc.UtilityException;

/** Loads navigator configuration file. */
public class NavigatorConfig
{
	/** Om services */
	private URL[] omServices;

	/** URL of this navigator */
	private URL thisTN;

	/** URLs of other test navigators */
	private String[] otherNavigators;

	/** The URL people acutally use to access OpenMark. */
	private String publicUrl;

	/** Class of database plugin */
	private String dbClass;

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

	/** Extra report plugins */
	private String[] extraReports;	

	/**
	 * Location of the maintenance mode file on disc.
	 * If this file exists, the system is in maintenance mode.
	 */
	private String maintenanceModeFile;

	/**
	 * Location of the not notifier file on disc.
	 * If this file exists, the overdue test notifer will not run.
	 */
	private String stopNotifierFile;

	/** Location of the testbank on disc. */
	private String testbankPath;

	/** Location of the questionbank on disc. */
	private String questionbankPath;

	/** Location of the openmark logs on disc. */
	private String logsPath;

	/** Template location. */
	private String templateLocation = "WEB-INF/templates";

	private boolean shouldRunOverdueTestNotifier;

	/** Parameters for auth */
	private Map<String,String> authParams=null;
	
	/** Standard admin usernames that may be required to be present in test deploy files */
	private List<String> standardAdmins = new ArrayList<String>();

	private Class<?> preProcessingRequestHandler;

	/** Parameters for stop file. */
	private String oldDataDeleteStopFile;

	/** Parameters to check if job needs to run. */
	private boolean shouldDeleteOldData;

	/** Parameters for time in hrs. */
	private String deleteDataDelay;

	public Class<?> retrievePreProcessingRequestHandler() {
		return preProcessingRequestHandler;
	}

	public String getAuthClass() {
		return authClass;
	}

	public String getDBClass() {
		return dbClass;
	}

	public String getDBPrefix() {
		return dbPrefix;
	}	

	private static Map<String,String> getParams(ServletContext sc) throws IOException
	{
		Map<String, String> m = new HashMap<String, String>();
		Enumeration<String> paramNames = sc.getInitParameterNames();
		while (paramNames.hasMoreElements())
		{
			String name = paramNames.nextElement();
			if (name.startsWith("auth-param-")) {
				m.put(name.substring("auth-param-".length()), sc.getInitParameter(name));
			}
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
			throw new IOException("Missing authentication parameter " + name + ".");
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
			throw new IOException("Missing authentication parameter " + name + ".");
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
	 * Make a blank config. Intended for unit tests only.
	 * @throws IOException
	 */
	public NavigatorConfig() throws IOException {
		extraReports = new String[] {};
		otherNavigators = new String[] {};
	}

	/**
	 * Initialises config.
	 * @param sc Servlet context from which to get the config.
	 * @throws IOException If there's any problem parsing the file etc.
	 * @throws OmException 
	 */
	public NavigatorConfig(ServletContext sc) throws IOException, OmException, UtilityException {
		dbClass = getParam(sc, "database-class");
		dbPrefix = getParam(sc, "database-prefix");

		publicUrl = getParam(sc, "public-url");
		thisTN = new URL(getParam(sc, "server-url"));
		otherNavigators = Strings.splitSensibly(getParam(sc, "server-other-urls"));
		for (int i = 0; i < otherNavigators.length; i++)
		{
			URL url = new URL(otherNavigators[i]);
			trustedTNs.add(InetAddress.getByName(url.getHost()));
		}

		String[] qeUrls = getParam(sc, "question-engine-service-urls").split("[, ]+");
		omServices = new URL[qeUrls.length];
		for (int i = 0; i < qeUrls.length; i++)
		{
			URL u = new URL(qeUrls[i]);
			omServices[i] = u;
			trustedQEs.add(InetAddress.getByName(u.getHost()));
		}
		if(omServices.length == 0)
		{
			throw new IOException("At least one question engine must be configured.");
		}

		Mail.setSMTPHost(getParam(sc, "smtp-server"));

		extraReports = Strings.splitSensibly(getParam(sc, "report-extra-classes"));

		authClass = getParam(sc, "authentication-class");
		authParams = getParams(sc);

		trustedAddresses = Strings.splitSensibly(getParam(sc, "trusted-ips"));
		IPAddressCheckUtil.checkIpAddressPatterns(trustedAddresses,
				"Invalid trusted-ips in the servlet configuration.");

		secureAddresses = Strings.splitSensibly(getParam(sc, "secure-ips"));
		IPAddressCheckUtil.checkIpAddressPatterns(secureAddresses,
				"Invalid secure-ips in the servlet configuration.");

		maintenanceModeFile = getParam(sc, "maintenance-mode-file");
		stopNotifierFile = getParam(sc, "stop-notifier-file");
		testbankPath = getParam(sc, "testbank-folder");
		questionbankPath = getParam(sc, "questionbank-folder");
		logsPath = getParam(sc, "logs-folder");
		templateLocation = getParam(sc, "template-folder");

		debugFlags.addAll(Arrays.asList(Strings.splitSensibly(getParam(sc, "log-flags"))));

		alertMailFrom = getParam(sc, "alert-email-from");
		alertMailTo = Strings.splitSensibly(getParam(sc, "alert-email-to"));
		alertMailCC = Strings.splitSensibly(getParam(sc, "alert-email-cc"));

		shouldRunOverdueTestNotifier = "yes".equals(getParam(sc, "run-overdue-notifier"));

		standardAdmins = Arrays.asList(Strings.splitSensibly(getParam(sc, "standard-admins")));

		shouldDeleteOldData = "yes".equals(getParam(sc, "run-deleteoldtestdata-jobs"));
		oldDataDeleteStopFile = getParam(sc, "stop-deleteoldtestdata-file");
		deleteDataDelay = getParam(sc, "run-delete-test-data-every-hours");
		try
		{
			preProcessingRequestHandler = getClass().getClassLoader().loadClass(
					getParam(sc, "preprocessor-class"));
		}
		catch(ClassNotFoundException e)
		{
			throw new OmException("Request preprocessor class could not be found.", e);
		}
	}

	/**
	 * Get a parameter, throwing an exception if it is not present.
	 * @param sc the servlet context.
	 * @param name the parameter to get.
	 * @return the value of the parameter.
	 * @throws OmException if the parameter is missing.
	 */
	private String getParam(ServletContext sc, String name) throws OmException {
		String value = sc.getInitParameter(name);
		if (null == value) {
			throw new OmException("Missing parameter " + name + " in the servlet configuration.");
		}
		return value;
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
	 * @return array of standard admin usernames
	 */
	public String[] getStandardAdminUsernames() {
		return standardAdmins.toArray(new String[0]);
	}

	/**
	 * @return the location of the maintenance mode file on disc.
	 * If this file exists, the system is in maintenance mode.
	 */
	public String getMaintenanceModeFilePath()
	{
		return 	maintenanceModeFile;
	}

	/**
	 * @return the location of the not notifier file on disc.
	 * If this file exists, the overdue test notifer will not run.
	 */
	public String getStopNotifierFilePath()
	{
		return 	stopNotifierFile;
	}

	/** @return the questionbankPath */
	public String getTestbankPath()
	{
		return testbankPath;
	}

	/** @return the questionbankPath */
	public String getQuestionbankPath()
	{
		return questionbankPath;
	}

	/** @return the questionbankPath */
	public String getLogsPath()
	{
		return logsPath;
	}

	/** @return the path to the folder where templates are stored. */
	public String getTemplateLocation()
	{
		return templateLocation;
	}

	public boolean shouldRunOverdueTestNotifier()
	{
		return shouldRunOverdueTestNotifier;
	}

	public String getPublicUrl()
	{
		return publicUrl;
	}

	/**
	 * The location of the deleting old file on disc.
	 * If this file exists, the job will not run.
	 * @return string file path
	 */
	public String getDeleteOldDataFilePath()
	{
		return oldDataDeleteStopFile;
	}

	/**
	 * Should deleting old test data job run
	 * @return boolean
	 */
	public boolean shouldDeleteOldData()
	{
		return shouldDeleteOldData;
	}

	/**
	 * Get the delete time from config file
	 * @return time.
	 */
	public String getDeleteDataDelay() {
		return deleteDataDelay;
	}
}
