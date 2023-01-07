package net.edwebb.jim.control.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import net.edwebb.jim.control.MapController;
import net.edwebb.jim.factory.FactoryManager;

public class SaveAction extends MapAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JFileChooser save = null;
	
	public SaveAction(MapController controller) {
		super(controller);
        putValue(Action.NAME, "Save");
        putValue(Action.SHORT_DESCRIPTION, "Save map");
        putValue(Action.LONG_DESCRIPTION, "Save the map");
        putValue(Action.SMALL_ICON, makeImageIcon("save-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
	}
	
	protected JFileChooser getSaveFileChooser() {
		if (save == null) {
			save = new JFileChooser();
			save.setAcceptAllFileFilterUsed(false);
			List<FileFilter> filters = FactoryManager.getInstance().getWriteFilters();
			for (int i = 0; i < filters.size(); i++) {
				save.addChoosableFileFilter(filters.get(i));
			}
		}
		return save;
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
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
				FactoryManager.getInstance().saveTo(getController().getModel().getData(), file);
				getController().getModel().setName(file.getName());
				getController().setMapLabel(getController().getModel());
				getController().getOpenAction().getLoadFileChooser().setSelectedFile(file);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(getController().getFrame(), "Cannot save map to " + file.getAbsolutePath());
			}
		}
	}
}
