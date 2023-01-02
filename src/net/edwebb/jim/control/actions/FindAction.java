package net.edwebb.jim.control.actions;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.edwebb.jim.control.MapController;
import net.edwebb.jim.control.MapFind;
import net.edwebb.mi.data.Feature;

public class FindAction extends MapAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private MapFind frmFind;
	
	public FindAction(MapController controller) {
		super(controller);
        putValue(Action.NAME, "Find");
        putValue(Action.SHORT_DESCRIPTION, "Find Feature");
        putValue(Action.LONG_DESCRIPTION, "Find features on the map");
        putValue(Action.SMALL_ICON, makeImageIcon("find-16x16.gif"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control F"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_F));
	}
	
	private MapFind getMapFind() {
		if (frmFind == null) {
			frmFind = new MapFind(getController().getFrame());
		}
		return frmFind;
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		MapFind mf = getMapFind(); 
		MapFind.FindData fd = mf.getFind();
		List<Point> list;
		String searchTerm;
		if (fd.getFeature() != null) {
			list = getFeatureList(fd.getFeature(), fd.getDistance());
			getController().getSearch().setFoundID(fd.getFeature().getId());
			getController().getSearch().setFoundNote(null);
			searchTerm = fd.getFeature().getName();
		} else if (fd.getNote() != null) {
			list = getNoteList(fd.getNote(), fd.getDistance());
			getController().getSearch().setFoundID((short)0);
			getController().getSearch().setFoundNote(fd.getNote());
			searchTerm = fd.getNote();
		} else {
			return;
		}
		getController().getSearch().setFoundSquares(list);
		if (list.size() > 0) {
			getController().setStatusLabel("Found " + list.size() + " " + searchTerm);
		} else {
			getController().setStatusLabel("Cannot find " + searchTerm);
		}
		getController().updateSearch();
	}
	
	private List<Point> getFeatureList(Feature f, int d) {
		if (d < 0) {
			return getController().getIndex().getPoints(f);
		} else {
			return getController().getIndex().getPoints(f, getController().getModel().getSelected(), d);
		}
	}

	private List<Point> getNoteList(String n, int d) {
		if (d < 0) {
			return getController().getIndex().getPoints(n);
		} else {
			return getController().getIndex().getPoints(n, getController().getModel().getSelected(), d);
		}
	}
}

