package net.edwebb.jim.undo;

public interface UndoListener {

	public void undoManagerChanged(ChangeUndoManager manager);
	
	public void changeMade(UndoableChange change, boolean undone);
}
