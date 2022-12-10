package net.edwebb.mi.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that can read the text of a turn.
 * 
 * @author Ed Webb
 *
 */
public class TurnReader {
	
	// A list of numbers in order
	private static final List<String> numbers = new ArrayList<String>();
	static {
		numbers.add("zero");
		numbers.add("one");
		numbers.add("two");
		numbers.add("three");
		numbers.add("four");
		numbers.add("five");
		numbers.add("six");
		numbers.add("seven");
		numbers.add("eight");
		numbers.add("nine");
		numbers.add("ten");
		numbers.add("eleven");
		numbers.add("twelve");
		numbers.add("thirteen");
		numbers.add("fourteen");
		numbers.add("fifteen");
		numbers.add("sixteen");
	}

	// An array of words in the turn
	private String[] words;
	
	// The position of the pointer in the turn
	private int pos;

	/**
	 * Create a turn reader from the contents of a file. The file must contain text.
	 * @param file the file to read
	 * @throws FileNotFoundException if the file cannot be found
	 * @throws IOException if the file cannot be read from
	 */
	public TurnReader(File file) throws FileNotFoundException, IOException {
		this(new FileReader(file));
	}

	/**
	 * Create a turn reader from the Reader. The reader must contain text.
	 * @param reader the reader to use
	 * @throws IOException if the reader cannot be read from
	 */
	public TurnReader(Reader reader) throws IOException {
		BufferedReader br = new BufferedReader(reader);
		StringBuffer sb = new StringBuffer();
		String line = br.readLine();
		while (line != null) {
			sb.append(line);
			sb.append("\n");
			line = br.readLine();
		}
		br.close();
		read(sb.toString());
	}

	/**
	 * Create a turn reader from the string.
	 * @param text the string containing the text
	 */
	public TurnReader(String text) {
		read(text);
	}
	
	/**
	 * Splits the text into words and sets the pointer to the first word. This is a utility method called by constructors only. 
	 * @param text the text of the turn.
	 */
	private void read(String text) {
		words = text.split("\\s");
		pos = 0;
	}
	
	/**
	 * Returns true if the words at the current pointer match those in the phrase.
	 * @param phrase the phrase to match
	 * @return true if the phrase matches what is at the current point in the turn
	 */
	public boolean match(String phrase) {
		return match(phrase, 0);
	}
	
	/**
	 * Returns true if the words at the pointer (plus or minus the offset) match those in the phrase. Phrases can contain the wildcard 
	 * character "*" at the beginning and/or end of any word in the phrase. This match is case sensitive.
	 * He* - any word starting He (Here, Her, Heather)
	 * *er - any word ending er (Her, other, heather)
	 * * - any word
	 * ** - two asterisks (**)
	 * *er* - any word containing er (error, her, here)
	 * @param phrase the phrase to match
	 * @param offset the number of words to look forward from the current pointer. If the offset is negative it will look back from the current pointer
	 * @return true if the phrase matches what is at the given point in the turn
	 */
	public boolean match(String phrase, int offset) {
		if (phrase.contains(" ")) {
			String[] bits = phrase.split("\\s");
			for (int i = 0; i < bits.length; i++) {
				if (pos + offset + i >= words.length) {
					return false;
				}
				if (bits[i].equals("**")) {
					return words[pos + offset].equals(phrase);
				} else if (bits[i].equals("*")) {
					// continue
				} else if (bits[i].startsWith("*") && bits[i].endsWith("*")) {
					if (!words[pos + offset + i].contains(bits[i].replaceAll("\\*", ""))) {
						return false;
					}
				} else if (bits[i].startsWith("*")) {
					if (!words[pos + offset + i].endsWith(bits[i].replaceAll("\\*", ""))) {
						return false;
					}
				} else if (bits[i].endsWith("*")) {
					if (!words[pos + offset + i].startsWith(bits[i].replaceAll("\\*", ""))) {
						return false;
					}
				} else if (!bits[i].equals(words[pos + offset + i])) {
					return false;
				}
			}
			return true;
		} else {
			if (phrase.equals("**")) {
				return words[pos + offset].equals(phrase);
			} else if (phrase.startsWith("*") && phrase.endsWith("*")) {
				return words[pos + offset].contains(phrase.replaceAll("\\*", ""));
			} else if (phrase.startsWith("*")) {
				return words[pos + offset].endsWith(phrase.replaceAll("\\*", ""));
			} else if (phrase.endsWith("*")) {
				return words[pos + offset].startsWith(phrase.replaceAll("\\*", ""));
			} else {
				return words[pos + offset].equals(phrase);
			}
		}
	}
	
	public boolean startsWith(String prefix) {
		return words[pos].startsWith(prefix);
	}

	public boolean startsWith(String prefix, int offset) {
		return words[pos + offset].startsWith(prefix);
	}

	public boolean endsWith(String suffix) {
		return words[pos].endsWith(suffix);
	}
	
	public boolean endsWith(String suffix, int offset) {
		return words[pos + offset].endsWith(suffix);
	}

	public void increment() {
		pos++;
	}
	
	public void increment(int inc) {
		pos += inc;
	}
	
	public boolean hasNext() {
		return (pos < words.length);
	}
	
	public String get() {
		return words[pos];
	}
	
	public String get(int offset) {
		return words[pos + offset];
	}
	
	public int getNumber(int offset, int defaultNumber) {
		int number = getNumber(offset); 
		if (number == Integer.MIN_VALUE) {
			return defaultNumber;
		} else {
			return number;
		}
	}
	
	public int getNumber() {
		return getNumber(0);
	}
	
	public int getNumber(int offset) {
		String digits = words[pos + offset].replaceAll("[^0-9\\-]", "");
		if (digits.length() > 0) {
			return Integer.parseInt(digits);
		} else {
			String num = words[pos + offset].replaceAll("[^A-Za-z]", "");
			if (num.equals("a") || num.equals("an") || num.equals("the")) {
				return 1;
			} else if (num.equals("some")) {
				return 3;
			} else {
				if (numbers.contains(num)) {
					return numbers.indexOf(num);
				} else {
					return Integer.MIN_VALUE;
				}
			}
		}
	}
	
	public int getPosition() {
		return pos;
	}
}
