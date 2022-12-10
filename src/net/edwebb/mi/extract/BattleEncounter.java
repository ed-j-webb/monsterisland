package net.edwebb.mi.extract;

import java.util.ArrayList;
import java.util.List;

import net.edwebb.mi.db.DataStore;


/**
 * @author aaw129
 * @version 1.0 : 11 Mar 2011
 */
public class BattleEncounter extends Encounter {

	private String creature;
	private Integer creatureID;
	private int food;
	private int item;
	private int outcome;
	
	public static final int CREATURE_KILLED = 1;
	public static final int CREATURE_FLEE = 2;
	public static final int MONSTER_KILLED = 3;
	public static final int MONSTER_FLEE = 4;
	
	public static final String[] OUTCOME = new String[] {"UK", "CK", "CF", "MK", "MF"};
	
	List<Round> rounds = new ArrayList<Round>();

	public String getEncType() {
		if (creatureID != null && creatureID < 100) {
			return "Monster";
		} else {
			return "Creature";
		}
	}
	
	public String getCreature() {
		return creature;
	}

	public void setCreature(String creature) {
		this.creature = creature;
		if (parent.getEncType().equals("Location")) {
			LocationEncounter le = (LocationEncounter)parent;
			if (le.getLocation().equals("Loggerhead Camp")) {
				creatureID = DataStore.getInstance().getRaceID("Loggerhead");
			} else if (le.getLocation().equals("Hillock")) {
				creatureID = DataStore.getInstance().getRaceID("Knolltir");
			} else if (le.getLocation().equals("Mine Shaft")) {
				creatureID = DataStore.getInstance().getRaceID("Rock Troll");
			} else if (le.getLocation().equals("Bodden Camp") && creature.toUpperCase().equals(creature)) {
				creatureID = DataStore.getInstance().getRaceID("Bodden");
			} else if (le.getLocation().equals("Bodden Camp") && creature.contains("'")) {
				creatureID = DataStore.getInstance().getRaceID("High Bodden");
			}
		}
		//if (creature.equals(creature.toUpperCase())) {
		//    creatureID = DataStore.getRaceID("Loggerhead");
		//}
		if (creatureID == null || creatureID == 0) {
			creatureID = DataStore.getInstance().getCreatureID(creature);
		}
	}

	public Integer getCreatureID() {
		return creatureID;
	}

	public int getFood() {
		return food;
	}

	public void setFood(int food) {
		this.food = food;
	}

	public int getItem() {
		return item;
	}

	public void setItem(int item) {
		this.item = item;
	}

	public List<Round> getRounds() {
		return rounds;
	}

	public void setRounds(List<Round> rounds) {
		this.rounds = rounds;
	}

	public int getOutcome() {
		return outcome;
	}

	public void setOutcome(int outcome) {
		this.outcome = outcome;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		sb.append(": ");
		sb.append(creature);
		sb.append(" (");
		sb.append(creatureID);
		sb.append(") ");
		sb.append(OUTCOME[outcome]);
		sb.append(" Items: ");
		sb.append(item);
		sb.append(" Food: ");
		sb.append(food);
		sb.append(" Muscle: ");
		sb.append(muscle);
		sb.append(" AC: ");
		sb.append(armourClass);
		return sb.toString();
	}

	
	/*
	public void setParent(Encounter enc) {
		super.setParent(enc);
		setMuscle(enc.getMuscle());
		setX(enc.getX());
		setY(enc.getY());
	}
	*/
	public String getData() {
		StringBuffer sb = new StringBuffer();
		sb.append("Encounter-");
		sb.append(getEncType());
		sb.append(",");
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
		sb.append(creatureID);
		if (getEncType().equals("Monster")) {
			sb.append(",");
			sb.append(creature);
		}
		sb.append(",");
		sb.append(outcome);
		sb.append(",");
		sb.append(muscle);
		sb.append(",");
		sb.append(armourClass);
		if (getEncType().equals("Monster")) {
			sb.append(",");
			sb.append(opponentAC);
		} else {
			sb.append(",");
			sb.append(food);
			sb.append(",");
			sb.append(item);
		}
		sb.append("\n");
		return sb.toString();
	}
}
