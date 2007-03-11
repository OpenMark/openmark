/* OpenMark online assessment system
   Copyright (C) 2007 The Open University

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package samples.mu120.lib;

import org.w3c.dom.Document;

import om.*;
import om.question.*;
import om.question.Results;
import om.stdcomponent.BoxComponent;
import om.stdquestion.*;

/**
 * Base class for simple questions that have an input box and an answer box
 * (simultaneously visible) and match certain other 'standard' requirements in
 * their XML.
 * <p>
 * Specifically, the XML must contain the following component IDs:
 * <ul>
 * <li>inputbox - box that contains controls into which user enters their
 * answers</li>
 * <li>answerbox - box that contains information about the answer, e.g. was it
 * right or not</li>
 * <li>wrong - component within answerbox, shown only if answer is wrong</li>
 * <li>still - component within answerbox, shown only if answer is wrong and
 * it's not the first time</li>
 * <li>right - component within answerbox, shown only if answer is right</li>
 * <li>pass - component within answerbox, shown only if user passed</li>
 * <li>hint - component within answerbox, shown only if user solicited hint</li>
 * <li>feedback - component within answerbox, shown only if getFeedbackID()
 * returns something other than null.
 * </ul>
 * There should be examples matching this pattern in the documentation.
 */
public abstract class SimpleQuestion2 extends StandardQuestion {
    /** Which question attempt the user is on */
    private int iAttempt = 1;
    
    /** Maximum number of attempts allowed (after that it tells you the answer) */
    private int iMaxAttempts = 3;
    
    /** true once you get a wrong answer, to cater for the 'hint, wrong, ...' sequence */
    private boolean bPreviouslyWrong = false;
    
    /** If true, question ends when you click OK */
    private boolean bEndNext = false;
    
    public Rendering init(Document d, InitParams ip) throws OmException {
        Rendering r = super.init(d, ip);
        if (iMaxAttempts == 1)
            r.setProgressInfo("You have only one attempt.");
        else
            r.setProgressInfo("You have " + iMaxAttempts + " attempts.");
        return r;
    }
    
    /**
     * Set the maximum number of attempts permitted. After that it will tell you
     * the answer. The default is 3.
     *
     * @param iMaxAttempts Number of permitted attempts
     */
    protected void setMaxAttempts(int iMaxAttempts) {
        this.iMaxAttempts = iMaxAttempts;
    }
    
    /**
     * Callback that Om calls when the user clicks the 'Submit' button to enter
     * their answer. Calls checkAnswer for further processing.
     *
     * @throws OmException
     */
    public void actionSubmit() throws OmException {
        checkAnswer(false, false);
    }
    
    /**
     * Callback that Om calls when the user clicks the 'Give Up' button. Calls
     * checkAnswer for further processing.
     *
     * @throws OmException
     */
    public void actionGiveUp() throws OmDeveloperException {
        getResults().appendActionSummary("Attempt " + iAttempt + ": " + "Passed");
        checkAnswer(true, false);
    }
    
    /**
     * Callback that Om calls when the user clicks the 'Hint' button.
     * Treat 'Hint' as a wrong answer for feedback and scoring.
     *
     * @throws OmDeveloperException
     */
    public void actionHint() throws OmDeveloperException {
        getResults().appendActionSummary("Attempt " + iAttempt + ": " + "Hint");
        checkAnswer(false, true);
    }
    
    /**
     * Callback that Om calls when the user clicks the 'OK' button after seeing
     * the response to their answer. This either ends the question, or hides the
     * answer box and re-enables the input box for another attempt.
     *
     * @throws OmException
     */
    public void actionOK() throws OmException {
        if (bEndNext)
            end();
        else {
            int iAttemptsLeft = (iMaxAttempts - iAttempt + 1);
            if (iAttemptsLeft == 1)
                setProgressInfo("This is your last attempt.");
            else
                setProgressInfo("You have " + iAttemptsLeft + " attempts left.");
            
            getComponent("answerbox").setDisplay(false);
            getComponent("inputbox").setBoolean(
                    BoxComponent.PROPERTY_PLAINHIDE, false);
            getComponent("inputbox").setEnabled(true);
        }
    }
    
    /**
     * Hide all components where the id is defined by the user, recursively
     */
    private void hideAllQComponents(QComponent qc)
    throws OmDeveloperException {
        if (qc == null)
            return;
        if (qc.hasUserSetID()) {
            qc.setDisplay(false);
            QComponent [] qcs = qc.getComponentChildren();
            for (int i = 0; i < qcs.length; i++) {
                hideAllQComponents(qcs [i]);
            }
        }
    }
    
    /**
     * Handles the framework around checking the user's answer.
     *
     * @param bPass
     *            True if the user passed rather than submitting an answer
     * @throws OmDeveloperException
     */
    private void checkAnswer(boolean bPass, boolean bHint)
    throws OmDeveloperException {
        
        // Disable input, show answer
        getComponent("inputbox").setEnabled(false);
        getComponent("inputbox").setBoolean(BoxComponent.PROPERTY_PLAINHIDE,true);
        getComponent("answerbox").setDisplay(true);
        
        //sFeedbackID = null;
        
        // hide all hints and feedback, they may not be there!
        try {
            hideAllQComponents(getComponent("hints"));
        } catch (OmDeveloperException omde) {};
        try {
            hideAllQComponents(getComponent("feedback"));
        } catch (OmDeveloperException omde) {};
        try {
            hideAllQComponents(getComponent("answer"));
        } catch (OmDeveloperException omde) {};
        
        // Get the four basic states
        boolean bRight = false;
        if (! bPass && ! bHint) {
            bRight = isRight(iAttempt);
        }
        boolean bWrong = ! bRight && ! bPass && ! bHint;
        
        // OK now show/hide the basic 'you were wrong' bit
        getComponent("wrong").setDisplay(bWrong);
        getComponent("still").setDisplay(bWrong && bPreviouslyWrong);
        bPreviouslyWrong = bPreviouslyWrong || bWrong;
        getComponent("right").setDisplay(bRight);
        getComponent("pass").setDisplay(bPass);
        
        // Optionally provide a hint out of the feedback block
        if (bHint) {
            provideHint(iAttempt);
        }
        
        // Should we end next time?
        bEndNext = bRight || iAttempt == iMaxAttempts || bPass;
        
        // If so, show answer, a possible reference, and update score
        getComponent("hints").setDisplay(! bEndNext);
        getComponent("feedback").setDisplay(! bEndNext && ! bHint);
        getComponent("answer").setDisplay(bEndNext);
        getComponent("reference").setDisplay(bWrong && bEndNext);
        if (bEndNext) {
            setScore(bRight, bPass, iAttempt);
            sendResults();
            try {
                getComponent("ok").setDisplay(false);
                getComponent("next").setDisplay(true);
            } catch (OmDeveloperException ode) {
                // Ignore, just means question.xml hasn't been update
                log("Warning: question.xml should be updated to make OK and Next buttons work");
            }
        }
        
        // Give overriders a chance
        doAdditionalAnswerProcessing(bRight, bWrong, bPass, bHint, iAttempt);
        
        // Increment feedback level
        iAttempt++;
        
        // hide hintButton, if its there, for the final attempt
        try {
            if (iAttempt == iMaxAttempts)
                getComponent("hintButton").setDisplay(false);
        } catch (OmDeveloperException omde) {};
        
        // Clear progress info (looks bad esp. in plain mode)
        setProgressInfo("");
    }
    
    /**
     * Override this method to check whether the user's answer
     * is correct or not.
     * <p>
     * This is a simple right/wrong flag.
     * To provide feedback as to how they were wrong,
     * you should call setFeedbackID()
     * [probably only for wrong answers and certain attempts].
     *
     * @param iAttempt
     *            Question attempt (1 for first, 2 for second, etc.)
     * @return True if the user is right; false if they are wrong.
     * @throws OmDeveloperException
     */
    protected abstract boolean isRight(int iAttempt)
    throws OmDeveloperException;
    
    /**
     * Method to provide hints.
     * There can be a single hint called 'hint'
     * that displays on pressing the hint button on either the first or second attempt.
     * This hint is displayed once only on either the first or second attempt.
     * Alternatively two hints can be provided called 'hint1' and 'hint2'
     * that can be displayed on the first or the second attempt.
     *
     * @param iAttempt
     *            Attempt number (1 = first attempt)
     */
    protected void provideHint(int iAttempt)
    throws OmDeveloperException {
        try {
            setFeedbackID("hint");
            getComponent("hintButton").setDisplay(false); // once you see it that's it!
        } catch (OmDeveloperException omde) {};
        switch (iAttempt) {
            case 1:
                try {
                    setFeedbackID("hint1");
                } catch (OmDeveloperException omde) {};
                break;
            case 2:
                try {
                    setFeedbackID("hint2");
                } catch (OmDeveloperException omde) {};
                break;
            case 3:
                break;
        }
    }
    
    /**
     * Override this method to change the scoring.
     * The method should call getResults().setScore.
     * <p>
     * Default scoring is out of 3 points:
     * 3 for correct answer first time,
     * 2 second time,
     * 1 third time,
     * 0 for fail or pass.
     *
     * @param bRight
     *            True if user was right (rather than passing/failing)
     * @param bPass
     *            True if user passed on question
     * @param iAttempt
     *            Attempt number (1 = first attempt)
     */
    protected void setScore(boolean bRight, boolean bPass, int iAttempt)
    throws OmDeveloperException {
        if (! bRight)
            getResults().setScore(0, bPass ? Results.ATTEMPTS_PASS : Results.ATTEMPTS_WRONG);
        else
            getResults().setScore(4 - iAttempt, iAttempt);
    }
    
    /**
     * This is quite different from SimpleQuestion1.
     * We simply switch on a message for display, i.e. feedback.
     * Initially all feedback is switched off.
     * Hints are also stored within the feedback XML block.
     * @param sID
     *            ID for the QComponent message
     */
    protected void setFeedbackID(String sID) throws OmDeveloperException {
        QComponent qc = getComponent(sID);
        if (qc != null)
            qc.setDisplay(true);
    }
    
    /**
     * Override this method if you want to do any additional processing with the
     * user's answer. This is called after all default processing.
     *
     * @param bRight
     *            True if user was right
     * @param bWrong
     *            True if they were wrong
     * @param bPass
     *            True if they passed
     * @param bHint
     *            True if hint solicited
     * @param iAttempt
     *            Attempt number (1 is first attempt)
     * @throws OmDeveloperException
     */
    protected void doAdditionalAnswerProcessing(boolean bRight, boolean bWrong,
            boolean bPass, boolean bHint, int iAttempt)
            throws OmDeveloperException {
        // Default does nothing
    }
}
