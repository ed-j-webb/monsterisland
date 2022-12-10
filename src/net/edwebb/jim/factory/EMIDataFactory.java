package net.edwebb.jim.factory;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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

/**
 * A DataFactory that can read and write EditMI dat files
 * @author Ed Webb
 *
 */
public class EMIDataFactory implements DataFactory {

	// A file filter to load emi files
	private static final FileFilter emiFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith(".dat");
		}

		@Override
		public String getDescription() {
			return "Edit MI Files (*.dat)";
		}
	};
	
	private static final Integer TERRAIN = 0;
	private static final Integer LOCATIONS = 1;
	private static final Integer PLANTS = 2;
	
	private static short VISITED;
	
	private Map<String, String> emitrans = new HashMap<String, String>();
	
	// A Set of strings holding features that have not been matched to a feature in jim
	private Set<String> unmatched;
	
	public EMIDataFactory(File translationFile) throws IOException {
		emitrans = readTranslationFile(translationFile);

		String temp = getCodeForEMI("*");
		if (temp != null && temp.length() != 1) {
			throw new IllegalStateException("Cannot find a translation for * in the emitrans.csv file");
		}
		VISITED = Short.valueOf(temp);
	}
	
	private Map<String, String> readTranslationFile(File f) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		Map<String, String> trans = new HashMap<String, String>();
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(f));
		try {
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(",");
				if (part.length == 2) {
					trans.put(part[0], part[1]);
				}
			}
			return trans;
		} finally {
			reader.close();
		}
	}
	
	@Override
	public Set<String> getUnmatched() {
		return unmatched;
	}

	@Override
	public FileFilter getFilter() {
		return emiFilter;
	}

	@Override
	public String getSuffix() {
		return ".dat";
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

	/**
	 * Creates a MapData object from a file containing an EditMI map. The folder that the dat file is located in must also contain
	 * TERRAIN.DAT, PLANTS.DAT, STRUCT.DAT and SETUP.TXT. It uses the emitrans.csv file to translate EditMI code to jim codes. The 
	 * SETUP.TXT file must contain a default XY: entry with the DOWN attribute for the map data to be read.
	 * @param dat the file to read
	 * @return a MapData object based on the contents of the file
	 * @throws IOException
	 */
	public MapData createFrom(File dat) throws IOException {
		unmatched = new HashSet<String>();

		Map<Integer, List<String>> lookups = new HashMap<Integer, List<String>>();
		lookups.put(TERRAIN, readEMITerrain(dat.getParentFile()));
		lookups.put(LOCATIONS, readEMILocations(dat.getParentFile()));
		lookups.put(PLANTS, readEMIPlants(dat.getParentFile()));
		
		Map<Integer, String> emiNotes = readEMINotes(dat.getParentFile());

		Point p = readEMITopLeft(dat.getParentFile());

		DataInputStream dis = new DataInputStream(new FileInputStream(dat));
		int height = readEMIShort(dis);
		int width = readEMIShort(dis);

		MapData data = new MapData(p.y, p.x, width, height);
		List<String> notes = new ArrayList<String>();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				data.setSquare(x, y, readEMISquare(dis, lookups, emiNotes, notes));
				data.setSquareNotes(x, y, readEMINotes(notes));
			}
		}

		return data;
	}

	public void saveTo(MapData data, File dat) throws IOException {
		throw new UnsupportedOperationException("Cannot save to Shadowmapper file format");
	}
	
	/**
	 * Reads a little endian short from the DataInputStream
	 * @param dis the DataInputStream
	 * @return a short created from the next two bytes in the stream
	 * @throws IOException if the stream cannot be read from
	 */
	private short readEMIShort(DataInputStream dis) throws IOException {
		byte[] b = new byte[2];
		dis.readFully(b);
		return Decoder.shortFromLittleEndian(b);
	}

	/**
	 * Reads a square of data from the EditMI dat file and returns an array of shorts 
	 * @param dis the input stream the data is being read from
	 * @param lookups a map of lookup Lists (TERRAIN, LOCATIONS and PLANTS)
	 * @param emiNotes a map of EditMI map notes
	 * @param notes a List to store the square's notes in (Used as an OUT parameter)
	 * @return an array of shorts representing the square and its contents
	 * @throws IOException if the input stream cannot be read from
	 */
	private short[] readEMISquare(DataInputStream dis, Map<Integer, List<String>> lookups, Map<Integer, String> emiNotes, List<String> notes) throws IOException {
		byte b = 0;
		short s = 0;
		notes.clear();
		short[] sqr = new short[5];

		int k = 1;

		// Read the terrain
		b = dis.readByte();
		if (b > 0) {
			if (b < lookups.get(TERRAIN).size()) {
				String t = lookups.get(TERRAIN).get(b);
				String terr = getCodeForEMI("*" + t);
				if (terr != null && FeatureData.getInstance().isValid(terr)) {
					sqr[0] = Decoder.shortFromChar(terr);
				} else {
					unmatched.add(t);
				}
			} else {
				unmatched.add(Byte.toString(b));
			}
		}

		b = dis.readByte();

		// Read the scouted flag
		if (b < 0) {
			b = (byte) (b & 0x7f);
			sqr[0] |= (1 << (VISITED + 8));
		}

		// Read the location
		if (b > 0) {
			if (b < lookups.get(LOCATIONS).size()) {
				String l = lookups.get(LOCATIONS).get(b);
				String loc = getCodeForEMI(l);
				if (loc != null && FeatureData.getInstance().isValid(loc)) {
					sqr[1] = Decoder.shortFromString(loc);
					k++;
				} else {
					unmatched.add(l);
				}
			} else {
				unmatched.add("Structure:" + Byte.toString(b));
			}
		}

		// Read the 3 codes
		for (int i = 2; i < 5; i++) {
			s = readEMIShort(dis);
			if (s == 0) {
				// Do nothing 
			} else if (s >= 2000) {
				notes.add(emiNotes.get(s - 2000));
			} else if (s >= 900) {
				s -= 900;
				if (s >= 100) {
					s -= 100;
				}
				if (s < lookups.get(PLANTS).size()) {
					String p = lookups.get(PLANTS).get(s);
					String pla = getCodeForEMI(p);
					if (pla != null && FeatureData.getInstance().isValid(pla)) {
						sqr[i] = Decoder.shortFromString(pla);
						k++;
					} else {
						unmatched.add(p);
					}
				} else {
					unmatched.add("Plant:" + Short.toString(s));
				}
			} else if (s > 100) {
				if (FeatureData.getInstance().isValid(Short.toString(s))) {
					sqr[i] = s;
					k++;
				} else {
					unmatched.add("Creature:" + Short.toString(s));
				}
			} else {
				unmatched.add(Short.toString(s));
			}
		}

		if (k > 0 && sqr[0] > 0) {
			// Copy the data found into an array
			short[] ret = new short[k];
			
			// Copy the terrain
			ret[0] = sqr[0];
			k = 1;
			
			// Copy everything else
			for (int i = 1; i < sqr.length; i++) {
				if (sqr[i] != 0) {
					ret[k++] = sqr[i];
				}
			}
	
			return ret;
		} else {
			// the square contains nothing
			return null;
		}
	}

	/**
	 * Concatenates the EditMI notes into a single string
	 * @param notes the notes to concatenate
	 * @return a String containing all the notes' text
	 */
	private String readEMINotes(List<String> notes) {
		if (notes.size() == 0) {
			return null;
		} else if (notes.size() == 1) {
			return notes.get(0);
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < notes.size(); i++) {
				if (i > 0) {
					sb.append("\n");
				}
				sb.append(notes.get(i));
			}
			return sb.toString();
		}
	}

	/**
	 * Reads the SETUP.TXT file and returns the default co-ordinate
	 * @param folder the map.dat file's location
	 * @return a List of EditMI Terrain codes
	 * @throws IOException if the Terrain.dat file cannot be found or read from
	 */
	private Point readEMITopLeft(File folder) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(folder, "SETUP.TXT")));
        
        try {
        	Point p = new Point();
	        String line;
	        while((line = reader.readLine()) != null) {
	        	line = line.trim();
	        	if (line.startsWith("XY:")) {
	        		if (line.endsWith("DOWN")) {
	        			line = line.substring(3, line.length() - 4).trim();
	        			String[] xy = line.split(" ");
	        			p.x = -Integer.parseInt(xy[0]);
	        			p.y = Integer.parseInt(xy[1]);
	        			break;
	        		}
	        	}
	        }
	        return p;
        } finally {
        	reader.close();
        }
    }
	
	/**
	 * Reads the TERRAIN.DAT file and returns a List of Terrain codes
	 * @param folder the map.dat file's location
	 * @return a List of EditMI Terrain codes
	 * @throws IOException if the Terrain.dat file cannot be found or read from
	 */
	private List<String> readEMITerrain(File folder) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(folder, "TERRAIN.DAT")));
        String line = reader.readLine();
        int size = 0;
        try {
            size = Integer.parseInt(line);
        } catch (NumberFormatException e) {
            reader.close();
            throw new NumberFormatException("The first line of the Terrain.dat file is not a number");
        }
        List<String> l = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            line = reader.readLine();
            if (line != null) {
                l.add(line.substring(0, 2));
            }
        }
        reader.close();
        return l;
    }

	/**
	 * Reads the TERRAIN.DAT file and returns a List of Location codes
	 * @param folder the map.dat file's location
	 * @return a List of EditMI Location codes
	 * @throws IOException if the Terrain.dat file cannot be found or read from
	 */
	private List<String> readEMILocations(File folder) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(folder, "TERRAIN.DAT")));
        String line = reader.readLine();
        int size = 0;
        try {
            size = Integer.parseInt(line);
        } catch (NumberFormatException e) {
            reader.close();
            throw new NumberFormatException("The first line of the Terrain.dat file is not a number");
        }
        for (int i = 0; i < size; i++) {
            line = reader.readLine();
        }

        line = reader.readLine();

        try {
            size = Integer.parseInt(line);
        } catch (NumberFormatException e) {
            reader.close();
            throw new NumberFormatException("The first line after terrain in the Terrain.dat file is not a number");
        }

        List<String> l = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            line = reader.readLine();
            if (line != null) {
                l.add(line.substring(0, 2));
            }
        }
        reader.close();
        return l;
    }

	/**
	 * Reads the PLANTS.DAT file and returns a List of Plant codes
	 * @param folder the map.dat file's location
	 * @return a List of EditMI Plant codes
	 * @throws IOException if the Plants.dat file cannot be found or read from
	 */
	private List<String> readEMIPlants(File folder) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(folder, "PLANTS.DAT")));
		List<String> l = new ArrayList<String>();
		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.equals("*")) {
				l.add(line);
			} else {
				break;
			}
		}
		reader.close();
		return l;
	}

	/**
	 * Reads the STRUCTS.DAT file and returns a Map of note codes and text
	 * @param folder the map.dat file's location
	 * @return a Map of EditMI Note codes and text
	 * @throws IOException if the Structs.dat file cannot be found or read from
	 */
	private Map<Integer, String> readEMINotes(File folder) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(folder, "STRUCTS.DAT")));
		Map<Integer, String> m = new HashMap<Integer, String>();
		String line;
		int id = 0;
		while ((line = reader.readLine()) != null) {
			id = Integer.valueOf(line.substring(0, 3));
			m.put(id, line.substring(4));
		}
		reader.close();
		return m;

	}

	private String getCodeForEMI(String name) {
		return emitrans.get(name);
	}

	@Override
	public String listTranslations(File dat) throws IOException {
		StringBuilder sb = new StringBuilder();
		addList(sb,readEMITerrain(dat.getParentFile()), true);
		addList(sb,readEMILocations(dat.getParentFile()), false);
		addList(sb,readEMIPlants(dat.getParentFile()), false);
		sb.append("*,\n");
		return sb.toString();
	}

	private void addList(StringBuilder sb, List<String> list, boolean starred) {
		Iterator<String> it = list.iterator();
		while (it.hasNext()) {
			if (starred) {
				sb.append("*");
			}
			sb.append(it.next().trim());
			sb.append(",\n");
		}
		
	}
}
