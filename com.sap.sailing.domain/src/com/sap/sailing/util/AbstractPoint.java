package com.sap.sailing.util;

/**
 * Provides canonical equals and hashCode implementation for points.
 * 
 * @author Axel Uhl
 *
 */
public abstract class AbstractPoint implements Point {
    public boolean equals(Object o) {
	return getX() == ((Point) o).getX() && getY() == ((Point) o).getY();
    }
    public int hashCode() {
	return 94837 ^ getX() ^ getY();
    }
}
