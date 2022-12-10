package net.edwebb.jim.view;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JPanel;

import net.edwebb.jim.model.MapModel;

/**
 * A MapRuler displays a line of numbers either vertically or horizontally corresponding to the co-ordinate of the row or column of the map
 * 
 * @author Ed Webb
 *
 */
public class MapRuler extends JPanel {

	/**
	 * The version ID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The horizontal orientation
	 */
	public static final int HORIZONTAL = 1;
	
	/**
	 * The vertical orientation
	 */
	public static final int VERTICAL = 2;
	
	// The model the ruler is to use
	private MapModel model;
	
	// The orientation of the ruler
	private int orientation;
	
	// The font used to print the co-ordinate
	private Font font;
	
	// The font metrics of the font
	private FontMetrics fm;
	
	/**
	 * Create a MapRuler for the model with the given orientation
	 * @param orientation the orientation of the ruler. Either VERTICAL or HORIZONTAL
	 * @param model the model that this ruler will use
	 */
	public MapRuler(int orientation, MapModel model) {
		if (orientation != HORIZONTAL && orientation != VERTICAL) {
			throw new IllegalArgumentException("Orientation must be Ruler.HORIZONTAL (" + HORIZONTAL + ") or Ruler.VERTICAL (" + VERTICAL + ")");
		}
		if (model == null) {
			throw new IllegalArgumentException("a MapModel must be passed to this constructor");
		}
		this.orientation = orientation;
		this.model = model;
		this.setPreferredSize(new Dimension(30, 30));
		this.font = new Font("Verdana", Font.BOLD, 10);
	}

	/**
	 * Returns the orientation of this ruler
	 * @return the orientation of this ruler
	 */
	public int getOrientation() {
		return orientation;
	}
	
	/**
	 * Sets the model that this ruler will use
	 * @param model the model for the ruler
	 */
	public void setModel(MapModel model) {
		this.model = model;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (fm == null) {
			g.setFont(font);
			fm = g.getFontMetrics();
		}
		
		int x = model.getSize() / 2;
		int y = model.getSize() / 2;
		
		int p = model.getSize() / 2;
		
		int off = 0;
		
		int t = 0;
		int sqr = 0;
		int lst = 0;
		if (orientation == HORIZONTAL) {
			x += 30;
			p += 30;
			off = model.getOffset().x;
			y = this.getHeight() - 5;
			t = this.getWidth() - 16;
			sqr = model.getView().x + off;
			lst = model.getView().x + model.getView().width + 1 + off;
			lst = Math.min(lst, model.getBounds().x + model.getBounds().width + 1);
		} else {
			x = this.getWidth() - 2;
			t = this.getHeight();
			off = model.getOffset().y;
			sqr = model.getView().y + off;
			lst = model.getView().y - model.getView().height - 1 + off;
			lst = Math.max(lst,  model.getBounds().y - model.getBounds().height - 1);
		}
		while (p < t && sqr != lst) {
			String str = Integer.toString(sqr);
			if (orientation == HORIZONTAL) {
				x -= fm.getStringBounds(str, g).getWidth() / 2;
			} else {
				y += fm.getHeight() / 2;
				x = 25 - (int)fm.getStringBounds(str, g).getWidth();
			}
			g.drawString(str, x, y);
			p += model.getSize() + 1;
			if (orientation == HORIZONTAL) {
				sqr++;
				x = p;
			} else {
				sqr--;
				y = p;
			}
		}
	}
}
