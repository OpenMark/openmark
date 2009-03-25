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
package om.helper;

import util.misc.Strings;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * <p>The OpenMark <code>PMatch</code> (pattern match) class is used to test
 * if a student response string satisfies a specified response match.</p>
 * 
 * <p>PMatch extends the older OpenMark Match routine
 * <ul>
 * <li>to allow misspellings of a single character or two characters for longer words</li>
 * <li>to support proximity of words</li>
 * <li>to define the containing unit as a sentence</li>
 * </ul>
 * </p>
 * 
 * <p><b>Match users should beware</b> that:
 * <ul>
 * <li>In PMatch the response is set and then matched against multiple patterns
 * c.f. Match where the pattern is set and then matched against a response.</li>
 * <li>In PMatch the word OR symbol is '|'. This replaces the $ used by Match.</li>
 * </ul>
 * </p>
 * <p>PMatch works on the basis that you have a student response which you wish
 * to match against any number of response matching patterns.</p>
 * 
 * <p>Create a PMatch object with the student response as a parameter<br/>
 * <code>PMatch m = new PMatch(resp);</code><br/>
 * where resp is a String holding the student response.</p>
 * 
 * <p>Or create a PMatch object with an empty student response<br/>
 * <code>PMatch m = new PMatch("");</code><br/>
 * then set the student response using<br>
 * <code>m.setResponse(resp);</code></p>
 * 
 * <p>Then carry out repeated response matching tests using the match method.
 * Match returns true if the student response contains the pattern.<br/>
 * <code>if (m.match("mow", "tom|dick|harry")) { }</code></p>
 * 
 * <p>The match method takes two parameters
 * <ul>
 * <li>the matching options: <code>mow</code></li>
 * <li>the words to be matched: <code>tom|dick|harry</code></li>
 * </ul>
 * </p>
 * 
 * <p>The basic unit that PMatch operates on is the word, where a word is defined
 * as a sequence of characters between spaces.</p>
 * 
 * <p>The containing unit is the sentence. All words in the pattern must be in
 * the same sentence.</p>
 * 
 * <p>PMatch matches what it is given. Whether case does or does not matter is
 * left to the author to decide and to handle using normal Java methods. Typically
 * if case does not matter the pattern match is specified in lower case and the
 * response is transformed to lower case using the standard Java method
 * toLowerCase() before PMatch is called.</p>
 * 
 * <p>'comma', 'semi-colon' and 'colon' have no significance to PMatch. If authors
 * wish to use any of these as a delimiter in a response they should replace it
 * with 'space' using the standard Java method replace() before calling PMatch.</p>
 * 
 * <p>The matching options are:
 * <table border="1">
 * <tr><th>Matching option</th><th>Symbol</th><th>Description</th></tr>
 * <tr><td>allowExtraChars</td><td>c</td><td>Extra characters can be anywhere
 * within the word. Authors are expected to omit allowExtraChars when using the
 * misspelling options below.</td></tr>
 * <tr><td>allowAnyWordOrder</td><td>o</td><td>Where multiple words are to be
 * matched they can be in any order.</td></tr>
 * <tr><td>allowExtraWords</td><td>w</td><td>Extra words beyond those being
 * searched for are accepted.</td></tr>
 * <tr><td>misspelling: allowReplaceChar</td><td>mr</td><td>Will match a word
 * where one character is different to those specified in the pattern. The pattern
 * word must be 4 characters or greater for replacement to kick in. Authors are
 * expected to omit allowExtraChars when using this option.</td></tr>
 * <tr><td>misspelling: allowExtraChar</td><td>mx</td><td>Will match a word where
 * one character is extra to those specified in the pattern. The pattern word must
 * be 3 characters or greater for extra to kick in. Authors are expected to omit
 * allowExtraChars when using this option.</td></tr>
 * <tr><td>misspelling: allowFewerChar</td><td>mf</td><td>Will match a word where
 * one character is missing from those specified in the pattern. The pattern word
 * must be 4 characters or greater for fewer to kick in. Without this 'no' would
 * be reduced to just matching  'n' or 'o'. Authors are expected to omit
 * allowExtraChars when using this option.</td></tr>
 * <tr><td>misspelling</td><td>m</td><td>This combines the three ways of
 * misspelling a word described above i.e. m is equivalent to mxfr. Authors are
 * expected to omit allowExtraChars when using this option.</td></tr>
 * <tr><td>misspellings</td><td>m2</td><td>Allows two misspellings, as defined by
 * option 'm', in words of 8 characters or more. Authors are expected to omit
 * allowExtraChars when using this option.</td></tr>
 * <tr><td>allowProximityOf0</td><td>p0</td><td>No words are allowed in between
 * any words specified in the proximity sequence.</td></tr>
 * <tr><td>allowProximityOf1</td><td>p1</td><td>One word is allowed in between any
 * two words specified in the proximity sequence.</td></tr>
 * <tr><td>allowProximityOf2</td><td>p2</td><td>(Default value) Two words are
 * allowed in between any two words specified in the proximity sequence.</td></tr>
 * <tr><td>allowProximityOf3</td><td>p3</td><td>Three words are allowed in between
 * any two words specified in the proximity sequence.</td></tr>
 * <tr><td>allowProximityOf4</td><td>p4</td><td>Four words are allowed in between
 * any two words specified in the proximity sequence.</td></tr>
 * </table></p>
 * 
 * <p>Within a word 'special characters' provide more localised control of the
 * patterns:
 * <table border="1">
 * <tr><th>Special character</th><th>Symbol</th><th>Description</th></tr>
 * <tr><td>Word AND</td><td>space</td><td>'space' delimits words and acts as the
 * logical AND.</td></tr>
 * <tr><td>Word OR</td><td>|</td><td>| between words indicates that either word
 * will be matched. | delimits words and acts as the logical OR.</td></tr>
 * <tr><td>Proximity control</td><td>_</td><td>Words must be in the order given
 * and with no more than n (where n is 0 - 4) intervening words. _ delimits words
 * and also acts as logical 'AND'. </td></tr>
 * <tr><td>Single character wildcard</td><td>#</td><td>Matches any single
 * character.</td></tr>
 * <tr><td>Multiple character wildcard</td><td>&</td><td>Matches any sequence of
 * characters including none.</td></tr>
 * </table></p>
 * 
 * <p>Examples:
 * <table border="1">
 * <tr><th>Student response</th><th>Matching options</th><th>Pattern match</th><th>
 * match method return</th></tr>
 * <tr><td>tom dick harry</td><td>empty</td><td>tom dick harry</td><td>true.
 * This is the exact match.</td></tr>
 * <tr><td>thomas</td><td>c</td><td>tom</td><td>true. Extra characters are allowed
 * anywhere within the word.</td></tr>
 * <tr><td>tom, dick and harry</td><td>w</td><td>dick</td><td>true. Extra words
 * are allowed anywhere within the sentence.</td></tr>
 * <tr><td>harry dick tom</td><td>o</td><td>tom dick harry</td><td>true. Any order
 * of words is allowed.</td></tr>
 * <tr><td>rick</td><td>m</td><td>dick</td><td>true. One character in the word can
 * differ.</td></tr>
 * <tr><td>rick and harry and tom</td><td>mow</td><td>tom dick harry</td>
 * <td>true.</td></tr>
 * <tr><td>dick and harry and thomas</td><td>cow</td><td>tom dick harry</td>
 * <td>true.</td></tr>
 * <tr><td>arthur, harry and sid</td><td>mow</td><td>tom|dick|harry</td>
 * <td>true. Any of tom or dick or harry will be matched.</td></tr>
 * <tr><td>tom, harry and sid</td><td>mow</td><td>tom|dick harry|sid</td>
 * <td>true. The pattern requires either tom or dick AND harry or sid. Note that
 * 'tom,' is only allowed because m allows the extra character, the comma, in
 * tom.</td></tr>
 * <tr><td>rick</td><td>empty</td><td>#ick</td><td>true. The first character can
 * be anything.</td></tr>
 * <tr><td>harold</td><td>empty</td><td>har&</td><td>true. Any sequence of
 * characters can follow 'har'.</td></tr>
 * <tr><td>tom married maud, sid married jane.</td><td>mow</td><td>tom_maud</td>
 * <td>true. Only one word is between tom and maud.</td></tr>
 * <tr><td>maud married tom, sid married jane.</td><td>mow</td><td>tom_maud</td>
 * <td>false. The proximity control also specifies word order and over-rides the
 * allowAnyWordOrder matching option.</td></tr>
 * <tr><td>tom married maud, sid married jane.</td><td>mow</td><td>tom_jane</td>
 * <td>false. Only two words are allowed between tom and jane.</td></tr>
 * <tr><td>tom married maud</td><td>mow</td><td>tom|thomas marr& maud</td>
 * <td>true.</td></tr>
 * <tr><td>maud marries thomas</td><td>mow</td><td>tom|thomas marr& maud</td>
 * <td>true.</td></tr>
 * <tr><td>tom is to marry maud</td><td>mow</td><td>tom|thomas marr& maud</td>
 * <td>true.</td></tr>
 * <tr><td>tempratur</td><td>m2ow</td><td>temperature</td>
 * <td>true. Two characters are missing.</td></tr>
 * <tr><td>temporatur</td><td>m2ow</td><td>temperature</td><td>true. Two characters
 * are incorrect; one has been replaced and one is missing.</td></tr>
 * </table></p>
 * 
 * <h2>History</h2>
 * <p>The underlying structure of the response matching described here was developed
 * in the Computer Based Learning Unit of Leeds University in the 1970s and was
 * incorporated into Leeds Author Language. The basic unit of the word, the matching
 * options of allowAnyChars, allowAnyWords, allowAnyOrder and the word OR feature
 * all date back to Leeds Author Language.</p>
 * 
 * <p>In 1976 the CALCHEM project which was a hosted by the Computer Based Learning
 * Unit, the Chemistry Department at Leeds University and the Computer Centre of
 * Sheffield Polytechnic (now Sheffield Hallam University) produced a portable
 * version of Leeds Author Language.</p>
 * 
 * <p>A portable version for microcomputers was developed in 1982 by the Open
 * University, the Midland Bank (as it then was; now Midland is part of HSBC) and
 * Imperial College. The single and multiple character wildcards were added at this
 * time.</p>
 * 
 * <p>The misspelling and proximity additions and the containing unit of the
 * sentence were added as part of the Open University COLMSCT projects looking at
 * free text response matching during 2006 - 2009.</p>
 * 
*/
public class PMatch
{
	/**
	  * This field controls whether or not extra characters in a word are to be
	  * permitted. For example, if the pattern is "abc" and this field is
	  * set to a value of <code>true</code>, this will successfully match with strings
	  * such as "abcde" or "abacab". If this field is set to a value of <code>false</code>
	  * , only the string "abc" will match the pattern "abc".<p>
	  * This field has a default value of <code>true</code>.
	  */
	private boolean allowExtraChars = true;

	/**
	  * This field controls whether or not words in the match string must appear in
	  * the same order as the words in the pattern. For example, if the pattern
	  * is "Mary had a little lamb" and this field is set to a value of <code>true</code>,
	  * this will successfully match with strings such as "Mary lamb had a little".<p>
	  * This field has a default value of <code>true</code>.
	  */
	private boolean allowAnyWordOrder = true;

	/**
	  * This field controls whether or not words that are not in the pattern
	  * are permitted in the match string. For example, if the pattern
	  * is "banana" and this field is set to a value of <code>true</code>,
	  * this will successfully match with strings such as "Eat a banana today".
	  * <p>
	  * This field has a default value of <code>true</code>.
	  */
	private boolean allowExtraWords = true;

	/**
	  * allowReplaceChar
	  * allowExtraChar
	  * allowFewerChar
	  * These fields enable minor typing/spelling mistakes to be accounted for.
	  *    - if allowReplaceChar is true then one character in the word may be
	  *      incorrect and the match will still succeed 
	  *    - if allowExtraChar is true then an additional single character 
	  *      can be present and the match will still succeed 
	  *    - if allowFewerChar is true then a single character from the word may
	  *      be missing and the match will still succeed 
	  *    - if allowTwoMispellings is true then for words containing 8 or more
	  *      characters two misspellings may occur and the match will still succeed 
	  * <p>
	  * These fields have default values of <code>false</code>.
	  */
	private boolean allowReplaceChar = false;
	private boolean allowExtraChar = false;
	private boolean allowFewerChar = false;
	private boolean allowTwoMisspellings = false;

	/**
	 * p0, p1, p2, p3, p4 are the proximity settings that specify the number of
	 * words that are allowed in between words specified in a proximity sequence.
	 * If none of p0-p4 are specified then the default is taken as p2.
	 */
	private int	 	patternProximity[], proximityDistance = 3;

	/**
	  * An array of strings whose elements contain the individual words in the
	  * pattern string.
	  * Together with an array to specify if words must be in proximity
	  */
	private String 	patternWords[];
	private String	sentences[];
	private String	words[];
	/**
	  * Constructs a response object via setResponse()
	  * @param String response
	  * 
	  */
	public PMatch(String response) {

		setResponse(response);
	}

	/**
	  * Constructs an empty response object which will be filled in later
	  * using setResponse().
	  */
	public PMatch() {
		setResponse("");
	}

	/**
	 * Sets the pattern that we want to test strings against.
	 * The pattern is a set of one or more words (each word separated by a space).
	 * To get a match, all of the words in the pattern must match with a word
	 * in the string you are matching against. Extra control of the matching
	 * process can be achieved using the '&', '#' '|' and '_' characters. If you want
	 * to use these characters literally, precede them with the two backslash characters.
	 * <br>
	 * '&' means match any string. <br>
	 * '#' means match any character. <br>
	 * '|' match with the preceding OR the next word. <br>
	 * '_' match if the linked words exist in the given order and with no more than
	 *     two intervening words. <br>
	 * See the examples above for details of usage.
	 * @param pattern
	 */
	public void setPattern(String pattern) {
		int		i = 0, wordCount = 0;
		String	pstr, pstr1, pstr2;
    
		// remove leading and trailing spaces
		pstr = pattern.trim();
		// remove double spaces
		do {
			pstr1 = pstr;
			pstr = pstr.replace("  ", " ");
			pstr2 = pstr;
		} while (pstr1 != pstr2);

		// count the words to set the appropriate array size
		for (i = 0; i < pstr.length(); ++i) {
			if ((pstr.charAt(i) == ' ')
			 || (pstr.charAt(i) == '_')) ++wordCount;
		}
		patternProximity = new int[wordCount+1];
		// now set the word count back to zero and this time count and mark the words for proximity
		wordCount = 0;
		// identify word sequences that must be in proximity
		for (i = 0; i < pstr.length(); ++i) {
			if (pstr.charAt(i) == ' ') {
				patternProximity[wordCount] = 999;
				++wordCount;
			}
			else if (pstr.charAt(i) == '_') {
				patternProximity[wordCount] = proximityDistance;
				++wordCount;
			}
		}
		// can now convert underscore to space
		pstr = pstr.replace("_", " "); 
		// and break into words
		patternWords = breakIntoTokens(pstr, ' ');
	}

	public void setMatchingOptions(String matchingOptions) {
		String mo = matchingOptions.toLowerCase();
		  
		if (mo.contains("c")) allowExtraChars = true;
		else allowExtraChars = false;

		if (mo.contains("o")) allowAnyWordOrder = true;
		else allowAnyWordOrder = false;

		if (mo.contains("p0")) proximityDistance = 1;
		else if (mo.contains("p1"))  proximityDistance = 2;
		else if (mo.contains("p2"))  proximityDistance = 3;
		else if (mo.contains("p3"))  proximityDistance = 4;
		else if (mo.contains("p4"))  proximityDistance = 5;
		else proximityDistance = 3;

		if (mo.contains("w")) allowExtraWords = true;
		else allowExtraWords = false;

		allowReplaceChar = false;
		allowExtraChar = false;
		allowFewerChar = false;

        if (Pattern.matches("m2[^xfr]*", mo)) {
        	allowExtraChar = true;
        	allowFewerChar = true;
        	allowReplaceChar = true;
        	allowTwoMisspellings = true;
        }
        else if (Pattern.matches("m[^xfr]*", mo)) {
        	allowExtraChar = true;
        	allowFewerChar = true;
        	allowReplaceChar = true;
        }
        else {
        	if (Pattern.matches(".*m.*[x].*", mo)) {allowExtraChar = true;}
        	if (Pattern.matches(".*m.*[f].*", mo)) {allowFewerChar = true;}
        	if (Pattern.matches(".*m.*[r].*", mo)) {allowReplaceChar = true;}
        	if (Pattern.matches(".*m.*[2].*", mo)) {allowTwoMisspellings = true;}
        }
	}
	////////////////////////////////////////////////////////////////////////////////

	public void setResponse(String response) {
		int		i;
		String	rsp, rsp1, rsp2;
		Pattern p;
		Matcher m;
		// remove leading and trailing spaces
		rsp = response.trim();
		// remove any multiple spaces
		do {
			rsp1 = rsp;
			rsp = rsp.replaceAll("  ", " ");
			rsp2 = rsp;
		} while (rsp1 != rsp2);
			
//System.out.println(rsp);		
		
		// start by breaking the response into sentences
		// before this can be done we need to differentiate
		// full stops at the end of sentences from
		//	- decimal points
		//	- indicators of abbreviations such as i.e.

		// first of all the full stop '.' means 'any character' in regular expressions
		// so we'll start by replacing it with something out of the ordinary
		rsp = rsp.replace('.', '\uBAD0');	// char replace doesn't use reg. ex.
											// substitute with Hangul (Korean) symbol
		// now replace i.e. etc.
		rsp = rsp.replaceAll("i\uBAD0e\uBAD0", "ie");	// i.e.
		rsp = rsp.replaceAll("ie\uBAD0", "ie");	// ie.
		rsp = rsp.replaceAll("e\uBAD0g\uBAD0", "eg");	// e.g.
		rsp = rsp.replaceAll("eg\uBAD0", "eg");	// eg.
		rsp = rsp.replaceAll("etc\uBAD0", "etc");	// etc.

		// replace <letter>.<space> <letter> with a character (\uBAD1) to denote end sentence 
		do {
	    	if (Pattern.matches(".*([a-zA-Z]\uBAD0 [a-zA-Z]).*", rsp)) {
	    		rsp1 = rsp;
//	    		System.out.println("truee");		
	    		p = Pattern.compile("[a-zA-Z]\uBAD0 [a-zA-Z]");
	    		m = p.matcher(rsp);
	    		m.find();
	    		rsp = rsp1.substring(0,m.start()+1) + "\uBAD1" + rsp1.substring(m.start()+3);
//	    		System.out.println(rsp);
	    	}
	    } while (Pattern.matches(".*([a-zA-Z]\uBAD0 [a-zA-Z]).*", rsp));

		// replace <letter>.<letter> with a character (\uBAD1) to denote end sentence 
		do {
	    	if (Pattern.matches(".*([a-zA-Z]\uBAD0[a-zA-Z]).*", rsp)) {
	    		rsp1 = rsp;
	    		System.out.println("truee");		
	    		p = Pattern.compile("[a-zA-Z]\uBAD0[a-zA-Z]");
	    		m = p.matcher(rsp);
	    		m.find();
	    		rsp = rsp1.substring(0,m.start()+1) + "\uBAD1" + rsp1.substring(m.start()+2);
	    		System.out.println(rsp);
	    	}
	    } while (Pattern.matches(".*([a-zA-Z]\uBAD0[a-zA-Z]).*", rsp));
		
		// replace <letter> .<letter> with a character (\uBAD1) to denote end sentence 
		do {
	    	if (Pattern.matches(".*([a-zA-Z] \uBAD0[a-zA-Z]).*", rsp)) {
	    		rsp1 = rsp;
	    		System.out.println("truee");		
	    		p = Pattern.compile("[a-zA-Z] \uBAD0[a-zA-Z]");
	    		m = p.matcher(rsp);
	    		m.find();
	    		rsp = rsp1.substring(0,m.start()+1) + "\uBAD1" + rsp1.substring(m.start()+3);
	    		System.out.println(rsp);
	    	}
	    } while (Pattern.matches(".*([a-zA-Z] \uBAD0[a-zA-Z]).*", rsp));

		if (rsp.endsWith("\uBAD0"))	// remove full stop replacement from end of response
			rsp = rsp.substring(0, rsp.length() - 1); 

		// now break into sentences on the char set above
		sentences = breakIntoTokens(rsp, '\uBAD1');

//		System.out.println("rsp length" + rsp.length() + " sentences length " + sentences.length);
		// and put back any remaining full stops - hopefully as decimal points
		if (rsp.length() > 0) { 
			if (sentences.length > 0) {
				for (i = 0; i < sentences.length; ++i) {
					sentences[i] = sentences[i].replace('\uBAD0', '.');
				}
			}
		}
//		for (i = 0; i < sentences.length; ++i) {
//			System.out.println("#" + i + ":" + sentences[i] + ":");
//		}
		
//		System.out.println(sentences.length);
		setResponseWords(0);
	}
	////////////////////////////////////////////////////////////////////////////////
	
	private void setResponseWords(int whichSentence) {
		
//		System.out.println(sentences[whichSentence]);
		words = breakIntoTokens(sentences[whichSentence], ' ');
	}
	////////////////////////////////////////////////////////////////////////////////
	
   /**
	 * matchWordEx handles groups of 'or-ed' words
	 * Tests whether the specified string satisfies the pattern matching rules.
	 * @param response
	 * @return if the response matches the pattern, this method returns a
	 * value of <code>true</code>, otherwise it returns <code>false</code>.
	 *
	 *
	 * Calls matchWord for each or-ed word in chars.
	 * ('or-ed' combinations are delimited by the '|' character)
	 */
	private boolean matchWordEx(String word, String chars) {
		String parts[] = breakIntoTokens(chars, '|');
		
//!! unnecessary if final character of word is a full stop and the penultimate character
// is not a digit, remove it
//		if ((word.charAt(word.length()-1) == '.')
//			&& Character.isLetter(word.length()-2)) {
//			word = word.substring(0, word.length()-1);
//			System.out.println(word);
//		}
//		System.out.println(word);

		for (int i = 0; i < parts.length; i++) {
			String pattern = substituteWildCards(parts[i]);
			if (matchWord(word, pattern)) return true;
		}
		return false;
	}

	/**
	 * Checks if the specified word contains all the characters in 'chars'
	 * in the same order.
	 * @param word - from response
	 * @param chars - pattern from response match
	 * @return if the word matches the pattern, this method returns true
	 * otherwise false
	 */
	private boolean matchWord(String word, String chars) {
		int index = 0;
		
		for (int i = 0; i < chars.length(); i++) {
			char c = chars.charAt(i);
			if (c != hash.charAt(0)) { // code for '#' matches any char
				if (c == ampersand.charAt(0)) { // code for '&' matches any string
					if (i == chars.length() - 1) return true; // '&' at end
					
					index = findNextIndex(word, index, chars.substring(i + 1));
					if (index == -1) return false;
        
				}
				else if (allowExtraChars) {
					index = word.indexOf(c, index);
					if (index == -1) return false;
					
					index++;
				}
				else {
					if (index >= word.length() || c != word.charAt(index)) return false;
					index++;
				}
			}
			else index++;
		}
		return allowExtraChars ? true : (index == word.length());
	}

	/**
	 * For a pattern word containing the '&' wildcard, we need to look-ahead to
	 * find something that matches the part of the word that follows the '&'
	 * but not including any text following any subsequent '&' in the pattern word
	 */
	private int findNextIndex(String word, int index, String chars) {

		if (chars.charAt(0) == ampersand.charAt(0)) return index; // "&&" is the same as "&"
    
		int endMatch = chars.indexOf(ampersand.charAt(0));
		if (endMatch != -1) chars = chars.substring(0, endMatch);
    
		for (int i = index; i < word.length(); i++) {
			if (matchWord(word.substring(i), chars)) return i;
		}
		return index;
	}

	/**
	 * Encodes special matching options characters of '#', '&', '|' and '_' as
	 * "unlikely" unicode characters (from Korean character set).
	 * (also de-escapes any escaped wildcard characters).
	 * This code reliess on these unicode characters not being present in the
	 * the pattern string; a pretty safe assumption because the typical usage
	 * will only use characters in the ASCII range i.e. 0x00 to 0x7F
	 */
	private static final String tempC = "\uBAD1";	// Hangul syllable
	private static final String hash = "\uBAD2";	// Hangul syllable
	private static final String ampersand = "\uBAD3";	// Hangul syllable
	private static final String verticalBar = "\uBAD4";	// Hangul syllable
	private static final String underscore = "\uBAD5";	// Hangul syllable
	
	private String substituteWildCards(String chars) {
		chars = Strings.replace(chars, "\\#", tempC);
		chars = Strings.replace(chars, "#", hash);
		chars = Strings.replace(chars, tempC, "#");

		chars = Strings.replace(chars, "\\&", tempC);
		chars = Strings.replace(chars, "&", ampersand);
		chars = Strings.replace(chars, tempC, "&");

		chars = Strings.replace(chars, "\\|", tempC);
		chars = Strings.replace(chars, "|", verticalBar);
		chars = Strings.replace(chars, tempC, "|");

		chars = Strings.replace(chars, "\\_", tempC);
		chars = Strings.replace(chars, "_", underscore);
		chars = Strings.replace(chars, tempC, "_");

		return chars;
	}

	/**
	 * This is a variant of Util.breakIntoTokens except this version preserves
	 * any escaped delimiter characters by replacing them with a "hopefully"
	 * extremely unlikely unicode character, then reverting these characters
	 * in the tokenized strings.
	 */
	static private String[] breakIntoTokens(String text, char delimiter) {
    
		String sDelimiter = "" + delimiter;
		if (delimiter == '|') sDelimiter = "\\|"; // needs escaping for regex

		text = Strings.replace(text, "\\" + delimiter, tempC);
		String tokens[] = text.split(sDelimiter);
		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = Strings.replace(tokens[i], tempC, sDelimiter);
		}
		return tokens;
	}

	/**
	 * match works within sentences
	 * 
	 */
	private boolean matchSentence() {
		boolean	matched;
		boolean	justUnwound = false;	// when having to unwind a proximity sequence
		int		proximityCounter;		// tracks how many words are left in required proximity
		int 	correct = 0;			// matches found
		int 	ip = 0, iw = 0;			// ip points to patternWords, iw points to words; 
		int		ic, currentWordLength = 0;;
		int		isub;
		StringBuilder currentSb = new StringBuilder(100);
		String 	subPatternWords[];
		String	currentSubPatternWord = "";

		if (words.length < patternWords.length) return false;
		if (!allowExtraWords && words.length != patternWords.length) return false;

		int ptrWords[] = new int[words.length]; // array to hold info. on which words have been matched already
	  
		for (int i = 0; i < words.length; i++) {
			ptrWords[i] = -1;
		}
	  
		for (ip = 0; ip < patternWords.length; ip++) {
			matched = false;

			// is proximity required?
			if (ip > 0) // first pattern word is an exception as it can't follow anything
				proximityCounter = patternProximity[ip-1];
			else
				proximityCounter = 999;
		    
			// allow any words to be matched unless
			//  - allowAnyOrder is false
			//  - proximity is currently required
			//  - we have just unwound a proximity match which failed at the second
			//    or subsequent match in the proximity sequence
			// if any of these are false iw moves to the next word
			if (allowAnyWordOrder && (proximityCounter > proximityDistance) && !justUnwound) iw = 0;

			justUnwound = false;
			
			// while there are still words to consider and we're not outside the proximity
			while ((iw < words.length) && (proximityCounter > 0)) {
				// if the word has already been matched it can't be used to 
				// satisfy a second match
				if (ptrWords[iw] != -1) {
					// this code section is repeated below
					// if a word can't be used it still counts when calculating the proximity
					proximityCounter--;
			        
					if (proximityCounter == 0) {
						// unwind this proximity sequence
						// go backwards searching for start of sequence
						while ((ip > 0) && (patternProximity[ip - 1] == proximityDistance)) {
							ip--;
							correct--;
						}
						for (int i = (words.length - 1); i > 0; --i) {
							if (ptrWords[i] >= ip) {
								iw = i;
								ptrWords[i] = -1;
							}
						}
						ip--;
						iw++;
						justUnwound = true;
						break;
					}
				}
				else if (ptrWords[iw] == -1) {
					// try a 'normal' match with wildcards but no misspellings
					if (matchWordEx(words[iw], patternWords[ip])) {
						matched = true;
					}
	
					// if 'normal' match failed try misspellings if allowed
					if ((!matched) && (allowReplaceChar || allowExtraChar || allowFewerChar)) {  

						subPatternWords = breakIntoTokens(patternWords[ip], '|');

						subPatternLabel:
							for (isub = 0; isub < subPatternWords.length; isub++) {
								currentSubPatternWord = subPatternWords[isub];
								currentWordLength = currentSubPatternWord.length();
								currentSb.delete(0, 100);
						  
								if (!matched && allowReplaceChar && (currentSubPatternWord.length() > 3)) {
									for (ic = 0; ic < currentWordLength; ic++) {
										currentSb.replace(0, currentWordLength, currentSubPatternWord);
										if ((currentSb.charAt(ic) != hash.charAt(0))
												&& (currentSb.charAt(ic) != ampersand.charAt(0))) {
											currentSb.deleteCharAt(ic);
											currentSb.insert(ic, hash.charAt(0));
											
											matched = matchWordEx(words[iw], currentSb.toString());
								  
											if (matched) {
												break subPatternLabel;
											}
										}
									}
								}

								if (!matched && allowExtraChar && (currentSubPatternWord.length() > 2)) {
									for (ic = 0; ic < currentWordLength + 1; ic++) {
										currentSb.replace(0, currentWordLength + 1, currentSubPatternWord);
										currentSb.insert(ic, hash.charAt(0));

										matched = matchWordEx(words[iw], currentSb.toString());
										
										if (matched) {
											break subPatternLabel;
										}
									}
								}

								if (!matched && allowFewerChar && (currentSubPatternWord.length() > 3)) {
									for (ic = 0; ic < currentWordLength; ic++) {
										currentSb.replace(0, currentWordLength + 2, currentSubPatternWord);
										if ((currentSb.charAt(ic) != hash.charAt(0))
												&& (currentSb.charAt(ic) != ampersand.charAt(0))) {
											currentSb.deleteCharAt(ic);
											
											matched = matchWordEx(words[iw], currentSb.toString());
								  
											if (matched) {
												break subPatternLabel;
											}
										}
									}
								}
							} // end of subpattern loop
						subPatternLabel2:
							if ((!matched) && (currentWordLength > 7) && (allowTwoMisspellings)) {
								for (isub = 0; isub < subPatternWords.length; isub++) {
									currentSubPatternWord = subPatternWords[isub];
									currentWordLength = currentSubPatternWord.length();
									currentSb.delete(0, 100);
					  
									if (!matched && allowReplaceChar && (currentSubPatternWord.length() > 3)) {
										for (ic = 0; ic < currentWordLength; ic++) {
											currentSb.replace(0, currentWordLength, currentSubPatternWord);
											if ((currentSb.charAt(ic) != hash.charAt(0))
													&& (currentSb.charAt(ic) != ampersand.charAt(0))) {
												currentSb.deleteCharAt(ic);
												currentSb.insert(ic, hash.charAt(0));
										
												matched = secondMismatch(iw, currentSb.toString(), ic+1, currentWordLength);
							  
												if (matched) {
													break subPatternLabel2;
												}
											}
										}
									}

									if (!matched && allowExtraChar && (currentSubPatternWord.length() > 2)) {
										for (ic = 0; ic < currentWordLength + 1; ic++) {
											currentSb.replace(0, currentWordLength + 1, currentSubPatternWord);
											currentSb.insert(ic, hash.charAt(0));

											matched = secondMismatch(iw, currentSb.toString(), ic+1, currentWordLength+1);
									
											if (matched) {
												break subPatternLabel2;
											}
										}
									}

									if (!matched && allowFewerChar && (currentSubPatternWord.length() > 3)) {
										for (ic = 0; ic < currentWordLength; ic++) {
											currentSb.replace(0, currentWordLength + 2, currentSubPatternWord);
											if ((currentSb.charAt(ic) != hash.charAt(0))
													&& (currentSb.charAt(ic) != ampersand.charAt(0))) {
												currentSb.deleteCharAt(ic);
										
												matched = secondMismatch(iw, currentSb.toString(), ic+1, currentWordLength-1);
							  
												if (matched) {
													break subPatternLabel2;
												}
											}
										}
									}
								} // end of subpattern2 loop
							} // end of subpattern2 if
					}
			      
					if (matched) {
						correct++;
						ptrWords[iw] = ip + 1;
						iw++;
						break;
					}
					else {
						// this is a repeat of the code section at the start of this function
						proximityCounter--;
									        
						if (proximityCounter == 0) {
							// unwind this proximity sequence
							// go backwards searching for start of sequence
							while ((ip > 0) && (patternProximity[ip - 1] == proximityDistance)) {
								ip--;
								correct--;
							}
							for (int i = (words.length - 1); i > 0; --i) {
								if (ptrWords[i] >= ip) {
									iw = i;
									ptrWords[i] = -1;
								}
							}
							ip = ip - 1;
							iw = iw + 1;
							justUnwound = true;
							break;
						}
					}
				}

				iw++;
			}
		}
		return (correct == patternWords.length);
	} // end of matchSentence
	////////////////////////////////////////////////////////////////////////////////

	/**
	 * secondMismatch allows for a second spelling mistake in words of more than 8 characters
	 */

	public boolean secondMismatch(int wordPtr, String currentSubPattern, int start, int currentWordLength) {
		boolean	lmatched = false;
		int	id;
		StringBuilder lSb= new StringBuilder(100);
	  
		lSb.delete(0, 100);

		if (allowReplaceChar) {
			// allow second extra replacement
			for (id = start; id < currentWordLength; id++) {
				lSb.delete(0, 100);
				lSb.replace(0, currentWordLength, currentSubPattern);
				
				if ((lSb.charAt(id) != hash.charAt(0))
					&& (lSb.charAt(id) != ampersand.charAt(0))) {
					lSb.deleteCharAt(id);
					lSb.insert(id, hash.charAt(0));
//System.out.println("second extra replace:" + lSb);				
					lmatched = matchWordEx(words[wordPtr], lSb.toString());
		  
					if (lmatched) {
						return(true);
					}
				}
			}
		}
		if (allowExtraChar) {	
			// allow second extra character
			for (id = start; id < currentWordLength + 1; id++) {
				lSb.delete(0, 100);
				lSb.replace(0, currentWordLength + 1, currentSubPattern);
				
				lSb.insert(id, hash.charAt(0));
//System.out.println("second extra character:" + lSb);				

				lmatched = matchWordEx(words[wordPtr], lSb.toString());
			
				if (lmatched) {
					return(true);
				}
			}
		}
		if (allowFewerChar) {
			// allow second extra character fewer
			for (id = 0; id < currentWordLength; id++) {
				lSb.delete(0, 100);
				lSb.replace(0, currentWordLength + 2, currentSubPattern);
				if ((lSb.charAt(id) != hash.charAt(0))
				&& (lSb.charAt(id) != ampersand.charAt(0))) {
					lSb.deleteCharAt(id);
//System.out.println("second character fewer:" + lSb);				
				
					lmatched = matchWordEx(words[wordPtr], lSb.toString());
	  
					if (lmatched) {
						return(true);
					}
				}
			}
		}
		return(false);
	}

	///////////////////////////////////////////////////////////////////////////////
		
	/**
	 * match checks the response provided against an established pattern using the
	 * matching options and special characters specified
	 * 
	 * match works within sentences i.e. a match cannot span sentences
	 */
			
	public boolean match(String matchingOptions, String pattern) {

		setMatchingOptions(matchingOptions);
		return match(pattern);
	}
			
	////////////////////////////////////////////////////////////////////////////////
			
	public boolean match(String pattern) {
		int		sentencePtr;

		setPattern(pattern);

		// sentence[0] is already loaded into words[]
		if (sentences.length <= 1) {
			if (matchSentence()) return(true);
			return(false); // the norm; few responses are > 1 sentence
		}
		else {
			// it's rare to match multiple sentences
			// so this is the exception not the norm
			// check remaining sentences
			for (sentencePtr = 0; sentencePtr < sentences.length; sentencePtr++) {
				setResponseWords(sentencePtr);
				//System.out.println("length:" + sentences.length + "pointer:" + sentencePtr);
				if (matchSentence()) return(true);
			}
			return(false);
		}
	} // end of match()

}