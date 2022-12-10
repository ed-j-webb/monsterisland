package net.edwebb.mi.extract;

import net.edwebb.mi.db.DataStore;

/**
 * @author aaw129
 * @version 1.0 : 15 Mar 2011
 */
public class LocationEncounter extends Encounter {

	private String location;
	
	public String getEncType() {
		return "Location";
	}
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
		this.locationID = DataStore.getInstance().getLocationID(location);
	}

	public int getLocationID() {
		return locationID;
	}

	public void setLocationID(int locationID) {
		this.locationID = locationID;
	}

	public int getSubEncounterNumber() {
		if (encounters.size() < 2) {
			return encounters.size();
		} else if (encounters.get(encounters.size() - 2).getEncType().equals("Treasure")
		 && encounters.get(encounters.size() - 1).getEncType().equals("Treasure")) {
			return encounters.get(encounters.size() - 2).getSubEncounterNumber();
		} else {
			return encounters.get(encounters.size() - 2).getSubEncounterNumber() + 1;
		}
	}
	
	public String toString() {
		
		return super.toString() + ": " + location + " (" + locationID + ")";
	}
	
	public String getData() {
		return "";
	}
}
