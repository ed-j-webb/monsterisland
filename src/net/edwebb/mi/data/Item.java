package net.edwebb.mi.data;

public class Item implements Comparable<Item> {

	public enum EQUIP_TYPE {
		Ba, Mi, Pl, Pt, Wp, Ed,	Wr, De, He, Ga, Gr, Bo, Am, Ri, Ch;
	}
	
	private short id;
	private String name;
	private EQUIP_TYPE equipType;
	private int equipLevel;
	
	public Item(short id, String name) {
		this(id, name, null, 0);
	}

	public Item(short id, String name, String equipType) {
		this(id, name, equipType, 0);
	}
	
	public Item(short id, String name, String equipType, int equipLevel) {
		this.id = id;
		this.name = name;
		this.equipType =  equipType == null ? null : EQUIP_TYPE.valueOf(equipType);
		this.equipLevel = equipLevel;
	}

	public short getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public EQUIP_TYPE getEquipType() {
		return equipType;
	}

	public int getEquipLevel() {
		return equipLevel;
	}

	@Override
	public int compareTo(Item o) {
		if (o == null) {
			return 1;
		} else {
			return getName().compareTo(o.getName());
		}
	}

	@Override
	public String toString() {
		return "Item #" + id + ": " + name + (equipType != null ? " " + equipType + ":" + equipLevel : "");
	}
}

