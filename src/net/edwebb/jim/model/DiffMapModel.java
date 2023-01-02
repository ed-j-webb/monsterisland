package net.edwebb.jim.model;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.edwebb.jim.model.events.CoordinateChangeEvent;
import net.edwebb.jim.model.events.FeatureChangeEvent;
import net.edwebb.jim.model.events.FlagChangeEvent;
import net.edwebb.jim.model.events.MapChangeEvent;
import net.edwebb.jim.model.events.NoteChangeEvent;
import net.edwebb.jim.model.events.TerrainChangeEvent;
import net.edwebb.mi.data.Coordinate;
import net.edwebb.mi.data.Feature;
import net.edwebb.mi.data.Flag;
import net.edwebb.mi.data.Terrain;

public class DiffMapModel extends AbstractMapModel {

	public static final int SQUARE_PRIMARY_ONLY = 0;
	public static final int SQUARE_NEW_OR_MATCH = 1;
	public static final int SQUARE_DIFFERENCES = 2;
	
	public static final int FEATURE_ONE_OR_MATCH = 0;
	public static final int FEATURE_PRIMARY_ONLY = 1;
	public static final int FEATURE_SECONDARY_ONLY = 2;
	
	private MapModel primary;
	private MapModel secondary;
	
	private Rectangle bounds;
	
	private Rectangle view = new Rectangle(0, 0, 30, 20);
	private Point selected = new Point(0, 0);

	protected List<TerrainChangeEvent> terrainEvents = new ArrayList<TerrainChangeEvent>();
	protected List<FeatureChangeEvent> featureEvents = new ArrayList<FeatureChangeEvent>();
	protected List<FlagChangeEvent> flagEvents = new ArrayList<FlagChangeEvent>();
	protected List<NoteChangeEvent> noteEvents = new ArrayList<NoteChangeEvent>();
	protected List<CoordinateChangeEvent> coordEvents = new ArrayList<CoordinateChangeEvent>();
	
	public DiffMapModel(MapModel primary, MapModel secondary) {
		if (primary == null || secondary == null) {
			throw new IllegalArgumentException("Neither Map Model can be null");
		}
		this.primary = primary;
		primary.setParent(this);
		this.secondary = secondary;
		secondary.setDefaultCoOrdinates(primary.getDefaultCoOrdinates());
		secondary.setParent(this);
		
		Point topLeft = new Point(Math.min(primary.getBounds().x, secondary.getBounds().y), Math.max(primary.getBounds().y, secondary.getBounds().y));
		Point bottomRight = new Point(Math.max(primary.getBounds().x + primary.getBounds().width, secondary.getBounds().x + secondary.getBounds().width), Math.min(primary.getBounds().y - primary.getBounds().height, secondary.getBounds().y - secondary.getBounds().height));
		Dimension widthHeight = new Dimension(bottomRight.x - topLeft.x + 1, topLeft.y - bottomRight.y + 1);  
		bounds = new Rectangle(topLeft, widthHeight);
		
	}
	
	@Override
	public String getName() {
		return primary.getName() + " < " + secondary.getName();
	}

	@Override
	public void setName(String name) {
		// Do nothing
	}

	@Override
	public MapData getData() {
		return primary.getData();
	}
	
	public MapData getDifferences() {
		MapData diff = new MapData(bounds.y, bounds.x, bounds.width, bounds.height);
		Point p = new Point(0,0);
		for (int i = getBounds().x; i < getBounds().x + getBounds().width; i++) {
			for (int j = getBounds().y; j > getBounds().y - getBounds().height; j--) {
				p.move(i, j);
				if (getExtra(p) != 0) {
					diff.setSquare(i - bounds.x, bounds.y - j, secondary.getSquare(p));
				}
			}
		}
		return diff;
	}
	
	public MapData getMerged() {
		MapData merge = new MapData(bounds.y, bounds.x, bounds.width, bounds.height);
		Point p = new Point(0,0);
		for (int i = getBounds().x; i < getBounds().x + getBounds().width; i++) {
			for (int j = getBounds().y; j > getBounds().y - getBounds().height; j--) {
				p.move(i, j);
				if (i == 231 && j == -135) {
					System.out.println();
				}
				// If there is the terrain from the secondary map in the square's data then
				// remove it.
				short[] combined = getSquare(p);
				if (combined != null) {
					short[] second = secondary.getSquare(p);
					if (second != null) {
						for (int k = 1; k < combined.length; k++) {
							if (combined[k] == second[0]) {
								short[] exterrain = new short[combined.length - 1];
								System.arraycopy(combined, 0, exterrain, 0, k);
								if (k + 1 < combined.length) {
									System.arraycopy(combined, k + 1, exterrain, k, combined.length - k - 1);
								}
								// If the primary terrain is unknown then plug in the secondary terrain we've just cut out
								if (combined[0] == 0) {
									exterrain[0] = combined[k];
								}
		
								combined = exterrain;
								break;
							}
						}
					}
				}
				merge.setSquare(i - bounds.x, bounds.y - j, combined);
				merge.setSquareNotes(i - bounds.x, bounds.y - j, getSquareNote(p));
			}
		}
		return merge;
	}

	@Override
	public void setSize(int size) {
		primary.setSize(size);
		secondary.setSize(size);
	}

	@Override
	public int getSize() {
		return primary.getSize();
	}

	@Override
	public void setBounds(Rectangle rect) {
		throw new UnsupportedOperationException("Cannot set the bounds of this Model"); 
	}

	@Override
	public Rectangle getBounds() {
		return bounds;
	}

	@Override
	public void setView(Rectangle rect) {
		this.view = rect;
	}

	@Override
	public Rectangle getView() {
		return view;
	}

	@Override
	public void setSelected(Point square) {
		this.selected = square;
	}

	@Override
	public Point getSelected() {
		return selected;
	}

	@Override
	public boolean isSelected(Point square) {
		if (selected == null) {
			return false;
		}
		return selected.equals(square);
	}

	@Override
	public boolean isWithin(Point square) {
		return bounds.contains(square);
	}
	
	@Override
	public short[] getSquare(Point square) {
		short[] p = primary.getSquare(square);
		short[] s = secondary.getSquare(square);
		
		if ((p == null || p.length == 0) && (s == null || s.length == 0)) {
			return null;
		} else if (p == null || p.length == 0) {
			short[] x; 
			if (s[0] == 0) {
				return s; 
			} else {
				x = new short[s.length + 1];
				x[0] = 0;
				System.arraycopy(s, 0, x, 1, s.length);
				return x;
			}
		} else if (s == null || s.length == 0) {
			return p;
		}
		
		short[] x = new short[s.length];
		System.arraycopy(s, 0, x, 0, s.length);
		
		// Blank out the flags from the secondary terrain otherwise it may look like a feature. 
		x[0] = (short)(x[0] & 0xff);
		
		int k = s.length;
		if (x[0] == 0 || x[0] == p[0]) {
			x[0] = 0;
			k--;
		}
		for (int i = 1; i < x.length; i++) {
			for (int j = 1; j < p.length; j++) {
				if (p[j] == x[i]) {
					x[i] = 0;
					k--;
					break;
				}
			}
		}
		
		short[] z = new short[p.length + k];
		
		System.arraycopy(p, 0, z, 0, p.length);
		
		k = p.length;
		for (int i = 1; i < s.length; i++) {
			if (x[i] != 0) {
				z[k] = x[i];
				k++;
			}
		}
		if (x[0] != 0) {
			z[k] = x[0];
		}
		return z;
	}

	@Override
	public String getSquareNote(Point square) {
		String p = primary.getSquareNote(square);
		String s = secondary.getSquareNote(square);
		
		if (p == null) {
			return s;
		}
		if (s == null) {
			return p;
		}
		
		if (p.equals(s)) {
			return p;
		} else {
			return p + "\n" + s;
		}
	}

	
	@Override
	public void setTerrain(Point square, Terrain terrain) {
		primary.setTerrain(square, terrain);
		secondary.setTerrain(square, terrain);
	}
	
	@Override
	public boolean isFlagged(Point square, Flag flag) {
		boolean p = primary.isFlagged(square, flag);
		boolean s = secondary.isFlagged(square, flag);
		return p || s;
	}

	@Override
	public void toggleFlag(Point square, Flag flag, int state) {
		if (state == INVERSE) {
			if (primary.isFlagged(square, flag) || secondary.isFlagged(square, flag)) {
				toggleFlag(square, flag, OFF);
			} else {
				toggleFlag(square, flag, ON);
			}
		} else {
			primary.toggleFlag(square, flag, state);
			secondary.toggleFlag(square, flag, state);
		}
	}

	@Override
	public void remove(Point square, Feature feature) {
		primary.remove(square, feature);
		secondary.remove(square, feature);
	}

	@Override
	public void add(Point square, Feature feature) {
		if (primary.isWithin(square)) {
			primary.add(square, feature);
		}
		if (secondary.isWithin(square)) {
			secondary.add(square, feature);
		}
	}

	@Override
	public boolean contains(Point square, Feature feature) {
		boolean p = primary.contains(square, feature);
		boolean s = secondary.contains(square, feature);
		return p || s;
	}

	@Override
	public void setSquareNote(Point square, String note) {
		primary.setSquareNote(square, note);
		secondary.setSquareNote(square, note);
	}

	@Override
	public void setDefaultCoOrdinates(Coordinate coord) {
		primary.setDefaultCoOrdinates(coord);
		secondary.setDefaultCoOrdinates(coord);
	}

	@Override
	public Coordinate getDefaultCoOrdinates() {
		return primary.getDefaultCoOrdinates();
	}

	@Override
	public void setCurrentCoOrdinates(Coordinate coord) {
		primary.setCurrentCoOrdinates(coord);
		secondary.setCurrentCoOrdinates(coord);
	}

	@Override
	public Coordinate getCurrentCoOrdinates() {
		return primary.getCurrentCoOrdinates();
	}

	@Override
	public Point getOffset() {
		return primary.getOffset();
	}

	@Override
	public boolean isDirty() {
		return primary.isDirty();
	}
	
	@Override
	public int getExtra(Point square, Flag id) {
		boolean p = primary.isFlagged(square, id);
		boolean s = secondary.isFlagged(square, id);
		if ((p && s) || (!p && !s)) {
			return FEATURE_ONE_OR_MATCH;
		} else if (p) {
			if (isEmpty(secondary, square)) {
				return FEATURE_ONE_OR_MATCH;
			} else {
				return FEATURE_PRIMARY_ONLY;
			}
		} else {
			return FEATURE_SECONDARY_ONLY;
		}
	}
	
	@Override
	public int getExtra(Point square, Feature id) {
		boolean p = primary.contains(square, id);
		boolean s = secondary.contains(square, id);
		if ((p && s) || (!p && !s)) {
			return FEATURE_ONE_OR_MATCH;
		} else if (p) {
			if (isEmpty(secondary, square)) {
				return FEATURE_ONE_OR_MATCH;
			} else {
				return FEATURE_PRIMARY_ONLY;
			}
		} else {
			return FEATURE_SECONDARY_ONLY;
		}
	}

	/**
	 * Work out if the primary and secondary square have conflicts. 
	 * If both are empty return 0. - No new info
	 * If secondary is empty by primary isn't return 0. - No new info
	 * If primary is empty by secondary isn't return 1. - Adding new Data to Primary
	 * If there are more features in primary return 1. - removing stale data from Primary
	 * If the squares are the same return 2 - Squares match
	 * @param square the square to compare
	 * @return
	 */
	@Override
	public int getExtra(Point square) {
		short[] p = primary.getSquare(square);
		short[] s = secondary.getSquare(square);
		
		if (isEmpty(p) && isEmpty(s)) {
			return SQUARE_PRIMARY_ONLY;
		} else if (isEmpty(p)) {
			return SQUARE_NEW_OR_MATCH;
		} else if (isEmpty(s)) {
			return SQUARE_PRIMARY_ONLY; 
		} else {
		
			int k = s.length;
			if (s[0] == 0 || s[0] == p[0]) {
				k--;
			}
			for (int i = 1; i < s.length; i++) {
				for (int j = 1; j < p.length; j++) {
					if (p[j] == s[i]) {
						k--;
						break;
					}
				}
			}
			if (k > 0) {
				return SQUARE_DIFFERENCES;
			} else if (p.length > s.length) {
				return SQUARE_DIFFERENCES;
			} else {
				return SQUARE_NEW_OR_MATCH; 
			}
		}
	}
	
	private boolean isEmpty(MapModel model, Point square) {
		return isEmpty(model.getSquare(square));
	}
	
	private boolean isEmpty(short[] m) {
		return (m == null || m.length == 0);
	}
	
	@Override
	public void recieveMapChangeEvent(MapChangeEvent event) {
		//TODO deal with incomming events
	}

	public String toString() {
		return "Diff " + bounds.x + ", " + bounds.y + " " + bounds.width + "x" + bounds.height + " Primary:" + primary.toString() + ", Secondary: " + secondary.toString() ;
	}
}
