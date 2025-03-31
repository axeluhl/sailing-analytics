package com.sap.sse.util.graph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.util.graph.impl.DirectedEdgeImpl;

public interface DirectedEdge<T> extends Edge<T> {
    static <T> DirectedEdge<T> create(T from, T to) {
        return new DirectedEdgeImpl<T>(from, to);
    }
    
    T getFrom();
    T getTo();
    default Set<T> getNodes() { return new HashSet<>(Arrays.asList(getFrom(), getTo())); }     
}
