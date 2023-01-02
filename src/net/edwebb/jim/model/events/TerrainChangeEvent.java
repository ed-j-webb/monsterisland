package net.edwebb.jim.model.events;

import java.awt.Point;

import net.edwebb.jim.model.MapModel;
import net.edwebb.mi.data.Terrain;

public class TerrainChangeEvent extends MapSquareChangeEvent {

	protected Terrain oldTerrain;
	protected Terrain newTerrain;
	
	public TerrainChangeEvent(MapModel model, Point square, Terrain oldTerrain, Terrain newTerrain) {
		super(model, MAP_CHANGE_TYPE.TERRAIN, square);
		this.oldTerrain = oldTerrain;
		this.newTerrain = newTerrain;
	}

	public Terrain getOldTerrain() {
		return oldTerrain;
	}

	public Terrain getNewTerrain() {
		return newTerrain;
	}

	@Override
	public String toString() {
		return "(" + square.y + "," + square.x + ") terrain changed from " + oldTerrain + " to " + newTerrain;
	}
}
