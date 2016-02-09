package om.administration.questionbank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.misc.QuestionVersion;
import util.misc.Strings;
import util.misc.VersionUtil;

/**
 * Holds all references to a particular question from the different question
 * banks.  The internal collection "questionsWithVersionNumbering" holds
 * reference to the actual question name with the version numbering also in
 * order to identify the latest version and also so to fully qualify those that
 * are then superfluous.
 */
public class QuestionPoolDetails {

	private String namePrefix;

	Map<String, Set<String>> questionsWithVersionNumbering = new HashMap<String, Set<String>>();

	public QuestionPoolDetails(String prefix) {
		namePrefix = prefix;
	}

	/**
	 * In order to determine those Questons which are not relevant any more we
	 *  need to identify the latest version so that is kept.
	 * @return
	 * @author Trevor Hinson
	 */
	public String identifyLatestVersion() {
		Set<String> keys = questionsWithVersionNumbering.keySet();
		QuestionVersion qv = VersionUtil.findLatestVersion(namePrefix,
				VersionUtil.VERSION_UNSPECIFIED, keys);
		if (qv == null) {
			return null;
		}
		return namePrefix + "." + qv + ".jar";
	}

	/**
	 * Adds to the composite collection the question name and the location it
	 *  has been found in.
	 * @param name
	 * @param locationFoundIn
	 * @author Trevor Hinson
	 */
	public void addTo(String name, String locationFoundIn) {
		if (Strings.isNotEmpty(name)
			&& Strings.isNotEmpty(locationFoundIn)) {
			Set<String> qu = questionsWithVersionNumbering.get(name);
			if (null == qu) {
				qu = new HashSet<String>();
				questionsWithVersionNumbering.put(name, qu);
			}
			qu.add(locationFoundIn);
		}
	}

	public Map<String, Set<String>> getQuestionsWithVersionNumbering() {
		return questionsWithVersionNumbering;
	}

	public String toString() {
		return null != namePrefix ? namePrefix : super.toString();
	}
}
