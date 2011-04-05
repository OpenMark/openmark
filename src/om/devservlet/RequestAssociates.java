package om.devservlet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RequestAssociates {

	private boolean post;

	private String path;

	private Map<String, Object> configuration;

	public RequestAssociates(String sPath, boolean b,
		Map<String, Object> config) {
		post = b;
		path = sPath;
		configuration = config;
	}

	public String getPath() {
		return path;
	}

	public boolean getPost() {
		return new Boolean(post);
	}

	public Map<String, Object> getConfiguration() {
		if (null == configuration) {
			configuration = new HashMap<String, Object>();
		}
		return Collections.unmodifiableMap(configuration);
	}

	public boolean valid() {
		return null != getPath() ? getPath().length() > 0 : false;
	}
}
