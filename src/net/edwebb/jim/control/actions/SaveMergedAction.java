package net.edwebb.jim.control.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.edwebb.jim.control.MapController;
import net.edwebb.jim.factory.FactoryManager;
import net.edwebb.jim.model.DiffMapModel;
import net.edwebb.jim.model.MapData;

public class SaveMergedAction extends SaveAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SaveMergedAction(MapController controller) {
		super(controller);
        putValue(Action.NAME, "Merge");
        putValue(Action.SHORT_DESCRIPTION, "Save Merged maps");
        putValue(Action.LONG_DESCRIPTION, "Save all the features from both maps");
        putValue(Action.SMALL_ICON, makeImageIcon("merge-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control M"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_M));
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		if (getController().getModel().getClass().equals(DiffMapModel.class)) {
			DiffMapModel d = (DiffMapModel)getController().getModel();
			MapData md = d.getMerged();

			JFileChooser fc = getSaveFileChooser();
			int returnVal = fc.showSaveDialog(getController().getFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = suffixFile(fc);

				if (file.exists()) {
					int confirm = JOptionPane.showConfirmDialog(getController().getFrame(), file.getName() + " exists. Do you want to overwrite?");
					if (confirm != JOptionPane.OK_OPTION) {
						return;
					}
				}
				
				try {
					FactoryManager.getInstance().saveTo(md, file);
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(getController().getFrame(), "Cannot save map to " + file.getAbsolutePath());
				}
			}
		} else {
			JOptionPane.showMessageDialog(getController().getFrame(), "This can only be done if you are comparing maps");
		}
	}
}
