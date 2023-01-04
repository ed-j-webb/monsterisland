package net.edwebb.jim.undo;

import java.awt.Point;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.edwebb.jim.model.MapModel;
import net.edwebb.mi.data.Flag;

public class UndoableFlagChange extends UndoableMapChange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Flag flag;
	private boolean state;
	
	public UndoableFlagChange(MapModel model, Point pos, Flag flag, boolean state) {
		super(model, pos);
		this.flag = flag;
		this.state = state;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		model.toggleFlag(pos, flag, state ? 1 : -1);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		model.toggleFlag(pos, flag, state ? -1 : 1);
	}
	
	public String getPresentationName() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.getPresentationName());
		sb.append(" ");
		sb.append( state ? "set": "unset");
		sb.append(" ");
		sb.append(flag);
		return sb.toString();
	}
}
