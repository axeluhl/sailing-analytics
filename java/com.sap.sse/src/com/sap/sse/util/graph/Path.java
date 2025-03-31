package com.sap.sse.util.graph;

public interface Path<T> extends Iterable<T> {
    Iterable<T> getNodes();
    boolean contains(T node);
    boolean contains(DirectedEdge<T> edge);
    Path<T> subPath(T start);
    Path<T> subPath(T start, T endExclusive);
    
    /**
     * Returns a new path that has all nodes of this path with {@code tail} appended at the end
     */
    Path<T> extend(T tail);
    
    /**
     * Constructs a new path that contains this path's nodes and then {@code subPath}'s nodes appended
     * to them
     */
    Path<T> extend(Path<T> subPath);

    /**
     * The first node in this path, or {@code null} if this path is empty
     */
    T head();
    
    /**
     * A new path that has all elements but the first of this one; empty if this path has zero or
     * one element.
     */
    Path<T> tail();
    
    /**
     * The last node in this path, or {@code null} if this path is empty
     */
    T last();
    boolean isEmpty();
    
    /**
     * Do this and the other path have one or more nodes in common?
     */
    boolean intersects(Path<T> other);
}
