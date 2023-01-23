package net.edwebb.jim.model;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.edwebb.jim.MapConstants.ChangeType;
import net.edwebb.jim.model.events.FeatureChangeEvent;
import net.edwebb.jim.model.events.FlagChangeEvent;
import net.edwebb.jim.model.events.MapChangeEvent;
import net.edwebb.jim.model.events.MapChangeListener;
import net.edwebb.jim.model.events.NoteChangeEvent;
import net.edwebb.jim.model.events.TerrainChangeEvent;
import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.data.Feature;

/**
 * An index of features and notes on the map that can be used to quickly search for features or notes.
 * 
 * @author Ed Webb
 *
 */
public class MapIndex implements MapChangeListener {
	
	private static final List<Point> EMPTY_LIST = new ArrayList<Point>(0);
	
	private MapModel model;
	
	// A map keyed on Feature with a list of squares where the feature is present
	private Map<Feature, List<Point>> features;
	
	// A map keyed on note with the square where the note is present.
	private Map<String, List<Point>> notes;
	
	// A map keyed on the extra information from the model
	private Map<Integer, List<Point>> extras;
	
	/**
	 * Creates a new empty MapIndex
	 */
	public MapIndex(MapModel model) {
		setModel(model);
	}
	
	/**
	 * Indexes the given model
	 * @param model the map to index
	 */
	public void setModel(MapModel model) {
		if (this.model != null) {
			this.model.removeMapChangeListener(this);
		}
		this.model = model;
		model.addMapChangeListener(this);
		
		features = new HashMap<Feature, List<Point>>();
		notes = new HashMap<String, List<Point>>();
		extras = new HashMap<Integer, List<Point>>();

		Point p = new Point(0,0);
		Rectangle bounds = model.getBounds();
		for(int x = bounds.x; x < bounds.width + bounds.width; x++) {
			for (int y = bounds.y; y > bounds.y - bounds.height; y--) {
				p.move(x, y);
				short[] sqr = model.getSquare(p);
				if (sqr != null && sqr.length > 1) {
					Point pt = new Point(p);
					for (int z = 1; z < sqr.length; z++) {
						Feature f = DataStore.getInstance().getFeatureById(sqr[z]);
						if (f != null) {
							addFeature(f, pt);
						}
					}
				}
				String note = model.getSquareNote(p);
				if (note != null && note.trim().length() > 0) {
					Point pt = new Point(p);
					addNote(note.toLowerCase(), pt);
				}
				checkExtra(p);
			}
		}
		sortPoints();
	}
	
	@Override
	public void mapChanged(MapChangeEvent event) {
		if (event.getChangeType().equals(ChangeType.NOTE)) {
			NoteChangeEvent noteEvent = (NoteChangeEvent)event;
			if (noteEvent.getOldNote() != null) {
				removeNote(noteEvent.getOldNote(), noteEvent.getSquare());
			}
			if (noteEvent.getNewNote() != null) {
				addNote(noteEvent.getNewNote(), noteEvent.getSquare());
			}
			checkExtra(noteEvent.getSquare());
			return;
		}
		
		if (event.getChangeType().equals(ChangeType.FLAG)) {
			FlagChangeEvent flagEvent = (FlagChangeEvent)event;
			if (flagEvent.isSet()) {
				addFeature(flagEvent.getFlag(), flagEvent.getSquare());
			} else {
				removeFeature(flagEvent.getFlag(), flagEvent.getSquare());
			}
			checkExtra(flagEvent.getSquare());
			return;
		}

		if (event.getChangeType().equals(ChangeType.FEATURE)) {
			FeatureChangeEvent featureEvent = (FeatureChangeEvent)event;
			if (featureEvent.isAdded()) {
				addFeature(featureEvent.getFeature(), featureEvent.getSquare());
			} else {
				removeFeature(featureEvent.getFeature(), featureEvent.getSquare());
			}
			checkExtra(featureEvent.getSquare());
			return;
		}

		if (event.getChangeType().equals(ChangeType.TERRAIN)) {
			TerrainChangeEvent terrainEvent = (TerrainChangeEvent)event;
			if (terrainEvent.getOldTerrain() != null) {
				removeFeature(terrainEvent.getOldTerrain(), terrainEvent.getSquare());
			}
			if (terrainEvent.getNewTerrain() != null) {
				addFeature(terrainEvent.getNewTerrain(), terrainEvent.getSquare());
			}
			checkExtra(terrainEvent.getSquare());
			return;
		}
	}

	/**
	 * Returns all squares where the feature is present
	 * @param f the feature to search for
	 * @return a list of squares where the feature is present
	 */
	public List<Point> getPoints(Feature f) {
		List<Point> l = features.get(f);
		if (l == null) {
			l = EMPTY_LIST;
		}
		return l;
	}
	
	/**
	 * Returns all squares within a given distance where the feature is present
	 * @param f the feature to search for
	 * @param p the origin of the search
	 * @param distance the distance from the origin to search within
	 * @return a list of squares where the feature is present within the given distance
	 */
	public List<Point> getPoints(Feature f, Point p, int distance) {
		List<Point> l = new ArrayList<Point>();
		List<Point> points = getPoints(f);
		Iterator<Point> it = points.iterator();
		while (it.hasNext()) {
			Point z = it.next();
			int d = distance(z, p);
			if (d < distance) {
				l.add(z);
			}
		}
		return l;
	}

	/**
	 * Returns the closest square to the origin where the feature is present
	 * @param f the feature to search for
	 * @param p the origin of the search
	 * @return the square that is closest to the origin and where the feature is present
	 */
	public Point getPoint(Feature f, Point p) {
		Point closest = null;
		int distance = Integer.MAX_VALUE;
		List<Point> points = getPoints(f);
		Iterator<Point> it = points.iterator();
		while (it.hasNext()) {
			Point z = it.next();
			int d = distance(z, p);
			if (d < distance) {
				closest = z;
				distance = d;
			}
		}
		return closest;
	}

	/**
	 * Returns all the squares where the note of the square contains the given string. The search is case insensitive.
	 * @param n the string to search for
	 * @return a list of squares whose note contains the string
	 */
	public List<Point> getPoints(String n) {
		n = n.toLowerCase();
		List<Point> l = new ArrayList<Point>();
		Iterator<String> it = notes.keySet().iterator();
		while (it.hasNext()) {
			String s = it.next();
			if (s.contains(n)) {
				l.addAll(notes.get(s));
			}
		}
		return l;
	}
	
	/**
	 * Returns all the squares within the given distance where the note of the square contains the given string. The search is case insensitive.
	 * @param n the string to search for
	 * @param p the origin of the search
	 * @param distance the distance from the origin to search within
	 * @return a list of squares within the search distance whose note contains the string
	 */
	public List<Point> getPoints(String n, Point p, int distance) {
		List<Point> l = new ArrayList<Point>();
		List<Point> points = getPoints(n);
		Iterator<Point> it = points.iterator();
		while (it.hasNext()) {
			Point z = it.next();
			int d = distance(z, p);
			if (d < distance) {
				l.add(z);
			}
		}
		return l;
	}

	/**
	 * Returns all the squares where the extra of the square equals the extra to search for.
	 * @param n the extra to search for
	 * @return a list of squares whose note contains the string
	 */
	public List<Point> getPoints(Integer n) {
		List<Point> l = new ArrayList<Point>();
		Iterator<Integer> it = extras.keySet().iterator();
		while (it.hasNext()) {
			Integer s = it.next();
			if (s.equals(n)) {
				l.addAll(extras.get(s));
			}
		}
		return l;
	}
	
	/**
	 * Returns all the squares within the given distance where the extra of the square equals the given integer.
	 * @param n the extra to search for
	 * @param p the origin of the search
	 * @param distance the distance from the origin to search within
	 * @return a list of squares within the search distance whose note contains the string
	 */
	public List<Point> getPoints(Integer n, Point p, int distance) {
		List<Point> l = new ArrayList<Point>();
		List<Point> points = getPoints(n);
		Iterator<Point> it = points.iterator();
		while (it.hasNext()) {
			Point z = it.next();
			int d = distance(z, p);
			if (d < distance) {
				l.add(z);
			}
		}
		return l;
	}

	/**
	 * Returns the closest square to the origin whose note contains the string.
	 * @param n the string to search for
	 * @param p the origin of the search
	 * @return the closest square to the origin whose note contains the string
	 */
	public Point getPoint(String n, Point p) {
		Point closest = null;
		int distance = Integer.MAX_VALUE;
		List<Point> points = getPoints(n);
		Iterator<Point> it = points.iterator();
		while (it.hasNext()) {
			Point z = it.next();
			int d = distance(z, p);
			if (d < distance) {
				closest = z;
				distance = d;
			}
		}
		return closest;
	}
		
	/**
	 * Returns the number of squares between the two points. Can't use the Point.distance() method 
	 * because they're not Points but squares.
	 * 
	 * @param p1 the starting square
	 * @param p2 the destination square
	 * @return the number of squares you need to traverse to get from p1 to p2
	 */
	private int distance(Point p1, Point p2) {
		int x = Math.abs(p1.x - p2.x);
		int y = Math.abs(p1.y - p2.y);
		return Math.max(x, y);
	}
	
	/**
	 * Adds a feature/square reference to the index
	 * @param f the feature
	 * @param p the square
	 */
	private void addFeature(Feature f, Point p) {
		List<Point> l = features.get(f);
		if (l == null) {
			l = new ArrayList<Point>();
			features.put(f, l);
		}
		if (!l.contains(p)) {
			l.add(p);
		}
	}
	
	/**
	 * Removes a feature/square reference from the index
	 * @param f the feature
	 * @param p the square
	 */
	private void removeFeature(Feature f, Point p) {
		List<Point> l = features.get(f);
		if (l != null && l.contains(p)) {
			l.remove(p);
		}
	}
	
	/**
	 * Adds a note reference to the index
	 * @param n the note
	 * @param p the square
	 */
	private void addNote(String n, Point p) {
		List<Point> l = notes.get(n);
		if (l == null) {
			l = new ArrayList<Point>();
			notes.put(n, l);
		}
		if (!l.contains(p)) {
			l.add(p);
		}
	}
	
	/**
	 * Removes a note reference from the index
	 * @param n the note
	 * @param p the square
	 */
	private void removeNote(String n, Point p) {
		List<Point> l = notes.get(n);
		if (l != null && l.contains(p)) {
			l.remove(p);
		}
	}


	/**
	 * Find out if this square has extra info update the extra lists accordingly
	 */
	private void checkExtra(Point p) {
		Integer extra = model.getExtra(p);
		if (extra != 0) {
			List<Point> l = extras.get(extra);
			if (l == null) {
				l = new ArrayList<Point>();
				extras.put(extra, l);
			}
		}
		
		Iterator<Map.Entry<Integer, List<Point>>> it = extras.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, List<Point>> entry = it.next();
			if (!entry.getKey().equals(extra)) {
				entry.getValue().remove(p);
			} else if(!entry.getValue().contains(p)) {
				entry.getValue().add(p);
			}
		}
	}
	
	/**
	 * Sorts the points in the index
	 */
	private void sortPoints() {
		PointComparator comp = new PointComparator();
		Iterator<List<Point>> it = features.values().iterator();
		while (it.hasNext()) {
			List<Point> pts = it.next();
			Collections.sort(pts, comp);
		}
	}
	
	/**
	 * A comparator to sort Point objects
	 * @author Ed Webb
	 *
	 */
	class PointComparator implements Comparator<Point> {

		@Override
		public int compare(Point p1, Point p2) {
			if (p1 == null && p2 == null) {
				return 0;
			}
			if (p1 == null) {
				return -1;
			}
			if (p2 == null) {
				return 1;
			}
			int c = p2.x - p1.x;
			if (c == 0) {
				c = p2.y - p2.x;
			}
			return c;
		}
	}

}
