package om.administration.questionbank;

import java.util.Map;

import om.abstractservlet.RequestAssociates;

/**
 * Used to remove orphaned questions from the questionbank(s) as a means of
 *  tidying.
 * @author Trevor Hinson
 */

public interface CleanQuestionBanks {

	/**
	 * Runs over the configured testbanks to identify questions within the
	 *  configured questionbanks that are superfluous to requirements (old or 
	 *  not linked to a test).
	 * 
	 * @param locations
	 * @param ra
	 * @return
	 * @throws CleaningException
	 */
	ClearanceResponse identify(QuestionAndTestBankLocations locations,
		RequestAssociates ra) throws CleaningException;

	/**
	 * Take the user selected Question names and tries to remove them from the
	 *  configured locations which should be stored within each of the
	 *  IdentifiedSuperfluousQuestion by this time held in the 
	 *  originating ClearanceResponse from the invocation of the identify
	 *  method which needs to take place before this method is called.
	 * @param cr
	 * @param selected
	 * @return
	 * @throws CleaningException
	 * @author Trevor Hinson
	 */
	ClearanceResponse clearSelected(ClearanceResponse cr,
		Map<String, String> params) throws CleaningException;

	/**
	 * In the event of a mistaken clean of a question the implementation of this
	 *  method will revert the action and place the question back into the
	 *  original place.
	 * @param cr
	 * @param params
	 * @return
	 * @throws CleaningException
	 * @author Trevor Hinson
	 */
	ClearanceResponse undo(ClearanceResponse cr, Map<String, String> params)
		throws CleaningException;
}
