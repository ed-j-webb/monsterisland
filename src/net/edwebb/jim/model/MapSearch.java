package net.edwebb.jim.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class MapSearch {

	private List<Point> found = new ArrayList<Point>(0);
	private short foundID;
	private String foundNote;
	
	public void setFoundSquares(List<Point> squares) {
		found = squares;
	}

	public List<Point> getFoundSquares() {
		return found;
	}
	
	public boolean isFound(Point square) {
		return found.contains(square);
	}

	public void setFoundID(short id) {
		this.foundID = id;
	}

	public short getFoundID() {
		return foundID;
	}

	public void setFoundNote(String note) {
		this.foundNote = note;
	}

	public String getFoundNote() {
		return foundNote;
	}
}
