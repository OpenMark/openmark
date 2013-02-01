/**
 * 
 */
package om.qengine;

/**
 * Shared interface between QEngine and the Developer servlet so they can provide
 * configuration information to other bits of code.
 */
public interface QEngineConfig {
	/**
	 * @param key key to identify the bit of information requested.
	 * @return the corresponding object.
	 */
	public Object getConfiguration(String key);

	/**
	 * Store some configuration information.
	 * @param key key to identify the bit of information requested.
	 * @param value the corresponding object.
	 */
	public void setConfiguration(String key, Object value);
}
