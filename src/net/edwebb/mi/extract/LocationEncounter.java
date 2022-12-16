package net.edwebb.mi.extract;

import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.data.Location;

/**
 * @author aaw129
 * @version 1.0 : 15 Mar 2011
 */
public class LocationEncounter extends Encounter {

	private Location location;
	
	
	public LocationEncounter() {
		super();
	}
	
	public String getEncType() {
		return "Location";
	}
	
	public Location getLocation() {
		return location;
	}

	public void setLocation(String locationName) {
		this.location = DataStore.getInstance().getLocation(locationName);
	}

	@Override
	public String getLocationCode() {
		if (location != null) {
			return location.getCode();
		}
		return null;
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
		
		return super.toString() + ": " + location + " (" + locationCode + ")";
	}
	
	public String getData() {
		return "";
	}
}
