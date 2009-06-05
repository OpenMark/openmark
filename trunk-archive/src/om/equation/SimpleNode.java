/* OpenMark online assessment system
	 Copyright (C) 2007 The Open University

	 This program is free software; you can redistribute it and/or
	 modify it under the terms of the GNU General Public License
	 as published by the Free Software Foundation; either version 2
	 of the License, or (at your option) any later version.

	 This program is distributed in the hope that it will be useful,
	 but WITHOUT ANY WARRANTY; without even the implied warranty of
	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
	 GNU General Public License for more details.

	 You should have received a copy of the GNU General Public License
	 along with this program; if not, write to the Free Software
	 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA	02110-1301, USA.
 */
package om.equation;
import java.util.*;

import om.equation.generated.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.xml.XML;

/**
 * Represents parts of equations that are just bits of text.
 */
public class SimpleNode implements Node {
	protected Node parent;
	protected Node[] children;
	protected int id;
	protected EquationFormat parser;

	// sam added:
	private final static Set<String> STANDARDFUNCTIONS=new HashSet<String>(
		Arrays.asList(new String[]
		{
			// List from p. 124 of 'A Guide To LaTeX 2e' 2nd ed (tim's book)
			"arccos","arcsin","arctan","arg","cos","cosh","cot","coth","csc",
			"deg","det","dim","exp","gcd","hom","inf","ker","lg","lim","liminf",
			"limsup","ln","log","max","min","Pr","sec","sin","sinh","sup","tan","tanh"
		}));

	private final static Map<String, String> STANDARDSYMBOLS;
	private final static String[] STANDARDSYMBOLS_ARRAY=
	{
		// Lists from p. 121 of above text
		// Greek lower
		"alpha","\u03b1",
		"beta","\u03b2",
		"gamma","\u03b3",
		"delta","\u03b4",
		"epsilon","\u03b5",//"\u03f5", - LaTeX really wants that symbol but it's not in TNR
		"varepsilon","\u03b5",
		"zeta","\u03b6",
		"eta","\u03b7",
		"theta","\u03b8",
		"vartheta","\u03b8",//"\u03d1", - Not in TNR
		"iota","\u03b9",
		"kappa","\u03ba",
		"lambda","\u03bb",
		"mu","\u03bc",
		"nu","\u03bd",
		"xi","\u03be",
		"pi","\u03c0",
		"varpi","\u03c0",//"\u03d6", - Not in TNR
		"rho","\u03c1",
		"varrho","\u03c1",//"\u03f1", - Not in TNR
		"sigma","\u03c3",
		"varsigma","\u03c2",
		"tau","\u03c4",
		"upsilon","\u03c5",
		"phi","\u03c6",//"\u03d5", - Not in TNR
		"varphi","\u03c6",
		"chi","\u03c7",
		"psi","\u03c8",
		"omega","\u03c9",
		// Greek upper
		"Gamma","\u0393",
		"Delta","\u0394",
		"Theta","\u0398",
		"Lambda","\u039b",
		"Xi","\u039e",
		"Pi","\u03a0",
		"Sigma","\u03a3",
		"Upsilon","\u03a5",
		"Phi","\u03a6",
		"Psi","\u03a8",
		"Omega","\u03a9",
	};
	static
	{
		HashMap<String, String> hm=new HashMap<String, String>();
		for(int i=0;i<STANDARDSYMBOLS_ARRAY.length;i+=2)
		{
			hm.put(STANDARDSYMBOLS_ARRAY[i],STANDARDSYMBOLS_ARRAY[i+1]);
		}
		STANDARDSYMBOLS=hm;
	}


	private String sContent=null;
	private String sName=null;
	private List<String[]> lAttributes=new LinkedList<String[]>();
	/**
	 * @param sContent the content to set on this node.
	 */
	public void setContent(String sContent) { this.sContent=sContent; }
	/**
	 * @param sName the name for this node.
	 */
	public void setName(String sName) { this.sName=sName; }
	/**
	 * @param sAttribute
	 * @param sName
	 */
	public void setAttribute(String sAttribute,String sName) {
		lAttributes.add(new String[]{sAttribute,sName});
	}
	private String getName() { return sName!=null ? sName : toString(); }
	/**
	 * @param d
	 * @return this node's name.
	 */
	public Element createDOM(Document d)
	{
		return createDOM(d,false);
	}
	/**
	 * @param d
	 * @param bIncludeWhitespace
	 * @return a DOM fragment for this node.
	 */
	public Element createDOM(Document d,boolean bIncludeWhitespace)
	{
		Element e=d.createElement(getName());
		bIncludeWhitespace|=getName().equals("mbox");

		if(sContent!=null)
			e.appendChild(d.createTextNode(sContent));
		for(String[] as : lAttributes)
		{
			e.setAttribute(as[0],as[1]);
		}

		String sTextNodeBuildup="";
		boolean bWhitespaceBefore=false,bBuildupWhitespaceBefore=false;
		for(int i=0;i<jjtGetNumChildren();i++)
		{
			SimpleNode sn=(SimpleNode)jjtGetChild(i);
			if(sn.getName().equals("int_whitespace") && !bIncludeWhitespace)
			{
				bWhitespaceBefore=true;
				continue;
			}

			Element eChild=sn.createDOM(d,bIncludeWhitespace);

			// Replace standard functions
			String sChildName=eChild.getTagName();
			if(STANDARDFUNCTIONS.contains(sChildName))
			{
				eChild=d.createElement("mbox");

				String sSpacer="";
				if(i+1 < jjtGetNumChildren())
				{
					SimpleNode snNext=(SimpleNode)jjtGetChild(i+1);
					// Even if we're not including whitespace, add whitespace after this
					// if there is one
					if(snNext.getName().equals("int_whitespace") && !bIncludeWhitespace)
					{
						sSpacer=" ";
					}
				}

				XML.createText(eChild,"int_text",sChildName + sSpacer);
			}
			else if(STANDARDSYMBOLS.containsKey(sChildName))
			{
				eChild=d.createElement("int_text");
				XML.createText(eChild,STANDARDSYMBOLS.get(sChildName));
			}

			// Coagulate text nodes that appear as siblings
			if(sChildName.equals("int_text") || sChildName.equals("int_whitespace"))
			{
				if(sTextNodeBuildup.equals(""))
					bBuildupWhitespaceBefore=bWhitespaceBefore;
				sTextNodeBuildup+=XML.getText(eChild);
				bWhitespaceBefore=false;
			}
			else
			{
				if(sTextNodeBuildup.length()>0)
				{
					addText(d,e,sTextNodeBuildup,bBuildupWhitespaceBefore);
					bBuildupWhitespaceBefore=false;
					sTextNodeBuildup="";
				}
				if(bWhitespaceBefore)
					eChild.setAttribute("whitespacebefore","yes");
				e.appendChild(eChild);
				bWhitespaceBefore=false;
			}

			if(sn.getName().equals("int_whitespace"))
				bWhitespaceBefore=true;
		}
		if(sTextNodeBuildup.length()>0)
		{
			addText(d,e,sTextNodeBuildup,bBuildupWhitespaceBefore);
			sTextNodeBuildup="";
		}

		return e;
	}
	private static void addText(Document d,Element e,String sText,boolean bWhitespaceBefore)
	{
		Element eText=d.createElement("int_text");
		if(bWhitespaceBefore)
			eText.setAttribute("whitespacebefore","yes");
		eText.appendChild(d.createTextNode(sText));
		e.appendChild(eText);
	}

	/**
	 * @param i
	 */
	public SimpleNode(int i) {
		id = i;
	}

	/**
	 * @param p
	 * @param i
	 */
	public SimpleNode(EquationFormat p, int i) {
		this(i);
		parser = p;
	}

	public void jjtOpen() {
	}

	public void jjtClose() {
	}

	public void jjtSetParent(Node n) { parent = n; }
	public Node jjtGetParent() { return parent; }

	public void jjtAddChild(Node n, int i) {
		if (children == null) {
			children = new Node[i + 1];
		} else if (i >= children.length) {
			Node c[] = new Node[i + 1];
			System.arraycopy(children, 0, c, 0, children.length);
			children = c;
		}
		children[i] = n;
	}

	public Node jjtGetChild(int i) {
		return children[i];
	}

	public int jjtGetNumChildren() {
		return (children == null) ? 0 : children.length;
	}

	/* You can override these next two methods in subclasses of SimpleNode to
		 customize the way the node appears when the tree is dumped.	If
		 your output uses more than one line you should override
		 toString(String), otherwise overriding toString() is probably all
		 you need to do. */

	@Override
	public String toString() { return EquationFormatTreeConstants.jjtNodeName[id]; }
	/**
	 * @param prefix used to indent the tree structure display.
	 * @return multiline string representation of this item.
	 */
	public String toString(String prefix) { return prefix + toString(); }

	/**
	 * Dump this node and it's children for debugging.
	 *
	 * Override this method if you want to customize how the node dumps
	 * out its children.
	 * @param prefix used to indent the tree structure display.
	 */
	public void dump(String prefix) {
		System.out.println(toString(prefix));
		if (children != null) {
			for (int i = 0; i < children.length; ++i) {
	SimpleNode n = (SimpleNode)children[i];
	if (n != null) {
		n.dump(prefix + " ");
	}
			}
		}
	}
}

