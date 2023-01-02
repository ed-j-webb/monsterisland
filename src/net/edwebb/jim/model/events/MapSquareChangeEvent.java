package net.edwebb.jim.model.events;

import java.awt.Point;

import net.edwebb.jim.model.MapModel;

public class MapSquareChangeEvent extends MapChangeEvent {

	protected Point square;

	public MapSquareChangeEvent(MapModel model, MAP_CHANGE_TYPE changeType, Point square) {
		super(model, changeType);
		this.square = square;
	}
	
	public Point getSquare() {
		return square;
	}
	
}
