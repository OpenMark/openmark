package om.administration.questionbank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Holds reference to a superfluous question.
 * @author Trevor Hinson
 */

public class IdentifiedSuperfluousQuestion {

	private String name;

	private List<String> locationsFoundIn = new ArrayList<String>();

	public String getName() {
		return name;
	}

	public void setName(String s) {
		if (null != s? s.length() > 0 : false) {
			name = s;
		}
	}

	public IdentifiedSuperfluousQuestion(String questionName, Set<String> locations) {
		if (null != locations ? locations.size() > 0 : false) {
			locationsFoundIn.addAll(locations);
		}
		name = questionName;
	}

	public Iterator<String> getLocations() {
		return Collections.unmodifiableList(locationsFoundIn).iterator();
	}

	/**
	 * Used to see if we have something out of sync within each of the
	 *  environments.  If that is the case then we know that this question is
	 *  problematic and should be highlighted as such for removal.
	 * @param checkIsIn
	 * @return
	 * @author Trevor Hinson
	 */
	public boolean isInAllLocations(List<String> checkIsIn) {
		boolean found = true;
		if (null != checkIsIn) {
			x : for (String name : checkIsIn) {
				if(!locationsFoundIn.contains(name)) {
					found = false;
					break x;
				}
			}
		} else {
			found = false;
		}
		return found;
	}
}
