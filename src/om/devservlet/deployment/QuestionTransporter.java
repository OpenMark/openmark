package om.devservlet.deployment;

import java.util.Map;

import om.abstractservlet.RenderedOutput;
import util.misc.GracefulFinalization;

/**
 * Used in order to move actual Questions from the Question Developers local
 *  machine to the configured locations within the qengine.xml
 * @author Trevor Hinson
 */

public interface QuestionTransporter extends GracefulFinalization {

	/**
	 * The implementation should take the Question and try to copy it to all
	 *  of the locations specified within the metaData.
	 * @param qth
	 * @param metaData
	 * @param or The OutputRendering containing information about the process
	 *  that is used to report back to the user.
	 * @throws QuestionTransporterException
	 * @author Trevor Hinson
	 */
	void deploy(QuestionHolder qth, Map<String, String> metaData,
		RenderedOutput or) throws QuestionTransporterException;

}
