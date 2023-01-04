package net.edwebb.jim.undo;

import java.awt.Point;

import net.edwebb.jim.model.MapModel;

public abstract class UndoableMapChange extends UndoableChange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected MapModel model;
	protected Point pos;
	
	public UndoableMapChange(MapModel model, Point pos) {
		super();
		this.model = model;
		if (pos != null) {
			this.pos = new Point(pos);
		}
	}
	
	public String getPresentationName() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(pos.y);
		sb.append(",");
		sb.append(pos.x);
		sb.append(")");
		return sb.toString();
	}
}
