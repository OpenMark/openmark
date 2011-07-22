package om.tnavigator.auth;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import om.Log;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.db.DatabaseAccess;

public class AuthenticationFactory {

	/**
	 * Refactored from the NavigatorServlet for reuse. Here we use the
	 *  NavigatorConfig in order to determine which Authentication
	 *  implementation to return.  The configured implementation is then
	 *  dynamically classloaded and, if there are no problems, it is returned.
	 * 
	 * @param nc
	 * @param da
	 * @param templateFolder
	 * @param log
	 * @return
	 * @throws AuthenticationInstantiationException
	 * @author Trevor Hinson
	 */
	public static Authentication initialiseAuthentication(NavigatorConfig nc,
		DatabaseAccess da, File templateFolder, Log log)
		throws AuthenticationInstantiationException {
		Authentication authentication = null;
		String authClass = nc.getAuthClass();
		try {
			// auth = (Authentication) Class.forName(authClass).getConstructor(
			// new Class[] { NavigatorServlet.class }).newInstance(
			// new Object[] { this });
			AuthenticationInitialisation ai = new AuthenticationInitialisation(
					da, nc, templateFolder, log);
			Class<?> cla = Class.forName(authClass);
			Constructor<?> c = cla
				.getConstructor(new Class[] { AuthenticationInitialisation.class });
			authentication = (Authentication) c.newInstance(new Object[] { ai });
		} catch (SecurityException x) {
			throw new AuthenticationInstantiationException(x);
		} catch (IllegalArgumentException x) {
			throw new AuthenticationInstantiationException(x);
		} catch (ClassNotFoundException x) {
			throw new AuthenticationInstantiationException(x);
		} catch (NoSuchMethodException x) {
			throw new AuthenticationInstantiationException(x);
		} catch (InstantiationException x) {
			throw new AuthenticationInstantiationException(x);
		} catch (IllegalAccessException x) {
			throw new AuthenticationInstantiationException(x);
		} catch (InvocationTargetException x) {
			throw new AuthenticationInstantiationException(x);
		}
		return authentication;
	}

}
