package net.edwebb.mi.data;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * A static class that is used to convert to and from the short primitive data type. This class is used to decode and encode data to the format used
 * by the MapData class
 * 
 * @author Ed Webb
 *
 */
public class Decoder {

	public static final short ZERO = (short)0;
	
	/**
	 * Turns a single character into a short. If the text is U it returns 0 else it returns the character's code point
	 * @param text the character to transform
	 * @return the code Point of the character or 0 if it is a U
	 */
	public static short shortFromChar(String text) {
		if (text.equals("U")) {
			return 0;
		} else {
			return (short) text.codePointAt(0);
		}
	}
	
	/**
	 * Turns a short into a character. If the short is 0 it returns "U" else it returns the character that is at the UTF-16
	 * code point
	 * @param num the UTF-16 code point or zero
	 * @return the character at that code point or U if zero
	 */
	public static String charFromShort(short num) {
		if (num == 0) {
			return "U";
		} else {
			return String.valueOf(Character.toChars(num));
		}
	}	
	
	/**
	 * Turns a string of 3 characters into a short. The string must be either all UPPER or all lower case and only from the 26 latin characters.
	 * @param text the three character string
	 * @return a short that uniquely represents that string
	 */
	public static short shortFromString(String text) {
		int a = text.codePointAt(0);
		int b = text.codePointAt(1);
		int c = text.codePointAt(2);
		int d = 0;
		if (a > 96) {
			a -= 97;
			b -= 97;
			c -= 97;
		} else {
			a -= 64;
			b -= 64;
			c -= 64;
			d = 1;
		}
		return (short)(c + (b << 5) + (a << 10) + (d << 15));
	}

	/**
	 * Turns a short into a 3 character string. The string returned will be either all UPPER or all lower case and only from the 26 latin characters.
	 * @param num the short
	 * @return a 3 character string
	 */
	public static String stringFromShort(short num) {
		int cd = num < 0 ? 64 : 97;
		int c = cd + (num & 31);
		num = (short) (num >> 5);
		int b = cd + (num & 31);
		num = (short) (num >> 5);
		int a = cd + (num & 31);
		return String.valueOf(Character.toChars(a)) + String.valueOf(Character.toChars(b)) + String.valueOf(Character.toChars(c));
	}
	
	/**
	 * Turns an string representation of a number into a short. The string must only contain numbers (the minus sign is supported but invalid for
	 * this application. 
	 * @param text the number represented as a string
	 * @return a short representation of that string
	 */
	public static short shortFromInt(String text) {
		return Short.parseShort(text);
	}
	
	/**
	 * Turns a short into a string representation of the number. The short may be negative although this is invalid for this application.
	 * @param num the short
	 * @return a string representation of the number
	 */
	public static String intFromShort(short num) {
		return Short.valueOf(num).toString();
	}

	public static short shortLowByte(short num) {
		return (short)(num & 0xff);
	}
	
	public static short shortHighByte(short num) {
		return (short)((num >> 8) & 0xff);
	}
	
	public static short shortFromBytes(short high, short low) {
		return (short)((high << 8) | low);
	}
	
	public static short shortFromShorts(short high, short low) {
		return (short)((high & 0xff00) | (low & 0xff));
	}

	public static short shortFromLittleEndian(byte[] b) {
		if (b.length != 2) {
			throw new IllegalArgumentException("This method only accepts an array of 2 bytes");
		}
		return (short) ((b[0] & 0xFF) | (b[1] & 0xFF) << 8);		
	}

	public static int intFromLittleEndian(byte[] b) {
		if (b.length != 4) {
			throw new IllegalArgumentException("This method only accepts an array of 4 bytes");
		}
		return ((b[3] & 0xFF) << 24) | ((b[2] & 0xFF) << 16) | ((b[1] & 0xFF) << 8) | (b[0] & 0xFF);		
	}

	public static Color makeColour(String text) {
		return Color.decode(text);
	}
	
	public static Image makeImage(String text, File imageFolder) throws IOException {
		return ImageIO.read(new File(imageFolder, text));
	}
	
	public static ImageIcon makeImageIcon(String text, File imageFolder) throws IOException {
		return new ImageIcon(makeImage(text, imageFolder));
	}

	public static void main(String[] args) {
		short test = -32433;
		System.out.println((short)(test & 0xff));
		System.out.println((short)(test >> 8) & 0xff);
		System.out.println(shortLowByte(test));
		System.out.println(shortHighByte(test));
		System.out.println(shortFromBytes((short)129,(short)79));
		System.out.println(shortFromShorts((short)-32433, (short)1));
	}
	
}

