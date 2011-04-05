package om;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * TODO : this mixes the presentation logic with the business logic.  Look to
 *  refactor away into seperate presentational objects and pull from a seperate
 *  easy to change source. TMH.
 */

public class DisplayUtils {

	private static String LOCATION = "location";

	public static String header() {
		return "<xhtml>" +
		"<head>" +
		"<title>OpenMark-S (Om) question development</title>"+
		"<style type='text/css'>\n"+
		"body { font: 12px Verdana, sans-serif; }\n" +
		"h1 { font: bold 14px Verdana, sans-serif; }\n" +
		"a { color: black; }\n" +
		"h2 { font: 14px Verdana, sans-serif; }\n" +
		".alert { font: bold 21px; font-weight: bold; color:red; Verdana, sans-serif; }\n" +
		"#create,#questionbox { margin-bottom:20px; border:1px solid #888; padding:10px; }\n"+
		"#create span { float:left; width:20em; margin-top: 5px }\n" +
		"#create span.fields { width:auto; }\n" +
		"#create div { clear:left; }\n"+
		"</style>"+
		"</head>"+
		"<body>";
	}

	public static String applyListingLinkDisplay() {
		return "<a href='/om'>Back to Listing</a>";
	}

	public static String footer() {
		return "</body></xhtml>";
	}

	/**
	 * Takes the configured metaData and extracts the settings that specify
	 *  a location as the key.
	 * @param metaData
	 * @return
	 * @author Trevor Hinson
	 */
	public static List<String> getLocations(Map<String, String> metaData) {
		List<String> locations = new ArrayList<String>();
		if (null != metaData) {
			for (String key : metaData.keySet()) {
				String value = metaData.get(key);
				if (StringUtils.isNotEmpty(key)
					? key.startsWith(LOCATION) : false) {
					if (StringUtils.isNotEmpty(value)) {
						locations.add(value);
					}
				}
			}
		}
		return locations;
	}
}
