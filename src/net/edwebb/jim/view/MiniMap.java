package net.edwebb.jim.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import javax.swing.JPanel;

import net.edwebb.jim.MapConstants.ChangeType;
import net.edwebb.jim.model.DiffMapModel;
import net.edwebb.jim.model.MapIndex;
import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.MapSearch;
import net.edwebb.jim.model.events.MapChangeEvent;
import net.edwebb.jim.model.events.MapChangeListener;
import net.edwebb.jim.model.events.TerrainChangeEvent;
import net.edwebb.jim.model.events.ViewChangeEvent;
import net.edwebb.mi.data.DataStore;
import net.edwebb.mi.data.Terrain;

public class MiniMap extends JPanel implements MapChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final Color PRIMARY = Color.MAGENTA;
	public static final Color SECONDARY = Color.PINK;
	public static final Color[] colours = new Color[] {null, SECONDARY, PRIMARY};
	
	private BufferedImage img;
	private MapModel model;
	private MapSearch search;
	private MapIndex index;
	
	public MiniMap(MapModel model) {
		setModel(model);
	}
	
	public void setModel(MapModel model) {
		if (this.model != null) {
			this.model.removeMapChangeListener(this);
		}
		this.model = model;
		model.addMapChangeListener(this);
		
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
					Terrain t = DataStore.getInstance().getTerrain(sqr[0]);
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
	
	@Override
	public void mapChanged(MapChangeEvent event) {
		if (event.getChangeType().equals(ChangeType.VIEW)) {
			ViewChangeEvent viewEvent = (ViewChangeEvent)event;
			if (viewEvent.getNewView() != null) {
				repaint();
			}
			return;
		}
		
		if (event.getChangeType().equals(ChangeType.TERRAIN)) {
			TerrainChangeEvent terrainEvent = (TerrainChangeEvent)event;
			setPoint(terrainEvent.getSquare(), terrainEvent.getNewTerrain());
			repaint();
		}
		
		if (model instanceof DiffMapModel) {
			repaint();
		}
		
	}

	public void setSearch(MapSearch search) {
		this.search = search;
	}
	
	public void setIndex(MapIndex index) {
		this.index = index;
	}
	
	private void setPoint(Point p, Terrain t) {
		Rectangle bounds = model.getBounds();
		Graphics g = img.getGraphics();
		if (t == null) {
			g.setColor(Color.BLACK);
		} else {
			g.setColor(t.getColour());
		}
		g.drawLine(p.x - bounds.x, bounds.y - p.y, p.x - bounds.x, bounds.y - p.y);
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
}
