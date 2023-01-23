package net.edwebb.jim.undo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.edwebb.jim.MapConstants.ChangeType;

public class UndoableCombinedChange extends UndoableChange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<UndoableChange> undoableChanges;
	
	private String presentationName = "";
	
	public UndoableCombinedChange() {
		this("An unknown change");
	}

	public UndoableCombinedChange(String presentationName) {
		this(presentationName, null);
	}

	public UndoableCombinedChange(List<? extends UndoableChange> changes) {
		this("An Unknown Change", changes);
	}

	public UndoableCombinedChange(String presentationName, List<? extends UndoableChange> changes) {
		super(ChangeType.COMBINED);
		this.presentationName = presentationName;
		undoableChanges = new ArrayList<UndoableChange>();
		if (changes != null) {
			undoableChanges.addAll(changes);
		}
	}
	
	public void addChange(UndoableChange change) {
		if (change != null) {
			undoableChanges.add(change);
		}
	}
	
	public void addAllChanges(UndoableCombinedChange change) {
		if (change != null) {
			undoableChanges.addAll(change.undoableChanges);
		}
	}
	
	public boolean hasChanges() {
		return !undoableChanges.isEmpty();
	}
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Iterator<UndoableChange> it = undoableChanges.iterator();
		while (it.hasNext()) {
			it.next().redo();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		Iterator<UndoableChange> it = undoableChanges.iterator();
		while (it.hasNext()) {
			it.next().undo();
		}
	}

	@Override
	public String getPresentationName() {
		return presentationName;
	}
	
	public boolean contains(Point p) {
		Iterator<UndoableChange> it = undoableChanges.iterator();
		while (it.hasNext()) {
			UndoableChange change = it.next();
			if (change instanceof UndoableMapChange) {
				//TODO problem with the MapModel's offset I think. Need to investigate.
				if(((UndoableMapChange)change).getSquare().equals(p)) {
					return true;
				}
			}
		}
		return false;
	}
}
