package net.edwebb.jim.model.events;

import java.awt.Point;
import java.util.List;

import net.edwebb.jim.model.MapModel;
import net.edwebb.mi.data.Feature;

public class FeatureChangeEvent extends MapSquareChangeEvent {

	protected Feature feature;
	protected boolean added;
	
	public FeatureChangeEvent(MapModel model, Point square, Feature feature, boolean added) {
		this(model, square, feature, added, null);
	}

	public FeatureChangeEvent(MapModel model, Point square, Feature feature, boolean added, List<MapChangeEvent> subEvents) {

		super(model, MAP_CHANGE_TYPE.FEATURE, square, subEvents);
		this.feature = feature;
		this.added = added;
	}

	public Feature getFeature() {
		return feature;
	}

	public boolean isAdded() {
		return added;
	}

	@Override
	public String toString() {
		return "(" + square.y + "," + square.x + ") " + feature + " " + (added ? "added" : "removed");
	}
}
