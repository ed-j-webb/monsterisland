package net.edwebb.jim.data;

import javax.swing.ImageIcon;

public class Flag extends Feature implements Comparable<Flag> {

	private short range = 1;
	
	private ImageIcon icon;
	
	public Flag(short id, String code, String name, short range, ImageIcon icon) {
		super(id, code, name);
		this.icon = icon;
		this.range = range;
	}

	public short getRange() {
		return range;
	}

	@Override
	public ImageIcon getIcon() {
		return icon;
	}

	@Override
	public int compareTo(Flag o) {
		if (o == null) {
			return 1;
		} else {
			return getName().compareTo(o.getName());
		}
	}
}
