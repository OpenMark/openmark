package om.helper.spellcheck;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import om.OmUnexpectedException;

import com.swabunga.spell.engine.Configuration;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

/**
 * Provides utility methods for spell checking. Uses an open source API
 * <a href="http://jazzy.sourceforge.net/">Jazzy</a> for spell checking.
 */
public final class SpellCheckerUtil {
	
	private static SpellDictionaryHashMap dictionary = null;
	
	private static SpellChecker spellChecker = null;
	
	static {
		try {
			//Load all words from dictionary files into spellchecker's dictionary map.
			dictionary = new SpellDictionaryHashMap(new InputStreamReader(
					SpellCheckerUtil.class.getResourceAsStream("eng_com.dic")));
			dictionary.addDictionary(new InputStreamReader(
					SpellCheckerUtil.class.getResourceAsStream("center.dic")));
			dictionary.addDictionary(new InputStreamReader(
					SpellCheckerUtil.class.getResourceAsStream("centre.dic")));
			dictionary.addDictionary(new InputStreamReader(
					SpellCheckerUtil.class.getResourceAsStream("color.dic")));
			dictionary.addDictionary(new InputStreamReader(
					SpellCheckerUtil.class.getResourceAsStream("colour.dic")));
			dictionary.addDictionary(new InputStreamReader(
					SpellCheckerUtil.class.getResourceAsStream("ise.dic")));
			dictionary.addDictionary(new InputStreamReader(
					SpellCheckerUtil.class.getResourceAsStream("ize.dic")));
			dictionary.addDictionary(new InputStreamReader(
					SpellCheckerUtil.class.getResourceAsStream("labeled.dic")));
			dictionary.addDictionary(new InputStreamReader(
					SpellCheckerUtil.class.getResourceAsStream("labelled.dic")));
			dictionary.addDictionary(new InputStreamReader(
					SpellCheckerUtil.class.getResourceAsStream("yse.dic")));
			dictionary.addDictionary(new InputStreamReader(
					SpellCheckerUtil.class.getResourceAsStream("yze.dic")));
			dictionary.addDictionary(new InputStreamReader(
					SpellCheckerUtil.class.getResourceAsStream("ou_words.dic")));
			
		} catch (FileNotFoundException e) {
			throw new OmUnexpectedException("Dictionary file not found.", e);
		} catch (IOException e) {
			throw new OmUnexpectedException("Error reading dictionary file.", e);
		}
		spellChecker = new SpellChecker(dictionary);

		getConfig().setBoolean(Configuration.SPELL_IGNOREMIXEDCASE, false);
		getConfig().setBoolean(Configuration.SPELL_IGNOREUPPERCASE, true);
		getConfig().setBoolean(Configuration.SPELL_IGNOREDIGITWORDS, true);
		getConfig().setBoolean(Configuration.SPELL_IGNOREINTERNETADDRESSES,	true);
	}
	
	private SpellCheckerUtil() {}
	
	private static Configuration getConfig() {
		return spellChecker.getConfiguration();
	}
	
	/**
	 * Adds a word to the spell checker's dictionary.
	 * @param word single word
	 */
	public static synchronized void addToDictionary(String word) {
		spellChecker.addToDictionary(word);
	}
	
	/**
	 * Checks whether a word spelt correctly or not. This method also checks 
	 * to see if a misspelt word can be ignored according to the ignore settings.
	 * 
	 * @param word single word for spell check
	 * @param startsSentence true if it is the first word in a sentence, 
	 * 						used to check mixed case word.
	 * @return true if the word spelt correctly
	 */
	public static boolean isWordCorrect(String word, boolean startsSentence) {
		boolean correct = spellChecker.isCorrect(word);
		if (!correct) {
			//Ignore the misspelling if one of these cases is true
			if ((getConfig().getBoolean(Configuration.SPELL_IGNOREMIXEDCASE) && isMixedCaseWord(
					word, startsSentence))
					|| (getConfig().getBoolean(
							Configuration.SPELL_IGNOREUPPERCASE) && isUpperCaseWord(word))
					|| (getConfig().getBoolean(
							Configuration.SPELL_IGNOREDIGITWORDS) && isDigitWord(word))
					|| (getConfig().getBoolean(
							Configuration.SPELL_IGNOREINTERNETADDRESSES) && isINETWord(word))) {
				return true;
			}
		}
		return correct;
	}
	
	/**
	 * Takes a misspelt word as an argument and returns an array of suggestions
	 * from the dictionary.
	 * @param word Misspelt word for suggestions
	 * @return String array of available suggestions from the dictionary
	 */
	@SuppressWarnings("unchecked")
	public static String[] getSuggestions(String word) {
		List<String> suggestions = new ArrayList<String>();
		List<Word> suggestedWords = spellChecker.getSuggestions(word,
				getConfig().getInteger(Configuration.SPELL_THRESHOLD));

		for (Word suggestedWord : suggestedWords) {
			suggestions.add(suggestedWord.getWord());
		}
		return suggestions.toArray(new String[0]);
	}
	
	/**
	 * Spell checks all words in the input string and returns an array of
	 * misspelt words. Returns an empty array if all the words are correct.
	 * @param words String of words for spell check
	 * @return string array of misspelt words
	 */
	public static String[] checkAllWords(String words) {
		List<String> misspeltWords = new ArrayList<String>();
		StringWordTokenizer tokenizer = new StringWordTokenizer(words);
		
		while (tokenizer.hasMoreWords()) {
			String word = tokenizer.nextWord();
			if (!isWordCorrect(word, tokenizer.isNewSentence())) {
				misspeltWords.add(word);
			}
		}
		return misspeltWords.toArray(new String[0]);
	}
	
	/**
	 * Returns true if this word contains a digit.
	 * @param word single word
	 * @return true if this word contains a digit.
	 */
	public final static boolean isDigitWord(String word) {
		for (int i = word.length() - 1; i >= 0; i--) {
			if (Character.isDigit(word.charAt(i))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if this word looks like an internet address.	
	 * @param word single word
	 * @return true if it is a internet address
	 */
	public final static boolean isINETWord(String word) {
		String lowerCaseWord = word.toLowerCase();
		return lowerCaseWord.startsWith("http://")
				|| lowerCaseWord.startsWith("www.")
				|| lowerCaseWord.startsWith("ftp://")
				|| lowerCaseWord.startsWith("https://")
				|| lowerCaseWord.startsWith("ftps://");
	}
	
	/**
	 * Returns true if this word contains all upper case characters
	 * @param word single word
	 * @return true if it is a uppercase word
	 */
	public final static boolean isUpperCaseWord(String word) {
		for (int i = word.length() - 1; i >= 0; i--) {
			if (Character.isLowerCase(word.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
   /**
	 * Returns true if this word contains mixed case characters
	 * @param word single word for mixed case check
	 * @param startsSentence
	 *            True if this word is at the start of a sentence
	 * @return true if it is amixed case word
	 */
	public final static boolean isMixedCaseWord(String word,
			boolean startsSentence) {
		int strLen = word.length();
		boolean isUpper = Character.isUpperCase(word.charAt(0));
		// Ignore the first character if this word starts the sentence and the
		// first character was upper cased, since this is normal behaviour
		if ((startsSentence) && isUpper && (strLen > 1))
			isUpper = Character.isUpperCase(word.charAt(1));
		if (isUpper) {
			for (int i = word.length() - 1; i > 0; i--) {
				if (Character.isLowerCase(word.charAt(i))) {
					return true;
				}
			}
		} else {
			for (int i = word.length() - 1; i > 0; i--) {
				if (Character.isUpperCase(word.charAt(i))) {
					return true;
				}
			}
		}
		return false;
	}


}
