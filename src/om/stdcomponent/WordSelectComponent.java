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

import java.util.*;

import om.OmDeveloperException;
import om.OmException;
import om.question.ActionParams;
import om.stdquestion.*;

import org.w3c.dom.*;

import util.xml.XML;

/**
Indents contained text or components.
<h2>XML usage</h2>
&lt;indent&gt;...&lt;/indent&gt;
<h2>Properties</h2>
<table border="1">
<tr><th>Property</th><th>Values</th><th>Effect</th></tr>
<tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
<tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
<tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates all children</td></tr>
</table>
*/
public class WordSelectComponent extends QComponent
{
	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
	public static String getTagName()
	{
		return "wordselect";
	}
	
	private static class Word
	{
		String word;
		String following;
		int id;
		boolean selected = false;

		private Word(String word, String following, int id) {
			this.word = word;
			this.following = following;
			this.id = id;
		}
	}

	private static class WordBlock
	{
		List<Word> words = new ArrayList<Word>();
		String preceding;
		String id;
		boolean isSecondHighlighted = false;
		boolean isSelectable = false;
		
		private WordBlock(String content, String id, boolean isSelectable, boolean isSecondHighlighted) {
			this.id = id;
			this.isSecondHighlighted = isSecondHighlighted;
			this.isSelectable = isSelectable;

			int i = 0;
			StringBuffer fragment = new StringBuffer();
			
			// Extract any non-word characters before the first word starts.
			while (i < content.length() && !isWordCharacter(content.charAt(i))) {
				fragment.append(content.charAt(i++));
			}
			preceding = fragment.toString();
			fragment.setLength(0);

			int wordIndex = 1;
			// Extract the words, followed by any non-word characters.
			while (i < content.length()) {
				// Extract a word.
				while (i < content.length() && isWordCharacter(content.charAt(i)))
				{
					fragment.append(content.charAt(i++));
				}
				String word = fragment.toString();
				fragment.setLength(0);

				// Extract any following non-word characters.
				while (i < content.length() && !isWordCharacter(content.charAt(i)))
				{
					fragment.append(content.charAt(i++));
				}
				words.add(new Word(word, fragment.toString(), wordIndex++));
				fragment.setLength(0);
			}
		}
	}

	private List<WordBlock> wordBlocks = new ArrayList<WordBlock>();
	private Map<String, WordBlock> wordsById = new HashMap<String, WordBlock>();

	@Override
	protected void initChildren(Element eThis) throws OmException
	{
		StringBuffer sbText=new StringBuffer();
		int idCounter = 1;
		for(Node n=eThis.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element e=(Element)n;
				if(e.getTagName().equals("sw"))
				{
					if(sbText.length()>0)
					{
						String id = "" + (idCounter++);
						WordBlock wb = new WordBlock(sbText.toString(), id, false, false);
						wordBlocks.add(wb);
						wordsById.put(id, wb);

						sbText.setLength(0);
					}
					String id;
					if (e.hasAttribute("id")) {
						id = e.getAttribute("id");
					} else {
						id = "" + (idCounter++);
					}
					WordBlock wb =new WordBlock(XML.getText(e), id, true, e.hasAttribute("highlight"));
					wordBlocks.add(wb);
					wordsById.put(id, wb);
				}
				else
				{
					throw new OmDeveloperException("<wordselect> can only contain <sw> tags");
				}
			}
			else if(n instanceof Text)
			{
				sbText.append(n.getNodeValue());
			}
		}
		if(sbText.length()>0)
		{
			String id = "" + (idCounter++);
			WordBlock wb = new WordBlock(sbText.toString(), id, false, false);
			wordBlocks.add(wb);
			wordsById.put(id, wb);

			sbText.setLength(0);
		}
	}

	private static boolean isWordCharacter(char c) {
		return Character.isLetterOrDigit(c);
	}

	private String makeCheckwordId(WordBlock wb, Word w) {
		return "_b" + wb.id + "_w" + w.id;
	}

	@Override
	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
	{
		for (WordBlock wb : wordBlocks)
		{
			if ("" != wb.preceding) {
				Element span = qc.createElement("span");
				XML.createText(span, wb.preceding);
				if (wb.isSecondHighlighted) {
					span.setAttribute("class","secondhilight");
				}
				qc.addInlineXHTML(span);
			}

			for(Word w : wb.words)
			{
				String labelclass = "";
				String checkwordID = makeCheckwordId(wb, w);
				Element input=qc.getOutputDocument().createElement("input");
				input.setAttribute("type","checkbox");
				input.setAttribute("class", "offscreen");
				input.setAttribute("name", QDocument.ID_PREFIX+"wordselectword_"+getID() + checkwordID);
				input.setAttribute("value", "1");
				input.setAttribute("onclick","wordOnClick('"+getID()+checkwordID+"','"+QDocument.ID_PREFIX+"');");
				input.setAttribute("id",QDocument.ID_PREFIX+"wordselectword_"+getID() + checkwordID);
				if (w.selected) {
					input.setAttribute("checked", "checked");
					labelclass = "selectedhilight ";
				}
				qc.addInlineXHTML(input);
				
				Element label=qc.getOutputDocument().createElement("label");
				label.setAttribute("for",QDocument.ID_PREFIX+"wordselectword_"+getID() + checkwordID);
				if (wb.isSecondHighlighted) {
					labelclass += "secondhilight";
				}
				label.setAttribute("class",labelclass);
				label.setAttribute("id",QDocument.ID_PREFIX+"label_wordselectword_"+getID() + checkwordID);
				XML.createText(label,w.word);
				qc.addInlineXHTML(label);

				if ("" != w.following) {
					Element span = qc.createElement("span");
					XML.createText(span, w.following);
					if (wb.isSecondHighlighted) {
						label.setAttribute("class","secondhilight");
					}
					qc.addInlineXHTML(span);
				}

			}
		}
	}

	@Override
	protected void formAllValuesSet(ActionParams ap) throws OmException
	{
		if(!isEnabled()) return;

		// Get selected words data
		for (WordBlock wb : wordBlocks)
		{
			for(Word w : wb.words)
			{
				String checkwordID = makeCheckwordId(wb, w);
				w.selected = false;
				if(ap.hasParameter("wordselectword_"+getID() + checkwordID))
				{
					w.selected = true;
				}
			}
		}
	}

	/**
	 * Clear all the selected words.
	 */
	public void clearSelection()
	{
		for (WordBlock wb : wordBlocks) {
			clearSelection(wb.id);
		}
	}

	/**
	 * Clear all the selected words in the block with the given id.
	 * @param blockId the id of the block of words to clear.
	 */
	public void clearSelection(String blockId) {
		WordBlock wb = wordsById.get(blockId);
		for (Word w : wb.words) {
			w.selected = false;
		}
	}
}
