package net.edwebb.jim.model.events;

import java.awt.Rectangle;

import net.edwebb.jim.MapConstants.ChangeType;
import net.edwebb.jim.model.MapModel;

public class ViewChangeEvent extends MapChangeEvent {

	private Rectangle oldView;
	private Rectangle newView;
	
	public ViewChangeEvent(MapModel model, Rectangle oldView, Rectangle newView) {
		super(model, ChangeType.VIEW);
		this.oldView = oldView;
		this.newView = newView;
	}
	
	public Rectangle getOldView() {
		return oldView;
	}

	public Rectangle getNewView() {
		return newView;
	}

	@Override
	public String toString() {
		if (oldView == null) {
			return "View set to (" + newView.y + "," + newView.x + ") " + newView.height + "x" + newView.width;
		} else if (newView == null) {
			return "View unset from (" + oldView.y + "," + oldView.x + ") " + oldView.height + "x" + oldView.width;
		} else {
			return "View set from ("  + oldView.y + "," + oldView.x + ") " + oldView.height + "x" + oldView.width + " to (" + newView.y + "," + newView.x + ") " + newView.height + "x" + newView.width;
		}
	}

}
