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
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.jeuclid.MutableLayoutContext;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.context.Parameter;
import net.sourceforge.jeuclid.converter.Converter;
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
 * as the rendering is done on server side using JEuclid.
 * Writing MathML for complex equations can be tedious; TeX to MathML converters
 * or other known MathML authoring tools can be used to generate MathML. 
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

			context.setParameter(Parameter.FONTS_SERIF, serifFonts);
			context.setParameter(Parameter.MATHSIZE, Float
					.valueOf((float) (DEFAULT_FONTSIZE * dZoom)));
			context.setParameter(Parameter.MATHBACKGROUND, cBackground);
			context.setParameter(Parameter.MATHCOLOR, cForeground);

			// Hash to filename/identifier
			String sFilename = "meq" + (currentEq.hashCode() + cBackground.hashCode() * 3
									 + cForeground.hashCode() * 7 
									 + (new Double(dZoom)).hashCode() * 11) + ".png";

			// get a new xml document builder factory to build an xml document
			// for the mathml
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// the xml document to contain the mathml
			Document document;
			// the image to return
			BufferedImage image;
			try {
				// init a new xml document
				DocumentBuilder builder = factory.newDocumentBuilder();
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
