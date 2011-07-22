package util.misc;

import java.io.File;
import java.io.FileFilter;

/**
 * Simplified file filter for the deploy files.  Used to distinguish those
 *  files from within the configured test bank directory.
 * @author Trevor Hinson
 */

public class TestDeployFileFilter implements FileFilter {

	private static String DEPLOY_FILE_SUFFIX = "deploy.xml";

	@Override
	public boolean accept(File f) {
		return null != f ? f.getName().endsWith(DEPLOY_FILE_SUFFIX) : false;
	}

}
