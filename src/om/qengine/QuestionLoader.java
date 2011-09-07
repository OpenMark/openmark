package om.qengine;

import java.io.File;

import om.OmException;
import om.qengine.QuestionCache.QuestionStuff;
import om.question.Question;

/**
 * Derived from the QuestionCache in order to provide multiple ways of loading
 *  the Questions themselves into the "QuestionCache.QuestionStuff" so that
 *  alternative approaches can be made.
 * 
 * @author Trevor Hinson
 */

public interface QuestionLoader {

	/**
	 * Based on the File argument the xml document for the Question itself
	 *  should be loaded into the QuestionStuff argument for use later.
	 * 
	 * @param qs
	 * @param f
	 * @throws OmException
	 * @author Trevor Hinson
	 */
	void loadMetaData(QuestionStuff qs, File f) throws OmException;

	/**
	 * From the File argument the Class that handles the Question itself is
	 *  loaded into the QuestionStuff object.
	 * 
	 * @param qs
	 * @param f
	 * @throws OmException
	 * @author Trevor Hinson
	 */
	void loadClass(QuestionStuff qs, File f) throws OmException;

	Question load(QuestionStuff qs) throws OmException;

}
