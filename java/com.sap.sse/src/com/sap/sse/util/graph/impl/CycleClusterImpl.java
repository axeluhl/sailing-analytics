package com.sap.sse.util.graph.impl;

import java.util.Set;

import com.sap.sse.util.graph.CycleCluster;

public class CycleClusterImpl<T> implements CycleCluster<T> {
    private final T representative;
    private final Set<T> clusterNodes;
    
    public CycleClusterImpl(T representative, Set<T> clusterNodes) {
        this.representative = representative;
        this.clusterNodes = clusterNodes;
    }

    @Override
    public T getRepresentative() {
        return representative;
    }

    @Override
    public Set<T> getClusterNodes() {
        return clusterNodes;
    }

    @Override
    public boolean contains(T node) {
        return clusterNodes.contains(node);
    }
}
