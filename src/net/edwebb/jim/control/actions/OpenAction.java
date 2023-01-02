package net.edwebb.jim.control.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import net.edwebb.jim.control.MapController;
import net.edwebb.jim.factory.FactoryManager;
import net.edwebb.jim.model.MapData;
import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.StandardMapModel;

public class OpenAction extends MapAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JFileChooser load;
	
	public OpenAction(MapController controller) {
        super(controller);
		putValue(Action.NAME, "Open");
        putValue(Action.SHORT_DESCRIPTION, "Open map");
        putValue(Action.LONG_DESCRIPTION, "Open a map");
        putValue(Action.SMALL_ICON, makeImageIcon("open-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
	}
	
	private JFileChooser getLoadFileChooser() {
		if (load == null) {
			load = new JFileChooser();
			load.setAcceptAllFileFilterUsed(false);
			List<FileFilter> filters = FactoryManager.getInstance().getReadFilters();
			for (int i = 0; i < filters.size(); i++) {
				load.addChoosableFileFilter(filters.get(i));
			}
		}
		return load;
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		JFileChooser fc = getLoadFileChooser();
		int returnVal = fc.showOpenDialog(getController().getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			MapData md;
			try {
				md = FactoryManager.getInstance().createFrom(fc.getSelectedFile());
				MapModel m = new StandardMapModel(getController().getModel().getSize(), md, fc.getSelectedFile().getName());
				getController().setModel(m);
				//getController().getMiniMap().revalidate();
				if (FactoryManager.getInstance().getUnmatched().size() > 0) {
					StringBuilder sb = new StringBuilder();
					sb.append("There were some features that were not recognised:\n");
					Iterator<String> it = FactoryManager.getInstance().getUnmatched().iterator();
					while (it.hasNext()) {
						sb.append(it.next());
						sb.append("\n");
					}
					getTxtNote().setText(sb.toString());
					getTxtNote().setCaretPosition(0);
					JOptionPane.showMessageDialog(getController().getFrame(), getScrNote(), "Cannot read all data", JOptionPane.WARNING_MESSAGE);
				}
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(getController().getFrame(), "Cannot load map from " + fc.getSelectedFile().getAbsolutePath(), "Cannot open file", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}

