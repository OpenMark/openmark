package util.misc;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.xml.XML;

public class LabelSets
{
	
	private File containingFolder;
	/** Cache label replacement (Map of String (labelset id) -> Map ) */
	protected Map<String, Map<String, String>> labelReplace = new HashMap<String, Map<String, String>>();

	public LabelSets(File containingFolder)
	{
		this.containingFolder = containingFolder;
	}

	/**
	 * Returns a given map of label replacements.
	 * 
	 * @param label set name.
	 * @return Map of replacements (don't change this)
	 * @throws IOException Any problems loading it
	 */
	public Map<String, String> getLabelSet(String labelSet) throws IOException
	{
		// Get from cache
		Map<String, String> mLabels = labelReplace.get(labelSet);
		if (mLabels != null)
		{
			return mLabels;
		}

		// Load from file
		Map<String, String> m = new HashMap<String, String>();
		File f = new File(containingFolder, labelSet + ".xml");
		if (!f.exists())
		{
			throw new IOException("Unable to find requested label set: " + labelSet);
		}
		Document d = XML.parse(f);
		Element[] aeLabels = XML.getChildren(d.getDocumentElement());
		for (int i = 0; i < aeLabels.length; i++) {
			m.put(XML.getRequiredAttribute(aeLabels[i], "id"), XML
					.getText(aeLabels[i]));
		}
		m = Collections.unmodifiableMap(m);

		// Cache and return
		labelReplace.put(labelSet, m);
		return m;
	}
}
