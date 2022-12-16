package net.edwebb.mi.extract;

public class NumericEncounter extends Encounter {

	private int typeID;
	private int quantity;
	
	private String[] types = new String[] {"", "Health", "Spell Pts Max", "Muscle", "Stealth", "Food", "Toughness"};
	
	@Override
	public String getEncType() {
		return "Numeric";
	}

	public int getTypeID() {
		return typeID;
	}

	public void setType(String type) {
		if (type.equals("Health.]") || type.equals("Health]")) {
			typeID = 1;
		} else if (type.equals("Toughness.]")) {
			typeID = 6;
		} else if (type.equals("Muscle.]")) {
			typeID = 3;
		} else if (type.equals("Stealth.]") || type.equals("Stealth]")) {
			typeID = 4;
		} else if (type.equals("Food.]")) {
			typeID = 5;
		} else if (type.equals("Spell Pt Max.]")) {
			typeID = 2;
		}
	}
	
	public void setTypeID(int typeID) {
		this.typeID = typeID;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		sb.append(": ");
		sb.append(quantity);
		sb.append(" ");
		sb.append(types[typeID]);
		sb.append(" (");
		sb.append(typeID);
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String getData() {
			StringBuffer sb = new StringBuffer();
			sb.append("Encounter-Numeric,");
			sb.append(monsterNumber);
			sb.append(",");
			sb.append(turnNumber);
			sb.append(",");
			sb.append(encounterNumber);
			sb.append(",");
			sb.append(subEncounterNumber);
			sb.append(",");
			sb.append(locationCode);
			sb.append(",");
			sb.append(typeID);
			sb.append(",");
			sb.append(quantity);
			sb.append("\n");
			return sb.toString();
	}

}
