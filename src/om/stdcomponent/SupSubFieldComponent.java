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
package om.stdcomponent;

import java.util.Arrays;

import om.OmDeveloperException;
import om.OmException;
import om.OmFormatException;
import om.OmUnexpectedException;
import om.question.ActionParams;
import om.stdquestion.QComponent;
import om.stdquestion.QContent;
import om.stdquestion.QDocument;

import org.w3c.dom.Element;

import util.misc.Strings;
import util.xml.XML;
import util.xml.XMLException;

/**
 * A text field that can allow the user to enter superscripts and subscripts.<br/>
 * The <b>plain</b> text version of the control asks the user to type the sub
 * and sup tags themselves. <br/>
 *
 * <h2>XML usage</h2> &lt;supsubfield id='reaction' type='superscript' /&gt;
 * <h2>Properties</h2>
 * <table border="1">
 * <tr>
 * <th>Property</th>
 * <th>Values</th>
 * <th>Effect</th>
 * </tr>
 * <tr>
 * <td>id</td>
 * <td>(string)</td>
 * <td>Specifies unique ID</td>
 * </tr>
 * <tr>
 * <td>display</td>
 * <td>(boolean)</td>
 * <td>Includes in/removes from output</td>
 * </tr>
 * <tr>
 * <td>enabled</td>
 * <td>(boolean)</td>
 * <td>Activates/deactivates this control</td>
 * </tr>
 * <tr>
 * <td>lang</td>
 * <td>(string)</td>
 * <td>Specifies the language of the content, like the HTML lang attribute. For
 * example 'en' = English, 'el' - Greek, ...</td>
 * </tr>
 * <tr>
 * <td>cols</td>
 * <td>(integer)</td>
 * <td>Number of columns (approx) to allow space for the component</td>
 * </tr>
 * <tr>
 * <td>value</td>
 * <td>(string)</td>
 * <td>Current value of field. (See below)</td>
 * </tr>
 * <tr>
 * <td>type</td>
 * <td>(string; 'superscript' | 'subscript' | 'both')</td>
 * <td>Type of field.</td>
 * </tr>
 * </table>
 **/
public class SupSubFieldComponent extends QComponent implements Labelable
{

	/** The possible values for the type attribute. */
	enum Type
	{
		superscript, both, subscript
	}

	/** Maximum length of single line */
	private final static int MAXCHARS = 100;

	/** Property name for value of editfield */
	public final static String PROPERTY_VALUE = "value";
	/** Number of columns */
	public static final String PROPERTY_COLS = "cols";
	/** Label text */
	public static final String PROPERTY_LABEL = "label";
	/** Type of editfield. "superscript", "subscript" or "both". */
	public final static String PROPERTY_TYPE = "type";

	private boolean bGotLabel = false;

	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "supsubfield";
	}

	@Override
	protected String[] getRequiredAttributes()
	{
		return new String[] { PROPERTY_TYPE };
	}

	@Override
	protected void defineProperties() throws OmDeveloperException
	{
		super.defineProperties();
		defineString(PROPERTY_VALUE);
		defineInteger(PROPERTY_COLS);
		defineString(PROPERTY_LABEL);
		defineString(PROPERTY_TYPE, Strings.join("|", Arrays.asList(Type.values())));
		setString(PROPERTY_VALUE, "");
		setInteger(PROPERTY_COLS, 20);
	}

	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		if (eThis.getFirstChild() != null)
			throw new OmFormatException(
					"<supsubfield> may not contain other content");
	}

	@Override
	public void produceVisibleOutput(QContent qc, boolean bInit, boolean bPlain)
			throws OmException
	{

		if (!(bGotLabel || isPropertySet(PROPERTY_LABEL)))
		{
			throw new OmFormatException("<supsubfield> " + getID()
					+ ": Requires <label> or label=");
		}

		if (bPlain)
		{
			generateVisibleOutputForPlainDisplay(qc);
		}
		else
		{
			generateVisibleOutput(qc);
		}
	}

	protected Type getElementType() throws OmException
	{
		return Type.valueOf(getString(PROPERTY_TYPE));
	}

	protected void generateVisibleOutput(QContent qc)
			throws OmException
	{
		double dZoom = getQuestion().getZoom();

		Element eDiv = qc.createElement("div");
		qc.addInlineXHTML(eDiv);
		String classname = "supsubfield";
		if (!isEnabled())
		{
			classname += " readonly";
		}
		eDiv.setAttribute("class", classname);

		outputTextArea(qc, eDiv, dZoom);
	}

	protected void outputTextArea(QContent qc, Element eDiv, double dZoom)
			throws OmException
	{
		String elementName = QDocument.ID_PREFIX + QDocument.VALUE_PREFIX + getID();

		// Don't use a text area or editor when question is read only.
		if (!isEnabled())
		{
			double spacerHeight = 30 * dZoom;
			int pixelHeight = (int) (60 * dZoom);
			int pixelWidth = (int) (10 * dZoom * getInteger(PROPERTY_COLS));
			Element div = qc.createElement("div");
			div.setAttribute("style", "height:" + spacerHeight + "px");
			div.setAttribute("class", "answerSpacer");
			eDiv.appendChild(div);
			eDiv.setAttribute("style", "width:" + pixelWidth + "px");
			Element span = qc.createElement("span");
			try {
				XML.importChildren(span, XML.parse("<div>" + getValue() + "</div>").getDocumentElement());
			} catch (XMLException e) {
				throw new OmException(e);
			}
			span.setAttribute("class", "answer");
			span.setAttribute("style", "height:" + (pixelHeight - 23) + "px");
			eDiv.appendChild(span);
			addLangAttributes(span);

			Element eHidden = qc.createElement("input");
			eHidden.setAttribute("type", "hidden");
			eHidden.setAttribute("name", elementName);
			eHidden.setAttribute("id", elementName);
			eHidden.setAttribute("value", getString(PROPERTY_VALUE));
			div.appendChild(eHidden);

			return;
		}

		Element textarea = qc.createElement("textarea");
		textarea.setAttribute("id", elementName);
		textarea.setAttribute("name", elementName);
		textarea.setAttribute("rows", "" + 2);
		textarea.setAttribute("cols", "" + getInteger(PROPERTY_COLS));
		textarea.setTextContent(getValue());
		eDiv.appendChild(textarea);
		addLangAttributes(textarea);

		Element eScript = qc.createElement("script");
		XML.createText(eScript,
				  "if (!document.getElementById('ousupsubloader')) {\n"
				+ "  var scripttag = document.createElement('script');\n"
				+ "  scripttag.setAttribute('type', 'text/javascript');\n"
				+ "  scripttag.setAttribute('id', 'ousupsubloader');\n"
				+ "  scripttag.setAttribute('src', '%%SHAREDRESOURCE:SUPSUB%%/ousupsub.js');\n"
				+ "  document.getElementsByTagName('body')[0].appendChild(scripttag);\n"
				+ "}\n"
				+ "addOnLoad(function() {\n"
				+ "  editor_ousupsub.createEditorSimple('" + elementName + "', '" + getElementType() + "');\n"
				+ "});");
		eDiv.appendChild(eScript);

		// Can be focused (hopefully)
		if (isEnabled())
		{
			qc.informFocusable(elementName + "editable", false);
		}
	}

	protected void generateVisibleOutputForPlainDisplay(QContent qc) throws OmException {
		Type type = getElementType();

		if (isPropertySet(PROPERTY_LABEL) && !getString(PROPERTY_LABEL).equals(""))
		{
			Element eLabel = qc.createElement("label");
			qc.addInlineXHTML(eLabel);
			eLabel.setAttribute("for", QDocument.ID_PREFIX + QDocument.VALUE_PREFIX + getID());
			XML.createText(eLabel, getString(PROPERTY_LABEL));
		}

		Element eDiv = qc.createElement("div");
		qc.addInlineXHTML(eDiv);
		XML.createText(eDiv, "p", "(In the following edit field: "
				+ ((type == Type.superscript || type == Type.both)
						? " Type { before, and } after, any text you want superscripted."
						: "")
				+ ((type == Type.subscript || type == Type.both)
						? " Type [ before, and ] after, any text you want subscripted."
						: "")
				+ ")");

		Element eInput = XML.createChild(eDiv, "input");
		eInput.setAttribute("type", "text");
		eInput.setAttribute("size", "" + getInteger(PROPERTY_COLS));
		eInput.setAttribute("name", QDocument.ID_PREFIX + QDocument.VALUE_PREFIX + getID());
		eInput.setAttribute("id", QDocument.ID_PREFIX + QDocument.VALUE_PREFIX + getID());

		String sValue = getString(PROPERTY_VALUE);
		sValue = sValue.replaceAll("<sup>", "{");
		sValue = sValue.replaceAll("</sup>", "}");
		sValue = sValue.replaceAll("<sub>", "[");
		sValue = sValue.replaceAll("</sub>", "]");
		eInput.setAttribute("value", sValue);

		if (!isEnabled())
			eInput.setAttribute("disabled", "disabled");
		addLangAttributes(eInput);
	}

	/**
	 * Replaces something repeatedly until it's DEAD. (I think you might need
	 * this when replacing something where the source string might overlap with
	 * the next replacement.)
	 *
	 * @param sValue Original string
	 * @param sFind Thing to find
	 * @param sReplace Thing to replace
	 * @return Resulting string
	 */
	private static String replaceContinuous(String sValue, String sFind, String sReplace) {
		while (true)
		{
			String sNew = sValue.replaceFirst(sFind, sReplace);
			if (sNew.equals(sValue))
				return sNew;
			sValue = sNew;
		}
	}

	@Override
	protected void formSetValue(String sValue, ActionParams ap)
			throws OmException
	{
		// In plain mode they enter {} and []. We try hard to make it into valid
		// xhtml but uh, I don't guarantee it.
		if (ap.hasParameter("plain"))
		{
			// Replace double-opening {[ (nesting is not allowed)
			sValue = replaceContinuous(sValue, "(\\{[^}]*)\\{", "$1");
			sValue = replaceContinuous(sValue, "(\\[[^\\]]*)\\[", "$1");
			// Replace double-closing }]
			sValue = replaceContinuous(sValue, "(\\}[^{]*)\\}", "$1");
			sValue = replaceContinuous(sValue, "(\\][^\\[]*)\\]", "$1");

			// Untangle tangled groups { [ becomes { }[
			sValue = sValue.replaceAll("(\\{[^}]*)\\[", "$1}[");
			sValue = sValue.replaceAll("(\\[[^\\]]*)\\{", "$1]{");
			// Add missing closes
			sValue = sValue.replaceFirst("(\\{[^}]*)$", "$1}");
			sValue = sValue.replaceFirst("(\\[[^\\]]*)$", "$1]");
			// Remove extra closes
			sValue = sValue.replaceFirst("(\\{[^}]*)$", "$1}");
			sValue = sValue.replaceFirst("(\\[[^\\]]*)$", "$1]");

			// The above might mess up the first part again, so...

			// Replace double-opening {[ (nesting is not allowed)
			sValue = replaceContinuous(sValue, "(\\{[^}]*)\\{", "$1");
			sValue = replaceContinuous(sValue, "(\\[[^\\]]*)\\[", "$1");
			// Replace double-closing }]
			sValue = replaceContinuous(sValue, "(\\}[^{]*)\\}", "$1");
			sValue = replaceContinuous(sValue, "(\\][^\\[]*)\\]", "$1");

			// Replace with HTML tags
			sValue = sValue.replaceAll("\\[", "<sub>");
			sValue = sValue.replaceAll("\\{", "<sup>");
			sValue = sValue.replaceAll("\\]", "</sub>");
			sValue = sValue.replaceAll("\\}", "</sup>");
		}
		else
		{
			// firefox adds nbsp which should be replaced with space
			sValue = sValue.replaceAll("&nbsp;", " ");
			// uppercase tags
			sValue = sValue.replaceAll("<SUP>", "<sup>");
			sValue = sValue.replaceAll("</SUP>", "</sup>");
			sValue = sValue.replaceAll("<SUB>", "<sub>");
			sValue = sValue.replaceAll("</SUB>", "</sub>");
			// remove redundant pairs
			sValue = sValue.replaceAll("</sup><sup>|</sub><sub>", "");
			// need to save sup and sub
			sValue = sValue.replaceAll("<(sup|/sup|sub|/sub)>",
						"`om~$1~mo`");
			// remove all other tags
			sValue = sValue.replaceAll("<[^<]+>", "");
			// put sub and sup tags back again
			sValue = sValue.replaceAll("`om~(sup|/sup|sub|/sub)~mo`", "<$1>");

			sValue = sValue.replaceAll("&lt;", "<"); // in case typed literally
			sValue = sValue.replaceAll("&gt;", ">"); // in case typed literally
		}

		setString(PROPERTY_VALUE, trim(sValue, MAXCHARS));
	}

	/**
	 * @return Current value of edit field (may include HTML codes).
	 *      Trimmed to 100 characters.
	 */
	public String getValue()
	{
		try
		{
			return getString(PROPERTY_VALUE);
		}
		catch (OmDeveloperException e)
		{
			throw new OmUnexpectedException(e);
		}
	}

	/**
	 * @param sValue New value for edit field (may include HTML codes)
	 */
	public void setValue(String sValue)
	{
		try
		{
			setString(PROPERTY_VALUE, sValue);
		}
		catch (OmDeveloperException e)
		{
			throw new OmUnexpectedException(e);
		}
	}

	public String getLabelTarget(boolean bPlain) throws OmDeveloperException
	{
		if (isPropertySet(PROPERTY_LABEL))
		{
			throw new OmFormatException(
					"<supsubfield>: You cannot have both a label= and a <label> for the same field");
		}
		else
		{
			bGotLabel = true;
			if (bPlain) {
				return QDocument.ID_PREFIX + QDocument.VALUE_PREFIX + getID();
			}
			else
			{
				return QDocument.ID_PREFIX + QDocument.OM_PREFIX + getID() + "_iframe";
			}
		}
	}
}
