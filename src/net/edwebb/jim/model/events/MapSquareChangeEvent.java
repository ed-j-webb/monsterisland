package net.edwebb.jim.model.events;

import java.awt.Point;
import java.util.List;

import net.edwebb.jim.MapConstants.ChangeType;
import net.edwebb.jim.model.MapModel;

public class MapSquareChangeEvent extends MapChangeEvent {

	protected Point square;

	public MapSquareChangeEvent(MapModel model, ChangeType changeType, Point square) {
		this(model, changeType, square, null);
	}
	
	public MapSquareChangeEvent(MapModel model, ChangeType changeType, Point square, List<MapChangeEvent> subEvents) {
		super(model, changeType, subEvents);
		this.square = square;
	}

	public Point getSquare() {
		return square;
	}
	
}
