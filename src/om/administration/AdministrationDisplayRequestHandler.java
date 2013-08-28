package om.administration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.Log;
import om.abstractservlet.AbstractRequestHandler;
import om.abstractservlet.RequestAssociates;
import om.abstractservlet.RequestHandlerEnums;
import om.abstractservlet.RequestHandlerSettings;
import om.abstractservlet.RequestHandlingException;
import om.abstractservlet.RequestManagement;
import om.abstractservlet.RequestParameterNames;
import om.abstractservlet.RequestResponse;
import om.abstractservlet.StandardFinalizedResponse;
import util.misc.FinalizedResponse;
import util.misc.IO;
import util.misc.Strings;
import util.misc.UtilityException;
import util.xml.XML;

/**
 * Provides for rendering out the requestHandling.xml configuration settings so
 *  to provide them as options on the administration home page.
 * @author Trevor Hinson
 */

public class AdministrationDisplayRequestHandler extends AbstractRequestHandler {

	private static final long serialVersionUID = -5182723126562478570L;

	private static String ADMINISTRATION = "Administration";


	@Override
	public RequestResponse handle(HttpServletRequest request,
		HttpServletResponse response, RequestAssociates associates)
		throws RequestHandlingException {
		RequestResponse rr = super.handle(request, response, associates);
		RequestManagement rm = getRequestManagement(associates);
		String output = renderRequestHandlerSettings(associates, rm);
		if (Strings.isNotEmpty(output)) {
			String responseString = applyOutputToTemplate(request,
				associates, output);
			getLog().logDebug(responseString);
			rr.append(responseString);
		} else {
			rr.setSuccessful(false);
		}
		return rr;
	}

	/**
	 * Returns the configured template path location held within the
	 *  configuration.
	 * @param ra
	 * @return
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	protected String retrieveFullTemplatePath(RequestAssociates ra)
		throws RequestHandlingException {
		String fullTemplatePath = null; 
		String templateName = null != ra ? ra.getConfig(
			AdministrationDisplayEnums.template.toString()) : null;		
		if (Strings.isNotEmpty(templateName)) {
			fullTemplatePath = ra.getServletContext().getRealPath(templateName);
		}
		return fullTemplatePath;
	}

	/**
	 * Combines the output argument with the configured template for display
	 *  back to the end user.
	 * @param ra
	 * @param output
	 * @return
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	protected String applyOutputToTemplate(HttpServletRequest request,
		RequestAssociates ra, String output) throws RequestHandlingException {
		String response = null;
		String templateLocation = retrieveFullTemplatePath(ra);
		getLog().logDebug("Full Template Path : " + templateLocation);
		Map<String,String> replacements = new HashMap<String, String>();
		replacements.put(AdministrationDisplayEnums.TIME.toString(),
			Log.DATETIMEFORMAT.format(new Date()));
		replacements.put(AdministrationDisplayEnums.TITLE.toString(),
			ADMINISTRATION);
		replacements.put(AdministrationDisplayEnums.MESSAGE.toString(),
			output);
		try {
			response = IO.loadString(new FileInputStream(
				new File(templateLocation)));
			response = XML.replaceTokens(response, "%%", replacements);
		} catch (IOException x) {
			throw new RequestHandlingException(x);
		}
		return response;
	}

	/**
	 * Renders to a StringBuffer the details of the RequestHandlers so that they
	 *  can be displayed within the Administration screen so to be able to
	 *  invoke them.  This is achieved by iterating over all of the configured
	 *  RequestHandlers.
	 * @param rm
	 * @return
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	protected String renderRequestHandlerSettings(RequestAssociates associates,
		RequestManagement rm) throws RequestHandlingException 
	{
		StringBuffer sb = new StringBuffer();
		Map<String, RequestHandlerSettings> rhs = getRequestHandlerSettings(rm);
		/* get the values into a list */
	    List<RequestHandlerSettings> rhsValues = new ArrayList<RequestHandlerSettings>(rhs.values());
		Comparator<RequestHandlerSettings> rhsComp=RequestHandlerSettings.rhsDisplayNameComparator;
	    Collections.sort(rhsValues,rhsComp);	
		if (null != rhs && null != associates) 
		{
			String contextPath = associates.getServletContext().getContextPath();
			if (Strings.isNotEmpty(contextPath)) 
			{
				for (RequestHandlerSettings settings : rhsValues) 
				{
					sb.append(renderRequestHandlerSettings(contextPath, settings));
				}
			}
		}
		return sb.toString();
	}
	 


	/**
	 * Renders an individual RequestHandlerSettings to an xhtml format for
	 *  displaying on the administration screen.  Note that each of the 
	 *  invocationPath, displayName, description and contextPath need to be
	 *  present and valid in the requestHandling.xml for it to be rendered here.
	 * @param contextPath
	 * @param rhs
	 * @return
	 * @author Trevor Hinson
	 */
	protected StringBuffer renderRequestHandlerSettings(String contextPath,
		RequestHandlerSettings rhs) {
		StringBuffer sb = new StringBuffer();
		if (null != rhs && Strings.isNotEmpty(contextPath)) {
			String displayName = rhs.get(RequestHandlerEnums.displayName);
			String description = rhs.get(RequestHandlerEnums.description);
			String invocationPath = rhs.getInvocationPath();
			String servletName=rhs.getServletName();

			if (Strings.isNotEmpty(displayName)
				&& Strings.isNotEmpty(description)
				&& Strings.isNotEmpty(invocationPath)) {
				sb.append("<p>").append("<b><a href=\"")
					.append("/").append(servletName).append(invocationPath)
//					.append("/").append(SERVLETNAME).append(invocationPath)
//					.append("/").append(invocationPath)
					.append("\">").append(displayName)
					.append("</a></b>").append("<br /><br />")
					.append(description).append("</p>");
			}
		}
		return sb;
	}

	/**
	 * Tries to pick up and return the configuration from the RequestManagement
	 *  and return the settings.
	 * @param rm
	 * @return
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	protected Map<String, RequestHandlerSettings> getRequestHandlerSettings(
		RequestManagement rm) throws RequestHandlingException {
		Map<String, RequestHandlerSettings> rhs = null;
		if (null != rm ? null != rm.getSettings() : false) {
			rhs = rm.getSettings();
		}
		return rhs;
	}

	/**
	 * Tries to return the RequestManagement from the RequestAssociates argument
	 * @param associates
	 * @return
	 * @throws RequestHandlingException
	 * @author Trevor Hinson
	 */
	protected RequestManagement getRequestManagement(RequestAssociates associates)
		throws RequestHandlingException {		
		RequestManagement rm = null;
		if (null != associates) {
			rm = associates.get(RequestManagement.class,
				RequestParameterNames.RequestManagement);
		}
		return rm;
	}

	@Override
	public FinalizedResponse close(Object o) throws UtilityException {
		try {
			getLog().close();
		} catch (RequestHandlingException x) {
			x.printStackTrace();
		}
		return new StandardFinalizedResponse(true);
	}

}
