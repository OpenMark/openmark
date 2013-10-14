package samples.shared;

import org.w3c.dom.*;

import om.*;
import om.question.*;
import om.stdcomponent.BoxComponent;
import om.stdquestion.*;
import util.xml.XML;

/** 
 * Base class for simple questions that have an input box and an answer box 
 * (simultaneously visible) and match certain other 'standard' requirements 
 * in their XML. 
 * <p> 
 * Specifically, the XML must contain the following component IDs:
 * <ul>
 * <li>inputbox - box that contains controls into which user enters their
 *   answers</li>
 * <li>answerbox - box that contains information about the answer, e.g. was
 *   it right or not</li>
 * <li>wrong - component within answerbox, shown only if answer is wrong</li>
 * <li>still - component within answerbox, shown only if answer is wrong and
 *   it's not the first time</li>
 * <li>right - component within answerbox, shown only if answer is right</li>
 * <li>feedback - component within answerbox, shown only if getFeedbackID()
 *   returns something other than null.
 * </ul>
 * There should be examples matching this pattern in the documentation. 
 */
public abstract class PartialQuestion1 extends StandardQuestion
{
	/** Which question attempt the user is on */
	private int iAttempt = 1;
  
	/** Maximum number of attempts allowed (after that it tells you the answer) */
	private int iMaxAttempts = 3;
  
	/** If true, question ends when you click OK */
	private boolean bEndNext = false;
  
	/** If true, question has been completed prompting 'your answer is correct'
	 *  Necessary because partial scores can mean
	 *   - not fully correct have another go
	 *   - and fully correct but not at first attempt (& so < 100%)
	 **/
	private boolean qCompleted = false;
  
//	/** ID used for feedback */
//	private String sFeedbackID;
  
	/** Max marks, or 0 if no scoring */
	private int iMaxMarks = 0;
  
	///////////////////////////////////////////////////////////////////////////

	public Rendering init(Document d,InitParams ip) throws OmException
	{
		Rendering r=super.init(d,ip);
		if (iMaxAttempts == 1)
			r.setProgressInfo("You have only one attempt.");
		else
			r.setProgressInfo("You have "+iMaxAttempts+" attempts.");
		
		try {
			Element eScoring=XML.getChild(d.getDocumentElement(),"scoring");
			iMaxMarks = Integer.parseInt(XML.getText(eScoring,"marks"));
		}
		catch(Exception e) {
			// Ignore, leave iMaxMarks at 0			
		}
		
		return r;
	}
	///////////////////////////////////////////////////////////////////////////
  
	/**
	 * Set the maximum number of attempts permitted. After that it will tell you
	 * the answer. The default is 3.
	 * @param iMaxAttempts Number of permitted attempts
	 */
	protected void setMaxAttempts(int iMaxAttempts)
	{
		this.iMaxAttempts = iMaxAttempts;
	}
  
	///////////////////////////////////////////////////////////////////////////
  
	/**
	 * Callback that Om calls when the user clicks the 'Submit' button to enter
	 * their answer. Calls checkAnswer for further processing.
	 * @throws OmException
	 */
	public void actionSubmit() throws OmException
	{
		checkAnswer();
	}
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Callback that Om calls when the user clicks the 'OK' button after seeing
	 * the response to their answer. This either ends the question, or hides
	 * the answer box and re-enables the input box for another attempt.
	 * @throws OmException
	 */
	public void actionOK() throws OmException
	{
		if (bEndNext) 
			end();
		else {
			int iAttemptsLeft = (iMaxAttempts-iAttempt+1);
			if (iAttemptsLeft == 1)
				setProgressInfo("This is your last %%lTRY%%.");
			else
				setProgressInfo("You have "+iAttemptsLeft+" %%lTRIES%% left.");
  		
			getComponent("answerbox").setDisplay(false);
			getComponent("inputbox").setBoolean(BoxComponent.PROPERTY_PLAINHIDE,false);
			getComponent("inputbox").setEnabled(true);
		}
	}
	///////////////////////////////////////////////////////////////////////////

	/**
     * Hide all components where the id is defined by the user, recursively
     */
    private void hideAllQComponents(QComponent qc) throws OmDeveloperException
    {
        if (qc == null)
            return;
//        if (qc.hasUserSetID()) {
//            qc.setDisplay(false);
            QComponent[] qcs = qc.getComponentChildren();
            for (int i = 0; i < qcs.length; i++) {
//                hideAllQComponents(qcs [i]);
            	if (qcs[i].hasUserSetID())
            		qcs[i].setDisplay(false);
            }
//        }
    }
	///////////////////////////////////////////////////////////////////////////
    
	/**
     * Show the specified component
     */
    public void showComponent(String qcid) throws OmDeveloperException
    {
		getComponent(qcid).setDisplay(true);
    }
	///////////////////////////////////////////////////////////////////////////
    
	/**
     * Show the specified component
     */
    public void setQCompleted() throws OmDeveloperException
    {
		qCompleted = true;
    }
	///////////////////////////////////////////////////////////////////////////
	/**
	 * Handles the framework around checking the user's answer.
	 * @throws OmDeveloperException
	 */
	private void checkAnswer() throws OmDeveloperException
	{
		boolean bRight = false, bWrong = false;
		double	percentRight = 0;

		// Disable input, show answer
		getComponent("inputbox").setEnabled(false);
		getComponent("inputbox").setBoolean(BoxComponent.PROPERTY_PLAINHIDE,true);
		getComponent("answerbox").setDisplay(true);

		// hide all components that are within the answerboxtext component
		try {
            hideAllQComponents(getComponent("answerboxtext"));
        } catch (OmDeveloperException omde) {};

        // now turn on the components required for this feedback or answer
        percentRight = isRight(iAttempt); 
		
        if (qCompleted) {
        	bRight = true; 	// note that this means right at 1st, 2nd or 3rd
        					// attempt and need not correspond to a score
        					// of 100
			showComponent("right");
        }
        else if ((int) percentRight == 0) {
			bWrong = true;
			showComponent("wrong");
			getComponent("still").setDisplay(bWrong && (iAttempt > 1));
        }
		else {
			// bPartial = true; //would be set if it were needed - but it isn't
			showComponent("partial");
		}
		
		// Should we end next time?
		bEndNext = (bRight || (iAttempt == iMaxAttempts));
				
		// If so, show answer and update score
		getComponent("answer").setDisplay(bEndNext);
		if (bEndNext) {
			setScore(percentRight);
			sendResults();
			try {
				getComponent("ok").setDisplay(false);
				getComponent("next").setDisplay(true);
			}
			catch(OmDeveloperException ode)
			{
				// Ignore, just means question.xml hasn't been updated
				log("Warning: question.xml should be updated to make OK and Next buttons work");
			}
		}

		// Give overriders a chance
		doAdditionalAnswerProcessing(bRight, bWrong, iAttempt);
		
		// Increment feedback level
		iAttempt++;

		// Clear progress info (looks bad esp. in plain mode)
		setProgressInfo("");
	}

	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Override this method to check whether the user's answer is correct or not.
	 * <p>
	 * This is a simple right/wrong flag. To provide feedback as to how they were
	 * wrong, you should call setFeedbackID() [probably only for wrong answers
	 * and certain attempts]. 
	 * @param iAttempt Question attempt (1 for first, 2 for second, etc.)
	 * @return True if the user is right; false if they are wrong.
	 * @throws OmDeveloperException
	 */
	protected abstract double isRight(int iAttempt) throws OmDeveloperException;
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Override this method to change the scoring. The method should call
	 * getResults().setScore.
	 * <p>
	 * Default scoring is a percentage of maximum score - rules are determined by questions.  
	 * @param bRight True if user was right (rather than passing/failing)
	 * @param iAttempt Attempt number (1 = first attempt)
	 */

	protected void setScore(double percentRight) throws OmDeveloperException
	{
  	if (iMaxMarks == 0) throw new OmDeveloperException(
  		"Cannot set score on question: <scoring> not defined in question.xml");
  	
  		getResults().setScore((int)(percentRight * (double)iMaxMarks / 100.0), iAttempt);
	}
	///////////////////////////////////////////////////////////////////////////
  
	/**
	 * Sets the feedback ID for current question attempt. Should be called from 
	 * within isRight(). 
	 * <p>
	 * Feedback works as follows:
	 * <ul>
	 * <li>You must have a component with id 'feedback'. This component will
	 *   be hidden if this method returns null, and shown otherwise.</li>
	 * <li>If you return "" then no further action is taken. Otherwise you must
	 *   have multiple components with IDs, directly inside the 'feedback' 
	 *   component. These will all be hidden except the one named by the
	 *   return value. Components which don't have IDs will be unaffected.</li> 
	 * </ul>
	 * @param sID ID of component to use for feedback. May be null if there is 
	 *   no feedback this time, or an empty string to display just the 'feedback' 
	 *   component without messing with its children 
	 */

//	protected void setFeedbackID(String sID)
//	{
//		sFeedbackID=sID;
//	}
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Override this method if you want to do any additional processing with
	 * the user's answer. This is called after all default processing.
	 * @param bRight True if user was right
	 * @param bWrong True if they were wrong
	 * @param iAttempt Attempt number (1 is first attempt)
	 * @throws OmDeveloperException
	 */

	protected void doAdditionalAnswerProcessing(
		boolean bRight, boolean bWrong, int iAttempt) throws OmDeveloperException
	{
		// Default does nothing
	}    
}
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
