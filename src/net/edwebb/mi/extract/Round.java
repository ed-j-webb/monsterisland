package net.edwebb.mi.extract;

import java.util.Iterator;

import net.edwebb.mi.data.Item;

/**
 * @author aaw129
 * @version 1.0 : 11 Mar 2011
 */
public class Round extends Encounter{
	
	public static final int MELEE = 0;
	public static final int MISSILE = 1;
	public static final int SPELL = 2;
	
	public static final String[] TYPES = new String[] {"Melee", "Missile", "Spell"};
	
	private int type;
	private boolean monster;
	private boolean firstRound;
	private int shots;
	private int hits;
	private int health;
	private Item weapon;
	private int weaponSkill;
	private int skill;

	public void setParent(Encounter parent) {
		super.setParent(parent);
		if (type == MELEE && parent instanceof BattleEncounter) {
			firstRound = true;
			BattleEncounter enc = (BattleEncounter)parent;
			Iterator<Encounter> it = enc.getEncounters().iterator();
			while (it.hasNext()) {
				Round rnd = (Round)it.next();
				if (rnd != this && rnd.getType() == MELEE && rnd.isMonster() == this.isMonster()) {
					firstRound = false;
				}
			}
		}
	}	
	
	public String getEncType() {
		return TYPES[type];
	}
	
	public boolean isMonster() {
		return monster;
	}

	public void setMonster(boolean monster) {
		this.monster = monster;
	}

	public int getShots() {
		return shots;
	}

	public void setShots(int shots) {
		this.shots = shots;
	}

	public int getHits() {
		return hits;
	}

	public void setHits(int hits) {
		this.hits = hits;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public Item getWeapon() {
		return weapon;
	}

	public void setWeapon(Item weapon) {
		this.weapon = weapon;
	}

	public int getSkill() {
		return skill;
	}

	public void setSkill(int skill) {
		this.skill = skill;
	}

	public boolean getFirstRound() {
		return firstRound;
	}

	public void setFirstRound(boolean firstRound) {
		this.firstRound = firstRound;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public int getWeaponSkill() {
		return weaponSkill;
	}

	public void setWeaponSkill(int weaponSkill) {
		this.weaponSkill = weaponSkill;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(TYPES[type]);
		sb.append(" ");
		sb.append(monster ? "M " : "C ");
		sb.append(weapon);
		sb.append(", WS:");
		sb.append(weaponSkill);
		sb.append(" ");
		if (type == MISSILE) {
			sb.append(shots);
			sb.append(":");
		}
		sb.append(hits);
		sb.append(":");
		sb.append(health);
		return sb.toString();
	}
	
	public String getData() {
		StringBuffer sb = new StringBuffer();
		sb.append("Combat-");
		sb.append(TYPES[type]);
		sb.append(",");
		sb.append(monsterNumber);
		sb.append(",");
		sb.append(turnNumber);
		sb.append(",");
		sb.append(encounterNumber);
		sb.append(",");
		sb.append(subEncounterNumber);
		sb.append(",");
		sb.append(monster);
		sb.append(",");
		if (type == MELEE) {
			sb.append(firstRound);
			sb.append(",");
		}
		if (weapon != null) {
			sb.append(weapon.getId());
		}
		if (type == MELEE) {
			// WeaponType
			sb.append(",");
			if (weapon != null) {
				if (weapon.equals("Wrestling")) {
					sb.append("Wr");
				} else {
					sb.append(weapon.getEquipType());
				}
			}
		}
		if (type != SPELL) {
			sb.append(",");
			if (weapon != null) {
				sb.append(weapon.getEquipLevel());
			}
			sb.append(",");
			sb.append(weaponSkill);
			sb.append(",");
			if (type == MISSILE) {
				sb.append(shots);
				sb.append(",");
			}
			sb.append(hits);
		}
		sb.append(",");
		sb.append(health);
		sb.append("\n");
		return sb.toString();
	}
}
