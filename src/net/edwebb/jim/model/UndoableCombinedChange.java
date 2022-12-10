package net.edwebb.jim.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class UndoableCombinedChange extends UndoableChange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<UndoableChange> undoableChanges;
	
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
}
