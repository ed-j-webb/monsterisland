package net.edwebb.jim.undo;

import javax.swing.undo.AbstractUndoableEdit;

import net.edwebb.jim.MapConstants.ChangeType;

/**
 * Super class for all Undoable actions in JIM
 * @author Ed Webb
 *
 */
public abstract class UndoableChange extends AbstractUndoableEdit {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected ChangeType changeType;
	
	public UndoableChange(ChangeType changeType) {
		this.changeType = changeType;
	}
	
	@Override
	public String getRedoPresentationName() {
		return "Redo " + getPresentationName();
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo " + getPresentationName();
	}
	
	public ChangeType getChangeType() {
		return changeType;
	}
}
