package util.misc;

import java.io.File;
import java.io.IOException;

import om.OmException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.xml.XML;

public class DynamicQuestionUtils {

	public static String QUESTION = "question";

	public static String UTF_8 = "UTF-8";

	public static boolean isDynamicQuestion(Document d) {
		boolean is = false;
		if (null != d) {
			is = XML.hasChild(d, "dynamic-question");
		}
		return is;
	}

	/**
	 * Creates the Question part as a Document from the Dynamic Question file
	 *  argument provided.
	 *  
	 * @param f
	 * @return
	 * @throws OmException
	 * @author Trevor Hinson
	 */
	public static Document metaDataFromDynamicQuestion(File f) throws OmException {
		Document doc = null;
		if (null != f) {
			try {
				Element e = retrieveElement(f, QUESTION);
				doc = XML.createDocument();
				doc.appendChild(doc.importNode(e, true));
			} catch (DOMException x) {
				throw new OmException(x);
			}
		}
		return doc;
	}

	/**
	 * Looks for a particular Element from within the provided file and returns.
	 * 
	 * @param f
	 * @param name
	 * @return
	 * @throws OmException
	 * @author Trevor Hinson
	 */
	public static Element retrieveElement(File f, String name)
		throws OmException {
		Element e = null;
		if ((null != f ? f.exists() : false) && StringUtils.isNotEmpty(name)) {
			try {
				String s = FileUtils.readFileToString(f, UTF_8);
				Document parent = XML.parse(s.getBytes());
				Node node = parent.getFirstChild();
				Node n = XML.getChild(node, name);
				if (null != n ? Node.ELEMENT_NODE == n.getNodeType() : false) {
					e = (Element) n; 
				}
			} catch (IOException x) {
				throw new OmException(x);
			}			
		}
		return e;
	}

}
