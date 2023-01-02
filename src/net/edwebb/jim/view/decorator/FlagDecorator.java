package net.edwebb.jim.view.decorator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Iterator;

import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.MapSearch;
import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.data.Flag;

public class FlagDecorator implements Decorator {

	public static final Color PRIMARY = Color.MAGENTA;
	public static final Color SECONDARY = Color.PINK;
	
	public static final Color[] colours = new Color[] {null, PRIMARY, SECONDARY};
	
	@Override
	public void decorate(Graphics2D g2d, Point sqr, int px, int py, MapModel model, MapSearch search) {
		int size = model.getSize();
		int ox = 0;
		int oy = size - 13;
		Iterator<Flag> flags = DataStore.getInstance().getFlags().iterator();
		while (flags.hasNext()) {
			Flag flag = flags.next();
			if (model.isFlagged(sqr, flag)) {
				if (model.getExtra(sqr, flag) != 0) {
					g2d.setColor(colours[model.getExtra(sqr, flag)]);
					g2d.fillRect(px + ox, py + oy, 13, 13);
					g2d.setColor(Color.BLACK);
				}
				g2d.drawImage(flag.getIcon().getImage(), px + ox, py + oy, null);
				ox += 13;
				if (ox >= size) {
					ox = 0;
					oy -= 13;
				}
				if (oy < 0) {
					break;
				}
			}
		}
	}
}
