package om.tnavigator.scores;

import om.OmFormatException;

import org.w3c.dom.Element;

import util.xml.XML;
import util.xml.XMLException;

/**
 * Information about a part of a remapping, indicating that an
 * input score of X out of Y on fromAxis in the input should contribute
 * a score of (X*newMax/Y) out of newMax on axis toAxis in the output.
 */
class RemappingPart
{
	/** Axis that marks came from (null if default) */
	private String fromAxis = null;

	/** New total marks range */
	private double newMax;

	/** Axis that marks go to */
	private String toAxis = null;

	/**
	 * @param fromAxis
	 * @param newMax
	 * @param toAxis
	 */
	RemappingPart(String fromAxis, double newMax, String toAxis) {
		this.fromAxis = fromAxis;
		this.newMax = newMax;
		this.toAxis = toAxis;
	}

	/**
	 * Construct from a given rescore tag in a test definition.
	 * @param e XML element
	 * @throws OmFormatException If any attributes are invalid
	 */
	static RemappingPart fromXML(Element e) throws OmFormatException
	{
		String to = null;
		String from = null;
		if (e.hasAttribute("axis"))
		{
			to = e.getAttribute("axis");
			if(to.equals("")) to = null;
			from = to; // Make fromaxis default to axis
		}
		if (e.hasAttribute("fromaxis"))
		{
			from = e.getAttribute("fromaxis");
			if(from.equals("")) from = null;
		}
		try
		{
			double max = Double.parseDouble(XML.getRequiredAttribute(e, "marks"));
			if (max < 0) throw new OmFormatException(
					"<rescore> - Invalid negative number for marks=: "+e.getAttribute("marks"));
			return new RemappingPart(from, max, to);
		}
		catch(NumberFormatException nfe)
		{
			throw new OmFormatException("<rescore> - Invalid number for marks=: "+e.getAttribute("marks"));
		}
		catch(XMLException xe)
		{
			throw new OmFormatException("<rescore> - Must have a marks= attribute");
		}
	}

	/**
	 * @return the fromAxis
	 */
	String getFromAxis()
	{
		return fromAxis;
	}

	/**
	 * @return the newMax
	 */
	double getNewMax()
	{
		return newMax;
	}

	/**
	 * @return the toAxis
	 */
	String getToAxis()
	{
		return toAxis;
	}

	/**
	 * Adds the mark contribution from this rescore to the new value.
	 * @param newScore New partial score we're building up
	 * @param baseScore Base (source)
	 * @throws OmFormatException if the baseScore does not contain the score for the fromAxis.
	 */
	void rescore(CombinedScore newScore, CombinedScore baseScore) throws OmFormatException
	{
		newScore.add(toAxis, baseScore.getScore(fromAxis)*newMax/baseScore.getMax(fromAxis),
				newMax);
	}
}