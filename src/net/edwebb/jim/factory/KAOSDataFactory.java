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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import net.edwebb.jim.model.MapData;
import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.data.Decoder;

public class KAOSDataFactory implements DataFactory {

	private static final Integer TERRAIN = 0;
	private static final Integer LOCATIONS = 1;
	private static final Integer REMARKS = 2;
	private static final Integer CREATURES = 3;

	private static final String[] TYPES = new String[] {"Terrain", "Structure", "Remark", "Creature"};
	
	private static final short NOTE = Decoder.shortFromChar("*");

	short TRAIL = 0;
	short VISITED = 0;
	
	private Map<String, String> kaostrans = new HashMap<String, String>();
	
	
	// A file filter to load kaos files
	private static final FileFilter kaosFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith(".mom");
		}

		@Override
		public String getDescription() {
			return "Kaos Files (*.mom)";
		}
	};
	
	public KAOSDataFactory(File translationFile) throws IOException {
		kaostrans = readTranslationFile(translationFile);
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
				int pos = line.lastIndexOf(",");
				if (pos > 0) {
					trans.put(line.substring(0, pos), line.substring(pos + 1));
				}
			}
			return trans;
		} finally {
			reader.close();
		}
	}
	
	// A Set of strings holding features that have not been matched to a feature in jim
	private Set<String> unmatched;
	
	@Override
	public Set<String> getUnmatched() {
		return unmatched;
	}

	@Override
	public FileFilter getFilter() {
		return kaosFilter;
	}

	@Override
	public String getSuffix() {
		return ".mom";
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

	@Override
	public MapData createFrom(File file) throws IOException {
		unmatched = new HashSet<String>();

		File mod = new File(file.getParentFile(), file.getName().toLowerCase().replaceAll("\\.mom", ".mod"));
		if (!mod.exists()) {
			throw new IOException("Cannot find the map's data file " + mod.getName());
		}
		Map<Integer, List<DBEntry>> lookups = readMOD(mod);

		DataInputStream dis = new DataInputStream(new FileInputStream(file));
		return readMOM(dis, lookups);
	}

	@Override
	public void saveTo(MapData data, File file) throws IOException {

	}

	/**
	 * Reads the TERRAIN.DAT file and returns a List of Terrain codes
	 * @param modFile the map.dat file's location
	 * @return a List of EditMI Terrain codes
	 * @throws IOException if the Terrain.dat file cannot be found or read from
	 */
	private Map<Integer,List<DBEntry>> readMOD(File modFile) throws IOException {
        
        Map<Integer, List<DBEntry>> lookups = new HashMap<Integer, List<DBEntry>>();
        
        byte b = 0;
        byte[] text = new byte[40];
        byte[] crea = new byte[20];
        byte[] bint = new byte[2];
        byte[] code = new byte[3];
        int[] length = new int[4];

        DataInputStream is = new DataInputStream(new FileInputStream(modFile));
        try {
	        b = is.readByte();
	        if (b != 2) {
	        	throw new IllegalArgumentException("The MOD file must start with a byte of '2'");
	        }
	        length[0] = is.readByte();
	        length[1] = is.readByte();
	        length[2] = is.readByte();
	        length[3] = is.readByte();
	        for (int i = 0; i < length.length; i++) {
	        	if (length[i] < 0) {
	        		length[i] = 256 + length[i];
	        	}
	        }
	
	        b = is.readByte();
	        if (b != 0) {
	        	throw new IllegalArgumentException("The MOD file must separate the table sizes from the first table with a byte of '0'");
	        }
	
	        // Read Terrains
	        List<DBEntry> terrain = new ArrayList<DBEntry>();
			lookups.put(TERRAIN, terrain);
	        for (int i = 0; i < length[0]; i++) {
	        	b = is.readByte();
	        	is.readFully(text);
	        	String s = new String(text, 0, b);
	        	b = is.readByte();
	        	terrain.add(new DBEntry(s, "", b));
	        }
	
	        // Read Structures
	        List<DBEntry> structures = new ArrayList<DBEntry>();
			lookups.put(LOCATIONS, structures);
	        for (int i = 0; i < length[1]; i++) {
	        	b = is.readByte();
	        	is.readFully(text);
	        	String s = new String(text, 0, b);
	        	b = is.readByte();
	        	is.readFully(code);
	        	String c = new String(code, 0, b);
	        	b = is.readByte();
	        	structures.add(new DBEntry(s, c, b));
	        }
	        
	        // Read Remarks
	        List<DBEntry> remarks = new ArrayList<DBEntry>();
	        lookups.put(REMARKS, remarks);
	        for (int i = 0; i < length[2]; i++) {
	        	b = is.readByte();
	        	is.readFully(text);
	        	String s = new String(text, 0, b);
	        	b = is.readByte();
	        	is.readFully(code);
	        	String c = new String(code, 0, b);
	        	b = is.readByte();
	        	remarks.add(new DBEntry(s, c, b));
	        }
	        
	        // Read Creatures
	        List<DBEntry> creatures = new ArrayList<DBEntry>();
	        lookups.put(CREATURES, creatures);
	        for (int i = 0; i < length[3]; i++) {
	        	b = is.readByte();
	        	is.readFully(code);
	        	String c = new String(code, 0, b);
	        	b = is.readByte();
	        	is.readFully(crea);
	        	String s = new String(crea, 0, b);
	        	is.readFully(bint);
	        	short num = Decoder.shortFromLittleEndian(bint);
	        	creatures.add(new DBEntry(s, c, num));
	        }

	        return lookups;
        } finally {
        	is.close();
        }
	}
	
	private MapData readMOM(DataInputStream dis, Map<Integer, List<DBEntry>> lookups) throws IOException {
		//int pos = 0;

		byte[] bint = new byte[4];
		byte[] bshrt = new byte[2];
		byte b;

		b = dis.readByte();
		if (b != 3) {
        	throw new IllegalArgumentException("The MOM file must start with a byte of '3'");
		}

		// Read the block section of the file
		Point bottomRight = new Point();
		List<Point> blocks = readBlocks(dis, bottomRight);
		
		// Create a MapData big enough to hold all of the blocks
		MapData data = new MapData((bottomRight.x + 1) * 100, (bottomRight.y + 1) * 100);
		
		//System.out.println("Blocks: " + blocks.size());
		
		short x = 0;
		short y = 0;

		StringBuilder note = new StringBuilder();
		
		// set the Trail code
		TRAIL = Decoder.shortFromString("TRL");
		if (DataStore.getInstance().getFeatureById(TRAIL) == null) {
			throw new IllegalStateException("Cannot find the Trail feature using code TRL");
		}
		
		// Set the visited code
		short[] temp = getIDsForKAOS(".");
		if (temp.length != 1) {
			throw new IllegalStateException("Cannot find a translation for . in the kaostrans.csv file");
		}
		VISITED = temp[0];
		
		while (blocks.size() > 0) {
			dis.readFully(bint);
			int cell = Decoder.intFromLittleEndian(bint);
			if (cell < 0) {
				short[] sqr = readCell(cell, lookups, note);

				try {
					Point p = blocks.get(0);
					data.setSquare(x + (p.x * 100), y + (p.y * 100), sqr);
					if (note.length() > 0) {
						data.setSquareNotes(x + (p.x * 100), y + (p.y * 100), note.toString());
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
				
				note.delete(0, note.length());
				
				x++;
			} else {
				// Skip this number of squares
				x += cell;
			}
			
			if (x >= 100) {
				x = 0;
				y++;
			}
			if (y >= 100) {
				y = 0;
				x = 0;
				blocks.remove(0);
			}
		}
		
		// Find out the top left co-ordinate of the map
		data.setTopLeft(readTopLeft(dis));
		
		// Read extra information
		dis.readFully(bshrt);
		short extra = Decoder.shortFromLittleEndian(bshrt);
		//System.out.println("Extras: " + extra);
		for (int i = 0; i < extra; i++) {
			dis.readFully(bshrt);
			x = Decoder.shortFromLittleEndian(bshrt);
			dis.readFully(bshrt);
			y = Decoder.shortFromLittleEndian(bshrt);

			short[] sqr = data.getSquare(x, y);
			if (sqr == null) {
				sqr = new short[0];
			}
			String n = data.getSquareNotes(x, y);
			note = new StringBuilder();
			if (n != null) {
				note.append(n);
			}
			
			dis.readFully(bshrt);
			short ext = Decoder.shortFromLittleEndian(bshrt);
			
			sqr = addExtraFeature(sqr, note, lookups, ext);

			data.setSquare(x, y, sqr);
			data.setSquareNotes(x, y, note.toString());

		}

        byte[] nt = new byte[20];

		dis.readFully(bshrt);
		short notes = Decoder.shortFromLittleEndian(bshrt);
		short num;
		for (int i = 0; i < notes; i++) {
			dis.readFully(bshrt);
			num = Decoder.shortFromLittleEndian(bshrt);
			b = dis.readByte();
			dis.readFully(nt);
			String n = new String(nt, 0, b);
			dis.readFully(bshrt);
			x = Decoder.shortFromLittleEndian(bshrt);
			dis.readFully(bshrt);
			y = Decoder.shortFromLittleEndian(bshrt);
			StringBuilder sb = new StringBuilder();
			String s = data.getSquareNotes(x, y);
			if (s != null) {
				sb.append(s);
				sb.append("\n");
			}
			sb.append(n);
			sb.append(" (");
			sb.append(num);
			sb.append(")");
			data.setSquareNotes(x, y, sb.toString());
		}

		dis.readFully(bshrt);
		notes = Decoder.shortFromLittleEndian(bshrt);
		for (int i = 0; i < notes; i++) {
			dis.readFully(bshrt);
			num = Decoder.shortFromLittleEndian(bshrt);
			b = dis.readByte();
			dis.readFully(nt);
			String n = new String(nt);
			dis.readFully(bshrt);
			x = Decoder.shortFromLittleEndian(bshrt);
			dis.readFully(bshrt);
			y = Decoder.shortFromLittleEndian(bshrt);
			StringBuilder sb = new StringBuilder();
			String s = data.getSquareNotes(x, y);
			if (s == null) {
				sb.append(s);
				sb.append("\n");
			}
			sb.append(n);
			data.setSquareNotes(x, y, sb.toString());
		}
		
		return data;
	}
	
	private short[] getIDsForKAOS(String name) {
		String code = kaostrans.get(name);
		if (code == null) {
			return new short[0];
		} else if (code.trim().length() == 1) {
			short x = Decoder.shortFromChar(code);
			
			// Numbers from 0 to 7 should be returned as short 0 - 7
			// Everything else should return its ASCII value
			if (x >= 48 && x <= 55) {
				x -= 48;
			}
			return new short[] {x};
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
	
	private List<Point> readBlocks(DataInputStream dis, Point bottomRight) throws IOException {
		int bottom = 0;
		int right = 0;
		List<Point> blocks = new ArrayList<Point>();
		byte b;
		// Record the location of the blocks of 100x100 squares
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 20; j++) {
				b = dis.readByte();
				if (b > 0) {
					blocks.add(new Point(j, i));
					right = Math.max(j, right);
					bottom = Math.max(i, bottom);
				}
			}
		}
		bottomRight.move(right, bottom);
		return blocks;
	}
	
	/**
	 * Reads the data from the integer that represents a square and returns a list of short codes
	 * @param cell the integer that holds the square's data
	 * @param lookups a map of lookup tables
	 * @param note a stringbuilder to add notes to
	 * @return a list of short codes
	 */
	private short[] readCell(int cell, Map<Integer, List<DBEntry>> lookups, StringBuilder note) {
		short[] sqr = new short[0];
		
		//Terrain
		int terr = ((cell & 0x07c00000) >> 22);
		if (terr < lookups.get(TERRAIN).size()) {
			DBEntry entry = lookups.get(TERRAIN).get(terr);
			if (entry != null) {
				short[] id = getIDsForKAOS(entry.getName());
				short s = 0;
				
				if (id.length > 0 && DataStore.getInstance().isValid(id[0])) {
					s = id[0];
				} else {
					s = 0;
					unmatched.add(entry.getName());
				}

				if ((cell & 0x10000000) > 0) {
					s |= (1 << (VISITED + 8));
				}
				sqr = combineArrays(sqr, s);
			}
		} else {
			unmatched.add("Terrain:" + terr);
		}

		// Trails
		if ((cell & 0x08000000) > 0) {
			sqr = combineArrays(sqr, TRAIL);
		}

		// Structure
		int struct = ((cell & 0x003f0000) >> 16);
		sqr = addFeature(sqr, note, lookups, LOCATIONS, struct);
		
		// Remarks
		int rem = ((cell & 0x0000fe00) >> 9);
		sqr = addFeature(sqr, note, lookups, REMARKS, rem);
		
		// Creature
		int crea = ((cell & 0x000001ff));
		sqr = addFeature(sqr, note, lookups, CREATURES, crea);
		
		return sqr;
	}
	
	private short[] combineArrays(short[] sqr, short[] feat) {
		short[] newSqr = new short[sqr.length + feat.length];
		System.arraycopy(sqr, 0, newSqr, 0, sqr.length);
		System.arraycopy(feat, 0, newSqr, sqr.length, feat.length);
		return newSqr;
	}
	
	private short[] combineArrays(short[] sqr, short feat) {
		short[] newSqr = new short[sqr.length + 1];
		System.arraycopy(sqr, 0, newSqr, 0, sqr.length);
		newSqr[sqr.length] = feat;
		return newSqr;
	}
	
	private Point readTopLeft(DataInputStream dis) throws IOException {
		byte[] bshrt = new byte[2];
		dis.readFully(bshrt);
		short offx = Decoder.shortFromLittleEndian(bshrt);
		dis.readFully(bshrt);
		short offy = Decoder.shortFromLittleEndian(bshrt);
		dis.readFully(bshrt);
		short offxc = Decoder.shortFromLittleEndian(bshrt);
		dis.readFully(bshrt);
		short offyc = Decoder.shortFromLittleEndian(bshrt);
		return new Point(-(offx + offxc), (offy + offyc));
		
	}
	
	private short[] addExtraFeature(short[] sqr, StringBuilder note, Map<Integer, List<DBEntry>> lookups, short ext) throws IOException {
		short id;

		try {
			id = (short)(ext & 0x1fff);
			if ((ext & 0x8000) > 0) {
				sqr = addFeature(sqr, note, lookups, CREATURES, id);
			} else if ((ext & 0x4000) > 0) {
				sqr = addFeature(sqr, note, lookups, REMARKS, id);
			} else if ((ext & 0x2000) > 0) {
				sqr = addFeature(sqr, note, lookups, LOCATIONS, id);
			}
		} catch (Exception e) {
			unmatched.add("Extra feature " + ((ext & 0xe000) >> 13) + " " + (ext & 0x1fff));
		}
		return sqr;
	}
	
	/**
	 * Adds a feature to a square.
	 * @param sqr the square to add a feature to
	 * @param note the square's note
	 * @param lookups the lookup table
	 * @param index the index of the entry in the lookup table
	 * @return the new square array
	 */
	private short[] addFeature(short[] sqr, StringBuilder note, Map<Integer,List<DBEntry>> lookups, int type, int index) {
		if (index == 0) {
			return sqr;
		}
		
		List<DBEntry> lookup = lookups.get(type);
		
		if (index >= lookup.size()) {
			unmatched.add(TYPES[type] + ":" + index);
			return sqr;
		}
		DBEntry entry = lookup.get(index);
		if (entry != null) {
			if (entry.getNumber() > 100) {
				if (DataStore.getInstance().isValid(Short.toString(entry.getNumber()))) {
					sqr = combineArrays(sqr, entry.getNumber());
				} else {
					unmatched.add(entry.getName());
				}
			} else {
				short[] id = getIDsForKAOS(entry.getName());
				if (id.length > 1) {
					sqr = combineArrays(sqr, cleanArray(id));
				} else if (id.length == 1) {
					if (id[0] == NOTE) {
						note.append(entry.getName());
						note.append("\n");
					} else if (id[0] >= 0 && id[0] < 8) {
						sqr[0] |= (1 << (id[0] + 8));
					} else if (DataStore.getInstance().isValid(id[0])) {
						sqr = combineArrays(sqr, id[0]);
					} else {
						unmatched.add(entry.getName());
					}
				} else {
					unmatched.add(entry.getName());
				}
			}
		}
		return sqr;
	}
	
	private short[] cleanArray(short[] sqr) {
		int k = sqr.length;
		for (int i = 0; i < sqr.length; i++) {
			if (!DataStore.getInstance().isValid(sqr[i])) {
				if (sqr[i] < 0 || sqr[i] > 999) {
					unmatched.add(Decoder.stringFromShort(sqr[i]));
				} else {
					unmatched.add(Short.toString(sqr[i]));
				}
				sqr[i] = 0;
				k--;
			}
		}
		
		if (k == sqr.length) {
			return sqr;
		}
		
		short[] temp = new short[k];
		k = 0;
		for (int i = 0; i < sqr.length; i++) {
			if (sqr[i] > 0) {
				temp[k++] = sqr[i];
			}
		}
		
		return temp;
	}
	
	class DBEntry implements Comparable<DBEntry> {
		private String name;
		private String code;
		private byte symbol;
		private short number;
		
		public DBEntry(String name, String code, byte symbol) {
			this.name = name;
			this.code = code;
			this.symbol = symbol;
		}

		public DBEntry(String name, String code, short number) {
			this.name = name;
			this.code = code;
			this.number = number;
		}

		public String getName() {
			return name;
		}
		
		public String getCode() {
			return code;
		}
		
		public byte getSymbol() {
			return symbol;
		}

		public short getNumber() {
			return number;
		}

		@Override
		public String toString() {
			if (symbol == 0 && number > 0) {
				return "DBEntry [name=" + name + ", code=" + code + ", number="	+ number + "]";
			} else {
				return "DBEntry [name=" + name + ", code=" + code + ", symbol="	+ symbol + "]";
			}
		}

		@Override
		public int compareTo(DBEntry o) {
			if (o == null) {
				return 1;
			} else {
				return name.compareTo(o.getName());
			}
		}
		
		
	}

	@Override
	public String listTranslations(File file) throws IOException {
		File mod = new File(file.getParentFile(), file.getName().toLowerCase().replaceAll("\\.mom", ".mod"));
		if (!mod.exists()) {
			throw new IOException("Cannot find the map's data file " + mod.getName());
		}
		StringBuilder sb = new StringBuilder();
		sb.append(".,\n");
		Map<Integer, List<DBEntry>> lookups = readMOD(mod);
		addList(sb, lookups.get(TERRAIN));
		addList(sb, lookups.get(LOCATIONS));
		addList(sb, lookups.get(REMARKS));
		return sb.toString();
	}

	private void addList(StringBuilder sb, List<DBEntry> list) {
		Collections.sort(list);
		Iterator<DBEntry> it = list.iterator();
		while (it.hasNext()) {
			sb.append(it.next().getName());
			sb.append(",\n");
		}
		
	}
	

}
