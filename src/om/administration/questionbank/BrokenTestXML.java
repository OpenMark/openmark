package om.administration.questionbank;

public class BrokenTestXML {

	private String fullPath;

	public String getFullPath() {
		return fullPath;
	}

	public BrokenTestXML(String path) {
		fullPath = path;
	}

	public String toString() {
		return null != getFullPath() ? getFullPath() : "Path not set.";
	}
}
