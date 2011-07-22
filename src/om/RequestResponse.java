package om;

import java.io.Serializable;

/**
 * Simply interface for the RequestHandler contract.  Implementations
 *  can therefore be anything necessary for the requirement.
 * @author Trevor Hinson
 */

public interface RequestResponse extends Serializable {

	/**
	 * Used to determine the success of the request itself.
	 * @return
	 * @author Trevor Hinson
	 */
	boolean isSuccessful();

	/**
	 * In the event of any error processing the request the success should be
	 *  set to false.
	 * @param b
	 * @author Trevor Hinson
	 */
	void setSuccessful(boolean b);

	/**
	 * Apply an output response message.
	 * @param output
	 * @author Trevor Hinson
	 */
	StringBuffer append(String output);
}
