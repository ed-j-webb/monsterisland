package net.edwebb.mi.extract;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ed Webb
 * @version 1.0 : 11 Mar 2011
 */
public class Turn extends Encounter {

	private Stats stats = new Stats(this);
	private Set<Sighting> sightings;

	public Turn() {
		muscle = 0;
		encounterNumber = 0;
		subEncounterNumber = 0;
		turnNumber = 0;
		sightings = new HashSet<Sighting>();
		locationCode = "";
	}
	
	public void setMonsterNumber(int monsterNumber) {
		this.monsterNumber = monsterNumber;
	}
	
	public Set<Sighting> getSightings() {
		return sightings;
	}


	public void setSightings(Set<Sighting> sightings) {
		this.sightings = sightings;
	}

	public String getEncType() {
		return "Turn";
	}
	
	public Stats getStats() {
		return stats;
	}

	public void setStats(Stats stats) {
		this.stats = stats;
	}
	
	public int getEncounterNumber() {
		return encounters.size();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Turn ");
		sb.append(turnNumber);
		sb.append(" Monster ");
		sb.append(monsterNumber);
		return sb.toString();
	}
	public String getData() {
		return "";
	}
}
