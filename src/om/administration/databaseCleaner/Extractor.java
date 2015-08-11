package om.administration.databaseCleaner;

import java.util.Map;

import om.administration.extraction.ExtractorException;

public interface Extractor {

	/**
	 * For accomodating for all requests for the extraction of student data from
	 *  a source so that it may be represented within another source (typically
	 *  for debugging purposes in the event of an issue).
	 * @param metaData
	 * @return
	 * @throws ExtractorException
	 * @author Trevor Hinson
	 */
	ExtractionResponse extract(Map<String, Object> metaData)
		throws ExtractorException;

}
