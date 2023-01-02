package net.edwebb.jim.control.actions;

import java.io.File;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.edwebb.jim.control.MapController;

public abstract class MapAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private MapController controller;

	// The long text dialog box
	private JTextArea txtNote;
	private JScrollPane scrNote;

	public MapAction(MapController controller) {
		this.controller = controller;
	}
	
	protected JTextArea getTxtNote() {
		if (txtNote == null) {
			txtNote = new JTextArea(6, 30);
			txtNote.setEditable(false);
		}
		return txtNote;
	}
	
	protected JScrollPane getScrNote() {
		if (scrNote == null) {
			scrNote = new JScrollPane(getTxtNote());
		}
		return scrNote;
	}
	
	protected MapController getController() {
		return controller;
	}
	
	/**
	 * Makes an ImageIcon from the filepath specified
	 * @param path the path to the image file
	 * @return an ImageIcon
	 */
	protected ImageIcon makeImageIcon(String path) {
		URL url = this.getClass().getResource(path);
        if (url == null) {
            return null;
        } else {
            return new ImageIcon(url);
        }
	}

	protected File suffixFile(JFileChooser fc) {
		File file = fc.getSelectedFile();
		
		// Add the correct suffix if the user was too lazy to write it in themselves
		String desc = fc.getFileFilter().getDescription();
		String suffix = desc.substring(desc.lastIndexOf("(")+2, desc.lastIndexOf(")"));
		if (!file.getName().endsWith(suffix)) {
			file = new File(file.getParentFile(), file.getName() + suffix);
		}
		return file;
	}

}
