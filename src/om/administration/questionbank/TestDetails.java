package om.administration.questionbank;

import java.io.File;

public class TestDetails {

	private File testDefinition;
	
	private TestQuestionsReferenced questionsReferenced;
	
	private boolean isAssessed;

	public TestDetails(File test) {
		testDefinition = test;
	}

	public String getTestDefinitionName() {
		return null != testDefinition ? testDefinition.getName() : null;
	}

	public File getTestDefinition() {
		return testDefinition;
	}

	public TestQuestionsReferenced getQuestionsReferenced() {
		return questionsReferenced;
	}

	public void setQuestionsReferenced(TestQuestionsReferenced qr) {
		questionsReferenced = qr;
	}

	public int getNumberOfQuestionsReferenced() {
		return null != questionsReferenced
			? questionsReferenced.getNumberOfQuestionsHeld() : 0;
	}
	
	public boolean getIsAssessed(){
		return isAssessed;
	}
	
	public void setIsAssessed(boolean ia)
	{
		this.isAssessed=ia;
	}
	
}
