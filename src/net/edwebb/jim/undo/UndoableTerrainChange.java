package net.edwebb.jim.undo;

import java.awt.Point;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.edwebb.jim.model.MapModel;
import net.edwebb.mi.data.Terrain;

public class UndoableTerrainChange extends UndoableMapChange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Terrain oldTerrain;
	private Terrain newTerrain;
	
	public UndoableTerrainChange(MapModel model, Point pos, Terrain oldTerrain, Terrain newTerrain) {
		super(model, pos);
		this.oldTerrain = oldTerrain;
		this.newTerrain = newTerrain;
	}
	
	public void undo() throws CannotUndoException {
		super.undo();
		model.setTerrain(pos, oldTerrain);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		model.setTerrain(pos, newTerrain);
	}
	
	public String getPresentationName() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.getPresentationName());
		sb.append(" ");
		if (oldTerrain == null) {
			sb.append("set terrain to ");
			sb.append(newTerrain);
		} else if (newTerrain == null) {
			sb.append("unset terrain from ");
			sb.append(oldTerrain);
		} else {
			sb.append("change terrain from ");
			sb.append(oldTerrain);
			sb.append(" to ");
			sb.append(newTerrain);
		}
		return sb.toString();
	}
}
