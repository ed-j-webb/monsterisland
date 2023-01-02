package net.edwebb.jim.model;

import java.awt.Point;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.edwebb.mi.data.Coordinate;
import net.edwebb.mi.data.Feature;
import net.edwebb.mi.data.Flag;
import net.edwebb.mi.data.Terrain;

public class Undo {

	
	public class UndoableTerrainChange extends UndoableMapChange {
		
		private Terrain oldTerrain;
		private Terrain newTerrain;
		
		public UndoableTerrainChange(MapModel model, Point pos, Terrain oldTerrain, Terrain newTerrain) {
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

		private Feature feature;
		private boolean added;
		
		public UndoableFeatureChange(MapModel model, Point pos, Feature feature, boolean added) {
			super(model, pos);
			this.feature = feature;
			this.added = added;
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			if (added) {
				model.add(pos, feature);
			} else {
				model.remove(pos, feature);
			}
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			if (added) {
				model.remove(pos, feature);
			} else {
				model.add(pos, feature);
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

		private Flag flag;
		private int state;
		
		public UndoableFlagChange(MapModel model, Point pos, Flag flag, int state) {
			super(model, pos);
			this.flag = flag;
			this.state = state;
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			model.toggleFlag(pos, flag, state);
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			model.toggleFlag(pos, flag, -state);
		}
	}
	
}
