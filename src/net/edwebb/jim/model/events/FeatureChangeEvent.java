package net.edwebb.jim.model.events;

import java.awt.Point;

import net.edwebb.jim.model.MapModel;
import net.edwebb.mi.data.Feature;

public class FeatureChangeEvent extends MapSquareChangeEvent {

	protected Feature feature;
	protected boolean added;
	
	public FeatureChangeEvent(MapModel model, Point square, Feature feature, boolean added) {
		super(model, MAP_CHANGE_TYPE.FEATURE, square);
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
