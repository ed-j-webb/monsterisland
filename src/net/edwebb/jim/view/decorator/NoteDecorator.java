package net.edwebb.jim.view.decorator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;

import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.MapSearch;

public class NoteDecorator implements Decorator {

	@Override
	public void decorate(Graphics2D g2d, Point sqr, int px, int py, MapModel model, MapSearch search) {
		if (model.getSquareNote(sqr) != null) {
			g2d.setColor(Color.RED);
			if (search != null && search.isFound(sqr) && search.getFoundID() <= 0) {
				g2d.fill(getNoteTriangle(model.getSize(), px, py, 20));
			} else {
				g2d.fill(getNoteTriangle(model.getSize(), px, py, 10));
			}
		}
	}

	private Path2D.Double getNoteTriangle(int size, int px, int py, int x) {
		Path2D.Double triangle = new Path2D.Double();
		triangle.moveTo(px + size, py + size);
		triangle.lineTo(px + size, py + size - x);
		triangle.lineTo(px + size - x, py + size);
		triangle.closePath();
		return triangle;
	}
}
