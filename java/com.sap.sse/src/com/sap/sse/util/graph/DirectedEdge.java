package com.sap.sse.util.graph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface DirectedEdge<T> extends Edge<T> {
    T getFrom();
    T getTo();
    default Set<T> getNodes() { return new HashSet<>(Arrays.asList(getFrom(), getTo())); }     
}
