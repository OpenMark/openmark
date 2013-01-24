package om.qengine;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.tools.JavaFileObject;

import om.OmException;
import om.qengine.QuestionCache.QuestionStuff;
import om.qengine.dynamics.QuestionClassBuilder;
import om.qengine.dynamics.QuestionRepresentation;
import om.qengine.dynamics.util.DynamicCompilationReport;
import om.qengine.dynamics.util.DynamicCompilationResponse;
import om.qengine.dynamics.util.DynamicJavaFile;
import om.qengine.dynamics.util.DynamicOMClassLoader;
import om.qengine.dynamics.util.DynamicQuestionUtils;
import om.qengine.dynamics.util.DynamicQuestionsCompilationUtil;
import om.question.Question;

import org.w3c.dom.Element;

import util.misc.Strings;

/**
 * An implementation of the QuestionLoader specifically designed for the
 *  ".omxml" implementations.
 * 
 * This implementation has a Java compilation as the default and therefore
 *  ignores the language attribute of the <handler /> node which has been placed
 *  there to show the intention for potentially using different languages in
 *  the future if the need arises.  i.e. it is there to illustrate the intention
 *  and therefore not a requirement.
 * 
 * @author Trevor Hinson
 */

public class DynamicQuestionsLoader implements QuestionLoader {

	private static String DOT_JAR = ".jar";

	private static String HANDLER = "handler";

	//private String classOutputFolderX = "/Temp/dynamics/";
	private String classOutputFolder;

	private List<String> classPathItems = new ArrayList<String>();

	private QuestionClassBuilder questionClassBuilder;

	public DynamicQuestionsLoader(String libPath) {
		if (null != libPath) {
			File path = new File(libPath);
			if (path.exists() && path.isDirectory() && path.canRead()) {
				File[] files = path.listFiles();
				if (null != files) {
					for (int i = 0; i < files.length; i++) {
						File f = files[i];
						if (null != f ? isJarOrDirectory(f) : false) {
							classPathItems.add(f.getAbsolutePath());
						}
					}
				}
			}
			classPathItems.add(path.getAbsolutePath());
			classOutputFolder = path.getAbsolutePath();
			questionClassBuilder = new QuestionClassBuilder();
		}
	}

	/**
	 * Checks to ensure only Jar files and directories that are readable to be
	 *  added to the classPathItems.
	 * 
	 * @param f
	 * @return
	 * @author Trevor Hinson
	 */
	protected boolean isJarOrDirectory(File f) {
		boolean is = false;
		if (null != f ? f.getName().endsWith(DOT_JAR) || f.isDirectory() : false) {
			if (f.canRead()) {
				is = true;
			}
		}
		return is;
	}

	@Override
	public Question load(QuestionStuff qs) throws OmException {
		Question q = null;
		if (null != qs ? null != qs.omclc : false) {
			
		}
		return q;
	}

	/**
	 * Builds a String representation of the composite classPathItems.
	 * 
	 * @return
	 * @author Trevor Hinson
	 */
	private String getClassPath() {
		StringBuilder path = new StringBuilder();
		if (null != classPathItems ? classPathItems.size() > 0 : false) {
			for (Iterator<String> i = classPathItems.iterator(); i.hasNext();) {
				String s = i.next();
				if (Strings.isNotEmpty(s)) {
					path.append(s);
					if (i.hasNext()) {
						path.append(";");
					}
				}
			}
		}
		return path.toString();
	}

	/**
	 * This implementation uses the class that is either defined OR referenced
	 *  in the handler node.  To clarify the definition of the Java class within
	 *  the text part of the handler node OR the "className" that is an
	 *  Attribute of the handler node.
	 *  <br /><br />
	 * <b>Importantly</b> The "className" needs to be unique and the same as
	 *  that defined within the text part of the handler node.  This is because
	 *  the "className" is first used to see if there is already a class by that
	 *  name within the ClassLoader.  IF there is then that is used.  This is 
	 *  deliberately carried out as a means both for performance AND re-usability
	 *  between questions.  It means that one dynamic question can refer to the
	 *  same className as that used by another that is know to exist.
	 * <br /><br />
	 * <b>WARNING</b> It is best to redefine the class within the handler each
	 *  time.  A check is made for an existing class by that name space and used
	 *  if found but otherwise it is best to have the definition there just
	 *  in case things change.
	 * 
	 * @author Trevor Hinson
	 */
	@Override
	public void loadClass(QuestionStuff qs, File f) throws OmException {
		if (null != qs && null != f) {
			Element e = DynamicQuestionUtils.retrieveElement(f, HANDLER);
			QuestionRepresentation qr = questionClassBuilder.generate(e);
			if (null != qr ? qr.isValid() : false) {
				String className = qr.getFullClassName();
				DynamicOMClassLoader d = retrieveClassLoader(qs);
				Class<?> cla = retrieveClass(className, d);
				if (null == cla) {
					synchronized (DynamicQuestionUtils.QUESTION) {
						if (null == cla) {
							compile(className, qr.getRepresentation());
							cla = retrieveClass(className, d);
						}
					}
				}
				qs.c = cla;
			}
		}
	}

	/**
	 * Tries to retrieve the composite DynamicOMClassLoader from the
	 *  QuestionStuff argument and return it.
	 * 
	 * @param qs
	 * @return
	 * @throws OmException
	 * @author Trevor Hinson
	 */
	protected DynamicOMClassLoader retrieveClassLoader(QuestionStuff qs)
		throws OmException {
		DynamicOMClassLoader d = null;
		if (null != qs ? null != qs.omclc : false) {
			if (qs.omclc instanceof DynamicOMClassLoader) {
				d = (DynamicOMClassLoader) qs.omclc;
			}
		}
		return d;
	}

	/**
	 * Tries to retrieve the class from the classpath.
	 * 
	 * @param className
	 * @param d
	 * @return
	 * @throws OmException
	 * @author Trevor Hinson
	 */
	protected Class<?> retrieveClass(String className, DynamicOMClassLoader d) {
		Class<?> cla = null;
		try {
			cla = d.loadClass(className);
		} catch (Throwable x) {
			//x.printStackTrace();
		}
		return cla;
	}

	/**
	 * Looks to compile the class based on the java definition found within the
	 *  omxml file.
	 * 
	 * @param className
	 * @param definition
	 * @throws OmException
	 * @author Trevor Hinson
	 */
	protected void compile(String className, String definition) throws OmException {
		DynamicJavaFile tjf = new DynamicJavaFile(className, definition);
		Iterable<? extends JavaFileObject> files = Arrays.asList(tjf);
		DynamicCompilationResponse response = DynamicQuestionsCompilationUtil
			.compile(getClassPath(), files, classOutputFolder);
		if (null != response ? !response.isSuccess() : false) {
			StringBuilder sb = new StringBuilder();
			for (DynamicCompilationReport dcr : response.getReports()) {
				System.out.println(dcr.getMessage());
				sb.append(dcr.getMessage());
				// LOG THIS ALSO !
			}
			throw new OmException("Unable to continue as the engine was not able"
				+ " to build the Question : " + sb);
		}
	}

	@Override
	public void loadMetaData(QuestionStuff qs, File f) throws OmException {
		if (null != qs && null != f) {
			qs.dMeta = DynamicQuestionUtils.metaDataFromDynamicQuestion(f);
		}
	}

}
