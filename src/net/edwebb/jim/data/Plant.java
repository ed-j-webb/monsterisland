package net.edwebb.jim.data;

import javax.swing.ImageIcon;

/**
 * A feature that represents a plant
 * 
 * @author Ed Webb
 *
 */
public class Plant extends Feature implements Comparable<Plant> {

	/**
	 * The plant icon
	 */
	public static ImageIcon icon;
	
	/**
	 * Set the icon of the Plant class to the given icon
	 * @param icon the icon to use as the plant icon
	 */
	static void setIcon(ImageIcon icon) {
		Plant.icon = icon;
	}
	
	/**
	 * Create a new plant with the given id, code and name
	 * @param id the id of the plant
	 * @param code the code of the plant. This is by convention three letters in lower case. 
	 * @param name the name of the plant
	 */
	public Plant(short id, String code, String name) {
		super(id, code, name);
	}
	
	@Override
	public ImageIcon getIcon() {
		return icon;
	}
	
	@Override
	public int compareTo(Plant o) {
		if (o == null) {
			return 1;
		} else {
			return getName().compareTo(o.getName());
		}
	}
	
}
