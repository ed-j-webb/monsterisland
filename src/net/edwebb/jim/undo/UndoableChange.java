package net.edwebb.jim.undo;

import javax.swing.undo.AbstractUndoableEdit;

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
	
	@Override
	public String getRedoPresentationName() {
		return "Redo " + getPresentationName();
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo " + getPresentationName();
	}
}
