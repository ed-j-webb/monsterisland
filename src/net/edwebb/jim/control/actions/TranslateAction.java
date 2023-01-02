package net.edwebb.jim.control.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import net.edwebb.jim.control.MapController;
import net.edwebb.jim.factory.FactoryManager;

public class TranslateAction extends MapAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JFileChooser trans;
	private JFileChooser csv;
	
	public TranslateAction(MapController controller) {
        super(controller);
		putValue(Action.NAME, "Translate");
        putValue(Action.SHORT_DESCRIPTION, "Create translation file");
        putValue(Action.LONG_DESCRIPTION, "Create a translation file for this file");
        putValue(Action.SMALL_ICON, makeImageIcon("trans-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control T"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
	}
	
	private JFileChooser getTransFileChooser() {
		if (trans == null) {
			trans = new JFileChooser();
			trans.setAcceptAllFileFilterUsed(false);
			List<FileFilter> filters = FactoryManager.getInstance().getTranslateFilters();
			for (int i = 0; i < filters.size(); i++) {
				trans.addChoosableFileFilter(filters.get(i));
			}
		}
		return trans;
	}
	
	private JFileChooser getCSVFileChooser() {
		if (csv == null) {
			csv = new JFileChooser();
			csv.setFileFilter(new FileFilter() {

				@Override
				public boolean accept(File f) {
					return f.getName().toLowerCase().endsWith(".csv");
				}

				@Override
				public String getDescription() {
					return "JIM Translation Files (.csv)";
				}
				
			});
			csv.setAcceptAllFileFilterUsed(false);
			List<FileFilter> filters = FactoryManager.getInstance().getTranslateFilters();
			for (int i = 0; i < filters.size(); i++) {
				csv.addChoosableFileFilter(filters.get(i));
			}
		}
		return csv;
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		JFileChooser fc = getTransFileChooser();
		int returnVal = fc.showOpenDialog(getController().getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			
			if (!file.exists()) {
				JOptionPane.showMessageDialog(getController().getFrame(), "You must select an existing file to read from", "Cannot open file", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			String text = "";
			try {
				text = FactoryManager.getInstance().listTranslations(file);
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(getController().getFrame(), "Cannot read from " + file.getAbsolutePath() + " or related files", "Cannot open file", JOptionPane.ERROR_MESSAGE);
				return;
			}

			JFileChooser csv = getCSVFileChooser();
			returnVal = csv.showSaveDialog(getController().getFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = suffixFile(fc);
				if (csv.getSelectedFile().exists()) {
					int confirm = JOptionPane.showConfirmDialog(getController().getFrame(), file.getName() + " exists. Do you want to overwrite?");
					if (confirm != JOptionPane.OK_OPTION) {
						return;
					}
				}
				try {
					FileWriter write = new FileWriter(file);
					write.write(text);
					write.close();
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(getController().getFrame(), "Cannot save translations to " + file.getAbsolutePath());
				}
			}
		}
	}
}
