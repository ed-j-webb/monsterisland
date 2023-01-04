package net.edwebb.jim.undo;

import java.awt.Point;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.edwebb.jim.model.MapModel;
import net.edwebb.mi.data.Feature;

public class UndoableFeatureChange extends UndoableMapChange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Feature feature;
	private boolean added;
	
	public UndoableFeatureChange(MapModel model, Point pos, Feature feature, boolean added) {
		super(model, pos);
		this.feature = feature;
		this.added = added;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (added) {
			model.add(pos, feature);
		} else {
			model.remove(pos, feature);
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (added) {
			model.remove(pos, feature);
		} else {
			model.add(pos, feature);
		}
	}
	
	public String getPresentationName() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.getPresentationName());
		sb.append(" ");
		sb.append( added ? "add": "remove");
		sb.append(" ");
		sb.append(feature);
		return sb.toString();
	}
}
