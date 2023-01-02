package net.edwebb.jim.control.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import net.edwebb.jim.control.MapController;
import net.edwebb.jim.model.UndoableCombinedChange;
import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.data.Feature;
import net.edwebb.mi.data.Terrain;
import net.edwebb.mi.extract.MIExtractor;
import net.edwebb.mi.extract.Sighting;
import net.edwebb.mi.extract.Stats;
import net.edwebb.mi.extract.Turn;
import net.edwebb.mi.pdf.PDFExtractor;

public class ExtractAction extends MapAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JFileChooser extract;
	private JFileChooser dir;
	
	public ExtractAction(MapController controller) {
        super(controller);
		putValue(Action.NAME, "Extract");
        putValue(Action.SHORT_DESCRIPTION, "Extract data from Turns");
        putValue(Action.LONG_DESCRIPTION, "Extract data from Monster Island pdf files");
        putValue(Action.SMALL_ICON, makeImageIcon("extract-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
	}
	
	private JFileChooser getExtractFileChooser() {
		if (extract == null) {
			extract = new JFileChooser();
			extract.setAcceptAllFileFilterUsed(false);
			extract.setMultiSelectionEnabled(true);
			
			FileFilter extractFilter = new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isDirectory() || file.getName().toLowerCase().endsWith(".pdf");
				}

				@Override
				public String getDescription() {
					return "MI Turn Result Files (*.pdf)";
				}
			};

			extract.addChoosableFileFilter(extractFilter);
		}
		return extract;
	}
	
	private JFileChooser getDirectoryChooser() {
		if (dir == null) {
			
			dir = new JFileChooser();
         	dir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		return dir;
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		
		JFileChooser fc = getExtractFileChooser();
		int returnVal = fc.showOpenDialog(getController().getFrame());
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		File[] files = fc.getSelectedFiles();
		PDFExtractor pdfExtractor = new PDFExtractor();
		
		MIExtractor miExtractor;
		
		fc = getDirectoryChooser();
		returnVal = fc.showOpenDialog(getController().getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			miExtractor = new MIExtractor(fc.getSelectedFile());
		} else {
			miExtractor = new MIExtractor();
		}
		
		ExtractTask task = new ExtractTask(this, pdfExtractor, miExtractor, files);
		task.execute();
	}
}

class ExtractTask extends SwingWorker<Object, String> {

	private PDFExtractor pdfExtractor;
	private MIExtractor miExtractor;
	private File[] files;
	private Set<Sighting> masterSightings = new HashSet<Sighting>();
	private ExtractAction action;
	
	
	public ExtractTask(ExtractAction action, PDFExtractor pdfExtractor, MIExtractor miExtractor, File[] files) {
		this.action = action;
		this.pdfExtractor = pdfExtractor;
		this.miExtractor = miExtractor;
		this.files = files;
	}
	
	@Override
	protected Object doInBackground() throws Exception {

		readTurns();
		addToMap();

		//refresh();
		//getMiniMap().revalidate();
		
		return null;
	}

	private void readTurns() {
		String mode = "D";
	    int[] coords = new int[] {0,0}; //getCoords();  
		
		Stats stats = null;
		
		String text = null;
		for (int i = 0; i < files.length; i++) {
			action.getController().setStatusLabel("Processing " + files[i].getName());
			try {
				text = pdfExtractor.extract(files[i]);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(action.getController().getFrame(), "Cannot read data from " + files[i].getAbsolutePath());
				action.getController().setStatusLabel("Java Island Mapper");
				return;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println();
			}
			if (text == null || text.length() == 0) {
				JOptionPane.showMessageDialog(action.getController().getFrame(), "Cannot find any text in " + files[i].getAbsolutePath());
				action.getController().setStatusLabel("Java Island Mapper");
				return;
			}
			try {
				Turn turn = miExtractor.extract(new StringReader(text), (i==0 ? "N" : mode), coords[0], coords[1], stats);
				coords[0] = turn.getX();
				coords[1] = turn.getY();
				stats = turn.getStats();
				masterSightings.addAll(turn.getSightings());
			} catch (IOException e) {
				JOptionPane.showMessageDialog(action.getController().getFrame(), "Cannot extract data from " + files[i].getAbsolutePath());
				action.getController().setStatusLabel("Java Island Mapper");
				return;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println();
			}
		}
	}
	
	private void addToMap() {
		StringBuilder sb = new StringBuilder();
		String result;
		Iterator<Sighting> it = masterSightings.iterator();
		UndoableCombinedChange change = new UndoableCombinedChange("Update map with extracted information");
		
		while (it.hasNext()) {
			Sighting s = it.next();
			
			if (s.getCode().length() == 8 && s.getCode().startsWith("%")) {
				for (int i = 0; i < s.getCode().length(); i+=4) {
					result = addFeature(new Sighting(s.getX(),s.getY(),s.getCode().substring(i, i + 4)), change);
					if (result != null) {
						sb.append(result);
					}
				}
			} else {
				result = addFeature(s, change);
				if (result != null) {
					sb.append(result);
				}
			}
		}

		action.getController().setStatusLabel("Java Island Mapper");
		//refresh();
		
		if (sb.length() > 0) {
			sb.insert(0, "These sightings were not recognised:\n");
			action.getTxtNote().setText(sb.toString());
			action.getTxtNote().setCaretPosition(0);
			JOptionPane.showMessageDialog(action.getController().getFrame(), action.getScrNote(), "Cannot read all data", JOptionPane.WARNING_MESSAGE);
		}
	}

	private String addFeature(Sighting s, UndoableCombinedChange masterChange) {
		Feature f = DataStore.getInstance().getFeatureByCode(s.getCode());
		
		if (f == null) {
			return "(" + s.getY()  + "," + s.getX() + ") " + s.getCode() + "\n";
		} else {
			Point point = new Point(s.getX(), s.getY());
			Rectangle rect = action.getController().getModel().getBounds();
			rect = new Rectangle(rect.x, rect.y - rect.height, rect.width, rect.height);
			if (!rect.contains(point)) {
				return "(" + s.getY()  + "," + s.getX() + ") " + s.getCode() + " (out of bounds)\n";
			} else {
				if (f instanceof Terrain) {
					action.getController().getModel().setTerrain(point, (Terrain)f);
				} else {
					action.getController().getModel().add(point, f);
				}
			}
		}
		return null;
	}
}
