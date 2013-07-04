package samples.multichoice.wordselect;

import om.*;
import samples.shared.PartialQuestion1;
import om.stdcomponent.WordSelectComponent;

public class WordSelect extends PartialQuestion1
{
	double scoreAttempt1 = 0;
	double scoreAttempt2 = 0;

	protected void init() throws OmException
	{
		this.setMaxAttempts(2);
		getResults().setQuestionLine("Select some words.");
	}

	public void actionOK() throws OmException
	{
		super.actionOK();
		getComponent("scrolldown").setDisplay(false);
	}

	protected double isRight(int iAttempt) throws OmDeveloperException
	{
		getComponent("scrolldown").setDisplay(true);
		getComponent("howmany").setDisplay(false);

		boolean wsCorrect = true;
		int swWords = 0;
		int totalSWWords = 0;
		int totalSelected = 0;

		for (int i = 0; i < 4; i++) {
			WordSelectComponent ws = (WordSelectComponent) getComponent("para_" + i);
			if (wsCorrect) wsCorrect = ws.getIsCorrect();
			swWords += ws.getTotalSWWordsSelected();
			totalSelected += ws.getTotalWordsSelected();
			totalSWWords += ws.getTotalSWWords();
		}
		String str = "words selected = " + totalSelected + " words correct = " + swWords +
					" out of the " + totalSWWords + " required.";
		getResults().setAnswerLine(str);
		getResults().appendActionSummary("Attempt " + iAttempt + ": " + str);

		//calculate score
		double dbl = 0.0;
		int extraselections = 0;
		if ((totalSelected - swWords) > 0) {
			extraselections = totalSelected - swWords;
		}

		if (iAttempt == 1){
			scoreAttempt1 = ((double)(swWords)/((double)(totalSWWords) + (double)(extraselections))) * 100.0;
			dbl = scoreAttempt1;
		}
		else if(iAttempt == 2){
			scoreAttempt2 = ((double)(swWords)/((double)(totalSWWords) + (double)(extraselections))) * 100.0;
			dbl = scoreAttempt1 + ((scoreAttempt2 - scoreAttempt1)/2);
		}

		setPlaceholder("PERCENT", "" + (int) dbl);

		if (wsCorrect){
			setQCompleted();
			if ((int) dbl != 100)
				showComponent("not100");
			return dbl;
		}

		setPlaceholder("SWWORDS", "" + swWords);
		setPlaceholder("TOTALSELECTED", "" + totalSelected);
		setPlaceholder("TOTALSWWORDS", "" + totalSWWords);

		if (iAttempt == 1) {
			getComponent("howmany").setDisplay(true);
			getComponent("feedback").setDisplay(true);
		}

		if (iAttempt == 2) {
			for(int i = 0; i < 4; i++){
				WordSelectComponent ws = (WordSelectComponent) getComponent("para_" + i);
				ws.highlightCorrectandUnselectedSWWords();
			}

			getComponent("showAns").setDisplay(true);
			if((totalSelected - swWords)> 0){
				setPlaceholder("TOTALINCORRECTSELECS", "" + (totalSelected - swWords));
				getComponent("extraSelections").setDisplay(true);
			}
			showComponent("not100");
		}
		return dbl;
	}
}
