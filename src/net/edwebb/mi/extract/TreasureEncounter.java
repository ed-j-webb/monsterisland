package net.edwebb.mi.extract;

import net.edwebb.mi.db.DataStore;

/**
 * @author aaw129
 * @version 1.0 : 15 Mar 2011
 */
public class TreasureEncounter extends Encounter {

	private int quantity;
	private String item;
	private Integer itemID;

	public String getEncType() {
		return "Treasure";
	}
	
	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
		this.itemID = DataStore.getInstance().getItemID(item);
	}

	public Integer getItemID() {
		return itemID;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		sb.append(": ");
		sb.append(quantity);
		sb.append(" ");
		sb.append(item);
		sb.append(" (");
		sb.append(itemID);
		sb.append(")");
		return sb.toString();
	}
	
	public String getData() {
		StringBuffer sb = new StringBuffer();
		sb.append("Encounter-Treasure,");
		sb.append(monsterID);
		sb.append(",");
		sb.append(turnNumber);
		sb.append(",");
		sb.append(encounterNumber);
		sb.append(",");
		sb.append(subEncounterNumber);
		sb.append(",");
		sb.append(locationID);
		sb.append(",");
		sb.append(itemID);
		sb.append(",");
		sb.append(quantity);
		sb.append("\n");
		return sb.toString();
	}
}
