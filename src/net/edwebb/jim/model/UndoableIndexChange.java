package net.edwebb.jim.model;

import java.awt.Point;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.edwebb.jim.data.MapIndex;
import net.edwebb.mi.data.Feature;

public class UndoableIndexChange extends UndoableChange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected MapIndex index;
	protected Point pos;
	protected String text;
	protected Feature feature;
	protected boolean add;
	
	public UndoableIndexChange(MapIndex index, Point pos, String text, boolean add) {
		this.index = index;
		this.pos = pos;
		this.text = text;
		this.add = add;
	}
	
	public UndoableIndexChange(MapIndex index, Point pos, Feature feature, boolean add) {
		this.index = index;
		this.pos = pos;
		this.feature = feature;
		this.add = add;
	}
	
	public void undo() throws CannotUndoException {
		super.undo();
		if (text != null) {
			if (add) {
				index.removeNote(text, pos);
			} else {
				index.addNote(text, pos);
			}
		}

		if (feature != null) {
			if (add) {
				index.removePoint(feature, pos);
			} else {
				index.addPoint(feature, pos);
			}
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (text != null) {
			if (add) {
				index.addNote(text, pos);
			} else {
				index.removeNote(text, pos);
			}
		}

		if (feature != null) {
			if (add) {
				index.addPoint(feature, pos);
			} else {
				index.removePoint(feature, pos);
			}
		}
	}
	
	
}
