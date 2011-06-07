package com.sap.sailing.util;

/**
 * When a node needs to be split into several children, the splitter tells which
 * of the nodes an object of type <tt>T</tt> really intersects with.
 * 
 * @author Axel Uhl
 */
public interface Splitter<T> {
    boolean intersects(T leaf, Bounds bounds);
    double getDistance(Point point, T leaf);
}
