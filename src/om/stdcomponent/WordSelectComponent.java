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
A paragraph of plain text with selectable words
<h2>XML usage</h2>
<pre>&lt;wordselect id="paragraph_1"&gt;
	Some text that is not part of the answer 
 &lt;sw id="answer_1"&gt; required answer words &lt;/sw&gt;
 	Some more text then 
 	&lt;sw&gt; other required answer words auto id generation&lt;/sw&gt;
 	 etc...
&lt;/wordselect&gt;
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
	
	//used to store the properties of each word
	private static class Word
	{
		String word;
		String following;
		String idTag;
		int id;
		boolean selected = false;

		private Word(String word, String following, String idTag, int id) {
			this.word = word;
			this.following = following;
			this.idTag = idTag;
			this.id = id;
		}
	}

	//used to store the properties of each block of words
	private static class WordBlock
	{
		List<Word> words = new ArrayList<Word>();
		String preceding;
		String id;
		boolean isSecondHighlighted = false;
		boolean isSW = false;
		
		private WordBlock(String content, String id, int wordCounter, boolean isSW, boolean isSecondHighlighted) {
			this.id = id;
			this.isSecondHighlighted = isSecondHighlighted;
			this.isSW = isSW;
			

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
				String idTag = "" + (wordCounter + wordIndex);
				words.add(new Word(word, fragment.toString(), idTag, wordIndex++));
				fragment.setLength(0);
			}
		}
	}
	
	
	private List<WordBlock> wordBlocks = new ArrayList<WordBlock>();
	private Map<String, WordBlock> wordsById = new HashMap<String, WordBlock>();
	
	private int getWordCount(){
		int wordCounter = 0;
		if(wordBlocks != null){
			for(WordBlock wb : wordBlocks){
				wordCounter += wb.words.size();
			}
		}
		return wordCounter;
	}

	
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
						WordBlock wb = new WordBlock(sbText.toString(), id, getWordCount(), false, false);
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
					
					WordBlock wb =new WordBlock(XML.getText(e), id, getWordCount(), true, e.hasAttribute("highlight"));
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
			WordBlock wb = new WordBlock(sbText.toString(), id, getWordCount(), false, false);
			wordBlocks.add(wb);
			wordsById.put(id, wb);

			sbText.setLength(0);
		}
	}

	private static boolean isWordCharacter(char c) {
		return Character.isLetterOrDigit(c);
	}
	
	private String makeCheckwordId(WordBlock wb, Word w) {
		//return "_b" + wb.id + "_w" + w.id;
		return "_" + w.idTag;
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
				input.setAttribute("onfocus","wordOnFocus('"+getID()+checkwordID+"','"+QDocument.ID_PREFIX+"');");
				input.setAttribute("onblur","wordOnBlur('"+getID()+checkwordID+"','"+QDocument.ID_PREFIX+"');");
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
				label.setAttribute("border", "1px");
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

	private int countAllSelected;
	private int countSWWordsSelected;
	private int countSWWords;
	
	@Override
	protected void formAllValuesSet(ActionParams ap) throws OmException
	{
		if(!isEnabled()) return;

		countAllSelected = 0;
		countSWWordsSelected = 0;
		countSWWords = 0;
		// Get selected words data
		for (WordBlock wb : wordBlocks)
		{
			for(Word w : wb.words)
			{
				if(wb.isSW){
					countSWWords++;
				}
				
				String checkwordID = makeCheckwordId(wb, w);
				w.selected = false;
				if(ap.hasParameter("wordselectword_"+getID() + checkwordID))
				{
					w.selected = true;
					countAllSelected++;
					if(wb.isSW){
						countSWWordsSelected++;
					}
					
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
	 * @param swId the id of the block of words to clear.
	 */
	public void clearSelection(String swId) {
		WordBlock wb = wordsById.get(swId);
		for (Word w : wb.words) {
			w.selected = false;
		}
	}
	
	
	/**
	 * returns a count of all the selected words (integer value) 
	 */
	public int getTotalWordsSelected(){
		return countAllSelected;
	}
	
	/**
	 * returns a count of the words within the sw tags that 
	 * have been selected (integer value) 
	 */
	public int getTotalSWWordsSelected(){
		return countSWWordsSelected;
	}
	
	/**
	 * returns a count of the total number of words within 
	 * the sw tags (integer value) 
	 */
	public int getTotalSWWords(){
		return countSWWords;
	}
	
	
	/**
	 * boolean returns true only if all the words within the sw tags 
	 * have been selected and no other words are selected 
	 */
	public boolean getIsCorrect(){
		if(countSWWords == countSWWordsSelected
				&& countSWWords == countAllSelected){
			return true;
		}
		else{
			return false;
		}
		
	}
	
	/**
	 * highlights all the words contained within all the sw tags
	 * in a second colour. Intended for use with final feedback
	 */
	public void secondHilightSWWords(){
		for (WordBlock wb : wordBlocks) {
			if(wb.isSW){
				secondHilightSWWords(wb.id);
			}
		}
	}
	
	/**
	 * highlights all the words contained within an sw tag
	 * of the given id in a second colour.
	 * @param swId the id of the block of words to highlight.
	 */
	public void secondHilightSWWords(String swId) {
		WordBlock wb = wordsById.get(swId);
		wb.isSecondHighlighted = true;
	}
	
}
