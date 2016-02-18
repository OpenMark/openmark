package om.tnavigator.teststructure;

import java.io.IOException;
import java.rmi.RemoteException;

import om.OmException;
import om.axis.qengine.Score;
import om.tnavigator.NavigatorServlet.RequestTimings;
import util.misc.QuestionName;
import util.misc.QuestionVersion;


/**
 * Interface that defines the methods for getting meta data about questions.
 * Implemented by both NavigatorServlet and QuestionMetadataSource.
 */
public interface QuestionMetadataSource
{
	/**
	 * Find the latest version of the question with a given id, and optionally
	 * major version.
	 * @param sQuestionID Question ID
	 * @param iRequiredVersion Required major version, or TestQuestion.VERSION_UNSPECIFIED
	 * @return appropriate version
	 * @throws OmException
	 */
	QuestionVersion getLatestVersion(String sQuestion, int iMajor) throws OmException;

	/**
	 * Get the maximum score for each axis from this question.
	 * @param rt used to record the time taken.
	 * @param questionName identifies the question and version.
	 * @return maximum score for each axis.
	 * @throws IOException
	 * @throws RemoteException
	 */
	Score[] getMaximumScores(RequestTimings rt, QuestionName questionName) throws RemoteException, IOException;
}
