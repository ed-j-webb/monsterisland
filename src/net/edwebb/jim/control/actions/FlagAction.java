package net.edwebb.jim.control.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

import net.edwebb.jim.control.MapController;

public class FlagAction extends MapAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public FlagAction(MapController controller) {
        super(controller);
		putValue(Action.NAME, "Show Flags");
        putValue(Action.SHORT_DESCRIPTION, "Show flags on the map");
        putValue(Action.LONG_DESCRIPTION, "Show Flags on the map");
        putValue(Action.SMALL_ICON, makeImageIcon("flag-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control F"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_F));
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		JToggleButton cmd = (JToggleButton)evt.getSource();
		getController().getView().setViewFlags(cmd.isSelected());
	}
}

