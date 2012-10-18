package om.administration.databaseCleaner;

public class TestXML {

	private String fullPath;

	public String getFullPath() {
		return fullPath;
	}

	public TestXML(String path) {
		fullPath = path;
	}

	public String toString() {
		return null != getFullPath() ? getFullPath() : "Path not set.";
	}
}
