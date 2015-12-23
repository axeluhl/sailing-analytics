// **********************************************************************
//
// <copyright>
//
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
//
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
//
// </copyright>
// **********************************************************************
//
// $Source$
// $RCSfile$
// $Revision: 184 $
// $Date: 2008-09-16 16:17:21 +0200 (Tue, 16 Sep 2008) $
// $Author: axel.uhl $
//
// **********************************************************************

package com.sap.sailing.domain.common.quadtree;

import java.io.Serializable;
import java.util.Collection;
import java.util.Vector;

import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.BoundsImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.quadtree.impl.QuadTreeNode;

/**
 * A spatial data structure that provides efficient (O(log n)) access to nearest neighbors and
 * to objects in a certain radius around another object. The location of objects is provided as
 * {@link Position}.
 * 
 * @param <T>
 *            type of object stored by coordinates
 * @author Axel Uhl (D043530)
 */
public class QuadTree<T> implements Serializable {

    static final long serialVersionUID = -7707825592455579873L;

    private QuadTreeNode<T> top;
    
    public QuadTree() {
        this(new BoundsImpl(new DegreePosition(-90.0, -180.0), new DegreePosition(90.0, 180.0)), 20, QuadTreeNode.NO_MIN_SIZE);
    }

    public QuadTree(Position southWest, Position northEast, int maxItems) {
        this(new BoundsImpl(southWest, northEast), maxItems, QuadTreeNode.NO_MIN_SIZE);
    }

    private QuadTree(Bounds bounds, int maxItems, double minSize) {
        top = new QuadTreeNode<T>(bounds, maxItems, minSize);
    }

    /**
     * Add a object into the tree at a location.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return true if the insertion worked.
     * @throws RuntimeException in case the leaf's lat/lng lies outside of the node's bounds.
     * This would typically be caused by the point being outside the whole quad tree's bounds.
     */
    public void put(Position point, T obj) {
        getTop().put(point, obj);
    }

    /**
     * Remove a object out of the tree at a location.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return the object removed, null if the object not found.
     */
    public T remove(Position point, T obj) {
        return getTop().remove(point, obj);
    }
    
    public void replace(Position point, T newObj) {
        getTop().replace(point, newObj);
    }

    /** Clear the tree. */
    public void clear() {
        getTop().clear();
    }

    /**
     * Get an object closest to a lat/lon.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return the object that was found.
     */
    public T get(Position point) {
        return getTop().get(point);
    }

    /**
     * Get an object closest to a lat/lon, within a maximum distance.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @param withinDistance maximum get distance. The distance is given
     *        as the square root of the sum of
     *        the squares of the latitude and longitude differences, respectively.
     *        It therefore does not correspond to any distance in meters or
     *        any euclidian distance at all. However, it should be good enough
     *        (at least outside the polar regions, and in particular for smaller
     *        regions), and in particular to find <em>minimum</em> distances.
     * @return the object that was found, null if nothing is within
     *         the maximum distance.
     */
    public T get(Position point, double withinDistance) {
        return getTop().get(point, withinDistance);
    }

    /**
     * Get all the objects within a bounding box.
     * 
     * @return Vector of objects.
     */
    public Collection<T> get(Bounds rect) {
        return get(rect, new Vector<T>());
    }

    /**
     * Get all the objects within a bounding box, and return the
     * objects within a given Vector.
     * 
     * @param vector a vector to add objects to.
     * @return Vector of objects.
     */
    private Collection<T> get(Bounds rect, Collection<T> vector) {

        if (vector == null) {
            vector = new Vector<T>();
        }
        // crossing the dateline, right?? Or at least containing the
        // entire earth. Might be trouble for VERY LARGE scales. The
        // last check is for micro-errors that happen to lon points
        // where there might be a smudge overlap for very small
        // scales.
        if (rect.getSouthWest().getLngDeg() > rect.getNorthEast().getLngDeg() || (Math.abs(rect.getSouthWest().getLngDeg() - rect.getNorthEast().getLngDeg()) < .001)) {
            return getTop().get(new BoundsImpl(rect.getSouthWest(), new DegreePosition(rect.getNorthEast().getLatDeg(), 180)),
                   getTop().get(new BoundsImpl(new DegreePosition(rect.getSouthWest().getLatDeg(), -180), rect.getNorthEast()), vector));
        } else
            return getTop().get(rect, vector);
    }

    private QuadTreeNode<T> getTop() {
        return top;
    }

    /**
     * Calculates an approximated "distance" between two lat/lng points by interpreting the coordinates as a euclidian
     * and doing the "sqrt thing"
     */
    public static double getLatLngDistance(Position a, Position b) {
        double distance = Math.sqrt((a.getLatDeg() - b.getLatDeg()) * (a.getLatDeg() - b.getLatDeg()) + (a.getLngDeg() - b.getLngDeg())
                * (a.getLngDeg() - b.getLngDeg()));
        return distance;
    }
}