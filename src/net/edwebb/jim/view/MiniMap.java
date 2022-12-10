package net.edwebb.jim.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.edwebb.jim.data.FeatureData;
import net.edwebb.jim.data.MapIndex;
import net.edwebb.jim.data.Terrain;
import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.MapSearch;
import net.edwebb.jim.model.UndoableChange;

public class MiniMap extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final Color PRIMARY = Color.MAGENTA;
	public static final Color SECONDARY = Color.MAGENTA;
	public static final Color[] colours = new Color[] {null, null, PRIMARY};
	
	private BufferedImage img;
	private MapModel model;
	private MapSearch search;
	private MapIndex index;
	
	public MiniMap(MapModel model) {
		setModel(model);
	}
	
	public void setModel(MapModel model) {
		this.model = model;
		this.search = null;
		Rectangle bounds = model.getBounds();
		if (img != null) {
			img.flush();
		}
		img = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = img.getGraphics();
		Point p = new Point();
		for (int x = bounds.x;  x < bounds.width + bounds.x; x++) {
			for (int y = bounds.y; y > bounds.y - bounds.height; y--) {
				p.move(x, y);
				short[] sqr = model.getSquare(p);
				if (sqr != null && sqr.length > 0) {
					Terrain t = FeatureData.getInstance().getTerrain(sqr[0]);
					if (t != null) {
						g.setColor(t.getColour());
						g.drawLine(p.x - bounds.x, bounds.y - p.y, p.x - bounds.x, bounds.y - p.y);
					}
				}
			}
		}
		Dimension dim = new Dimension(bounds.width, bounds.height);
		this.setMinimumSize(dim);
		this.setPreferredSize(dim);
	}
	
	public void setSearch(MapSearch search) {
		this.search = search;
	}
	
	public void setIndex(MapIndex index) {
		this.index = index;
	}
	
	public UndoableChange setPoint(Point p, Terrain t) {
		
		short[] square = model.getSquare(p);
		Color oldColor = null;
		if (square == null || square.length == 0) {
			oldColor = Color.BLACK;
		} else {
			oldColor = FeatureData.getInstance().getTerrain(square[0]).getColour();
		}
		
		Rectangle bounds = model.getBounds();
		Graphics g = img.getGraphics();
		if (t == null) {
			g.setColor(Color.BLACK);
		} else {
			g.setColor(t.getColour());
		}
		g.drawLine(p.x - bounds.x, bounds.y - p.y, p.x - bounds.x, bounds.y - p.y);

		if (t == null) {
			return new UndoableMiniMapChange(p, oldColor, Color.BLACK);
		} else {
			return new UndoableMiniMapChange(p, oldColor, t.getColour());
		}
	}
	
	@Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(img, 0, 0, null);
        Rectangle bounds = model.getBounds();

        if (index != null) {
            for (int i = 1; i < 3; i++) {
            	if ( colours[i] != null) { 
	            	g.setColor(colours[i]);
	            	Iterator<Point> it = index.getPoints(Integer.valueOf(i)).iterator();
	            	while (it.hasNext()) {
	            		Point p = it.next();
	            		g.fillRect((p.x - bounds.x) - 1, (bounds.y - p.y) - 1, 3, 3);
	            	}
            	}
        	}
        }
        
        g.setColor(Color.RED);
        if (search != null) {
	        Iterator<Point> it = search.getFoundSquares().iterator();
	        while (it.hasNext()) {
	        	Point p = it.next();
				g.fillRect((p.x - bounds.x) - 1, (bounds.y - p.y) - 1, 3, 3);
	        }
        }

        g.setColor(Color.RED);
        Rectangle view = model.getView();
        g.drawRect(view.x - bounds.x, bounds.y - view.y, view.width, view.height);
	
	}
	
	public class UndoableMiniMapChange extends UndoableChange {
		private Point pos;
		private Color oldColour; 
		private Color newColour;
		
		public UndoableMiniMapChange(Point pos, Color oldColour, Color newColour) {
			super();
			this.pos = pos;
			this.oldColour = oldColour;
			this.newColour = newColour;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			Rectangle bounds = model.getBounds();
			Graphics g = img.getGraphics();
			g.setColor(oldColour);
			g.drawLine(pos.x - bounds.x, bounds.y - pos.y, pos.x - bounds.x, bounds.y - pos.y);
		}
		
		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			Rectangle bounds = model.getBounds();
			Graphics g = img.getGraphics();
			g.setColor(newColour);
			g.drawLine(pos.x - bounds.x, bounds.y - pos.y, pos.x - bounds.x, bounds.y - pos.y);
		}

		
	}
}
