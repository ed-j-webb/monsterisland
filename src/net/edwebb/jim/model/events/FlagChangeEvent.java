package net.edwebb.jim.model.events;

import java.awt.Point;

import net.edwebb.jim.model.MapModel;
import net.edwebb.mi.data.Flag;

public class FlagChangeEvent extends MapSquareChangeEvent {

	protected Flag flag;
	protected boolean state;
	
	public FlagChangeEvent(MapModel model, Point square, Flag flag, boolean state) {
		super(model, MAP_CHANGE_TYPE.FLAG, square);
		this.flag = flag;
		this.state = state;
	}

	public Flag getFlag() {
		return flag;
	}

	public boolean getState() {
		return state;
	}

	@Override
	public String toString() {
		return "(" + square.y + "," + square.x + ") flag " + flag + " set " + state;
	}
	
	
}
