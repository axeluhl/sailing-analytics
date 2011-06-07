package com.sap.sailing.util;

import java.util.Collection;
import java.util.Vector;


/**
 * Instead of inserting objects by points like the {@link QuadTree}, this structure allows clients
 * to insert objects that have rectangular bounds associated with them. An object may end up in
 * multiple cells, depending on its bounds.
 * <p>
 * 
 * The actual object's boundaries don't have to be rectangular. This matters upon splitting
 * quad tree cells, where objects contained in a cell need to be assigned to one or more
 * child nodes. For example, a rectangle that spans the original bounds of the whole cell will
 * be assigned to all children resulting from the split. A line that crosses at the top from
 * left to right will only end up in the topmost cells after the split. Were the objects
 * really rectangular in shape, then the splits would not lower the average number of objects
 * per cell and therefore not pay off. Therefore, this quad tree needs to be initialized with
 * a {@link Splitter} implementation that tells which of a given set of cells an object really
 * intersects.<p> 
 * 
 * The original use case for this structure was the management of polyline segments for hit
 * detection. It turns out to be useless to keep the polyline's vertices in a regular quad tree
 * because it are the segments that are relevant for hit detection. Segments may span multiple cells
 * in the quad tree. The cells in between need to be considered too.<p>
 * 
 * Note, that currently this class does <em>not</em> work for "degenerated" sizes that cover the
 * whole earth. Particularly, any setup that includes or comes close to one of the poles is prone to fail
 * because the pixel rectangle to which it is projected is too small.
 * 
 * @param <S> the type for the splitter arguments; <tt>T</tt> must conform to this type
 * @param <T> the type of nodes to be stored in the tree
 * 
 * @author Axel Uhl
 * 
 */
public class QuadTreeForBounds<S, T extends S> {
//    private static final long serialVersionUID = 3159925788549149044L;
//
//    private QuadTreeNodeForBounds<S, T> top;
//
//    /**
//     * When a node needs to be split into several children, the splitter tells which
//     * of the nodes an object of type <tt>T</tt> really intersects with. The splitter needs to
//     * be able to take something of <tt>T</tt> but may well be a splitter for something of a supertype
//     * of <tt>T</tt> because it uses the template arg only as argument but never as return type.
//     */
//    private Splitter<S> splitter;
//    
//    public QuadTreeForBounds(Splitter<S> splitter) {
//	this(new BoundsImpl(new PointImpl(-Integer.MAX_VALUE, -Integer.MAX_VALUE), new PointImpl(Integer.MAX_VALUE, Integer.MAX_VALUE)), 20,
//		QuadTreeNodeForBounds.NO_MIN_SIZE, splitter);
//    }
//
//    public QuadTreeForBounds(Bounds bounds, int maxItems, Splitter<S> splitter) {
//	this(bounds, maxItems, QuadTreeNodeForBounds.NO_MIN_SIZE, splitter);
//    }
//
//    public QuadTreeForBounds(Bounds bounds, int maxItems, int minSize, Splitter<S> splitter) {
//	this.splitter = splitter;
//	top = new QuadTreeNodeForBounds<S, T>(bounds, maxItems, minSize, splitter);
//    }
//
//    /**
//     * Add a object into the tree at a location.
//     * 
//     * @param lat
//     *                up-down location in QuadTree Grid (latitude, y)
//     * @param lon
//     *                left-right location in QuadTree Grid (longitude, x)
//     * @return true if the insertion worked.
//     * @throws RuntimeException
//     *                 in case the leaf's lat/lng lies outside of the node's bounds. This would
//     *                 typically be caused by the point being outside the whole quad tree's bounds.
//     */
//    public void put(T obj) {
//	getTop().put(obj);
//    }
//
//    /**
//     * Remove a object out of the tree at a location.
//     * 
//     * @param lat
//     *                up-down location in QuadTree Grid (latitude, y)
//     * @param lon
//     *                left-right location in QuadTree Grid (longitude, x)
//     * @return the object removed, null if the object not found.
//     */
//    public T remove(T obj) {
//	return getTop().remove(obj);
//    }
//
//    /** Clear the tree. */
//    public void clear() {
//	getTop().clear();
//    }
//
//    /**
//     * Get an object closest to a lat/lon.
//     * 
//     * @param lat
//     *                up-down location in QuadTree Grid (latitude, y)
//     * @param lon
//     *                left-right location in QuadTree Grid (longitude, x)
//     * @return the object that was found.
//     */
//    public T get(Point point) {
//	return getTop().get(point);
//    }
//
//    /**
//     * Get an object closest to a lat/lon, within a maximum distance.
//     * 
//     * @param lat
//     *                up-down location in QuadTree Grid (latitude, y)
//     * @param lon
//     *                left-right location in QuadTree Grid (longitude, x)
//     * @param withinDistance
//     *                maximum get distance. The distance is given as the square root of the sum of
//     *                the squares of the x and y differences, respectively. It
//     *                therefore does not correspond to any distance in meters or any euclidian
//     *                distance at all. However, it should be good enough (at least outside the polar
//     *                regions, and in particular for smaller regions), and in particular to find
//     *                <em>minimum</em> distances.
//     * @return the object that was found, null if nothing is within the maximum distance.
//     */
//    public T get(Point point, double withinDistance) {
//	return getTop().get(point, withinDistance);
//    }
//
//    /**
//     * Get all the objects within a bounding box.
//     * 
//     * @return Vector of objects.
//     */
//    public Collection<T> get(Bounds rect) {
//	return get(rect, new Vector<T>());
//    }
//
//    /**
//     * Get all the objects within a bounding box, and return the objects within a given Vector.
//     * 
//     * @param vector
//     *                a vector to add objects to.
//     * @return Vector of objects.
//     */
//    private Collection<T> get(Bounds rect, Collection<T> vector) {
//
//	if (vector == null) {
//	    vector = new Vector<T>();
//	}
//	return getTop().get(rect, vector);
//    }
//
//    protected QuadTreeNodeForBounds<S, T> getTop() {
//	return top;
//    }
//
//    /**
//     * Calculates an approximated "distance" between two lat/lng points by interpreting the
//     * coordinates as a euclidian and doing the "sqrt thing"
//     */
//    public static double getLatLngDistance(GLatLng a, GLatLng b) {
//	double distance = Math.sqrt((a.lat() - b.lat()) * (a.lat() - b.lat()) + (a.lng() - b.lng())
//		* (a.lng() - b.lng()));
//	return distance;
//    }
//
//    protected Splitter<S> getSplitter() {
//        return splitter;
//    }
//
}
