package net.edwebb.jim.view.decorator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Path2D;

import net.edwebb.jim.data.Feature;
import net.edwebb.jim.data.FeatureData;
import net.edwebb.jim.data.Terrain;
import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.MapSearch;

public class TerrainDecorator implements Decorator {

	public void decorate(Graphics2D g2d, Point sqr, int px, int py, MapModel model, MapSearch search) {
		short[] square = model.getSquare(sqr);
		Terrain t = FeatureData.getInstance().getTerrain(square[0]);
		if (t != null) {
			g2d.setPaint(t.getTexture());
			g2d.fillRect(px, py, model.getSize(), model.getSize());
		} else {
			g2d.setColor(Color.BLACK);
		}
		
		for(int i = 1; i < square.length; i++) {
			Feature f = FeatureData.getInstance().getFeature(square[i]);
			if (f != null && f instanceof Terrain) {
				drawTerrain(g2d, (Terrain)f, px, py, model);
			}
		}
	}

	private void drawTerrain(Graphics2D g2d, Terrain t, int px, int py, MapModel model) {
		Path2D.Double triangle = getDiffTriangle(model, px, py, model.getSize());
		Color c = g2d.getColor();
		Paint p = g2d.getPaint();
		g2d.setColor(t.getColour());
		g2d.setPaint(t.getTexture());
		g2d.fill(triangle);
		g2d.setColor(c);
		g2d.setPaint(p);
	}
	
	private Path2D.Double getDiffTriangle(MapModel model, int px, int py, int size) {
		Path2D.Double triangle = new Path2D.Double();
		triangle.moveTo(px, py + model.getSize());
		triangle.lineTo(px, py + model.getSize() - size);
		triangle.lineTo(px + size, py + model.getSize());
		triangle.closePath();
		return triangle;
	}

}
