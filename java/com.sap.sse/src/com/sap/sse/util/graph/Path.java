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
    
    T head();
    T tail();
    boolean isEmpty();
    
    /**
     * Do this and the other path have one or more nodes in common?
     */
    boolean intersects(Path<T> other);
}
