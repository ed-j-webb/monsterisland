package net.edwebb.mi.data;

import java.awt.Point;

/**
 * @deprecated Replaced by a more comprehensive turn extractor version 2.0.0
 * @author edw
 *
 */
public class Sighting {

	private Point p;
	private Feature feature;
	private String thing;
	
	public Sighting(int x, int y, String thing) {
		p = new Point(x, y);
		//this.type = type;
		this.thing = thing.substring(0, thing.length() - 2).trim();
		
		feature = findFeature(this.thing);
	}
	
	public String getThing() {
		return thing;
	}

	public Feature getFeature() {
		return feature;
	}
	
	public Point getSquare() {
		return p;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		sb.append(p.y);
		sb.append(",");
		sb.append(p.x);
		sb.append(") ");
		sb.append(" ");
		sb.append(thing);
		sb.append(" (");
		sb.append(feature);
		sb.append(")");
		
		return sb.toString();
	}
	
	public String getData() {
		StringBuffer sb = new StringBuffer();
		sb.append(p.x);
		sb.append(",");
		sb.append(p.y);
		sb.append(",");
		sb.append(feature.getCode());
		sb.append("\n");
		return sb.toString();
	}

	/**
	 * Attempts to find the Feature that thing describes. thing may be plural so try various methods to convert a plural to singular
	 * and return the first match that is made or null if none of the strategies work
	 * @param thing the name of a feature to find
	 * @return the found feature or null
	 */
	private Feature findFeature(String thing) {
		feature = DataStore.getInstance().getFeatureByName(thing);
		if (feature != null) {
			return feature;
		}
		
		int len = thing.length();
		if (thing.endsWith("s")) {
			feature = DataStore.getInstance().getFeatureByName(thing.substring(0, len-1));
			if (feature != null) {
				return feature;
			}
		}
		if (thing.endsWith("es")) {
			feature = DataStore.getInstance().getFeatureByName(thing.substring(0, len-2));
			if (feature != null) {
				return feature;
			}
		}
		if (thing.endsWith("ies")) {
			feature = DataStore.getInstance().getFeatureByName(thing.substring(0, len-3) + "y");
			if (feature != null) {
				return feature;
			}
		}
		if (thing.endsWith("i")) {
			feature = DataStore.getInstance().getFeatureByName(thing.substring(0, len-1) + "us");
			if (feature != null) {
				return feature;
			}
		}
		if (thing.endsWith("i")) {
			feature = DataStore.getInstance().getFeatureByName(thing.substring(0, len-1) + "a");
			if (feature != null) {
				return feature;
			}
		}
		if (thing.endsWith("ice")) {
			feature = DataStore.getInstance().getFeatureByName(thing.substring(0, len-3) + "ouse");
			if (feature != null) {
				return feature;
			}
		}
		if (thing.endsWith("ves")) {
			feature = DataStore.getInstance().getFeatureByName(thing.substring(0, len-3) + "f");
			if (feature != null) {
				return feature;
			}
		}
		if (thing.endsWith("ves")) {
			feature = DataStore.getInstance().getFeatureByName(thing.substring(0, len-3) + "fe");
			if (feature != null) {
				return feature;
			}
		}
		if (thing.endsWith("men")) {
			feature = DataStore.getInstance().getFeatureByName(thing.substring(0, len-3) + "man");
			if (feature != null) {
				return feature;
			}
		}
		if (thing.endsWith("eese")) {
			feature = DataStore.getInstance().getFeatureByName(thing.substring(0, len-4) + "oose");
			if (feature != null) {
				return feature;
			}
		}
		if (thing.endsWith("eet")) {
			feature = DataStore.getInstance().getFeatureByName(thing.substring(0, len-3) + "oot");
			if (feature != null) {
				return feature;
			}
		}
		return null;
	}
}
