package net.edwebb.mi.data;

import javax.swing.ImageIcon;

/**
 * An abstract feature that can be displayed on the map
 * 
 * @author Ed Webb
 *
 */
public abstract class Feature {

	// The feature's id
	private short id;
	
	// The feature's code
	private String code;
	
	// The feature's name
	private String name;
	
	/**
	 * Create a new feature with the given id, code and name
	 * @param id the feature's id
	 * @param code the feature's code
	 * @param name the feature's name
	 */
	public Feature(short id, String code, String name) {
		this.id = id;
		this.code = code;
		this.name = name;
	}
	
	/**
	 * Returns the id of the feature
	 * @return the id of the feature
	 */
	public short getId() {
		return id;
	}
	
	/**
	 * Returns the code of the feature
	 * @return the code of the feature
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * Returns the name of the feature
	 * @return the name of the feature
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the icon of the feature
	 * @return the icon of the feature
	 */
	public abstract ImageIcon getIcon();

	@Override
	public String toString() {
		return getName() + " (" + getCode() + ")";
	}

}
