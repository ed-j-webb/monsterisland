package net.edwebb.jim.factory;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import net.edwebb.jim.data.MapData;
import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.data.Decoder;

public class CSVDataFactory implements DataFactory {

	// A file filter to load csv files
	private static final FileFilter csvFilter = new FileFilter() {

		@Override
		public String getDescription() {
			return "Comma Separated Variables (*.csv)";
		}

		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory()
					|| pathname.getName().toLowerCase().endsWith(".csv");
		}
	};
	
	// A Set of strings holding features that have not been matched to a feature in jim
	private Set<String> unmatched;
	
	@Override
	public Set<String> getUnmatched() {
		return unmatched;
	}

	@Override
	public FileFilter getFilter() {
		return csvFilter;
	}

	@Override
	public String getSuffix() {
		return ".csv";
	}

	@Override
	public boolean isCreate() {
		return true;
	}

	@Override
	public boolean isSave() {
		return true;
	}

	@Override
	public boolean isTranslate() {
		return false;
	}

	/**
	 * Create a MapData object from a csv file. The csv file must contain 3
	 * elements per record separated by a comma. The elements are X, Y, Data.
	 * The Data is Bad News standard format which is a leading character
	 * denoting terrain followed by one or more 4 character strings. These
	 * strings are %XXX for locations, @nnn for creatures, $xxx for plants and
	 * #nnn for notes. If the Data section begins with a # symbol it is a note for the
	 * square and will be added to the MapData. If the Data section begins with
	 * a + symbol it is the co-ordinates used by the map. The x and y fields are
	 * the offset from the arbitrary default origin which is to the south-west
	 * of the island.
	 * 
	 * @param csv The csv file containing the data
	 * @return a MapData object populated from the csv file
	 * @throws IOException if the csv file cannot be read
	 */
	public MapData createFrom(File csv) throws IOException {
		unmatched = new HashSet<String>();
		
		int top = -1000;
		int left = 1000;
		int bottom = 1000;
		int right = -1000;

		String line = null;
		int x;
		int y;

		BufferedReader reader = new BufferedReader(new FileReader(csv));
		try {
			// Find out the bounds of the map
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(",");
				if (!part[2].startsWith("+")) {
					x = Integer.parseInt(part[0]);
					if (x < left) {
						left = x;
					}
					if (x > right) {
						right = x;
					}
					y = Integer.parseInt(part[1]);
					if (y < bottom) {
						bottom = y;
					}
					if (y > top) {
						top = y;
					}
				}
			}
		} finally {
			reader.close();
		}

		// Create an empty MapData to hold the map
		MapData data = new MapData(top, left, right - left + 1, top - bottom + 1);

		reader = new BufferedReader(new FileReader(csv));
		try {
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(",");
				if (part[2].length() > 0) {
					x = Integer.parseInt(part[0]);
					y = Integer.parseInt(part[1]);
	
					if (part[2].startsWith("#")) {
						StringBuilder sb = new StringBuilder(part[2].substring(1));
						for (int i = 3; i < part.length; i++) {
							sb.append(",");
							sb.append(part[i]);
						}
						data.setSquareNotes(x - left, top - y, sb.toString());
					} else if (part[2].startsWith("+")) {
						data.setOffset(x, y, part[2].substring(1));
					} else {
						// Read the whole of the data into a short array
						int z = 1 + (part[2].length() - 1) / 4;
						if (part[2].contains("#")) {
							z--;
						}
						short[] sqr = new short[z];
	
						// Terrain
						sqr[0] = Decoder.shortFromChar(part[2]);
						int index = 1;
						int pos = 1;
						while (pos < part[2].length()) {
							String bit = part[2].substring(pos, pos + 4);
							if (bit.startsWith("#")) {
								// Ignore notes
							} else if (DataStore.getInstance().isValid(bit.substring(1))) {
								if (bit.startsWith("%")) {
									sqr[index++] = Decoder.shortFromString(bit.substring(1));
								} else if (bit.startsWith("$")) {
									sqr[index++] = Decoder.shortFromString(bit.substring(1));
								} else if (bit.startsWith("@")) {
									sqr[index++] = Decoder.shortFromInt(bit.substring(1));
								} else {
									unmatched.add(bit);
								}
							} else {
								unmatched.add(bit);
							}
	
							pos += 4;
						}
						
						data.setSquare(x - left, top - y, sqr);
					}
				}
			}
			data.setDirty(false);
			return data;
		} finally {
			reader.close();
		}
	}

	/**
	 * Saves the MapData to a csv file. See createFromCSV for details on the file format.
	 * @param data the MapData to save
	 * @param csv the csv file to create
	 * @throws IOException if the file cannot be written to
	 */
	public void saveTo(MapData data, File csv) throws IOException {
		FileWriter writer = new FileWriter(csv);
		for (int x = 0; x < data.getWidth(); x++) {
			for (int y = data.getHeight() - 1; y >= 0; y--) {
				short[] sqr = data.getSquare(x, y);
				if (sqr != null && sqr.length > 0) {
					writer.write(Integer.toString(x + data.getLeft()));
					writer.write(",");
					writer.write(Integer.toString(data.getTop() - y));
					writer.write(",");
					writer.write(Decoder.charFromShort(sqr[0]));
					for (int i = 1; i < sqr.length; i++) {
						if (sqr[i] < 0) {
							writer.write("%");
							writer.write(Decoder.stringFromShort(sqr[i]));
						} else if (sqr[i] >= 1024) {
							writer.write("$");
							writer.write(Decoder.stringFromShort(sqr[i]));
						} else {
							writer.write("@");
							writer.write(Decoder.intFromShort(sqr[i]));
						}
					}
					writer.write("\n");
				}
			}
		}
		Iterator<Map.Entry<Point, String>> it = data.getMapNotesIterator();
		while (it.hasNext()) {
			Map.Entry<Point, String> note = it.next();
			writer.write(Integer.toString(note.getKey().x + data.getLeft()));
			writer.write(",");
			writer.write(Integer.toString(data.getTop() - note.getKey().y));
			writer.write(",#");
			writer.write(note.getValue());
			writer.write("\n");
		}
		if (data.getOffset() != null) {
			writer.write(Integer.toString(data.getOffX()));
			writer.write(",");
			writer.write(Integer.toString(data.getOffY()));
			writer.write(",+");
			writer.write(data.getOffset());
			writer.write("\n");
		}
		writer.close();
		data.setDirty(false);
	}

	@Override
	public String listTranslations(File file) throws IOException {
		throw new UnsupportedOperationException("CSV files do not have a translation file");
	}
	
	
}
