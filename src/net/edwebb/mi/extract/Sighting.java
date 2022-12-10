package net.edwebb.mi.extract;

import net.edwebb.mi.db.DataStore;

public class Sighting {

	private int x;
	private int y;
	private String type;
	private String thing;
	private String code;
	
	public Sighting(int x, int y, String type, String thing) {
		this.x = x;
		this.y = y;
		this.type = type;
		this.thing = thing.substring(0, thing.length() - 2).trim();
		createCode();
	}

	public Sighting(int x, int y, String code) {
		this.x = x;
		this.y = y;
		this.code = code;
	}

	public void createCode() {
		StringBuffer sb = new StringBuffer();
		if (type.equals("Unknown")) {
			if (DataStore.getInstance().getPlantCode(thing) != null) {
				type = "Plant";
			} else if (DataStore.getInstance().getPlaceCode(thing) != null) {
				type = "Location";
			} else if (thing.equals("Trapped Pit")) {
				type = "Location";
				thing = "Deep Pit";
			} else {
				type = "Creature";
			}
		}
		if (type.equals("Creature")) {
			sb.append("@");
			sb.append(DataStore.getInstance().getCreatureID(thing));
		} else if (type.equals("Plant")) {
			sb.append("$");
			sb.append(DataStore.getInstance().getPlantCode(thing));
		} else {
			sb.append("%");
			sb.append(DataStore.getInstance().getPlaceCode(thing));
		}
		
		code = sb.toString();
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getThing() {
		return thing;
	}
	
	public void setThing(String thing) {
		this.thing = thing;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		sb.append(y);
		sb.append(",");
		sb.append(x);
		sb.append(") ");
		sb.append(type);
		sb.append(" ");
		sb.append(thing);
		sb.append(" (");
		sb.append(code);
		sb.append(")");
		
		return sb.toString();
	}
	
	public String getData() {
		StringBuffer sb = new StringBuffer();
		sb.append(x);
		sb.append(",");
		sb.append(y);
		sb.append(",");
		sb.append(code);
		sb.append("\n");
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Sighting) {
			return obj.toString().equals(this.toString());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	

}
