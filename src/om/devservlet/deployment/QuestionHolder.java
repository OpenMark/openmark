package om.devservlet.deployment;

import java.io.File;
import java.util.Map;

public class QuestionHolder {

	private File jarFile;

	private File xmlFile;

	private String namePrefix;

	private Map<String, String> metaData;

	private File deployableQuestionLocation;

	public String toString() {
		return namePrefix;
	}

	public File getJarFile() {
		return jarFile;
	}

	public File getXmlFile() {
		return xmlFile;
	}

	public Map<String, String> getMetaData() {
		return metaData;
	}

	public String getNamePrefix() {
		return namePrefix;
	}

	public File getDeployableQuestionLocation() {
		return deployableQuestionLocation;
	}

	public String getOriginatingLocation() {
		return null != getDeployableQuestionLocation()
			? getDeployableQuestionLocation().getAbsolutePath() : null;
	}

	public QuestionHolder(File xml, File jar, Map<String, String> md,
		String prefix, File originalLocation) {
		jarFile = jar;
		xmlFile = xml;
		metaData = md;
		namePrefix = prefix;
		deployableQuestionLocation = originalLocation;
	}

}
