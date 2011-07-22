package util.misc;

import java.io.File;
import java.io.FileFilter;

/**
 * Simplified file filter for the deploy files.  Used to distinguish those
 *  files from within the configured test bank directory.
 * @author Trevor Hinson
 */

public class StandardFileFilter implements FileFilter {

	protected String suffix;

	public StandardFileFilter(String s) {
		suffix = s;
	}

	@Override
	public boolean accept(File f) {
		return null != f ? f.getName().endsWith(suffix) : false;
	}

}
