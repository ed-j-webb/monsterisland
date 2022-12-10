package net.edwebb.jim.model;

import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.edwebb.jim.data.Coordinate;
import net.edwebb.jim.data.Decoder;
import net.edwebb.jim.data.MapData;

public class StandardMapModel implements MapModel {

	private String name;
	
	private MapData data;
	
	private int size = 52;
	
	private Coordinate coord;
	private Point offset = new Point(0, 0);
	
	private Rectangle bounds;
	private Rectangle view = new Rectangle(0, 0, 30, 20);
	private Point selected = new Point(0, 0);
	
	public StandardMapModel(int size, MapData data, String name) {
		this.name = name;
		this.size = size;
		this.data = data;
		this.coord = data.getCoord();
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
	public short[] getSquare(Point square) {
		return data.getSquare(square.x - bounds.x, bounds.y - square.y);
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
	public UndoableChange setTerrain(Point square, short id) {
		short[] sqr = getSquare(square);
		if (sqr == null || sqr.length == 0) {
			sqr = new short[1];
			setSquare(square, sqr);
		}
		short oldTerrain = Decoder.shortLowByte(sqr[0]);
		sqr[0] = Decoder.shortFromShorts(sqr[0], id);
		
		if (sqr.length == 1 && sqr[0] == 0) {
			setSquare(square, null);
		}
		
		if (oldTerrain == id) {
			return null;
		} else {
			return new UndoableTerrainChange(this, square, oldTerrain, id);
		}
	}

	@Override
	public UndoableChange toggleFlag(Point square, short id, int state) {
		short[] sqr = getSquare(square);
		if (sqr == null || sqr.length == 0) {
			sqr = new short[1];
			setSquare(square, sqr);
		}
		short oldFlags = Decoder.shortHighByte(sqr[0]);
		if (state > 0) {
			sqr[0] |= (1 << (id + 8));
		} else if (state < 0) {
			sqr[0] &= ~(1 << (id + 8));
		} else {
			sqr[0] ^= (1 << (id + 8));
		}

		if (sqr.length == 1 && sqr[0] == 0) {
			setSquare(square, null);
		}
		
		if (oldFlags == Decoder.shortHighByte(sqr[0])) {
			return null;
		} else {
			return new UndoableFlagChange(this, square, id, state);
		}
	}

	@Override
	public boolean isFlagged(Point square, short id) {
		short[] sqr = getSquare(square);
		if (sqr == null || sqr.length == 0) {
			return false;
		}
		return (sqr[0] & (1 << (id + 8))) > 0;
	}
	
	@Override
	public UndoableChange remove(Point square, short id) {
		if (!contains(square, id)) {
			return null;
		}
		short[] sqr = getSquare(square);
		short[] newSqr = new short[sqr.length-1];
		int j = 0;
		for (int i = 0; i < sqr.length; i++) {
			if (sqr[i] != id) {
				newSqr[j++] = sqr[i];
			}
		}
		if (newSqr.length == 1 && newSqr[0] == 0) {
			setSquare(square, null);
		} else {
			setSquare(square, newSqr);
		}
		return new UndoableFeatureChange(this, square, id, false);
	}

	@Override
	public UndoableChange add(Point square, short id) {
		if (contains(square, id)) {
			return null;
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
		newSqr[i] = id;
		setSquare(square, newSqr);
		return new UndoableFeatureChange(this, square, id, true);
	}
	
	@Override
	public boolean contains(Point square, short id) {
		short[] sqr = getSquare(square);
		if (sqr == null || sqr.length == 0) {
			return false;
		}
		for (int i = 0; i < sqr.length; i++) {
			if (sqr[i] == id) {
				return true;
			}
		}
		return false;
	}

	@Override
	public UndoableChange setSquareNote(Point square, String note) {
		String oldNote = data.getSquareNotes(square.x - bounds.x, bounds.y - square.y);
		data.setSquareNotes(square.x - bounds.x, bounds.y - square.y, note);
		if (oldNote != null && oldNote.equals(note)) {
			return null;
		} else {
			return new UndoableNoteChange(this, square, oldNote, note);
		}
	}

	@Override
	public UndoableChange setCurrentCoOrdinates(Coordinate coord) {
		Coordinate oldCoord = data.getCoord();
		this.coord = coord;
		updateOffset();
		if (oldCoord != null && oldCoord.equals(coord)) {
			return null;
		} else {
			return new UndoableCoordChange(this, oldCoord, coord, true);
		}
	}

	@Override
	public Coordinate getCurrentCoOrdinates() {
		return coord;
	}
	
	@Override
	public UndoableChange setDefaultCoOrdinates(Coordinate coord) {
		Coordinate oldCoord = data.getCoord();
		data.setCoord(coord);
		updateOffset();
		if (oldCoord != null && oldCoord.equals(coord)) {
			return null;
		} else {
			return new UndoableCoordChange(this, oldCoord, coord, false);
		}
	}

	@Override
	public Coordinate getDefaultCoOrdinates() {
		return data.getCoord();
	}

	public Point getOffset() {
		return offset;
	}
	
	private void updateOffset() {
		if (data.getCoord() != null && coord != null) {
			this.offset = data.getCoord().getOffset(coord);
		} else {
			this.offset = new Point(0, 0);
		}
	}
	
	public boolean isDirty() {
		return data.isDirty();
	}
	
	public int getExtra(Point square, short id) {
		return 0;
	}
	
	public String toString() {
		return "Standard " + bounds.x + ", " + bounds.y + " " + bounds.width + "x" + bounds.height;
	}
	
	public class UndoableTerrainChange extends UndoableMapChange {
		
		private short oldTerrain;
		private short newTerrain;
		
		public UndoableTerrainChange(MapModel model, Point pos, short oldTerrain, short newTerrain) {
			super(model, pos);
			this.oldTerrain = oldTerrain;
			this.newTerrain = newTerrain;
		}
		
		public void undo() throws CannotUndoException {
			super.undo();
			model.setTerrain(pos, oldTerrain);
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			model.setTerrain(pos, newTerrain);
		}
		
	}
	
	public class UndoableFeatureChange extends UndoableMapChange {

		private short id;
		private boolean added;
		
		public UndoableFeatureChange(MapModel model, Point pos, short id, boolean added) {
			super(model, pos);
			this.id = id;
			this.added = added;
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			if (added) {
				model.add(pos, id);
			} else {
				model.remove(pos, id);
			}
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			if (added) {
				model.remove(pos, id);
			} else {
				model.add(pos, id);
			}
		}
	}
	
	public class UndoableNoteChange extends UndoableMapChange {

		private String oldNote;
		private String newNote;
		
		public UndoableNoteChange(MapModel model, Point pos, String oldNote, String newNote) {
			super(model, pos);
			this.oldNote = oldNote;
			this.newNote = newNote;
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			model.setSquareNote(pos, newNote);
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			model.setSquareNote(pos, oldNote);
		}
	}
	
	public class UndoableCoordChange extends UndoableMapChange {

		private Coordinate oldCoord;
		private Coordinate newCoord;
		private boolean current;
		
		public UndoableCoordChange(MapModel model, Coordinate oldCoord, Coordinate newCoord, boolean current) {
			super(model, null);
			this.oldCoord = oldCoord;
			this.newCoord = newCoord;
			this.current = current;
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			if (current) {
				model.setCurrentCoOrdinates(newCoord);
			} else {
				model.setDefaultCoOrdinates(newCoord);
			}
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			if (current) {
				model.setCurrentCoOrdinates(oldCoord);
			} else {
				model.setDefaultCoOrdinates(oldCoord);
			}
		}
	}

	public class UndoableFlagChange extends UndoableMapChange {

		private short id;
		private int state;
		
		public UndoableFlagChange(MapModel model, Point pos, short id, int state) {
			super(model, pos);
			this.id = id;
			this.state = state;
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			model.toggleFlag(pos, id, state);
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			model.toggleFlag(pos, id, -state);
		}
	}

}
