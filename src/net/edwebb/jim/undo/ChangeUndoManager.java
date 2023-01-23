package net.edwebb.jim.undo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import net.edwebb.jim.MapConstants.ChangeType;
import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.events.CoordinateChangeEvent;
import net.edwebb.jim.model.events.FeatureChangeEvent;
import net.edwebb.jim.model.events.FlagChangeEvent;
import net.edwebb.jim.model.events.MapChangeEvent;
import net.edwebb.jim.model.events.MapChangeListener;
import net.edwebb.jim.model.events.NoteChangeEvent;
import net.edwebb.jim.model.events.TerrainChangeEvent;

public class ChangeUndoManager extends UndoManager implements MapChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private MapModel model;
	private boolean ignore;
	
	private List<UndoListener> listeners = new ArrayList<UndoListener>();
	
	public ChangeUndoManager(MapModel model) {
		this.model = model;
	}

	public void addUndoListener(UndoListener l) {
		listeners.add(l);
	}
	
	public void removeUndoListener(UndoListener l) {
		listeners.remove(l);
	}

	@Override
	public synchronized void undo() throws CannotUndoException {
		ignore = true;
		UndoableChange change = (UndoableChange)editToBeUndone();
		super.undo();
		for (UndoListener l : listeners) {
			l.changeMade(change, true);
			l.undoManagerChanged(this);
		}
		ignore = false;
	}

	@Override
	public synchronized void redo() throws CannotRedoException {
		ignore = true;
		UndoableChange change = (UndoableChange)editToBeRedone();
		super.redo();
		for (UndoListener l : listeners) {
			l.changeMade(change, false);
			l.undoManagerChanged(this);
		}
		ignore = false;
	}

	public void setModel(MapModel model) {
		if (this.model != null) {
			this.model.removeMapChangeListener(this);
		}
		discardAllEdits();
		this.model = model;
		model.addMapChangeListener(this);
		for (UndoListener l : listeners) {
			l.undoManagerChanged(this);
		}
	}
	
	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		UndoableEdit nextEdit = editToBeUndone();
		if (nextEdit == null) {
			return false;
		} else {
			edits.set(edits.indexOf(nextEdit), anEdit);
			for (UndoListener l : listeners) {
				l.undoManagerChanged(this);
			}
			return true;
		}
	}
	
	@Override
	public synchronized String getUndoPresentationName() {
		if (canUndo()) {
			return editToBeUndone().getUndoPresentationName();
		}
		return "No change to undo";
	}

	@Override
	public synchronized String getRedoPresentationName() {
		if (canRedo()) {
			return editToBeRedone().getRedoPresentationName();
		}
		return "No change to redo";
	}

	@Override
	public void mapChanged(MapChangeEvent event) {
		if (ignore) {
			return;
		}

		UndoableChange change = null;
		if (event.hasSubEvents()) {
			UndoableCombinedChange combined = new UndoableCombinedChange(getUndoableChange(event).getPresentationName());
			Iterator<MapChangeEvent> it = event.getSubEvents().iterator();
			while (it.hasNext()) {
				MapChangeEvent subEvent = it.next();
				UndoableChange subChange = getUndoableChange(subEvent);
				if (subChange != null) {
					combined.addChange(subChange);
				}
			}
			if (combined.hasChanges()) {
				change = combined;
			}
		} else {
			change = getUndoableChange(event);
		}
		
		if (change != null) {
			this.addEdit(change);
			for (UndoListener l : listeners) {
				l.undoManagerChanged(this);
			}
		}
	}

	private UndoableChange getUndoableChange(MapChangeEvent event) {
		if (event.getChangeType().equals(ChangeType.TERRAIN)) {
			TerrainChangeEvent terrainEvent = (TerrainChangeEvent)event;
			return new UndoableTerrainChange(event.getModel(), terrainEvent.getSquare(), terrainEvent.getOldTerrain(), terrainEvent.getNewTerrain());
		}

		if (event.getChangeType().equals(ChangeType.FEATURE)) {
			FeatureChangeEvent featureEvent = (FeatureChangeEvent)event;
			return new UndoableFeatureChange(event.getModel(), featureEvent.getSquare(), featureEvent.getFeature(), featureEvent.isAdded());
		}

		if (event.getChangeType().equals(ChangeType.NOTE)) {
			NoteChangeEvent noteEvent = (NoteChangeEvent)event;
			return new UndoableNoteChange(event.getModel(), noteEvent.getSquare(), noteEvent.getOldNote(), noteEvent.getNewNote());
		}

		if (event.getChangeType().equals(ChangeType.COORDINATE)) {
			CoordinateChangeEvent coordEvent = (CoordinateChangeEvent)event;
			if (coordEvent.isDefaultCoord()) {
				return new UndoableCoordinateChange(event.getModel(), coordEvent.getOldCoord(), coordEvent.getNewCoord());
			}
		}

		if (event.getChangeType().equals(ChangeType.FLAG)) {
			FlagChangeEvent flagEvent = (FlagChangeEvent)event;
			return new UndoableFlagChange(event.getModel(), flagEvent.getSquare(), flagEvent.getFlag(), flagEvent.isSet());
		}
		
		return null;
	}
	
	public boolean removeNextUndo() {
		UndoableEdit nextEdit = editToBeUndone();
		if (nextEdit == null) {
			return false;
		} else {
			trimEdits(edits.size() - 1, edits.size() - 1);
			for (UndoListener l : listeners) {
				l.undoManagerChanged(this);
			}
			return true;
		}
	}

	public boolean removeNextRedo() {
		UndoableEdit nextEdit = editToBeRedone();
		if (nextEdit == null) {
			return false;
		} else {
			for (UndoListener l : listeners) {
				l.undoManagerChanged(this);
			}
			return edits.remove(nextEdit);
		}
	}
}			
