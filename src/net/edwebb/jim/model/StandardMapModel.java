package net.edwebb.jim.model;

import java.awt.Point;
import java.awt.Rectangle;

import net.edwebb.jim.model.events.CoordinateChangeEvent;
import net.edwebb.jim.model.events.FeatureChangeEvent;
import net.edwebb.jim.model.events.FlagChangeEvent;
import net.edwebb.jim.model.events.MapChangeListener;
import net.edwebb.jim.model.events.NoteChangeEvent;
import net.edwebb.jim.model.events.SelectedChangeEvent;
import net.edwebb.jim.model.events.TerrainChangeEvent;
import net.edwebb.jim.model.events.ViewChangeEvent;
import net.edwebb.mi.data.Coordinate;
import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.data.Decoder;
import net.edwebb.mi.data.Feature;
import net.edwebb.mi.data.Flag;
import net.edwebb.mi.data.Terrain;

public class StandardMapModel extends AbstractMapModel {

	private String name;

	private MapData data;
	
	private MapModel parent;
	
	private int size = 52;
	
	private Coordinate currentCoord;
	private Coordinate defaultCoord;
	private Point offset = new Point(0, 0);
	
	private Rectangle bounds;
	private Rectangle view = new Rectangle(0, 0, 30, 20);
	private Point selected = null;
	
	public StandardMapModel(int size, MapData data, String name) {
		this.name = name;
		this.size = size;
		this.data = data;
		this.currentCoord = new Coordinate(new Point(data.getOffX(), data.getOffY()), name);
		this.defaultCoord = new Coordinate(new Point(data.getOffX(), data.getOffY()), name);
		bounds = new Rectangle(data.getLeft(), data.getTop(), data.getWidth(), data.getHeight());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public MapData getData() {
		return data;
	}
	
	@Override
	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public int getSize() {
		return size;
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
		Rectangle oldView = view;
		view = bound(rect);
		if (!oldView.equals(view)) {
			ViewChangeEvent event = new ViewChangeEvent(this, oldView, view);
			for (MapChangeListener l : mapChangeListeners) {
				l.mapChanged(event);
			}
		}
	}

	@Override
	public Rectangle getView() {
		return view;
	}

	@Override
	public void setSelected(Point square) {
		Point oldSelected = selected;
		selected = bound(square);
		if (oldSelected == null || !oldSelected.equals(selected)) {
			SelectedChangeEvent event = new SelectedChangeEvent(this, oldSelected, selected);
			for (MapChangeListener l : mapChangeListeners) {
				l.mapChanged(event);
			}
		}
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
	public short[] getSquare(Point square) {
		return data.getSquare(square.x - bounds.x, bounds.y - square.y);
	}

	private short[] getNewSquare(Point square) {
		short[] sqr = getSquare(square);
		if (sqr == null || sqr.length == 0) {
			sqr = new short[1];
			setSquare(square, sqr);
		}
		return sqr;
	}
	
	@Override
	public boolean isWithin(Point square) {
		if (square.x < bounds.x || square.x >= bounds.x + bounds.width) {
			return false;
		}
		if (square.y > bounds.y || square.y <= bounds.y - bounds.height) {
			return false;
		}
		return true;
	}
	
	@Override
	public String getSquareNote(Point square) {
		return data.getSquareNotes(square.x - bounds.x, bounds.y - square.y);
	}

	private void setSquare(Point square, short[] features) {
		data.setSquare(square.x - bounds.x, bounds.y - square.y, features);
	}

	@Override
	public void setTerrain(Point square, Terrain terrain) {
		short[] sqr = getNewSquare(square);
		
		short oldid = Decoder.shortLowByte(sqr[0]);
		if (oldid == terrain.getId()) {
			return;
		}
		
		sqr[0] = Decoder.shortFromShorts(sqr[0], terrain.getId());
		
		if (sqr.length == 1 && sqr[0] == 0) {
			setSquare(square, null);
		}

		TerrainChangeEvent event = new TerrainChangeEvent(this, square, DataStore.getInstance().getTerrain(oldid), terrain);
		if (parent != null) {
			parent.recieveMapChangeEvent(event);
		}
		for (MapChangeListener l : mapChangeListeners) {
			l.mapChanged(event);
		}
	}

	@Override
	public void toggleFlag(Point square, Flag flag, int state) {
		short[] sqr = getNewSquare(square);

		short oldFlags = Decoder.shortHighByte(sqr[0]);
		if (state > 0) {
			sqr[0] |= (1 << (flag.getId() + 8));
		} else if (state < 0) {
			sqr[0] &= ~(1 << (flag.getId() + 8));
		} else {
			sqr[0] ^= (1 << (flag.getId() + 8));
		}

		if (sqr.length == 1 && sqr[0] == 0) {
			setSquare(square, null);
		}
		
		if (oldFlags == Decoder.shortHighByte(sqr[0])) {
			return;
		}
		
		FlagChangeEvent event = new FlagChangeEvent(this, square, flag, true); // TODO work out the state
		if (parent != null) {
			parent.recieveMapChangeEvent(event);
		}
		for (MapChangeListener l : mapChangeListeners) {
			l.mapChanged(event);
		}
	}

	@Override
	public boolean isFlagged(Point square, Flag flag) {
		short[] sqr = getSquare(square);
		if (sqr == null || sqr.length == 0) {
			return false;
		}
		return (sqr[0] & (1 << (flag.getId() + 8))) > 0;
	}
	
	@Override
	public void remove(Point square, Feature feature) {
		if (!contains(square, feature)) {
			return;
		}
		short[] sqr = getSquare(square);
		short[] newSqr = new short[sqr.length-1];
		int j = 0;
		for (int i = 0; i < sqr.length; i++) {
			if (sqr[i] != feature.getId()) {
				newSqr[j++] = sqr[i];
			}
		}
		if (newSqr.length == 1 && newSqr[0] == 0) {
			setSquare(square, null);
		} else {
			setSquare(square, newSqr);
		}
		FeatureChangeEvent event = new FeatureChangeEvent(this, square, feature, false);
		if (parent != null) {
			parent.recieveMapChangeEvent(event);
		}
		for (MapChangeListener l : mapChangeListeners) {
			l.mapChanged(event);
		}
	}

	@Override
	public void add(Point square, Feature feature) {
		if (contains(square, feature)) {
			return;
		}
		short[] sqr = getSquare(square);
		if (sqr == null) {
			sqr = new short[1];
			setSquare(square, sqr);
		}
		short[] newSqr = new short[sqr.length+1];
		int i = 0;
		for (; i < sqr.length; i++) {
			newSqr[i] = sqr[i];
		}
		newSqr[i] = feature.getId();
		setSquare(square, newSqr);
		
		FeatureChangeEvent event = new FeatureChangeEvent(this, square, feature, true);
		if (parent != null) {
			parent.recieveMapChangeEvent(event);
		}
		for (MapChangeListener l : mapChangeListeners) {
			l.mapChanged(event);
		}
	}
	
	@Override
	public boolean contains(Point square, Feature feature) {
		short[] sqr = getSquare(square);
		if (sqr == null || sqr.length == 0) {
			return false;
		}
		
		if (feature instanceof Terrain) {
			return feature.getId() == Decoder.shortLowByte(sqr[0]);
		}

		if (feature instanceof Flag) {
			return isFlagged(square, (Flag)feature);
		}
		for (int i = 1; i < sqr.length; i++) {
			if (sqr[i] == feature.getId()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setSquareNote(Point square, String note) {
		String oldNote = data.getSquareNotes(square.x - bounds.x, bounds.y - square.y);
		if ((oldNote == null && note == null) || (oldNote != null && oldNote.equals(note))) {
			return;
		}
		
		data.setSquareNotes(square.x - bounds.x, bounds.y - square.y, note);
		
		NoteChangeEvent event = new NoteChangeEvent(this, square, oldNote, note);
		if (parent != null) {
			parent.recieveMapChangeEvent(event);
		}
		for (MapChangeListener l : mapChangeListeners) {
			l.mapChanged(event);
		}
	}

	@Override
	public void setCurrentCoOrdinates(Coordinate coord) {
		Coordinate oldCoord = this.currentCoord;
		if ((oldCoord == null && coord == null) || (oldCoord != null && oldCoord.equals(coord))) {
			return;
		}
		this.currentCoord = coord;
		if (oldCoord != null && !oldCoord.getOffset().equals(new Point(0,0))) {
			updateOffset();
		}

		CoordinateChangeEvent event = new CoordinateChangeEvent(this, oldCoord, coord, false);
		if (parent != null) {
			parent.recieveMapChangeEvent(event);
		}
		for (MapChangeListener l : mapChangeListeners) {
			l.mapChanged(event);
		}
	}

	@Override
	public Coordinate getCurrentCoOrdinates() {
		return currentCoord;
	}
	
	@Override
	public void setDefaultCoOrdinates(Coordinate coord) {
		Coordinate oldCoord = this.defaultCoord;
		if ((oldCoord == null && coord == null) || (oldCoord != null && oldCoord.equals(coord))) {
			return;
		}
		this.defaultCoord = coord;
		if (oldCoord != null && !oldCoord.getOffset().equals(new Point(0,0))) {
			updateOffset();
		}

		CoordinateChangeEvent event = new CoordinateChangeEvent(this, oldCoord, coord, true);
		if (parent != null) {
			parent.recieveMapChangeEvent(event);
		}
		for (MapChangeListener l : mapChangeListeners) {
			l.mapChanged(event);
		}
	}

	@Override
	public Coordinate getDefaultCoOrdinates() {
		return defaultCoord;
	}

	@Override
	public Point getOffset() {
		return offset;
	}
	
	private void updateOffset() {
		if (defaultCoord != null && currentCoord != null) {
			this.offset = defaultCoord.getOffset(currentCoord);
		} else {
			this.offset = new Point(0, 0);
		}
	}
	
	@Override
	public boolean isDirty() {
		return data.isDirty();
	}

	@Override
	public void setParent(MapModel model) {
		this.parent = model;
	}

	@Override
	public MapModel getParent() {
		return parent;
	}

	/**
	 * Ensures that the point is within the bounds of the current MapModel. If either co-ordinate is beyond the bounds of the 
	 * MapModel then the point is adjusted to the closest point within the bounds.
	 * @param p the Point to check and adjust if necessary
	 * @return a point that is within the bounds of the MapModel
	 */
	private Point bound(Point p) {
		Rectangle bounds = getBounds();
		int x = Math.min(Math.max(p.x, bounds.x), bounds.width + bounds.x);
		int y = Math.max(Math.min(p.y, bounds.y), bounds.y - bounds.height);
		if (p.x != x || p.y != y) {
			return new Point(x, y);
		}
		return p;
	}
	
	/**
	 * Ensures that the rectangle is within the bounds of the current MapModel. If any side is beyond the bounds of the 
	 * MapModel then the rectangle is adjusted to the closest rectangle within the bounds. This method does not adjust the width
	 * and height of the rectangle only the x and y co-ordinates
	 * @param rect the Point to check and adjust if necessary
	 * @return a rectangle that is within the bounds of the MapModel
	 */
	private Rectangle bound(Rectangle rect) {
		Rectangle bounds = getBounds();
		int x = 0;
		int y = 0;
		
		// Check not beyond top/left
		boolean changed = false;
		if (rect.x < bounds.x) {
			x = bounds.x;
			changed = true;
		} else {
			x = rect.x;
		}
		if (rect.y > bounds.y) {
			y = bounds.y;
			changed = true;
		} else {
			y = rect.y;
		}
		if (changed) {
			return new Rectangle(x, y,  rect.width, rect.height);
		}
		
		// Check not beyond bottom/right
		x = (bounds.x + bounds.width) - (rect.x + rect.width) + 2;
		y = (bounds.y - bounds.height) - (rect.y - rect.height) - 1;
		if (x > 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}
		if (x < 0 || y > 0) {
			return new Rectangle(rect.x + x, rect.y + y, rect.width, rect.height);
		}

		return rect;
	}
	
	
	public String toString() {
		return "Standard " + bounds.x + ", " + bounds.y + " " + bounds.width + "x" + bounds.height;
	}
}
