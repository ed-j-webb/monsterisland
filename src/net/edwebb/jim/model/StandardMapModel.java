package net.edwebb.jim.model;

import java.awt.Point;
import java.awt.Rectangle;

import net.edwebb.jim.model.events.CoordinateChangeEvent;
import net.edwebb.jim.model.events.FeatureChangeEvent;
import net.edwebb.jim.model.events.FlagChangeEvent;
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
	
	private int size = 52;
	
	private Coordinate currentCoord;
	private Coordinate defaultCoord;
	private Point offset = new Point(0, 0);
	
	private Rectangle bounds;
	private Rectangle used;
	
	private Rectangle view = new Rectangle(0, 0, 30, 20);
	private Point selected = null;
	
	public StandardMapModel(int size, MapData data, String name) {
		this.name = name;
		this.size = size;
		this.data = data;	
		this.currentCoord = new Coordinate(new Point(data.getOffX(), data.getOffY()), data.getOffset());
		this.defaultCoord = new Coordinate(new Point(data.getOffX(), data.getOffY()), data.getOffset());
		bounds = new Rectangle(data.getLeft(), data.getTop(), data.getWidth(), data.getHeight());
		used = new Rectangle(data.getLeft() + data.getWest(), data.getTop() - data.getNorth(), data.getEast() - data.getWest(), data.getSouth() - data.getNorth());
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
	public Rectangle getUsed() {
		return used;
	}

	@Override
	public void setView(Rectangle rect) {
		Rectangle oldView = view;
		view = bound(rect);
		if (!oldView.equals(view)) {
			sendEvent(new ViewChangeEvent(this, oldView, view));
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
			sendEvent(new SelectedChangeEvent(this, oldSelected, selected));
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

		sendEvent(new TerrainChangeEvent(this, square, DataStore.getInstance().getTerrain(oldid), terrain));
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
		
		sendEvent(new FlagChangeEvent(this, square, flag, true)); // TODO work out the state
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

		sendEvent(new FeatureChangeEvent(this, square, feature, false));
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
		
		sendEvent(new FeatureChangeEvent(this, square, feature, true));
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
		
		sendEvent(new NoteChangeEvent(this, square, oldNote, note));
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

		sendEvent(new CoordinateChangeEvent(this, oldCoord, coord, false));
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
		if (coord != null) {
			data.setOffset(coord.getOffset().x, coord.getOffset().y, coord.getName());
		}

		sendEvent(new CoordinateChangeEvent(this, oldCoord, coord, true));
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
		if (defaultCoord != null && currentCoord != null && !currentCoord.getOffset().equals(new Point(0, 0))) {
			this.offset = defaultCoord.getOffset(currentCoord);
		} else {
			this.offset = new Point(0, 0);
		}
	}
	
	@Override
	public boolean isDirty() {
		return data.isDirty();
	}

	public String toString() {
		return "Standard " + bounds.x + ", " + bounds.y + " " + bounds.width + "x" + bounds.height;
	}
}
