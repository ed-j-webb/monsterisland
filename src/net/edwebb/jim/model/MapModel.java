package net.edwebb.jim.model;

import java.awt.Point;
import java.awt.Rectangle;

import net.edwebb.jim.data.Coordinate;
import net.edwebb.jim.data.MapData;

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
	
	public UndoableChange setSquareNote(Point square, String note);
	
	public UndoableChange setTerrain(Point square, short id);
	
	public UndoableChange toggleFlag(Point square, short id, int state);
	public boolean isFlagged(Point square, short id);
	
	public UndoableChange remove(Point square, short id);
	public UndoableChange add(Point square, short id);
	public boolean contains(Point square, short id);

	public UndoableChange setDefaultCoOrdinates(Coordinate coord);
	public Coordinate getDefaultCoOrdinates();
	
	public UndoableChange setCurrentCoOrdinates(Coordinate coord);
	public Coordinate getCurrentCoOrdinates();
	
	public Point getOffset();
	
	public boolean isDirty();
	
	public int getExtra(Point square, short id);
}