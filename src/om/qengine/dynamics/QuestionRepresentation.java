package om.qengine.dynamics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import om.OmException;

public class QuestionRepresentation {

	private static List<String> allowed = new ArrayList<String>();

	private String representation;

	private String fullClassName;

	static {
		allowed.add("implements Question");
		allowed.add("extends StandardQuestion");
		allowed.add("extends om.stdquestion.StandardQuestion");
		allowed.add("extends DeferredFeedbackQuestion1");
		allowed.add("extends om.helper.DeferredFeedbackQuestion1");
		allowed.add("extends SimpleQuestion1");
		allowed.add("extends om.helper.SimpleQuestion1");
		allowed.add("extends SimpleQuestion20ForIAT");
		allowed.add("extends om.helper.SimpleQuestion20ForIAT");
		allowed.add("extends SimpleQuestion3");
		allowed.add("extends om.helper.SimpleQuestion3");
		Collections.unmodifiableList(allowed);
	}

	public String getFullClassName() throws OmException {
		if (!validFullClassName()) {
			throw new OmException("The fullClassName was not valid : "
				+ fullClassName);
		}
		return fullClassName;
	}

	private boolean validFullClassName() {
		return null != fullClassName ? fullClassName.length() > 0 : false;
	}

	public void setFullClassName(String s) {
		fullClassName = s;
	}

	public String getRepresentation() throws OmException {
		if (!simpleValidation()) {
			throw new OmException("The composite representation of a"
				+ " Question Java class is not valid : " + representation);
		}
		return representation;
	}

	private boolean simpleValidation() {
		boolean valid = false;
		if (null != representation ? representation.length() > 0 : false) {
			x : for (String test : allowed) {
				if (null != test ? test.length() > 0 : false) {
					if (representation.contains(test)) {
						valid = true;
						break x;
					}
				}
			}
		}
		return valid;
	}

	public boolean isValid() {
		return validFullClassName();
	}

	public QuestionRepresentation(String rep, String fullName) {
		representation = rep;
		fullClassName = fullName;
	}

}
