package net.edwebb.jim.control.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.edwebb.jim.undo.ChangeUndoManager;
import net.edwebb.jim.undo.UndoListener;
import net.edwebb.jim.undo.UndoableChange;

public class UndoAction extends MapAction implements UndoListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	ChangeUndoManager undoManager;
	
	public UndoAction(ChangeUndoManager undoManager) {
		super(null);
		this.undoManager = undoManager;
		undoManager.addUndoListener(this);
        putValue(Action.NAME, "Undo");
        putValue(Action.SHORT_DESCRIPTION, "Undo change");
        putValue(Action.LONG_DESCRIPTION, "Undo a change to the map");
        putValue(Action.SMALL_ICON, makeImageIcon("undo-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Z"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Z));
        setEnabled(false);
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		if (undoManager.canUndo()) {
			undoManager.undo();
		}
	}

	@Override
	public void undoManagerChanged(ChangeUndoManager manager) {
		setEnabled(manager.canUndo());
		putValue(SHORT_DESCRIPTION, manager.getUndoPresentationName());
	}
	
	@Override
	public void changeMade(UndoableChange change, boolean undone) {
	}
}
