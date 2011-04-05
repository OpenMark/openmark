package om.devservlet.deployment.transport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import om.DisplayUtils;
import om.devservlet.deployment.RenderedOutput;
import om.devservlet.deployment.QuestionDeploymentRenderer;
import om.devservlet.deployment.QuestionHolder;
import om.devservlet.deployment.QuestionTransporter;
import om.devservlet.deployment.QuestionTransporterException;

import org.apache.commons.lang.StringUtils;

import util.misc.GeneralUtils;

/**
 * This implementation of the QuestionTransporter takes the provided Question
 *  and copies it to the given locations.  Obviously in order for this to work
 *  the solution requires read and write access to the locations configured
 *  and the Question location also.  In the event of failures for what ever
 *  reason or issues such as not being able to determine the next version of a
 *  question to apply to the file we report this back to the user through the
 *  visiting RenderedOutput object.
 * @author Trevor Hinson
 */

public class StandardQuestionTransporter implements QuestionTransporter {

	private static String FIRST_VERSION = ".1.0";

	private static String REPORT_TO_MESSAGE_KEY = "ReportToMessage";

	@Override
	public void deploy(QuestionHolder qh, Map<String, String> metaData,
		RenderedOutput or) throws QuestionTransporterException {
		if (null != qh && null != metaData && null != or) {
			startReport(or, qh);
			List<String> locations = DisplayUtils.getLocations(metaData);
			or.append(" - Setup to copy to these locations : ")
				.append(locations).append(QuestionDeploymentRenderer.BRS);
			LatestNameAndVersion ver = determineCurrentVersion(locations,
				metaData, qh, or);
			if (null != ver ? ver.valid() : false) {
				File copy = makeLocalCopy(qh, newFileNamePrefix(ver), or);
				boolean success = deployCopy(copy, or, metaData);
				tidyUp(copy, or);
				or.append(QuestionDeploymentRenderer.BRS)
					.append(" - <b>The transfer of the Question was : ")
					.append(success ? "successful" : "unsuccessful.")
					.append("</b>").append(QuestionDeploymentRenderer.BRS);;
				
			} else {
				handleInvalidVersioning(or, metaData);
			}
		}
	}

	/**
	 * Invoked to ensure that the copy of the Question itself is removed from
	 *  the Question Developers local instance.
	 * @param copy
	 * @param or
	 * @author Trevor Hinson
	 */
	private void tidyUp(File copy, RenderedOutput or) {
		or.append(QuestionDeploymentRenderer.BRS)
			.append(" - The tidy up of the local copy : ").append(copy.getName())
			.append(" was ").append(
				copy.delete() ? "successful" : "unsuccessful")
			.append(QuestionDeploymentRenderer.BRS);
	}

	private void startReport(RenderedOutput or, QuestionHolder qh) {
		or.append("<h2>Deployment of : ")
		.append(qh).append(QuestionDeploymentRenderer.BRS).append("</h2>");
	}

	/**
	 * Report invalid versioning to the user.  Adds in the additional configured
	 *  contact details for whom to report this to.
	 * @param or
	 * @author Trevor Hinson
	 */
	private void handleInvalidVersioning(RenderedOutput or,
		Map<String, String> metaData) {
		or.append(QuestionDeploymentRenderer.BRS)
			.append(" - The latest version could not be determined.")
			.append("  Please check the deploy locations to ensure that")
			.append(" they are in sync as it seems that they are not at the moment.");
		appendConfiguredReportMessage(metaData, or);
	}

	private void appendConfiguredReportMessage(Map<String, String> metaData,
		RenderedOutput or) {
		String msg = metaData.get(REPORT_TO_MESSAGE_KEY);
		if (StringUtils.isNotEmpty(msg)) {
			or.append(QuestionDeploymentRenderer.BRS)
				.append("<div class=\"alert\">").append(msg).append("</div>");
		}
	}

	/**
	 * Tries to copy the source Question to the configured locations.  If there
	 *  is a failure then it will call the rollBack and report this through
	 *  the OutputRendering object which is presented back to the user. 
	 * @param source The Question file itself.
	 * @param or For messages back to the user.
	 * @param metaData Containing the configuration from the qengine.xml
	 * @author Trevor Hinson
	 */
	boolean deployCopy(File source, RenderedOutput or,
		Map<String, String> metaData) {
		List<String> locations = DisplayUtils.getLocations(metaData);
		List<String> locationsCopiedTo = new ArrayList<String>();
		boolean copySucceeded = true;
		x : for (String location : locations) {
			File dir = new File(location);
			if (dir.exists() && dir.canWrite()) {
				File target = new File(location + "/" + source.getName());
				try {
					if (target.createNewFile()) {
						GeneralUtils.copyFile(source, target);
						locationsCopiedTo.add(location);
					} else {
						throw new IOException("Unable to create the file : "
							+ target.getAbsolutePath());
					}
				} catch (IOException x) {
					or.append(QuestionDeploymentRenderer.BRS)
						.append(" - Unable to copy the Question to : ")
						.append(location).append(" - The Exception was : ")
						.append(x.getMessage());
					copySucceeded = false;
					break x;
				}
			}
		}
		if (!copySucceeded) {
			rollBack(locationsCopiedTo, source, or, metaData);
		}
		return copySucceeded;
	}

	/**
	 * In the event of a failure is copying the Question to a location we
	 *  rollback and try to remove those that did get copied over.
	 * @param rollBackFrom
	 * @param source
	 * @param metaData configuration details
	 * @param or
	 * @author Trevor Hinson
	 */
	void rollBack(List<String> rollBackFrom, File source, RenderedOutput or,
		Map<String, String> metaData) {
		for (String location : rollBackFrom) {
			File f = new File(location + "/" + source.getName());
			if (!f.delete()) {
				problemRemovingOutOfSyncCopy(or, f, metaData);
			}
		}
	}

	/**
	 * Here we simply provide notification to the user that there is a
	 *  synchronization with the different questionbank location configured.
	 * @param or
	 * @param f
	 * @param metaData configuration details
	 * @author Trevor Hinson
	 */
	void problemRemovingOutOfSyncCopy(RenderedOutput or, File f,
		Map<String, String> metaData) {
		or.append(QuestionDeploymentRenderer.BRS)
			.append(" - There was a problem removing the file from the server in order")
			.append(" to make things synchronised.  This is a big issue. ")
			.append("The file that was not removed was : ")
			.append(f.getAbsolutePath())
			.append("<h2>Please report this.</h2>\n\n");
		appendConfiguredReportMessage(metaData, or);
	}

	/**
	 * Makes a local copy of the Question with the next version number so that
	 *  it may be distributed to the various configured locations.
	 * @param qh
	 * @param newNamePrefix
	 * @param or
	 * @return
	 * @author Trevor Hinson
	 */
	File makeLocalCopy(QuestionHolder qh, String newNamePrefix,
		RenderedOutput or) {
		File newCopy = new File(qh.getOriginatingLocation() + "/"
			+ newNamePrefix + QuestionDeploymentRenderer.DEPLOY_FILE_SUFFIX);
		try {
			if(newCopy.createNewFile()) {
				GeneralUtils.copyFile(qh.getJarFile(), newCopy);
			} else {
				throw new IOException();
			}
		} catch (IOException x) {
			or.append(QuestionDeploymentRenderer.BRS)
				.append(" -There was a problem creating a copy of the ")
				.append("original Question : ").append(x.getMessage());
			newCopy = null;
		}
		return newCopy;
	}

	/**
	 * Tries to identify the file name to use by incrementing the version number
	 *  from that found.
	 * @param ver A representation of the latest version found of the question.
	 * @return
	 * @author Trevor Hinson
	 */
	String newFileNamePrefix(LatestNameAndVersion ver) {
		StringBuffer newPrefix = null;
		if (ver.valid()) {
			newPrefix = new StringBuffer();
			newPrefix.append(ver.fileNamePrefix).append(".")
				.append(ver.version.primary).append(".")
				.append(ver.version.secondary + 1);
		}
		return null != newPrefix ? newPrefix.toString() : null;
	}

	/**
	 * Will return the LatestNameAndVersion of the Question to use so that we
	 *  can copy across to the configured locations in that name.  NOTE that if
	 *  no agreed latest version can be determined then we will try to create a
	 *  new LatestNameAndVersion representation.
	 * @param locations
	 * @param metaData
	 * @param qh
	 * @param or
	 * @return
	 * @author Trevor Hinson
	 */
	private LatestNameAndVersion determineCurrentVersion(List<String> locations,
		Map<String, String> metaData, QuestionHolder qh, RenderedOutput or) {
		LatestNameAndVersion version = null;
		if (null != locations ? locations.size() > 0 : false) {
			Map<String, LatestNameAndVersion> versions = retrieveLatestVersions(
				locations, metaData, qh, or);
			if (null != versions ? versions.size() > 0 : false) {
				version = agreedLatestVersion(versions);
			} else {
				// For when we have a completely new Question ...
				version = new LatestNameAndVersion();
				version.fileNamePrefix = qh.getNamePrefix() + FIRST_VERSION;
			}
		}
		or.append(" - LatestNameAndVersion found within the locations : ")
			.append(version);
		return version;
	}

	/**
	 * Determines the actual latest version from those picked up in the various
	 *  locations.  If there is a disparity in the versioning then nothing is
	 *  returned and we need to deal with that as a failed copy because the
	 *  servers are currently out of sync.
	 * @param vers
	 * @return
	 * @author Trevor Hinson
	 */
	private LatestNameAndVersion agreedLatestVersion(
		Map<String, LatestNameAndVersion> vers) {
		LatestNameAndVersion identified = null;
		boolean first = true;
		for (LatestNameAndVersion version : vers.values()) {
			if (first) {
				identified = version;
				first = false;
			} else {
				if (null != identified) {
					if (!version.validMatch(identified)) {
						identified = null;
					}
				}
			}
		}
		return identified;
	}

	/**
	 * Will look within each of the provided locations for the latest version
	 *  and return a Collection of these that can be used to determine the next
	 *  version number along with checking to identify if things match on all
	 *  instances.
	 * @param locations
	 * @param metaData
	 * @param qh
	 * @param or
	 * @return
	 * @author Trevor Hinson
	 */
	private Map<String, LatestNameAndVersion> retrieveLatestVersions(
		List<String> locations, Map<String, String> metaData, QuestionHolder qh,
		RenderedOutput or) {
		Map<String, LatestNameAndVersion> latest = new HashMap<String, LatestNameAndVersion>();
		x: for (String location : DisplayUtils.getLocations(metaData)) {
			File directory = new File(location);
			if (directory.exists() && directory.canRead() && directory.canWrite()) {
				LatestNameAndVersion nandv = identifyLatestVersion(qh, directory, or);
				if (null != nandv) {
					latest.put(location, nandv);
				}
			} else {
				problemWithTestDankDirectory(location, or);
				break x;
			}
		}
		return latest;
	}

	/**
	 * Simplified data holder for the most recent version found of the given
	 *  question in the locations specified.
	 * @author Trevor Hinson
	 */
	class LatestNameAndVersion {
		
		Versioner version;
		
		// The file name without .jar
		String fileNamePrefix;
		
		boolean valid() {
			return null != fileNamePrefix ? (
				fileNamePrefix.length() > 0
					&& null != version ? version.valid() : false)
					: false;
		}
		
		public String toString() {
			return "Name : " + fileNamePrefix + " - version : " + version; 
		}
		
		public boolean validMatch(LatestNameAndVersion ver) {
			boolean is = false;
			if (null != fileNamePrefix
				? fileNamePrefix.equals(ver.fileNamePrefix) : false) {
				if (null != version && null != ver.version) {
					if (version.validMatch(ver.version)) {
						is = true;
					}
				}
			}
			return is;
		}
	}

	private void problemWithTestDankDirectory(String location,
		RenderedOutput or) {
		or.append(QuestionDeploymentRenderer.BRS)
			.append(" - There was a problem with the testbank directory. ")
			.append(" Either it does not exist or it can not be read : ")
			.append(location);
	}

	/**
	 * Will look within the given testbank directory for the latest version
	 *  of the given Question. 
	 * The method will always return a LatestNameAndVersion object even when
	 *  nothing is found so that we can test against the results to ensure that
	 *  everything is in sync.
	 * @param directory
	 * @return
	 * @author Trevor Hinson
	 */
	LatestNameAndVersion identifyLatestVersion(QuestionHolder qh,
		File directory, RenderedOutput or) {
		File[] questions = directory.listFiles();
		List<Versioner> versions = new ArrayList<Versioner>();
		for (int i = 0; i < questions.length; i++) {
			File f = questions[i];
			if (null != f ? f.getName().endsWith(
				QuestionDeploymentRenderer.DEPLOY_FILE_SUFFIX) : false) {
				if (f.getName().contains(qh.getNamePrefix())) {
					Versioner ver = retrieveVersionerFromFileName(f.getName());
					if (null != ver) {
						versions.add(ver);
					}
				}
			}
		}
		return ensureLatestMatch(versions, qh);
	}

	/**
	 * Checks the collection of Versioner's and returns the latest version.  In
	 *  the event when we do not have any Versioner's within the given
	 *  collection then it assumes that we are dealing with a completely new
	 *  Question and returns a completely plain (1.0) new LatestNameAndVersion.
	 * @param versions
	 * @param qh
	 * @return
	 * @author Trevor Hinson
	 */
	private LatestNameAndVersion ensureLatestMatch(List<Versioner> versions,
		QuestionHolder qh) {
		LatestNameAndVersion nandv = null;
		if (null != versions && null != qh) {
			if (versions.size() > 0) {
				Versioner version = getMostRecent(versions);
				if (null != version) {
					nandv = new LatestNameAndVersion();
					nandv.fileNamePrefix = qh.getNamePrefix();
					nandv.version = version;
				}
			} else {
				// We treat this case as a new version of the question ...
				nandv = new LatestNameAndVersion();
				nandv.fileNamePrefix = qh.getNamePrefix();
				nandv.version = create("1.0");
			}			
		}
		return nandv;
	}

	/**
	 * Derives a Versioner object from the name of that passed in.
	 *  The name is assumed to be in this format :
	 *  samples.mu120.module5.question02.4.5.jar
	 * @author Trevor Hinson 
	 */
	Versioner retrieveVersionerFromFileName(String name) {
		Versioner v = null;
		if (StringUtils.isNotEmpty(name) ? name.length() > 7 : false) {
			String workWith = name;
			if (workWith.endsWith(QuestionDeploymentRenderer.DEPLOY_FILE_SUFFIX)) {
				workWith = name.substring(0, name.length() - 4);
			}
			int sec = workWith.lastIndexOf(".");
			if (sec > -1 ? workWith.length() > sec + 1 : false) {
				String second = workWith.substring(sec + 1, workWith.length());
				String remainder = workWith.substring(0, sec);
				int fir = remainder.lastIndexOf(".");
				if (fir > -1 ? remainder.length() > fir + 1 : false) {
					String first = remainder.substring(fir + 1, remainder.length());
					v = create(first + "." + second);
				}
			}
		}
		return v;
	}

	/**
	 * Create an internal representation of the name of the question file
	 *  so that we can test it against all the others.
	 * @author Trevor Hinson
	 */
	Versioner create(String s) {
		Versioner v = null;
		if (StringUtils.isNotEmpty(s)) {
			int n = s.indexOf(".");
			if (n > -1 ? s.length() > n + 1 : false) {
				String first = s.substring(0, n);
				String second = s.substring(n + 1, s.length());
				try {
					Integer primary = new Integer(first);
					Integer secondary = new Integer(second);
					v = new Versioner();
					v.primary = primary;
					v.secondary = secondary;
				} catch (NumberFormatException x) {
					// log this ...
				}
			}
		}
		return v;
	}

	/**
	 * Provides a simple check of the collection of Versioner's to ensure that
	 *  they are all of the SAME version level.  If not then we have a potential
	 *  system out of sync issue which will be noted to the Question Developer
	 *  and should be resolved as a matter of urgency.
	 * @param vers
	 * @return
	 * @author Trevor Hinson
	 */
	Versioner getMostRecent(List<Versioner> vers) {
		Versioner ver = null;
		for (Versioner v : vers) {
			if (null == ver) {
				ver = v;
			} else {
				if (ver.isLessThan(v)) {
					ver = v;
				}
			}
		}
		return ver;
	}

	/**
	 * A simple class that holds the details of the particular version of a
	 *  given Question.  This is then used to test against other Versioner
	 *  objects so to do things like see if they match or if one is less than
	 *  the other. 
	 * @author Trevor Hinson
	 */
	class Versioner {

		int primary = 0;

		int secondary = 0;

		boolean isLessThan(Versioner v) {
			boolean is = false;
			if (null != v) {
				if (v.primary > primary) {
					is = true;
				} else if (v.primary == primary) {
					if (v.secondary > secondary) {
						is = true;
					}
				}
			}
			return is;
		}
		
		boolean valid() {
			return primary > -1 && secondary > -1;
		}
		
		public String toString() {
			return primary + "." + secondary;
		}
		
		public boolean validMatch(Versioner v) {
			boolean is = false;
			if (null != v ? v.primary == primary
				&& v.secondary == secondary : false) {
				is = true;
			}
			return is;
		}
	}

}
