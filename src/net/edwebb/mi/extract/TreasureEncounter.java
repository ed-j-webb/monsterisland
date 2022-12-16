package net.edwebb.mi.extract;

import net.edwebb.mi.data.Item;

/**
 * @author aaw129
 * @version 1.0 : 15 Mar 2011
 */
public class TreasureEncounter extends Encounter {

	private int quantity;
	private Item item;

	public String getEncType() {
		return "Treasure";
	}
	
	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		sb.append(": ");
		sb.append(quantity);
		sb.append(" ");
		sb.append(item.getName());
		sb.append(" (");
		sb.append(item.getId());
		sb.append(")");
		return sb.toString();
	}
	
	public String getData() {
		StringBuffer sb = new StringBuffer();
		sb.append("Encounter-Treasure,");
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
		sb.append(item.getId());
		sb.append(",");
		sb.append(quantity);
		sb.append("\n");
		return sb.toString();
	}
}
