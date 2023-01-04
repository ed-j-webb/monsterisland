package net.edwebb.jim.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.edwebb.jim.model.MapModel;
import net.edwebb.mi.data.Coordinate;

public class UndoableCoordinateChange extends UndoableMapChange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Coordinate oldCoord;
	private Coordinate newCoord;
	private boolean current;
	
	public UndoableCoordinateChange(MapModel model, Coordinate oldCoord, Coordinate newCoord, boolean current) {
		super(model, null);
		this.oldCoord = oldCoord;
		this.newCoord = newCoord;
		this.current = current;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (current) {
			model.setCurrentCoOrdinates(newCoord);
		} else {
			model.setDefaultCoOrdinates(newCoord);
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (current) {
			model.setCurrentCoOrdinates(oldCoord);
		} else {
			model.setDefaultCoOrdinates(oldCoord);
		}
	}
	
	public String getPresentationName() {
		StringBuilder sb = new StringBuilder();
		if (oldCoord == null) {
			sb.append("Set coordinates to ");
			sb.append(newCoord);
		} else if (newCoord == null) {
			sb.append("Unset coordinates from ");
			sb.append(oldCoord);
		} else {
			sb.append("Change coordinates from ");
			sb.append(oldCoord);
			sb.append(" to ");
			sb.append(newCoord);
		}
		return sb.toString();
	}
}
