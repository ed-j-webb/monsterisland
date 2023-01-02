package net.edwebb.jim.control.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.edwebb.jim.control.ChangeUndoManager;

public class RedoAction extends MapAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ChangeUndoManager undoManager;
	
	public RedoAction(ChangeUndoManager undoManager) {
		super(null);
		this.undoManager = undoManager;
        putValue(Action.NAME, "Redo");
        putValue(Action.SHORT_DESCRIPTION, "Redo change");
        putValue(Action.LONG_DESCRIPTION, "Redo a change to the map");
        putValue(Action.SMALL_ICON, makeImageIcon("redo-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control R"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
        setEnabled(false);
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		if (undoManager.canRedo()) {
			undoManager.redo();
			//refresh();
		}
	}
}
