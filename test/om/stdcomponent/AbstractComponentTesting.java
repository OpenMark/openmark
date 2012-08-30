package om.stdcomponent;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import om.AbstractTestCase;
import om.OmDeveloperException;
import om.helper.QEngineConfig;
import om.question.InitParams;
import om.stdquestion.StandardQuestion;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.misc.ClosableClassLoader;
import util.xml.XML;

public class AbstractComponentTesting extends AbstractTestCase {

	protected QuestionHolder getQuestionHolder(File fJar, InitParams ips)
		throws Exception {
		assertNotNull(fJar);
		ClosableClassLoader ccl = new ClosableClassLoader(fJar,
			getClass().getClassLoader());
		URL uXML = ccl.getResource("question.xml");
		assertNotNull(uXML);
		InputStream is=uXML.openStream();
		Document doc = XML.parse(is);
		is.close();
		assertNotNull(doc);
		String className = extractClassName(fJar, doc);
		assertNotNull(className);
		Class<?> cla = ccl.loadClass(className);
		assertNotNull(cla);
		Object obj = cla.newInstance();
		assertTrue(obj instanceof StandardQuestion);
		StandardQuestion sq = (StandardQuestion) obj;
		sq.init(doc, ips);
		return new QuestionHolder(doc, sq);
	}

	protected String extractClassName(File fJar, Document doc) throws Exception {
		Element eRoot=doc.getDocumentElement();
		if(!eRoot.getTagName().equals("question"))
			throw new OmDeveloperException(
				"Expecting <question> as root of question.xml in: "+fJar);
		if(!eRoot.hasAttribute("class"))
			throw new OmDeveloperException(
				"Expecting class= attribute on root of question.xml in: "+fJar);
		return eRoot.getAttribute("class");
	}

	class QuestionHolder {
		
		StandardQuestion question;
		
		Document document;
		
		QuestionHolder(Document doc, StandardQuestion q) {
			document = doc;
			question = q;
		}

	}

	protected InitParams newInitParams() {
		long lRandomSeed = System.currentTimeMillis();
		String sFixedFG = "#00ff00";
		String sFixedBG = "#00ff00";
		double dZoom = 2.0;
		boolean bPlain = false;
		ClassLoader cl = null;
		int iFixedVariant = 0;
		QEngineConfig engineConfig = null;
		int attempt = 1;
		String navigatorVersion = "1.14";
		boolean readOnly = false;
		boolean showFeedback = true;
		return new InitParams(lRandomSeed,sFixedFG,sFixedBG,dZoom,bPlain,
			cl,iFixedVariant,engineConfig, attempt, navigatorVersion, readOnly, showFeedback);
		
	}

}
