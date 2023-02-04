package net.edwebb.jim.control.actions;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.edwebb.jim.control.MapController;
import net.edwebb.jim.control.MapDimensions;
import net.edwebb.jim.model.DiffMapModel;
import net.edwebb.jim.model.MapData;
import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.StandardMapModel;

public class ResizeAction extends MapAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private MapDimensions frmDimensions;
	
	public ResizeAction(MapController controller) {
		super(controller);
		putValue(Action.NAME, "Resize");
        putValue(Action.SHORT_DESCRIPTION, "Resize the open map");
        putValue(Action.LONG_DESCRIPTION, "Resize the open map");
        putValue(Action.SMALL_ICON, makeImageIcon("resize-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control R"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
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

		MapModel model = getController().getModel();
		if (model instanceof DiffMapModel) {
			JOptionPane.showMessageDialog(getController().getFrame(), "Cannot resize a map that is being compared.");
			return;
		}

		MapDimensions d = getMapDimensions();
		int[] dims = d.getDimensions(model.getBounds().y, model.getBounds().x, model.getBounds().width, model.getBounds().height);
		if (dims != null && dims.length == 4 && dims[2] > 0 && dims[3] > 0) {
			Rectangle used = getController().getModel().getUsed();
			
			if (dims[0] < used.y || dims[1] > used.x ||  dims[2] + dims[0] < used.x + used.width ||dims[1] - dims[3] > used.y - used.height) {
				int confirm = JOptionPane.showConfirmDialog(getController().getFrame(), "The resized map will crop some of the existing map. Are you sure you want to continue?");
				if (confirm != JOptionPane.OK_OPTION) {
					return;
				}
			}
			MapData data = model.getData().resize(dims[0], dims[1], dims[2], dims[3]);
			model = new StandardMapModel(model.getSize(), data, model.getName());
			getController().setModel(model);
		}
	}

}
