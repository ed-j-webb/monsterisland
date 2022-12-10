package net.edwebb.jim.factory;

import java.awt.Point;

import net.edwebb.jim.data.Decoder;
import net.edwebb.jim.data.MapData;


/**
 * A class that can diff two MapData objects into a new one
 * @author Ed Webb
 *
 */
public class Diff {
	/**
	 * Create a new MapData object that is the difference between the new and
	 * base maps. The diff map will have the co-ordinate system of the base map
	 * and only contain features that are in the new map but not in the base
	 * map. Terrain will only be in the diff map if it is different in the new
	 * map from the base map. This diff can then be used with the merge function
	 * to merge the differences into the base map.
	 * 
	 * @param newData a MapData that contains more or different data to the base map
	 * @param baseData a MapData that is to be used as the base for the diff
	 * @return a MapData containing the differences in newData from baseData
	 */
	public static MapData diff(MapData newData, MapData baseData) {
		if (newData.getCoord() == null || baseData.getCoord() == null) {
			throw new IllegalArgumentException("Both MapData objects must have their co-ordinate systems set");
		}

		// Calculate the offset between the top-left of the maps
		Point off = newData.getCoord().getOffset(baseData.getCoord());

		MapData data = new MapData(newData.getTop() - off.y, newData.getLeft()
				- off.x, newData.getWidth(), newData.getHeight());

		off.x = newData.getLeft() + off.x - baseData.getLeft();
		off.y = newData.getTop() - off.y - baseData.getTop();

		data.setCoord(baseData.getCoord());
		for (int x = 0; x < newData.getWidth(); x++) {
			for (int y = 0; y < newData.getHeight(); y++) {
				data.setSquare(x, y, diffSquare(newData.getSquare(x, y), baseData.getSquare(x + off.x, y - off.y)));
				data.setSquareNotes(x, y, diffNote(newData.getSquareNotes(x, y),	baseData.getSquareNotes(x + off.x, y - off.y)));
			}
		}

		return data;
	}

	/**
	 * Create a diff between a pair of squares. if a or b is null or empty it returns a. If the terrain of a matches the terrain of b
	 * or the terrain of a is U then the first index in the return array will be zero. The rest of the returned array will contain
	 * features that are in a but not in b.
	 * @param a the different square's data
	 * @param b the base square's data
	 * @return everything that is different in a from b
	 */
	private static short[] diffSquare(short[] a, short[] b) {
		if (a == null || a.length == 0 || b == null || b.length == 0) {
			return a;
		}
		
		short low;
		short high;
		// Check if terrain matches
		low = Decoder.shortLowByte(a[0]);
		if (low == Decoder.shortLowByte(b[0]) || low == Decoder.shortFromChar("U")) {
			low = 0;
		}
		
		// Only take flags that are set in a but not b
		high = (short)(Decoder.shortHighByte(a[0]) & ~Decoder.shortHighByte(b[0]));
		
		a[0] = Decoder.shortFromBytes(high, low);
		
		int k = a.length;
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

		// Create an array that is the size of the number of features left in a
		short[] c = new short[k];

		// Copy the features from a to c
		c[0] = a[0];
		k = 1;
		for (int i = 1; i < a.length; i++) {
			if (a[i] != 0) {
				c[k] = a[i];
				k++;
			}
		}
		return c;
	}

	/**
	 * Create a diff between a pair of strings. If a or b are null or empty it returns a. If a is equal to b it returns null else it returns a
	 * @param a the extra string
	 * @param b the base string
	 * @return a if it is different to b
	 */
	private static String diffNote(String a, String b) {
		if (a == null || a.length() == 0 || b == null || b.length() == 0) {
			return a;
		}
		if (a.equals(b)) {
			return null;
		} else {
			return a;
		}
	}
}
