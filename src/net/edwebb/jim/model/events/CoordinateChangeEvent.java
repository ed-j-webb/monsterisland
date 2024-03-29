package net.edwebb.jim.model.events;

import java.util.List;

import net.edwebb.jim.MapConstants.ChangeType;
import net.edwebb.jim.model.MapModel;
import net.edwebb.mi.data.Coordinate;

public class CoordinateChangeEvent extends MapChangeEvent {

	protected Coordinate oldCoord;
	protected Coordinate newCoord;
	protected boolean defaultCoord;
	
	public CoordinateChangeEvent(MapModel model, Coordinate oldCoord, Coordinate newCoord, boolean defaultCoord) {
		this(model, oldCoord, newCoord, defaultCoord, null);
	}
	public CoordinateChangeEvent(MapModel model, Coordinate oldCoord, Coordinate newCoord, boolean defaultCoord, List<MapChangeEvent> subEvents) {
		super(model, ChangeType.COORDINATE, subEvents);
		this.oldCoord = oldCoord;
		this.newCoord = newCoord;
		this.defaultCoord = defaultCoord;
	}

	public Coordinate getOldCoord() {
		return oldCoord;
	}

	public Coordinate getNewCoord() {
		return newCoord;
	}

	public boolean isDefaultCoord() {
		return defaultCoord;
	}

	@Override
	public String toString() {
		if (oldCoord == null) {
			return (defaultCoord ? "Default" : "Current") + " co-ordinate set to " + newCoord;
		} else if (newCoord == null) {
			return (defaultCoord ? "Default" : "Current") + " co-ordinate unset from " + oldCoord;
		} else {
			return (defaultCoord ? "Default" : "Current") + " co-ordinate changed from " + oldCoord + " to " + newCoord;
		}
	}
}
