package net.edwebb.jim.control.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.edwebb.jim.control.MapController;
import net.edwebb.jim.control.MapDimensions;
import net.edwebb.jim.model.MapData;
import net.edwebb.jim.model.StandardMapModel;

public class NewAction extends MapAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private MapDimensions frmDimensions;
	
	public NewAction(MapController controller) {
		super(controller);
		putValue(Action.NAME, "New");
        putValue(Action.SHORT_DESCRIPTION, "Create new map");
        putValue(Action.LONG_DESCRIPTION, "Create a new map");
        putValue(Action.SMALL_ICON, makeImageIcon("new-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
	}
	
	/**
	 * Returns the Map Dimensions dialog box
	 * @return the Map Dimensions dialog box
	 */
	private MapDimensions getMapDimensions() {
		if (frmDimensions == null) {
			frmDimensions = new MapDimensions(getController().getFrame());
		}
		return frmDimensions;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		MapDimensions d = getMapDimensions();
		int[] dims = d.getDimensions();
		if (dims != null && dims.length == 4 && dims[2] > 0 && dims[3] > 0) {
			getController().setModel(new StandardMapModel(getController().getModel().getSize(), new MapData(dims[0], dims[1], dims[2], dims[3]), "Unsaved.jim"));
		}
	}

}
