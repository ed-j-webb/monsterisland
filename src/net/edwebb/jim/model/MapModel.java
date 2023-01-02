package net.edwebb.jim.model;

import java.awt.Point;
import java.awt.Rectangle;

import net.edwebb.jim.model.events.MapChangeEvent;
import net.edwebb.jim.model.events.MapChangeListener;
import net.edwebb.mi.data.Coordinate;
import net.edwebb.mi.data.Feature;
import net.edwebb.mi.data.Flag;
import net.edwebb.mi.data.Terrain;

public interface MapModel {

	public static final int BORDER = 0;
	public static final int BACKGROUND = 1;
	public static final int FOREGROUND = 2;
	public static final int NOTE = 3;
	
	public static final int INVERSE = 0;
	public static final int ON = 1;
	public static final int OFF = -1;
	
	public MapData getData();
	
	public String getName();
	public void setName(String name);
	
	public void setSize(int size);
	public int getSize();
	
	public void setBounds(Rectangle rect);
	public Rectangle getBounds();
	
	public void setView(Rectangle rect);
	public Rectangle getView();
	
	public void setSelected(Point square);
	public Point getSelected();
	public boolean isSelected(Point square);
	
	public short[] getSquare(Point square);
	public String getSquareNote(Point square);
	public boolean isWithin(Point square);
	
	public void setSquareNote(Point square, String note);
	
	public void setTerrain(Point square, Terrain terrain);
	
	public void toggleFlag(Point square, Flag flag, int state);
	public boolean isFlagged(Point square, Flag id);
	
	public void remove(Point square, Feature feature);
	public void add(Point square, Feature id);
	public boolean contains(Point square, Feature id);

	public void setDefaultCoOrdinates(Coordinate coord);
	public Coordinate getDefaultCoOrdinates();
	
	public void setCurrentCoOrdinates(Coordinate coord);
	public Coordinate getCurrentCoOrdinates();
	
	public Point getOffset();
	
	public boolean isDirty();
	
	public int getExtra(Point square, Feature feature);
	public int getExtra(Point square, Flag flag);
	public int getExtra(Point square);
	
	public void addMapChangeListener(MapChangeListener l);
	
	public void removeMapChangeListener(MapChangeListener l);

	public void setParent(MapModel model);
	public MapModel getParent();
	public void recieveMapChangeEvent(MapChangeEvent event);
}
