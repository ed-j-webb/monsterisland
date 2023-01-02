package net.edwebb.mi.data;

import java.awt.Point;

/**
 * A named point that is the offset from an arbitrary point to the South-West of Monster Island
 * 
 * @author Ed Webb
 *
 */
public class Coordinate {
	
	// The offset
	private Point offset;
	
	// Its name
	private String name;

	/**
	 * Creates a new Coordinate with the given offset and name
	 * @param offset the offset
	 * @param name the name
	 */
	public Coordinate(Point offset, String name) {
		this.offset = new Point(offset);
		this.name = name;
	}

	/**
	 * Returns the offset of the coordinate
	 * @return  the offset of the coordinate
	 */
	public Point getOffset() {
		return new Point(offset);
	}

	/**
	 * Returns the name of the coordinate
	 * @return the name of the coordinate
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name + " (" + offset.y + ", " + offset.x + ")";
	}

	/**
	 * Returns the difference between the two coordinates
	 * @param coord the coordinate to compare
	 * @return the offset from this coordinate to the given coordinate
	 */
	public Point getOffset(Coordinate coord) {
		if (coord == null) {
			return new Point(0, 0);
		} else {
			Point p = getOffset();
			p.translate(-coord.getOffset().x, -coord.getOffset().y);
			return p;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((offset == null) ? 0 : offset.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coordinate other = (Coordinate) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (offset == null) {
			if (other.offset != null)
				return false;
		} else if (!offset.equals(other.offset))
			return false;
		return true;
	}
}
