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
import java.util.Iterator;
import java.util.Vector;

/**
 * The QuadTreeNode is the part of the QuadTree that either holds
 * children nodes, or objects as leaves. Currently, the nodes that
 * have children do not hold items that span across children
 * boundaries, since this was designed to handle point data.
 */
public class QuadTreeNode<T> implements Serializable {

    static final long serialVersionUID = -6111633198469889444L;

    private final static int NORTHWEST = 0;
    private final static int NORTHEAST = 1;
    private final static int SOUTHEAST = 2;
    private final static int SOUTHWEST = 3;
    public final static double NO_MIN_SIZE = -1;

    private Vector<QuadTreeLeaf<T>> items;
    private QuadTreeNode<T>[] children;
    private int maxItems;
    private double minSize;
    private GLatLngBounds bounds;
    
    /**
     * Added to avoid problems when a node is completely filled with a
     * single point value.
     */
    private boolean allTheSamePoint;
    private GLatLng firstPoint;

    /**
     * Constructor to use if you are going to store the objects in
     * lat/lon space, and there is really no smallest node size.
     * 
     * @param maximumItems number of items to hold in a node before
     *        splitting itself into four children and redispensing the
     *        items into them.
     */
    public QuadTreeNode(GLatLngBounds rect, int maximumItems) {
        this(rect, maximumItems, NO_MIN_SIZE);
    }

    /**
     * Constructor to use if you are going to store the objects in x/y
     * space, and there is a smallest node size because you don't want
     * the nodes to be smaller than a group of pixels.
     * 
     * @param north northern border of node coverage.
     * @param west western border of node coverage.
     * @param south southern border of node coverage.
     * @param east eastern border of node coverage.
     * @param maximumItems number of items to hold in a node before
     *        splitting itself into four children and redispensing the
     *        items into them.
     * @param minimumSize the minimum difference between the
     *        boundaries of the node.
     */
    public QuadTreeNode(GLatLngBounds rect, int maximumItems, double minimumSize) {
        bounds = rect;
        maxItems = maximumItems;
        minSize = minimumSize;
        items = new Vector<QuadTreeLeaf<T>>();
    }

    /** Return true if the node has children. */
    public boolean hasChildren() {
        if (children != null)
            return true;
        else
            return false;
    }

    /**
     * This method splits the node into four children, and disperses
     * the items into the children. The split only happens if the
     * boundary size of the node is larger than the minimum size (if
     * we care). The items in this node are cleared after they are put
     * into the children.
     */
    @SuppressWarnings("unchecked")
	protected void split() {
        // Make sure we're bigger than the minimum, if we care,
        if (minSize != NO_MIN_SIZE) {
            if (Math.abs(bounds.getNorthEast().lat() - bounds.getSouthWest().lat()) < minSize
                    && Math.abs(bounds.getNorthEast().lng() - bounds.getSouthWest().lng()) < minSize)
                return;
        }

        double nsHalf = (bounds.getNorthEast().lat() + bounds.getSouthWest().lat()) / 2.0;
        double ewHalf = (bounds.getNorthEast().lng() + bounds.getSouthWest().lng()) / 2.0;
        children = new QuadTreeNode[4];

        children[NORTHWEST] = new QuadTreeNode<T>(new GLatLngBounds(new GLatLng(nsHalf, bounds.getSouthWest().lng()), new GLatLng(bounds.getNorthEast().lat(), ewHalf)), maxItems);
        children[NORTHEAST] = new QuadTreeNode<T>(new GLatLngBounds(new GLatLng(nsHalf, ewHalf), bounds.getNorthEast()), maxItems);
        children[SOUTHEAST] = new QuadTreeNode<T>(new GLatLngBounds(new GLatLng(bounds.getSouthWest().lat(), ewHalf), new GLatLng(nsHalf, bounds.getNorthEast().lng())), maxItems);
        children[SOUTHWEST] = new QuadTreeNode<T>(new GLatLngBounds(bounds.getSouthWest(), new GLatLng(nsHalf, ewHalf)), maxItems);
        Vector<QuadTreeLeaf<T>> temp = new Vector<QuadTreeLeaf<T>>(items);
        items.removeAllElements();
        for (Iterator<QuadTreeLeaf<T>> i=temp.iterator(); i.hasNext(); ) {
            put(i.next());
        }
        //items.removeAllElements();
    }

    /**
     * Get the node that covers a certain lat/lon pair.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return node if child covers the point, null if the point is
     *         out of range.
     */
    protected QuadTreeNode<T> getChild(GLatLng point) {
        if (bounds.contains(point)) {
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    if (children[i].bounds.contains(point))
                        return children[i].getChild(point);
                }
            } else
                return this; // no children, lat, lon here...
        }
        return null;
    }

    /**
     * Add a object into the tree at a location.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @param obj object to add to the tree.
     * @return true if the pution worked.
     * @throws RuntimeException in case the leaf's lat/lng lies outside of the node's bounds.
     * This would typically be caused by the point being outside the whole quad tree's bounds.
     */
    public void put(GLatLng point, T obj) {
        put(new QuadTreeLeaf<T>(point, obj));
    }

    /**
     * Add a QuadTreeLeaf into the tree at a location.
     * 
     * @param leaf object-location composite
     * @return true if the pution worked.
     * @throws RuntimeException in case the leaf's lat/lng lies outside of the node's bounds.
     * This would typically be caused by the point being outside the whole quad tree's bounds.
     */
    public void put(QuadTreeLeaf<T> leaf) {
        if (children == null) {
            this.items.addElement(leaf);
            if (this.items.size() == 1) {
                this.allTheSamePoint = true;
                this.firstPoint = leaf.getPoint();
            } else {
                if (!this.firstPoint.equals(leaf.getPoint())) {
                    this.allTheSamePoint = false;
                }
            }

            if (this.items.size() > maxItems && !this.allTheSamePoint)
                split();
        } else {
            QuadTreeNode<T> node = getChild(leaf.getPoint());
            if (node != null) {
                node.put(leaf);
            } else {
            	throw new RuntimeException("leaf "+leaf+" not contained in bounds (("+
            			bounds.getSouthWest().lat()+", "+bounds.getSouthWest().lng()+"), ("+
            			bounds.getNorthEast().lat()+", "+bounds.getNorthEast().lng()+"))");
            }
        }
    }

    /**
     * Remove a object out of the tree at a location.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return the object removed, null if the object not found.
     */
    public T remove(GLatLng point, T obj) {
        return remove(new QuadTreeLeaf<T>(point, obj));
    }

    /**
     * Remove a QuadTreeLeaf out of the tree at a location.
     * 
     * @param leaf object-location composite
     * @return the object removed, null if the object not found.
     */
    public T remove(QuadTreeLeaf<T> leaf) {
        if (children == null) {
            // This must be the node that has it...
            for (int i = 0; i < items.size(); i++) {
                QuadTreeLeaf<T> qtl = items.elementAt(i);
                if (leaf.getObject() == qtl.getObject()) {
                    items.removeElementAt(i);
                    return qtl.getObject();
                }
            }
        } else {
            QuadTreeNode<T> node = getChild(leaf.getPoint());
            if (node != null) {
                return node.remove(leaf);
            }
        }
        return null;
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
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return the object that matches the best distance, null if no
     *         object was found.
     */
    public T get(GLatLng point) {
        return get(point, Double.POSITIVE_INFINITY);
    }

    /**
     * Get an object closest to a <tt>point</tt>. If there are children at
     * this node, then the children are searched. The children are
     * checked first, to see if they are closer than the best distance
     * already found. If a closer object is found, bestDistance will
     * be updated with a new Double object that has the new distance.
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
     * @return the object that matches the best distance, null if no
     *         closer object was found.
     */
    public T get(GLatLng point, double withinDistance) {
        return get(point, new MutableDistance(withinDistance));
    }

    /**
     * Get an object closest to a <tt>point</tt>. If there are children at
     * this node, then the children are searched. The children are
     * checked first, to see if they are closer than the best distance
     * already found. If a closer object is found, bestDistance will
     * be updated with a new Double object that has the new distance.
     * @param point location in QuadTree Grid
     * @param bestDistance the closest distance of the object found so
     *        far. The distance is given as the square root of the sum of
     *        the squares of the latitude and longitude differences, respectively.
     *        It therefore does not correspond to any distance in meters or
     *        any euclidian distance at all. However, it should be good enough
     *        (at least outside the polar regions, and in particular for smaller
     *        regions), and in particular to find <em>minimum</em> distances.
     * 
     * @return the object that matches the best distance, null if no
     *         closer object was found.
     */
    public T get(GLatLng point, MutableDistance bestDistance) {
        T closest = null;
        if (children == null) {
            // This must be the node that has it...
        	for (QuadTreeLeaf<T> qtl:items) {
                double distance = QuadTree.getLatLngDistance(point, qtl.getPoint());

                if (distance < bestDistance.value) {
                    bestDistance.value = distance;
                    closest = qtl.getObject();
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
     * A utility method to figure out the closest distance of a bound's border
     * to a point. If the point is inside the bounds, return 0.
     * 
     * @return closest distance to the point.
     */
    private static double borderDistance(GLatLngBounds bounds, GLatLng point) {

        double nsdistance;
        double ewdistance;

        if (bounds.getSouthWest().lat() <= point.lat() && point.lat() <= bounds.getNorthEast().lat()) {
            nsdistance = 0;
        } else {
            nsdistance = Math.min((Math.abs(point.lat() - bounds.getNorthEast().lat())), (Math.abs(point.lat()
                    - bounds.getSouthWest().lat())));
        }

        if (bounds.getSouthWest().lng() <= point.lng() && point.lng() <= bounds.getNorthEast().lng()) {
            ewdistance = 0;
        } else {
            ewdistance = Math.min((Math.abs(point.lng() - bounds.getNorthEast().lng())),
                    (Math.abs(point.lng() - bounds.getSouthWest().lng())));
        }

        double distance = Math.sqrt(nsdistance*nsdistance + ewdistance*ewdistance);
        return distance;
    }

    /**
     * Get all the objects within a bounding box.
     * 
     * @param rect boundary of area to fill.
     * @param vector current vector of objects.
     * @return updated Vector of objects.
     */
    public Collection<T> get(GLatLngBounds rect, Collection<T> vector) {
        if (children == null) {
            for (Iterator<QuadTreeLeaf<T>> i=items.iterator(); i.hasNext(); ) {
            	QuadTreeLeaf<T> qtl = i.next();
                if (rect.contains(new GLatLng(qtl.getPoint().lat(), qtl.getPoint().lng()))) {
                    vector.add(qtl.getObject());
                }
            }
        } else {
            for (int i = 0; i < children.length; i++) {
                if (children[i].bounds.containsBounds(rect)) {
                    children[i].get(rect, vector);
                }
            }
        }
        return vector;
    }
}