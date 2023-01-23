package net.edwebb.jim.model.events;

import java.awt.Point;
import java.util.List;

import net.edwebb.jim.MapConstants.ChangeType;
import net.edwebb.jim.model.MapModel;
import net.edwebb.mi.data.Flag;

public class FlagChangeEvent extends MapSquareChangeEvent {

	protected Flag flag;
	protected boolean state;
	
	public FlagChangeEvent(MapModel model, Point square, Flag flag, boolean state) {
		this(model, square, flag, state, null);
	}
	public FlagChangeEvent(MapModel model, Point square, Flag flag, boolean state, List<MapChangeEvent> subEvents) {
		super(model, ChangeType.FLAG, square, subEvents);
		this.flag = flag;
		this.state = state;
	}

	public Flag getFlag() {
		return flag;
	}

	public boolean isSet() {
		return state;
	}

	@Override
	public String toString() {
		return "(" + square.y + "," + square.x + ") flag " + flag + " set " + state;
	}
	
	
}
