package net.edwebb.jim.view;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import net.edwebb.jim.model.MapModel;
import net.edwebb.jim.model.MapSearch;

import net.edwebb.jim.view.decorator.BorderDecorator;
import net.edwebb.jim.view.decorator.Decorator;
import net.edwebb.jim.view.decorator.FeatureDecorator;
import net.edwebb.jim.view.decorator.FlagDecorator;
import net.edwebb.jim.view.decorator.NoteDecorator;
import net.edwebb.jim.view.decorator.TerrainDecorator;

public class MapPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String BORDER = "Border";
	public static final String TERRAIN = "Terrain";
	public static final String FEATURE = "Feature";
	public static final String FLAG = "Flag";
	public static final String NOTE = "Note";
	
	private Font fnt;
	
	private MapModel model;
	private MapSearch search;
	private boolean flag = true;
	
	private Map<String, Decorator> decorators = new HashMap<String, Decorator>();
	
	public MapPanel(MapModel model) {
		fnt = new Font("Arial", Font.BOLD, 9);
		setModel(model);
		decorators.put(BORDER, new BorderDecorator());
		decorators.put(TERRAIN, new TerrainDecorator());
		decorators.put(FEATURE, new FeatureDecorator());
		decorators.put(FLAG, new FlagDecorator());
		decorators.put(NOTE, new NoteDecorator());
	}
	
	public void setModel(MapModel model) {
		this.model = model;
		this.search = null;
	}
	
	public void putDecorator(String type, Decorator decorator) {
		decorators.put(type, decorator);
	}

	public void removeDecorator(String type) {
		decorators.remove(type);
	}
	
	public void setSearch(MapSearch search) {
		this.search = search;
	}

	public boolean isViewFlags() {
		return flag;
	}

	public void setViewFlags(boolean flag) {
		this.flag = flag;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setFont(fnt);
		
		int px = 1;
		int py = 1;
		for (int i = model.getView().x; i <= model.getView().x + model.getView().width; i++) {
			for (int j = model.getView().y; j >= model.getView().y - model.getView().height; j--) {
				Point sqr = new Point(i, j);
				paintSquare(g2d, px, py, model, search, sqr);
				py += model.getSize() + 1;
			}
			py = 1;
			px += model.getSize() + 1;
		}
	}

	private void paintSquare(Graphics2D g2d, int px, int py, MapModel model, MapSearch search, Point sqr) {
		
		short[] square = model.getSquare(sqr);
		if (square != null && square.length > 0) {

			if (decorators.containsKey(TERRAIN)) {
				decorators.get(TERRAIN).decorate(g2d, sqr, px, py, model, search);
			}
			
			if (decorators.containsKey(FEATURE)) {
				decorators.get(FEATURE).decorate(g2d, sqr, px, py, model, search);
			}
			
			if (isViewFlags() && decorators.containsKey(FLAG)) {
				decorators.get(FLAG).decorate(g2d, sqr, px, py, model, search);
			}
	
		}

		if (decorators.containsKey(BORDER)) {
			decorators.get(BORDER).decorate(g2d, sqr, px, py, model, search);
		}

		String note = model.getSquareNote(sqr);
		if (note != null) {
			if (decorators.containsKey(NOTE)) {
				decorators.get(NOTE).decorate(g2d, sqr, px, py, model, search);
			}
		}
		
	}
}
