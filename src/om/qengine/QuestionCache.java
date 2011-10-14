/* OpenMark online assessment system
   Copyright (C) 2007 The Open University

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package om.qengine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import om.OmDeveloperException;
import om.OmException;
import om.question.Question;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import util.misc.ClosableClassLoader;
import util.misc.DynamicOMClassLoader;
import util.misc.DynamicQuestionUtils;
import util.misc.IO;
import util.misc.OmClassLoaderContract;
import util.xml.XML;

/**
 * Provides access to questions.
 * <p>
 * Note that all methods in here take a 'question key' parameter (
 * {@link om.qengine.QuestionCache.QuestionKey}); this is a combination of the
 * question ID and version.
 */
public class QuestionCache {

	public static String dynamicQuestionType = "application/x-openmark-dynamics";

	public static String jarQuestionType = "application/x-openmark";

	public static String DOT_JAR = ".jar";

	public static String DOT_OMXML = ".omxml";

	/** Map of String (question key) -> QuestionStuff */
	private Map<QuestionKey, QuestionStuff> mActiveQuestions1 = new HashMap<QuestionKey, QuestionStuff>();

	/** Map of Question -> String (question key) */
	private Map<Question, QuestionKey> mActiveQuestions2 = new HashMap<Question, QuestionKey>();

	/** Folder where questions are cached */
	private File fFolder;

	private static Map<String, QuestionLoader> questionLoaders = new HashMap<String, QuestionLoader>();

	private static String FAILED_CLASSLOADER_MESSAGE = "Failed to start question classloader for: ";

	private String classPath;

	QuestionCache(File f, String cPath) {
		if (!f.exists())
			f.mkdirs();
		this.fFolder = f;
		if (!cPath.endsWith(File.separator)) {
			cPath = cPath + File.separator;
		}
		classPath = cPath;
		questionLoaders.put(QuestionTypeEnum.jar.toString(),
				new JarQuestionLoader());
		questionLoaders.put(QuestionTypeEnum.dynamic.toString(),
				new DynamicQuestionsLoader(classPath));
		Collections.unmodifiableMap(questionLoaders);
	}

	/** Holds stuff related to a particular question (ID) */
	protected static class QuestionStuff {
		Class<?> c = null;
		// ClosableClassLoader ccl;
		OmClassLoaderContract omclc;
		Document dMeta;
		List<Question> lActive = new LinkedList<Question>();
	}

	public static class QuestionInstance {
		Question q;
		// ClosableClassLoader cclX;
		OmClassLoaderContract omclc;
	}

	/**
	 * Questions are referenced in the cache by their ID and version. A question
	 * key combines both these, with a full stop between.
	 */
	static class QuestionKey {

		private String sQuestionID, sVersion;

		private String contentType;

		private String fileExtension() {
			return isDynamicQuestion() ? DOT_OMXML : DOT_JAR;
		}

		public boolean isDynamicQuestion() {
			return null != contentType ? dynamicQuestionType
					.equals(contentType) : false;
		}

		public void setContentType(String s) {
			contentType = s;
		}

		/**
		 * @param sQuestionID
		 *            ID of question
		 * @param sVersion
		 *            Version string
		 */
		QuestionKey(String sQuestionID, String sVersion) {
			this.sQuestionID = sQuestionID;
			this.sVersion = sVersion;
		}

		// Hashcode and equals so it can be used in maps
		@Override
		public int hashCode() {
			return (sQuestionID + sVersion).hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof QuestionKey))
				return false;
			QuestionKey qkObj = (QuestionKey) obj;
			return qkObj.sQuestionID.equals(sQuestionID)
					&& qkObj.sVersion.equals(sVersion);
		}

		// For display in exceptions
		@Override
		public String toString() {
			return getURLPart();
		}

		/**
		 * @return filename for cache
		 */
		public String getFileName() {
			// return getURLPart()+".jar";
			return getURLPart() + fileExtension();
		}

		/**
		 * @return url fragment for this question.
		 */
		public String getURLPart() {
			return sQuestionID + "." + sVersion;
		}

	}

	/**
	 * Picks up the approiate QuestionLoader based on the
	 * QuestionCache.QuestionKey argument.
	 * 
	 * @param qk
	 * @return
	 * @author Trevor Hinson
	 */
	protected QuestionLoader getQuestionLoader(QuestionCache.QuestionKey qk) {
		QuestionLoader ql = questionLoaders
				.get(QuestionTypeEnum.jar.toString());
		if (qk.isDynamicQuestion()) {
			ql = questionLoaders.get(QuestionTypeEnum.dynamic.toString());
		}
		return ql;
	}

	/**
	 * Returns metadata for a particular question ID. Must not be called unless
	 * it is certain that the question is loaded (i.e. after newQuestion, before
	 * returnQuestion).
	 * 
	 * @param qk
	 *            Question key
	 * @return Metadata
	 * @throws OmException
	 *             If question isn't loaded
	 */
	synchronized Document getMetadata(QuestionKey qk) throws OmException {
		checkNotShutdown();
		// Find question ID in first map
		QuestionStuff qs = mActiveQuestions1.get(qk);
		if (qs == null) {
			// TODO Make it so it can cache the metadata somehow
			// Load question temporarily just for metadata
			File f = getFile(qk);
			if (!f.exists())
				throw new OmException("Question '" + qk + "' does not exist");
			if (f.getName().endsWith(DOT_OMXML)) {
				return DynamicQuestionUtils.metaDataFromDynamicQuestion(f);
			} else {
				try {
					JarFile jf = new JarFile(f);
					InputStream is = jf.getInputStream(jf
							.getEntry("question.xml"));
					Document dMeta = XML.parse(IO.loadBytes(is));
					jf.close();
					return dMeta;
				} catch (IOException ioe) {
					throw new OmException(
							"Error loading question metadata for: " + qk, ioe);
				}
			}
		}
		return qs.dMeta;
	}

	/**
	 * UPDATED.
	 * Note here that IF the contentType is known then we can derive the correct
	 *  question. However, as it found in the system IF the contentType has not
	 *  been set within the argument QuestionKey then we have to check both.
	 *  THIS does lead to a situation that BOTH can actually exist.  ie: there
	 *  could be both a question.1.1.jar and a question.1.1.omxml  In that
	 *  scenario we will return the .jar first as that is existing behaviour.
	 * 
	 * @param qk Question key
	 * @return File that does/would correspond to that question
	 */
	public File getFile(QuestionKey qk) {
		String filePrefix = qk.getURLPart();
		String contentType = qk.contentType;
		if (StringUtils.isNotEmpty(contentType)) {
			return new File(fFolder, qk.getFileName());
		} else {
			File f = new File(fFolder, filePrefix + DOT_JAR);
			if (!f.exists()) {
				f = new File(fFolder, filePrefix + DOT_OMXML);
				if (f.exists()) {
					qk.setContentType(dynamicQuestionType);
				}
			} else {
				qk.setContentType(jarQuestionType);
			}
			return f;
		}
	}

	/**
	 * Checks whether a question is in the cache.
	 * 
	 * @param qk
	 *            Question key
	 * @return True if it's in the cache, false if it needs to be obtained and
	 *         saved
	 */
	synchronized boolean containsQuestion(QuestionKey qk) {
		if (mActiveQuestions1 == null)
			return false; // If shutdown

		// If it's loaded in memory, we've got it, return true to save time
		if (mActiveQuestions1.containsKey(qk))
			return true;

		// Otherwise see if it exixsts
		return getFile(qk).exists();
	}

	/**
	 * Saves a newly-retrieved question into the cache so that it can be loaded
	 * with {@link #newQuestion(String)}.
	 * 
	 * @param qk
	 *            Question key
	 * @param abData
	 *            Data of question .jar file
	 * @throws OmException
	 *             If there's an error saving it
	 */
	synchronized void saveQuestion(QuestionKey qk, byte[] abData)
			throws OmException {
		checkNotShutdown();
		try {
			FileOutputStream fos = new FileOutputStream(getFile(qk));
			fos.write(abData);
			fos.close();
		} catch (IOException ioe) {
			throw new OmException("Failed to save question file", ioe);
		}
	}

	public Question getDynamicClass(QuestionKey qk, QuestionStuff qs)
			throws Exception {
		// String className = qs.c.toString().substring("class ".length(),
		// qs.c.toString().length());
		if (qs.omclc instanceof DynamicOMClassLoader) {
			if (StringUtils.isNotEmpty(qk.contentType)) {
				qk.contentType = QuestionCache.dynamicQuestionType;
			}
		}
//		ClassLoader cl = null;
//		if (qk.isDynamicQuestion()) {
//			cl = (DynamicOMClassLoader) qs.omclc;
//		} else {
//			cl = (ClosableClassLoader) qs.omclc;
//		}
		Object obj = qs.c.newInstance();
		return (Question) obj;
		// return (Question) Class.forName(className,true, cl).newInstance();
	}

	/**
	 * Returns new instance of a question with the given question ID. Question
	 * has not yet been initialised, only constructed.
	 * <p>
	 * The question jar file is loaded if necessary.
	 * 
	 * @param qk
	 *            Question key
	 * @return New Question object
	 * @throws OmException
	 *             If various problems with the .jar occur
	 */
	synchronized QuestionInstance newQuestion(QuestionKey qk)
			throws OmException {
		checkNotShutdown();
		// Find question ID in first map
		QuestionStuff qs = mActiveQuestions1.get(qk);
		if (qs == null) {
			qs = new QuestionStuff();
		}
		// If we don't already have the class loaded, we need to load it
		loadQuestionsClassLoader(qs, qk);
		mActiveQuestions1.put(qk, qs);
		// Instantiate question
		Question q;
		boolean bSuccess = false;
		try {
			q = getDynamicClass(qk, qs);
			bSuccess = true;
		} catch (ClassCastException cce) {
			throw new OmDeveloperException("Class " + qs.c
					+ " doesn't implement Question, in: " + qk);
		} catch (InstantiationException ie) {
			throw new OmException("Error instantiating " + qs.c + " in: " + qk,
					ie);
		} catch (IllegalAccessException iae) {
			throw new OmException("Error instantiating " + qs.c
					+ " (check it's public) in: " + qk, iae);
		} catch (Throwable t) {
			throw new OmException("Error instantiating " + qs.c + " in: " + qk,
					t);
		} finally {
			// If no other questions were already instantiated, throw away the
			// classloader too
			if (!bSuccess && qs.lActive.isEmpty()) {
				// mActiveQuestions1.remove(qk);
				// qs.omclc.close();
			}
		}

		// Add question to both directional maps
		qs.lActive.add(q);
		mActiveQuestions2.put(q, qk);

		// Return question
		QuestionInstance qi = new QuestionInstance();
		qi.q = q;
		qi.omclc = qs.omclc;
		return qi;
	}

	void loadQuestionsClassLoader(QuestionStuff qs, QuestionCache.QuestionKey qk)
			throws OmException {
		if (qs.c == null) {
			File f = getFile(qk);
			if (!f.exists()) {
				throw new OmException("Question '" + qk + "' does not exist");
			}
			qs.omclc = determineClassLoader(f, qk);
			QuestionLoader qLoader = getQuestionLoader(qk);
			boolean bSuccess = false;
			try {
				qLoader.loadMetaData(qs, f);
				qLoader.loadClass(qs, f);
				bSuccess = true;
			} finally {
				if (!bSuccess)
					qs.omclc.close();
			}
		}
	}

	/**
	 * Creates the appropriate ClassLoader for the based on the type of Question
	 * being requested.
	 * 
	 * @param qk
	 * @param f
	 * @return
	 * @throws IOException
	 * @author Trevor Hinson
	 */
	protected OmClassLoaderContract determineClassLoader(File f,
			QuestionCache.QuestionKey qk) throws OmException {
		return null != qk ? qk.isDynamicQuestion() ? newDynamicOMClassLoader(f,
				qk) : newClosableClassLoader(f, qk) : null;
	}

	protected DynamicOMClassLoader newDynamicOMClassLoader(File f,
			QuestionCache.QuestionKey qk) throws OmException {
		try {
			return new DynamicOMClassLoader(classPath, getClass()
					.getClassLoader());
		} catch (IOException x) {
			throw new OmException(FAILED_CLASSLOADER_MESSAGE + f, x);
		}
	}

	protected ClosableClassLoader newClosableClassLoader(File f,
			QuestionCache.QuestionKey qk) throws OmException {
		try {
			return new ClosableClassLoader(f, getClass().getClassLoader());
		} catch (IOException x) {
			throw new OmException(FAILED_CLASSLOADER_MESSAGE + f, x);
		}
	}

	/**
	 * Puts a question back in the bank once it's finished with. Call this after
	 * you're sure there are no other references to the question. All questions
	 * <strong>must</strong> be closed if newQuestion returned successfully,
	 * otherwise it won't unload the jar file.
	 * 
	 * @param q
	 *            Question to return to the bank.
	 * @throws OmException
	 *             If the data structures are inconsistent
	 */
	synchronized void returnQuestion(Question q) throws OmException {
		checkNotShutdown();
		// Remove from both directional maps
		QuestionKey qk = mActiveQuestions2.remove(q);
		if (qk == null)
			throw new OmException(
					"Attempt to close question that wasn't currently open");
		QuestionStuff qs = mActiveQuestions1.get(qk);
		if (qs == null)
			throw new OmException("Question maps inconsistent (missing list)");
		if (!qs.lActive.remove(q))
			throw new OmException(
					"Question maps inconsistent (missing question)");
		if (!qs.lActive.isEmpty())
			return; // If it wasn't the last, we're done
		mActiveQuestions1.remove(qk);
		qs.omclc.close();
	}

	/** Abandon all questions and close all classloaders */
	synchronized void shutdown() {
		for (QuestionStuff qs : mActiveQuestions1.values()) {
			((ClosableClassLoader) qs.c.getClassLoader()).close();
			qs.c = null;
		}
		mActiveQuestions1 = null;
		mActiveQuestions2 = null;
	}

	/**
	 * @throws OmException
	 *             If the question cache was shut down
	 */
	private void checkNotShutdown() throws OmException {
		if (mActiveQuestions1 == null)
			throw new OmException(
					"Can't call question cache methods after shutdown");
	}

}
