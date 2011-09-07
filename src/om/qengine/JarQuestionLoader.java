package om.qengine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import om.OmDeveloperException;
import om.OmException;
import om.qengine.QuestionCache.QuestionStuff;
import om.question.Question;

import org.w3c.dom.Element;

import util.xml.XML;

/**
 * The implementation of the QuestionLoader methods here have been re-factored
 *  from the QuestionCache to here.
 * 
 * @author Trevor Hinson
 */

public class JarQuestionLoader implements QuestionLoader {

	public void loadMetaData(QuestionStuff qs, File f) throws OmException {
		try {
			URL uXML=qs.omclc.identifyResource("question.xml");
			if(uXML==null)
				throw new OmDeveloperException("question.xml not present in: " + f);
			InputStream is=uXML.openStream();
			qs.dMeta=XML.parse(is);
			is.close();
		} catch(IOException ioe) {
			throw new OmDeveloperException(
				"Failed to load or parse question.xml in: " + f, ioe);
		}		
	}

	public void loadClass(QuestionStuff qs, File f) throws OmException {
		Element eRoot=qs.dMeta.getDocumentElement();
		if(!eRoot.getTagName().equals("question"))
			throw new OmDeveloperException(
				"Expecting <question> as root of question.xml in: " + f);
		if(!eRoot.hasAttribute("class"))
			throw new OmDeveloperException(
				"Expecting class= attribute on root of question.xml in: " + f);
		String sClass=eRoot.getAttribute("class");
		// Load class
		try {
			qs.c = qs.omclc.identifyClass(sClass);
		} catch(ClassNotFoundException cnfe) {
			throw new OmDeveloperException("Failed to find "+sClass+" in: " + f);
		}		
	}

	@Override
	public Question load(QuestionStuff qs) throws OmException {
		return null;
	}

}
