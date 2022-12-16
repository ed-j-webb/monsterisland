package net.edwebb.jim.model;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import net.edwebb.jim.data.MapData;
import net.edwebb.mi.data.Coordinate;

public class DiffMapModel implements MapModel {

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

	public DiffMapModel(MapModel primary, MapModel secondary) {
		if (primary == null || secondary == null) {
			throw new IllegalArgumentException("Neither Map Model can be null");
		}
		this.primary = primary;
		this.secondary = secondary;
		secondary.setDefaultCoOrdinates(primary.getDefaultCoOrdinates());
		
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
				if (getExtra(p, Short.MIN_VALUE) != 0) {
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
		
		if (p == null && s == null) {
			return null;
		} else if (p == null) {
			short[] x; 
			if (s[0] == 0) {
				return s; 
			} else {
				x = new short[s.length + 1];
				x[0] = 0;
				System.arraycopy(s, 0, x, 1, s.length);
				return x;
			}
		} else if (s == null) {
			return p;
		}
		
		short[] x = new short[s.length];
		System.arraycopy(s, 0, x, 0, s.length);
		
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
	public UndoableChange setTerrain(Point square, short id) {
		UndoableCombinedChange change = new UndoableCombinedChange();
		change.addChange(primary.setTerrain(square, id));
		change.addChange(secondary.setTerrain(square, id));
		return change.hasChanges() ? change : null;
	}
	
	@Override
	public boolean isFlagged(Point square, short id) {
		boolean p = primary.isFlagged(square, id);
		boolean s = secondary.isFlagged(square, id);
		return p || s;
	}

	@Override
	public UndoableChange toggleFlag(Point square, short id, int state) {
		UndoableCombinedChange change = new UndoableCombinedChange();
		if (state == INVERSE) {
			if (primary.isFlagged(square, id) || secondary.isFlagged(square, id)) {
				return toggleFlag(square, id, OFF);
			} else {
				return toggleFlag(square, id, ON);
			}
		} else {
			change.addChange(primary.toggleFlag(square, id, state));
			change.addChange(secondary.toggleFlag(square, id, state));
			return change.hasChanges() ? change : null;
		}
	}

	@Override
	public UndoableChange remove(Point square, short id) {
		UndoableCombinedChange change = new UndoableCombinedChange();
		change.addChange(primary.remove(square, id));
		change.addChange(secondary.remove(square, id));
		return change.hasChanges() ? change : null;
	}

	@Override
	public UndoableChange add(Point square, short id) {
		UndoableCombinedChange change = new UndoableCombinedChange();
		change.addChange(primary.isWithin(square) ? primary.add(square, id) : null);
		change.addChange(secondary.isWithin(square) ? secondary.add(square, id) : null);
		return change.hasChanges() ? change : null;
	}

	@Override
	public boolean contains(Point square, short id) {
		boolean p = primary.contains(square, id);
		boolean s = secondary.contains(square, id);
		return p || s;
	}

	@Override
	public UndoableChange setSquareNote(Point square, String note) {
		UndoableCombinedChange change = new UndoableCombinedChange();
		change.addChange(primary.setSquareNote(square, note));
		change.addChange(secondary.setSquareNote(square, note));
		return change.hasChanges() ? change : null;
	}

	@Override
	public UndoableChange setDefaultCoOrdinates(Coordinate coord) {
		UndoableCombinedChange change = new UndoableCombinedChange();
		change.addChange(primary.setDefaultCoOrdinates(coord));
		change.addChange(secondary.setDefaultCoOrdinates(coord));
		return change.hasChanges() ? change : null;
	}

	@Override
	public Coordinate getDefaultCoOrdinates() {
		return primary.getDefaultCoOrdinates();
	}

	@Override
	public UndoableChange setCurrentCoOrdinates(Coordinate coord) {
		UndoableCombinedChange change = new UndoableCombinedChange();
		change.addChange(primary.setCurrentCoOrdinates(coord));
		change.addChange(secondary.setCurrentCoOrdinates(coord));
		return change.hasChanges() ? change : null;
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
	public int getExtra(Point square, short id) {
		if (id == Short.MIN_VALUE) {
			return areEqual(square);
		} else if (id < 10 && id >= 0) {
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
		} else {
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
	private int areEqual(Point square) {
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

	public String toString() {
		return "Diff " + bounds.x + ", " + bounds.y + " " + bounds.width + "x" + bounds.height + " Primary:" + primary.toString() + ", Secondary: " + secondary.toString() ;
	}
}
