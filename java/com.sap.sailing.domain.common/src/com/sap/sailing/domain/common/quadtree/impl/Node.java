package com.sap.sailing.domain.common.quadtree.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.BoundsImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.quadtree.QuadTree;

/**
 * A node in a {@link QuadTree}. There may be internal nodes that have no elements in them but have exactly
 * four child nodes, or leaf nodes that have no children but contain item elements.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public class Node<T> {
    /**
     * Either an array of exactly four child nodes which are then all non-<code>null</code>, or <code>null</code>,
     * meaning that this node is a child node, having no children but potentially having items. A node that has children
     * has no items. Quadrants are numbered as usual in geometry. See {@link #NE}, {@link #NW}, {@link #SW} and
     * {@link #SE}.
     */
    private Node<T>[] children;
    
    private Map<Position, T> items;
    
    private final Bounds bounds;
    
    /**
     * The maximum number of items to hold in {@link #items}. If {@link #put(Position, Object) adding} an item to this node
     * would increase the item collection's size beyond this number, the node is {@link #split() split} into four new leaf
     * nodes, and its items are distributed across those new leaf nodes. This number must be a positive integer.
     */
    private final int maxItems;
    
    /**
     * The quadrant index in {@link #children} for the north-east subtree
     */
    private final int NE = 0;

    /**
     * The quadrant index in {@link #children} for the north-west subtree
     */
    private final int NW = 1;
    
    /**
     * The quadrant index in {@link #children} for the south-west subtree
     */
    private final int SW = 2;
    
    /**
     * The quadrant index in {@link #children} for the south-east subtree
     */
    private final int SE = 3;
    
    /**
     * Creates a new node with the <code>bounds</code> as specified. The node starts out empty, as a leaf node that has
     * an empty set of items.
     * 
     * @param bounds
     *            must not have a north latitude less than the south latitude, or an {@link IllegalArgumentException}
     *            will result
     */
    public Node(Bounds bounds, int maxItems) {
        if (bounds.getNorthEast().getLatDeg() < bounds.getSouthEast().getLatDeg()) {
            throw new IllegalArgumentException("North border of bounds "+bounds+" is further south than its south border");
        }
        if (maxItems <= 0) {
            throw new IllegalArgumentException("Maximum number of items must be positive but was "+maxItems);
        }
        this.maxItems = maxItems;
        this.bounds = bounds;
        items = new HashMap<>(maxItems);
    }

    private void split() {
        assert children == null;
        assert items != null;
        createChildren();
        distributeItemsToChildren();
        assert children != null;
        assert items == null;
    }

    private void distributeItemsToChildren() {
        assert items != null;
        assert children != null;
        for (final Entry<Position, T> item : items.entrySet()) {
            getChild(item.getKey()).put(item.getKey(), item.getValue());
        }
        assert items == null;
    }

    private Node<T> getChild(Position key) {
        assert children != null;
        assert items == null;
        assert bounds.contains(key);
        for (final Node<T> child : children) {
            if (child.bounds.contains(key)) {
                return child;
            }
        }
        throw new RuntimeException("Internal error: position "+key+" is within node bounds "+bounds+" but no child contains it");
    }
    
    /**
     * Adds the <code>value</code> to this node and ensures that the node still meets the requirements regarding size.
     * If necessary, the node is split with its items distributed across the new children. If a value already existed at
     * position <code>key</code>, it is replaced.
     * 
     * @param key
     *            the position at which to insert <code>value</code>. Must be contained in this node's {@link #bounds}.
     *            If it is not, an {@link IllegalArgumentException} will be thrown. Must not be <code>null</code>.
     * @return the value previously at position <code>key</code> or <code>null</code> if there was no value at that
     *         position.
     */
    public T put(Position key, T value) {
        if (value == null) {
            throw new NullPointerException("Cannot insert null values into this node");
        }
        if (key == null) {
            throw new NullPointerException("null keys not allowed");
        }
        if (!bounds.contains(key)) {
            throw new IllegalArgumentException("key "+key+" must be within this node's bounds "+bounds);
        }
        final T result;
        if (items != null) {
            result = items.put(key, value);
            if (result == null) {
                // the size of this node has increased by one; check size constraint
                if (items.size() > maxItems) {
                    split();
                }
            }
        } else {
            result = getChild(key).put(key, value);
        }
        return result;
    }
    
    /**
     * Removes the element at position <code>key</code> from this node or any child nodes if such an element exists
     * 
     * @return the value removed, or <code>null</code> if no element existed at position <code>key</code> in this node
     *         or any of its children
     */
    public T remove(Position key) {
        final T result;
        if (key != null) {
            if (items != null) {
                result = items.remove(key);
            } else {
                result = getChild(key).remove(key);
            }
        } else {
            result = null;
        }
        return result;
    }

    private void createChildren() {
        assert children == null;
        @SuppressWarnings("unchecked")
        final Node<T>[] newChildren = (Node<T>[]) new Node<?>[4];
        children = newChildren;
        final Position middleWest = new DegreePosition((bounds.getSouthWest().getLatDeg() + bounds.getNorthEast().getLatDeg())/2.,
                bounds.getSouthWest().getLngDeg());
        final Position southCenter = new DegreePosition(bounds.getSouthWest().getLatDeg(),
                (bounds.getNorthEast().getLngDeg() + bounds.getSouthWest().getLngDeg()) / 2. -
                // adjust for date line crossing if necessary
                        bounds.getNorthEast().getLngDeg() >= bounds.getSouthWest().getLngDeg() ? 0. : 360.);
        final Position middleCenter = new DegreePosition(middleWest.getLatDeg(), southCenter.getLngDeg());
        final Position middleEast = new DegreePosition(middleWest.getLatDeg(), bounds.getNorthEast().getLngDeg());
        final Position northCenter = new DegreePosition(bounds.getNorthEast().getLatDeg(), southCenter.getLngDeg());
        children[NE] = new Node<T>(new BoundsImpl(middleCenter, bounds.getNorthEast()), maxItems);
        children[NW] = new Node<T>(new BoundsImpl(middleWest, northCenter), maxItems);
        children[SW] = new Node<T>(new BoundsImpl(bounds.getSouthWest(), middleCenter), maxItems);
        children[SE] = new Node<T>(new BoundsImpl(southCenter, middleEast), maxItems);
        assert children != null;
        assert children.length == 4;
        assert children[NE] != null && children[NW] != null && children[SW] != null && children[SE] != null;
    }

    /**
     * Remove all elements from this node by either removing all its items locally or by removing all children
     * and converting this back into a leaf node.
     */
    public void clear() {
        if (items != null) {
            items.clear();
        } else {
            items = new HashMap<>();
            children = null;
        }
    }

    /**
     * Get the value nearest to <code>point</code>. If the node is empty, <code>null</code> is returned. Distance is
     * calculated using the method {@link QuadTree#getLatLngDistance(Position, Position)} which is an approximation
     * only, based on Euklidian geometry with the latitude/longitude values.
     * <p>
     * 
     * If this is a leaf node, the nearest key by the definition above is used to determine the corresponding value and
     * return it. Otherwise, the children are traversed. For the first child, the nearest key is determined recursively.
     * Other children only need to be traversed if their bounds are closer to <code>point</code> than the key found so
     * far.
     */
    public T get(Position point) {
        // TODO Auto-generated method stub
        return null;
    }

    public T get(Position point, double withinDistance) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<T> get(Bounds rect) {
        // TODO Auto-generated method stub
        return null;
    }
}
