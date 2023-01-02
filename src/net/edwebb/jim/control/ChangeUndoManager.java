package net.edwebb.jim.control;

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

public class ChangeUndoManager extends UndoManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		UndoableEdit nextEdit = editToBeUndone();
		if (nextEdit == null) {
			return false;
		} else {
			edits.set(edits.indexOf(nextEdit), anEdit);
			return true;
		}
	}
	
	public boolean removeNextUndo() {
		UndoableEdit nextEdit = editToBeUndone();
		if (nextEdit == null) {
			return false;
		} else {
			trimEdits(edits.size() - 1, edits.size() - 1);
			return true;
		}
	}

	public boolean removeNextRedo() {
		UndoableEdit nextEdit = editToBeRedone();
		if (nextEdit == null) {
			return false;
		} else {
			return edits.remove(nextEdit);
		}
	}
}			
