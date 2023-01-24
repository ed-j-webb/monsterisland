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

	// The dimensions of the map
	private int top;
	private int left;
	private int width;
	private int height;
	
	// The top left corner
	private int selx;
	private int sely;
	
	// the used portion of the map (most *-erly square)
	private int north;
	private int east;
	private int south;
	private int west;
	
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

		this.north = top - height;
		this.east = left;
		this.south = top; 
		this.west = left + width;

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
		setUsed(x, y);
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
		setUsed(x, y);
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
		this.offx = x;
		this.offy = y;
		dirty = true;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	private void setUsed(int x, int y) {
		if (x + left < west) {
			west = x + left;
		}
		if (x + left > east) {
			east = x + left;
		}
		if (top - y > north) {
			north = top - y;
		}
		if (top - y < south) {
			south = top - y;
		}
	}

	public int getNorth() {
		return north;
	}

	public int getEast() {
		return east;
	}

	public int getSouth() {
		return south;
	}

	public int getWest() {
		return west;
	}
	
	private void copy(MapData from, MapData to) {
		int uleft = Math.max(from.getWest(), to.getLeft());
		int uright = Math.min(from.getEast(), to.getLeft() + to.getWidth());
		int utop = Math.min(from.getNorth(), to.getTop());
		int ubottom = Math.max(from.getSouth(), to.getTop() - to.getHeight());
		
		for (int posx = uleft; posx <= uright; posx++) {
			for (int posy = utop; posy >= ubottom; posy--) {
				short[] sqr = from.getSquare(posx - from.left, from.top - posy);
				if (sqr != null) {
					to.setSquare(posx - to.left, to.top - posy, sqr);
				}
				String note = from.getSquareNotes(posx - from.left, from.top - posy);
				if (note != null) {
					to.setSquareNotes(posx - to.left, to.top - posy, note);
				}
			}
		}
	}
	
	public MapData resize(int top, int left, int width, int height) {
		MapData to = new MapData(top, left, width, height);
		to.setOffset(this.offx, this.offy, this.offset);
		copy(this, to);
		return to;
	}
	
}
