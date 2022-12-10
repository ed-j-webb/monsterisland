package net.edwebb.jim.view.decorator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.MapSearch;

public class BorderDecorator implements Decorator {

	public static final Color PRIMARY = Color.MAGENTA;
	public static final Color SECONDARY = Color.PINK;
	public static final Color[] colours = new Color[] {null, SECONDARY, PRIMARY};
	
	@Override
	public void decorate(Graphics2D g2d, Point sqr, int px, int py, MapModel model, MapSearch search) {
		if (model.isSelected(sqr)) {
			g2d.setColor(Color.RED);
			g2d.drawRect(px - 1, py - 1, model.getSize() + 1, model.getSize() + 1);
		}
		int extra = model.getExtra(sqr, Short.MIN_VALUE);
		if (extra != 0) {
			g2d.setColor(colours[extra]);
			g2d.drawRect(px, py, model.getSize() - 1, model.getSize() - 1);
			g2d.drawRect(px + 1, py + 1, model.getSize() - 3, model.getSize() - 3);
		}
	}
}
