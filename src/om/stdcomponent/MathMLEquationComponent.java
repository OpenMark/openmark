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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import net.sourceforge.jeuclid.MutableLayoutContext;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.context.Parameter;
import net.sourceforge.jeuclid.converter.Converter;
import net.sourceforge.jeuclid.parser.Parser;
import om.OmDeveloperException;
import om.OmException;
import om.OmFormatException;
import om.stdquestion.QComponent;
import om.stdquestion.QContent;
import om.stdquestion.QDocument;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import util.xml.XML;

/**
 * Represents equations described in MathML format. Uses JEuclid to render
 * equations as images. MathML player plugins are not required for the browsers
 * as the rendering is done on server side using <a href="http://jeuclid.sourceforge.net/">JEuclid</a>.
 * <p>
 * Writing MathML for complex equations can be tedious; TeX to MathML converters
 * or other known MathML authoring tools can be used to generate MathML. 
 * </p>
 * <h2>XML usage</h2>
 * &lt;mequation&gt;
 * 	&lt;![CDATA[
 * 		&lt;math&gt;&lt;mi&gt;x&lt;/mi&gt;&lt;/math&gt;
	]]&gt;
 * &lt;/mequation&gt;
 * <p>
 * Note: Font size, colour and other style changes can be made using style attributes in MathML,
 * But, these style settings are strongly discouraged as they affect the accessibility of the question. 
 * </p>
 * <h2>Properties</h2>
 * <table border="1">
 * <tr><th>Property</th><th>Values</th><th>Effect</th></tr>
 * <tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
 * <tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
 * <tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates children</td></tr>
 * <tr><td>lang</td><td>(string)</td><td>Specifies the language of the content, like the HTML lang attribute. For example 'en' = English, 'el' - Greek, ...</td></tr>
 * <tr><td>alt</td><td>(string)</td><td>Alternative text for those who can't read the bitmap</td></tr>
 * </table>
 */
public class MathMLEquationComponent extends QComponent {

	/** String property: text alternative for equation */
	public final static String PROPERTY_ALT = "alt";

	/** Default font size for the eqaution. */
	private final static float DEFAULT_FONTSIZE = 16.0f;

	/** Default font family for the equation */
	private final static String DEFAULT_FONT_FAMILY = "Times New Roman";

	/** Actual content of required equation */
	private String equation;

	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName() {
		return "mequation";
	}

	public void setEquation(String equation) {
		this.equation = equation;
	}

	public String getEquation() {
		return equation;
	}

	@Override
	protected String[] getRequiredAttributes() {
		return new String[] { PROPERTY_ALT, };
	}

	@Override
	protected void defineProperties() throws OmDeveloperException {
		super.defineProperties();
		defineString(PROPERTY_ALT);
	}

	@Override
	protected void initChildren(Element eThis) throws OmException {
		Node child = eThis.getFirstChild();
		if (child != null) {
			if (child.getNextSibling() instanceof CDATASection) {
				equation = child.getNextSibling().getNodeValue();
			} else if (child instanceof Text) {
				equation = child.getNodeValue();
			}
		}
	}

	@Override
	protected void produceVisibleOutput(QContent qc, boolean init, boolean plain)
			throws OmException {

		if (equation == null) {
			throw new OmFormatException(
					"Missing MathML equation in <mequation>");
		}

		// Get the actual equation
		String currentEq = getQuestion().applyPlaceholders(equation);
		
		// If DOCTYPE is not provided, set the default.
		// This is required to process the MathML operators such as &PlusMinus;,&Integral; etc
		if (currentEq.indexOf("DOCTYPE") == -1) {
			currentEq = "<!DOCTYPE math PUBLIC \"-//W3C//DTD MathML 2.0//EN\" \"http://www.w3.org/TR/MathML2/dtd/mathml2.dtd\">"
					+ currentEq;
		}

		if (plain) {
			Element eDiv = qc.createElement("div");
			eDiv.setAttribute("style", "display:inline");
			qc.addInlineXHTML(eDiv);
			XML.createText(eDiv, getString("alt"));
			qc.addTextEquivalent(getString("alt"));
		} else {
			// Check background colour, foreground, and zoom
			Color cBackground = getBackground();
			if (cBackground == null) {
				cBackground = Color.white;
			}
			Color cForeground = getQuestion().isFixedColour() 
								? convertRGB(getQuestion().getFixedColourFG())
								: Color.black;
			double dZoom = getQuestion().getZoom();

			// get the default LayoutContext and set the parameters
			MutableLayoutContext context = (MutableLayoutContext) LayoutContextImpl
					.getDefaultLayoutContext();

			List<String> serifFonts = new ArrayList<String>();
			serifFonts.add(DEFAULT_FONT_FAMILY);
			serifFonts.add("DejaVu Serif");
			serifFonts.add("serif");

			context.setParameter(Parameter.FONTS_SERIF, serifFonts);
			context.setParameter(Parameter.MATHSIZE, Float
					.valueOf((float) (DEFAULT_FONTSIZE * dZoom)));
			context.setParameter(Parameter.MATHBACKGROUND, cBackground);
			context.setParameter(Parameter.MATHCOLOR, cForeground);

			// Hash to filename/identifier
			String sFilename = "meq" + (currentEq.hashCode() + cBackground.hashCode() * 3
									 + cForeground.hashCode() * 7 
									 + (new Double(dZoom)).hashCode() * 11) + ".png";
			
			// the xml document to contain the mathml
			Document document;
			// the image to return
			BufferedImage image;
			try {				
				// get JEuclid's MathML parser
				Parser mathMLParser = Parser.getInstance();
				// Retrieve a DocumentBuilder suitable for MathML parsing
				DocumentBuilder builder = mathMLParser.getDocumentBuilder();
				// parse the mathml
				document = builder.parse(new InputSource(new StringReader(currentEq)));	
			} catch (Exception e) {
				throw new OmFormatException("Invalid MathML (not well-formed)", e);
			}
			try {
				// get JEuclid to render the image from the xml document's
				// mathml into an image
				image = Converter.getInstance().render(document, context);
			} catch (Exception e) {
				throw new OmFormatException("Invalid MathML (JEuclid conversion failure)", e);
			}

			qc.addResource(sFilename, "image/png", QContent.convertPNG(image));

			Element eEnsureSpaces = qc.createElement("div");
			eEnsureSpaces.setAttribute("class", "mequation");
			qc.addInlineXHTML(eEnsureSpaces);

			String sImageID = QDocument.ID_PREFIX + getID() + "_img";
			Element eImg = XML.createChild(eEnsureSpaces, "img");
			eImg.setAttribute("id", sImageID);
			// Prevent Firefox drag/drop
			eImg.setAttribute("onmousedown", "return false;");
			eImg.setAttribute("src", "%%RESOURCES%%/" + sFilename);
			eImg.setAttribute("alt", getString("alt"));

			qc.addTextEquivalent(getString("alt"));

		}
	}

}
