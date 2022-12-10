package net.edwebb.mi.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataStore {

	private static DataStore instance;
	
	private File dbDir;

	private Map<String, Integer> creatures;
	private Map<String, Integer> locations;
	private Map<String, Integer> races;
	private Map<String, Integer> items;
	private Map<String, Integer> monsters;
	private Map<String, String> plants;
	private Map<String, String> places;
	private Map<String, Integer> itemclasses;
	private Map<String, String> equipment;
	
	private DataStore (File dbDir) {
		this.dbDir = dbDir;
	}
	
	public static void createInstance(File dbDir) {
		instance = new DataStore(dbDir);
	}
	
	public static DataStore getInstance() {
		return instance;
	}
	
	private String cleanKey(String key) {
		key = key.replaceAll("Dam\\.", "Damaged");
		key = key.replaceAll("Dam ", "Damaged ");
		key = key.replaceAll("Armor", "Armour");
		key = key.replaceAll("Leaves", "Leaf");
		key = key.replaceAll("suit of ", "");
		key = key.replaceAll("Piglron", "PigIron");
		key = key.replaceAll("- a", "");
		return key.trim();
	}
	
	public Map<String, Integer> getCreatures() {
		if (creatures == null) {
			creatures = readIDFile("micreatures.csv");
		}
		return creatures;
	}
	
	public int getCreatureID(String key) {
		Integer id = getCreatures().get(key);
		if (id == null && key.endsWith("s")) {
			id = getCreatures().get(key.substring(0, key.length()-1));
		}
		if (id == null && key.endsWith("es")) {
			id = getCreatures().get(key.substring(0, key.length()-2));
		}
		if (id == null) {
			if (key.equals("Kabamongeese")) {
				id = getCreatures().get("Kabamongoose");
			} else if (key.equals("Blood Mice")) {
				id = getCreatures().get("Blood Mouse");
			} else if (key.equals("Bloating Cheesmoes")) {
				id = getCreatures().get("Bloating Cheesmo");
			} else if (key.equals("Sistratelli")) {
				id = getCreatures().get("Sistratella");
			} else if (key.equals("Spitting Pachypi")) {
				id = getCreatures().get("Spitting Pachypus");
			} else if (key.equals("Sharkflies")) {
				id = getCreatures().get("Sharkfly");
			} else if (key.equals("Tentaculi")) {
				id = getCreatures().get("Tentaculus");
			}
		}
		return id;
	}
	
	public Map<String, Integer> getLocations() {
		if (locations == null) {
			locations = readIDFile("milocations.csv");
		}
		return locations;
	}

	public int getLocationID(String key) {
		return getLocations().get(key);
	}

	public Map<String, Integer> getItems() {
		if (items == null) {
			items = readIDFile("miitems.csv");
		}
		return items;
	}

	public int getItemID(String key) {
		key = cleanKey(key);
		if (key.equals("Gold Ring")) {
			key = "Gold Ring (Health)";
		}
		Integer id = getItems().get(key);
		if (id == null && key.endsWith("s")) {
			id = getItems().get(key.substring(0, key.length()-1));
		}
		return id;
	}

	public Map<String, String> getEquipment() {
		if (equipment == null) {
			equipment = readCodeFile("miequipment.csv");
		}
		return equipment;
	}

	public String getEquipmentCode(String key) {
		if (key == null) {
			return "";
		}
		key = cleanKey(key);
		String code = getEquipment().get(key);
		if (code == null && key.endsWith("s")) {
			code = getEquipment().get(key.substring(0, key.length()-1));
		}
		return code == null ? "" : code;
	}

	public Map<String, Integer> getClasses() {
		if (itemclasses == null) {
			itemclasses = readIDFile("miitemclasses.csv");
		}
		return itemclasses;
	}

	public int getItemClass(String key) {
		if (key == null) {
			return 0;
		}
		key = cleanKey(key);
		Integer id = getClasses().get(key);
		if (id == null && key.endsWith("s")) {
			id = getClasses().get(key.substring(0, key.length()-1));
		}
		if (id == null) {
			id = 0;
		}
		return id;
	}

	public Map<String, Integer> getRaces() {
		if (races == null) {
			races = readIDFile("miraces.csv");
		}
		return races;
	}

	public int getRaceID(String key) {
		return getRaces().get(key);
	}
	
	public Map<String, Integer> getMonsters() {
		if (monsters == null) {
			monsters = readIDFile("mimonsters.csv");
		}
		return monsters;
	}

	public int getMonsterID(Integer key) {
		Integer i = getMonsters().get(key.toString());
		if (i == null) {
			return 0;
		} else {
			return i;
		}
	}
	
	public Map<String, String> getPlants() {
		if (plants == null) {
			plants = readCodeFile("miplants.csv");
		}
		return plants;
	}

	public String getPlantCode(String key) {
		String code = getPlants().get(key);
		if (code == null && key.endsWith("s")) {
			code = getPlants().get(key.substring(0, key.length()-1));
		}
		if (code == null && key.endsWith("es")) {
			code = getPlants().get(key.substring(0, key.length()-2));
		}
		return code;
	}
	
	public Map<String, String> getPlaces() {
		if (places == null) {
			places = readCodeFile("miplaces.csv");
		}
		return places;
	}

	public String getPlaceCode(String key) {
		return getPlaces().get(key);
	}
		

	private Map<String, Integer> readIDFile(String fileName) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		File f = new File(dbDir, fileName);

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line = reader.readLine();
			while (line != null) {
				String[] data = line.split(";");
				map.put(data[0], Integer.valueOf(data[1]));
				line = reader.readLine();
			}
			return map;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				reader.close();
			} catch (Throwable t) {

			}
		}
	}

	private Map<String, String> readCodeFile(String fileName) {
		Map<String, String> map = new HashMap<String, String>();
		File f = new File(dbDir, fileName);

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line = reader.readLine();
			while (line != null) {
				String[] data = line.split(";");
				map.put(data[0], data[1]);
				line = reader.readLine();
			}
			return map;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				reader.close();
			} catch (Throwable t) {

			}
		}
	}
}
