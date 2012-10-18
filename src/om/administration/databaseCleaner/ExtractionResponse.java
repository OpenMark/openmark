package om.administration.databaseCleaner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import om.administration.extraction.ExtractDataHolder;

/**
 * Holds the details of the Extraction from the data store so that is can be
 *  used as neccessary after the processing has finished.
 * @author Trevor Hinson
 */

public class ExtractionResponse {

	private boolean extracted;

	private String responseMessage;

	private Map<String, List<ExtractDataHolder>> extractDataHolders;

	private Map<String, List<String>> insertStatements;

	/**
	 * Provides access to the composite Collection of insert statements
	 *  generated through the extraction process.
	 * @return
	 * @author Trevor Hinson
	 */
	public Map<String, List<String>> getInsertStatements() {
		if (null == insertStatements) {
			synchronized (this) {
				if (null == insertStatements) {
					insertStatements = new HashMap<String, List<String>>();
				}
			}
		}
		return insertStatements;
	}

	/**
	 * Adds an insert statement line to the composite insertStatements based on
	 *  the tableName provided.  This is done so that we can reuse and
	 *  manipulate this information at runtime at a later point (for example,
	 *  changing the "ti" and "qi" or primary keys for other database
	 *  consumption if need be.
	 * @param tableName
	 * @param insert
	 * @author Trevor Hinson
	 */
	public void addInsertStatement(String tableName, String insert) {
		if ((null != tableName ? tableName.length() > 0 : false) && null != insert) {
			List<String> statements = getInsertStatements().get(tableName);
			if (null == statements) {
				synchronized (this) {
					if (null == statements) {
						statements = new ArrayList<String>();
						getInsertStatements().put(tableName, statements);
					}
				}
			}
			statements.add(insert.toString());
		}
	}

	/**
	 * Returns access to the composite Collection maintained of the tables and
	 *  data composed within those tables for the particular extraction that has
	 *  been run.
	 * @return
	 * @author Trevor Hinson
	 */
	public Map<String, List<ExtractDataHolder>> getExtractDataHolders() {
		if (null == extractDataHolders) {
			synchronized (this) {
				if (null == extractDataHolders) {
					extractDataHolders = new HashMap<String, List<ExtractDataHolder>>();
				}
			}
		}
		return extractDataHolders;
	}

	/**
	 * Allows for the addition of an ExtractDataHolder for the specified table
	 *  to the composite Collection.
	 * @param tableName
	 * @param edh
	 */
	public void addExtractDataHolder(String tableName, ExtractDataHolder edh) {
		if ((null != tableName ? tableName.length() > 0 : false)
			&& null != edh) {
			List<ExtractDataHolder> holders = getExtractDataHolders().get(tableName);
			if (null == holders) {
				synchronized (this) {
					if (null == holders) {
						holders = new ArrayList<ExtractDataHolder>();
						extractDataHolders.put(tableName, holders);
					}
				}
			}
			holders.add(edh);
		}
	}

	/**
	 * Should return true at the end of a successful process.
	 * @return
	 * @author Trevor Hinson
	 */
	public boolean isExtracted() {
		return extracted;
	}

	
	public void setExtracted(boolean b) {
		extracted = b;
	}

	/**
	 * Provides access to the actual SQL data that has been generated through
	 *  the extraction process.
	 * @return
	 * @author Trevor Hinson
	 */
	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String s) {
		responseMessage = s;
	}
}
