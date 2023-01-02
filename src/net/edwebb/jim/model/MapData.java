package net.edwebb.jim.model;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Holds information about the map squares
 * @author Ed Webb
 *
 */
public class MapData {

	// The dimensions of the map and its top left square
	private int top;
	private int left;
	private int width;
	private int height;
	private int selx;
	private int sely;
	
	private boolean dirty = false;
	
	// The array of squares
	private short[][][] data;
	
	// The map of notes
	private Map<Point,String> notes;
	
	private int offx;
	private int offy;
	private String offset;
	
	// Load an empty map
	public MapData() {
		this(0, 0, 100, 100);
	}

	public MapData(int top, int left, int width, int height) {
		this(top, left, width, height, top, left);
	}
	
	public MapData(int width, int height) {
		this(0, 0, width, height, 0, 0);
	}
	
	public MapData(int top, int left, int width, int height, int y, int x) {
		this.top = top;
		this.left = left;
		this.width = width;
		this.height = height;
		this.sely = y;
		this.selx = x;
		this.data = new short[width][height][];
		notes = new HashMap<Point,String>();
	}

	public void setTopLeft(Point p) {
		top = p.y;
		left = p.x;
	}
	
	public short[] getSquare(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return null;
		}
		return data[x][y];
	}
	
	public void setSquare(int x, int y, short[] sqr) {
		data[x][y] = sqr;
		dirty = true;
	}
	
	public String getSquareNotes(int x, int y) {
		return notes.get(new Point(x, y));
	}
	
	public void setSquareNotes(int x, int y, String note) {
		if (note == null || note.trim().length() == 0) {
			notes.remove(new Point(x, y));
		} else {
			notes.put(new Point(x, y), note);
		}
		dirty = true;
	}
	
	public Iterator<Map.Entry<Point, String>> getMapNotesIterator() {
		return notes.entrySet().iterator();
	}
	
	public short[][][] getArea(int x, int y, int width, int height) {
		short[][][] area = new short[width][height][];
		for (int i = x; i < x + width; i++) {
			for (int j = y; j < y + height; j++) {
				area[i-x][j-y] = data[i][j];
			}
		}
		return area;
	}
	
	public int getTop() {
		return top;
	}

	public int getLeft() {
		return left;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getX() {
		return selx;
	}

	public void setX(int x) {
		this.selx = x;
		dirty = true;
	}

	public int getY() {
		return sely;
	}

	public void setY(int y) {
		this.sely = y;
		dirty = true;
	}

	public int getOffX() {
		return offx;
	}

	public int getOffY() {
		return offy;
	}

	public String getOffset() {
		return offset;
	}

	public void setOffset(int x, int y, String offset) {
		this.offset = offset;
		dirty = true;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	
}
