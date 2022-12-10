package net.edwebb.jim.model;

import java.awt.Point;

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

	@Override
	public String getRedoPresentationName() {
		return "Redo " + getPresentationName();
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo " + getPresentationName();
	}
}
