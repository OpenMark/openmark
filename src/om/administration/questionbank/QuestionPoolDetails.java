package om.administration.questionbank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import util.misc.QuestionVersion;
import util.misc.VersionUtil;

/**
 * Holds all references to a particular question from the different question
 *  banks.  The internal collection "questionsWithVersionNumbering" holds
 *  reference to the actual question name with the version numbering also in
 *  order to identify the latest version and also so to fully qualify those that
 *  are then superfluous.
 * @author Trevor Hinson
 */

class QuestionPoolDetails {

	private static String JAR = ".jar";

	private static String DOT = ".";

	private String namePrefix;

	Map<String, Set<String>> questionsWithVersionNumbering
		= new HashMap<String, Set<String>>();

	QuestionPoolDetails(String prefix) {
		namePrefix = prefix;
	}

	/**
	 * In order to determine those Questons which are not relevant any more we
	 *  need to identify the latest version so that is kept.
	 * @return
	 * @author Trevor Hinson
	 */
	String identifyLatestVersion() {
		String latest = null;
		Set<String> keys = questionsWithVersionNumbering.keySet();
		QuestionVersion qv = new QuestionVersion();
		if (VersionUtil.findLatestVersion(namePrefix,
			VersionUtil.VERSION_UNSPECIFIED, qv, keys)) {
			latest = namePrefix + DOT + qv.toString() + JAR;
		}
		return latest;
	}

	/**
	 * Adds to the composite collection the question name and the location it
	 *  has been found in.
	 * @param name
	 * @param locationFoundIn
	 * @author Trevor Hinson
	 */
	void addTo(String name, String locationFoundIn) {
		if (StringUtils.isNotEmpty(name)
			&& StringUtils.isNotEmpty(locationFoundIn)) {
			Set<String> qu = questionsWithVersionNumbering.get(name);
			if (null == qu) {
				qu = new HashSet<String>();
				questionsWithVersionNumbering.put(name, qu);
			}
			qu.add(locationFoundIn);
		}
	}

	Map<String, Set<String>> getQuestionsWithVersionNumbering() {
		return questionsWithVersionNumbering;
	}

	public String toString() {
		return null != namePrefix ? namePrefix : super.toString();
	}
}
