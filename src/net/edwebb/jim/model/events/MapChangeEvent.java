package net.edwebb.jim.model.events;

import java.util.ArrayList;
import java.util.List;

import net.edwebb.jim.MapConstants.ChangeType;
import net.edwebb.jim.model.MapModel;

public abstract class MapChangeEvent {

	protected MapModel model;
	protected ChangeType changeType;
	protected List<MapChangeEvent> subEvents;
	
	protected MapChangeEvent(MapModel model, ChangeType changeType) {
		this.model = model;
		this.changeType = changeType;
	}
	
	protected MapChangeEvent(MapModel model, ChangeType changeType, List<MapChangeEvent> subEvents) {
		this.model = model;
		this.changeType = changeType;
		if (subEvents != null) {
			this.subEvents = new ArrayList<MapChangeEvent>(subEvents);
		}
	}

	public ChangeType getChangeType() {
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
