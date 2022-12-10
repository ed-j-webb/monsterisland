package net.edwebb.jim.view.decorator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import net.edwebb.jim.data.Decoder;
import net.edwebb.jim.data.Feature;
import net.edwebb.jim.data.FeatureData;
import net.edwebb.jim.data.Location;
import net.edwebb.jim.data.Terrain;
import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.MapSearch;

public class FeatureDecorator implements Decorator {

	public static final Color PRIMARY = Color.MAGENTA;
	public static final Color SECONDARY = Color.PINK;
	
	public static final Color[] colours = new Color[] {null, PRIMARY, SECONDARY};
	
	@Override
	public void decorate(Graphics2D g2d, Point sqr, int px, int py,  MapModel model, MapSearch search) {
		int size = model.getSize();
		short[] square = model.getSquare(sqr);
		Terrain t = FeatureData.getInstance().getTerrain(square[0]);
		int ox = 0;
		int oy = 0;
		for(int i = 1; i < square.length; i++) {
			Feature f = FeatureData.getInstance().getFeature(square[i]);
			if (f != null) {
				int extra = model.getExtra(sqr, f.getId());
				if (ox < size && oy < size) {
					if (f instanceof Location) {
						drawLocation(g2d, f, t, px + ox, py + oy, model, search, extra);
						if (ox + 52 <= size) {
							ox += 26;
						} else {
							ox = 0;
							if (oy + 52 <= size) {
								oy += 26;
							} else {
								ox = size;
								oy = size;
							}
						}
					} else if (f instanceof Terrain) {
						// Ignore terrain
					} else {
						drawOther(g2d, f, t, px + ox, py + oy, model, search, extra);
						if (oy % 2 == 0) {
							oy += 13;
						} else if (ox + 52 <= size) {
							ox += 26;
							oy -= 13;
						} else {
							ox = 0;
							if (oy + 26 <= size) {
								oy += 13;
							} else {
								ox = size;
								oy = size;
							}
						}
					}
				}
			} else {
				// It may be a terrain with flags
				f = FeatureData.getInstance().getFeature(Decoder.shortLowByte(square[i]));
				if (f == null) {
					System.out.println(square[i] + " (" + Decoder.stringFromShort(square[i]) + ") is not valid");
				}
			}
		}
	}

	private void drawOther(Graphics2D g2d, Feature f, Terrain t, int px, int py, MapModel model, MapSearch search, int extra) {
		Rectangle2D r  = g2d.getFontMetrics().getStringBounds(f.getCode(), g2d);
		int ht = g2d.getFontMetrics().getHeight();

		if (search != null && search.getFoundID() == f.getId()) {
			g2d.setColor(Color.RED);
			g2d.fillRect(px + 2, py, (int) r.getWidth(), (int) r.getHeight());
			g2d.setColor(Color.WHITE);
		} else if (extra > 0) {
			g2d.setColor(colours[extra]);
			g2d.fillRect(px + 2, py, (int) r.getWidth(), (int) r.getHeight());
			g2d.setColor(Color.WHITE);
		} else {
			if (t != null && t.getColour() != null) {
				g2d.setColor(t.getColour());
				g2d.fillRect(px + 2, py, (int) r.getWidth(), (int) r.getHeight());						
				g2d.setColor(t.getText());
			}
		}
		g2d.drawString(f.getCode(), px + 2, py + ht - 3);
	}

	private void drawLocation(Graphics2D g2d, Feature f, Terrain t, int px, int py, MapModel model, MapSearch search, int extra) {
		if (search != null && search.getFoundID() == f.getId()) {
			g2d.setColor(Color.RED);
		} else if (extra > 0) {
			g2d.setColor(colours[extra]);
		} else if (t != null && t.getColour() != null) {
			g2d.setColor(t.getColour());
		} else {
			g2d.setColor(Color.WHITE);
		}
		g2d.fillRect(px, py, f.getIcon().getImage().getWidth(null), f.getIcon().getImage().getHeight(null));						
		g2d.drawImage(f.getIcon().getImage(), px, py, null);
	}


}
