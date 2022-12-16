package net.edwebb.mi.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.data.Item;

/**
 * @author aaw129
 * @version 1.0 : 15 Mar 2011
 */
public class Stats {
	private Map<String, Integer> stats = new HashMap<String, Integer>();
	private Map<String, String> equip = new HashMap<String, String>();
	private Turn turn;
	
	public static final List<String> STATS = new ArrayList<String>();
	public static final List<String> EQUIP = new ArrayList<String>();
	public static final List<String> SKILL = new ArrayList<String>();
	
	static {
		SKILL.add("Missile:");
		SKILL.add("Bashing:");
		SKILL.add("Pointed:");
		SKILL.add("Edged:");
		SKILL.add("Pole:");
		SKILL.add("Whip:");

		EQUIP.add("Primary Weapon:");
		EQUIP.add("Missile Weapon:");
		EQUIP.add("Defense Weapon:");
		EQUIP.add("Body Armor:");
		EQUIP.add("Helm:");
		EQUIP.add("Gauntlets:");
		EQUIP.add("Greaves:");
		EQUIP.add("Ring:");
		EQUIP.add("Amulet:");
		EQUIP.add("Charm:");
		EQUIP.add("Voodoo Item:");
		EQUIP.add("Wrestle Weapon:");
		EQUIP.add("Defensive Battle Spell:");
		EQUIP.add("Offensive Battle Spell:");
		
		STATS.add("Skin Toughness");
		STATS.add("Toughness");
		STATS.add("Muscle");
		STATS.add("Badness");
		STATS.add("Monsterliness");
		STATS.add("Stealth");
		STATS.add("Knowledge Blurbs");
		STATS.add("Health");
		STATS.add("Spell Pts");
		STATS.add("Max Spell Pts.");
		STATS.add("Food consumed");
		STATS.add("Creatures killed");
		STATS.add("Monsters battled");
		STATS.add("Offense");
		STATS.add("Defense");
		STATS.add("Bouts");
		STATS.add("Tricks");
		STATS.add("Action Pts");
		//TODO riding skill comes after the text not like all others!
		//STATS.add("Riding Skill");
	}

	public Stats(Turn turn) {
		this.turn = turn;
	}
	
	public Map<String, Integer> getStats() {
		return stats;
	}

	public void setStats(Map<String, Integer> stats) {
		this.stats = stats;
	}

	public Map<String, String> getEquip() {
		return equip;
	}

	public void setEquip(Map<String, String> equip) {
		this.equip = equip;
	}
	
	public Turn getTurn() {
		return turn;
	}

	public void setTurn(Turn turn) {
		this.turn = turn;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("\n");
		sb.append("\n   Monsterliness: ");
		sb.append(stats.get("Monsterliness"));
		sb.append("\n       Toughness: ");
		sb.append(stats.get("Toughness"));
		sb.append("\n          Muscle: ");
		sb.append(stats.get("Muscle"));
		sb.append("\n         Badness: ");
		sb.append(stats.get("Badness"));
		sb.append("\n         Stealth: ");
		sb.append(stats.get("Stealth"));
		sb.append("\n          Health: ");
		sb.append(stats.get("Health"));
		sb.append("\n       Spell Pts: ");
		sb.append(stats.get("Spell Pts"));
		sb.append("\n   Max Spell Pts: ");
		sb.append(stats.get("Max Spell Pts."));
		sb.append("\n       Knowledge: ");
		sb.append(stats.get("Knowledge Blurbs"));
		sb.append("\nCreatures killed: ");
		sb.append(stats.get("Creatures killed"));
		sb.append("\nMonsters battled: ");
		sb.append(stats.get("Monsters battled"));
		sb.append("\n  Skin Toughness: ");
		sb.append(stats.get("Skin Toughness"));
		sb.append("\n      Action Pts: ");
		sb.append(stats.get("Action Pts"));
		sb.append("\n    Riding Skill: ");
		sb.append(stats.get("Riding Skill"));
		sb.append("\n");
		sb.append("Missile: ");
		sb.append(stats.get("Missile:"));
		sb.append(", Bashing: ");
		sb.append(stats.get("Bashing:"));
		sb.append(", Pointed: ");
		sb.append(stats.get("Pointed:"));
		sb.append("\n");
		sb.append("  Edged: ");
		sb.append(stats.get("Edged:"));
		sb.append(",    Pole: ");
		sb.append(stats.get("Pole:"));
		sb.append(",    Whip: ");
		sb.append(stats.get("Whip:"));
		sb.append("\n");
		sb.append("Bouts: ");
		sb.append(stats.get("Bouts"));
		sb.append(", Offense: ");
		sb.append(stats.get("Offense"));
		sb.append(", Defense: ");
		sb.append(stats.get("Defense"));
		sb.append(", Tricks: ");
		sb.append(stats.get("Tricks"));
		sb.append("\nPrimary Weapon: ");
		sb.append(equip.get("Primary Weapon:"));
		sb.append("\nMissile Weapon: ");
		sb.append(equip.get("Missile Weapon:"));
		sb.append("\nDefense Weapon: ");
		sb.append(equip.get("Defense Weapon:"));
		sb.append("\n    Body Armor: ");
		sb.append(equip.get("Body Armor:"));
		sb.append("\n          Helm: ");
		sb.append(equip.get("Helm:"));
		sb.append("\n     Gauntlets: ");
		sb.append(equip.get("Gauntlets:"));
		sb.append("\n       Greaves: ");
		sb.append(equip.get("Greaves:"));
		sb.append("\n          Ring: ");
		sb.append(equip.get("Ring:"));
		sb.append("\n        Amulet: ");
		sb.append(equip.get("Amulet:"));
		sb.append("\n         Charm: ");
		sb.append(equip.get("Charm:"));
		sb.append("\n   Voodoo Item: ");
		sb.append(equip.get("Voodoo Item:"));
		sb.append("\nWrestle Weapon: ");
		sb.append(equip.get("Wrestle Weapon:"));
		sb.append("\nOff. Spell: ");
		sb.append(equip.get("Offensive Battle Spell:"));
		sb.append(", Def Spell: ");
		sb.append(equip.get("Defensive Battle Spell:"));
		sb.append("\nTalismans: ");
		sb.append(stats.get("Talismans"));
		sb.append(", Treasures: ");
		sb.append(stats.get("Treasures"));
		sb.append("\nMount: ");
		sb.append(equip.get("Mount"));
		sb.append(" (");
		sb.append(stats.get("MountHealth"));
		sb.append(", ");
		sb.append(stats.get("MountTough"));
		sb.append(")");
		sb.append("\n\n-------------------------------------------------------------------------\n");
		
		return sb.toString();
	}
	
	public String getData() {
		StringBuffer sb = new StringBuffer();
		sb.append("Stats,");
		sb.append(turn.getMonsterNumber());
		sb.append(",");
		sb.append(turn.getTurnNumber());
		sb.append(",");
		sb.append(stats.get("Monsterliness"));
		sb.append(",");
		sb.append(stats.get("Toughness"));
		sb.append(",");
		sb.append(stats.get("Muscle"));
		sb.append(",");
		sb.append(stats.get("Badness"));
		sb.append(",");
		sb.append(stats.get("Stealth"));
		sb.append(",");
		sb.append(stats.get("Health"));
		sb.append(",");
		sb.append(stats.get("Spell Pts"));
		sb.append(",");
		sb.append(stats.get("Knowledge Blurbs"));
		sb.append(",");
		sb.append(stats.get("Max Spell Pts."));
		sb.append(",");
		sb.append(stats.get("Creatures killed"));
		sb.append(",");
		sb.append(stats.get("Monsters battled"));
		sb.append(",");
		sb.append(stats.get("Action Pts"));
		sb.append(",");
		sb.append(stats.get("Missile:"));
		sb.append(",");
		sb.append(stats.get("Bashing:"));
		sb.append(",");
		sb.append(stats.get("Pointed:"));
		sb.append(",");
		sb.append(stats.get("Edged:"));
		sb.append(",");
		sb.append(stats.get("Pole:"));
		sb.append(",");
		sb.append(stats.get("Whip:"));
		sb.append(",");
		sb.append(stats.get("Bouts"));
		sb.append(",");
		sb.append(stats.get("Offense"));
		sb.append(",");
		sb.append(stats.get("Defense"));
		sb.append(",");
		sb.append(stats.get("Tricks"));
		sb.append(",");
		sb.append(stats.get("Skin Toughness"));
		sb.append(",");
		sb.append(stats.get("Talismans"));
		sb.append(",");
		sb.append(stats.get("Treasures"));
		sb.append(",");
		sb.append(getItemID(equip.get("Primary Weapon:")));
		sb.append(",");
		sb.append(getItemID(equip.get("Missile Weapon:")));
		sb.append(",");
		sb.append(getItemID(equip.get("Defense Weapon:")));
		sb.append(",");
		sb.append(getItemID(equip.get("Body Armor:")));
		sb.append(",");
		sb.append(getItemID(equip.get("Helm:")));
		sb.append(",");
		sb.append(getItemID(equip.get("Gauntlets:")));
		sb.append(",");
		sb.append(getItemID(equip.get("Greaves:")));
		sb.append(",");
		sb.append(getItemID(equip.get("Ring:")));
		sb.append(",");
		sb.append(getItemID(equip.get("Amulet:")));
		sb.append(",");
		sb.append(getItemID(equip.get("Charm:")));
		sb.append(",");
		sb.append(getItemID(equip.get("Voodoo Item:")));
		sb.append(",");
		sb.append(getItemID(equip.get("Wrestle Weapon:")));
		sb.append(",");
		sb.append(equip.get("Defensive Battle Spell:"));
		sb.append(",");
		sb.append(equip.get("Offensive Battle Spell:"));
		sb.append(",");
		sb.append(stats.get("Riding Skill"));
		sb.append(",");
		sb.append(equip.get("Mount"));
		sb.append(",");
		sb.append(equip.get("MountTough"));
		sb.append(",");
		sb.append(equip.get("MountHealth"));
		sb.append("\n");
		return sb.toString();
	}
	
	public int getArmourClass() {
		int ac = 0;
		ac += getItemClass(equip.get("Greaves:")); 
		ac += getItemClass(equip.get("Gauntlets:"));
		ac += getItemClass(equip.get("Helm:"));
		ac += getItemClass(equip.get("Defense Weapon:"));
		ac += getItemClass(equip.get("Body Armor:")) * 3;
		ac += (stats.get("Skin Toughness") == null) ? 0 : stats.get("Skin Toughness");
		if (equip.get("Amulet:") != null && equip.get("Amulet:").endsWith("Protection")) {
			ac += 4;
		}
		if (equip.get("Charm:") != null && equip.get("Charm:").endsWith("Protection")) {
			ac += 4;
		}
		return ac;
	}
	
	private int getItemClass(String itemName) {
		if (itemName == null) {
			return 0;
		}
		itemName = itemName.replaceAll("Armor", "Armour");
		if (itemName.endsWith(",")) {
			itemName = itemName.substring(0, itemName.length()-1);
		}
		if (itemName.equals("Gold Ring")) {
			itemName = "Gold Ring (Health)";
		}
		Item item = DataStore.getInstance().getItem(itemName);
		if (item == null) {
			return 0;
		}
		return item.getEquipLevel();
	}
	
	private String getItemID(String itemName) {
		if (itemName == null) {
			return "";
		}
		if (itemName.endsWith(",")) {
			itemName = itemName.substring(0, itemName.length()-1);
		}
		if (itemName.equals("Gold Ring")) {
			itemName = "Gold Ring (Invisibility)";
		}
		Item item = DataStore.getInstance().getItem(itemName);
		if (item == null) {
			return "";
		}
		return Short.toString(item.getId());
	}
}
