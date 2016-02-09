package util.misc;

import java.io.File;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import om.OmException;

public class VersionUtil {

	public final static int VERSION_UNSPECIFIED = -1;

	public static String suffix = ".([0-9]+).([0-9]+).jar";

	public static String xmlSuffix = ".([0-9]+).([0-9]+).omxml";

	public static String DOT_JAR = ".jar";

	public static String DOT = ".";

	/**
	 * Looks through the provided File[] arguments for the latest version of
	 * a question based on the sQuestionID argument.
	 * @param sQuestionID the questionId of interest.
	 * @param iRequiredVersion required major version, or VERSION_UNSPECIFIED for any.
	 * @param af list of question .jar files.
	 * @return the latest QuestionVersion of one was found, else null.
	 * @throws OmException
	 */
	public static QuestionVersion findLatestVersion(String sQuestionID,
			int iRequiredVersion, File[] af) throws OmException
	{

		Pattern p = Pattern.compile(sQuestionID + suffix);

		return findLatestVersionOfQuestion(sQuestionID, iRequiredVersion, af, p);
	}

	/**
	 * Find the latest version of a given questionId.
	 * @param sQuestionID the questionId of interest.
	 * @param iRequiredVersion required major version, or VERSION_UNSPECIFIED for any.
	 * @param af list of question .jar files.
	 * @param p File name regex.
	 * @return the latest QuestionVersion of one was found, else null.
	 * @throws OmException
	 * **/
	private static QuestionVersion findLatestVersionOfQuestion(String sQuestionID,
			int iRequiredVersion, File[] af, Pattern p) throws OmException {
		boolean bFound = false;
		QuestionVersion qv = new QuestionVersion(0, 0);
		for (int i = 0; i < af.length; i++) {
			Matcher toUse = null;
			// See if it's the question we're looking for
			Matcher m = p.matcher(af[i].getName());
			boolean mMatched = m.matches();
			if (!mMatched ) {
				continue;
			} else {
				if (mMatched) {
					toUse = m;
				}
			}
			if (null != toUse) {
				int iMajor = Integer.parseInt(toUse.group(1)),
						iMinor = Integer.parseInt(toUse.group(2));
				if (
				// Major version is better than before and either matches version or
				// unspecified.
				(iMajor > qv.getMajor() && (iRequiredVersion == iMajor || iRequiredVersion == VERSION_UNSPECIFIED))
					||
					// Same major version as before, better minor version
					(iMajor == qv.getMajor() && iMinor > qv.getMinor())) {
					qv = new QuestionVersion(iMajor, iMinor);
					bFound = true;
				}
			}
		}
		if (bFound) {
			return qv;
		} else {
			return null;
		}
	}

	/**
	 * Looks through the provided Set<String> names in order to identify the
	 *  latest version.
	 * @param sQuestionID
	 * @param iRequiredVersion
	 * @param qv
	 * @param names
	 * @return
	 */
	public static QuestionVersion findLatestVersion(String sQuestionID,
			int iRequiredVersion, Set<String> names) {
		boolean bFound = false;
		QuestionVersion qv = new QuestionVersion(0, 0);
		Pattern p = Pattern.compile(sQuestionID + suffix);
		for (String name : names) {
			Matcher m = p.matcher(name);
			if (!m.matches())
				continue;

			int iMajor = Integer.parseInt(m.group(1)), iMinor =
					Integer.parseInt(m.group(2));
			if (
					// Major version is better than before and either matches version or
					// unspecified.
					(iMajor > qv.getMajor() && (iRequiredVersion == iMajor || iRequiredVersion == VERSION_UNSPECIFIED))
				||
					// Same major version as before, better minor version
					(iMajor == qv.getMajor() && iMinor > qv.getMinor())) {
				qv = new QuestionVersion(iMajor, iMinor);
				bFound = true;
			}
		}
		if (bFound) {
			return qv;
		} else {
			return null;
		}
	}

	public static QuestionName represented(String fileName) {
		QuestionName qn = null;
		if (Strings.isNotEmpty(fileName) ? fileName.contains(DOT) : false) {
			if (fileName.endsWith(DOT_JAR)) {
				String questionName = GeneralUtils.questionNamePrefix(fileName);
				if (Strings.isNotEmpty(questionName)) {
					String remainder = fileName.substring(questionName.length() + 1,
						fileName.length() - DOT_JAR.length());
					if (Strings.isNotEmpty(remainder)) {
						String[] bits = remainder.split("\\.");
						if (null != bits ? bits.length == 2 : false) {
							try {
								QuestionVersion ver = new QuestionVersion(new Integer(bits[0]), new Integer(bits[1]));
								qn = new QuestionName(questionName, ver);
							} catch (NumberFormatException x) {
								// log ...
								// x.printStackTrace();
							}
						}
					}
				}
			}
		}
		return qn;
	}


}
