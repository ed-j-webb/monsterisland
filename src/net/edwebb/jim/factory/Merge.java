package net.edwebb.jim.factory;

import java.awt.Point;

import net.edwebb.jim.data.Decoder;
import net.edwebb.jim.data.MapData;

/**
 * A class that can merge two MapData objects into a new one
 * @author Ed Webb
 *
 */
public class Merge {
	
	/**
	 * Creates a new MapData object that is the sum of the two maps. Where there
	 * is a conflict of terrain the newData terrain will take precedence. The
	 * merged map will have the co-ordinate system of the baseData
	 * 
	 * @param newData a MapData that contains more or different data to the base map
	 * @param baseData a MapData that is to be used as the base for the diff
	 * @return a MapData containing the sum of features in newData and baseData
	 */
	public static MapData merge(MapData newData, MapData baseData) {
		if (newData.getCoord() == null || baseData.getCoord() == null) {
			throw new IllegalArgumentException("Both MapData objects must have their co-ordinate systems set");
		}

		// Calculate the offset between the top-left of the maps
		Point off = newData.getCoord().getOffset(baseData.getCoord());

		// Figure out the dimensions of the new MapData
		short top = (short) Math.max(baseData.getTop(), newData.getTop() + off.y);
		short left = (short) Math.min(baseData.getLeft(), newData.getLeft() + off.x);
		short right = (short) Math.max(baseData.getLeft() + baseData.getWidth(), newData.getLeft() + off.x + newData.getWidth());
		short bottom = (short) Math.min(baseData.getTop() - baseData.getHeight(), newData.getTop() + off.y - newData.getHeight());

		MapData data = new MapData(top, left, right - left, top - bottom);

		Point baseOff = new Point(left - baseData.getLeft(), baseData.getTop() - top);
		Point newOff = new Point(left - newData.getLeft() + off.x, newData.getTop() - top - off.y);

		data.setCoord(baseData.getCoord());
		for (int x = 0; x < data.getWidth(); x++) {
			for (int y = 0; y < data.getHeight(); y++) {
				data.setSquare(x, y, mergeSquare(newData.getSquare(x + newOff.x, y+ newOff.y), baseData.getSquare(x + baseOff.x, y + baseOff.y)));
				data.setSquareNotes(x, y, mergeNotes(newData.getSquareNotes(x + newOff.x, y + newOff.y), baseData.getSquareNotes(x + baseOff.x, y + baseOff.y)));
			}
		}
		return data;
	}

	/**
	 * Merges a pair of squares together. If a is null or empty it returns b. If b is null or empty it returns a. It prefers the terrain of a over b.
	 * @param a the extra square
	 * @param b the base square
	 * @return the combination of the two squares
	 */
	private static short[] mergeSquare(short[] a, short[] b) {
		if (a == null || a.length == 0) {
			return b;
		}
		if (b == null || b.length == 0) {
			return a;
		}

		int k = a.length - 1;
		// Set all features in a that are also in b to 0
		for (int i = 1; i < a.length; i++) {
			for (int j = 1; j < b.length; j++) {
				if (a[i] == b[j]) {
					a[i] = 0;
					k--;
					break;
				}
			}
		}

		// Create an array that is the size of the number of features in b plus
		// those left in a
		short[] c = new short[b.length + k];

		// Set the terrain
		short low;
		short high;
		
		low = Decoder.shortLowByte(a[0]);
		if (low == 0 || low == Decoder.shortFromChar("U")) {
			low = Decoder.shortLowByte(b[0]);
		}
		
		// Combine the flags
		high = (short)(Decoder.shortHighByte(a[0]) | Decoder.shortHighByte(b[0]));

		c[0] = Decoder.shortFromBytes(high, low);
		
		// Copy the features from b to c
		for (k = 1; k < b.length; k++) {
			c[k] = b[k];
		}

		// Copy the features from a to c
		for (int i = 1; i < a.length; i++) {
			if (a[i] != 0) {
				c[k] = a[i];
				k++;
			}
		}
		return c;
	}

	/**
	 * Returns the merging of two strings. If a is null or empty it returns b. If b is null or empty it returns a. It returns the two notes separated
	 * by a newline character.
	 * @param a the extra note
	 * @param b the base note
	 * @return the concatenation of the two notes
	 */
	private static String mergeNotes(String a, String b) {
		if (a == null || a.length() == 0) {
			return b;
		}
		if (b == null || b.length() == 0) {
			return a;
		}
		if (a.equals(b)) {
			return a;
		} else {
			return b + "\n" + a;
		}
	}
}
