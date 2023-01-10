package net.edwebb.jim.model;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.edwebb.jim.model.events.MapChangeEvent;
import net.edwebb.jim.model.events.MapChangeListener;
import net.edwebb.mi.data.Feature;
import net.edwebb.mi.data.Flag;

public abstract class AbstractMapModel implements MapModel {

	protected List<MapChangeListener> mapChangeListeners = new ArrayList<MapChangeListener>();

	protected MapModel parent;
	
	protected boolean busy;
	
	@Override
	public int getExtra(Point square, Feature feature) {
		return 0;
	}

	@Override
	public int getExtra(Point square, Flag flag) {
		return 0;
	}

	@Override
	public int getExtra(Point square) {
		return 0;
	}

	@Override
	public void addMapChangeListener(MapChangeListener l) {
		if (!mapChangeListeners.contains(l)) {
			mapChangeListeners.add(l);
		}
	}

	@Override
	public void removeMapChangeListener(MapChangeListener l) {
		mapChangeListeners.remove(l);
	}

	protected void sendEvent(MapChangeEvent event) {
		try {
			busy = true;
			if (getParent() != null) {
				getParent().recieveMapChangeEvent(event);
			}
	
			for (MapChangeListener l : mapChangeListeners) {
				l.mapChanged(event);
			}
		} finally {
			busy = false;
		}
	}
	
	@Override
	public boolean isBusy() {
		return busy;
	}

	@Override
	public void setParent(MapModel model) {
		this.parent = model;
	}

	@Override
	public MapModel getParent() {
		return parent;
	}

	@Override
	public void recieveMapChangeEvent(MapChangeEvent event) {
	}

	/**
	 * Ensures that the point is within the bounds of the current MapModel. If either co-ordinate is beyond the bounds of the 
	 * MapModel then the point is adjusted to the closest point within the bounds.
	 * @param p the Point to check and adjust if necessary
	 * @return a point that is within the bounds of the MapModel
	 */
	protected Point bound(Point p) {
		Rectangle bounds = getBounds();
		int x = Math.min(Math.max(p.x, bounds.x), bounds.width + bounds.x);
		int y = Math.max(Math.min(p.y, bounds.y), bounds.y - bounds.height);
		if (p.x != x || p.y != y) {
			return new Point(x, y);
		}
		return p;
	}
	
	/**
	 * Ensures that the rectangle is within the bounds of the current MapModel. If any side is beyond the bounds of the 
	 * MapModel then the rectangle is adjusted to the closest rectangle within the bounds. This method does not adjust the width
	 * and height of the rectangle only the x and y co-ordinates
	 * @param rect the Point to check and adjust if necessary
	 * @return a rectangle that is within the bounds of the MapModel
	 */
	protected Rectangle bound(Rectangle rect) {
		Rectangle bounds = getBounds();
		int x = 0;
		int y = 0;
		
		// Check not beyond top/left
		boolean changed = false;
		if (rect.x < bounds.x) {
			x = bounds.x;
			changed = true;
		} else {
			x = rect.x;
		}
		if (rect.y > bounds.y) {
			y = bounds.y;
			changed = true;
		} else {
			y = rect.y;
		}
		if (changed) {
			return new Rectangle(x, y,  rect.width, rect.height);
		}
		
		// Check not beyond bottom/right
		x = (bounds.x + bounds.width) - (rect.x + rect.width) + 2;
		y = (bounds.y - bounds.height) - (rect.y - rect.height) - 1;
		if (x > 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}
		if (x < 0 || y > 0) {
			return new Rectangle(rect.x + x, rect.y + y, rect.width, rect.height);
		}

		return rect;
	}
}
