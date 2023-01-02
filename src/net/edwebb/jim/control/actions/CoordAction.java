package net.edwebb.jim.control.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.edwebb.jim.control.MapController;
import net.edwebb.mi.data.Coordinate;

public class CoordAction extends MapAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JComboBox<Coordinate> cmbCoords;
	
	public CoordAction(MapController controller, JComboBox<Coordinate> cmbCoords) {
		super(controller);
		this.cmbCoords = cmbCoords;
        putValue(Action.NAME, "Origin");
        putValue(Action.SHORT_DESCRIPTION, "Set as default Co-ordinates");
        putValue(Action.LONG_DESCRIPTION, "Set as default Co-ordinates");
        putValue(Action.SMALL_ICON, makeImageIcon("set-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		Coordinate coord = (Coordinate)cmbCoords.getSelectedItem();
		if (coord != null) {
			int result = JOptionPane.showConfirmDialog(getController().getFrame(), "Are you sure you want to set this map's default co-ordinates to " + coord + "?\n", "Set Default Co-Ordinates", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				getController().getModel().setDefaultCoOrdinates(coord);

				//TODO this should be handled by a listener on the model
				getController().setMapLabel(getController().getModel());
			}
		}
	}
}
