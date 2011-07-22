package om.administration.questionbank;

/**
 * Provides for a simplified wrapping of the rendered composite StringBuffer so
 *  that this may be easily extended.
 * @author Trevor Hinson
 */
public class RenderedCleaningResult {

	private StringBuffer output = new StringBuffer();

	public void append(String s) {
		if (null != s ? s.length() > 0 : false) {
			output.append(s);
		}
	}

	public String toString() {
		return output.toString();
	}
}
