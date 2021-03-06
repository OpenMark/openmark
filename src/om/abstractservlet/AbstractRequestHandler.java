package om.abstractservlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.Log;
import om.OmException;
import util.misc.FinalizedResponse;
import util.misc.GeneralUtils;
import util.misc.UtilityException;

public abstract class AbstractRequestHandler implements RequestHandler {

	private static final long serialVersionUID = -7707230391964258898L;

	private Log log;

	private String logPath;

	public String getLogPath() {
		return logPath;
	}

	/**
	 * Picks up a specific Log implementation for this class otherwise the class
	 *  uses the one provided in from the RequestAssociates visitor object.
	 * @return
	 * @throws IOException
	 * @throws UtilityException
	 * @author Trevor Hinson
	 */
	protected Log getLog() throws UtilityException {
		if (null == log) {
			log = GeneralUtils.getLog(getClass(), getLogPath(), true);
		}
		return log;
	}

	/**
	 * This implementation should be overridden but with the initial call back
	 *  to super.handle(...
	 * @author Trevor Hinson
	 * @throws UtilityException 
	 */
	public RequestResponse handle(HttpServletRequest request,
		HttpServletResponse response, RequestAssociates associates)
		throws UtilityException {
		initialise(associates);
		return new RenderedOutput();
	}

	/**
	 * Initialises the RequestHandler implementation with the logPath.
	 * @param associates
	 * @throws UtilityException
	 * @author Trevor Hinson
	 */
	public void initialise(RequestAssociates associates)
		throws UtilityException {
		logPath = associates.getConfig(RequestParameterNames.logPath.toString());
	}

	@Override
	public FinalizedResponse close(Object o) throws UtilityException {
		try {
			Log log = getLog();
			if (null != log) {
				log.close();
			}
		} catch (UtilityException x) {
			x.printStackTrace();
		}
		return null;
	}
}
