package om.administration.questionbank;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds reference to a Test that is out of sync with other Test banks.
 * @author Trevor Hinson
 */

public class TestSynchronizationCheck {

	// The actual name of the test itself. For example : sdk125-10j.icma46.test.xml
	private String name;

	private Map<String, TestDetails> testDetails = new HashMap<String, TestDetails>();

	public void setName(String s) {
		name = s;
	}

	public String getName() {
		return name;
	}

	/**
	 * Provides a list of the locations from which there is a valid TestDetails
	 *  which represents if a Test is actually present in a given location.
	 * 
	 * @return
	 * @author Trevor Hinson
	 */
	public List<String> getLocationsTestIsFoundIn() {
		List<String> locations = new ArrayList<String>();
		for (String key : getTestDetails().keySet()) {
			if (null != key ? key.length() > 0 : false) {
				TestDetails td = getTestDetails().get(key);
				if (null != td) {
					locations.add(key);
				}
			}
		}
		return locations;
	}

	public Map<String, TestDetails> getTestDetails() {
		if (null == testDetails) {
			testDetails = new HashMap<String, TestDetails>();
		}
		return testDetails;
	}

	public TestSynchronizationCheck(List<String> allLocations) {
		if (null != allLocations) {
			for (String s : allLocations) {
				if (null != s ? s.length() > 0 : false) {
					testDetails.put(s, null);
				}
			}
		}
	}

	/**
	 * Checks to see if a question by the questionName argument is referenced
	 *  within any of the composite TestDetails.
	 * 
	 * @param questionName
	 * @return
	 * @author Trevor Hinson
	 */
	public boolean referencesQuestion(String questionName) {
		boolean does = false;
		if (null != questionName ? questionName.length() > 0 : false) {
			if (null != testDetails) {
				x : for (TestDetails td : testDetails.values()) {
					if (null != td ? null != td.getQuestionsReferenced() : false) {
						does = td.getQuestionsReferenced()
							.containsQuestionReference(questionName);
						if (does) {
							break x;
						}
					}
				}
			}
		}
		return does;
	}

	/**
	 * Checks that the Test itself is within each of the predetermined Test
	 *  banks and returns true if that is the case.
	 * 
	 * @return
	 * @author Trevor Hinson
	 */
	public boolean isFoundInAllTestBanks() {
		boolean is = false;
		if (null != testDetails) {
			boolean positive = true;
			x : for (String key : testDetails.keySet()) {
				Object o = testDetails.get(key);
				if (null == o) {
					positive = false;
					break x;
				}
			}
			is = positive;
		}
		return is;
	}

	/**
	 * Simplified test against the number of TestDetails that this object is 
	 *  associated with.
	 * 
	 * @return
	 * @author Trevor Hinson
	 */
	public int getNumberOfTestDetailsHeld() {
		return null != testDetails ? testDetails.size() : 0;
	}

	/**
	 * Checks each of the associated TestDetails to determine if the questions
	 *  that are referenced are in sync with each other.
	 * 
	 * @return
	 * @author Trevor Hinson
	 */
	public boolean areAllReferencedQuestionsInSync() {
		boolean inSync = true;
		if (null != testDetails) {
			TestDetails first = null;
			x : for (TestDetails td : testDetails.values()) {
				if (null != td && inSync) {
					if (null == first) {
						 first = td;
					} else {
						if (first.getNumberOfQuestionsReferenced()
							!= td.getNumberOfQuestionsReferenced()) {
							inSync = false;
							break x;
						}
						if (first.getNumberOfQuestionsReferenced() > 0) {
							k : for (String questionName : first
								.getQuestionsReferenced().getNamesOfQuestions()) {
								if (!td.getQuestionsReferenced()
									.containsQuestionReference(questionName)) {
									inSync = false;
									break k;
								}
							}	
						}
					}
				} else{
					inSync = false;
					break x;
				}
			}
			if (null == first) {
				inSync = false;
			}
		}
		return inSync;
	}

	/**
	 * For use when invoked external to this class.  Provides an immutable
	 *  collection response so to not effect the underlying references.
	 * 
	 * @return
	 * @author Trevor Hinson
	 */
	public List<String> getNamesOfTestQuestionsReferenced() {
		List<String> referenced = new ArrayList<String>();
		if (null != testDetails) {
			for (TestDetails td : testDetails.values()) {
				if (null != td ? null != td.getQuestionsReferenced() : false) {
					List<String> names = td.getQuestionsReferenced()
						.getNamesOfQuestions();
					if (null != names ? names.size() > 0 : false) {
						referenced.addAll(names);
					}
				}
			}				
		}
		return Collections.unmodifiableList(referenced);
	}

	/**
	 * Overrides the existing location and sets it to false.
	 * 
	 * @param location
	 * @author Trevor Hinson
	 */
	public void foundAt(String location, File f, TestQuestionsReferenced refs) {
		if (null != location ? location.length() > 0 : false) {
			if (null == testDetails.get(location)) {
				TestDetails td = new TestDetails(f);
				td.setQuestionsReferenced(refs);
				testDetails.put(location, td);
			}
		}
	}

}
