package net.edwebb.jim.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.edwebb.jim.model.events.MapChangeEvent;
import net.edwebb.jim.model.events.MapChangeListener;
import net.edwebb.mi.data.Feature;
import net.edwebb.mi.data.Flag;

public abstract class AbstractMapModel implements MapModel {

	protected List<MapChangeListener> mapChangeListeners = new ArrayList<MapChangeListener>();

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

	@Override
	public void setParent(MapModel model) {
	}

	@Override
	public MapModel getParent() {
		return null;
	}

	@Override
	public void recieveMapChangeEvent(MapChangeEvent event) {
	}
}
