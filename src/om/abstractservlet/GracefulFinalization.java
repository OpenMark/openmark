package om.abstractservlet;

import om.OmException;
import util.misc.FinalizedResponse;
import util.misc.UtilityException;


/**
 * We can not rely on the finalize method of an Object in order to release
 *  certain critial objects without risking memory leaks.  Therefore this 
 *  interface tries to ensure that the implementor tidies itself up.  Naturally
 *  this needs to be invoked by the running process at the appropriate time and
 *  that is down to the client developer to determine. 
 * @author Trevor Hinson
 */

public interface GracefulFinalization {

	/**
	 * Simple template for the closing of resources within given implementations
	 *  this method should then be invoked before disposal of the implementor.
	 * Where waiting for the finalize method on an implementor is either not
	 *  sufficient or valid / appropriate. Ensure to invoke the implementation
	 *  at teh appropriate point also !
	 * @param o
	 * @return
	 * @throws UtilityException
	 * @author Trevor Hinson
	 * @throws OmException 
	 */
	FinalizedResponse close(Object o) throws UtilityException;

}
