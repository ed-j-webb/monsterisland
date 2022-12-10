package net.edwebb.jim.view.decorator;

import java.awt.Graphics2D;
import java.awt.Point;

import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.MapSearch;

/**
 * An interface for decorating a MapPanel
 * @author edw
 *
 */
public interface Decorator {
	public void decorate(Graphics2D g2d, Point sqr, int px, int py, MapModel model, MapSearch search);
}
