package net.edwebb.jim.model;

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
	
	protected String presentationName;


	public void setPresentationName(String presentationName) {
		this.presentationName = presentationName; 
	}
	
	@Override
	public String getPresentationName() {
		return presentationName;
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo " + presentationName;
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo " + presentationName;
	}
}
