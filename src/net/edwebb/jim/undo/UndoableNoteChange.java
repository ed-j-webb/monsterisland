package net.edwebb.jim.undo;

import java.awt.Point;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.edwebb.jim.model.MapModel;

public class UndoableNoteChange extends UndoableMapChange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String oldNote;
	private String newNote;
	
	public UndoableNoteChange(MapModel model, Point pos, String oldNote, String newNote) {
		super(model, pos);
		this.oldNote = oldNote;
		this.newNote = newNote;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		model.setSquareNote(pos, newNote);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		model.setSquareNote(pos, oldNote);
	}
	
	public String getPresentationName() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.getPresentationName());
		sb.append(" ");
		if (oldNote == null) {
			sb.append("add note '");
			sb.append(newNote);
			sb.append("'");
		} else if (newNote == null) {
			sb.append("remove note '");
			sb.append(oldNote);
			sb.append("'");
		} else {
			sb.append("change note from '");
			sb.append(oldNote);
			sb.append("' to '");
			sb.append(newNote);
			sb.append("'");
		}
		return sb.toString();
	}
}
