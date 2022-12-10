package net.edwebb.mi.extract;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aaw129
 * @version 1.0 : 11 Mar 2011
 */
public abstract class Encounter {

	protected Encounter parent;
	protected List<Encounter> encounters = new ArrayList<Encounter>();
	protected int monsterID;
	protected int turnNumber;
	protected int encounterNumber;
	protected int subEncounterNumber;
	protected int locationID;
	protected int muscle;
	protected int armourClass;
	protected int opponentAC;
	protected int x;
	protected int y;
	
	public List<Encounter> getEncounters() {
		return encounters;
	}

	public void setEncounters(List<Encounter> encounters) {
		this.encounters = encounters;
	}

	public Encounter getParent() {
		return parent;
	}

	public void setParent(Encounter parent) {
		this.parent = parent;
		parent.getEncounters().add(this);
		this.monsterID = parent.getMonsterID();
		this.turnNumber = parent.getTurnNumber();
		this.encounterNumber = parent.getEncounterNumber();
		this.subEncounterNumber = parent.getSubEncounterNumber();
		if (locationID == 0) {
			locationID = parent.getLocationID();
		}
		this.muscle = parent.getMuscle(); 
		this.x = parent.getX();
		this.y = parent.getY();
		this.armourClass = parent.getArmourClass();
	}
	
	public abstract String getEncType();
	
	public abstract String getData();

	public int getMonsterID() {
		return monsterID;
	}

	public int getTurnNumber() {
		return turnNumber;
	}

	public void setTurnNumber(int turnNumber) {
		this.turnNumber = turnNumber;
	}

	public int getEncounterNumber() {
		return encounterNumber;
	}

	public void setEncounterNumber(int encounterNumber) {
		this.encounterNumber = encounterNumber;
	}

	public int getSubEncounterNumber() {
		return subEncounterNumber;
	}

	public void setSubEncounterNumber(int subEncounterNumber) {
		this.subEncounterNumber = subEncounterNumber;
	}

	public int getLocationID() {
		return locationID;
	}

	public int getMuscle() {
		return muscle;
	}

	public void setMuscle(int muscle) {
		this.muscle = muscle;
	}

	public int getOpponentAC() {
		return opponentAC;
	}

	public void setOpponentAC(int opponentAC) {
		this.opponentAC = opponentAC;
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

	public int getArmourClass() {
		return armourClass;
	}

	public void setArmourClass(int armourClass) {
		this.armourClass = armourClass;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(encounterNumber);
		sb.append(":");
		sb.append(subEncounterNumber);
		sb.append(" ");
		sb.append(getEncType());
		sb.append(" (");
		sb.append(y);
		sb.append(",");
		sb.append(x);
		sb.append(")");
		return sb.toString();
	}
}
