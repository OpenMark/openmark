package om.administration.databaseCleaner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import om.abstractservlet.GracefulFinalization;
import om.abstractservlet.StandardFinalizedResponse;
import om.administration.extraction.ExtractorException;
import om.tnavigator.AbstractPersistenceDelegator;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.db.DatabaseAccess;
import util.misc.FinalizedResponse;
import util.misc.GeneralUtils;
import util.misc.Strings;
import util.misc.UtilityException;

/**
 * Handles the extraction of the students details from the underlying
 *  persistence mechanism in Openmark so that their details can be reused 
 *  within another environment (at this time for testing if they have problems)
 *  Equally can be reused for other purposes in the future.
 * <br /><br />
 * At this time the required tables are statically held within this class. 
 * 	You can however override these tables by specifying the them within the
 *  Map<String, Object> metaData passed to the 
 *  handleRequest(Map<String, Object> metaData) method of this class.
 * <br /><br />
 * The implementation itself is "in memory".  This means the data is processed,
 *  held in memory and then output to the neccessary file(s).  This approach
 *  was taken at this time as the underlying amount of information held for
 *  a test instance is not particularly large.  In the future, should this
 *  change, then the implementation will need to be addapted accordingly.
 *  
 * @author Trevor Hinson
 */

public class ReportDetailsGenerator extends AbstractPersistenceDelegator
	implements Extractor, GracefulFinalization {

	private static String LINE_SEPERATOR = "line.separator";

	private static String COMMA = ",";

	private static String DAT_SUFFIX = ".dat";

	private static String CSV_SUFFIX = ".csv";

	private static Integer SIZE = new Integer(999999);

	private static String QI_TABLE = "nav_questions";

	private static String TEST_INSTANCE_SQL = "select * from {0} where ti={1}";

	private static String QUESTION_INSTANCE_SQL = "select * from {0} where qi={1}";

	private static String INSERT_INTO = "INSERT INTO";

	private static String VALUES = ") VALUES (";

	private static String OPEN = "(";

	private static String CLOSE = ")";
	
	private static String COL_TABLE_NAME = "table name";

	public static final String FILENAME_DATE_FORMAT_NOW = ".yyyyMMdd.HHmmss";

	private static List<String> tableNamesByTestInstance = new ArrayList<String>();

	private static List<String> tableNamesByQuestionInstance = new ArrayList<String>();

	private String lineSeperator;

	/**
	 * These are the standard Openmark database tables which are required in the
	 *  extraction of a students details process.  Note that there are two types
	 *  of tables.  Those that contain a ti and those that contain a "qi".
	 */
	static {
		tableNamesByTestInstance.add("nav_tests");
		tableNamesByTestInstance.add("nav_testquestions");
		tableNamesByTestInstance.add("nav_infopages");
		tableNamesByTestInstance.add("nav_questions");
		tableNamesByTestInstance.add("nav_sessioninfo");
		tableNamesByQuestionInstance.add("nav_actions");
		tableNamesByQuestionInstance.add("nav_params");
		tableNamesByQuestionInstance.add("nav_results");
		tableNamesByQuestionInstance.add("nav_scores");
		tableNamesByQuestionInstance.add("nav_customresults");
	}

	/**
	 * Here we check that everything is ok with the request itself and the
	 *  metaData provided and then start processing the request by running over
	 *  the configured tables and extracting the Students details.
	 * @author Trevor Hinson
	 */
	@Override
	public ExtractionResponse extract(Map<String, Object> metaData)
		throws ExtractorException {
		ExtractionResponse er = new ExtractionResponse();
		if (initialise(metaData)) {
			StringBuilder output = new StringBuilder();
			//TODO slw2
			String ti = "";
			if (validateStudentTestInstance(ti)) {
				InMemoryRepresentation imr = new InMemoryRepresentation();
				QuestionInstanceIdentification qii = renderFromTestInstanceTables(imr,
					ti, output, er);
				if (null != qii) {
					renderFromQuestionInstanceTables(imr, output, qii, er);
					String fileNamePrefix = retrieve(metaData,
						ExtractorEnums.extractionFileNamePrefix.toString());
					//put a timestamp on it
					String nowTime=GeneralUtils.timeNow(FILENAME_DATE_FORMAT_NOW);
					fileNamePrefix=fileNamePrefix+nowTime;
					writeToFile(fileNamePrefix, imr, output);
					er.setResponseMessage(renderResultPage(fileNamePrefix));
					er.setExtracted(true);
				} else {
					throw new ExtractorException("Unable to determine the"
						+ " QuestionInstance from the TestInstance tables.");
				}
			} else {
				throw new ExtractorException("The Test Instance identifier"
					+ " specified is invalid.");
			}
		} else {
			throw new ExtractorException("Unable to carry out the extraction as the"
				+ " metaData was invalid : " + metaData);
		}
		return er;
	}

	protected void writeToFile(String fileNamePrefix,
		InMemoryRepresentation imr, StringBuilder output)
		throws ExtractorException {
		OutputRendering or = new OutputRendering();
		//writeToFile(fileNamePrefix + ".li", output);
		writeToFile(fileNamePrefix + CSV_SUFFIX, or.renderToCSVOutput(imr));
		writeToFile(fileNamePrefix + DAT_SUFFIX, or.renderToDataOutput(imr));
	}

	private class OutputRendering {

		StringBuilder renderToDataOutput(InMemoryRepresentation imr) {
			StringBuilder sb = new StringBuilder();
			if (null != imr) {
				for (TableDetails td : imr.getTableDetails()) {
					if (null != td) {
						StringBuilder columnNames = new StringBuilder();
						for (Iterator<String> i = td.getColumns().iterator(); i.hasNext();) {
							columnNames.append(i.next());
							if (i.hasNext()) {
								columnNames.append(COMMA);
							}
						}
						for (List<ExtractDataHolder> edhs : td.getData()) {
							if (null != edhs) {
								StringBuilder insert = new StringBuilder(INSERT_INTO)
									.append(td.getTableName()).append(OPEN)
									.append(columnNames).append(VALUES);
								StringBuilder values = new StringBuilder();
								for (Iterator<ExtractDataHolder> edh = edhs.iterator(); edh.hasNext();) {
									ExtractDataHolder holder = edh.next();
									values.append(holder.getSqlRenderedValue());
									if (edh.hasNext()) {
										values.append(COMMA);
									}
								}
								insert.append(values).append(CLOSE);
								sb.append(insert).append(" ").append(getLineSeperator())
									.append(getLineSeperator());
							}
						}
						//addInsert(insert, er, td.getTableName());
					}
				}
			}
			return sb;
		}

		StringBuilder renderToCSVOutput(InMemoryRepresentation imr) {
			StringBuilder sb = new StringBuilder();
			if (null != imr) {
				List<TableDetails> tables = imr.getTableDetails();
				if (null != tables ? tables.size() > 0 : false) {
					for (TableDetails td : tables) {
						if (null != td) {
							sb.append(renderCSVOutput(td))
								.append(getLineSeperator())
								.append(getLineSeperator());
						}
					}
				}
			}
			return sb;
		}
		
		// Refactor into a TableDetailsRenderer ...
		String renderCSVOutput(TableDetails td) {
			StringBuffer sb = new StringBuffer();
			//sb.append(td.getTableName())
			//	.append(getLineSeperator()).append(getLineSeperator())
				sb.append(renderColumnNamesInCSVFormat(td))
				.append(getLineSeperator())
				//.append(getLineSeperator())
				.append(renderDataSetInCSVFormat(td));
			return sb.toString();
		}
		
		/**
		 * Takes the composite columns collections and renders each String into
		 *  the returned StringBuffer seperated by COMMA's
		 * @return
		 * @author Trevor Hinson
		 */
		StringBuffer renderColumnNamesInCSVFormat(TableDetails td) {
			StringBuffer sb = new StringBuffer();
			sb.append(COL_TABLE_NAME);
			sb.append(COMMA);
			for (Iterator<String> i = td.getColumns().iterator(); i.hasNext();) {
				String name = i.next();
				sb.append(name);
				if (i.hasNext()) {
					sb.append(COMMA);
				}
			}
			return sb;
		}
		
		/**
		 * Renders to the returned StringBuffer the data held in the composite
		 *  collection.
		 * @return
		 * @author Trevor Hinson
		 */
		StringBuffer renderDataSetInCSVFormat(TableDetails td) {
			StringBuffer sb = new StringBuffer();

			for (List<ExtractDataHolder> row : td.getData()) {
				sb.append(td.getTableName());sb.append(COMMA);
				for (Iterator<ExtractDataHolder> i = row.iterator(); i.hasNext();) {
					ExtractDataHolder element =  i.next();
					sb.append(element.getSqlRenderedValue());
					if (i.hasNext()) {
						sb.append(COMMA);
					}
				}
				sb.append(getLineSeperator());
			}
			return sb;
		}
	}

	/**
	 * Checks the input is a number and less than a particular level.
	 * @param ti 
	 * @exception 
	 * @author Trevor Hinson
	 */
	protected boolean validateStudentTestInstance(String ti)
		throws ExtractorException {
		boolean valid = false;
		if (Strings.isNotEmpty(ti)) {
			try {
				Integer inte = new Integer(ti);
				if (inte < SIZE) {
					valid = true;
				}
			} catch (NumberFormatException x) {
				try {
					getLog().logDebug("Input is not a valid Integer value.", x);
				} catch (UtilityException ex) {
					ex.printStackTrace();
				}
			}
		}
		return valid;
	}

	/**
	 * Writes the extraction data to the specified file so that it can be
	 *  utilised elsewhere.
	 * @param fileName
	 * @param output
	 * @throws ExtractorException
	 * @author Trevor Hinson
	 */
	private void writeToFile(String fileName, StringBuilder output)
		throws ExtractorException {
		if (Strings.isNotEmpty(fileName)
			&& (null != output ? output.length() > 0 : false)) {
			File f = new File(fileName);
			if (f.exists()) {
				f.delete();
			}
			try {
				GeneralUtils.copyToFile(f, output.toString());
			} catch (FileNotFoundException x) {
				throw new ExtractorException(x);
			} catch (IOException x) {
				throw new ExtractorException(x);
			}
		} else {
			throw new ExtractorException("Unable to write to the file as either"
				+ " the fileName or the actual output was null : "
				+ "\n - fileName = " + fileName
				+ "\n - output = " + output);
		}
	}

	/**
	 * Simply to render the resulting page from the extract and provide a
	 *  link to the generated extraction file.
	 * @param extractionFileName
	 * @return
	 * @author Trevor Hinson
	 */
	private String renderResultPage(String extractionFileName) {
		return new StringBuilder()
			.append("The extraction has finished and the resulting file can ")
			.append("be found at : <br /><br />")
			.append(extractionFileName).append(DAT_SUFFIX).append(" and ")
			.append(extractionFileName).append(CSV_SUFFIX).toString();
	}

	/**
	 * First phase of the extraction processing.  Here we iterate over the
	 *  "TestInstance" tables with the supplied ti.  With each ResultSet we
	 *  delegate to the applyToOutputRendering().
	 * @param ti
	 * @param output
	 * @param er
	 * @return We return the associated QuestionInstance qi for use later.
	 * @throws ExtractorException
	 * @author Trevor Hinson
	 */
	private QuestionInstanceIdentification renderFromTestInstanceTables(
		InMemoryRepresentation imr, String ti, StringBuilder output,
		ExtractionResponse er) throws ExtractorException {
		QuestionInstanceIdentification qii = null;
		for (String name : tableNamesByTestInstance) {
			if (Strings.isNotEmpty(name) && Strings.isNotEmpty(ti)) {
				Object[] arguments = {name, ti};
				String sql = MessageFormat.format(TEST_INSTANCE_SQL, arguments);
				DatabaseAccess.Transaction dat = null;
				try {
					getLog().logDebug("Running : " + sql);
					dat = getDatabaseAccess().newTransaction();
					ResultSet rs = dat.query(sql);
					QuestionInstanceIdentification identifier = applyToOutputRendering(
						output, imr, rs, name, null, er);
					if (null == qii) {
						qii = identifier;
					}
				} catch (UtilityException x) {
					throw new ExtractorException(x);
				} catch (SQLException x) {
					throw new ExtractorException(x);
				} finally {
					if (null != dat) {
						dat.finish();
					}
				}
			}
		}
		return qii;
	}

	/**
	 * Here we deal with all the QuestionInstance based tables of the student
	 *  for the extraction process.  We iterate over them all with retrieving
	 *  the details from the database.  We lastly invoke applyToOutputRendering
	 *  on each ResultSet.
	 * @param output
	 * @param qii Must contain the qi
	 * @param er
	 * @throws ExtractorException
	 * @author Trevor Hinson
	 */
	private void renderFromQuestionInstanceTables(InMemoryRepresentation imr,
		StringBuilder output, QuestionInstanceIdentification qii,
		ExtractionResponse er) throws ExtractorException {
		if ((null != qii ? Strings.isNotEmpty(qii.toString()) : false)
			&& null != output && null != er) {
			for (String tableName : tableNamesByQuestionInstance) {
				if (null != tableName ? Strings.isNotEmpty(tableName) : false) {
					Object[] arguments = {tableName, qii.toString()};
					String sql = MessageFormat.format(QUESTION_INSTANCE_SQL, arguments);
					DatabaseAccess.Transaction dat = null;
					try {
						dat = getDatabaseAccess().newTransaction();
						ResultSet rs = dat.query(sql);
						applyToOutputRendering(output, imr, rs, tableName, qii, er);
					} catch (UtilityException x) {
						throw new ExtractorException(x);
					} catch (SQLException x) {
						throw new ExtractorException(x);
					} finally {
						if (null != dat) {
							dat.finish();
						}
					}
				}
			}
		} else {
			throw new ExtractorException("Unable to run the query against the"
				+ " following tables as either the QuestionInstanceIdentifier"
				+ " was invalid or the StringBuilder output was null : "
				+ "\n QuestionInstanceIdentification = " + qii
				+ "\n tables = " + tableNamesByQuestionInstance
				+ "\n StringBuilder output = " + output);
		}
	}

	/**
	 * Here we iterate over the ResultSet from the specified tableName and 
	 *  apply the values to the output StringBuilder.
	 * @param output 
	 * @param imr 
	 * @param rs 
	 * @param tableName 
	 * @param er 
	 * @throws ExtractorException
	 * @author Trevor Hinson
	 */
	public QuestionInstanceIdentification applyToOutputRendering(
		StringBuilder output, InMemoryRepresentation imr, ResultSet rs,
		String tableName, QuestionInstanceIdentification qii, ExtractionResponse er)
		throws ExtractorException {
		if (null != output && null != rs
			&& (null != tableName ? Strings.isNotEmpty(tableName) : false)) {
			try {
				TableDetails td = new TableDetails(tableName);
				ResultSetMetaData md = rs.getMetaData();
				int columnCount = md.getColumnCount();
				List<String> columns = new ArrayList<String>();
				while (rs.next()) {
					List<ExtractDataHolder> rowData = new ArrayList<ExtractDataHolder>();
					qii = checkForQuestionInstance(rs, tableName, qii);
					StringBuilder columnNames = new StringBuilder();
					StringBuilder values = new StringBuilder();
					for (int i = 0; i < columnCount; i++) {
						String colName = md.getColumnName(i + 1);
						columns.add(colName);
						String typeName = md.getColumnTypeName(i + 1);
						getLog().logDebug("Handling type : " + typeName);
						columnNames.append(colName);
						Object value = rs.getObject(colName);
						getLog().logDebug("Java Class type = "
							+ (null != value ? value.getClass() : "null"));
						StringBuilder sb = accomodateForDataType(typeName, value);
						ExtractDataHolder edh = new ExtractDataHolder(typeName,
							value, colName, sb);
						record(edh, er, tableName);
						rowData.add(edh);
						values.append(sb);
						if (i + 1 != columnCount) {
							columnNames.append(",");
							values.append(",");
						}
					}
					if (!td.areColumnNamesSet()) {
						td.addColumnNames(columns);
					}
					td.addData(rowData);
					StringBuilder insert = new StringBuilder(INSERT_INTO)
						.append(tableName).append(OPEN).append(columnNames)
						.append(VALUES).append(values).append(CLOSE);
					addInsert(insert, er, tableName);
					output.append(insert).append(" ")
						.append(getLineSeperator()).append(getLineSeperator());
				}
				if (null != imr) {
					imr.addTableDetails(td);
				}
			} catch (SQLException x) {
				throw new ExtractorException(x);
			} catch (UtilityException x) {
				throw new ExtractorException(x);
			}
		} else {
			throw new ExtractorException("Unable to continue with the ");
		}
		return qii;
	}

	/**
	 * Tries to pickup the system line seperator.
	 * @return
	 * @author Trevor Hinson
	 */
	private String getLineSeperator() {
		if (Strings.isEmpty(lineSeperator)) {
			lineSeperator = System.getProperty(LINE_SEPERATOR);
		}
		return lineSeperator;
	}

	/**
	 * Here we record the actual insert statements so that they can be run 
	 *  individually against a table should.
	 * @param insert
	 * @param er
	 * @param tableName
	 * @author Trevor Hinson
	 */
	private void addInsert(StringBuilder insert, ExtractionResponse er,
		String tableName) {
		if (null != er
			&& (null != insert
				? Strings.isNotEmpty(insert.toString()) : false)) {
			er.addInsertStatement(tableName, insert.toString());
		}
	}

	/**
	 * Keep track of what has been extracted so that we can do other things with
	 *  the data later on in a much more convienient fashion.
	 * @param edh
	 * @param er
	 * @param tableName
	 * @throws ExtractorException
	 * @author Trevor Hinson
	 */
	private void record(ExtractDataHolder edh, ExtractionResponse er,
		String tableName) throws ExtractorException {
		if (null != edh && null != er
			&& Strings.isNotEmpty(tableName)) {
			//TODO SLW2
			//er.addExtractDataHolder(tableName, edh);
		}
	}

	/**
	 * Here we check the ResultSet for "qi" column.  If found then we use that
	 *  to run against the tableNamesByQuestionInstance
	 * @param rs
	 * @param tableName
	 * @param qii
	 * @return
	 * @throws SQLException
	 * @throws PersistenceException
	 * @author Trevor Hinson
	 */
	private QuestionInstanceIdentification checkForQuestionInstance(ResultSet rs,
		String tableName, QuestionInstanceIdentification qii)
		throws SQLException, UtilityException {
		if (null != rs && null == qii
			&& tableName.equalsIgnoreCase(QI_TABLE)) {
			//TODO SLW2

			//getLog().logDebug("ExtractorEnums.qi.toString() = "
			//	+ ExtractorEnums.qi.toString());
			//int qi = rs.getInt(ExtractorEnums.qi.toString());
			//qii = new QuestionInstanceIdentification(qi);
			//getLog().logDebug("Found the qi of : " + qi);
		}
		return qii;
	}

	/**
	 * In order to provide for an easy to interpret reading back in the SQL.
	 * @param typeName
	 * @param value
	 * @return
	 * @throws 
	 * @author Trevor Hinson
	 */
	StringBuilder accomodateForDataType(String typeName, Object value)
		throws UtilityException {
		StringBuilder res = new StringBuilder();
		if (Strings.isNotEmpty(typeName)) {
			if (DataType.bit.toString().equalsIgnoreCase(typeName)) {
				if (null != value ? value instanceof Boolean : false) {
					Boolean b = (Boolean) value;
					res.append(b ? "1" : "0");
				} else {
					applyTypicalRendering(res, value, typeName);
				}
			} else if (DataType.varchar.toString().equalsIgnoreCase(typeName)
				|| DataType.nvarchar.toString().equalsIgnoreCase(typeName)
				|| DataType.text.toString().equalsIgnoreCase(typeName)
				|| DataType.ntext.toString().equalsIgnoreCase(typeName)
				|| "char".equalsIgnoreCase(typeName)) {
				res.append("'").append(null != value ? value : "").append("'");
			} else if (DataType.datetime.toString().equalsIgnoreCase(typeName)) {
				res.append("'").append(null != value ? value : "").append("'");
			} else {
				applyTypicalRendering(res, value, typeName);
			}
		} else {
			applyTypicalRendering(res, value, typeName);
		}
		return res;
	}

	/**
	 * Simply applies a standard object to the output (ie: not an object that
	 *  requires special treatment.
	 * @param sb
	 * @param value
	 * @param type
	 * @throws PersistenceException
	 * @author Trevor Hinson
	 */
	private void applyTypicalRendering(StringBuilder sb, Object value, String type)
			throws UtilityException {
		getLog().logDebug("rendering normally for : " + type + " with value = "
			+ value);
		sb.append(null != value ? value : "''");
	}

	/**
	 * Used to check that we have everything we need in order to start running
	 *  the extractor.  Returns true if everything is found and we can continue
	 *  processing the request.
	 * @param metaData
	 * @return
	 * @author Trevor Hinson
	 */
	private boolean initialise(Map<String, Object> metaData) {
		boolean valid = false;
		if (null != metaData) {
			Object obj = metaData.get(ExtractorEnums.navigatorConfigKey.toString());
			//TODO SLW2
			//String ti = retrieve(metaData, ExtractorEnums.studentTi.toString());
			String ti="";
			String fn = retrieve(metaData,
				ExtractorEnums.extractionFileNamePrefix.toString());
			Object path = metaData.get(ExtractorEnums.logPath.toString());
			if ((null != obj ? obj instanceof NavigatorConfig : false)
				&& (null != path ? path instanceof String : false)) {
				if ((null != ti ? Strings.isNotEmpty((String) ti) : false)
					&& (null != fn ? Strings.isNotEmpty((String) fn) : false)
					&& Strings.isNotEmpty((String) path)) {
					try {
						initialise((NavigatorConfig) obj, (String) path,
							isDebugging(metaData));
						accomodateForConfiguredTables(metaData);
						valid = true;
					} catch (UtilityException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return valid;
	}

	/**
	 * Here we check the metaData for the tables from which to use.  If they are
	 *  found within the metaData then we replace those stored statically within
	 *  this class with these configured versions.
	 * @param metaData
	 * @author Trevor Hinson
	 */
	private void accomodateForConfiguredTables(Map<String, Object> metaData)
		throws UtilityException {
		if (null != metaData) {
			List<String> testInstanceTables = retrieveList(metaData,
				ExtractorEnums.TestInstanceTables);
			if (null != testInstanceTables ? testInstanceTables.size() > 0 : false) {
				tableNamesByTestInstance.clear();
				tableNamesByTestInstance.addAll(testInstanceTables);
				getLog().logDebug("Added Test Instance tables from configuration : "
					+ testInstanceTables);
			}
			List<String> questionInstanceTables = retrieveList(metaData,
				ExtractorEnums.QuestionInstanceTables);
			if (null != questionInstanceTables ? questionInstanceTables.size() > 0 : false) {
				tableNamesByQuestionInstance.clear();
				tableNamesByQuestionInstance.addAll(questionInstanceTables);
				getLog().logDebug("Added Question Instance tables from configuration : "
					+ questionInstanceTables);
			}
		}
	}

	/**
	 * Used to pick up a particular List based on the ExtractEnum parameter from
	 *  the metaData and return it.  It also checks to make sure that everything
	 *  held within the List found in the metaData is actually a String.  So for
	 *  using this method the composite List within the metaData must conform.
	 * @param metaData
	 * @param ee
	 * @return
	 * @author Trevor Hinson
	 */
	private List<String> retrieveList(Map<String, Object> metaData,
		ExtractorEnums ee) {
		List<String> tables = null;
		if (null != metaData && null != ee) {
			Object obj = metaData.get(ee.toString());
			if (null != obj ? obj instanceof List<?> : false) {
				for (Object o : (List<?>) obj) {
					if (null != o ? o instanceof String : false) {
						String name = (String) o;
						if (Strings.isNotEmpty(name)) {
							if (null == tables) {
								tables = new ArrayList<String>();
							}
							tables.add(name);
						}
					}
				}
			}
		}
		return tables;
	}

	/**
	 * Retrieve a String from the metaData Map based on the provided key.
	 * @param metaData
	 * @param key
	 * @return
	 * @author Trevor Hinson
	 */
	private String retrieve(Map<String, Object> metaData, String key) {
		String s = null;
		if (null != metaData) {
			Object obj = metaData.get(key);
			if (null != obj ? obj instanceof String : false) {
				s = (String) obj;
			}
		}
		return s;
	}

	/**
	 * Determines if logging debug has been turned on or not.
	 * @param metaData
	 * @return
	 * @author Trevor Hinson
	 */
	private boolean isDebugging(Map<String, Object> metaData) {
		boolean is = false;
		if (null != metaData) {
			Object obj = metaData.get(ExtractorEnums.debug.toString());
			if (null != obj ? obj instanceof Boolean : false) {
				is = (Boolean) obj;
			}
		}
		return is;
	}

	/**
	 * Used to hold reference to the "qi" when found against the "ti" provided.
	 * @author Trevor Hinson
	 */
	class QuestionInstanceIdentification {
		
		private Integer qi;
		
		QuestionInstanceIdentification(Integer s) {
			qi = s;
		}
		
		public String toString() {
			return null != qi ? qi.toString() : "";
		}
		
	}

	@Override
	public FinalizedResponse close(Object o) throws UtilityException {
		getDatabaseAccess().close();
		getLog().close();
		return new StandardFinalizedResponse(true);
	}

	/**
	 * Hold details of the everything that will be written to the file(s)
	 *  As the name suggests this is an in memory representation of the data.
	 *  Please see the explanation of the StudentDetailsExtractor class for
	 *  further details as to why and when it may be appropriate to change
	 *  the implementation.
	 * @author Trevor Hinson
	 */
	class InMemoryRepresentation {
		
		private List<TableDetails> tables = new ArrayList<TableDetails>();
		
		void addTableDetails(TableDetails tableDetails) {
			if (null != tableDetails) {
				tables.add(tableDetails);
			}
		}
		
		public List<TableDetails> getTableDetails() {
			return tables;
		}
		
	}

	/**
	 * Represents a particular table from the database that is then used to 
	 *  write out to the files.
	 * @author Trevor Hinson
	 */
	class TableDetails {
		
		private String tableName;
		
		private List<String> columns = new ArrayList<String>();
		
		private List<List<ExtractDataHolder>> data = new ArrayList<List<ExtractDataHolder>>();
	
		String getTableName() {
			return tableName;
		}
		
		List<String> getColumns() {
			return Collections.unmodifiableList(columns);
		}
		
		List<List<ExtractDataHolder>> getData() {
			return Collections.unmodifiableList(data);
		}
		
		public TableDetails(String name) {
			tableName = name;
		}
		
		void addData(List<ExtractDataHolder> rowData) {
			if (null != rowData) {
				data.add(rowData);
			}
		}
		
		void addColumnNames(List<String> columnNames) {
			if (null != columnNames) {
				columns.addAll(columnNames);
			}
		}
		
		boolean areColumnNamesSet() {
			return null != columns ? columns.size() > 0 : false;
		}
	}

}

