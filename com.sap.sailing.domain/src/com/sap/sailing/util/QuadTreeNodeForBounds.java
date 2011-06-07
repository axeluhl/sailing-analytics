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
// $Revision: 101 $
// $Date: 2008-07-09 23:09:42 +0200 (Wed, 09 Jul 2008) $
// $Author: axel.uhl $
// 
// **********************************************************************

package com.sap.sailing.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 * The QuadTreeNodeForBounds is the part of the QuadTreeForBounds that either holds children nodes, or objects as
 * leaves. The nodes that have children may hold items that span across children boundaries, since
 * this was designed to handle bounds-defined data.
 * 
 * @param <T> the type of node to manage in this tree; can be anything; however, a
 * {@link Splitter<T>} needs to be provided that knows whether an object of type <tt>T</tt>
 * intersects a {@link Bounds} and what the distance of an object of type <tt>T</tt> is to
 * a {@link Point}.
 * 
 * @param <S> the type for the splitter arguments; <tt>T</tt> must conform to this type
 * @param <T> the type of nodes to be stored in the tree
 */
public class QuadTreeNodeForBounds<S, T extends S> implements Serializable {

    static final long serialVersionUID = -6111633198469889444L;

    private final static int NORTHWEST = 0;
    private final static int NORTHEAST = 1;
    private final static int SOUTHEAST = 2;
    private final static int SOUTHWEST = 3;
    public final static int NO_MIN_SIZE = 1;

    private Vector<T> items;
    private QuadTreeNodeForBounds<S, T>[] children;
    private int maxItems;
    private int minSize;
    private Bounds bounds;
    private Splitter<S> splitter;

    /**
     * Constructor to use if you are going to store the objects in lat/lon space, and there is
     * really no smallest node size.
     * 
     * @param maximumItems
     *                number of items to hold in a node before splitting itself into four children
     *                and redispensing the items into them.
     */
    public QuadTreeNodeForBounds(Bounds rect, int maximumItems, Splitter<S> splitter) {
	this(rect, maximumItems, NO_MIN_SIZE, splitter);
    }

    /**
     * Constructor to use if you are going to store the objects in x/y space, and there is a
     * smallest node size because you don't want the nodes to be smaller than a group of pixels.
     * 
     * @param north
     *                northern border of node coverage.
     * @param west
     *                western border of node coverage.
     * @param south
     *                southern border of node coverage.
     * @param east
     *                eastern border of node coverage.
     * @param maximumItems
     *                number of items to hold in a node before splitting itself into four children
     *                and redispensing the items into them.
     * @param minimumSize
     *                the minimum difference between the boundaries of the node.
     */
    public QuadTreeNodeForBounds(Bounds rect, int maximumItems, int minimumSize, Splitter<S> splitter) {
	bounds = rect;
	maxItems = maximumItems;
	if (minimumSize < 1) {
	    throw new RuntimeException("Minimum cell size is 1; requested minimumSize "+minimumSize+" not supported.");
	}
	minSize = minimumSize;
	items = new Vector<T>();
	this.splitter = splitter;
    }

    /** Return true if the node has children. */
    public boolean hasChildren() {
	if (children != null)
	    return true;
	else
	    return false;
    }

    /**
     * This method splits the node into four children, and disperses the items into the children.
     * The split only happens if the boundary size of the node is larger than the minimum size (if
     * we care). The items in this node are cleared after they are put into the children, fulfilling
     * again the invariant that a node either holds items by itself or has child nodes.
     */
    @SuppressWarnings("unchecked") // because of the array of QuadTreeNodeForBounds w/o template arguments
    protected void split() {
	int nsHalf = (bounds.getNE().getY() + bounds.getSW().getY()) / 2;
	int ewHalf = (bounds.getNE().getX() + bounds.getSW().getX()) / 2;
	children = new QuadTreeNodeForBounds[4];

	children[NORTHWEST] = new QuadTreeNodeForBounds<S, T>(new BoundsImpl(new PointImpl(bounds.getSW().getX(),
		nsHalf), new PointImpl(ewHalf, bounds.getNE().getY())),
		maxItems, getSplitter());
	children[NORTHEAST] = new QuadTreeNodeForBounds<S, T>(new BoundsImpl(new PointImpl(ewHalf,
		nsHalf), bounds.getNE()), maxItems, getSplitter());
	children[SOUTHEAST] = new QuadTreeNodeForBounds<S, T>(new BoundsImpl(new PointImpl(ewHalf,
		bounds.getSW().getY()), new PointImpl(bounds.getNE().getX(), nsHalf)),
		maxItems, getSplitter());
	children[SOUTHWEST] = new QuadTreeNodeForBounds<S, T>(new BoundsImpl(bounds.getSW(),
		new PointImpl(ewHalf, nsHalf)), maxItems, getSplitter());
	for (Iterator<T> i = items.iterator(); i.hasNext();) {
	    T t = i.next();
	    i.remove();
	    put(t);
	}
    }

    /**
     * Get the child nodes from the tree's splitter that really intersect with a given object
     * 
     * @return a set of nodes from the direct and transitive children that intersect with the
     * object represented by the leaf, or <tt>null</tt> if the leaf does not intersect this node at all.
     */
    protected Set<QuadTreeNodeForBounds<S, T>> getChildren(T leaf) {
	Set<QuadTreeNodeForBounds<S, T>> result = null;
	if (splitter.intersects(leaf, this.getBounds())) {
	    result = new HashSet<QuadTreeNodeForBounds<S, T>>();
	    if (children != null) {
		for (int i = 0; i < children.length; i++) {
		    if (splitter.intersects(leaf, children[i].getBounds())) {
			result.addAll(children[i].getChildren(leaf));
		    }
		}
	    } else {
		result.add(this);
	    }
	}
	return result;
    }

    /**
     * Add an object with bounds into the tree. Splits if necessary (more than {@link #maxItems}
     * items in this cell) and possible (size in both directions >= 2 pixels or {@link #minSize}).
     * 
     * @param leaf
     *                object-location composite
     * @return true if the pution worked.
     * @throws RuntimeException
     *                 in case the leaf's lat/lng doesn't intersect the node's bounds. This would
     *                 typically be caused by the point being outside the whole quad tree's bounds.
     */
    public void put(T leaf) {
	if (children == null) {
	    items.addElement(leaf);
	    // Make sure we're bigger than the minimum, if we care,
	    if (items.size() > maxItems
		    && Math.abs(bounds.getNE().getY() - bounds.getSW().getY()) >= 2*minSize
		    && Math.abs(bounds.getNE().getX() - bounds.getSW().getX()) >= 2*minSize) {
		split();
	    }
	} else {
	    Set<QuadTreeNodeForBounds<S, T>> nodes = getChildren(leaf);
	    if (nodes != null) {
		for (QuadTreeNodeForBounds<S, T> node:nodes) {
		    node.put(leaf);
		}
	    } else {
		throw new RuntimeException("leaf " + leaf + " not contained in bounds (("
			+ bounds.getSW().getY() + ", " + bounds.getSW().getX() + "), ("
			+ bounds.getNE().getY() + ", " + bounds.getNE().getX() + "))");
	    }
	}
    }

    /**
     * Remove a QuadTreeLeafForBounds out of the tree at a location.
     * 
     * @param leaf
     *                object-location composite
     * @return the object removed, null if the object was not found.
     */
    public T remove(T leaf) {
	T removed = null;
	if (children == null) {
	    // This must be the node that has it...
	    for (int i = 0; i < items.size(); i++) {
		if (items.remove(leaf)) {
		    removed = leaf;
		}
	    }
	} else {
	    Set<QuadTreeNodeForBounds<S, T>> nodes = getChildren(leaf);
	    if (nodes != null) {
		for (QuadTreeNodeForBounds<S, T> node:nodes) {
		    T removedFromChild = node.remove(leaf);
		    if (removedFromChild != null) {
			removed = removedFromChild;
		    }
		}
	    }
	}
	return removed;
    }

    /** Clear the tree below this node. */
    public void clear() {
	this.items.removeAllElements();
	if (children != null) {
	    for (int i = 0; i < children.length; i++) {
		children[i].clear();
	    }
	    children = null;
	}
    }

    /**
     * Get an object closest to a <tt>point</tt>.
     * 
     * @param lat
     *                up-down location in QuadTree Grid (latitude, y)
     * @param lon
     *                left-right location in QuadTree Grid (longitude, x)
     * @return the object that matches the best distance, null if no object was found.
     */
    public T get(Point point) {
	return get(point, Double.POSITIVE_INFINITY);
    }

    /**
     * Get an object closest to a <tt>point</tt>. If there are children at this node, then the
     * children are searched. The children are checked first, to see if they are closer than the
     * best distance already found. If a closer object is found, bestDistance will be updated with a
     * new Double object that has the new distance.
     * 
     * @param lat
     *                up-down location in QuadTree Grid (latitude, y)
     * @param lon
     *                left-right location in QuadTree Grid (longitude, x)
     * @param withinDistance
     *                maximum get distance. The distance is given as the square root of the sum of
     *                the squares of the latitude and longitude differences, respectively. It
     *                therefore does not correspond to any distance in meters or any euclidian
     *                distance at all. However, it should be good enough (at least outside the polar
     *                regions, and in particular for smaller regions), and in particular to find
     *                <em>minimum</em> distances.
     * @return the object that matches the best distance, null if no closer object was found.
     */
    public T get(Point point, double withinDistance) {
	return get(point, new MutableDistance(withinDistance));
    }

    /**
     * Get an object closest to a <tt>point</tt>. If there are children at this node, then the
     * children are searched. The children are checked first, to see if they are closer than the
     * best distance already found. If a closer object is found, bestDistance will be updated with a
     * new Double object that has the new distance.
     * 
     * @param point
     *                location in QuadTree Grid
     * @param bestDistance
     *                the closest distance of the object found so far. The distance is given as the
     *                square root of the sum of the squares of the latitude and longitude
     *                differences, respectively. It therefore does not correspond to any distance in
     *                meters or any euclidian distance at all. However, it should be good enough (at
     *                least outside the polar regions, and in particular for smaller regions), and
     *                in particular to find <em>minimum</em> distances.
     * 
     * @return the object that matches the best distance, null if no closer object was found.
     */
    public T get(Point point, MutableDistance bestDistance) {
	T closest = null;
	if (children == null) {
	    // This must be the node that has it...
	    for (T qtl : items) {
		double distance = splitter.getDistance(point, qtl);
		if (distance < bestDistance.value) {
		    bestDistance.value = distance;
		    closest = qtl;
		}
	    }
	    return closest;
	} else {
	    // Check the distance of the bounds of the children,
	    // versus the bestDistance. If there is a boundary that
	    // is closer, then it is possible that another node has an
	    // object that is closer.
	    for (int i = 0; i < children.length; i++) {
		double childDistance = borderDistance(children[i].bounds, point);
		if (childDistance < bestDistance.value) {
		    T test = children[i].get(point, bestDistance);
		    if (test != null)
			closest = test;
		}
	    }
	}
	return closest;
    }

    /**
     * A utility method to figure out the closest distance of a bound's border to a point. If the
     * point is inside the bounds, return 0.
     * 
     * @return closest distance to the point.
     */
    private static double borderDistance(Bounds bounds, Point point) {

	double nsdistance;
	double ewdistance;

	if (bounds.getSW().getY() <= point.getY()
		&& point.getY() < bounds.getNE().getY()) {
	    nsdistance = 0;
	} else {
	    nsdistance = Math.min((Math.abs(point.getY() - bounds.getNE().getY())), (Math
		    .abs(point.getY() - bounds.getSW().getY())));
	}

	if (bounds.getSW().getX() <= point.getX()
		&& point.getX() < bounds.getNE().getX()) {
	    ewdistance = 0;
	} else {
	    ewdistance = Math.min((Math.abs(point.getX() - bounds.getNE().getX())), (Math
		    .abs(point.getX() - bounds.getSW().getX())));
	}

	double distance = Math.sqrt(nsdistance * nsdistance + ewdistance * ewdistance);
	return distance;
    }

    /**
     * Get all the objects intersecting with a bounding box.
     * 
     * @param rect
     *                boundary of area to fill.
     * @param vector
     *                current vector of objects.
     * @return updated Vector of objects.
     */
    public Collection<T> get(Bounds rect, Collection<T> vector) {
	if (children == null) {
	    for (Iterator<T> i = items.iterator(); i.hasNext();) {
		T qtl = i.next();
		if (splitter.intersects(qtl, rect)) {
		    vector.add(qtl);
		}
	    }
	} else {
	    for (int i = 0; i < children.length; i++) {
		if (children[i].bounds.intersects(rect)) {
		    children[i].get(rect, vector);
		}
	    }
	}
	return vector;
    }

    public Bounds getBounds() {
        return bounds;
    }

    private Splitter<S> getSplitter() {
        return splitter;
    }
    
    public String toString() {
	StringBuilder sb = new StringBuilder("Node with ");
	if (children == null) {
	    sb.append(items.size());
	    sb.append(" leaves");
	} else {
	    sb.append("four children");
	}
	return sb.toString();
    }
}