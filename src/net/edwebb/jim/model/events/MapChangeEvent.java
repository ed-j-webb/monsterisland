package net.edwebb.jim.model.events;

import java.util.ArrayList;
import java.util.List;

import net.edwebb.jim.model.MapModel;

public abstract class MapChangeEvent {

	public enum MAP_CHANGE_TYPE {
		SELECTED, VIEW, TERRAIN, FEATURE, FLAG, NOTE, COORDINATE 
	}

	protected MapModel model;
	protected MAP_CHANGE_TYPE changeType;
	protected List<MapChangeEvent> subEvents;
	
	protected MapChangeEvent(MapModel model, MAP_CHANGE_TYPE changeType) {
		this.model = model;
		this.changeType = changeType;
	}
	
	protected MapChangeEvent(MapModel model, MAP_CHANGE_TYPE changeType, List<MapChangeEvent> subEvents) {
		this.model = model;
		this.changeType = changeType;
		if (subEvents != null) {
			this.subEvents = new ArrayList<MapChangeEvent>(subEvents);
		}
	}

	public MAP_CHANGE_TYPE getChangeType() {
		return changeType;
	}
	
	public MapModel getModel() {
		return model;
	}
	
	public boolean hasSubEvents() {
		return subEvents != null && !subEvents.isEmpty();
	}
	
	public List<MapChangeEvent> getSubEvents() {
		return subEvents;
	}
}
