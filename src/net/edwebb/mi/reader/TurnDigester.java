package net.edwebb.mi.reader;

import java.util.ArrayList;
import java.util.List;

import net.edwebb.mi.data.Sighting;

@Deprecated
public class TurnDigester {

	/**
	 * Reads the results of a Crystal Ball or Orb of Seeing use
	 * 
	 * @param reader the turn reader
	 * @param locx the monster's x co-ordinate
	 * @param locy the monster's y co-ordinate
	 * @return a list of Sightings from the scrying
	 */
	public static List<Sighting> readScrye(TurnReader reader, int locx, int locy) {
		List<Sighting> sightings = new ArrayList<Sighting>();
		boolean listing = false;
		StringBuffer thing = new StringBuffer();
		int x = 0;
		int y = 0;

		while(reader.hasNext()) {
			if (reader.match("Action Pts used,")) {
				break;
			}
			
			if (reader.match("You see the following") || reader.match("You detect the following")) {
				listing = false;
				thing = new StringBuffer();
			} else if (reader.match("Creatures:") || reader.match("Plants:") || reader.match("Hazards:") || reader.match("Structures:") || reader.match("Places:")) {
				listing = true;
				thing = new StringBuffer();
			} else if (reader.match("* East, * South*")) {
				x = locx + reader.getNumber();
				y = locy - reader.getNumber(2);
				reader.increment(3);
			} else if (reader.match("* East, * North*")) {
				x = locx + reader.getNumber();
				y = locy + reader.getNumber(2);
				reader.increment(3);
			} else if (reader.match("* West, * South*")) {
				x = locx - reader.getNumber();
				y = locy - reader.getNumber(2);
				reader.increment(3);
			} else if (reader.match("* West, * North*")) {
				x = locx - reader.getNumber();
				y = locy + reader.getNumber(2);
				reader.increment(3);
			} else if (reader.match ("*Your square*")) {
				x = locx;
				y = locy;
				reader.increment(1);
			} else if (reader.match ("* South*")) {
				x = locx;
				y = locy - reader.getNumber();
				reader.increment(1);
			} else if (reader.match ("* North*")) {
				x = locx;
				y = locy + reader.getNumber();
				reader.increment(1);
			} else if (reader.match ("* East*")) {
				x = locx + reader.getNumber();
				y = locy;
				reader.increment(1);
			} else if (reader.match ("* West*")) {
				x = locx - reader.getNumber();
				y = locy;
				reader.increment(1);
			} else if (reader.match("and")) {
				thing.append(" ");
			} else if (reader.match("a") || reader.match("an")) {
				// Ignore this
			} else {
				thing.append(reader.get());
				thing.append(" ");
			}
			if (reader.endsWith(",") || reader.endsWith(".") || reader.match("and")) {
				if (listing) {
					if (thing.toString().trim().length() > 0 && !thing.toString().startsWith("None")) {
						Sighting s = new Sighting(x, y, thing.toString());
						sightings.add(s);
					}
				}
				thing = new StringBuffer();
			}
			
			reader.increment();
		}
	
		return sightings;
	}
	
}
