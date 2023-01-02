package net.edwebb.jim.model.events;

import net.edwebb.jim.model.MapModel;

public abstract class MapChangeEvent {

	public enum MAP_CHANGE_TYPE {
		SELECTED, VIEW, TERRAIN, FEATURE, FLAG, NOTE, COORDINATE 
	}

	protected MapModel model;
	protected MAP_CHANGE_TYPE changeType;
	
	protected MapChangeEvent(MapModel model, MAP_CHANGE_TYPE changeType) {
		this.model = model;
		this.changeType = changeType;
	}
	
	public MAP_CHANGE_TYPE getChangeType() {
		return changeType;
	}
	
	public MapModel getModel() {
		return model;
	}
}
