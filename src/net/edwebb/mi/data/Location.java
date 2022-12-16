package net.edwebb.mi.data;

import javax.swing.ImageIcon;

/**
 * A Feature that represents a location
 * 
 * @author Ed Webb
 *
 */
public class Location extends Feature implements Comparable<Location> {

	// The icon of this location
	private ImageIcon icon;

	/**
	 * Create a new location with the given id, code, name and icon
	 * @param id the id of the location
	 * @param code the code of the location. This is by convention three letters in upper case.
	 * @param name the name of the location
	 * @param icon the icon of the location
	 */
	public Location(short id, String code, String name, ImageIcon icon) {
		super(id, code, name);
		this.icon = icon;
	}

	@Override
	public ImageIcon getIcon() {
		return icon;
	}

	@Override
	public int compareTo(Location o) {
		if (o == null) {
			return 1;
		} else {
			return getName().compareTo(o.getName());
		}
	}
	
}
