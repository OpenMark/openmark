package om.administration.databaseCleaner;

/**
 * Holds reference to a particular data element from the data store.
 * @author Trevor Hinson
 */

public class ExtractDataHolder {

	private String typeName;

	private Object value;

	private String columnName;

	private StringBuilder sqlRenderedValue;

	public ExtractDataHolder(String type, Object val, String colName,
		StringBuilder rendering) {
		typeName = type;
		value = val;
		columnName = colName;
		sqlRenderedValue = rendering;
	}

	public String getColumnName() {
		return columnName;
	}

	public StringBuilder getSqlRenderedValue() {
		return sqlRenderedValue;
	}

	public String getTypeName() {
		return typeName;
	}

	public Object getValue() {
		return value;
	}

}
