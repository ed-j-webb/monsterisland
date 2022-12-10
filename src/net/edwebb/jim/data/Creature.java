package net.edwebb.jim.data;

import javax.swing.ImageIcon;

/**
 * A Feature that represents a Creature
 * 
 * @author Ed Webb
 *
 */
public class Creature extends Feature implements Comparable<Creature> {

	/**
	 * The creature icon
	 */
	public static ImageIcon icon;
	
	/**
	 * Set the icon of the Creature class to the given icon
	 * @param icon the icon to use as the creature icon
	 */
	static void setIcon(ImageIcon icon) {
		Creature.icon = icon;
	}
	
	/**
	 * Create a new creature with the given id and name. ID is between 101 and 999. The code is set to the string representation of the id.
	 * @param id the creature's id
	 * @param name the creature's name
	 */
	public Creature(short id, String name) {
		super(id, Short.toString(id), name);
	}

	@Override
	public ImageIcon getIcon() {
		return icon;
	}
	
	@Override
	public int compareTo(Creature o) {
		if (o == null) {
			return 1;
		} else {
			return getName().compareTo(o.getName());
		}
	}
	
}
