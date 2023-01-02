package net.edwebb.jim.model.events;

import java.awt.Point;

import net.edwebb.jim.model.MapModel;

public class SelectedChangeEvent extends MapChangeEvent {

	private Point oldSelected;
	private Point newSelected;
	
	public SelectedChangeEvent(MapModel model, Point oldSelected, Point newSelected) {
		super(model, MAP_CHANGE_TYPE.SELECTED);
		this.oldSelected = oldSelected;
		this.newSelected = newSelected;
	}
	
	public Point getOldSelected() {
		return oldSelected;
	}

	public Point getNewSelected() {
		return newSelected;
	}

	@Override
	public String toString() {
		if (oldSelected == null) {
			return "Selected square set to (" + newSelected.y + "," + newSelected.x + ")";
		} else if (newSelected == null) {
			return "Square (" + oldSelected.y + "," + oldSelected.x + ") unselected";
		} else {
			return "Selected square changed from (" + oldSelected.y + "," + oldSelected.x + ") to (" + newSelected.y + "," + newSelected.x + ")";
		}
	}

}
