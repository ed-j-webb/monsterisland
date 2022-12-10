package net.edwebb.jim.data;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Singleton class that holds all the feature data for JIM
 * @author Ed Webb
 *
 */
public class FeatureData {

	private Map<Short, Terrain> terrain;
	private Map<String, Terrain> terString;
	private Map<String, Terrain> terCode;
	private List<Terrain> ter;
	
	private Map<Short, Location> locations;
	private Map<String, Location> locString;
	private Map<String, Location> locCode;
	private List<Location> loc;
	
	private Map<Short, Plant> plants;
	private Map<String, Plant> plaString;
	private Map<String, Plant> plaCode;
	private List<Plant> pla;
	
	private Map<Short, Creature> creatures;
	private Map<String, Creature> creString;
	private Map<String, Creature> creCode;
	private List<Creature> cre;
	
	private Map<Short, Flag> flags;
	private List<Flag> flg;
	
	private List<String> valid = new ArrayList<String>();
	
	private List<Coordinate> coords;
	
	private static FeatureData instance;
	
	public static FeatureData getInstance() {
		return instance;
	}
	
	public static void createInstance(File featureFolder) throws IOException {
		instance = new FeatureData(featureFolder);
	}
	
	private FeatureData(File featureFolder) throws IOException {
		File imageFolder = new File(featureFolder, "images");
		loadTerrain(new File(featureFolder, "terrain.csv"), imageFolder);
		loadLocations(new File(featureFolder, "locations.csv"), imageFolder);
		loadPlants(new File(featureFolder, "plants.csv"), imageFolder);
		loadCreatures(new File(featureFolder, "creatures.csv"), imageFolder);
		loadCoOrdinates(new File(featureFolder, "coordinates.csv"));
		loadFlags(new File(featureFolder, "flags.csv"), imageFolder);
	}
	
	public Terrain getTerrain(short index) {
		index = Decoder.shortLowByte(index);
		return terrain.get(index);
	}
	
	public Terrain getTerrain(String name) {
		return terString.get(name);
	}

	public List<Terrain> getTerrain() {
		return Collections.unmodifiableList(ter);
	}
	
	public Location getLocation(short index) { 
		return locations.get(index);
	}

	public Location getLocation(String name) {
		return locString.get(name);
	}
	
	public List<Location> getLocations() {
		return Collections.unmodifiableList(loc);
	}

	public Plant getPlant(short index) { 
		return plants.get(index);
	}

	public Plant getPlant(String name) {
		return plaString.get(name);
	}
	
	public List<Plant> getPlants() {
		return Collections.unmodifiableList(pla);
	}
	
	public Creature getCreature(short index) { 
		return creatures.get(index);
	}

	public Creature getCreature(String name) {
		return creString.get(name);
	}
	
	public List<Creature> getCreatures() {
		return Collections.unmodifiableList(cre);
	}
	
	public Flag getFlag(short index) {
		return flags.get(index);
	}
	
	public List<Flag> getFlags(short index) {
		index = Decoder.shortHighByte(index);
		List<Flag> flags = new ArrayList<Flag>();
		for (short i = 0; i < 8; i++) {
			if ((index & (short)Math.pow(2, i)) > 0) {
				Flag f = getFlag(i);
				if (f != null) {
					flags.add(f);
				}
			}
		}
		return flags;
	}

	public List<Flag> getFlags() {
		return Collections.unmodifiableList(flg);
	}
	
	public Feature getFeature(short index) {
		if (creatures.containsKey(index)) {
			return creatures.get(index);
		} else if (plants.containsKey(index)) {
			return plants.get(index);
		} else if (locations.containsKey(index)) {
			return locations.get(index);
		} else if (terrain.containsKey(Decoder.shortLowByte(index))) {
			return terrain.get(index);
		} else if (flags.containsKey(index)) {
			return flags.get(index);
		} else {
			return null;
		}
	}

	public Feature getFeature(String name) {
		if (creString.containsKey(name)) {
			return creString.get(name);
		} else if (terString.containsKey(name)) {
			return terString.get(name);
		} else if (plaString.containsKey(name)) {
			return plaString.get(name);
		} else if (locString.containsKey(name)) {
			return locString.get(name);
		} else {
			return null;
		}
	}

	public Feature findFeature(String code) {
		if (code.startsWith("%")) {
			return locCode.get(code.substring(1));
		} else if (code.startsWith("@")) {
			return creCode.get(code.substring(1));
		} else if (code.startsWith("$")) {
			return plaCode.get(code.substring(1));
		} else {
			return terCode.get(code);
		}
	}
	
	public List<Coordinate> getCoordinates() {
		return coords;
	}
	
	/**
	 * Returns true if the code is contained in the list of valid codes
	 * @param code the code to check
	 * @return true if it is a valid code
	 */
	public boolean isValid(String code) {
		return valid.contains(code);
	}
	
	/**
	 * Returns true if the id is for a feature
	 * @param index the id to check
	 * @return true if it references a feature
	 */
	public boolean isValid(short index) {
		return (getFeature(index) != null);
	}
	
	private void loadTerrain(File f, File imageFolder) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		terrain = new HashMap<Short, Terrain>();
		ter = new ArrayList<Terrain>();
		terString = new HashMap<String, Terrain>();
		terCode = new HashMap<String, Terrain>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(",");
				if (part.length == 5) {
					short id = Decoder.shortFromChar(part[0]);
					Terrain t = new Terrain(id, part[0], part[1], Decoder.makeColour(part[2]), Decoder.makeColour(part[3]), Decoder.makeImageIcon(part[4], imageFolder));
					terrain.put(id, t);
					terString.put(part[1], t);
					terCode.put(part[0], t);
					ter.add(t);
					valid.add(part[0]);
				}
			}
		} finally {
			reader.close();
		}
		Collections.sort(ter);
	}
	
	private void loadFlags(File f, File imageFolder) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		flags = new HashMap<Short, Flag>();
		flg = new ArrayList<Flag>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(",");
				if (part.length == 4) {
					short id = Decoder.shortFromInt(part[0]);
					if (id < 0 || id > 7) {
						throw new IllegalArgumentException("The flags must have a value of between 0 and 7. This one is " + id);
					}
					try {
						Flag g = new Flag(id, part[0], part[1], Decoder.shortFromInt(part[2]), Decoder.makeImageIcon(part[3], imageFolder));
						flags.put(id, g);
						flg.add(g);
						valid.add(part[0]);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} finally {
			reader.close();
		}
		Collections.sort(flg);
	}

	private void loadLocations(File f, File imageFolder) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		locations = new HashMap<Short, Location>();
		loc = new ArrayList<Location>();
		locString = new HashMap<String, Location>();
		locCode = new HashMap<String, Location>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(",");
				if (part.length == 3) {
					short id = Decoder.shortFromString(part[0]);
					try {
						Location l = new Location(id, part[0], part[1], Decoder.makeImageIcon(part[2], imageFolder));
						locations.put(id, l);
						locString.put(part[1], l);
						locCode.put(part[0], l);
						loc.add(l);
						valid.add(part[0]);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} finally {
			reader.close();
		}
		Collections.sort(loc);
	}

	private void loadPlants(File f, File imageFolder) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		Plant.setIcon(Decoder.makeImageIcon("plant.png", imageFolder));
		plants = new HashMap<Short, Plant>();
		pla = new ArrayList<Plant>();
		plaString = new HashMap<String, Plant>();
		plaCode = new HashMap<String, Plant>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(",");
				if (part.length == 2) {
					short id = Decoder.shortFromString(part[0]);
					Plant p = new Plant(id, part[0], part[1]);
					plants.put(id, p);
					plaString.put(part[1], p);
					plaCode.put(part[0], p);
					pla.add(p);
					valid.add(part[0]);
				}
			}
		} finally {
			reader.close();
		}
		Collections.sort(pla);
	}

	private void loadCreatures(File f, File imageFolder) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		Creature.setIcon(Decoder.makeImageIcon("creature.png", imageFolder));
		creatures = new HashMap<Short, Creature>();
		cre = new ArrayList<Creature>();
		creString = new HashMap<String, Creature>();
		creCode = new HashMap<String, Creature>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(",");
				if (part.length == 2) {
					short id = Decoder.shortFromInt(part[0]);
					Creature c = new Creature(id, part[1]);
					creatures.put(id, c);
					creString.put(part[1], c);
					creCode.put(part[0], c);
					cre.add(c);
					valid.add(part[0]);
				}
			}
		} finally {
			reader.close();
		}
		Collections.sort(cre);
	}

	private void loadCoOrdinates(File f) throws NumberFormatException, IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		coords = new ArrayList<Coordinate>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(",");
				if (part.length == 3) {
					Point p = new Point(Integer.valueOf(part[0]), Integer.valueOf(part[1]));
					Coordinate c = new Coordinate(p, part[2]);
					coords.add(c);
				}
			}
		} finally {
			reader.close();
		}
	}
}
