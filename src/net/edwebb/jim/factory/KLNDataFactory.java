package net.edwebb.jim.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import net.edwebb.jim.data.Decoder;
import net.edwebb.jim.data.FeatureData;
import net.edwebb.jim.data.MapData;

public class KLNDataFactory implements DataFactory {

	private Map<String, String> klntrans = new HashMap<String, String>();
	
	// A file filter to load kln files
	private static final FileFilter klnFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith(".kln");
		}

		@Override
		public String getDescription() {
			return "Shadowmapper Files (*.kln)";
		}
	};
	
	public KLNDataFactory(File translationFile) throws IOException {
		klntrans = readTranslationFile(translationFile);
	}
	
	// A Set of strings holding features that have not been matched to a feature in jim
	private Set<String> unmatched;
	
	@Override
	public Set<String> getUnmatched() {
		return unmatched;
	}

	@Override
	public FileFilter getFilter() {
		return klnFilter;
	}

	@Override
	public String getSuffix() {
		return ".kln";
	}

	@Override
	public boolean isCreate() {
		return true;
	}

	@Override
	public boolean isSave() {
		return false;
	}

	@Override
	public boolean isTranslate() {
		return true;
	}

	private Map<String, String> readTranslationFile(File f) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		Map<String, String> trans = new HashMap<String, String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(",");
				if (part.length == 2) {
					trans.put(part[0], part[1]);
				}
			}
		} finally {
			reader.close();
		}
		return trans;
	}
	
	/**
	 * Create a MapData object from a kln (Shadowmapper) file. The klntrans.csv file is used
	 * to translate the codes in the file to jim codes. The Trail information in the file is
	 * simplified to a Trail location in the square rather than the TSW TNE which would show
	 * the directions in which the trail runs. There must be a location in the locations.csv
	 * file with the code TRL for this to work.
	 * 
	 * @param kln The kln file containing the data
	 * @return a MapData object populated from the kln file
	 * @throws IOException if the kln file cannot be read
	 */
	public MapData createFrom(File kln) throws IOException {
		unmatched = new HashSet<String>();

		BufferedReader reader = new BufferedReader(new FileReader(kln));

		String line = null;
		String[] part;
		int x = 0;
		int y = 0;

		// Find out the bounds of the map
		int top = -1000;
		int left = 1000;
		int bottom = 1000;
		int right = -1000;
		int count = 0;
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() == 0 || line.startsWith("#")) {
				// Ignore this line
			} else if (line.startsWith("[")) {
				part = line.substring(1, line.length() - 1).split(",");
				x = Integer.parseInt(part[1]);
				if (x < left) {
					left = x;
				}
				y = Integer.parseInt(part[0]);
				if (y < bottom) {
					bottom = y;
				}
				if (y > top) {
					top = y;
				}
			} else {
				count = line.length() - line.replace("\\|", "").length() + 1;
				if (x + count > right) {
					right = x + count;
				}
			}
		}
		reader.close();

		short TRAIL = Decoder.shortFromString("TRL");
		if (FeatureData.getInstance().getFeature(TRAIL) == null) {
			throw new IllegalStateException("Cannot find the Trail feature using code TRL");
		}

		short[] temp = getIDsForKLN("*");
		if (temp.length != 1) {
			throw new IllegalStateException("Cannot find a translation for * in the klntrans.csv file");
		}
		short VISITED = (short)(temp[0] - 48);

		short s = 0; 

		// Create an empty MapData to hold the map
		MapData data = new MapData(top, left, right - left + 2, top - bottom + 1);

		List<Short> list = new ArrayList<Short>();

		reader = new BufferedReader(new FileReader(kln));
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() == 0 || line.startsWith("#")) {
				// Ignore this line
			} else if (line.startsWith("[")) {
				part = line.substring(1, line.length() - 1).split(",");
				x = Integer.parseInt(part[1]);
				y = Integer.parseInt(part[0]);
			} else {
				// Each square is separated by a pipe |
				part = line.split("\\|");
				for (int i = 0; i < part.length; i++) {
					// Each feature is separated by a comma ,
					if (part[i].length() > 0) {
						String[] bits = part[i].split(",");
						list.clear();
						// Terrain always comes first
						if (bits[0].trim().length() > 0) {
							String terr = bits[0].replaceAll("\\*", "");
							short[] id = getIDsForKLN(terr);
							if (id.length > 0 && FeatureData.getInstance().isValid(id[0])) {
								s = Short.valueOf(id[0]);
							} else {
								unmatched.add(terr);
								s = 0;
							}
						
							if (bits[0].endsWith("*")) {
								s |= (1 << (VISITED + 8));
							}
							list.add(s);
						} else {
							list.add(Decoder.ZERO);
						}
						// Then other features
						for (int j = 1; j < bits.length; j++) {
							if (bits[j].startsWith("C")) {
								list.add(Short.valueOf(bits[j].substring(1)));
							} else if (bits[j].startsWith("T") && !list.contains(TRAIL)) {
								list.add(TRAIL);
							} else {
								short[] id = getIDsForKLN(bits[j]);
								for (int k = 0; k < id.length; k++) {
									if (id[k] != 0 && FeatureData.getInstance().isValid(id[k])) {
										list.add(Short.valueOf(id[k]));
									} else {
										unmatched.add(bits[j]);
									}
								}
							}
						}
						short[] sqr = new short[list.size()];
						for (int j = 0; j < list.size(); j++) {
							sqr[j] = list.get(j);
						}
						if (x - left >= data.getWidth() || top - y >= data.getHeight()) {
							System.out.println("beyond the bounds of the map! " + (x - left) + " > " + data.getWidth() + ", " + (top - y) + " > " + data.getHeight());
						}
						try {
							data.setSquare(x - left, top - y, sqr);
						} catch (ArrayIndexOutOfBoundsException e) {
							//e.printStackTrace();
						}
					}
					x++;
				}
			}
		}
		data.setDirty(false);
		return data;
	}

	@Override
	public void saveTo(MapData data, File file) throws IOException {
		throw new UnsupportedOperationException("Cannot save to kln files");
		
	}

	private short[] getIDsForKLN(String name) {
		String code = klntrans.get(name);
		if (code == null) {
			return new short[0];
		} else if (code.trim().length() == 1) {
			return new short[] {Decoder.shortFromChar(code)};
		} else {
			short[] id = new short[code.length()/3];
			int i = 0;
			while(i*3 < code.length()) {
				id[i] = Decoder.shortFromString(code.substring(i*3, i*3+3));
				i++;
			}
			return id;
		}
	}

	@Override
	public String listTranslations(File kln) throws IOException {
		Set<String> terr = new HashSet<String>();
		Set<String> feat = new HashSet<String>();
		
		String line = null;
		String[] part;

		BufferedReader reader = new BufferedReader(new FileReader(kln));
		try {
			
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() == 0 || line.startsWith("#")) {
					// Ignore this line
				} else if (line.startsWith("[")) {
					// Ignore this line
				} else {
					// Each square is separated by a pipe |
					part = line.split("\\|");
					for (int i = 0; i < part.length; i++) {
						// Each feature is separated by a comma ,
						if (part[i].length() > 0) {
							String[] bits = part[i].split(",");
							// Terrain always comes first
							if (bits[0].trim().length() > 0) {
								terr.add(bits[0].replaceAll("\\*", ""));
							}
							// Then other features
							for (int j = 1; j < bits.length; j++) {
								if (bits[j].startsWith("C")) {
									// Ignore Creatures
								} else if (bits[j].startsWith("T")) {
									// Ignore Trails
								} else {
									feat.add(bits[j]);
								}
							}
						}
					}
				}
			}
		} finally {
			reader.close();
		}

		StringBuilder sb = new StringBuilder();
		List<String> t = new ArrayList<String>(terr);
		Collections.sort(t);
		List<String> f = new ArrayList<String>(feat);
		Collections.sort(f);
		
		Iterator<String> it = t.iterator();
		while (it.hasNext()) {
			sb.append(it.next());
			sb.append(",\n");
		}
		it = f.iterator();
		while (it.hasNext()) {
			sb.append(it.next());
			sb.append(",\n");
		}
		sb.append("*,\n");
		
		return sb.toString();
	}
}
