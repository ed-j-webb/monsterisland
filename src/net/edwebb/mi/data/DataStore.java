package net.edwebb.mi.data;

import java.awt.Point;
//import java.awt.Point;
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
 * A Singleton class that holds all the data for Monster Island
 * @author Ed Webb
 *
 */
public class DataStore {

	private Map<Short, Terrain> terrainId;
	private Map<String, Terrain> terrainName;
	private Map<String, Terrain> terrainCode;
	private List<Terrain> terrain;
	
	private Map<Short, Location> locationId;
	private Map<String, Location> locationName;
	private Map<String, Location> locationCode;
	private List<Location> location;
	
	private Map<Short, Plant> plantId;
	private Map<String, Plant> plantName;
	private Map<String, Plant> plantCode;
	private List<Plant> plant;
	
	private Map<Short, Creature> creatureId;
	private Map<String, Creature> creatureName;
	private Map<String, Creature> creatureCode;
	private List<Creature> creature;
	
	private Map<String, Race> raceName;
	private Map<String, Race> raceCode;
	private List<Race> race;

	private Map<Short, Item> itemId;
	private Map<String, Item> itemName;
	private List<Item> item;
	
	private Map<Short, Flag> flagId;
	private List<Flag> flag;
	
	private List<String> valid = new ArrayList<String>();
	
	private List<Coordinate> coords;
	
	// Stores the fingerprint for every known image on the printed map
	// This is populated from an editable config file
	private Map<Integer, String> mapIcons = new HashMap<Integer, String>();
	
	
	private static DataStore instance;
	
	public static DataStore getInstance() {
		return instance;
	}
	
	public static void createInstance(File dataDir) throws IOException {
		instance = new DataStore(dataDir);
	}
	
	private DataStore(File dataDir) throws IOException {
		File imageDir = new File(dataDir, "images");
		loadTerrain(new File(dataDir, "terrain.dat"), imageDir);
		loadLocations(new File(dataDir, "locations.dat"), imageDir);
		loadPlants(new File(dataDir, "plants.dat"), imageDir);
		loadCreatures(new File(dataDir, "creatures.dat"), imageDir);
		loadCoOrdinates(new File(dataDir, "coordinates.dat"));
		loadRaces(new File(dataDir, "races.dat"));
		loadItems(new File(dataDir, "items.dat"));
		loadFlags(new File(dataDir, "flags.dat"), imageDir);
		loadMapIcons(new File(dataDir, "mapicons.dat"));
	}
	
	public Terrain getTerrain(short index) {
		index = Decoder.shortLowByte(index);
		return terrainId.get(index);
	}
	
	public Terrain getTerrain(String name) {
		name = name.trim();
		return terrainName.get(name);
	}

	public List<Terrain> getTerrain() {
		return Collections.unmodifiableList(terrain);
	}
	
	public Location getLocation(short index) { 
		return locationId.get(index);
	}

	public Location getLocation(String name) {
		name = name.trim();
		Location location = locationName.get(name);
		if (location != null) {
			return location;
		}
		return location;
	}
	
	public List<Location> getLocations() {
		return Collections.unmodifiableList(location);
	}

	public Plant getPlant(short index) { 
		return plantId.get(index);
	}

	public Plant getPlant(String name) {
		name = name.trim();
		Plant plant = plantName.get(name);
		if (plant != null) {
			return plant;
		}
		int len = name.length();
		if (name.endsWith("s")) {
			plant = plantName.get(name.substring(0, len-1));
			if (plant != null) {
				return plant;
			}
		}
		if (name.endsWith("es")) {
			plant = plantName.get(name.substring(0, len-2));
			if (plant != null) {
				return plant;
			}
		}
		return plant;
	}
	
	public List<Plant> getPlants() {
		return Collections.unmodifiableList(plant);
	}
	
	public Creature getCreature(short index) { 
		return creatureId.get(index);
	}

	public Creature getCreature(String name) {
		return getCreature(name, false);
	}
	
	private Creature getCreature(String name, boolean recursive) {
		name = name.trim();
		Creature creature = creatureName.get(name);
		if (creature != null) {
			return creature;
		}
		int len = name.length();
		if (name.endsWith("s")) {
			creature = creatureName.get(name.substring(0, len-1));
			if (creature != null) {
				return creature;
			}
		}
		if (name.endsWith("es")) {
			creature = creatureName.get(name.substring(0, len-2));
			if (creature != null) {
				return creature;
			}
		}
		if (name.endsWith("ies")) {
			creature = creatureName.get(name.substring(0, len-3) + "y");
			if (creature != null) {
				return creature;
			}
		}
		if (name.endsWith("i")) {
			creature = creatureName.get(name.substring(0, len-1) + "us");
			if (creature != null) {
				return creature;
			}
		}
		if (name.endsWith("i")) {
			creature = creatureName.get(name.substring(0, len-1) + "a");
			if (creature != null) {
				return creature;
			}
		}
		if (name.endsWith("ice")) {
			creature = creatureName.get(name.substring(0, len-3) + "ouse");
			if (creature != null) {
				return creature;
			}
		}
		if (name.endsWith("ves")) {
			creature = creatureName.get(name.substring(0, len-3) + "f");
			if (creature != null) {
				return creature;
			}
		}
		if (name.endsWith("ves")) {
			creature = creatureName.get(name.substring(0, len-3) + "fe");
			if (creature != null) {
				return creature;
			}
		}
		if (name.endsWith("men")) {
			creature = creatureName.get(name.substring(0, len-3) + "man");
			if (creature != null) {
				return creature;
			}
		}
		if (name.endsWith("eese")) {
			creature = creatureName.get(name.substring(0, len-4) + "oose");
			if (creature != null) {
				return creature;
			}
		}
		if (name.endsWith("eet")) {
			creature = creatureName.get(name.substring(0, len-3) + "oot");
			if (creature != null) {
				return creature;
			}
		}
		return creature;
	}
	
	public List<Creature> getCreatures() {
		return Collections.unmodifiableList(creature);
	}

	public Race getRace(String name) {
		return raceName.get(name);
	}
	
	public List<Race> getRaces() {
		return Collections.unmodifiableList(race);
	}
	
	public Item getItem(short index) { 
		return itemId.get(index);
	}

	public Item getItem(String name) {
		name = cleanItemName(name);
		Item item = itemName.get(name);

		if (item != null) {
			return item;
		}
		int len = name.length();
		if (item == null && name.endsWith("s")) {
			item = itemName.get(name.substring(0, len-1));
			if (item != null) {
				return item;
			}
		}
		if (item == null && name.endsWith("es")) {
			item = itemName.get(name.substring(0, len-2));
			if (item != null) {
				return item;
			}
		}
		if (name.endsWith("ies")) {
			item = itemName.get(name.substring(0, len-3) + "y");
			if (item != null) {
				return item;
			}
		}

		if (name.endsWith("ves")) {
			item = itemName.get(name.substring(0, len-3) + "f");
			if (item != null) {
				return item;
			}
		}

		return item;
	}
	
	private String cleanItemName(String name) {
		name = name.trim();
		name = name.replaceAll("Dam\\.", "Damaged");
		name = name.replaceAll("Dam ", "Damaged ");
		name = name.replaceAll("Armor", "Armour");
		name = name.replaceAll("suit of ", "");
		name = name.replaceAll("Piglron", "PigIron");
		name = name.replaceAll("- a", "");
		name = name.replaceAll("Legg'n", "Leggings");
		name = name.replaceAll(" Lg ", " Large ");
		if (name.endsWith(" Gaunt")) {
			name = name.replaceAll(" Gaunt", " Gauntlets");
		}
		return name.trim();
	}
	
	public List<Item> getItems() {
		return Collections.unmodifiableList(item);
	}

	public Flag getFlag(short index) {
		return flagId.get(index);
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
		return Collections.unmodifiableList(flag);
	}
	
	public Feature getFeatureById(short index) {
		if (creatureId.containsKey(index)) {
			return creatureId.get(index);
		} else if (plantId.containsKey(index)) {
			return plantId.get(index);
		} else if (locationId.containsKey(index)) {
			return locationId.get(index);
		} else if (terrainId.containsKey(Decoder.shortLowByte(index))) {
			return terrainId.get(index);
		} else if (flagId.containsKey(index)) {
			return flagId.get(index);
		} else {
			return null;
		}
	}

	public Feature getFeatureByName(String name) {
		name = name.trim();
		if (creatureName.containsKey(name)) {
			return creatureName.get(name);
		} else if (terrainName.containsKey(name)) {
			return terrainName.get(name);
		} else if (plantName.containsKey(name)) {
			return plantName.get(name);
		} else if (locationName.containsKey(name)) {
			return locationName.get(name);
		} else {
			return null;
		}
	}

	public Feature getFeatureByCode(String code) {
		if (code.startsWith("%")) {
			return locationCode.get(code.substring(1));
		} else if (code.startsWith("@")) {
			return creatureCode.get(code.substring(1));
		} else if (code.startsWith("$")) {
			return plantCode.get(code.substring(1));
		} else {
			return terrainCode.get(code);
		}
	}
	
	public List<Coordinate> getCoordinates() {
		return coords;
	}
	
	public Map<Integer, String> getMapIcons() {
		return Collections.unmodifiableMap(mapIcons);
	}
	
	/**
	 * Returns true if the code is contained in the list of valid codes
	 * @param code the code to check
	 * @return true if it is a valid code
	 */
	public boolean isValid(String code) {
		return getFeatureByCode(code) != null;
	}
	
	/**
	 * Returns true if the id is for a feature
	 * @param index the id to check
	 * @return true if it references a feature
	 */
	public boolean isValid(short index) {
		return getFeatureById(index) != null;
	}
	
	private void loadTerrain(File f, File imageDir) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		terrainId = new HashMap<Short, Terrain>();
		terrain = new ArrayList<Terrain>();
		terrainName = new HashMap<String, Terrain>();
		terrainCode = new HashMap<String, Terrain>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(";");
				if (part.length == 4) {
					short id = Decoder.shortFromChar(part[0]);
					Terrain t = new Terrain(id, part[0], part[1], Decoder.makeColour(part[2]), Decoder.makeColour(part[3]), Decoder.makeImageIcon(part[0] + ".png", imageDir));
					terrainId.put(id, t);
					terrainName.put(part[1], t);
					terrainCode.put(part[0], t);
					terrain.add(t);
					valid.add(part[0]);
				}
			}
		} finally {
			reader.close();
		}
		Collections.sort(terrain);
	}
	
	private void loadFlags(File f, File imageDir) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		flagId = new HashMap<Short, Flag>();
		flag = new ArrayList<Flag>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(";");
				if (part.length == 4) {
					short id = Decoder.shortFromInt(part[0]);
					if (id < 0 || id > 7) {
						throw new IllegalArgumentException("The flags must have a value of between 0 and 7. This one is " + id);
					}
					try {
						Flag g = new Flag(id, part[0], part[1], Decoder.shortFromInt(part[2]), Decoder.makeImageIcon(part[3], imageDir));
						flagId.put(id, g);
						flag.add(g);
						valid.add(part[0]);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} finally {
			reader.close();
		}
		Collections.sort(flag);
	}

	private void loadLocations(File f, File imageDir) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		locationId = new HashMap<Short, Location>();
		location = new ArrayList<Location>();
		locationName = new HashMap<String, Location>();
		locationCode = new HashMap<String, Location>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(";");
				if (part.length == 2) {
					short id = Decoder.shortFromString(part[0]);
					try {
						Location l = new Location(id, part[0], part[1], Decoder.makeImageIcon(part[0] + ".png", imageDir));
						locationId.put(id, l);
						locationName.put(part[1], l);
						locationCode.put(part[0], l);
						location.add(l);
						valid.add(part[0]);
					} catch (IOException e) {
						System.out.println(part[0]);
						e.printStackTrace();
					}
				}
			}
		} finally {
			reader.close();
		}
		Collections.sort(location);
	}

	private void loadPlants(File f, File imageDir) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		Plant.setIcon(Decoder.makeImageIcon("plant.png", imageDir));
		plantId = new HashMap<Short, Plant>();
		plant = new ArrayList<Plant>();
		plantName = new HashMap<String, Plant>();
		plantCode = new HashMap<String, Plant>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(";");
				if (part.length == 2) {
					short id = Decoder.shortFromString(part[0]);
					Plant p = new Plant(id, part[0], part[1]);
					plantId.put(id, p);
					plantName.put(part[1], p);
					plantCode.put(part[0], p);
					plant.add(p);
					valid.add(part[0]);
				}
			}
		} finally {
			reader.close();
		}
		Collections.sort(plant);
	}

	private void loadCreatures(File f, File imageDir) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		Creature.setIcon(Decoder.makeImageIcon("creature.png", imageDir));
		creatureId = new HashMap<Short, Creature>();
		creature = new ArrayList<Creature>();
		creatureName = new HashMap<String, Creature>();
		creatureCode = new HashMap<String, Creature>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(";");
				if (part.length == 2) {
					short id = Decoder.shortFromInt(part[0]);
					Creature c = new Creature(id, part[1]);
					creatureId.put(id, c);
					creatureName.put(part[1], c);
					creatureCode.put(part[0], c);
					creature.add(c);
					valid.add(part[0]);
				}
			}
		} finally {
			reader.close();
		}
		Collections.sort(creature);
	}

	private void loadRaces(File f) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		race = new ArrayList<Race>();
		raceName = new HashMap<String, Race>();
		raceCode = new HashMap<String, Race>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] part = line.split(";");
				if (part.length == 2) {
					Race r = new Race(part[0], part[1]);
					raceName.put(part[1], r);
					raceCode.put(part[0], r);
					race.add(r);
					valid.add(part[0]);
				}
			}
		} finally {
			reader.close();
		}
		Collections.sort(race);
	}

	private void loadItems(File f) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		itemId = new HashMap<Short, Item>();
		item = new ArrayList<Item>();
		itemName = new HashMap<String, Item>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				Item i = null;
				String[] part = line.split(";");
				if (part.length == 2) {
					short id = Decoder.shortFromInt(part[0]);
					i = new Item(id, part[1]);
				} else if (part.length == 3) {
					short id = Decoder.shortFromInt(part[0]);
					i = new Item(id, part[1], part[2]);
				} else if (part.length == 4) {
					short id = Decoder.shortFromInt(part[0]);
					i = new Item(id, part[1], part[2], Integer.valueOf(part[3]));
				}
				if (i != null) {
					itemId.put(i.getId(), i);
					itemName.put(part[1], i);
					item.add(i);
				}
			}
		} finally {
			reader.close();
		}
		Collections.sort(item);
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
	
	/**
	 * Reads the image fingerprint information from the file and returns a Map of 
	 * Integer fingerprint to feature code
	 * @param file the image fingerprint file
	 * @return a Map of fingerprints to feature codes
	 * @throws IOException
	 */
	private void loadMapIcons(File f) throws IOException {
		if (!f.exists()) {
			throw new FileNotFoundException("Cannot find " + f.getAbsolutePath());
		}
		mapIcons = new HashMap<Integer, String>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line = reader.readLine();
			while (line != null) {
				String[] data = line.split(";");
				mapIcons.put(Integer.valueOf(data[0]), data[1]);
				line = reader.readLine();
			}
		} finally {
			reader.close();
		}
	}
	
}
