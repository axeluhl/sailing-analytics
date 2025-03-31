package com.sap.sse.util.graph.impl;

import com.sap.sse.util.graph.DirectedEdge;

public class DirectedEdgeImpl<T> implements DirectedEdge<T> {
    private final T from;
    private final T to;
    
    public DirectedEdgeImpl(T from, T to) {
        super();
        this.from = from;
        this.to = to;
    }

    @Override
    public T getFrom() {
        return from;
    }

    @Override
    public T getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "(" + from + " -> " + to + ")";
    }
}
