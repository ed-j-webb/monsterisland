package net.edwebb.jim.control.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.edwebb.jim.control.ChangeUndoManager;

public class UndoAction extends MapAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	ChangeUndoManager undoManager;
	
	public UndoAction(ChangeUndoManager undoManager) {
		super(null);
		this.undoManager = undoManager;
        putValue(Action.NAME, "Undo");
        putValue(Action.SHORT_DESCRIPTION, "Undo change");
        putValue(Action.LONG_DESCRIPTION, "Undo a change to the map");
        putValue(Action.SMALL_ICON, makeImageIcon("undo-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control U"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
        setEnabled(false);
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		if (undoManager.canUndo()) {
			undoManager.undo();
		}
	}
}
