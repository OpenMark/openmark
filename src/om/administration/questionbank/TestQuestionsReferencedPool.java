package om.administration.questionbank;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Provides a simple container for referenced Questions from the Tests.
 * @author Trevor Hinson
 */

public class TestQuestionsReferencedPool {

	private Set<String> names = new HashSet<String>();

	public void add(String name) {
		if (null != name ? name.length() > 0 : false) {
			names.add(name);
		}
	}

	public void add(List<String> found) {
		if (null != found ? found.size() > 0 : false) {
			for (String name : found) {
				add(name);
			}
		}
	}

	public Iterator<String> getReferencedNames() {
		return names.iterator();
	}

	public boolean contains(String name) {
		boolean found = false;
		if (null != name ? name.length() > 0 : false) {
			found = names.contains(name);
		}
		return found;
	}

}
