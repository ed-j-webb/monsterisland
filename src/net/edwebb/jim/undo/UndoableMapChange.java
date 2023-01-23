package net.edwebb.jim.undo;

import java.awt.Point;

import net.edwebb.jim.MapConstants.ChangeType;
import net.edwebb.jim.model.MapModel;

public abstract class UndoableMapChange extends UndoableChange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected MapModel model;
	protected Point square;
	
	public UndoableMapChange(MapModel model, Point square, ChangeType changeType) {
		super(changeType);
		this.model = model;
		if (square != null) {
			this.square = new Point(square);
		}
	}
	
	public MapModel getModel() {
		return model;
	}
	
	public Point getSquare() {
		return square;
	}
	
	public String getPresentationName() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(square.y);
		sb.append(",");
		sb.append(square.x);
		sb.append(")");
		return sb.toString();
	}
}
