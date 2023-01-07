package net.edwebb.jim.control.actions;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import net.edwebb.jim.control.MapController;
import net.edwebb.jim.factory.FactoryManager;
import net.edwebb.jim.model.DiffMapModel;
import net.edwebb.jim.model.MapData;
import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.StandardMapModel;

public class CompareAction extends MapAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CompareAction(MapController controller) {
		super(controller);
        putValue(Action.NAME, "Compare");
        putValue(Action.SHORT_DESCRIPTION, "Compare this map to another");
        putValue(Action.LONG_DESCRIPTION, "Compare this map to another");
        putValue(Action.SMALL_ICON, makeImageIcon("compare-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control P"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
	}
	
	private void clearFilters(JFileChooser fc) {
		FileFilter[] ff = fc.getChoosableFileFilters();
		for (int i = 0; i < ff.length; i++) {
			fc.removeChoosableFileFilter(ff[i]);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		MapModel primary = getController().getModel();
		if (primary.isDirty()) {
			JOptionPane.showMessageDialog(getController().getFrame(), "You must save your map before comparing");
			return;
		}
		if (primary.getDefaultCoOrdinates() == null || primary.getDefaultCoOrdinates().getOffset().equals(new Point(0,0))) {
			JOptionPane.showMessageDialog(getController().getFrame(), "You must set default co-ordinates for the current map before it can be compared to another");
			return;
		}
		JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		clearFilters(fc);
		List<FileFilter> filters = FactoryManager.getInstance().getReadFilters();
		for (int i = 0; i < filters.size(); i++) {
			fc.addChoosableFileFilter(filters.get(i));
		}
		int returnVal = fc.showOpenDialog(getController().getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			MapData smd;
			try {
				smd = FactoryManager.getInstance().createFrom(fc.getSelectedFile());

				MapModel secondary = new StandardMapModel(primary.getSize(), smd, fc.getSelectedFile().getName());
				if (secondary.getDefaultCoOrdinates() == null || secondary.getDefaultCoOrdinates().getOffset().equals(new Point(0,0))) {
					JOptionPane.showMessageDialog(getController().getFrame(), "The map you want to compare must have default co-ordinates set before it can be compared to another");
					return;
				}

				MapModel m = new DiffMapModel(primary, secondary);
				
				getController().setModel(m);
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(getController().getFrame(), "Cannot load map from " + fc.getSelectedFile().getAbsolutePath());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(getController().getFrame(), e.getMessage());
			}
		}
	}
}
