package net.edwebb.jim.control.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.edwebb.jim.undo.ChangeUndoManager;
import net.edwebb.jim.undo.UndoListener;
import net.edwebb.jim.undo.UndoableChange;

public class RedoAction extends MapAction implements UndoListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ChangeUndoManager undoManager;
	
	public RedoAction(ChangeUndoManager undoManager) {
		super(null);
		this.undoManager = undoManager;
		undoManager.addUndoListener(this);
        putValue(Action.NAME, "Redo");
        putValue(Action.SHORT_DESCRIPTION, "Redo change");
        putValue(Action.LONG_DESCRIPTION, "Redo a change to the map");
        putValue(Action.SMALL_ICON, makeImageIcon("redo-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Y"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Y));
        setEnabled(false);
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		if (undoManager.canRedo()) {
			undoManager.redo();
		}
	}

	@Override
	public void undoManagerChanged(ChangeUndoManager manager) {
		setEnabled(manager.canRedo());
		putValue(SHORT_DESCRIPTION, manager.getRedoPresentationName());
	}

	@Override
	public void changeMade(UndoableChange change, boolean undone) {
	}
}
