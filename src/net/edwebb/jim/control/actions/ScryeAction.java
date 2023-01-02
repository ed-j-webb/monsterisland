package net.edwebb.jim.control.actions;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.edwebb.jim.control.MapController;
import net.edwebb.jim.control.MapText;
import net.edwebb.mi.data.Terrain;
import net.edwebb.mi.reader.TurnDigester;
import net.edwebb.mi.reader.TurnReader;

public class ScryeAction extends MapAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// The text dialog box
	private MapText frmText;
	
	public ScryeAction(MapController controller) {
        super(controller);
		putValue(Action.NAME, "Scrye");
        putValue(Action.SHORT_DESCRIPTION, "Enter Scrying data on to map");
        putValue(Action.LONG_DESCRIPTION, "Enter Scrying data on to map");
        putValue(Action.SMALL_ICON, makeImageIcon("ball-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Y"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Y));
	}
	
	private MapText getMapText() {
		if (frmText == null) {
			frmText = new MapText(getController().getFrame());
		}
		return frmText;
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		Point p = getController().getModel().getSelected();
		if (p == null) {
			JOptionPane.showMessageDialog(getController().getFrame(), "You must select the square where the scrying occurred.");
			return;
		}
		
		String text = getMapText().getText();
		if (text == null) {
			return; 
		}

		TurnReader r = new TurnReader(text);
		List<net.edwebb.mi.data.Sighting> list = TurnDigester.readScrye(r, p.x, p.y);
		StringBuilder sb = new StringBuilder();
		Iterator<net.edwebb.mi.data.Sighting> it = list.iterator();
		while (it.hasNext()) {
			net.edwebb.mi.data.Sighting s = it.next();
			if (s.getFeature() == null) {
				sb.append("(" + s.getSquare().y  + "," + s.getSquare().x + ") " + s.getThing() + "\n");
			} else {
				if (s.getFeature() instanceof Terrain) {
					getController().getModel().setTerrain(s.getSquare(), (Terrain)s.getFeature());
				} else {
					getController().getModel().add(s.getSquare(), s.getFeature());
				}
			}
		}
		
		if (sb.length() > 0) {
			sb.insert(0, "These sightings were not recognised:\n");
			getTxtNote().setText(sb.toString());
			getTxtNote().setCaretPosition(0);
			JOptionPane.showMessageDialog(getController().getFrame(), getScrNote(), "Cannot read all data", JOptionPane.WARNING_MESSAGE);
		}
		
	}
}
