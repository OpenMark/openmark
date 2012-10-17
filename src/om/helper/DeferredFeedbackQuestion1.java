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
package om.helper;

import om.OmDeveloperException;
import om.OmException;
import om.question.InitParams;
import om.question.Rendering;
import om.question.Results;
import om.stdcomponent.BoxComponent;
import om.stdquestion.QComponent;
import om.stdquestion.StandardQuestion;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
public abstract class DeferredFeedbackQuestion1 extends StandardQuestion
{
  /** If true, question ends when you click OK */
  private boolean bEndNext=false;

  /** ID used for feedback */
  private String sFeedbackID;

  /** Max marks, or 0 if no scoring */
  private int iMaxMarks=0;

  @Override
  public Rendering init(Document d,InitParams ip) throws OmException
	{
		Rendering r=super.init(d,ip);
		r.setProgressInfo("Not yet answered");

		try
		{
			Element eScoring=XML.getChild(d.getDocumentElement(),"scoring");
			iMaxMarks=Integer.parseInt(XML.getText(eScoring,"marks"));
		}
		catch(Exception e)
		{
			// Ignore, leave iMaxMarks at 0
		}

		return r;
	}

  /**
   * Callback that Om calls when the test attempt is finished. Calls checkAnswer
   * for further processing.
   * @throws OmException
   */
  public void finish() throws OmException
  {
    checkAnswer();
  }

  /**
   * Callback that Om calls when the test attempt is finished. Calls checkAnswer
   * for further processing.
   * @throws OmException
   */
  public void save() throws OmException
  {
    if (isComplete()) {
		setProgressInfo("Answer saved");
    } else {
		setProgressInfo("Not yet answered");
    }
  }

  /**
   * Handles the framework around checking the user's answer.
   * @throws OmDeveloperException
   */
	private void checkAnswer() throws OmDeveloperException
	{
		// Disable input, show answer
		getComponent("inputbox").setEnabled(false);
		getComponent("inputbox").setBoolean(BoxComponent.PROPERTY_PLAINHIDE,true);
		getComponent("answerbox").setDisplay(isFeedbackVisible());

		sFeedbackID=null;

		// Get the three basic states
		boolean bRight=isRight();

		// OK now show/hide the basic 'you were wrong' bit
		getComponent("wrong").setDisplay(!bRight);
		getComponent("right").setDisplay(bRight);

		// Now handle feedback
		QComponent qcFeedback=getComponent("feedback");
		if(!bRight)
		{
			qcFeedback.setDisplay(sFeedbackID!=null);
			if(sFeedbackID!=null && !sFeedbackID.equals(""))
			{
				QComponent[] aqc=qcFeedback.getComponentChildren();
				for(int i=0;i<aqc.length;i++)
				{
					if(aqc[i].hasUserSetID())
						aqc[i].setDisplay(aqc[i].getID().equals(sFeedbackID));
				}
			}
		}
		else qcFeedback.setDisplay(false);

		// Should we end next time?
		bEndNext=true;

		// If so, show answer and update score
		getComponent("answer").setDisplay(true);
		if(bEndNext)
		{
			setScore(bRight);
			sendResults();
		}

		// Give overriders a chance
		doAdditionalAnswerProcessing(bRight);

		// Clear progress info (looks bad esp. in plain mode)
		setProgressInfo("");
	}

	/**
	 * Override this method to check whether all parts of the question have been
	 * completed by the student. This is used to set the progress info to either
	 * "Not yet answered" or "Answer saved".
	 * @return true if the user has finished this question; false if there are
	 *      still some blanks to complete.
	 * @throws OmDeveloperException
	 */
  protected abstract boolean isComplete() throws OmDeveloperException;

	/**
	 * Override this method to check whether the user's answer is correct or not.
	 * <p>
	 * This is a simple right/wrong flag. To provide feedback as to how they were
	 * wrong, you should call setFeedbackID() [probably only for wrong answers
	 * and certain attempts].
	 * @return True if the user is right; false if they are wrong.
	 * @throws OmDeveloperException
	 */
  protected abstract boolean isRight() throws OmDeveloperException;

  /**
   * Override this method to change the scoring. The method should call
   * getResults().setScore.
   * <p>
   * Default scoring is out of 3 points: 3 for correct answer first time,
   * 2 second time, 1 third time, 0 for fail or pass.
   * @param bRight True if user was right (rather than passing/failing)
   * @param bPass True if user passed on question
   * @param iAttempt Attempt number (1 = first attempt)
   */
  protected void setScore(boolean bRight) throws OmDeveloperException
  {
  	if(iMaxMarks==0) throw new OmDeveloperException(
  			"Cannot set score on question: <scoring> not defined in question.xml");
  	if (bRight) getResults().setScore(iMaxMarks, 1);
  	else getResults().setScore(0, Results.ATTEMPTS_WRONG);
  }

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
  protected void setFeedbackID(String sID)
  {
  	sFeedbackID=sID;
  }

  /**
   * Override this method if you want to do any additional processing with
   * the user's answer. This is called after all default processing.
   * @param bRight True if user was right
   * @param bWrong True if they were wrong
   * @param bPass True if they passed
   * @param attempt Attempt number (1 is first attempt)
   * @throws OmDeveloperException
   */
	protected void doAdditionalAnswerProcessing(boolean bRight) throws OmDeveloperException
	{
		// Default does nothing
	}
}
