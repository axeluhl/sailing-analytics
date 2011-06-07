package com.sap.sailing.util;

/**
 * Bounds are inclusive on their south and west border and exclusive on their east and north border.
 * 
 * @author Axel Uhl
 *
 */
public interface Bounds {
    /**
     * Obtains the north-east (upper-right) point of the rectangle
     */
    Point getNE();

    /**
     * Obtains the south-west (lower-left) point of the rectangle
     */
    Point getSW();
    
    /**
     * Obtains the north-west (upper-left) point of the rectangle
     */
    Point getNW();

    /**
     * Obtains the south-east (lower-right) point of the rectangle
     */
    Point getSE();
    
    Bounds extend(Point p);
    boolean contains(Point p);
    boolean intersects(Bounds b);
    boolean hasCornerIn(Bounds b);
}
